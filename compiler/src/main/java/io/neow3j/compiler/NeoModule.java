package io.neow3j.compiler;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.framework.Contract;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.objectweb.asm.tree.ClassNode;

public class NeoModule {

    Map<String, JavaClass> javaClasses = new HashMap<>();

    /**
     * Holds this module's methods, mapping from method ID to {@link NeoMethod};
     */
    Map<String, NeoMethod> methods = new HashMap<>();

    /**
     * Holds the same references to the methods as {@link NeoModule#methods} but in the order they
     * have been added to this module.
     */
    List<NeoMethod> sortedMethods = new ArrayList<>();

    /**
     * The smart contract class that this module is compiled from.
     */
    ClassNode asmSmartContractClass;

    public NeoModule(ClassNode asmSmartContractClass) throws IOException {
        this.asmSmartContractClass = asmSmartContractClass;
        loadDevpackFrameWorkJar();
        // TODO: Load contract class or JAR.
    }

    private void loadDevpackFrameWorkJar() throws IOException {
        String path = null;
        try {
            path = new File(Contract.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI())
                    .getPath();
        } catch (URISyntaxException ignore) {
        }
        loadJar(path);
    }

    private void loadJar(String jarFileName) throws IOException {
        JarFile jar = new JarFile(jarFileName);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (!jarEntry.getName().endsWith(".class")) {
                continue;
            }
            JavaClass c = new JavaClass(jarEntry.getName(), jar.getName());
            this.javaClasses.put(jarEntry.getName(), c);
        }
    }

    void addMethod(NeoMethod method) {
        methods.put(method.id, method);
        sortedMethods.add(method);
    }

    void finalizeModule() {
        int startAddress = 0;
        for (NeoMethod method : this.sortedMethods) {
            method.startAddress = startAddress;
            // At this point, the `nextAddress` should be set to one byte after the last
            // instruction byte of a method. So we can simply add this number to the current
            // start address and get the start address of the next method.
            startAddress += method.nextAddress;
        }
        for (NeoMethod method : sortedMethods) {
            for (Entry<Integer, NeoInstruction> entry : method.instructions.entrySet()) {
                NeoInstruction insn = entry.getValue();
                // Currently we're only using OpCode.CALL_L. Using CALL instead of CALL_L might
                // lead to some savings in script size but will also require shifting
                // addresses of all following instructions.
                if (insn.opcode.equals(OpCode.CALL_L)) {
                    if (!(insn.extra instanceof NeoMethod)) {
                        throw new CompilerException(
                                "Missing reference to method in CALL opcode.");
                    }
                    NeoMethod calledMethod = (NeoMethod) insn.extra;
                    int offset =
                            calledMethod.startAddress - (method.startAddress + entry.getKey());
                    insn.operand = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                            .putInt(offset).array();
                }
            }
        }
    }

    int byteSize() {
        return methods.values().stream().map(NeoMethod::byteSize).reduce(Integer::sum).get();
    }

    /**
     * Concatenates all of this module's methods together into one script. Should only be called
     * after {@link NeoModule#finalizeModule()} becuase otherwise the {@link
     * NeoModule#sortedMethods} is not yet initialized.
     */
    byte[] toByteArray() {
        ByteBuffer b = ByteBuffer.allocate(byteSize());
        sortedMethods.forEach(m -> b.put(m.toByteArray()));
        return b.array();
    }

}
