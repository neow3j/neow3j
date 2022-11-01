package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.constants.NamedCurve;
import io.neow3j.devpack.constants.NativeContract;

/**
 * Represents an interface to the native CryptoLib contract that provides cryptographic algorithms.
 */
public class CryptoLib extends ContractInterface {

    /**
     * Initializes an interface to the native CryptoLib contract.
     */
    public CryptoLib() {
        super(NativeContract.CryptoLibScriptHash);
    }

    /**
     * Calculates the SHA-256 hash of the given value.
     * <p>
     * Note, if you use this method twice to generate a hash256 byte string, that byte string will be in big-endian
     * ordering.
     *
     * @param value the bytes to hash.
     * @return the 256-bit hash.
     */
    public native ByteString sha256(ByteString value);

    /**
     * Calculates a 160-bit RIPE message digest (RIPEMD) of the given value.
     * <p>
     * Note, if you use this method in combination with {@link CryptoLib#sha256(ByteString)} to generate a hash160
     * byte string, that byte string will be in big-endian ordering.
     *
     * @param value the bytes to hash.
     * @return the 160-bit hash.
     */
    public native ByteString ripemd160(ByteString value);

    /**
     * Computes the hash value for the specified byte array using the murmur32 algorithm.
     *
     * @param data the input to compute the hash code for.
     * @param seed the seed of the murmur32 hash function.
     * @return the computed hash code.
     */
    public native ByteString murmur32(ByteString data, int seed);

    /**
     * Verifies the {@code signature} of a {@code message} with the corresponding {@code publicKey}. The {@code curve}
     * can be one of the curves defined in {@link NamedCurve}.
     *
     * @param message   the signed message.
     * @param publicKey the public key of the key pair used for signing.
     * @param signature the message signature.
     * @param curve     the curve to use in the verification.
     * @return true if the signature is valid. False, otherwise.
     */
    public native boolean verifyWithECDsa(ByteString message, ECPoint publicKey, ByteString signature, byte curve);

}
