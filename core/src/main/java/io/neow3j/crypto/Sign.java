package io.neow3j.crypto;

import io.neow3j.crypto.ECKeyPair.ECPrivateKey;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.types.Hash160;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.math.ec.custom.sec.SecP256R1Curve;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Arrays;

import static io.neow3j.constants.NeoConstants.secp256r1DomainParams;
import static io.neow3j.utils.Assertions.verifyPrecondition;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.bouncycastle.math.ec.ECAlgorithms.sumOfTwoMultiplies;

/**
 * Transaction signing logic.
 * <p>
 * Originally adapted from the
 * <a href="https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/core/ECKey.java">BitcoinJ ECKey</a> implementation.
 * <p>
 * Class from web3j project, and adapted to neow3j project (with NEO requirements).
 */
public class Sign {

    private static final int LOWER_REAL_V = 27;

    /**
     * Signs the hash ({@code SHA256}) of the hexadecimal message with the private key of the provided
     * {@link ECKeyPair}.
     *
     * @param messageHex the message to sign in hexadecimal format.
     * @param keyPair    the key pair that holds the private key that is used to sign the message.
     * @return the signature data.
     */
    public static SignatureData signHexMessage(String messageHex, ECKeyPair keyPair) {
        return signMessage(hexStringToByteArray(messageHex), keyPair, true);
    }

    /**
     * Signs the hash ({@code SHA256}) of the message's UTF-8 bytes with the private key of the provided
     * {@link ECKeyPair}.
     *
     * @param message the message to sign.
     * @param keyPair the key pair that holds the private key that is used to sign message.
     * @return the signature data.
     */
    public static SignatureData signMessage(String message, ECKeyPair keyPair) {
        return signMessage(message.getBytes(StandardCharsets.UTF_8), keyPair, true);
    }

    /**
     * Signs the hash ({@code SHA256}) of the message with the private key of the provided {@link ECKeyPair}.
     *
     * @param message the message to sign.
     * @param keyPair the key pair that holds the private key that is used to sign the message.
     * @return the signature data.
     */
    public static SignatureData signMessage(byte[] message, ECKeyPair keyPair) {
        return signMessage(message, keyPair, true);
    }

    /**
     * Signs the message with the private key of the provided {@link ECKeyPair}.
     *
     * @param message    the message to sign.
     * @param keyPair    the key pair that holds the private key that is used to sign the message.
     * @param needToHash whether the message should be hashed ({@code SHA256}) before signing.
     * @return the signature data.
     */
    public static SignatureData signMessage(byte[] message, ECKeyPair keyPair, boolean needToHash) {
        byte[] messageHash;
        if (needToHash) {
            messageHash = Hash.sha256(message);
        } else {
            messageHash = message;
        }

        ECDSASignature sig = keyPair.signAndGetECDSASignature(messageHash);
        // Now we have to work backwards to figure out the recId needed to recover the signature.
        int recId = -1;
        for (int i = 0; i < 4; i++) {
            ECPublicKey k = recoverFromSignature(i, sig, messageHash);
            if (k != null && k.equals(keyPair.getPublicKey())) {
                recId = i;
                break;
            }
        }
        if (recId == -1) {
            throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        }

        int headerByte = recId + 27;

        // 1 header + 32 bytes for R + 32 bytes for S
        byte v = (byte) headerByte;
        byte[] r = Numeric.toBytesPadded(sig.r, 32);
        byte[] s = Numeric.toBytesPadded(sig.s, 32);

        return new SignatureData(v, r, s);
    }

