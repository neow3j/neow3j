package io.neow3j.compiler;

import io.neow3j.script.OpCode;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.io.IOException;
import java.util.List;

import static io.neow3j.compiler.Compiler.isAssertionDisabledStaticField;
import static io.neow3j.compiler.Compiler.isEvent;

public class InitsslotNeoMethod extends NeoMethod {

    private static final String INITSSLOT_METHOD_NAME = "_initialize";
    private static final String DESIRED_ASSERTION_STATUS = "desiredAssertionStatus";

    /**
     * Constructs a new INITSSLOT method.
     *
     * @param asmMethod   the Java method this Neo method is converted from.
     * @param sourceClass the Java class from which this method originates.
     * @param compUnit    the compilation unit object of the ongoing compilation, required for its classloader.
     */
    public InitsslotNeoMethod(MethodNode asmMethod, ClassNode sourceClass, CompilationUnit compUnit) {
        super(asmMethod, sourceClass);
        setName(INITSSLOT_METHOD_NAME);
        setIsAbiMethod(true);
        byte[] operand = new byte[]{(byte) calcNumberOfContractVariables(sourceClass.fields, compUnit)};
        addInstruction(new NeoInstruction(OpCode.INITSSLOT, operand));
    }

    private int calcNumberOfContractVariables(List<FieldNode> fields, CompilationUnit compUnit) {
        // Events are not counted as contract variables. They are only definitions and don't appear as actual
        // variables in the NeoVM script. We don't check for a maximum amount of contract variables here, that is
        // done in Compiler.collectContractVariables(ClassNode asmClass).
        return (int) fields.stream()
                .filter(f -> !isEvent(f.desc, compUnit))
                .count();
    }

    protected boolean containsOnlyAssertionRelatedInstructions() {
        AbstractInsnNode insn = getAsmMethod().instructions.getFirst().getNext().getNext();
        if (insn.getType() == AbstractInsnNode.LDC_INSN &&
                insn.getNext().getType() == AbstractInsnNode.METHOD_INSN) {
            MethodInsnNode methodInsn = (MethodInsnNode) insn.getNext();
            if (methodInsn.name.equals(DESIRED_ASSERTION_STATUS)) {
                insn = insn.getNext();
                while (!isAssertionDisabledStaticField(insn)) {
                    insn = insn.getNext();
                }
                return insn.getNext().getOpcode() == JVMOpcode.RETURN.getOpcode();
            }
        }
        return false;
    }

    @Override
    public void convert(CompilationUnit compUnit) throws IOException {
        AbstractInsnNode insn = getAsmMethod().instructions.get(0);
        while (insn != null) {
            if (insn.getOpcode() >= JVMOpcode.ISTORE.getOpcode() &&
                    insn.getOpcode() <= JVMOpcode.SASTORE.getOpcode()) {
                throw new CompilerException(this, "Local variables are not supported in the static constructor");
            }
            // Events must not be initialized, i.e., their constructor's must not be called.
            // Event variable are not actually variables in the NeoVM script code, just definitions.
            throwOnEventConstructorCall(insn, compUnit);
            insn = Compiler.handleInsn(insn, this, compUnit);
            insn = insn.getNext();
        }
        insertTryCatchBlocks();
    }

    private void throwOnEventConstructorCall(AbstractInsnNode insn, CompilationUnit compUnit) {
        if (insn instanceof TypeInsnNode) {
            TypeInsnNode typeInsn = (TypeInsnNode) insn;
            String desc = Type.getObjectType(typeInsn.desc).getDescriptor();
            if (isEvent(desc, compUnit)) {
                throw new CompilerException(this, "Events must not be initialized by calling their constructor.");
            }
        }
    }

    @Override
    public void initialize(CompilationUnit compUnit) {
        throw new UnsupportedOperationException("The INITSSLOT method cannotneed to be initialized with local " +
                "variable and parameter slots.");
    }

}
