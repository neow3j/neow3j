package com.axlabs.neow3j.crypto;

import com.axlabs.neow3j.utils.Numeric;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

import static com.axlabs.neow3j.constants.NEOConstants.COIN_VERSION;
import static com.axlabs.neow3j.crypto.Hash.sha256;
import static com.axlabs.neow3j.crypto.Hash.sha256AndThenRipemd160;
import static com.axlabs.neow3j.crypto.SecureRandomUtils.secureRandom;
import static com.axlabs.neow3j.utils.ArrayUtils.concatenate;
import static com.axlabs.neow3j.utils.ArrayUtils.reverseArray;


/**
 * Crypto key utilities.
 */
public class Keys {

    static final int PRIVATE_KEY_SIZE = 32;
    static final int PUBLIC_KEY_SIZE = 132;

    public static final int ADDRESS_SIZE = 34;
    static final int PUBLIC_KEY_LENGTH_IN_HEX = PUBLIC_KEY_SIZE << 1;
    public static final int PRIVATE_KEY_LENGTH_IN_HEX = PRIVATE_KEY_SIZE << 1;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
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

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256r1");
        keyPairGenerator.initialize(ecGenParameterSpec, secureRandom());
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

    public static byte[] getVerificationScriptFromPublicKey(byte[] publicKey) {
        return Numeric.hexStringToByteArray(new String("21" + Numeric.toHexStringNoPrefix(publicKey) + "ac"));
    }

    public static String toAddress(byte[] scriptHash) {
        byte[] data = new byte[1];
        data[0] = COIN_VERSION;
        byte[] dataAndScriptHash = concatenate(data, scriptHash);
        byte[] checksum = sha256(sha256(dataAndScriptHash));
        byte[] first4BytesCheckSum = new byte[4];
        System.arraycopy(checksum, 0, first4BytesCheckSum, 0, 4);
        byte[] dataToEncode = concatenate(dataAndScriptHash, first4BytesCheckSum);
        return Base58.encode(dataToEncode);
    }

    public static byte[] toScriptHash(String address) {
        byte[] data = Base58.decode(address);
        if (data.length != 25) {
            throw new IllegalArgumentException();
        }
        if (data[0] != COIN_VERSION) {
            throw new IllegalArgumentException();
        }
        byte[] checksum = sha256(sha256(data, 0, 21));
        for (int i = 0; i < 4; i++) {
            if (data[data.length - 4 + i] != checksum[i]) {
                throw new IllegalArgumentException();
            }
        }
        byte[] buffer = new byte[20];
        System.arraycopy(data, 1, buffer, 0, 20);
        return buffer;
    }

    public static byte[] getScriptHashFromPublicKey(byte[] publicKey) {
        // if public key is not encoded, then
        // convert to the encoded one
        if (!isPublicKeyEncoded(publicKey)) {
            publicKey = getPublicKeyEncoded(publicKey);
        }
        byte[] verificationScriptFromPublicKey = getVerificationScriptFromPublicKey(publicKey);
        byte[] hash160 = sha256AndThenRipemd160(verificationScriptFromPublicKey);
        return hash160;
    }

    public static byte[] getPublicKeyEncoded(byte[] publicKeyNotEncoded) {
        // converting to unsigned (Java byte's type is signed)
        int[] publicKeyArray = new int[publicKeyNotEncoded.length];
        for (int i = 0; i < publicKeyNotEncoded.length; i++) {
            publicKeyArray[i] = publicKeyNotEncoded[i] & 0xFF;
        }
        if (publicKeyArray[64] % 2 == 1) {
            return concatenate(new byte[]{0x03}, Arrays.copyOfRange(publicKeyNotEncoded, 1, 33));
        } else {
            return concatenate(new byte[]{0x02}, Arrays.copyOfRange(publicKeyNotEncoded, 1, 33));
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
