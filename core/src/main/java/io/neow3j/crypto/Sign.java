package io.neow3j.crypto;

import static io.neow3j.utils.Assertions.verifyPrecondition;

import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair.ECPrivateKey;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.math.ec.custom.sec.SecP256R1Curve;

/**
 * <p>Transaction signing logic.</p>
 * <br>
 * <p>Originally adapted from the
 * <a href="https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/core/ECKey.java">
 * BitcoinJ ECKey</a> implementation.
 * <br>
 * <p>Class from web3j project, and adapted to neow3j project (with NEO requirements).</p>
 */
public class Sign {

    private static final int LOWER_REAL_V = 27;

    public static SignatureData signMessage(byte[] message, ECKeyPair keyPair) {
        return signMessage(message, keyPair, true);
    }

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
            throw new RuntimeException(
                    "Could not construct a recoverable key. This should never happen.");
        }

        int headerByte = recId + 27;

        // 1 header + 32 bytes for R + 32 bytes for S
        byte v = (byte) headerByte;
        byte[] r = Numeric.toBytesPadded(sig.r, 32);
        byte[] s = Numeric.toBytesPadded(sig.s, 32);

        return new SignatureData(v, r, s);
    }

    /**
     * <p>Given the components of a signature and a selector value, recover and return the public
     * key that generated the signature according to the algorithm in SEC1v2 section 4.1.6.</p>
     * <br>
     * <p>The recId is an index from 0 to 3 which indicates which of the 4 possible keys is the
     * correct one. Because the key recovery operation yields multiple potential keys, the correct
     * key must either be stored alongside the signature, or you must be willing to try each recId
     * in turn until you find one that outputs the key you are expecting.</p>
     * <br>
     * <p>If this method returns null it means recovery was not possible and recId should be
     * iterated.</p>
     * <br>
     * <p>Given the above two points, a correct usage of this method is inside a for loop from
     * 0 to 3, and if the output is null OR a key that is not the one you expect, you try again with
     * the next recId.</p>
     *
     * @param recId   Which possible key to recover.
     * @param sig     the R and S components of the signature, wrapped.
     * @param message Hash of the data that was signed.
     * @return An ECKey containing only the public part, or null if recovery wasn't possible.
     */
    public static ECPublicKey recoverFromSignature(int recId, ECDSASignature sig, byte[] message) {
        verifyPrecondition(recId >= 0, "recId must be positive");
        verifyPrecondition(sig.r.signum() >= 0, "r must be positive");
        verifyPrecondition(sig.s.signum() >= 0, "s must be positive");
        verifyPrecondition(message != null, "message cannot be null");

        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
        //   1.1 Let x = r + jn
        BigInteger n = NeoConstants.curve().getN();  // Curve order.
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = sig.r.add(i.multiply(n));
        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion
        //        routine specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R
        //        using the conversion routine specified in Section 2.3.4. If this conversion
        //        routine outputs "invalid", then do another iteration of Step 1.
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
        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via
        //        iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n).
        // In the above equation ** is point multiplication and + is point addition (the EC group
        // operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For
        // example the additive inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and
        // -3 mod 11 = 8.
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = sig.r.modInverse(n);
        BigInteger srInv = rInv.multiply(sig.s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint q = ECAlgorithms.sumOfTwoMultiplies(NeoConstants.curve().getG(), eInvrInv, R, srInv);

        return new ECPublicKey(q);
    }

    /**
     * <p>Decompress a compressed public key (x co-ord and low-bit of y-coord).</p>
     * <br>
     * <p>Based on: https://tools.ietf.org/html/rfc5480#section-2.2</p>
     */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN,
                1 + x9.getByteLength(NeoConstants.curve().getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        return NeoConstants.curve().getCurve().decodePoint(compEnc);
    }

    /**
     * Given an arbitrary piece of text and an NEO message signature encoded in bytes, returns the
     * public key that was used to sign it. This can then be compared to the expected public key to
     * determine if the signature was correct.
     *
     * @param message       encoded message.
     * @param signatureData The message signature components
     * @return the public key used to sign the message
     * @throws SignatureException If the public key could not be recovered or if there was a
     *                            signature format error.
     */
    public static ECPublicKey signedMessageToKey(
            byte[] message, SignatureData signatureData) throws SignatureException {

        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        verifyPrecondition(r != null && r.length == 32, "r must be 32 bytes");
        verifyPrecondition(s != null && s.length == 32, "s must be 32 bytes");

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
     * @param privKey the private key to derive the public key from
     * @return BigInteger encoded public key
     */
    public static ECPublicKey publicKeyFromPrivate(ECPrivateKey privKey) {
        return new ECPublicKey(publicPointFromPrivateKey(privKey));
    }

    /**
     * Returns public key point from the given private key.
     *
     * @param privKey The private key as BigInteger
     * @return The ECPoint object representation of the public key based on the given private key
     */
    public static ECPoint publicPointFromPrivateKey(ECPrivateKey privKey) {
        BigInteger key = privKey.getInt();
        /*
         * TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group
         * order, but that could change in future versions.
         */
        if (key.bitLength() > NeoConstants.curve().getN().bitLength()) {
            key = key.mod(NeoConstants.curve().getN());
        }
        return new FixedPointCombMultiplier().multiply(NeoConstants.curve().getG(), key)
                                             .normalize();
    }

    /**
     * Recovers the address that created the given signature on the given message.
     * <p>
     * If the message is a Neo transaction, then make sure that it was serialized without the
     * verification and invocation script attached (i.e. without the signature).
     *
     * @param signatureData The signature.
     * @param message       The message for which the signature was created.
     * @return the address that produced the signature data from the transaction.
     * @throws SignatureException throws if the signature is invalid.
     */
    public static String recoverSigningAddress(byte[] message, SignatureData signatureData)
            throws SignatureException {

        byte v = signatureData.getV();
        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        SignatureData signatureDataV = new Sign.SignatureData(getRealV(v), r, s);
        ECPublicKey key = Sign.signedMessageToKey(message, signatureDataV);
        return ScriptHash.fromPublicKey(key.getEncoded(true)).toAddress();
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
            return new SignatureData(
                    (byte) 0x00,
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
