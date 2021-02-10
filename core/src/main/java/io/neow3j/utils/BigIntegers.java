package io.neow3j.utils;

import java.math.BigInteger;

public class BigIntegers {

    /**
     * <p>Gets the two's complement in little-endian order of the given integer.</p>
     * <br>
     * <p>The resulting byte array is correct for usage e.g. in NEO VM scripts. The conversion behaves
     * equally to the one used in the C#-based
     * <a href="https://github.com/neo-project/neo-vm">neo-vm</a> implementation.
     * It is basically how the BigIntger.toByteArray() in C# works
     * (Cf.
     * <a href="https://docs.microsoft.com/en-us/dotnet/api/system.numerics.biginteger.tobytearray?view=netframework-4.8">C# docs</a>
     * ).</p>
     *
     * @param value The integer to convert.
     * @return the byte representation of the integer.
     */
    public static byte[] toLittleEndianByteArray(BigInteger value) {
        return ArrayUtils.reverseArray(value.toByteArray());
    }

    /**
     * Gets the two's complement in little-endian order of the given integer.
     *
     * @param value the integer to convert
     * @return the byte representation of the integer.
     * @see BigIntegers#toLittleEndianByteArray(BigInteger)
     */
    public static byte[] toLittleEndianByteArray(int value) {
        return toLittleEndianByteArray(BigInteger.valueOf(value));
    }

    /**
     * Gets the two's complement in little-endian order of the given integer. If the resulting byte
     * array is smaller than the given length, it is padded with zero-valued bytes.
     *
     * @param value  The integer to convert.
     * @param length Desired length of the byte array.
     * @return the byte representation of the integer.
     * @throws IllegalArgumentException if the length of the integer in bytes is bigger than the
     *                                  given length.
     * @see BigIntegers#toLittleEndianByteArray(BigInteger)
     */
    public static byte[] toLittleEndianByteArrayZeroPadded(BigInteger value, int length) {
        // BigInteger.toByteArray() returns the two's complement of the number in big-endian order.
        byte[] bytes = value.toByteArray();
        if (bytes.length > length) {
            throw new IllegalArgumentException("given integer needs more space (" + bytes.length +
                    " bytes) than the given minimum length (" + length + " bytes).");
        }
        if (bytes.length < length) {
            byte[] temp = new byte[length];
            System.arraycopy(bytes, 0, temp, length - bytes.length, bytes.length);
            return ArrayUtils.reverseArray(temp);
        }
        return ArrayUtils.reverseArray(bytes);
    }

    /**
     * Gets the two's complement in little-endian order of the given integer. If the resulting byte
     * array is smaller than the given minimum length, it is padded with zero-valued bytes.
     *
     * @param value         the integer to convert
     * @param minByteLength Desired length of the byte array.
     * @return the byte representation of the integer.
     * @throws IllegalArgumentException if the length of the integer in bytes is bigger than the
     *                                  given minimum length.
     * @see BigIntegers#toLittleEndianByteArray(BigInteger)
     */
    public static byte[] toLittleEndianByteArrayZeroPadded(int value, int minByteLength) {
        return toLittleEndianByteArrayZeroPadded(BigInteger.valueOf(value), minByteLength);
    }

    /**
     * Converts the integer in the given byte array to a BigInteger. The integer is assumed to be
     * in its two's complement and in little-endian order.
     *
     * @param value The byte array to convert.
     * @return the BigInteger.
     */
    public static BigInteger fromLittleEndianByteArray(byte[] value) {
        // The BigInteger(byte[]) constructor takes a two's complement byte array in big-endian
        // order. So, the only thing that we have to do is to convert the little-endian byte array
        // into big-endian.
        return new BigInteger(ArrayUtils.reverseArray(value));
    }

    /**
     * Converts the integer in the given hex string to a BigInteger. The integer is assumed to be
     * in its two's complement and in big-endian order.
     *
     * @param value The byte array to convert.
     * @return the BigInteger.
     */
    public static BigInteger fromBigEndianHexString(String value) {
        // The BigInteger(byte[]) constructor takes a two's complement byte array in big-endian order.
        return new BigInteger(Numeric.hexStringToByteArray(value));
    }
}
