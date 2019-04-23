package io.neow3j.protocol.core;

import io.neow3j.crypto.KeyUtils;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;

public class HexUtils {

    public static String reverse(String input) {
        byte[] inputBytes = Numeric.hexStringToByteArray(input);
        byte[] reversedBytes = ArrayUtils.reverseArray(inputBytes);
        return Numeric.toHexStringNoPrefix(reversedBytes);
    }

    public static String hexToString(String input) {
        return new String(Numeric.hexStringToByteArray(input));
    }

    public static BigInteger hexToInteger(String input) {
        String reverse = reverse(input);
        return Numeric.toBigInt(reverse);
    }

    public static String scriptHashToAddress(String input) {
        byte[] inputBytes = Numeric.hexStringToByteArray(input);
        return KeyUtils.toAddress(inputBytes);
    }

}
