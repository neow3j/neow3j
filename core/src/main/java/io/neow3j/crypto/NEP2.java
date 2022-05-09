package io.neow3j.crypto;

import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

import static io.neow3j.crypto.Hash.hash256;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static io.neow3j.utils.ArrayUtils.getFirstNBytes;
import static io.neow3j.utils.ArrayUtils.getLastNBytes;
import static io.neow3j.utils.ArrayUtils.xor;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Provides encryption and decryption functionality according to NEP-2 specification.
 */
public class NEP2 {

    public static final int DKLEN = 64;
    public static final int NEP2_PRIVATE_KEY_LENGTH = 39;
    public static final byte NEP2_PREFIX_1 = (byte) 0x01;
    public static final byte NEP2_PREFIX_2 = (byte) 0x42;
    public static final byte NEP2_FLAGBYTE = (byte) 0xE0;
    public static final int N_STANDARD = 1 << 14;
    public static final int P_STANDARD = 8;
    public static final int R_STANDARD = 8;
    public static final ScryptParams DEFAULT_SCRYPT_PARAMS =
            new ScryptParams(N_STANDARD, P_STANDARD, R_STANDARD);

    /**
     * Decrypts the given encrypted private key in NEP-2 format with the given password and standard scrypt parameters.
     *
     * @param password   the passphrase used for decryption.
     * @param nep2String the NEP-2 ecnrypted private key.
     * @return an EC key pair constructed form the decrypted private key.
     * @throws NEP2InvalidFormat     if the encrypted NEP2 has an invalid format.
     * @throws CipherException       if failed encrypt the created wallet.
     * @throws NEP2InvalidPassphrase if the passphrase is not valid.
     */
    public static ECKeyPair decrypt(String password, String nep2String)
            throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {
        return decrypt(password, nep2String, new ScryptParams(N_STANDARD, P_STANDARD, R_STANDARD));
    }

    /**
     * Decrypts the given encrypted private key in NEP-2 format with the given password and scrypt parameters.
     *
     * @param password     the passphrase used for decryption.
     * @param nep2String   the NEP-2 ecnrypted private key.
     * @param scryptParams the scrypt parameters used for encryption.
     * @return an EC key pair constructed form the decrypted private key.
     * @throws NEP2InvalidFormat     if the encrypted NEP-2 has an invalid format.
     * @throws CipherException       if failed encrypt the created wallet.
     * @throws NEP2InvalidPassphrase if the passphrase is not valid.
     */
    public static ECKeyPair decrypt(String password, String nep2String, ScryptParams scryptParams)
            throws NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {

        byte[] nep2Data = Base58.base58CheckDecode(nep2String);

        if (nep2Data.length != NEP2_PRIVATE_KEY_LENGTH || nep2Data[0] != NEP2_PREFIX_1 ||
                nep2Data[1] != NEP2_PREFIX_2 || nep2Data[2] != NEP2_FLAGBYTE) {
            throw new NEP2InvalidFormat("Not valid NEP2 prefix.");
        }

        byte[] addressHash = new byte[4];
        // copy 4 bytes related to the address hash
        System.arraycopy(nep2Data, 3, addressHash, 0, 4);

        byte[] derivedKey = generateDerivedScryptKey(password.getBytes(UTF_8), addressHash, scryptParams, DKLEN);

        byte[] derivedKeyHalf1 = getFirstNBytes(derivedKey, 32);
        byte[] derivedKeyHalf2 = getLastNBytes(derivedKey, 32);

        byte[] encrypted = new byte[32];
        System.arraycopy(nep2Data, 7, encrypted, 0, 32);

        byte[] decrypted = performCipherOperation(Cipher.DECRYPT_MODE, encrypted, derivedKeyHalf2);

        byte[] plainPrivateKey = xor(decrypted, derivedKeyHalf1);

        ECKeyPair ecKeyPair = ECKeyPair.create(plainPrivateKey);
        byte[] calculatedAddressHash = getAddressHash(ecKeyPair);

        if (!Arrays.equals(calculatedAddressHash, addressHash)) {
            throw new NEP2InvalidPassphrase(
                    "Calculated address hash does not match the one in the provided encrypted address.");
        }

        return ecKeyPair;
    }

    /**
     * Encrypts the private key of the given key pair with the given password using standard Scrypt parameters.
     *
     * @param password  the passphrase used for encryption.
     * @param ecKeyPair the {@link ECKeyPair} to be encrypted.
     * @return the NEP-2 encrypted private key.
     * @throws CipherException if the key pair cannot be encrypted.
     */
    public static String encrypt(String password, ECKeyPair ecKeyPair) throws CipherException {
        return encrypt(password, ecKeyPair, N_STANDARD, P_STANDARD, R_STANDARD);
    }

