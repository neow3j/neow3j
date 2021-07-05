package io.neow3j.devpack;

import io.neow3j.script.InteropService;
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
    @Syscall(InteropService.SYSTEM_CRYPTO_CHECKSIG)
    public static native boolean checkSig(ECPoint publicKey, ByteString signature);

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
    @Syscall(InteropService.SYSTEM_CRYPTO_CHECKMULTISIG)
    public static native boolean checkMultisig(ECPoint[] publicKeys, ByteString[] signatures);

}
