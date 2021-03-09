package io.neow3j.devpack;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.devpack.annotations.Syscall;
import io.neow3j.devpack.contracts.CryptoLib;

/**
 * Offers cryptographic functions for use in smart contracts.
 */
public class Crypto {

    /**
     * Checks if the {@code signature} originates from the {@code publicKey}. The corresponding
     * message is taken from the script container that induced this call, e.g., a transaction.
     *
     * @param publicKey The public key.
     * @param signature The signature.
     * @return True if the signature originates from the public key. False otherwise.
     */
    @Syscall(InteropServiceCode.NEO_CRYPTO_CHECKSIG)
    public static native boolean checkSig(ECPoint publicKey, String signature);

    /**
     * Checks if the {@code signatures} originate from the {@code publicKeys}. The corresponding
     * message is taken from the script container that induced this call, e.g., a transaction.
     *
     * @param signatures The signatures.
     * @param publicKeys The public keys.
     * @return True if the signatures originate from the public keys. False otherwise.
     */
    @Syscall(InteropServiceCode.NEO_CRYPTO_CHECKMULTISIG)
    public static native boolean checkMultisig(ECPoint[] publicKeys, String[] signatures);

    /**
     * Applies SHA-256 twice to the given value.
     *
     * @param value The value to hash.
     * @return the 256 bit long hash.
     */
    public static Hash256 hash256(String value) {
        return new Hash256(CryptoLib.sha256(CryptoLib.sha256(value)));
    }

    /**
     * First applies SHA-256 then RIPEMD-160 to the given value.
     *
     * @param value The value to hash.
     * @return the 160 bit long hash.
     */
    public static Hash160 hash160(String value) {
        return new Hash160(CryptoLib.sha256(CryptoLib.sha256(value)));
    }


}
