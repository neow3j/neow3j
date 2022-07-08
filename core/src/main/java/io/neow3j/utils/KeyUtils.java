package io.neow3j.utils;

import io.neow3j.constants.NeoConstants;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

import static io.neow3j.constants.NeoConstants.secp256r1DomainParams;

/**
 * Key utilities.
 */
public class KeyUtils {

    public static byte[] publicKeyIntegerToByteArray(BigInteger publicKey) {
        return Numeric.toBytesPadded(publicKey, NeoConstants.PUBLIC_KEY_SIZE_COMPRESSED);
    }

    public static byte[] privateKeyIntegerToByteArray(BigInteger privateKey) {
        return Numeric.toBytesPadded(privateKey, NeoConstants.PRIVATE_KEY_SIZE);
    }

    /**
     * Transforms the given public key into its compressed format if not already compressed.
     * <p>
     * The given public key must be encoded as defined in section 2.3.3 of
     * <a href="http://www.secg.org/sec1-v2.pdf">SEC1</a>.
     *
     * @param notCompressedPubKey the uncompressed public key.
     * @return the public key encoded in compressed format.
     */
    public static byte[] compressPublicKey(byte[] notCompressedPubKey) {
        ECPoint point = secp256r1DomainParams().getCurve().decodePoint(notCompressedPubKey);
        return point.getEncoded(true);
    }

    /**
     * Checks if the given public key is in compressed format.
     * <p>
     * The given public key must be byte-encoded as defined in section 2.3.3 of
     * <a href="http://www.secg.org/sec1-v2.pdf">SEC1</a>.
     *
     * @param publicKey the public key to check.
     * @return true if the key is compressed. False, otherwise.
     */
    public static boolean isPublicKeyCompressed(byte[] publicKey) {
        // Check if the key can successfully be decoded.
        secp256r1DomainParams().getCurve().decodePoint(publicKey);
        return publicKey[0] == 0x02 || publicKey[0] == 0x03;
    }

}
