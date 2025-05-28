package io.neow3j.devpack.constants;

/**
 * The combinations of ECC curves and hash algorithms available in Neo.
 */
public class NamedCurveHash {

    /**
     * The secp256k1 curve and SHA256 hash algorithm.
     */
    public static final byte Secp256k1SHA256 = 22;

    /**
     * The secp256r1 curve, which is known as prime256v1 or nistP-256, and SHA256 hash algorithm.
     */
    public static final byte secp256r1SHA256 = 23;

    /**
     * The secp256k1 curve and Keccak256 hash algorithm.
     */
    public static final byte secp256k1Keccak256 = 122;

    /**
     * The secp256r1 curve, which known as prime256v1 or nistP-256, and Keccak256 hash algorithm.
     */
    public static final byte secp256r1Keccak256 = 123;

}
