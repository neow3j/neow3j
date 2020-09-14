package io.neow3j.devpack.neo;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_BINARY_BASE64DECODE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BINARY_BASE64ENCODE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BINARY_DESERIALIZE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_BINARY_SERIALIZE;

import io.neow3j.devpack.annotations.Syscall;

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

}
