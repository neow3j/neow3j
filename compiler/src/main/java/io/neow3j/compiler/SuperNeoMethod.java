package io.neow3j.compiler;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;

import static io.neow3j.compiler.Compiler.skipToSuperCtorCall;
import static io.neow3j.utils.ClassUtils.getFullyQualifiedNameForInternalName;

/**
 * Represents a method in a NeoVM script that handles the inheritance conversion of structs.
 */
public class SuperNeoMethod extends NeoMethod {

    /**
     * Constructs a new {@code SuperNeoMethod}.
     *
     * @param asmMethod   the Java method this Neo method is converted from.
     * @param sourceClass the Java class from which this method originates.
     */
    public SuperNeoMethod(MethodNode asmMethod, ClassNode sourceClass) {
        super(asmMethod, sourceClass);
    }

    /**
     * Converts the JVM instructions of this method to neo-vm instructions.
     *
     * @param compUnit the compilation unit.
     * @throws IOException if an error occurs when reading class files.
     */
    @Override
    public void convert(CompilationUnit compUnit) throws IOException {
        AbstractInsnNode insn = getAsmMethod().instructions.get(0);
        if (getFullyQualifiedNameForInternalName(getOwnerClass().superName).equals(Object.class.getCanonicalName())) {
            insn = skipToSuperCtorCall(getAsmMethod(), getOwnerClass()).getNext();
        }
        while (insn != null) {
            insn = Compiler.handleInsn(insn, this, compUnit);
            insn = insn.getNext();
        }
        insertTryCatchBlocks();
    }

}
