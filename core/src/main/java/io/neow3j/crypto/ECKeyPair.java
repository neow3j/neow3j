package io.neow3j.crypto;

import com.fasterxml.jackson.annotation.JsonValue;
import io.neow3j.constants.NeoConstants;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.Hash160;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Objects;

import static io.neow3j.crypto.SecurityProviderChecker.addBouncyCastle;
import static io.neow3j.script.ScriptBuilder.buildVerificationScript;
import static io.neow3j.utils.Numeric.cleanHexPrefix;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.toBytesPadded;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;

/**
 * Elliptic Curve SECP-256r1 generated key pair.
 */
public class ECKeyPair {

    static {
        addBouncyCastle();
    }

    private final ECPrivateKey privateKey;
    private final ECPublicKey publicKey;

    public ECKeyPair(ECPrivateKey privateKey, ECPublicKey publicKey) {
        if (privateKey == null) {
            throw new IllegalArgumentException("A ECKeyPair cannot be created without a private key.");
        }
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    /**
     * @return the private key of this EC key pair.
     */
    public ECPrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * @return the public key of this EC key pair.
     */
    public ECPublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Constructs the NEO address from this key pair's public key.
     * <p>
     * The address is constructed ad hoc each time this method is called.
     *
     * @return the NEO address of the public key.
     */
    public String getAddress() {
        return getScriptHash().toAddress();
    }

    /**
     * Constructs the script hash from this key pairs public key.
     * <p>
     * The script hash is constructed ad hoc each time this method is called.
     *
     * @return the script hash of the public key.
     */
    public Hash160 getScriptHash() {
        byte[] script = buildVerificationScript(publicKey.getEncoded(true));
        return Hash160.fromScript(script);
    }

    /**
     * Sign a hash with the private key of this key pair.
     *
     * @param messageHash the hash to sign.
     * @return a raw {@link BigInteger} array with the signature.
     */
    public BigInteger[] sign(byte[] messageHash) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKey.getInt(),
                NeoConstants.secp256r1DomainParams());
        signer.init(true, privKey);
        return signer.generateSignature(messageHash);
    }

    /**
     * Sign a hash with the private key of this key pair.
     *
     * @param messageHash the hash to sign.
     * @return an {@link ECDSASignature} of the hash.
     */
    public ECDSASignature signAndGetECDSASignature(byte[] messageHash) {
        BigInteger[] components = sign(messageHash);
        // in bitcoin and ethereum we would/could use .toCanonicalised(), but not in NEO, AFAIK
        return new ECDSASignature(components[0], components[1]);
    }

    /**
     * Sign a hash with the private key of this key pair.
     *
     * @param messageHash the hash to sign.
     * @return a byte array with the canonicalized signature.
     */
    public byte[] signAndGetArrayBytes(byte[] messageHash) {
        BigInteger[] components = sign(messageHash);
        byte[] signature = new byte[64];
        System.arraycopy(BigIntegers.asUnsignedByteArray(32, components[0]), 0, signature, 0, 32);
        System.arraycopy(BigIntegers.asUnsignedByteArray(32, components[1]), 0, signature, 32, 32);
        return signature;
    }

    /**
     * Creates an EC key pair from the given key pair.
     *
     * @param keyPair the key pair.
     * @return the EC key pair.
     */
    public static ECKeyPair create(KeyPair keyPair) {
        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();

        return new ECKeyPair(new ECPrivateKey(privateKey.getD()), new ECPublicKey(publicKey.getQ()));
    }

    /**
     * Creates an EC key pair from a private key.
     *
     * @param privateKey the private key.
     * @return the EC key pair.
     */
    public static ECKeyPair create(ECPrivateKey privateKey) {
        return new ECKeyPair(privateKey, Sign.publicKeyFromPrivate(privateKey));
    }

    /**
     * Creates a secp256r1 EC key pair from the private key.
     *
     * @param privateKey the private key.
     * @return the EC key pair.
     */
    public static ECKeyPair create(BigInteger privateKey) {
        return create(new ECPrivateKey(privateKey));
    }

    /**
     * Creates a secp256r1 EC key pair from the private key.
     *
     * @param privateKey the private key.
     * @return the EC key pair.
     */
    public static ECKeyPair create(byte[] privateKey) {
        return create(new ECPrivateKey(privateKey));
    }

    /**
     * <p>Create a fresh secp256r1 EC keypair.</p>
     *
     * @return the created {@link ECKeyPair}.
     * @throws InvalidAlgorithmParameterException throws if the algorithm parameter used is invalid.
     * @throws NoSuchAlgorithmException           throws if the encryption algorithm is not available in the
     *                                            specified provider.
     * @throws NoSuchProviderException            throws if the provider is not available.
     */
    public static ECKeyPair createEcKeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException {
        KeyPair keyPair = createSecp256r1KeyPair();
        return create(keyPair);
    }

    private static KeyPair createSecp256r1KeyPair() throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);

        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256r1");
        keyPairGenerator.initialize(ecGenParameterSpec, SecureRandomUtils.secureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * @return the WIF of this ECKeyPair.
     */
    public String exportAsWIF() {
        return WIF.getWIFFromPrivateKey(getPrivateKey().getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ECKeyPair that = (ECKeyPair) o;
        return Objects.equals(this.privateKey, that.privateKey)
                && Objects.equals(publicKey, that.publicKey);
    }

    @Override
    public int hashCode() {
        int result = privateKey != null ? privateKey.hashCode() : 0;
        result = 31 * result + (publicKey != null ? publicKey.hashCode() : 0);
        return result;
    }

    public static class ECPrivateKey {

        private byte[] privateKey;

        /**
         * Creates a ECPrivateKey instance from the given private key.
         *
         * @param key the private key.
         */
        public ECPrivateKey(BigInteger key) {
            if (key.toString(16).length() > NeoConstants.PRIVATE_KEY_SIZE * 2) {
                throw new IllegalArgumentException("Private key must fit into" + NeoConstants.PRIVATE_KEY_SIZE
                        + " bytes, but required " + key.toString(16).length() / 2 + "bytes.");
            }
            this.privateKey = toBytesPadded(key, NeoConstants.PRIVATE_KEY_SIZE);
        }

        /**
         * Creates a ECPrivateKey instance from the given private key. The bytes are interpreted as a positive
         * integer (not two's complement) in big-endian ordering.
         *
         * @param key the key's bytes.
         */
        public ECPrivateKey(byte[] key) {
            if (key.length != NeoConstants.PRIVATE_KEY_SIZE) {
                throw new IllegalArgumentException("Private key byte array must have length of "
                        + NeoConstants.PRIVATE_KEY_SIZE + " but had length " + key.length);
            }
            this.privateKey = key;
        }

        /**
         * @return this private key as an integer.
         */
        public BigInteger getInt() {
            return new BigInteger(1, this.privateKey);
        }

        /**
         * @return this private key as a byte array in big-endian order (not in two's complement).
         */
        public byte[] getBytes() {
            return this.privateKey;
        }

        /**
         * Overwrites the private key with zeros.
         * <p>
         * If this private key was generated from a byte array, that input array will be overwritten because this
         * private key simply holds a reference to that input byte array.
         */
        public void erase() {
            for (int i = 0; i < privateKey.length; i++) {
                this.privateKey[i] = 0;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ECPrivateKey that = (ECPrivateKey) o;
            return Arrays.equals(privateKey, that.privateKey);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.privateKey);
        }

    }

    public static class ECPublicKey extends NeoSerializable implements Comparable<ECPublicKey> {

        private ECPoint ecPoint;

        public ECPublicKey() {
        }

        /**
         * Creates a new instance from the given encoded public key in hex format. The public key must be encoded as
         * defined in section 2.3.3 of <a href="http://www.secg.org/sec1-v2.pdf">SEC1</a>.
         * It can be in compressed or uncompressed format.
         * <p>
         * Assumes the public key EC point is from the secp256r1 named curve.
         *
         * @param publicKey the public key in hex format.
         */
        public ECPublicKey(String publicKey) {
            this(hexStringToByteArray(cleanHexPrefix(publicKey)));
        }

        /**
         * Creates a new {@link ECPublicKey} based on an EC point ({@link ECPoint}).
         *
         * @param ecPoint the EC point (x,y) to construct the public key.
         */
        public ECPublicKey(ECPoint ecPoint) {
            if (!ecPoint.getCurve().equals(NeoConstants.secp256r1CurveParams().getCurve())) {
                throw new IllegalArgumentException("Given EC point is not of the required curve.");
            }
            this.ecPoint = ecPoint;
        }

        /**
         * Creates a new instance from the given encoded public key. The public key must be encoded as defined in
         * section 2.3.3 of <a href="http://www.secg.org/sec1-v2.pdf">SEC1</a>.
         * It can be in compressed or uncompressed format.
         * <p>
         * Assumes the public key EC point is from the secp256r1 named curve.
         *
         * @param publicKey the public key.
         */
        public ECPublicKey(byte[] publicKey) {
            this.ecPoint = NeoConstants.secp256r1CurveParams().getCurve().decodePoint(publicKey);
        }

        /**
         * Creates a new instance from the given encoded public key. The public key must be encoded as defined in
         * section 2.3.3 of <a href="http://www.secg.org/sec1-v2.pdf">SEC1</a>.
         * It can be in compressed or uncompressed format.
         *
         * @param publicKey the public key.
         */
        public ECPublicKey(BigInteger publicKey) {
            this(toBytesPadded(publicKey, NeoConstants.PUBLIC_KEY_SIZE_COMPRESSED));
        }

        /**
         * Gets this public key's elliptic curve point encoded as defined in section 2.3.3 of
         * <a href="http://www.secg.org/sec1-v2.pdf">SEC1</a>.
         *
         * @param compressed if the EC point should be encoded in compressed or uncompressed format.
         * @return the encoded public key.
         */
        public byte[] getEncoded(boolean compressed) {
            return ecPoint.getEncoded(compressed);
        }

        /**
         * Gets this public key's elliptic curve point encoded as defined in section 2.3.3 of
         * <a href="http://www.secg.org/sec1-v2.pdf">SEC1</a> in compressed format as hexadecimal.
         *
         * @return the encoded public key in compressed format as hexadecimal without a prefix.
         */
        @JsonValue
        public String getEncodedCompressedHex() {
            return toHexStringNoPrefix(getEncoded(true));
        }

        /**
         * @return the EC point of this public key.
         */
        public ECPoint getECPoint() {
            return this.ecPoint;
        }

        /**
         * Deserializes an EC point, which is assumed to be on the secp256r1 curve.
         *
         * @param reader the binary reader to read bytes from.
         * @throws DeserializationException if an error occurs while deserialization.
         */
        @Override
        public void deserialize(BinaryReader reader) throws DeserializationException {
            try {
                ecPoint = NeoConstants.secp256r1CurveParams().getCurve()
                        .decodePoint(reader.readBytes(NeoConstants.PUBLIC_KEY_SIZE_COMPRESSED));
            } catch (IOException e) {
                throw new DeserializationException();
            }
        }

        @Override
        public void serialize(BinaryWriter writer) throws IOException {
            writer.write(getEncoded(true));
        }

        @Override
        public int getSize() {
            return this.ecPoint.isInfinity() ? 1 : NeoConstants.PUBLIC_KEY_SIZE_COMPRESSED;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ECPublicKey that = (ECPublicKey) o;
            return Objects.equals(this.ecPoint, that.ecPoint);
        }

        @Override
        public int hashCode() {
            return ecPoint.hashCode();
        }

        @Override
        public int compareTo(ECPublicKey o) {
            if (this.equals(o)) {
                return 0;
            }
            int comparedXCoord = this.getECPoint().getXCoord().toBigInteger()
                    .compareTo(o.getECPoint().getXCoord().toBigInteger());
            if (comparedXCoord != 0) {
                return comparedXCoord;
            }
            return this.getECPoint().getYCoord().toBigInteger()
                    .compareTo(o.getECPoint().getYCoord().toBigInteger());
        }

        @Override
        public String toString() {
            return "ECPublicKey{" + getEncodedCompressedHex() + "}";
        }

    }

}
