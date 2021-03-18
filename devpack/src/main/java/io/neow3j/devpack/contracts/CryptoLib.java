package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ContractInterface;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.annotations.ContractHash;

@ContractHash("0x726cb6e0cd8628a1350a611384688911ab75f51b")
public class CryptoLib extends ContractInterface {

    /**
     * Calculates the SHA-256 hash of the given value.
     *
     * @param value The value to hash.
     * @return the 256-bit hash.
     */
    public static native String sha256(String value);

    /**
     * Calculates a 160-bit RIPE message digest (RIPEMD) of the given value.
     *
     * @param value The value to hash.
     * @return the 160-bit hash.
     */
    public static native String ripemd160(String value);

    /**
     * Verifies the {@code signature} of a {@code message} with the corresponding {@code publicKey}.
     * The {@code curve} can be one of the curves defined in {@link io.neow3j.devpack.NamedCurve}.
     *
     * @param message   The signed message.
     * @param publicKey The public key of the key pair used for signing.
     * @param signature The message signature.
     * @param curve     The curve to use in the verification.
     * @return true if the signature is valid. False otherwise.
     */
    public static native boolean verifyWithECDsa(String message, ECPoint publicKey,
            String signature, byte curve);
}
