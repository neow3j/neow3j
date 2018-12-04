package com.axlabs.neow3j.crypto;

import com.axlabs.neow3j.utils.Numeric;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.axlabs.neow3j.crypto.SecurityProviderChecker.addBouncyCastle;
import static com.axlabs.neow3j.utils.ArrayUtils.getFirstNBytes;
import static com.axlabs.neow3j.utils.ArrayUtils.getLastNBytes;

/**
 * Cryptographic hash functions.
 */
public class Hash {

    static {
        addBouncyCastle();
    }

    private Hash() {
    }

    /**
     * Performs a SHA256 followed by a RIPEMD160.
     *
     * @param input byte array with the input to be hashed
     * @return hash value as byte array
     */
    public static byte[] sha256AndThenRipemd160(byte[] input) {
        byte[] sha256 = sha256(input);
        return ripemd160(sha256);
    }

    /**
     * RipeMD-160 hash function.
     *
     * @param hexInput hex encoded input data with optional 0x prefix
     * @return hash value as hex encoded string
     */
    public static String ripemd160(String hexInput) {
        byte[] bytes = Numeric.hexStringToByteArray(hexInput);
        byte[] result = ripemd160(bytes);
        return Numeric.toHexString(result);
    }

    /**
     * Generates RipeMD-160 digest for the given {@code input}.
     *
     * @param input The input to digest
     * @return The hash value for the given input
     * @throws RuntimeException If we couldn't find any RipeMD160 provider
     */
    public static byte[] ripemd160(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("RipeMD160");
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't find a RipeMD160 provider", e);
        }
    }

    /**
     * Keccak-256 hash function.
     *
     * @param hexInput hex encoded input data with optional 0x prefix
     * @return hash value as hex encoded string
     */
    public static String sha3(String hexInput) {
        byte[] bytes = Numeric.hexStringToByteArray(hexInput);
        byte[] result = sha3(bytes);
        return Numeric.toHexString(result);
    }

    /**
     * Keccak-256 hash function.
     *
     * @param input  binary encoded input data
     * @param offset of start of data
     * @param length of data
     * @return hash value
     */
    public static byte[] sha3(byte[] input, int offset, int length) {
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        kecc.update(input, offset, length);
        return kecc.digest();
    }

    /**
     * Keccak-256 hash function.
     *
     * @param input binary encoded input data
     * @return hash value
     */
    public static byte[] sha3(byte[] input) {
        return sha3(input, 0, input.length);
    }

    /**
     * Keccak-256 hash function that operates on a UTF-8 encoded String.
     *
     * @param utf8String UTF-8 encoded string
     * @return hash value as hex encoded string
     */
    public static String sha3String(String utf8String) {
        return Numeric.toHexString(sha3(utf8String.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Generates SHA-256 digest for the given {@code input}.
     *
     * @param input The input to digest
     * @return The hash value for the given input
     * @throws RuntimeException If we couldn't find any SHA-256 provider
     */
    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't find a SHA-256 provider", e);
        }
    }

    public static byte[] sha256(byte[] input, int offset, int length) {
        if (offset != 0 || length != input.length) {
            byte[] array = new byte[length];
            System.arraycopy(input, offset, array, 0, length);
            input = array;
        }
        return sha256(input);
    }

    public static String base58CheckEncode(byte[] data) {
        byte[] checksum = sha256(sha256(data));
        byte[] buffer = new byte[data.length + 4];
        System.arraycopy(data, 0, buffer, 0, data.length);
        System.arraycopy(checksum, 0, buffer, data.length, 4);
        return Base58.encode(buffer);
    }

    public static byte[] base58CheckDecode(String input) {
        byte[] buffer = Base58.decode(input);
        if (buffer.length < 4) {
            throw new IllegalArgumentException("The input should contain at least 4 bytes.");
        }

        byte[] data = getFirstNBytes(buffer, buffer.length - 4);
        byte[] givenChecksum = getLastNBytes(buffer, 4);

        byte[] calculatedChecksum = sha256(sha256(data));
        byte[] first4BytesCalculatedChecksum = getFirstNBytes(calculatedChecksum, 4);

        if (!Arrays.equals(givenChecksum, first4BytesCalculatedChecksum)) {
            throw new IllegalArgumentException();
        }

        return data;
    }

}
