package io.neow3j.devpack;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.devpack.annotations.Syscall;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_BINARY_BASE64DECODE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BINARY_BASE64ENCODE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BINARY_DESERIALIZE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BINARY_SERIALIZE;

public class Binary {

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

    /**
     * Encodes the given byte array to a Base64 string.
     *
     * @param input The byte array to encode.
     * @return the encoded string.
     */
    @Syscall(SYSTEM_BINARY_BASE64ENCODE)
    public static native String base64Encode(byte[] input);

    /**
     * Decodes the given Base64-encoded string.
     *
     * @param input The Base64-encoded string.
     * @return the decoded byte array.
     */
    @Syscall(SYSTEM_BINARY_BASE64DECODE)
    public static native byte[] base64Decode(String input);

    /**
     * Converts the given number to its string representation.
     * <p>
     * The hexadecimal representation uses the 2's complement to represent negative numbers. Always
     * the smallest possible multiple of 4-bits is used to represent the number. E.g., for -1 the
     * hex representation is 0xf (1111). For -8 it's 0x8 (1000). If we move out of the range [-8,
     * 7], four more bits are added, such that -9 is 0xf7 (1111 0111).
     *
     * @param i    The number
     * @param base The base to use for the string representation. Can be decimal (10) or hexadecimal
     *             (16).
     * @return the number as a string.
     */
    @Syscall(InteropServiceCode.SYSTEM_BINARY_ITOA)
    public static native String itoa(int i, int base);

    /**
     * Converts the given number string into an integer.
     *
     * @param s    The number string.
     * @param base The base to use for interpreting the string. Can be decimal (10) or hexadecimal
     *             (16).
     * @return the number.
     */
    @Syscall(InteropServiceCode.SYSTEM_BINARY_ATOI)
    public static native int atoi(String s, int base);


}
