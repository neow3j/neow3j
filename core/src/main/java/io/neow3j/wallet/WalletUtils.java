package io.neow3j.wallet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.SecureRandomUtils;
import io.neow3j.crypto.exceptions.CipherException;
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

import static java.lang.String.format;

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

    public static String generateWalletFile(String password, File destinationDirectory)
            throws CipherException, IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException {

        ECKeyPair ecKeyPair = ECKeyPair.createEcKeyPair();
        return generateWalletFile(password, ecKeyPair, destinationDirectory);
    }

    public static String generateWalletFile(String password, ECKeyPair ecKeyPair, File destinationDirectory)
            throws CipherException, IOException {

        Account a = new Account(ecKeyPair);
        Wallet w = Wallet.withAccounts(a);
        w.encryptAllAccounts(password);
        return generateWalletFile(w.toNEP6Wallet(), destinationDirectory);
    }

    public static String generateWalletFile(NEP6Wallet nep6Wallet, File destinationDirectory) throws IOException {
        String fileName = getWalletFileName(nep6Wallet);
        File destination = new File(destinationDirectory, fileName);
        objectMapper.writeValue(destination, nep6Wallet);
        return fileName;
    }

    public static String getWalletFileName(NEP6Wallet nep6Wallet) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return now.format(format) + nep6Wallet.getName() + ".json";
    }

    public static String getDefaultKeyDirectory() {
        return getDefaultKeyDirectory(System.getProperty("os.name"));
    }

    static String getDefaultKeyDirectory(String osName) {
        String osNameLowerCase = osName.toLowerCase();

        if (osNameLowerCase.startsWith("mac")) {
            return format("%s%sLibrary%sneow3j", System.getProperty("user.home"), File.separator, File.separator);
        } else if (osNameLowerCase.startsWith("win")) {
            return format("%s%sneow3j", System.getenv("APPDATA"), File.separator);
        } else {
            return format("%s%s.neow3j", System.getProperty("user.home"), File.separator);
        }
    }

}
