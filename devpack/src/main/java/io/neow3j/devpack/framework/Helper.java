package io.neow3j.devpack.framework;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_BINARY_DESERIALIZE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BINARY_SERIALIZE;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.framework.annotations.Instruction;
import io.neow3j.devpack.framework.annotations.Syscall;
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
     * Converts the given string to a byte array.
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
     * Checks if the given integer is in the range [0, 255], converts it to a signed byte or exits
     * in fault state if not in range. Examples:
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
     * @param source The integer to convert.
     * @return the converted singed byte.
     */
    public static byte toByte(int source) {
        assertTrue(within(source, 0, 256));
        if (source > 127) {
            source -= 256;
        }
        return (byte) source;
    }

    /**
     * Converts the given byte array to an integer. No checks are made regarding the value range of
     * integers.
     * <p>
     * Examples
     * <ul>
     *  <li>[0x0a]: 10</li>
     *  <li>[0x80]: -128</li>
     *  <li>[]: 0</li>
     *  <li>[0xff00]: 255</li>
     * </ul>
     *
     * @param source The byte array to convert.
     * @return the converted integer.
     */
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER_CODE)
    public static native int toInt(byte[] source);

    /**
     * Casts the given byte array to a string.
     * <p>
     * Examples
     * <ul>
     *     <li>[0x68656c6c6f]: "hello"</li>
     *     <li>[]: ""</li>
     *     <li>[0x4e656f]: "Neo"</li>
     * </ul>
     * This doesn't actually require an operation in the NeoVM. I.e., calling this doesn't
     * add anything to the compiled script.
     *
     * @param source The byte array to cast.
     * @return the string.
     */
    @Instruction() // Nothing to do.
    public native static String asString(byte[] source);

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
     * Returns the last n elements from the given byte array. Faults if {@code n} &lt; 0.
     *
     * @param source The array to take the bytes from.
     * @param n      The number of bytes to return.
     * @return the last n bytes.
     */
    @Instruction(opcode = OpCode.RIGHT)
    public static native byte[] last(byte[] source, int n);

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
     * Attempts to serialize the given object to a byte array.
     *
     * @param source the object to serialize.
     * @return the serialized byte array.
     */
    @Syscall(SYSTEM_BINARY_SERIALIZE)
    public static native byte[] serialize(Object source);

    /**
     * Attempts to deserialize the given byte array. It is up to the developer to know what type to
     * expect from the deserialization.
     *
     * @param source the byte array to deserialize.
     * @return the deserialized object.
     */
    @Syscall(SYSTEM_BINARY_DESERIALIZE)
    public static native Object deserialize(byte[] source);

    // TODO: Add support for the following function that manipulate static string literals.
//    /**
//     * Converts the given Base-58 address to its script hash as a little-endian byte array. This
//     * method can only be applied to string literals, otherwise the compiler will throw an
//     * exception.
//     * <p>
//     * Example: "AFsCjUGzicZmXQtWpwVt6hNeJTBwSipJMS".ToScriptHash() generates
//     * 0102030405060708090a0b0c0d0e0faabbccddee
//     */
//    @NonemitWithConvert(method = ConvertMethod.toScriptHash)
//    public static native byte[] addressToScriptHash(String address);
//
//    /**
//     * Converts the given hex string to a byte array. This method can only be applied to string
//     * literals, otherwise the compiler will throw an exception.
//     * <p>
//     * Example: "0102030405060708090a0b0c0d0e0faabbccddee".hexToBytes() generates the
//     * corresponding byte array.
//     */
//    @NonemitWithConvert(method = ConvertMethod.toHexToBytes)
//    public static native byte[] hexToBytes(String hex);
//
//    /**
//     * Converts the given string, representing a number, to an integer. This method can only be
//     * applied to string literals, otherwise the compiler will throw an exception.
//     * <p>
//     * Example: "10000".stringToInt() generates the int with the value 10000.
//     */
//    @NonemitWithConvert(method = ConvertMethod.toInt)
//    public static native int stringToInt(String text);
}
