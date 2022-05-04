package io.neow3j.crypto;

import io.neow3j.utils.Numeric;
import java.nio.charset.StandardCharsets;

/**
 * Convenience class for encoding and decoding to and from Base64. Wraps {@link java.util.Base64}.
 */
public class Base64 {

    /**
     * Base64 encodes the given hexadecimal string according to RFC4648.
     *
     * @param input the hexadecimal string to encode.
     * @return the base64-encoded string.
     */
    public static String encode(String input) {
        return encode(Numeric.hexStringToByteArray(input));
    }

    /**
     * Base64 encodes the given byte array according to RFC4648.
     *
     * @param input the byte array to encode.
     * @return the base64-encoded string.
     */
    public static String encode(byte[] input) {
        if (input.length == 0) {
            return "";
        }
        byte[] encoded = java.util.Base64.getEncoder().encode(input);
        return new String(encoded, StandardCharsets.UTF_8);
    }

    /**
     * Decodes the given base64-encoded string into its original byte representation.
     *
     * @param input the base64-encoded string in hexadecimal format.
     * @return the decoded byte array.
     */
    public static byte[] decode(String input) {
        if (input.length() == 0) {
            return new byte[0];
        }
        return java.util.Base64.getDecoder().decode(input);
    }

}
