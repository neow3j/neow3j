package io.neow3j.devpack.compiler;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.devpack.framework.EntryPoint;
import io.neow3j.devpack.framework.Syscall;
import io.neow3j.devpack.framework.Syscall.Syscalls;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Compiler {

    private static final Logger log = LoggerFactory.getLogger(Compiler.class);

    public static final int MAX_PARAMS_COUNT = 255;
    public static final int MAX_LOCAL_VARIABLES_COUNT = 255;
//    public static final int MAX_STATIC_FIELDS_COUNT = 255;

    private int currentNeoAddr;

    /**
     * Compiles the class with the given name to NeoVM code.
     *
     * @param name the fully qualified name of the class.
     */
    public byte[] compileClass(String name) throws IOException {
        ClassReader reader = new ClassReader(name);
        ClassNode n = new ClassNode();
        reader.accept(n, 0);
        MethodNode entryPoint = getEntryPoint(n);
        NeoMethod neoMethod = handleMethod(entryPoint);
        return neoMethod.toByteArray();
    }

    private MethodNode getEntryPoint(ClassNode n) {
        MethodNode[] entryPoints = n.methods.stream()
                .filter(m -> m.invisibleAnnotations != null &&
                        m.invisibleAnnotations.stream()
                                .anyMatch(a -> a.desc.equals(Type.getDescriptor(EntryPoint.class))))
                .toArray(MethodNode[]::new);
        if (entryPoints.length > 1) {
            throw new CompilerException("Multiple entry points found.");
        } else if (entryPoints.length == 0) {
            throw new CompilerException("No entry point found.");
        }
        return entryPoints[0];
    }

    private NeoMethod handleMethod(MethodNode method) throws IOException {
        NeoMethod neoMethod = new NeoMethod();
        addMethodBeginCode(method, neoMethod);
        for (AbstractInsnNode insn : method.instructions) {
            JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
            if (opcode == null) {
                continue;
            }
            log.info(opcode.toString());
            switch (opcode) {
                case INVOKESTATIC:
                    handleMethodInstruction(insn, neoMethod);
                    break;
                case LDC:
                    handleLoadConstant(insn, neoMethod);
                    break;
                case ICONST_M1:
                case ICONST_0:
                case ICONST_1:
                case ICONST_2:
                case ICONST_3:
                case ICONST_4:
                case ICONST_5:
                    addPushNumber(opcode.getOpcode() - 3, neoMethod);
                    break;
                case RETURN:
                case IRETURN:
                case ARETURN:
                case FRETURN:
                case DRETURN:
                case LRETURN:
                    neoMethod.addInstruction(new NeoInstruction(OpCode.RET, this.currentNeoAddr++));
                    break;
                case ASTORE:
                case ASTORE_0:
                case ASTORE_1:
                case ASTORE_2:
                case ASTORE_3:
                    addStoreLocalVariable(insn, neoMethod);
                    break;
                case ALOAD:
                case ALOAD_1:
                case ALOAD_2:
                case ALOAD_3:
                    addLoadLocalVariable(insn, neoMethod);
                    break;
                case NEWARRAY:

                default:
                    break;
            }
        }
        return neoMethod;
    }

    private void addMethodBeginCode(MethodNode method, NeoMethod neoMethod) {
        int paramCount = Type.getArgumentTypes(method.desc).length;
        int localVarCount = method.localVariables.size() - paramCount;
        if (paramCount > MAX_PARAMS_COUNT) {
            throw new CompilerException("The method has more than the max number of "
                    + "parameters.");
        }
        if (localVarCount > MAX_LOCAL_VARIABLES_COUNT) {
            throw new CompilerException("The method has more than the max number of local "
                    + "variables.");
        }
        if (paramCount + localVarCount > 0) {
            NeoInstruction neoInsn = new NeoInstruction(OpCode.INITSLOT,
                    new byte[]{(byte) localVarCount, (byte) paramCount}, currentNeoAddr);
            neoMethod.addInstruction(neoInsn);
            currentNeoAddr++;
        }
    }

    private void addLoadLocalVariable(AbstractInsnNode insn, NeoMethod neoMethod) {
        assert insn.getType() == AbstractInsnNode.VAR_INSN : "Instruction type doesn't match "
                + "opcode.";
        VarInsnNode varInsn = (VarInsnNode) insn;
        assert varInsn.var <= 255 : "Local variable index to high.";
        NeoInstruction neoInsn;
        if (varInsn.var <= 6) {
            OpCode storeCode = OpCode.get(OpCode.LDLOC0.getCode() + (byte) varInsn.var);
            neoInsn = new NeoInstruction(storeCode, this.currentNeoAddr++);
        } else {
            byte[] operand = new byte[]{(byte) varInsn.var};
            neoInsn = new NeoInstruction(OpCode.LDLOC, operand, this.currentNeoAddr);
            this.currentNeoAddr += 2;
        }
        neoMethod.addInstruction(neoInsn);
    }

    private void addStoreLocalVariable(AbstractInsnNode insn, NeoMethod neoMethod) {
        assert insn.getType() == AbstractInsnNode.VAR_INSN : "Instruction type doesn't match "
                + "opcode.";
        VarInsnNode varInsn = (VarInsnNode) insn;
        assert varInsn.var <= 255 : "Local variable index to high.";
        NeoInstruction neoInsn;
        if (varInsn.var <= 6) {
            OpCode storeCode = OpCode.get(OpCode.STLOC0.getCode() + (byte) varInsn.var);
            neoInsn = new NeoInstruction(storeCode, this.currentNeoAddr++);
        } else {
            byte[] operand = new byte[]{(byte) varInsn.var};
            neoInsn = new NeoInstruction(OpCode.STLOC, operand, this.currentNeoAddr);
            this.currentNeoAddr += 2;
        }
        neoMethod.addInstruction(neoInsn);
    }

    private void handleLoadConstant(AbstractInsnNode insn, NeoMethod neoMethod) {
        assert insn.getType() == AbstractInsnNode.LDC_INSN : "Instruction type doesn't match "
                + "opcode.";
        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
        if (ldcInsn.cst instanceof String) {
            addPushDataArray(((String) ldcInsn.cst).getBytes(UTF_8), neoMethod);
        }
        // TODO: Handle other types.
    }

    private void addPushDataArray(byte[] data, NeoMethod neoMethod) {
        byte[] insnBytes = new ScriptBuilder().pushData(data).toArray();
        byte[] operand = Arrays.copyOfRange(insnBytes, 1, insnBytes.length);
        neoMethod.addInstruction(new NeoInstruction(
                OpCode.get(insnBytes[0]), operand, this.currentNeoAddr));
        this.currentNeoAddr += insnBytes.length;
    }


    private void handleMethodInstruction(AbstractInsnNode insn, NeoMethod neoMethod)
            throws IOException {
        assert insn.getType() == AbstractInsnNode.METHOD_INSN : "Instruction type doesn't match "
                + "opcode.";
        // The problem is that the `MethodInsnNode` does not carry all the information that the
        // `MethodNode` carries, i.e. the method annotations. Therefore, we first lookup the class
        // that contains the method and retrieve the `MethodeNode` from there.
        // In C# with Mono.Cecil this information is looked up via the `ModuleDefinition` which
        // at that point also holds the compiled `Neo.SmartContract.Framework` classes and can
        // thereby find the method definition via a reference. Probably similar to how we do it
        // in the following.
        MethodInsnNode methodInsn = (MethodInsnNode) insn;
        ClassNode owner = new ClassNode();
        new ClassReader(Type.getObjectType(methodInsn.owner).getClassName()).accept(owner, 0);
        // This assumes that all methods inside of a Syscall call class are syscalls. Might not
        // hold in the future, when we add helper methods to the Syscall classes.
        if (isSyscallClass(owner)) {
            addSyscall(owner, methodInsn, neoMethod);
        }
        // TODO: Handle other kinds of methods.
    }

    private boolean isSyscallClass(ClassNode classNode) {
        return classNode.invisibleAnnotations.stream()
                .anyMatch(a -> a.desc.equals(Type.getDescriptor(Syscall.class)));
    }

    private void addSyscall(ClassNode owner, MethodInsnNode methodInsn, NeoMethod neoMethod) {
        MethodNode method = owner.methods.stream()
                .filter(m -> m.desc.equals(methodInsn.desc))
                .findFirst().get();

        // Before doing the syscall the arguments have to be reversed. Additionally, the Opcode
        // NOP is inserted before every Syscall.
        neoMethod.addInstruction(new NeoInstruction(OpCode.NOP, this.currentNeoAddr++));
        int paramsCount = Type.getMethodType(method.desc).getArgumentTypes().length;
        NeoInstruction neoInsn = null;
        if (paramsCount == 2) {
            neoInsn = new NeoInstruction(OpCode.SWAP, this.currentNeoAddr);
        } else if (paramsCount == 3) {
            neoInsn = new NeoInstruction(OpCode.REVERSE3, this.currentNeoAddr);
        } else if (paramsCount == 4) {
            neoInsn = new NeoInstruction(OpCode.REVERSE4, this.currentNeoAddr);
        } else if (paramsCount > 4) {
            addPushNumber(paramsCount, neoMethod);
            neoInsn = new NeoInstruction(OpCode.REVERSEN, this.currentNeoAddr);
        }
        neoMethod.addInstruction(neoInsn);
        this.currentNeoAddr++;

        // Annotation has to be either Syscalls or Syscall.
        AnnotationNode syscallAnnotation = method.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(Syscalls.class))
                        || a.desc.equals(Type.getDescriptor(Syscall.class)))
                .findFirst().get();
        if (syscallAnnotation.desc.equals(Type.getDescriptor(Syscalls.class))) {
            // handle multiple syscalls
            assert syscallAnnotation.values.size() == 2;
            for (Object a : (List<?>) syscallAnnotation.values.get(1)) {
                addSingleSyscall((AnnotationNode) a, neoMethod);
            }
        } else {
            // handle single syscall
            addSingleSyscall(syscallAnnotation, neoMethod);
        }
    }

    private void addSingleSyscall(AnnotationNode syscallAnnotation, NeoMethod neoMethod) {
        assert syscallAnnotation.values.size() == 2;
        String syscall = (String) syscallAnnotation.values.get(1);
        byte[] hash = InteropServiceCode.getInteropCodeHash(syscall);
        neoMethod.addInstruction(new NeoInstruction(OpCode.SYSCALL, hash, this.currentNeoAddr));
        this.currentNeoAddr += 1 + hash.length;
    }

    private void addPushNumber(int number, NeoMethod neoMethod) {
        byte[] insnBytes = new ScriptBuilder().pushInteger(number).toArray();
        byte[] operand = Arrays.copyOfRange(insnBytes, 1, insnBytes.length);
        neoMethod.addInstruction(new NeoInstruction(
                OpCode.get(insnBytes[0]), operand, this.currentNeoAddr));
        this.currentNeoAddr += insnBytes.length;
    }

}
