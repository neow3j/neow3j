package io.neow3j.compiler;

import io.neow3j.constants.OpCode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;

public class InitsslotNeoMethod extends NeoMethod {

    private static final String INITSSLOT_METHOD_NAME = "_initialize";

    /**
     * Constructs a new INITSSLOT method.
     *
     * @param asmMethod   The Java method this Neo method is converted from.
     * @param sourceClass The Java class from which this method originates.
     */
    public InitsslotNeoMethod(MethodNode asmMethod, ClassNode sourceClass) {
        super(asmMethod, sourceClass);
        setName(INITSSLOT_METHOD_NAME);
        setIsAbiMethod(true);
        byte[] operand = new byte[]{(byte) sourceClass.fields.size()};
        addInstruction(new NeoInstruction(OpCode.INITSSLOT, operand));
    }

    @Override
    public void convert(CompilationUnit compUnit) throws IOException {
        AbstractInsnNode insn = getAsmMethod().instructions.get(0);
        while (insn != null) {
            if (insn.getOpcode() >= JVMOpcode.ISTORE.getOpcode() &&
                    insn.getOpcode() <= JVMOpcode.SASTORE.getOpcode()) {
                throw new CompilerException(this, "Local variables are not supported in the " +
                        "static constructor");
            }
            insn = Compiler.handleInsn(insn, this, compUnit);
            insn = insn.getNext();
        }
        insertTryCatchBlocks();
    }

    @Override
    public void initialize(CompilationUnit compUnit) {
        throw new UnsupportedOperationException("The INITSSLOT method cannotneed to be " +
                "initialized with local variable and parameter slots.");
    }
}
