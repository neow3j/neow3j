package io.neow3j.crypto;

import io.neow3j.crypto.transaction.RawVerificationScript;
import io.neow3j.utils.Numeric;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.neow3j.crypto.Hash.sha256AndThenRipemd160;
import static io.neow3j.crypto.KeyUtils.PRIVATE_KEY_SIZE;
import static io.neow3j.crypto.KeyUtils.PUBLIC_KEY_SIZE;
import static io.neow3j.crypto.KeyUtils.toAddress;
import static io.neow3j.crypto.SecurityProviderChecker.addBouncyCastle;
import static io.neow3j.utils.ArrayUtils.concatenate;

/**
 * Crypto key utilities.
 */
public class Keys {

    static {
        addBouncyCastle();
    }

    private Keys() {
    }

    /**
     * Create a keypair using SECP-256r1 curve.
     * <p>
     * <p>Private keypairs are encoded using PKCS8
     * <p>
     * <p>Private keys are encoded using X.509
     */
    static KeyPair createSecp256r1KeyPair() throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256r1");
        keyPairGenerator.initialize(ecGenParameterSpec, SecureRandomUtils.secureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    public static ECKeyPair createEcKeyPair() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair keyPair = createSecp256r1KeyPair();
        return ECKeyPair.create(keyPair);
    }

    public static String getAddress(ECKeyPair ecKeyPair) {
        return getAddress(ecKeyPair.getPublicKey());
    }

    public static String getAddress(BigInteger publicKey) {
        return getAddress(Numeric.toHexStringNoPrefix(publicKey));
    }

    public static String getAddress(String publicKeyWithNoPrefix) {
        return getAddress(Numeric.hexStringToByteArray(publicKeyWithNoPrefix));
    }

    public static String getAddress(byte[] publicKey) {
        byte[] scriptHash = getScriptHashFromPublicKey(publicKey);
        return toAddress(scriptHash);
    }

    public static String getMultiSigAddress(int amountSignatures, BigInteger... publicKeys) {
        byte[][] pubKeysArray = new byte[publicKeys.length][];
        for (int i = 0; i < publicKeys.length; i++) {
            pubKeysArray[i] = publicKeys[i].toByteArray();
        }
        byte[] scriptHash = getScriptHashFromPublicKey(amountSignatures, pubKeysArray);
        return toAddress(scriptHash);
    }

    public static String getMultiSigAddress(int amountSignatures, byte[]... publicKeys) {
        byte[] scriptHash = getScriptHashFromPublicKey(amountSignatures, publicKeys);
        return toAddress(scriptHash);
    }

    public static byte[] getScriptHashFromPublicKey(byte[] publicKey) {
        return getScriptHashFromPublicKey(1, publicKey);
    }

    public static byte[] getScriptHashFromPublicKey(int amountSignatures, byte[]... publicKeys) {
        byte[][] encodedPublicKeys = new byte[publicKeys.length][];
        for (int i = 0; i < publicKeys.length; i++) {
            // if public key is not encoded, then
            // convert to the encoded one
            if (!isPublicKeyEncoded(publicKeys[i])) {
                encodedPublicKeys[i] = getPublicKeyEncoded(publicKeys[i]);
            } else {
                encodedPublicKeys[i] = Arrays.copyOf(publicKeys[i], publicKeys[i].length);
            }
        }
        RawVerificationScript verificationScript = getVerificationScriptFromPublicKey(amountSignatures, encodedPublicKeys);
        byte[] hash160 = sha256AndThenRipemd160(verificationScript.toArray());
        return hash160;
    }

    public static RawVerificationScript getVerificationScriptFromPublicKey(byte[] publicKey) {
        return getVerificationScriptFromPublicKey(1, publicKey);
    }

    public static RawVerificationScript getVerificationScriptFromPublicKey(int amountSignatures, BigInteger... publicKeys) {
        List<BigInteger> pubKeysBigInt = Arrays.asList(publicKeys);
        return new RawVerificationScript(pubKeysBigInt, amountSignatures);
    }

    public static RawVerificationScript getVerificationScriptFromPublicKey(int amountSignatures, byte[]... publicKeys) {
        ArrayList<BigInteger> pubKeysBigInt = new ArrayList<>(publicKeys.length);
        for (byte[] pubKey : publicKeys) {
            pubKeysBigInt.add(Numeric.toBigInt(pubKey));
        }
        return new RawVerificationScript(pubKeysBigInt, amountSignatures);
    }

    public static byte[] getPublicKeyEncoded(byte[] publicKeyNotEncoded) {
        // converting to unsigned (Java byte's type is signed)
        int[] publicKeyArray = new int[publicKeyNotEncoded.length];
        for (int i = 0; i < publicKeyNotEncoded.length; i++) {
            publicKeyArray[i] = publicKeyNotEncoded[i] & 0xFF;
        }
        // based on: https://tools.ietf.org/html/rfc5480#section-2.2
        if (publicKeyArray[64] % 2 == 1) {
            return concatenate(new byte[]{0x03}, Arrays.copyOfRange(publicKeyNotEncoded, 1, PUBLIC_KEY_SIZE));
        } else {
            return concatenate(new byte[]{0x02}, Arrays.copyOfRange(publicKeyNotEncoded, 1, PUBLIC_KEY_SIZE));
        }
    }

    public static boolean isPublicKeyEncoded(byte[] publicKey) {
        if (publicKey.length > 1 && publicKey[0] != 0x04) {
            return true;
        }
        return false;
    }

    public static byte[] serialize(ECKeyPair ecKeyPair) {
        byte[] privateKey = Numeric.toBytesPadded(ecKeyPair.getPrivateKey(), PRIVATE_KEY_SIZE);
        byte[] publicKey = Numeric.toBytesPadded(ecKeyPair.getPublicKey(), PUBLIC_KEY_SIZE);

        byte[] result = Arrays.copyOf(privateKey, PRIVATE_KEY_SIZE + PUBLIC_KEY_SIZE);
        System.arraycopy(publicKey, 0, result, PRIVATE_KEY_SIZE, PUBLIC_KEY_SIZE);
        return result;
    }

    public static ECKeyPair deserialize(byte[] input) {
        if (input.length != PRIVATE_KEY_SIZE + PUBLIC_KEY_SIZE) {
            throw new RuntimeException("Invalid input key size");
        }

        BigInteger privateKey = Numeric.toBigInt(input, 0, PRIVATE_KEY_SIZE);
        BigInteger publicKey = Numeric.toBigInt(input, PRIVATE_KEY_SIZE, PUBLIC_KEY_SIZE);

        return new ECKeyPair(privateKey, publicKey);
    }
}
