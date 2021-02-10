package io.neow3j.utils;

import io.neow3j.exceptions.MessageDecodingException;
import io.neow3j.exceptions.MessageEncodingException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Pattern;

import static io.neow3j.constants.NeoConstants.FIXED8_DECIMALS;
import static io.neow3j.constants.NeoConstants.FIXED8_LENGTH;

/**
 * <p>Message codec functions.</p>
 * <br>
 * <p>Implementation as per https://github.com/ethereum/wiki/wiki/JSON-RPC#hex-value-encoding</p>
 */
public final class Numeric {

    private static final String HEX_PREFIX = "0x";
    private static final Pattern HEX_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2})*$");

    private Numeric() {
    }

    public static String encodeQuantity(BigInteger value) {
        if (value.signum() != -1) {
            return HEX_PREFIX + value.toString(16);
        } else {
            throw new MessageEncodingException("Negative values are not supported");
        }
    }

    public static BigInteger decodeQuantity(String value) {
        if (!isValidHexQuantity(value)) {
            throw new MessageDecodingException("Value must be in format 0x[1-9]+[0-9]* or 0x0");
        }
        try {
            return new BigInteger(value.substring(2), 16);
        } catch (NumberFormatException e) {
            throw new MessageDecodingException("Negative ", e);
        }
    }

    private static boolean isValidHexQuantity(String value) {
        if (value == null) {
            return false;
        }

        if (value.length() < 3) {
            return false;
        }

        if (!value.startsWith(HEX_PREFIX)) {
            return false;
        }

        if (value.length() > 3 && value.charAt(2) == '0') {
            return false;
        }

        return true;
    }

    public static String cleanHexPrefix(String input) {
        if (containsHexPrefix(input)) {
            return input.substring(2);
        } else {
            return input;
        }
    }

    public static String prependHexPrefix(String input) {
        if (!containsHexPrefix(input)) {
            return HEX_PREFIX + input;
        } else {
            return input;
        }
    }

    public static boolean containsHexPrefix(String input) {
        return !Strings.isEmpty(input) && input.length() > 1
                && input.charAt(0) == '0' && input.charAt(1) == 'x';
    }

    /**
     * Checks if the given string is a valid hexadecimal string. Next to the character constraint
     * (0-f) the string also needs to have a even number of character to pass as valid.
     *
     * @param string The string to check.
     * @return       true, if the string is hexadecimal or empty. False, otherwise.
     */
    public static boolean isValidHexString(String string) {
        string = cleanHexPrefix(string);
        return HEX_PATTERN.matcher(string).matches();
    }

    /**
     * Converts the given Fixed8 number to a BigDecimal.
     * @param value The Fixed8 value as a byte array. Must be max 8 bytes in little-endian order.
     * @return converted BigDecimal value.
     */
    public static BigDecimal fromFixed8ToDecimal(byte[] value) {
        if (value.length > FIXED8_LENGTH) {
            throw new IllegalArgumentException("Fixed8 byte array cannot be larger than 8 bytes.");
        }
        return fromFixed8ToDecimal(BigIntegers.fromLittleEndianByteArray(value));
    }

    /**
     * Converts the given Fixed8 number to a BigDecimal.
     * @param hexString The Fixed8 value as a hex string. Must represent max 8 bytes in big-endian
     *                  order.
     * @return converted BigDecimal value.
     */
    public static BigDecimal fromFixed8ToDecimal(String hexString) {
        checkAndThrowIsValidHexString(hexString);
        if (hexString.length() > FIXED8_LENGTH*2) {
            throw new IllegalArgumentException("Fixed8 number cannot be larger than 8 bytes.");
        }
        return fromFixed8ToDecimal(BigIntegers.fromBigEndianHexString(hexString));
    }
    /**
     * Converts the given Fixed8 number to a BigDecimal.
     * @param value The Fixed8 value as an integer.
     * @return converted BigDecimal value.
     */
    public static BigDecimal fromFixed8ToDecimal(BigInteger value) {
        return new BigDecimal(value).divide(FIXED8_DECIMALS);
    }

    /**
     * Converts the given decimal number to a Fixed8 byte array (8 bytes in little-endian order).
     * @param value The decimal number to convert.
     * @return the Fixed8 number.
     */
    public static byte[] fromDecimalToFixed8ByteArray(BigDecimal value) {
        BigInteger fixed8Value = value.multiply(FIXED8_DECIMALS).toBigInteger();
        return BigIntegers.toLittleEndianByteArrayZeroPadded(fixed8Value, FIXED8_LENGTH);
    }

    /**
     * Converts the given decimal number to a Fixed8 byte array (8 bytes in little-endian order).
     * @param value The decimal number to convert.
     * @return the Fixed8 number.
     */
    public static byte[] fromDecimalToFixed8ByteArray(BigInteger value) {
        BigInteger fixed8Value = value.multiply(FIXED8_DECIMALS.toBigInteger());
        return BigIntegers.toLittleEndianByteArrayZeroPadded(fixed8Value, FIXED8_LENGTH);
    }

    /**
     * Converts the given decimal number to a Fixed8 hexadecimal string (8 bytes in big-endian
     * order).
     * @param value The decimal number to convert.
     * @return the Fixed8 number.
     */
    public static String fromDecimalToFixed8HexString(BigDecimal value) {
        return toHexStringNoPrefix(ArrayUtils.reverseArray(fromDecimalToFixed8ByteArray(value)));
    }

    /**
     * Converts the given decimal number to a Fixed8 hexadecimal string (8 bytes in big-endian
     * order).
     * @param value The decimal number to convert.
     * @return the Fixed8 number.
     */
    public static String fromDecimalToFixed8HexString(BigInteger value) {
        return toHexStringNoPrefix(ArrayUtils.reverseArray(fromDecimalToFixed8ByteArray(value)));
    }

    public static BigInteger toBigInt(byte[] value, int offset, int length) {
        return toBigInt((Arrays.copyOfRange(value, offset, offset + length)));
    }

    public static BigInteger toBigInt(byte[] value) {
        return new BigInteger(1, value);
    }

    public static BigInteger toBigInt(String hexValue) {
        String cleanValue = cleanHexPrefix(hexValue);
        return toBigIntNoPrefix(cleanValue);
    }

    public static BigInteger toBigIntNoPrefix(String hexValue) {
        return new BigInteger(hexValue, 16);
    }

    public static String toHexStringWithPrefix(BigInteger value) {
        return HEX_PREFIX + value.toString(16);
    }

    public static String toHexStringNoPrefix(BigInteger value) {
        return value.toString(16);
    }

    public static String toHexStringNoPrefix(byte input) {
        return toHexString(new byte[]{input}, 0, 1, false);
    }

    public static String toHexStringNoPrefix(byte[] input) {
        return toHexString(input, 0, input.length, false);
    }

    public static String toHexStringWithPrefixZeroPadded(BigInteger value, int size) {
        return toHexStringZeroPadded(value, size, true);
    }

    public static String toHexStringWithPrefixSafe(BigInteger value) {
        String result = toHexStringNoPrefix(value);
        if (result.length() < 2) {
            result = Strings.zeros(1) + result;
        }
        return HEX_PREFIX + result;
    }

    /**
     * Converts the given integer to a hexadecimal string with an even number of characters by
     * padding in the front with a zero if the string is not already even.
     *
     * @param value The integer to convert.
     * @return The hex string with even length.
     */
    public static String toHexStringNoPrefixZeroPadded(BigInteger value) {
        String result = toHexStringNoPrefix(value);
        if (result.length() % 2 == 0) {
            return result;
        } else {
            return toHexStringZeroPadded(value, result.length() + 1, false);
        }
    }

    public static String toHexStringNoPrefixZeroPadded(BigInteger value, int size) {
        return toHexStringZeroPadded(value, size, false);
    }

    private static String toHexStringZeroPadded(BigInteger value, int size, boolean withPrefix) {
        String result = toHexStringNoPrefix(value);

        int length = result.length();
        if (length > size) {
            throw new UnsupportedOperationException(
                    "Value " + result + " is larger then length " + size);
        } else if (value.signum() < 0) {
            throw new UnsupportedOperationException("Value cannot be negative");
        }

        if (length < size) {
            result = Strings.zeros(size - length) + result;
        }

        if (withPrefix) {
            return HEX_PREFIX + result;
        } else {
            return result;
        }
    }

    public static byte[] toBytesPadded(BigInteger value, int length) {
        byte[] result = new byte[length];
        byte[] bytes = value.toByteArray();

        int bytesLength;
        int srcOffset;
        if (bytes[0] == 0) {
            bytesLength = bytes.length - 1;
            srcOffset = 1;
        } else {
            bytesLength = bytes.length;
            srcOffset = 0;
        }

        if (bytesLength > length) {
            throw new RuntimeException("Input is too large to put in byte array of size " + length);
        }

        int destOffset = length - bytesLength;
        System.arraycopy(bytes, srcOffset, result, destOffset, bytesLength);
        return result;
    }

    public static byte[] hexStringToByteArray(String input) {
        String cleanInput = cleanHexPrefix(input);

        int len = cleanInput.length();

        if (len == 0) {
            return new byte[]{};
        }

        byte[] data;
        int startIdx;
        if (len % 2 != 0) {
            data = new byte[(len / 2) + 1];
            data[0] = (byte) Character.digit(cleanInput.charAt(0), 16);
            startIdx = 1;
        } else {
            data = new byte[len / 2];
            startIdx = 0;
        }

        for (int i = startIdx; i < len; i += 2) {
            data[(i + 1) / 2] = (byte) ((Character.digit(cleanInput.charAt(i), 16) << 4)
                    + Character.digit(cleanInput.charAt(i + 1), 16));
        }
        return data;
    }

    public static String toHexString(byte[] input, int offset, int length, boolean withPrefix) {
        StringBuilder stringBuilder = new StringBuilder();
        if (withPrefix) {
            stringBuilder.append("0x");
        }
        for (int i = offset; i < offset + length; i++) {
            stringBuilder.append(String.format("%02x", input[i] & 0xFF));
        }

        return stringBuilder.toString();
    }

    public static String toHexString(byte input) {
        return toHexString(new byte[]{input});
    }

    public static String toHexString(byte[] input) {
        return toHexString(input, 0, input.length, true);
    }

    public static String hexToString(String input) {
        return new String(Numeric.hexStringToByteArray(input));
    }

    public static BigInteger hexToInteger(String input) {
        String reverse = reverseHexString(input);
        return Numeric.toBigInt(reverse);
    }

    public static String reverseHexString(String input) {
        byte[] inputBytes = hexStringToByteArray(input);
        byte[] reversedBytes = ArrayUtils.reverseArray(inputBytes);
        return toHexStringNoPrefix(reversedBytes);
    }

    public static byte asByte(int m, int n) {
        return (byte) ((m << 4) | n);
    }

    public static boolean isIntegerValue(BigDecimal value) {
        return value.signum() == 0
                || value.scale() <= 0
                || value.stripTrailingZeros().scale() <= 0;
    }

    private static void checkAndThrowIsValidHexString(String value) {
        if (!isValidHexString(value)) {
            throw new IllegalArgumentException("Given value is not a valid hexadecimal string.");
        }
    }

}
