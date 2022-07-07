package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;
import io.neow3j.types.StackItemType;

/**
 * Represents a public key elliptic curve point. Use this class instead of plain byte arrays and strings to validate
 * that a value is actually a 33 byte EC point.
 */
public class ECPoint {

    private static final byte LENGTH = 0x21; // 33 bytes

    /**
     * Constructs an {@code ECPoint} from the given string.
     * <p>
     * This constructor can only be used with a constant string literal, and does not work on string variables or
     * return values.
     *
     * @param value the EC point as a hexadecimal string.
     */
    public ECPoint(String value) {
    }

    /**
     * Constructs an {@code ECPoint} from the given byte array.
     * <p>
     * Does NOT check if the value has the appropriate size for an EC Point. Use {@code ECPoint.isValid()} in order
     * to verify the correct format.
     *
     * @param buffer the EC point as a byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public ECPoint(byte[] buffer) {
    }

    /**
     * Constructs an {@code ECPoint} from the given byte string.
     * <p>
     * Does NOT check if the value is a valid EC point. Use {@code ECPoint.isValid()} in order to verify the correct
     * format.
     *
     * @param value the EC point as a hex string.
     */
    @Instruction
    public ECPoint(ByteString value) {
    }

    /**
     * Checks if the given object is a valid EC point, i.e., if it is either a ByteString or Buffer and 33 bytes long.
     *
     * @param data the object to check.
     * @return true if the given object is a valid EC point. False, otherwise.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISTYPE, operand = StackItemType.BYTE_STRING_CODE)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.ISTYPE, operand = StackItemType.BUFFER_CODE)
    @Instruction(opcode = OpCode.BOOLOR)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = LENGTH) // 33 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.BOOLAND)
    public static native boolean isValid(Object data);

    /**
     * @return this {@code ECPoint} as a byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public native byte[] toByteArray();

    /**
     * Returns this {@code ECPoint} as a byte string.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @return the byte string.
     */
    @Instruction
    public native ByteString toByteString();

    /**
     * Compares this EC point to the given object. The comparison happens first by reference and then by value. I.e.,
     * two {@code ECPoint}s are compared byte by byte.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same object or have the same EC point value. False,
     * otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

}
