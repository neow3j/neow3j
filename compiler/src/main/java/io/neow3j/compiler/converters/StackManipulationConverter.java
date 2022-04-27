package io.neow3j.compiler.converters;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.script.OpCode;
import org.objectweb.asm.tree.AbstractInsnNode;

public class StackManipulationConverter implements Converter {

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod,
            CompilationUnit compUnit) {

        // TODO: Extract methods for the longer conversions below.
        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            case NOP:
                neoMethod.addInstruction(new NeoInstruction(OpCode.NOP));
                break;
            case DUP:
                neoMethod.addInstruction(new NeoInstruction(OpCode.DUP));
                break;
            case DUP2:
                // DUP2 operates differently on computational types of category 1 and 2. Category 1 types are int,
                // short, byte, boolean, char. Category 2 types are long and double. The latter need double the space
                // of the former on the stack. Therefore DUP2 copies the last two stack items if they are of the
                // first type category and only copies the last stack item if it is of type category 2.
                // TODO: At the moment we ignore that longs (type category 2) behave differently under this opcode.
                //  Either implement special handling or remove support for longs from the compiler.
                neoMethod.addInstruction(new NeoInstruction(OpCode.OVER));
                neoMethod.addInstruction(new NeoInstruction(OpCode.OVER));
                break;
            case POP:
                neoMethod.addInstruction(new NeoInstruction(OpCode.DROP));
                break;
            case POP2:
                // See comment at DUP2 opcode.
                // TODO: At the moment we ignore that longs (type category 2) behave differently under this opcode.
                //  Either implement special handling or remove support for longs from the compiler.
                neoMethod.addInstruction(new NeoInstruction(OpCode.DROP));
                neoMethod.addInstruction(new NeoInstruction(OpCode.DROP));
                break;
            case SWAP:
                neoMethod.addInstruction(new NeoInstruction(OpCode.SWAP));
                break;
            case DUP_X1:
                neoMethod.addInstruction(new NeoInstruction(OpCode.TUCK));
                break;
            case DUP_X2:
                // See comment at DUP2 opcode.
                // TODO: At the moment we ignore that longs (type category 2) behave differently under this opcode.
                //  Either implement special handling or remove support for longs from the compiler.
                neoMethod.addInstruction(new NeoInstruction(OpCode.ROT));
                neoMethod.addInstruction(new NeoInstruction(OpCode.ROT));
                neoMethod.addInstruction(new NeoInstruction(OpCode.PUSH2));
                neoMethod.addInstruction(new NeoInstruction(OpCode.PICK));
                break;
            case DUP2_X1:
                // DUP2_X1 handles types of category 1 and 2 differently but we only handle category 1.
                // TODO: At the moment we ignore the category 2 types here. Either implement special handling for
                //  them or remove support for longs from the compiler.
                neoMethod.addInstruction(new NeoInstruction(OpCode.ROT));
                neoMethod.addInstruction(new NeoInstruction(OpCode.PUSH2));
                neoMethod.addInstruction(new NeoInstruction(OpCode.PICK));
                neoMethod.addInstruction(new NeoInstruction(OpCode.PUSH2));
                neoMethod.addInstruction(new NeoInstruction(OpCode.PICK));
                break;
            case DUP2_X2:
                // See comment at DUP2 opcode.
                // TODO: At the moment we ignore that longs (type category 2) behave differently under this opcode.
                //  Either implement special handling or remove support for longs from the compiler.
                neoMethod.addInstruction(new NeoInstruction(OpCode.ROT));
                neoMethod.addInstruction(new NeoInstruction(OpCode.PUSH3));
                neoMethod.addInstruction(new NeoInstruction(OpCode.ROLL));
                neoMethod.addInstruction(new NeoInstruction(OpCode.SWAP));
                neoMethod.addInstruction(new NeoInstruction(OpCode.PUSH3));
                neoMethod.addInstruction(new NeoInstruction(OpCode.PICK));
                neoMethod.addInstruction(new NeoInstruction(OpCode.PUSH3));
                neoMethod.addInstruction(new NeoInstruction(OpCode.PICK));
                break;
        }
        return insn;
    }

}
