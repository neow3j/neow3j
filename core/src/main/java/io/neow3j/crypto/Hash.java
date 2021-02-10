package io.neow3j.crypto;

import io.neow3j.utils.Numeric;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.digest.Keccak;

/**
 * Cryptographic hash functions.
 */
public class Hash {

    static {
        SecurityProviderChecker.addBouncyCastle();
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
     * Generates HMAC SHA-512 digest for the given {@code input} with the given {@code key}.
     *
     * @param key   The key parameter
     * @param input The input to digest
     * @return The hash value for the given input
     */
    public static byte[] hmacSha512(byte[] key, byte[] input) {
        HMac hMac = new HMac(new SHA512Digest());
        hMac.init(new KeyParameter(key));
        hMac.update(input, 0, input.length);
        byte[] out = new byte[64];
        hMac.doFinal(out, 0);
        return out;
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

    /**
     * Generates SHA-256 digest for the given {@code input}.
     *
     * @param input  binary encoded input data
     * @param offset of start of data
     * @param length of data
     * @return The hash value for the given input
     * @throws RuntimeException If we couldn't find any SHA-256 provider
     */
    public static byte[] sha256(byte[] input, int offset, int length) {
        if (offset != 0 || length != input.length) {
            byte[] array = new byte[length];
            System.arraycopy(input, offset, array, 0, length);
            input = array;
        }
        return sha256(input);
    }

    /**
     * Applies SHA-256 twice to the input and returns the result.
     * <p>
     * Neo uses the name {@code hash256} for hashes created in this way.
     *
     * @param input The input to hash.
     * @return the hash value for the given input.
     */
    public static byte[] hash256(byte[] input) {
        return sha256(sha256(input));
    }

    /**
     * Applies SHA-256 twice to the slice of the given length of the input, starting at the given
     * offset.
     * <p>
     * Neo uses the name {@code hash256} for hashes created in this way.
     *
     * @param input The input to hash.
     * @param offset The offset at which the slice starts.
     * @param length The length of the slice to hash.
     * @return the hash value.
     */
    public static byte[] hash256(byte[] input, int offset, int length) {
        return sha256(sha256(input, offset, length));
    }
}
