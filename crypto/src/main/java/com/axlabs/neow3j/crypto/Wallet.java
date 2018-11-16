package com.axlabs.neow3j.crypto;

import com.axlabs.neow3j.crypto.exceptions.CipherException;
import com.axlabs.neow3j.crypto.exceptions.NEP2InvalidFormat;
import com.axlabs.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import com.axlabs.neow3j.utils.ArrayUtils;
import com.axlabs.neow3j.utils.Numeric;
import org.bouncycastle.crypto.generators.SCrypt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.axlabs.neow3j.crypto.Hash.sha256;
import static com.axlabs.neow3j.crypto.Keys.PRIVATE_KEY_SIZE;
import static com.axlabs.neow3j.crypto.SecureRandomUtils.secureRandom;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * <p>NEO wallet file management. For reference, refer to
 * <a href="https://github.com/neo-project/proposals/blob/master/nep-6.mediawiki">
 * Wallet Standards (NEP-6)</a> or the
 * <a href="https://github.com/neo-project/proposals/blob/master/nep-2.mediawiki">
 * Passphrase-protected Private Key Standard (NEP-2)</a>.</p>
 */
public class Wallet {

    private static final String DEFAULT_WALLET_NAME = "neow3jWallet";
    private static final String DEFAULT_ACCOUNT_NAME = "neow3jAccount";
    private static final String CURRENT_VERSION = "1.0";

    private static final int N_STANDARD = 1 << 14;
    private static final int P_STANDARD = 8;

    private static final int R = 8;
    private static final int DKLEN = 64;

    private static final int NEP2_PRIVATE_KEY_LENGTH = 39;
    private static final byte NEP2_PREFIX_1 = (byte) 0x01;
    private static final byte NEP2_PREFIX_2 = (byte) 0x42;
    private static final byte NEP2_FLAGBYTE = (byte) 0xE0;

    public static WalletFile.Account createAccount(String accountName,
                                                   String password,
                                                   ECKeyPair ecKeyPair,
                                                   int n,
                                                   int p,
                                                   int r)
            throws CipherException {

        byte[] encryptedPrivKey = encrypt(password, ecKeyPair, n, p, r);
        String encodedPrivateKey = Hash.base58CheckEncode(encryptedPrivKey);

        return new WalletFile.Account(
                Credentials.create(ecKeyPair).getAddress(),
                accountName,
                true,
                false,
                encodedPrivateKey,
                null,
                null
        );
    }

    public static WalletFile.Account createStandardAccount(String password, ECKeyPair ecKeyPair)
            throws CipherException {
        return createAccount(DEFAULT_ACCOUNT_NAME, password, ecKeyPair, N_STANDARD, P_STANDARD, R);
    }

    public static WalletFile createWallet(String name, int n, int p, int r) {
        return new WalletFile(
                name,
                CURRENT_VERSION,
                new WalletFile.ScryptParams(n, r, p),
                Arrays.asList(),
                null
        );
    }

    public static WalletFile createStandardWallet() {
        return new WalletFile(
                DEFAULT_WALLET_NAME,
                CURRENT_VERSION,
                new WalletFile.ScryptParams(N_STANDARD, R, P_STANDARD),
                new ArrayList<>(),
                null
        );
    }

    public static byte[] encryptStandard(String password, ECKeyPair ecKeyPair) throws CipherException {
        return encrypt(password, ecKeyPair, N_STANDARD, P_STANDARD, R);
    }

    /**
     * Encrypts the private key following the NEP-2 standard.
     *
     * @param password
     * @param ecKeyPair
     * @param n
     * @param p
     * @param r
     * @return encrypted private key as described on NEP-2
     * @throws CipherException
     */
    public static byte[] encrypt(String password, ECKeyPair ecKeyPair, int n, int p, int r)
            throws CipherException {

        byte[] addressHash = getAddressHash(ecKeyPair);

        byte[] derivedKey = generateDerivedScryptKey(
                password.getBytes(UTF_8), addressHash, n, r, p, DKLEN);

        byte[] derivedHalf1 = ArrayUtils.getFirstNBytes(derivedKey, 32);
        byte[] derivedHalf2 = ArrayUtils.getLastNBytes(derivedKey, 32);

        byte[] encryptedHalf1 = performCipherOperation(
                Cipher.ENCRYPT_MODE,
                xorPrivateKeyAndDerivedHalf(ecKeyPair, derivedHalf1, 0, 16),
                derivedHalf2);

        byte[] encryptedHalf2 = performCipherOperation(
                Cipher.ENCRYPT_MODE,
                xorPrivateKeyAndDerivedHalf(ecKeyPair, derivedHalf1, 16, 32),
                derivedHalf2);

        byte[] encryptedPrivKey = new byte[3];
        // prefix
        encryptedPrivKey[0] = NEP2_PREFIX_1;
        encryptedPrivKey[1] = NEP2_PREFIX_2;
        // flagbyte, which is always the same
        encryptedPrivKey[2] = NEP2_FLAGBYTE;

        return ArrayUtils.concatenate(encryptedPrivKey, addressHash, encryptedHalf1, encryptedHalf2);
    }

