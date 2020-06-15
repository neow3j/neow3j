package io.neow3j.devpack.compiler;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.contract.ScriptReader;
import io.neow3j.devpack.framework.EntryPoint;
import io.neow3j.devpack.framework.Syscall;
import io.neow3j.devpack.framework.Syscall.Syscalls;
import io.neow3j.io.BinaryWriter;
import io.neow3j.utils.BigIntegers;
import io.neow3j.utils.Numeric;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;
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

public class Compiler {

    public static void main(String[] args) throws Exception {
        ClassReader reader = new ClassReader("io.neow3j.devpack.template.HelloWorldContract");
        ClassNode n = new ClassNode();
        reader.accept(n, 0);
        MethodNode[] entryPoints = n.methods.stream()
                .filter(m -> m.invisibleAnnotations != null &&
                        m.invisibleAnnotations.stream()
                                .anyMatch(a -> a.desc.equals(Type.getDescriptor(EntryPoint.class))))
                .toArray(MethodNode[]::new);
        if (entryPoints.length > 1) {
            throw new Exception("Multiple entry points found.");
        } else if (entryPoints.length == 0) {
            throw new Exception("No entry point found.");
        }
        MethodNode entryPoint = entryPoints[0];
        Iterator<AbstractInsnNode> it = entryPoint.instructions.iterator();
        ByteArrayOutputStream ms = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(ms);
        while (it.hasNext()) {
            AbstractInsnNode insn = it.next();
            JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
            if (opcode == null) {
                continue;
            }
            switch (opcode) {
                case INVOKESTATIC:
                    handleMethod(insn, writer);
                    break;
                case LDC:
                    handleLoadConstant(insn, writer);
                    break;
                case ICONST_1:
                    writer.writeByte(OpCode.PUSH1.getValue());
                    break;
                case IRETURN:
                    writer.writeByte(OpCode.RET.getValue());
                    break;
                case ASTORE:
                case ASTORE_0:
                case ASTORE_1:
                case ASTORE_2:
                case ASTORE_3:
                    handleStoreVariable(insn, writer);
                    break;
                case ALOAD:
                case ALOAD_1:
                case ALOAD_2:
                case ALOAD_3:
                    handleLoadVariable(insn, writer);
                    break;
                default:
                    break;
            }
        }
        System.out.println(Numeric.toHexStringNoPrefix(ms.toByteArray()));
        System.out.println(ScriptReader.convertToOpCodeString(
                Numeric.toHexStringNoPrefix(ms.toByteArray())));
    }

    private static void handleLoadVariable(AbstractInsnNode insn, BinaryWriter writer)
            throws IOException {
        assert insn.getType() == AbstractInsnNode.VAR_INSN : "Instruction type doesn't match "
                + "opcode.";
        VarInsnNode varInsn = (VarInsnNode) insn;
        assert varInsn.var <= 255 : "Local variable index to high.";
        if (varInsn.var <= 6) {
            writer.writeByte((byte) (OpCode.LDLOC0.getValue() + (byte) varInsn.var));
        } else {
            writer.writeByte(OpCode.LDLOC.getValue());
            writer.writeByte((byte) varInsn.var);
        }
    }

    private static void handleStoreVariable(AbstractInsnNode insn, BinaryWriter writer)
            throws IOException {
        assert insn.getType() == AbstractInsnNode.VAR_INSN : "Instruction type doesn't match "
                + "opcode.";
        VarInsnNode varInsn = (VarInsnNode) insn;
        assert varInsn.var <= 255 : "Local variable index to high.";
        if (varInsn.var <= 6) {
            writer.writeByte((byte) (OpCode.STLOC0.getValue() + (byte) varInsn.var));
        } else {
            writer.writeByte(OpCode.STLOC.getValue());
            writer.writeByte((byte) varInsn.var);
        }
    }

    private static void handleLoadConstant(AbstractInsnNode insn, BinaryWriter writer)
            throws IOException {
        assert insn.getType() == AbstractInsnNode.LDC_INSN : "Instruction type doesn't match "
                + "opcode.";

        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
        if (ldcInsn.cst instanceof String) {
            pushDataArray(((String) ldcInsn.cst).getBytes(UTF_8), writer);
        }
        // TODO: Handle other types.
    }

    private static void pushDataArray(byte[] data, BinaryWriter writer) throws IOException {
        writer.write(new ScriptBuilder().pushData(data).toArray());
    }


    private static void handleMethod(AbstractInsnNode insn, BinaryWriter writer)
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
            handleSyscall(owner, methodInsn, writer);
        }
        // TODO: Handle other kinds of methods.
    }

    private static boolean isSyscallClass(ClassNode classNode) {
        return classNode.invisibleAnnotations.stream()
                .anyMatch(a -> a.desc.equals(Type.getDescriptor(Syscall.class)));
    }

    private static void handleSyscall(ClassNode owner, MethodInsnNode methodInsn,
            BinaryWriter writer) throws IOException {

        MethodNode method = owner.methods.stream()
                .filter(m -> m.desc.equals(methodInsn.desc))
                .findFirst().get();

        // Before doing the syscall the arguments have to be reversed. Additionally the Opcode
        // NOP is inserted before every Syscall.
        writer.writeByte(OpCode.NOP.getValue());
        int paramsCount = Type.getMethodType(method.desc).getArgumentTypes().length;
        if (paramsCount == 2) {
            writer.writeByte(OpCode.SWAP.getValue());
        } else if (paramsCount == 3) {
            writer.writeByte(OpCode.REVERSE3.getValue());
        } else if (paramsCount == 4) {
            writer.writeByte(OpCode.REVERSE4.getValue());
        } else if (paramsCount > 4) {
            handlePushNumber(paramsCount, writer);
            writer.writeByte(OpCode.REVERSEN.getValue());
        }

        // Annotation has to be either Syscalls or Syscall.
        AnnotationNode syscallAnnotation = method.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(Syscalls.class))
                        || a.desc.equals(Type.getDescriptor(Syscall.class)))
                .findFirst().get();
        if (syscallAnnotation.desc.equals(Type.getDescriptor(Syscalls.class))) {
            // handle multiple syscalls
            assert syscallAnnotation.values.size() == 2;
            for (Object a : (List<?>) syscallAnnotation.values.get(1)) {
                addSyscall((AnnotationNode) a, writer);
            }
        } else {
            // handle single syscall
            addSyscall(syscallAnnotation, writer);
        }
    }

    private static void addSyscall(AnnotationNode syscallAnnotation, BinaryWriter writer)
            throws IOException {

        assert syscallAnnotation.values.size() == 2;
        String syscall = (String) syscallAnnotation.values.get(1);
        writer.writeByte(OpCode.SYSCALL.getValue());
        writer.write(InteropServiceCode.getInteropCodeHash(syscall));
    }


    private static void handlePushNumber(int number, BinaryWriter writer) throws IOException {
        if (number == 0) {
            writer.writeByte(OpCode.PUSH0.getValue());
        } else if (number == -1) {
            writer.writeByte(OpCode.PUSHM1.getValue());
        } else if (number > 0 && number <= 16) {
            writer.writeByte((byte) (OpCode.PUSH0.getValue() + (byte) number));
        } else {
            byte[] data = BigIntegers.toLittleEndianByteArray(BigInteger.valueOf(number));
            pushDataArray(data, writer);
        }
    }

}
