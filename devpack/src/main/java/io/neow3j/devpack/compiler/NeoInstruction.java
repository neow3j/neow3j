package io.neow3j.devpack.compiler;

import io.neow3j.constants.OpCode;
import io.neow3j.utils.Numeric;

/**
 * Represents a single NeoVM opcode and possible operands that belong to the opcode.
 */
public class NeoInstruction {

    /**
     * The NeoVM opcode.
     */
    OpCode opcode;

    /**
     * One or multiple operands of variable byte size, joined together in one byte array.
     */
    byte[] operand;

    /**
     * The address of this instruction in the NeoVM script. // TODO: Clarify if it is a global
     * address over the whole script or only in its own method.
     */
    int address;

    NeoInstruction(OpCode opcode, byte[] operand, int address) {
        this.opcode = opcode;
        this.operand = operand;
        this.address = address;
    }

    NeoInstruction(OpCode opcode, int address) {
        this.opcode = opcode;
        this.operand = new byte[]{};
        this.address = address;
    }

    /**
     * Produces a consecutive array of bytes of this instruction's opcode and operands.
     *
     * @return the instruction as a byte array.
     */
    byte[] toByteArray() {
        byte[] bytes = new byte[1 + operand.length];
        bytes[0] = (byte) opcode.getCode();
        System.arraycopy(operand, 0, bytes, 1, operand.length);
        return bytes;
    }

    /**
     * Returns the size of this instruction in bytes.
     *
     * @return The byte-size of this instruction.
     */
    int byteSize() {
        return 1 + operand.length;
    }

    @Override
    public String toString() {
        return opcode.toString() + " " + Numeric.toHexStringNoPrefix(operand);
    }
}
