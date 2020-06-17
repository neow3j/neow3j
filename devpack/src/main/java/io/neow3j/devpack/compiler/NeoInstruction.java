package io.neow3j.devpack.compiler;

import io.neow3j.constants.OpCode;

public class NeoInstruction {

    OpCode opcode;
    byte[] operand;
    int address;

    public NeoInstruction(OpCode opcode, byte[] operand, int address) {
        this.opcode = opcode;
        this.operand = operand;
        this.address = address;
    }

    public NeoInstruction(OpCode opcode, int address) {
        this.opcode = opcode;
        this.operand = new byte[]{};
        this.address = address;
    }

    public byte[] toByteArray() {
        byte[] bytes = new byte[1 + operand.length];
        bytes[0] = (byte) opcode.getCode();
        System.arraycopy(operand, 0, bytes, 1, operand.length);
        return bytes;
    }

    public int getByteSize() {
        return 1 + operand.length;
    }
}
