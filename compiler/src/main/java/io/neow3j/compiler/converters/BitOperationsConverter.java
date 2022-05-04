package io.neow3j.compiler.converters;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.CompilerException;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.script.OpCode;
import org.objectweb.asm.tree.AbstractInsnNode;

public class BitOperationsConverter implements Converter {

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit) {
        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            case ISHL:
            case LSHL:
                neoMethod.addInstruction(new NeoInstruction(OpCode.SHL));
                break;
            case ISHR:
            case LSHR:
                neoMethod.addInstruction(new NeoInstruction(OpCode.SHR));
                break;
            case IUSHR:
            case LUSHR:
                throw new CompilerException(neoMethod, "Logical bit-shifts are not supported.");
            case IAND:
            case LAND:
                neoMethod.addInstruction(new NeoInstruction(OpCode.AND));
                break;
            case IOR:
            case LOR:
                neoMethod.addInstruction(new NeoInstruction(OpCode.OR));
                break;
            case IXOR:
            case LXOR:
                neoMethod.addInstruction(new NeoInstruction(OpCode.XOR));
                break;
        }
        return insn;
    }

}
