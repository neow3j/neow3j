package io.neow3j.devpack;

/**
 * Offers a few methods that take string literals and convert them to byte arrays, script hashes, or integers. All of
 * these methods can only be used on constant string literals, and do not work on string variables or return values.
 * <p>
 * The conversion from string to the resulting type is made at compile time.
 */
public class StringLiteralHelper {

    /**
     * Converts the given Neo address to the corresponding script hash as a little-endian byte array.
     * <p>
     * This method can only be applied to constant string literals.
     * <p>
     * Example: addressToScriptHash("AFsCjUGzicZmXQtWpwVt6hNeJTBwSipJMS") generates
     * 0102030405060708090a0b0c0d0e0faabbccddee
     *
     * @param address the address to convert to script hash.
     * @return the script hash.
     */
    public static native Hash160 addressToScriptHash(String address);

    /**
     * Converts the given hex string to a byte array.
     * <p>
     * If you use this for an account or contract hash, make sure to pass the hex string in little-endian order.
     * <p>
     * This method can only be applied to constant string literals.
     *
     * @param hex the hex string to convert to bytes.
     * @return the byte array.
     */
    public static native ByteString hexToBytes(String hex);

    /**
     * Converts the given number string to an integer.
     * <p>
     * This method can only be applied to constant string literals.
     * <p>
     * Example: stringToInt("10000") generates the int with the value 10000.
     *
     * @param text the string representation of the integer.
     * @return the integer.
     */
    public static native int stringToInt(String text);

}
