package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.InteropInterface;
import io.neow3j.devpack.constants.NamedCurveHash;
import io.neow3j.devpack.constants.NativeContract;
import io.neow3j.devpack.annotations.CallFlags;

import static io.neow3j.devpack.constants.CallFlags.ReadOnly;

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
     * Recovers the public key from a secp256k1 signature in a single byte array format.
     *
     * @param messageHash the hash of the message that was signed.
     * @param signature   the 65-byte signature in format: r[32] + s[32] + v[1]. 64-bytes for eip-2098, where v must
     *                    be 27 or 28.
     * @return the recovered public key in compressed format, or null if recovery fails.
     */
    @CallFlags(ReadOnly)
    public native ECPoint recoverSecp256K1(ByteString messageHash, ByteString signature);

    /**
     * Calculates the SHA-256 hash of the given value.
     * <p>
     * Note, if you use this method twice to generate a hash256 byte string, that byte string will be in big-endian
     * ordering.
     *
     * @param value the bytes to hash.
     * @return the 256-bit hash.
     */
    @CallFlags(ReadOnly)
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
    @CallFlags(ReadOnly)
    public native ByteString ripemd160(ByteString value);

    /**
     * Computes the hash value for the specified byte array using the murmur32 algorithm.
     *
     * @param data the input to compute the hash code for.
     * @param seed the seed of the murmur32 hash function.
     * @return the computed hash code.
     */
    @CallFlags(ReadOnly)
    public native ByteString murmur32(ByteString data, int seed);

    /**
     * Computes the hash value for the specified byte array using the keccak256 algorithm.
     *
     * @param data the input to compute the hash code for.
     * @return the computed hash.
     */
    @CallFlags(ReadOnly)
    public native ByteString keccak256(ByteString data);

    /**
     * Verifies the {@code signature} of a {@code message} with the corresponding {@code publicKey}. The {@code
     * curveHash} can be one of the curve/hash-algorithm combinations defined in {@link NamedCurveHash}.
     *
     * @param message   the signed message.
     * @param publicKey the public key of the key pair used for signing.
     * @param signature the message signature.
     * @param curveHash the curve/hash-algorithm combination to use in the verification.
     * @return true if the signature is valid. False, otherwise.
     */
    @CallFlags(ReadOnly)
    public native boolean verifyWithECDsa(ByteString message, ECPoint publicKey, ByteString signature, byte curveHash);

    /**
     * Verifies that a digital signature is appropriate for the provided key and message using the Ed25519 algorithm.
     *
     * @param message   the signed message.
     * @param publicKey the public key of the key pair used for signing.
     * @param signature the signature to be verified.
     * @return true if the signature is valid. False, otherwise.
     */
    @CallFlags(ReadOnly)
    public native boolean verifyWithEd25519(ByteString message, ECPoint publicKey, ByteString signature);

    /**
     * Serializes a bls12381 point.
     *
     * @param g the point to be serialized.
     * @return the serialized point.
     */
    @CallFlags(ReadOnly)
    public native ByteString bls12381Serialize(InteropInterface g);

    /**
     * Deserializes a bls12381 point.
     *
     * @param data the point as byte array.
     * @return the deserialized point.
     */
    @CallFlags(ReadOnly)
    public native InteropInterface bls12381Deserialize(ByteString data);

    /**
     * Determines whether the specified points are equal.
     *
     * @param x the first point.
     * @param y the second point.
     * @return true if the specified points are equal. False, otherwise.
     */
    @CallFlags(ReadOnly)
    public native boolean bls12381Equal(InteropInterface x, InteropInterface y);

    /**
     * Add operation of two points.
     *
     * @param x the first point.
     * @param y the second point.
     * @return the resulting point of the addition of x and y.
     */
    @CallFlags(ReadOnly)
    public native InteropInterface bls12381Add(InteropInterface x, InteropInterface y);

    /**
     * Mul operation of gt point and multiplier.
     *
     * @param x   the point.
     * @param mul little-endian multiplier (32 bytes).
     * @param neg whether mul should be used as negative number.
     * @return the resulting point of the multiplication of x with the multiplier.
     */
    @CallFlags(ReadOnly)
    public native InteropInterface bls12381Mul(InteropInterface x, ByteString mul, boolean neg);

    /**
     * Pairing operation of the points g1 and g2.
     *
     * @param g1 the g1 point.
     * @param g2 the g2 point.
     * @return the result of the pairing operation of g1 and g2.
     */
    @CallFlags(ReadOnly)
    public native InteropInterface bls12381Pairing(InteropInterface g1, InteropInterface g2);

}
