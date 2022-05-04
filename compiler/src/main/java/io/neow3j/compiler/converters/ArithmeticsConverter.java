package io.neow3j.compiler.converters;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.script.OpCode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;

import static io.neow3j.compiler.Compiler.addPushNumber;
import static io.neow3j.compiler.LocalVariableHelper.addLoadLocalVariable;
import static io.neow3j.compiler.LocalVariableHelper.addStoreLocalVariable;

public class ArithmeticsConverter implements Converter {

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit) {

        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            case IINC:
                handleIntegerIncrement(neoMethod, insn);
                break;
            case IADD:
            case LADD:
                neoMethod.addInstruction(new NeoInstruction(OpCode.ADD));
                break;
            case ISUB:
            case LSUB:
                neoMethod.addInstruction(new NeoInstruction(OpCode.SUB));
                break;
            case IMUL:
            case LMUL:
                neoMethod.addInstruction(new NeoInstruction(OpCode.MUL));
                break;
            case IDIV:
            case LDIV:
                neoMethod.addInstruction(new NeoInstruction(OpCode.DIV));
                break;
            case IREM:
            case LREM:
                neoMethod.addInstruction(new NeoInstruction(OpCode.MOD));
                break;
            case INEG:
            case LNEG:
                neoMethod.addInstruction(new NeoInstruction(OpCode.NEGATE));
                break;
        }
        return insn;
    }

    private static void handleIntegerIncrement(NeoMethod neoMethod, AbstractInsnNode insn) {
        IincInsnNode incInsn = (IincInsnNode) insn;
        if (incInsn.incr == 0) {
            // This case probably never happens, but if it does, do nothing.
            return;
        }

        addLoadLocalVariable(incInsn.var, neoMethod); // Load local variable
        if (incInsn.incr == 1) {
            neoMethod.addInstruction(new NeoInstruction(OpCode.INC));
        } else if (incInsn.incr == -1) {
            neoMethod.addInstruction(new NeoInstruction(OpCode.DEC));
        } else if (incInsn.incr > 1) {
            addPushNumber(incInsn.incr, neoMethod);
            neoMethod.addInstruction(new NeoInstruction(OpCode.ADD));
        } else if (incInsn.incr < -1) {
            addPushNumber(-incInsn.incr, neoMethod);
            neoMethod.addInstruction(new NeoInstruction(OpCode.SUB));
        }
        addStoreLocalVariable(incInsn.var, neoMethod); // Store incremented local variable.
    }

}
