package io.neow3j.devpack;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.model.types.StackItemType;

/**
 * A {@code ByteString} is an immutable byte array. Otherwise it behaves the same as a normal Java
 * byte array ({@code byte[]}).
 * Java {@code String}s are also represented as {@code ByteString}s on the neo-vm and can therefore
 * seamlessly be converted to and from {@code ByteString} without any GAS costs. The conversion
 * to byte array and integer are not free.
 */
public class ByteString {

    /**
     * Constructs a new {@code ByteString} from the given string. This does not incur any extra GAS
     * costs.
     *
     * @param str The string.
     */
    @Instruction()
    public ByteString(String str) {

    }

    /**
     * Constructs a new {@code ByteString} from the given byte array. This incurs the GAS cost of
     * converting the byte array to a {@code ByteString}.
     *
     * @param buffer The byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public ByteString(byte[] buffer) {

    }

    /**
     * Gets the item at the given index in this {@code ByteString}.
     *
     * @param index The index.
     * @return the item at the given index.
     */
    @Instruction(opcode = OpCode.PICKITEM)
    public native byte get(int index);

    /**
     * Gets the byte length of this {@code ByteString}.
     *
     * @return the length.
     */
    @Instruction(opcode = OpCode.SIZE)
    public native int length();

    /**
     * Returns this {@code ByteString} as a string. This does not incur any extra GAS costs.
     *
     * @return the string.
     */
    @Instruction()
    public native String asString();

    /**
     * Converts this {@code ByteString} to a byte array. This incurs the GAS cost of converting
     * the the {@code ByteString} to a byte array.
     *
     * @return the byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public native byte[] toByteArray();

    /**
     * Converts this {@code ByteString} to an integer. The bytes are read in little-endian format.
     * E.g., the byte string {@code 0102} (in hexadecimal representation) is converted to 513.
     *
     * @return the integer.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x05)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.DROP)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER_CODE)
    public native int toInteger();

    /**
     * Concatenates this and the given byte string. The returned value is a new byte string
     * instance.
     *
     * @param other The byte string to append.
     * @return the concatenated byte string.
     */
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString concat(ByteString other);

    /**
     * Concatenates this and the given byte string. The returned value is a new byte string
     * instance.
     *
     * @param other The byte string to append.
     * @return the concatenated byte string.
     */
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString concat(byte[] other);

    /**
     * Returns n consecutive characters of this byte string starting at the given index.
     *
     * @param index The start index of the range (inclusive).
     * @param n     The size of the range, i.e. number of characters to take.
     * @return the defined range of the given string.
     */
    @Instruction(opcode = OpCode.SUBSTR)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString range(int index, int n);

    /**
     * Returns the first n bytes from this byte string. Faults if {@code n} &lt; 0.
     *
     * @param n The number of bytes to return.
     * @return the first n bytes.
     */
    @Instruction(opcode = OpCode.LEFT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString take(int n);

    /**
     * Returns the last n bytes of this byte string. Faults if {@code n} &lt; 0.
     *
     * @param n The number bytes to return.
     * @return the last n bytes.
     */
    @Instruction(opcode = OpCode.RIGHT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString last(int n);
}