    /**
     * Given the components of a signature and a selector value, recover and return the public key that generated the
     * signature according to the algorithm in SEC1v2 section 4.1.6.
     * <p>
     * The recId is an index from 0 to 3 which indicates which of the 4 possible keys is the correct one. Because the
     * key recovery operation yields multiple potential keys, the correct key must either be stored alongside the
     * signature, or you must be willing to try each recId in turn until you find one that outputs the key you are
     * expecting.
     * <p>
     * If this method returns null it means recovery was not possible and recId should be iterated.
     * <p>
     * Given the above two points, a correct usage of this method is inside a for loop from 0 to 3, and if the output
     * is null OR a key that is not the one you expect, you try again with the next recId.
     *
     * @param recId   which possible key to recover.
     * @param sig     the R and S components of the signature, wrapped.
     * @param message the hash of the data that was signed.
     * @return an ECKey containing only the public part, or null if recovery wasn't possible.
     */
    public static ECPublicKey recoverFromSignature(int recId, ECDSASignature sig, byte[] message) {
        verifyPrecondition(recId >= 0, "recId must be positive");
        verifyPrecondition(sig.r.signum() >= 0, "r must be positive");
        verifyPrecondition(sig.s.signum() >= 0, "s must be positive");
        verifyPrecondition(message != null, "message cannot be null");

        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
        //   1.1 Let x = r + jn
        BigInteger n = secp256r1DomainParams().getN();  // Curve order.
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = sig.r.add(i.multiply(n));
        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion routine specified in
        //        Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R using the
        //        conversion routine specified in Section 2.3.4. If this conversion routine outputs "invalid", then do
        //        another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.
        BigInteger prime = SecP256R1Curve.q;
        if (x.compareTo(prime) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null;
        }
        // Compressed keys require you to know an extra bit of data about the y-coord as there are
        // two possibilities. So it's encoded in the recId.
        ECPoint R = decompressKey(x, (recId & 1) == 1);
        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers
        //        responsibility).
        if (!R.multiply(n).isInfinity()) {
            return null;
        }
        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        BigInteger e = new BigInteger(1, message);
        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n).
        // In the above equation ** is point multiplication and + is point addition (the EC group operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For example the additive
        // inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 = 8.
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = sig.r.modInverse(n);
        BigInteger srInv = rInv.multiply(sig.s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint q = sumOfTwoMultiplies(secp256r1DomainParams().getG(), eInvrInv, R, srInv);

        return new ECPublicKey(q);
    }

    /**
     * Decompress a compressed public key (x co-ord and low-bit of y-coord).
     * <p>
     * Based on: <a href="https://tools.ietf.org/html/rfc5480#section-2.2">RFC5480</a>
     */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(secp256r1DomainParams().getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        return secp256r1DomainParams().getCurve().decodePoint(compEnc);
    }

    /**
     * Given an arbitrary piece of text and an NEO message signature encoded in bytes, returns the public key that
     * was used to sign it. This can then be compared to the expected public key to determine if the signature was
     * correct.
     *
     * @param message       the encoded message.
     * @param signatureData the message signature components.
     * @return the public key used to sign the message.
     * @throws SignatureException if the public key could not be recovered or if there was a signature format error.
     */
    public static ECPublicKey signedMessageToKey(byte[] message, SignatureData signatureData)
            throws SignatureException {

        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        verifyPrecondition(r != null && r.length == 32, "r must be 32 bytes.");
        verifyPrecondition(s != null && s.length == 32, "s must be 32 bytes.");

        // unsigned byte to int
        int header = signatureData.getV() & 0xFF;
        // The header byte: 0x1B = first key with even y, 0x1C = first key with odd y,
        //                  0x1D = second key with even y, 0x1E = second key with odd y
        if (header < 27 || header > 34) {
            throw new SignatureException("Header byte out of range: " + header);
        }

        ECDSASignature sig = new ECDSASignature(
                new BigInteger(1, signatureData.getR()),
                new BigInteger(1, signatureData.getS()));

        byte[] messageHash = Hash.sha256(message);
        int recId = header - 27;
        ECPublicKey key = recoverFromSignature(recId, sig, messageHash);
        if (key == null) {
            throw new SignatureException("Could not recover public key from signature");
        }
        return key;
    }

    /**
     * Returns public key from the given private key.
     *
     * @param privKey the private key to derive the public key from.
     * @return the BigInteger-encoded public key.
     */
    public static ECPublicKey publicKeyFromPrivate(ECPrivateKey privKey) {
        return new ECPublicKey(publicPointFromPrivateKey(privKey));
    }

