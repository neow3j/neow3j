package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.InteropInterface;
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

    /**
     * Serializes a bls12381 point.
     *
     * @param g the point to be serialized.
     * @return the serialized point.
     */
    public native ByteString bls12381Serialize(InteropInterface g);

    /**
     * Deserializes a bls12381 point.
     *
     * @param data the point as byte array.
     * @return the deserialized point.
     */
    public native InteropInterface bls12381Deserialize(ByteString data);

    /**
     * Determines whether the specified points are equal.
     *
     * @param x the first point.
     * @param y the second point.
     * @return true if the specified points are equal. False, otherwise.
     */
    public native boolean bls12381Equal(InteropInterface x, InteropInterface y);

    /**
     * Add operation of two points.
     *
     * @param x the first point.
     * @param y the second point.
     * @return the resulting point of the addition of x and y.
     */
    public native InteropInterface bls12381Add(InteropInterface x, InteropInterface y);

    /**
     * Mul operation of gt point and multiplier.
     *
     * @param x   the point.
     * @param mul little-endian multiplier (32 bytes).
     * @param neg whether mul should be used as negative number.
     * @return the resulting point of the multiplication of x with the multiplier.
     */
    public native InteropInterface bls12381Mul(InteropInterface x, ByteString mul, boolean neg);

    /**
     * Pairing operation of the points g1 and g2.
     *
     * @param g1 the g1 point.
     * @param g2 the g2 point.
     * @return the result of the pairing operation of g1 and g2.
     */
    public native InteropInterface bls12381Pairing(InteropInterface g1, InteropInterface g2);

}
