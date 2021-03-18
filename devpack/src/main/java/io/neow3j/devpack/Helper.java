package io.neow3j.devpack;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.model.types.StackItemType;

/**
 * Provides helper methods to be used in a smart contract.
 */
public class Helper {

    /**
     * Asserts if the given boolean is true. If not, makes the smart contract exit in a fault state.
     * Otherwise, execution continues normally.
     *
     * @param condition The condition to check.
     */
    @Instruction(opcode = OpCode.ASSERT)
    public static native void assertTrue(boolean condition);

    /**
     * Aborts the execution of the contract.
     */
    @Instruction(opcode = OpCode.ABORT)
    public static native void abort();

    /**
     * Converts the given byte to a byte array.
     *
     * @param source The byte to convert.
     * @return the converted byte array.
     */
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.LEFT)
    public static native byte[] toByteArray(byte source);

    /**
     * Converts the given string to a byte array using UTF-8 encoding.
     * <p>
     * This method cannot be used to convert a Neo address to a valid script hash byte array, or
     * a script hash in hexadecimal format into its corresponding byte array.
     * <p>
     * Examples
     * <ul>
     *     <li>"hello": [0x68656c6c6f]</li>
     *     <li>"": []</li>
     *     <li>"Neo": [0x4e656f]</li>
     * </ul>
     *
     * @param source The string to convert.
     * @return the converted byte array.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public static native byte[] toByteArray(String source);

    /**
     * Asserts that the given value is in the range [-128, 127], i.e., the value range of a signed
     * byte. Returns the value as a signed byte if true, faults if not.
     * <p>
     * Examples:
     * <ul>
     *     <li>255: fault</li>
     *     <li>-128: [0x80]</li>
     *     <li>0: [0x00]</li>
     *     <li>10: [0x0a]</li>
     *     <li>127: [0x7f]</li>
     * </ul>
     *
     * @param value The value to cast.
     * @return the casted byte.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.ASSERT)
    public native static byte asByte(int value);

    /**
     * Asserts that the given value is in the range [0, 255], i.e., the range of an unsigned byte.
     * Returns the value as a signed byte if true, faults if not.
     * <ul>
     *      <li>256: fault</li>
     *      <li>-1: fault</li>
     *      <li>255: -1 [0xff]</li>
     *      <li>0: 0 [0x00]</li>
     *      <li>10: 10 [0x0a]</li>
     *      <li>127: 127 [0x7f]</li>
     *      <li>128: -128 [0x80]</li>
     * </ul>
     *
     * @param value The value to cast.
     * @return the casted byte.
     */
    public static byte asSignedByte(int value) {
        assertTrue(within(value, 0, 256));
        if (value > 127) {
            value -= 256;
        }
        return (byte) value;
    }

    /**
     * Converts the given byte array to an integer. The byte array is assumed to be a two's
     * complement in little-endian format.
     * <p>
     * The value of the converted integer can be outside of {@link Integer#MAX_VALUE} because the
     * neo-vm doesn't have that limit for {@code int} as the JVM has.
     * <p>
     * Examples
     * <ul>
     *  <li>[0x0a]: 10</li>
     *  <li>[0x80]: -128</li>
     *  <li>[0xff80]: -32513</li>
     *  <li>[]: 0</li>
     *  <li>[0xff00]: 255</li>
     *  <li>[0xfba600]: 42747</li>
     * </ul>
     *
     * @param source The byte array to convert.
     * @return the converted integer.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x05)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.DROP)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER_CODE)
    public static native int toInt(byte[] source);

    /**
     * Casts the given byte array to a string. Assumes that the byte array is a UTF-8 encoded
     * string.
     * <p>
     * In the NeoVM strings are represented as {@code ByteStrings}. Hence the method name.
     * <p>
     * Examples:
     * <ul>
     *     <li>[0x68656c6c6f]: "hello"</li>
     *     <li>[]: ""</li>
     *     <li>[0x4e656f]: "Neo"</li>
     * </ul>
     *
     * @param source The byte array to cast.
     * @return the string.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public native static String toByteString(byte[] source);

    /**
     * Checks if the value of x is in the range [a, b).
     * <p>
     * Examples: x=5 a=5 b=15 is true; x=15 a=5 b=15 is false
     *
     * @param x The value to check if it is in the range.
     * @param a The beginning of the range (inclusive).
     * @param b The end of the range (exclusive).
     * @return true iff {@literal a <= x < b}. False, otherwise.
     */
    @Instruction(opcode = OpCode.WITHIN)
    public static native boolean within(int x, int a, int b);

    /**
     * Concatenates the two given byte arrays.
     *
     * @param first  The first byte array.
     * @param second The second byte array.
     * @return the concatenation.
     */
    @Instruction(opcode = OpCode.CAT)
    public static native byte[] concat(byte[] first, byte[] second);

    /**
     * Concatenates the two given strings.
     *
     * @param first  The first string.
     * @param second The second string.
     * @return the concatenation.
     */
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public static native String concat(String first, String second);

    /**
     * Returns n consecutive characters from the given string starting at the given index.
     *
     * @param source The string to take the bytes from.
     * @param index  The start index of the range (inclusive).
     * @param n      The size of the range, i.e. number of characters to take.
     * @return the defined range of the given string.
     */
    @Instruction(opcode = OpCode.SUBSTR)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public static native String range(String source, int index, int n);

    /**
     * Returns n consecutive bytes from the given source starting at the given index.
     *
     * @param source The array to take the bytes from.
     * @param index  The start index of the range (inclusive).
     * @param n      The size of the range, i.e. number of bytes to take.
     * @return the defined range of the given byte array.
     */
    @Instruction(opcode = OpCode.SUBSTR)
    public static native byte[] range(byte[] source, int index, int n);

    /**
     * Returns the first n elements from the given byte array. Faults if {@code n} &lt; 0.
     *
     * @param source The array to take the bytes from.
     * @param n      The number of bytes to return.
     * @return the first n bytes.
     */
    @Instruction(opcode = OpCode.LEFT)
    public static native byte[] take(byte[] source, int n);

    /**
     * Returns the first n characters from the given string. Faults if {@code n} &lt; 0.
     *
     * @param source The string to take the characters from.
     * @param n      The number of characters to return.
     * @return the first n characters.
     */
    @Instruction(opcode = OpCode.LEFT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public static native String take(String source, int n);

    /**
     * Returns the last n elements of the given byte array. Faults if {@code n} &lt; 0.
     *
     * @param source The array to take the bytes from.
     * @param n      The number of bytes to return.
     * @return the last n bytes.
     */
    @Instruction(opcode = OpCode.RIGHT)
    public static native byte[] last(byte[] source, int n);

    /**
     * Returns the last n characters of the given String. Faults if {@code n} &lt; 0.
     *
     * @param source The string.
     * @param n      The number characters to return.
     * @return the last n characters.
     */
    @Instruction(opcode = OpCode.RIGHT)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public static native String last(String source, int n);

    /**
     * Returns a reversed copy of the given bytes. Example: [0a,0b,0c,0d,0e]: [0e,0d,0c,0b,0a]
     *
     * @param source The bytes to reverse.
     * @return The reversed bytes.
     */
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.REVERSEITEMS)
    public static native byte[] reverse(byte[] source);

    /**
     * Returns a reversed copy of the given string.
     *
     * @param source The string to reverse.
     * @return The reversed string.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.REVERSEITEMS)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    public static native String reverse(String source);

    /**
     * Returns the square root of the given number.
     *
     * @param x the number to get the root for.
     * @return the square root.
     */
    @Instruction(opcode = OpCode.SQRT)
    public static native int sqrt(int x);

    /**
     * Returns the {@code base} to the power of the {@code exponent}.
     *
     * @param base     the base.
     * @param exponent the exponent.
     * @return the exponentiation.
     */
    @Instruction(opcode = OpCode.POW)
    public static native int pow(int base, int exponent);

}
