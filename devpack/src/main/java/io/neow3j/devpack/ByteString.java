package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;
import io.neow3j.types.StackItemType;

/**
 * A {@code ByteString} is an immutable byte array. Otherwise, it behaves the same as a normal Java byte array
 * ({@code byte[]}).
 * <p>
 * Java {@code String}s are also represented as {@code ByteString}s on the NeoVM and can therefore seamlessly be
 * converted to and from {@code ByteString} without any GAS costs. The conversion to byte array and integer are not
 * free.
 */
public class ByteString {

    /**
     * Constructs a new {@code ByteString} from the given string. The given string is interpreted as a UTF-8 encoded
     * byte array, not as a hexadecimal string. If you need to create a {@code ByteString} from a hex string, use
     * {@link StringLiteralHelper#hexToBytes(String)}.
     * <p>
     * This constructor does not incur any GAS costs.
     *
     * @param str the string.
     */
    @Instruction
    public ByteString(String str) {
    }

    /**
     * Constructs a new {@code ByteString} from the given byte array. This incurs the GAS cost of converting the byte
     * array to a {@code ByteString}.
     *
     * @param buffer the byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public ByteString(byte[] buffer) {
    }

    /**
     * Constructs a new {@code ByteString} from the given integer. This incurs the GAS cost of converting the integer
     * to a {@code ByteString}.
     *
     * @param integer the integer.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public ByteString(int integer) {
    }


    /**
     * Gets the item at the given index in this {@code ByteString}. If the index is out of bounds the NeoVM will
     * throw an exception that needs to be catched. Otherwise, the NeoVM will FAULT.
     *
     * @param index the index.
     * @return the item at the given index.
     */
    @Instruction(opcode = OpCode.PICKITEM)
    public native byte get(int index);

    /**
     * @return the byte length of this {@code ByteString}.
     */
    @Instruction(opcode = OpCode.SIZE)
    public native int length();

    /**
     * Returns this {@code ByteString} as a string.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @return the string.
     */
    @Override
    @Instruction
    public native String toString();

    /**
     * Converts this {@code ByteString} to a byte array.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} to a byte array.
     *
     * @return the byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public native byte[] toByteArray();

    /**
     * Converts this {@code ByteString} to an integer. The bytes are read in little-endian format. E.g., the byte
     * string {@code 0102} (in hexadecimal representation) is converted to 513.
     *
     * @return the integer.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x03)
    @Instruction(opcode = OpCode.ABORT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER_CODE)
    public native int toInt();

    /**
     * Converts this {@code ByteString} to an integer. If the underlying value is null, zero is returned. The bytes
     * are read in little-endian format. E.g., the byte string {@code 0102} (in hexadecimal representation) is
     * converted to 513.
     *
     * @return the integer.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x06)
    @Instruction(opcode = OpCode.DROP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.JMP, operand = 0x04)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER_CODE)
    public native int toIntOrZero();

    /**
     * Concatenates this and the given byte string. The returned value is a new byte string instance.
     *
     * @param other the byte string to append.
     * @return the concatenated byte string.
     */
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString concat(ByteString other);

    /**
     * Concatenates this byte string and the given byte array. The returned value is a new byte string instance.
     *
     * @param other the byte string to append.
     * @return the concatenated byte string.
     */
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString concat(byte[] other);

    /**
     * Concatenates this byte string and the given string. The returned value is a new byte string instance.
     * <p>
     * The string is represented as a UTF-8 encoded byte array.
     *
     * @param other the string to append.
     * @return the concatenated byte string.
     */
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString concat(String other);

    /**
     * Concatenates this byte string and the given integer. The returned value is a new byte string instance.
     *
     * @param other the integer to append.
     * @return the concatenated byte string.
     */
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString concat(int other);

    /**
     * Returns n consecutive characters of this byte string starting at the given index.
     *
     * @param index the start index of the range (inclusive).
     * @param n     the size of the range, i.e. number of characters to take.
     * @return the defined range of the given string.
     */
    @Instruction(opcode = OpCode.SUBSTR)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString range(int index, int n);

    /**
     * Returns the first n bytes from this byte string. Faults if {@code n} &lt; 0.
     *
     * @param n the number of bytes to return.
     * @return the first n bytes.
     */
    @Instruction(opcode = OpCode.LEFT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString take(int n);

    /**
     * Returns the last n bytes of this byte string. Faults if {@code n} &lt; 0.
     *
     * @param n the number bytes to return.
     * @return the last n bytes.
     */
    @Instruction(opcode = OpCode.RIGHT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native ByteString last(int n);

    /**
     * Compares this byte string to the given object. The comparison happens first by reference and then by value.
     * I.e., two {@code ByteString}s are compared byte by byte.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same object or have the same value. False, otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

}