    /**
     * Returns public key point from the given private key.
     *
     * @param privKey the private key as BigInteger.
     * @return the ECPoint object representation of the public key based on the given private key.
     */
    public static ECPoint publicPointFromPrivateKey(ECPrivateKey privKey) {
        BigInteger key = privKey.getInt();
        /*
         * TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group order, but that
         *  could change in future versions.
         */
        if (key.bitLength() > secp256r1DomainParams().getN().bitLength()) {
            key = key.mod(secp256r1DomainParams().getN());
        }
        return new FixedPointCombMultiplier().multiply(secp256r1DomainParams().getG(), key).normalize();
    }

    /**
     * Recovers the signer's script hash that created the given signature on the given message.
     * <p>
     * If the message is a Neo transaction, then make sure that it was serialized without the verification and
     * invocation script attached (i.e. without the signature).
     *
     * @param signatureData the signature.
     * @param message       the message for which the signature was created.
     * @return the signer's script hash that produced the signature data from the transaction.
     * @throws SignatureException if the signature is invalid.
     */
    public static Hash160 recoverSigningScriptHash(byte[] message, SignatureData signatureData)
            throws SignatureException {

        byte v = signatureData.getV();
        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        SignatureData signatureDataV = new Sign.SignatureData(getRealV(v), r, s);
        ECPublicKey key = Sign.signedMessageToKey(message, signatureDataV);
        return Hash160.fromPublicKey(key.getEncoded(true));
    }

    private static byte getRealV(byte v) {
        if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) {
            return v;
        }
        byte realV = LOWER_REAL_V;
        int inc = 0;
        if ((int) v % 2 == 0) {
            inc = 1;
        }
        return (byte) (realV + inc);
    }

    /**
     * Verifies the that the signature is appropriate for the given message and public key.
     *
     * @param message     the message.
     * @param sig         the signature to verify.
     * @param pubKey      the public key.
     * @param hashMessage if the message should be hashed before verification.
     * @return true if the verification was successful. False otherwise.
     */
    public static boolean verifySignature(byte[] message, SignatureData sig, ECPublicKey pubKey, boolean hashMessage) {
        byte[] messageHash;
        if (hashMessage) {
            messageHash = Hash.sha256(message);
        } else {
            messageHash = message;
        }

        ECDSASigner verifier = new ECDSASigner();
        verifier.init(false, new ECPublicKeyParameters(pubKey.getECPoint(), secp256r1DomainParams()));
        return verifier.verifySignature(messageHash,
                new BigInteger(1, sig.getR()),
                new BigInteger(1, sig.getS()));
    }

    /**
     * Verifies the that the signature is appropriate for the given message and public key.
     * <p>
     * Beware that the message is hashed before verification.
     *
     * @param message the message.
     * @param sig     the signature to verify.
     * @param pubKey  the public key.
     * @return true if the verification was successful. False otherwise.
     */
    public static boolean verifySignature(byte[] message, SignatureData sig, ECPublicKey pubKey) {
        return verifySignature(message, sig, pubKey, true);
    }

    public static class SignatureData {
        private final byte v;
        private final byte[] r;
        private final byte[] s;

        public SignatureData(byte v, byte[] r, byte[] s) {
            this.v = v;
            this.r = r;
            this.s = s;
        }

        public static SignatureData fromByteArray(byte[] signature) {
            return fromByteArray((byte) 0x00, signature);
        }

        public static SignatureData fromByteArray(byte v, byte[] signature) {
            return new SignatureData(
                    v,
                    Arrays.copyOfRange(signature, 0, 32),
                    Arrays.copyOfRange(signature, 32, 64)
            );
        }

        public byte getV() {
            return v;
        }

        public byte[] getR() {
            return r;
        }

        public byte[] getS() {
            return s;
        }

        public byte[] getConcatenated() {
            return ArrayUtils.concatenate(r, s);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SignatureData that = (SignatureData) o;

            if (v != that.v) {
                return false;
            }
            if (!Arrays.equals(r, that.r)) {
                return false;
            }
            return Arrays.equals(s, that.s);
        }

        @Override
        public int hashCode() {
            int result = (int) v;
            result = 31 * result + Arrays.hashCode(r);
            result = 31 * result + Arrays.hashCode(s);
            return result;
        }

        @Override
        public String toString() {
            return "SignatureData{" +
                    "v=" + v +
                    ", r=" + Arrays.toString(r) +
                    ", s=" + Arrays.toString(s) +
                    '}';
        }

    }

}
