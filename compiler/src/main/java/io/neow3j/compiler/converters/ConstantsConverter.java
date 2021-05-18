package io.neow3j.compiler.converters;

import static io.neow3j.compiler.Compiler.addLoadConstant;
import static io.neow3j.compiler.Compiler.addPushNumber;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.script.OpCode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;

public class ConstantsConverter implements Converter {

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod,
            CompilationUnit compUnit) {

        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
                addPushNumber(opcode.getOpcode() - 3, neoMethod);
                break;
            case LCONST_0:
                addPushNumber(0, neoMethod);
                break;
            case LCONST_1:
                addPushNumber(1, neoMethod);
                break;
            case LDC:
            case LDC_W:
            case LDC2_W:
                addLoadConstant(insn, neoMethod);
                break;
            case ACONST_NULL:
                neoMethod.addInstruction(new NeoInstruction(OpCode.PUSHNULL));
                break;
            case BIPUSH: // Has an operand with an int value from -128 to 127.
            case SIPUSH: // Has an operand with an int value from -32768 to 32767.
                addPushNumber(((IntInsnNode) insn).operand, neoMethod);
                break;
        }
        return insn;
    }
}
