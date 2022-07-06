package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;
import io.neow3j.types.StackItemType;

/**
 * Represents a hash with 160 bit length that was produced by first applying SHA-256 and then RIPEMD-160. Use this
 * class when working with script hashes, i.e., accounts and contracts.
 * <p>
 * Note that the underlying bytes might have varying endianness. When calling a method from a native contract or the
 * devpack that returns a {@code Hash160}, the bytes will be little-endian. But if you construct a {@code Hash160} by
 * yourself the ordering is according to whatever you used as input.
 */
public class Hash160 {

    private static final byte LENGTH = 0x14; // 20 bytes

    /**
     * Creates a {@code Hash160} from the given string. The string must be in big-endian order and 160 bits long.
     * <p>
     * This constructor can only be used with a constant string literal, and does not work on string variables or
     * return values.
     *
     * @param value the hash in big-endian order.
     */
    public Hash160(String value) {
    }

    /**
     * Creates a {@code Hash160} from the given byte array.
     * <p>
     * Does NOT check if the value is a valid hash. Use {@code Hash160.isValid()} in order to verify the correct format.
     *
     * @param buffer the hash as a byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public Hash160(byte[] buffer) {
    }

    /**
     * Creates a {@code Hash160} from the given bytes.
     * <p>
     * Does NOT check if the value is a valid hash. Use {@code Hash160.isValid()} in order to verify the correct format.
     *
     * @param value the hash as a byte string.
     */
    @Instruction
    public Hash160(ByteString value) {
    }

    /**
     * @return a zero-valued {@code Hash160}.
     */
    @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = LENGTH,
                 operand = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0})
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
     * Checks if the given object is a valid Hash160, i.e., if it is either a ByteString or Buffer and 20 bytes long.
     *
     * @param data the object to check.
     * @return true if the given object is a valid Hash160. False, otherwise.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISTYPE, operand = StackItemType.BYTE_STRING_CODE)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.ISTYPE, operand = StackItemType.BUFFER_CODE)
    @Instruction(opcode = OpCode.BOOLOR)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = LENGTH) // 20 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.BOOLAND)
    public static native boolean isValid(Object data);

    /**
     * Returns this {@code Hash160} as a byte array.
     *
     * @return the byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public native byte[] toByteArray();

    /**
     * Returns this {@code Hash160} as a byte string.
     * <p>
     * This does not incur any GAS costs.
     *
     * @return the byte string.
     */
    @Instruction
    public native ByteString toByteString();

    /**
     * Compares this hash to the given object. The comparison happens first by reference and then by value. I.e., two
     * {@code Hash160} are compared byte by byte.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same object or have the same value. False, otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

}
