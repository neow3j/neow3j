package io.neow3j.wallet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.crypto.Credentials;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.KeyUtils;
import io.neow3j.crypto.Keys;
import io.neow3j.crypto.MnemonicUtils;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.SecureRandomUtils;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2AccountNotFound;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Wallet;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static io.neow3j.crypto.Hash.sha256;
import static io.neow3j.crypto.KeyUtils.ADDRESS_SIZE;
import static io.neow3j.crypto.KeyUtils.PRIVATE_KEY_LENGTH_IN_HEX;

/**
 * Utility functions for working with Wallet files.
 */
public class WalletUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SecureRandom secureRandom = SecureRandomUtils.secureRandom();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String generateNewWalletFile(
            String password, File destinationDirectory)
            throws CipherException, IOException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        return generateWalletFile(password, ecKeyPair, destinationDirectory);
    }

    public static String generateWalletFile(
            String password, ECKeyPair ecKeyPair, File destinationDirectory)
            throws CipherException, IOException {

        Account a = Account.with().ecKeyPair(ecKeyPair).build();
        Wallet w = Wallet.with().defaultValues().account(a).build();
        w.encryptAllAccounts(password);
        return generateWalletFile(w.toNEP6Wallet(), destinationDirectory);
    }

    public static String generateWalletFile(NEP6Wallet nep6Wallet, File destinationDirectory)
            throws IOException {

        String fileName = getWalletFileName(nep6Wallet);
        File destination = new File(destinationDirectory, fileName);

        objectMapper.writeValue(destination, nep6Wallet);

        return fileName;
    }

    /**
     * Generates a BIP-39 compatible NEO wallet. The private key for the wallet can
     * be calculated using following algorithm:
     * <pre>
     *     Key = SHA-256(BIP_39_SEED(mnemonic, password))
     * </pre>
     *
     * @param password             Will be used for both wallet encryption and passphrase for BIP-39 seed
     * @param destinationDirectory The directory containing the wallet
     * @return A BIP-39 compatible NEO wallet
     * @throws CipherException if the underlying cipher is not available
     * @throws IOException     if the destination cannot be written to
     */
    public static Bip39Wallet generateBip39Wallet(String password, File destinationDirectory)
            throws CipherException, IOException {

        byte[] initialEntropy = new byte[16];
        secureRandom.nextBytes(initialEntropy);

        String mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, password);
        ECKeyPair privateKey = ECKeyPair.create(sha256(seed));

        String walletFile = generateWalletFile(password, privateKey, destinationDirectory);

        return new Bip39Wallet(walletFile, mnemonic);
    }

    public static NEP6Wallet loadWalletFile(String source) throws IOException {
        return loadWalletFile(new File(source));
    }

    public static NEP6Wallet loadWalletFile(File source) throws IOException {
        return objectMapper.readValue(source, NEP6Wallet.class);
    }

    public static Credentials loadCredentials(String accountAddress, String password, String source)
            throws IOException, CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase, NEP2AccountNotFound {
        return loadCredentials(accountAddress, password, new File(source));
    }

    public static Credentials loadCredentials(String accountAddress, String password, File source)
            throws IOException, CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase, NEP2AccountNotFound {

        NEP6Wallet nep6Wallet = objectMapper.readValue(source, NEP6Wallet.class);

        NEP6Account account = nep6Wallet.getAccounts().stream()
                .filter((a) -> a.getAddress() != null)
                .filter((a) -> a.getAddress().equals(accountAddress))
                .findFirst()
                .orElseThrow(() -> new NEP2AccountNotFound("Account not found in the specified wallet."));

        return loadCredentials(account, password, nep6Wallet);
    }

    public static Credentials loadCredentials(NEP6Account account, String password, NEP6Wallet nep6Wallet)
            throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {
        ScryptParams scryptParams = nep6Wallet.getScrypt();
        ECKeyPair decrypted = NEP2.decrypt(password, account.getKey(), scryptParams);
        return new Credentials(decrypted);
    }

    public static Credentials loadBip39Credentials(String password, String mnemonic) {
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, password);
        return new Credentials(ECKeyPair.create(sha256(seed)));
    }

    private static String getWalletFileName(NEP6Wallet nep6Wallet) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(
                "'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        return now.format(format) + nep6Wallet.getName() + ".json";
    }

    public static String getDefaultKeyDirectory() {
        return getDefaultKeyDirectory(System.getProperty("os.name"));
    }

    static String getDefaultKeyDirectory(String osName1) {
        String osName = osName1.toLowerCase();

        if (osName.startsWith("mac")) {
            return String.format(
                    "%s%sLibrary%sneow3j", System.getProperty("user.home"), File.separator,
                    File.separator);
        } else if (osName.startsWith("win")) {
            return String.format("%s%sneow3j", System.getenv("APPDATA"), File.separator);
        } else {
            return String.format("%s%s.neow3j", System.getProperty("user.home"), File.separator);
        }
    }

    public static boolean isValidPrivateKey(String privateKey) {
        String cleanPrivateKey = Numeric.cleanHexPrefix(privateKey);
        return cleanPrivateKey.length() == PRIVATE_KEY_LENGTH_IN_HEX;
    }

    public static boolean isValidAddress(String address) {
        String cleanInput = Numeric.cleanHexPrefix(address);

        try {
            KeyUtils.toScriptHash(cleanInput);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return cleanInput.length() == ADDRESS_SIZE;
    }
}
