package io.neow3j.devpack;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.model.types.StackItemType;

/**
 * Represents a public key elliptic curve point. Use this class instead of plain byte arrays and
 * strings to validate that a value is actually a 33 byte EC point.
 */
public class ECPoint {

    /**
     * Constructs an {@code ECPoint} from the given byte array. Checks if the argument has the
     * appropriate size of 33 bytes for an EC point.
     *
     * @param value The EC point as a byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = 0x21) // 33 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.ASSERT)
    public ECPoint(byte[] value) {

    }

    /**
     * Constructs an {@code ECPoint} from the given hex string. Checks if the argument has the
     * appropriate size of 33 bytes for an EC point.
     *
     * @param value The EC point as a hex string.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = 0x21) // 33 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.ASSERT)
    public ECPoint(String value) {

    }

    /**
     * Returns this {@code ECPoint} as a byte array.
     *
     * @return the byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public native byte[] toByteArray();

    /**
     * Returns this {@code ECPoint} as a hex string.
     *
     * @return the string.
     */
    @Instruction
    public native String toString();

}

