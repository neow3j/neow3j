package io.neow3j.devpack;

import io.neow3j.script.OpCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.types.StackItemType;

/**
 * Represents a hash with 160 bit length that was produced by first applying SHA-256 and then
 * RIPEMD-160. Use this class when working with script hashes, i.e., accounts and contracts.
 */
public class Hash160 {

    private static final byte LENGTH = 0x14;

    /**
     * Provides a zero-valued {@code Hash160}.
     *
     * @return the zero-valued {@code Hash160}.
     */
    @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = LENGTH, operand = {0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0})
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
     *
     * @param value The hash byte array.
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
     * Creates a {@code Hash160} from the given bytes. Checks if it is valid and fails if it is not.
     *
     * @param value The hash string.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = LENGTH) // 20 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.ASSERT)
    public Hash160(ByteString value) {
    }

    /**
     * Returns this {@code Hash160} as a byte array.
     *
     * @return the byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public native byte[] toByteArray();

    /**
     * Returns this {@code Hash160} as a byte string. This does not incur any GAS costs.
     *
     * @return the byte string.
     */
    @Instruction
    public native ByteString asByteString();

    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals();

    /**
     * Compares this hash to the given object. The comparison happens first by reference and then by
     * value. I.e., two {@code Hash160} are compared byte by byte.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same object or have the same value.
     * False otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);
}
