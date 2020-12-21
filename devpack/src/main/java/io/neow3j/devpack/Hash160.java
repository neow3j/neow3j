package io.neow3j.devpack;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.model.types.StackItemType;

/**
 * Represents a hash with length of 20 bytes/160 bit. Use this class when working with hashes of
 * transactions and blocks.
 */
public class Hash160 {

    private static final byte LENGTH = 0x14;

    /**
     * Provides a zero-valued {@code Hash160}.
     *
     * @return the zero-valued {@code Hash160}.
     */
    @Instruction(opcode = OpCode.PUSHDATA1, operand = {LENGTH, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0})
    public static native Hash160 zero();

    /**
     * Checks if this {@code Hash160} is zero-valued.
     *
     * @return true if this {@code Hash160} is zero-valued. False, otherwise.
     */
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.NUMEQUAL)
    public native boolean isZero();

    /**
     * Checks if this {@code Hash160} is valid, i.e. is 20 bytes long.
     *
     * @return true if this {@code Hash160} is valid. False, otherwise.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISTYPE, operand = StackItemType.BYTE_STRING_CODE)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = LENGTH) // 20 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.BOOLAND)
    public native boolean isValid();

    /**
     * Creates a {@code Hash160} from the given byte array. Checks if it is valid and fails if it is
     * not.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = LENGTH) // 20 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.ASSERT)
    public Hash160(byte[] value) {
    }

    /**
     * Creates a {@code Hash160} from the given string. Checks if it is valid and fails if it is
     * not.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = LENGTH) // 20 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.ASSERT)
    public Hash160(String value) {
    }

    /**
     * Returns this {@code Hash160} as a byte array.
     *
     * @return the byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public native byte[] toByteArray();

    /**
     * Returns this {@code Hash160} as a string.
     *
     * @return the string.
     */
    @Instruction
    public native String toString();

}