    private static byte[] xorPrivateKeyAndDerivedHalf(ECKeyPair ecKeyPair, byte[] derivedHalf, int from, int to) {
        return ArrayUtils.xor(
                Arrays.copyOfRange(privateKeyToBytes(ecKeyPair), from, to),
                Arrays.copyOfRange(derivedHalf, from, to)
        );
    }

    private static byte[] privateKeyToBytes(ECKeyPair ecKeyPair) {
        return Numeric.toBytesPadded(ecKeyPair.getPrivateKey(), PRIVATE_KEY_SIZE);
    }

    private static byte[] generateDerivedScryptKey(
            byte[] password, byte[] salt, int n, int r, int p, int dkLen) {
        return SCrypt.generate(password, salt, n, r, p, dkLen);
    }

    private static byte[] performCipherOperation(
            int mode, byte[] data, byte[] encryptKey) throws CipherException {

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

            SecretKeySpec secretKeySpec = new SecretKeySpec(encryptKey, "AES");
            cipher.init(mode, secretKeySpec);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException e) {
            throw new CipherException("Error performing cipher operation", e);
        }
    }

    private static byte[] getAddressHash(ECKeyPair ecKeyPair) {
        Credentials credential = Credentials.create(ecKeyPair);
        String address = credential.getAddress();
        byte[] addressHashed = sha256(sha256(address.getBytes()));
        return ArrayUtils.getFirstNBytes(addressHashed, 4);
    }

    public static ECKeyPair decryptStandard(String password, WalletFile walletFile, WalletFile.Account account)
            throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {
        return decrypt(password, walletFile, account, N_STANDARD, P_STANDARD, R);
    }

    public static ECKeyPair decrypt(String password, WalletFile walletFile, WalletFile.Account account, int n, int p, int r)
            throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {

        validate(walletFile, n, p, r);

        WalletFile.ScryptParams scryptParams = walletFile.getScrypt();

        int nWallet = scryptParams.getN();
        int pWallet = scryptParams.getP();
        int rWallet = scryptParams.getR();

        String nep2String = account.getKey();
        byte[] nep2Data = Hash.base58CheckDecode(nep2String);

        if (nep2Data.length != NEP2_PRIVATE_KEY_LENGTH || nep2Data[0] != NEP2_PREFIX_1 || nep2Data[1] != NEP2_PREFIX_2 || nep2Data[2] != NEP2_FLAGBYTE) {
            throw new NEP2InvalidFormat("Not valid NEP2 prefix.");
        }

        byte[] addressHash = new byte[4];
        // copy 4 bytes related to the address hash
        System.arraycopy(nep2Data, 3, addressHash, 0, 4);

        byte[] derivedKey = generateDerivedScryptKey(
                password.getBytes(UTF_8), addressHash, nWallet, rWallet, pWallet, DKLEN);

        byte[] derivedKeyHalf1 = ArrayUtils.getFirstNBytes(derivedKey, 32);
        byte[] derivedKeyHalf2 = ArrayUtils.getLastNBytes(derivedKey, 32);

        byte[] encrypted = new byte[32];
        System.arraycopy(nep2Data, 7, encrypted, 0, 32);

        byte[] decrypted = performCipherOperation(Cipher.DECRYPT_MODE, encrypted, derivedKeyHalf2);

        byte[] plainPrivateKey = ArrayUtils.xor(decrypted, derivedKeyHalf1);

        Credentials credentials = Credentials.create(Numeric.toHexStringNoPrefix(plainPrivateKey));
        byte[] calculatedAddressHash = getAddressHash(credentials.getEcKeyPair());

        if (!Arrays.equals(calculatedAddressHash, addressHash)) {
            throw new NEP2InvalidPassphrase("Calculated address hash does not match the one in the provided encrypted address.");
        }

        return credentials.getEcKeyPair();
    }

    static void validate(WalletFile walletFile, int n, int p, int r) throws NEP2InvalidFormat {
        WalletFile.ScryptParams scryptParams = walletFile.getScrypt();
        validateVersion(walletFile);
        validateScryptParams(scryptParams, n, p, r);
    }

    static void validateVersion(WalletFile walletFile) throws NEP2InvalidFormat {
        if (walletFile.getVersion() != null && !walletFile.getVersion().equals(CURRENT_VERSION)) {
            throw new NEP2InvalidFormat("Wallet version is not supported");
        }
    }

    static void validateScryptParams(WalletFile.ScryptParams scryptParams, int n, int p, int r) throws NEP2InvalidFormat {
        if (scryptParams.getN() != n || scryptParams.getP() != p || scryptParams.getR() != r) {
            throw new NEP2InvalidFormat("Wallet scrypt params are incompatible with the provided values.");
        }
    }

    static byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        secureRandom().nextBytes(bytes);
        return bytes;
    }
}
