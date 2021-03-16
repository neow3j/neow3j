package io.neow3j.devpack;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.devpack.annotations.Syscall;
import io.neow3j.devpack.contracts.CryptoLib;

/**
 * Offers cryptographic functions for use in smart contracts.
 */
public class Crypto {

    /**
     * Checks if the {@code signature} is valid given that {@code publicKey} is the signer. The
     * message that corresponds to the signature is the container that started the current
     * execution of the smart contract, i.e., usually the transaction.
     * <p>
     * The ECC curve secp256r1 is assumed.
     *
     * @param publicKey The public key.
     * @param signature The signature.
     * @return True if the signature is valid. False otherwise.
     */
    @Syscall(InteropServiceCode.NEO_CRYPTO_CHECKSIG)
    public static native boolean checkSig(ECPoint publicKey, String signature);

    /**
     * Checks if the {@code signatures} are valid given that {@code publicKeys} are the signers.
     * The message that corresponds to the signatures is the container that started the current
     * execution of the smart contract, i.e., usually the transaction.
     * <p>
     * The ECC curve secp256r1 is assumed.
     *
     * @param signatures The signatures.
     * @param publicKeys The public keys.
     * @return True if the signatures are valid. False otherwise.
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
        return new Hash160(CryptoLib.ripemd160(CryptoLib.sha256(value)));
    }


}
