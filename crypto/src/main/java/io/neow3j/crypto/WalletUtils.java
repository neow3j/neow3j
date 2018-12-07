package io.neow3j.crypto;

import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2AccountNotFound;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.utils.Numeric;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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

        WalletFile standardWallet = Wallet.createStandardWallet();
        WalletFile.Account standardAccount = Wallet.createStandardAccount(password, ecKeyPair);
        standardWallet.addAccount(standardAccount);

        String fileName = getWalletFileName(standardWallet);
        File destination = new File(destinationDirectory, fileName);

        objectMapper.writeValue(destination, standardWallet);

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

    public static WalletFile loadWalletFile(String source) throws IOException {
        return objectMapper.readValue(new File(source), WalletFile.class);
    }

    public static WalletFile loadWalletFile(File source) throws IOException {
        return objectMapper.readValue(source, WalletFile.class);
    }

    public static Credentials loadCredentials(String accountAddress, String password, String source)
            throws IOException, CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase, NEP2AccountNotFound {
        return loadCredentials(accountAddress, password, new File(source));
    }

    public static Credentials loadCredentials(String accountAddress, String password, File source)
            throws IOException, CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase, NEP2AccountNotFound {

        WalletFile walletFile = objectMapper.readValue(source, WalletFile.class);

        WalletFile.Account account = walletFile.getAccounts().stream()
                .filter((a) -> a.getAddress() != null)
                .filter((a) -> a.getAddress().equals(accountAddress))
                .findFirst()
                .orElseThrow(() -> new NEP2AccountNotFound("Account not found in the specified wallet."));

        return loadCredentials(account, password, walletFile);
    }

    public static Credentials loadCredentials(WalletFile.Account account, String password, WalletFile walletFile)
            throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {
        WalletFile.ScryptParams scryptParams = walletFile.getScrypt();
        ECKeyPair decrypted = Wallet.decrypt(password, walletFile, account, scryptParams.getN(), scryptParams.getP(), scryptParams.getR());
        return Credentials.create(decrypted);
    }

    public static Credentials loadBip39Credentials(String password, String mnemonic) {
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, password);
        return Credentials.create(ECKeyPair.create(sha256(seed)));
    }

    private static String getWalletFileName(WalletFile walletFile) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(
                "'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        return now.format(format) + walletFile.getName() + ".json";
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
