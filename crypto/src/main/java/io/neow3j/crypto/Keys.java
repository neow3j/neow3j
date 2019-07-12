package io.neow3j.crypto;

import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptBuilder;
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

import static io.neow3j.constants.NeoConstants.MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT;
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

    public static String getMultiSigAddress(int amountSignatures, List<BigInteger> publicKeys) {
        return getMultiSigAddress(amountSignatures, publicKeys.toArray(new BigInteger[0]));
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
        byte[] verificationScript;
        if (publicKeys.length == 1) {
            verificationScript = getVerificationScriptFromPublicKey(publicKeys[0]);
        } else {
            verificationScript = getVerificationScriptFromPublicKeys(amountSignatures, publicKeys);
        }
        return Hash.getScriptHash(verificationScript);
    }

    /**
     * Checks if the given public key is in encoded format and encodes it if not.
     */
    public static byte[] checkAndEncodePublicKey(byte[] publicKey) {
        if (!isPublicKeyEncoded(publicKey)) {
            return getPublicKeyEncoded(publicKey);
        } else {
            return publicKey;
        }
    }

    public static byte[] publicKeyBigIntegerToByteArray(BigInteger publicKey) {
        // TODO 12.07.19 claude:
        // Check if BigInteger.toByteArray() is always giving the desired result.
        return publicKey.toByteArray();
    }

    /**
     * Creates the verification script for the given key.
     * Checks if the key is in encoded format, and encodes it if not.
     * @return the verification script.
     */
    public static byte[] getVerificationScriptFromPublicKey(BigInteger publicKey) {
        byte[] publicKeyBytes = checkAndEncodePublicKey(publicKeyBigIntegerToByteArray(publicKey));
        return getVerificationScriptFromPublicKeyEncoded(publicKeyBytes);
    }

    /**
     * Creates the verification script for the given key.
     * Checks if the key is in encoded format, and encodes it if not.
     * @return the verification script.
     */
    public static byte[] getVerificationScriptFromPublicKey(byte[] publicKey) {
        publicKey = checkAndEncodePublicKey(publicKey);
        return getVerificationScriptFromPublicKeyEncoded(publicKey);
    }

    private static byte[] getVerificationScriptFromPublicKeyEncoded(byte[] encodedPublicKey) {
        return new ScriptBuilder()
                .pushData(encodedPublicKey)
                .opCode(OpCode.CHECKSIG)
                .toArray();
    }

    /**
     * Creates the multi-sig verification script for the given keys and the signing threshold.
     * Checks if the keys are in encoded format, and encodes them if not.
     * @return the multi-sig verification script.
     */
    public  static byte[] getVerificationScriptFromPublicKeys(int signingThreshold,
                                                              List<BigInteger> publicKeys) {

       return getVerificationScriptFromPublicKeys(
               signingThreshold,
               publicKeys.stream()
                       .map(Keys::publicKeyBigIntegerToByteArray)
                       .toArray(byte[][]::new)
       );
    }

    /**
     * Creates the multi-sig verification script for the given keys and the signing threshold.
     * Checks if the keys are in encoded format, and encodes them if not.
     * @return the multi-sig verification script.
     */
    public  static byte[] getVerificationScriptFromPublicKeys(int signingThreshold,
                                                              byte[]... publicKeys) {

        List<byte[]> encodedPublicKeys = new ArrayList<>(publicKeys.length);
        for (byte[] key : publicKeys) {
            encodedPublicKeys.add(checkAndEncodePublicKey(key));
        }

        return getVerificationScriptFromPublicKeysEncoded(signingThreshold, encodedPublicKeys);
    }

    private static byte[] getVerificationScriptFromPublicKeysEncoded(int signingThreshold,
                                                                    List<byte[]> encodedPublicKeys) {

        if (signingThreshold < 2 || signingThreshold > encodedPublicKeys.size()) {
            throw new IllegalArgumentException("Signing threshold must be at least 2 and not " +
                    "higher than the number of public keys.");
        }
        if (encodedPublicKeys.size() > MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT) {
            throw new IllegalArgumentException("At max " + MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT +
                    " public keys can take part in a multi-sig account");
        }
        ScriptBuilder builder = new ScriptBuilder()
                .pushInteger(signingThreshold);
        encodedPublicKeys.forEach(key -> builder
                .pushData(key));
        return builder
                .pushInteger(encodedPublicKeys.size())
                .opCode(OpCode.CHECKMULTISIG)
                .toArray();
    }


    public static byte[] getPublicKeyEncoded(byte[] publicKeyNotEncoded) {
        // converting to unsigned (Java byte's type is signed)
        int[] publicKeyArray = new int[publicKeyNotEncoded.length];
        for (int i = 0; i < publicKeyNotEncoded.length; i++) {
            publicKeyArray[i] = publicKeyNotEncoded[i] & 0xFF;
        }
        // based on: https://tools.ietf.org/html/rfc5480#section-2.2
        if (publicKeyArray[64] % 2 == 1) {
            return concatenate(new byte[]{0x03}, Arrays.copyOfRange(publicKeyNotEncoded, 1, NeoConstants.PUBLIC_KEY_SIZE));
        } else {
            return concatenate(new byte[]{0x02}, Arrays.copyOfRange(publicKeyNotEncoded, 1, NeoConstants.PUBLIC_KEY_SIZE));
        }
    }

    public static boolean isPublicKeyEncoded(byte[] publicKey) {
        if (publicKey.length > 1 && publicKey[0] != 0x04) {
            return true;
        }
        return false;
    }

    public static byte[] serialize(ECKeyPair ecKeyPair) {
        byte[] privateKey = Numeric.toBytesPadded(ecKeyPair.getPrivateKey(), NeoConstants.PRIVATE_KEY_SIZE);
        byte[] publicKey = Numeric.toBytesPadded(ecKeyPair.getPublicKey(), NeoConstants.PUBLIC_KEY_SIZE);

        byte[] result = Arrays.copyOf(privateKey, NeoConstants.PRIVATE_KEY_SIZE + NeoConstants.PUBLIC_KEY_SIZE);
        System.arraycopy(publicKey, 0, result, NeoConstants.PRIVATE_KEY_SIZE, NeoConstants.PUBLIC_KEY_SIZE);
        return result;
    }

    public static ECKeyPair deserialize(byte[] input) {
        if (input.length != NeoConstants.PRIVATE_KEY_SIZE + NeoConstants.PUBLIC_KEY_SIZE) {
            throw new RuntimeException("Invalid input key size");
        }

        BigInteger privateKey = Numeric.toBigInt(input, 0, NeoConstants.PRIVATE_KEY_SIZE);
        BigInteger publicKey = Numeric.toBigInt(input, NeoConstants.PRIVATE_KEY_SIZE, NeoConstants.PUBLIC_KEY_SIZE);

        return new ECKeyPair(privateKey, publicKey);
    }

    public static String toAddress(byte[] scriptHash) {
        byte[] data = new byte[1];
        data[0] = NeoConstants.COIN_VERSION;
        byte[] dataAndScriptHash = concatenate(data, scriptHash);
        byte[] checksum = Hash.sha256(Hash.sha256(dataAndScriptHash));
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
        if (data[0] != NeoConstants.COIN_VERSION) {
            throw new IllegalArgumentException();
        }
        byte[] checksum = Hash.sha256(Hash.sha256(data, 0, 21));
        for (int i = 0; i < 4; i++) {
            if (data[data.length - 4 + i] != checksum[i]) {
                throw new IllegalArgumentException();
            }
        }
        byte[] buffer = new byte[20];
        System.arraycopy(data, 1, buffer, 0, 20);
        return buffer;
    }

    public static String scriptHashToAddress(String input) {
        byte[] inputBytes = Numeric.hexStringToByteArray(input);
        return toAddress(inputBytes);
    }
}
