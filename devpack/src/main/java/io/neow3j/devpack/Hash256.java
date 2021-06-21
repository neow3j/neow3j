package io.neow3j.devpack;

import io.neow3j.script.OpCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.types.StackItemType;

/**
 * Represents a hash with length of 256 bit that was created by applying SHA-256 twice. Use this
 * class when working with transaction and block hashes.
 *
 * Note that the underlying bytes might have varying endianness. When calling a method from a
 * native contract or the devpack that returns a {@code Hash256}, the bytes will be little-endian.
 * But if you construct a {@code Hash256} by yourself the ordering is according to whatever you
 * used as input.
 */
public class Hash256 {

    private static final byte LENGTH = 0x20;

    /**
     * Provides a zero-valued {@code Hash256}.
     *
     * @return the zero-valued {@code Hash256}.
     */
    @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = LENGTH, operand = {0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0})
    public static native Hash256 zero();

    /**
     * Checks if this {@code Hash256} is zero-valued.
     *
     * @return true if this {@code Hash256} is zero-valued. False, otherwise.
     */
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.NUMEQUAL)
    public native boolean isZero();

    /**
     * Checks if this {@code Hash256} is valid, i.e. is 32 bytes long.
     * <p>
     * This method is useful when a contract method takes a {@code Hash256} as a parameter and
     * you want to make sure that the underlying value really is a valid hash. The compiler and
     * NeoVM don't include or enforce this check automatically, because that would consume extra
     * GAS even if you don't require that check.
     *
     * @return true if this {@code Hash256} is valid. False, otherwise.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISTYPE, operand = StackItemType.BYTE_STRING_CODE)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = LENGTH) // 32 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.BOOLAND)
    public native boolean isValid();

    /**
     * Creates a {@code Hash256} from the given byte array.
     * <p>
     * Checks if the value is a valid hash. Fails if it is not.
     *
     * @param value The hash as a byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = LENGTH) // 32 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.ASSERT)
    public Hash256(byte[] value) {
    }

    /**
     * Creates a {@code Hash256} from the given bytes.
     * <p>
     * Checks if the value is a valid hash. Fails if it is not.
     *
     * @param value The hash as a byte string.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = LENGTH) // 32 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.ASSERT)
    public Hash256(ByteString value) {
    }

    /**
     * Returns this {@code Hash256} as a byte array.
     *
     * @return the byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public native byte[] toByteArray();

    /**
     * Returns this {@code Hash256} as a {@code ByteString}. No GAS costs accrue for this
     * conversion.
     *
     * @return the byte string.
     */
    @Instruction
    public native ByteString toByteString();

    /**
     * Compares this hash to the given object. The comparison happens first by reference and then by
     * value. I.e., two {@code Hash256} are compared byte by byte.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same object or have the same value.
     * False otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

}