    /**
     * Encrypts the private key of the given EC key pair following the NEP-2 standard.
     *
     * @param password     the passphrase to be used to encrypt.
     * @param ecKeyPair    the {@link ECKeyPair} to be encrypted.
     * @param scryptParams the scrypt parameters used for encryption.
     * @return the NEP-2 encrypted private key.
     * @throws CipherException if the AES/ECB/NoPadding cipher operation fails.
     */
    public static String encrypt(String password, ECKeyPair ecKeyPair, ScryptParams scryptParams)
            throws CipherException {
        return encrypt(password, ecKeyPair, scryptParams.getN(), scryptParams.getP(), scryptParams.getR());
    }

    /**
     * Encrypts the private key of the given EC key pair following the NEP-2 standard.
     *
     * @param password  the passphrase to be used to encrypt.
     * @param ecKeyPair the {@link ECKeyPair} to be encrypted.
     * @param n         the "n" parameter for {@link SCrypt#generate(byte[], byte[], int, int, int, int)} method.
     * @param p         the "p" parameter for {@link SCrypt#generate(byte[], byte[], int, int, int, int)} method.
     * @param r         the "r" parameter for {@link SCrypt#generate(byte[], byte[], int, int, int, int)} method.
     * @return the NEP-2 encrypted private key.
     * @throws CipherException if the AES/ECB/NoPadding cipher operation fails.
     */
    public static String encrypt(String password, ECKeyPair ecKeyPair, int n, int p, int r) throws CipherException {
        byte[] addressHash = getAddressHash(ecKeyPair);

        byte[] derivedKey = generateDerivedScryptKey(password.getBytes(UTF_8), addressHash, n, r, p, DKLEN);

        byte[] derivedHalf1 = getFirstNBytes(derivedKey, 32);
        byte[] derivedHalf2 = getLastNBytes(derivedKey, 32);

        byte[] encryptedHalf1 = performCipherOperation(
                Cipher.ENCRYPT_MODE,
                xorPrivateKeyAndDerivedHalf(ecKeyPair, derivedHalf1, 0, 16),
                derivedHalf2);

        byte[] encryptedHalf2 = performCipherOperation(
                Cipher.ENCRYPT_MODE,
                xorPrivateKeyAndDerivedHalf(ecKeyPair, derivedHalf1, 16, 32),
                derivedHalf2);

        byte[] prefixes = new byte[3];
        // prefix
        prefixes[0] = NEP2_PREFIX_1;
        prefixes[1] = NEP2_PREFIX_2;
        // flagbyte, which is always the same
        prefixes[2] = NEP2_FLAGBYTE;

        byte[] concatenation = concatenate(prefixes, addressHash, encryptedHalf1, encryptedHalf2);
        return Base58.base58CheckEncode(concatenation);
    }

    private static byte[] xorPrivateKeyAndDerivedHalf(ECKeyPair ecKeyPair, byte[] derivedHalf, int from, int to) {
        return xor(
                Arrays.copyOfRange(ecKeyPair.getPrivateKey().getBytes(), from, to),
                Arrays.copyOfRange(derivedHalf, from, to)
        );
    }

    private static byte[] generateDerivedScryptKey(byte[] password, byte[] salt, int n, int r, int p, int dkLen) {
        return SCrypt.generate(password, salt, n, r, p, dkLen);
    }

    private static byte[] generateDerivedScryptKey(byte[] password, byte[] salt, ScryptParams scryptParams, int dkLen) {
        return SCrypt.generate(password, salt, scryptParams.getN(), scryptParams.getR(), scryptParams.getP(), dkLen);
    }

    public static byte[] performCipherOperation(int mode, byte[] data, byte[] encryptKey) throws CipherException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", BouncyCastleProvider.PROVIDER_NAME);

            SecretKeySpec secretKeySpec = new SecretKeySpec(encryptKey, "AES");
            cipher.init(mode, secretKeySpec);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException |
                 BadPaddingException | IllegalBlockSizeException e) {
            throw new CipherException("Error performing cipher operation", e);
        }
    }

    public static byte[] getAddressHash(ECKeyPair ecKeyPair) {
        String address = ecKeyPair.getAddress();
        byte[] addressHashed = hash256(address.getBytes());
        return getFirstNBytes(addressHashed, 4);
    }

}
