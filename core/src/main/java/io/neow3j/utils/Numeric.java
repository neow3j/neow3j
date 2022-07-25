package io.neow3j.utils;

import io.neow3j.utils.exceptions.MessageDecodingException;
import io.neow3j.utils.exceptions.MessageEncodingException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Pattern;

import static io.neow3j.utils.ArrayUtils.reverseArray;
import static java.lang.String.format;

/**
 * Message codec functions.
 * <p>
 * Implementation as per
 * <a href="https://github.com/ethereum/wiki/wiki/JSON-RPC#hex-value-encoding">hex-value-encoding</a>
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

    /**
     * Removes an existing prefix.
     * <p>
     * If the input has no prefix, it is returned unchanged.
     *
     * @param input the hex string.
     * @return the hex string without a prefix.
     */
    public static String cleanHexPrefix(String input) {
        if (containsHexPrefix(input)) {
            return input.substring(2);
        } else {
            return input;
        }
    }

    /**
     * Prepends the input with a prefix.
     * <p>
     * If the input already has a prefix, the input is returned unchanged.
     *
     * @param input the hex string.
     * @return the hex string with a prefix.
     */
    public static String prependHexPrefix(String input) {
        if (!containsHexPrefix(input)) {
            return HEX_PREFIX + input;
        } else {
            return input;
        }
    }

    /**
     * Checks if the input contains a prefix.
     *
     * @param input the hex string.
     * @return true if the input contains a prefix. False, otherwise.
     */
    public static boolean containsHexPrefix(String input) {
        return !Strings.isEmpty(input) && input.length() > 1
                && input.charAt(0) == '0' && input.charAt(1) == 'x';
    }

    /**
     * Checks if the given string is a valid hexadecimal string. Next to the character constraint (0-f) the string
     * also needs to have a even number of character to pass as valid.
     *
     * @param string the string to check.
     * @return true if the string is hexadecimal or empty. False, otherwise.
     */
    public static boolean isValidHexString(String string) {
        string = cleanHexPrefix(string);
        return HEX_PATTERN.matcher(string).matches();
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
     * Converts the given integer to a hexadecimal string with an even number of characters by padding in the front
     * with a zero if the string is not already even.
     *
     * @param value the integer to convert.
     * @return the hex string with even length.
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
            throw new UnsupportedOperationException(format("Value %s is larger then length %s.", result, size));
        } else if (value.signum() < 0) {
            throw new UnsupportedOperationException("Value cannot be negative.");
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
        checkAndThrowIsValidHexString(input);
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
            stringBuilder.append(format("%02x", input[i] & 0xFF));
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
        return new String(hexStringToByteArray(input));
    }

    public static BigInteger hexToInteger(String input) {
        String reverse = reverseHexString(input);
        return toBigInt(reverse);
    }

    public static String reverseHexString(String input) {
        byte[] inputBytes = hexStringToByteArray(input);
        byte[] reversedBytes = reverseArray(inputBytes);
        return toHexStringNoPrefix(reversedBytes);
    }

    public static byte asByte(int m, int n) {
        return (byte) ((m << 4) | n);
    }

    public static boolean isIntegerValue(BigDecimal value) {
        return value.signum() == 0 ||
                value.scale() <= 0 ||
                value.stripTrailingZeros().scale() <= 0;
    }

    private static void checkAndThrowIsValidHexString(String value) {
        if (!isValidHexString(value)) {
            throw new IllegalArgumentException("Given value is not a valid hexadecimal string.");
        }
    }

}
