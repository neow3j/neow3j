package com.axlabs.neow3j.crypto;

import com.axlabs.neow3j.constants.NeoConstants;
import com.axlabs.neow3j.crypto.transaction.RawVerificationScript;
import com.axlabs.neow3j.utils.Numeric;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.KeyPair;

import static com.axlabs.neow3j.crypto.KeyUtils.PUBLIC_KEY_SIZE;


/**
 * Elliptic Curve SECP-256r1 generated key pair.
 */
public class ECKeyPair {
    private final BigInteger privateKey;
    private final BigInteger publicKey;

    public ECKeyPair(BigInteger privateKey, BigInteger publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    /**
     * Sign a hash with the private key of this key pair.
     *
     * @param transactionHash the hash to sign
     * @return A raw {@link BigInteger} array with the signature
     */
    public BigInteger[] sign(byte[] transactionHash) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKey, NeoConstants.CURVE);
        signer.init(true, privKey);
        return signer.generateSignature(Hash.sha256(transactionHash));
    }

    /**
     * Sign a hash with the private key of this key pair.
     *
     * @param transactionHash the hash to sign
     * @return An {@link ECDSASignature} of the hash
     */
    public ECDSASignature signAndGetECDSASignature(byte[] transactionHash) {
        BigInteger[] components = sign(transactionHash);
        return new ECDSASignature(components[0], components[1]).toCanonicalised();
    }

    /**
     * Sign a hash with the private key of this key pair.
     *
     * @param transactionHash the hash to sign
     * @return A byte array with the canonicalized signature
     */
    public byte[] signAndGetArrayBytes(byte[] transactionHash) {
        BigInteger[] components = sign(transactionHash);
        byte[] signature = new byte[64];
        System.arraycopy(BigIntegers.asUnsignedByteArray(32, components[0]), 0, signature, 0, 32);
        System.arraycopy(BigIntegers.asUnsignedByteArray(32, components[1]), 0, signature, 32, 32);
        return signature;
    }

    public static ECKeyPair create(KeyPair keyPair) {
        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();

        BigInteger privateKeyValue = privateKey.getD();

        byte[] publicKeyBytes = publicKey.getQ().getEncoded(true);
        BigInteger publicKeyValue = new BigInteger(1, publicKeyBytes);

        return new ECKeyPair(privateKeyValue, publicKeyValue);
    }

    public static ECKeyPair create(BigInteger privateKey) {
        return new ECKeyPair(privateKey, Sign.publicKeyFromPrivate(privateKey));
    }

    public static ECKeyPair create(byte[] privateKey) {
        return create(Numeric.toBigInt(privateKey));
    }

    public byte[] toScriptHash() {
        return KeyUtils.toScriptHash(Keys.getAddress(this));
    }

    public RawVerificationScript getVerificationScriptFromPublicKey() {
        return Keys.getVerificationScriptFromPublicKey(
                Numeric.toBytesPadded(this.getPublicKey(), PUBLIC_KEY_SIZE));
    }

    public byte[] getVerificationScriptAsArrayFromPublicKey() {
        return Keys.getVerificationScriptFromPublicKey(
                Numeric.toBytesPadded(this.getPublicKey(), PUBLIC_KEY_SIZE)).toArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ECKeyPair ecKeyPair = (ECKeyPair) o;

        if (privateKey != null
                ? !privateKey.equals(ecKeyPair.privateKey) : ecKeyPair.privateKey != null) {
            return false;
        }

        return publicKey != null
                ? publicKey.equals(ecKeyPair.publicKey) : ecKeyPair.publicKey == null;
    }

    @Override
    public int hashCode() {
        int result = privateKey != null ? privateKey.hashCode() : 0;
        result = 31 * result + (publicKey != null ? publicKey.hashCode() : 0);
        return result;
    }
}
