package com.axlabs.neow3j.crypto;

import com.axlabs.neow3j.utils.Numeric;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static com.axlabs.neow3j.crypto.SampleKeys.ADDRESS_1;
import static com.axlabs.neow3j.crypto.SampleKeys.ADDRESS_2;
import static com.axlabs.neow3j.crypto.SampleKeys.CREDENTIALS_1;
import static com.axlabs.neow3j.crypto.SampleKeys.CREDENTIALS_2;
import static com.axlabs.neow3j.crypto.SampleKeys.KEY_PAIR_1;
import static com.axlabs.neow3j.crypto.SampleKeys.PASSWORD_1;
import static com.axlabs.neow3j.crypto.SampleKeys.PASSWORD_2;
import static com.axlabs.neow3j.crypto.WalletUtils.isValidAddress;
import static com.axlabs.neow3j.crypto.WalletUtils.isValidPrivateKey;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class WalletUtilsTest {

    private File tempDir;

    @Before
    public void setUp() throws Exception {
        tempDir = createTempDir();
    }

    @After
    public void tearDown() {
        for (File file : tempDir.listFiles()) {
            file.delete();
        }
        tempDir.delete();
    }

    @Test
    public void testGenerateBip39Wallets() throws Exception {
        Bip39Wallet wallet = WalletUtils.generateBip39Wallet(PASSWORD_1, tempDir);
        byte[] seed = MnemonicUtils.generateSeed(wallet.getMnemonic(), PASSWORD_1);
        Credentials credentials = Credentials.create(ECKeyPair.create(Hash.sha256(seed)));

        assertEquals(credentials, WalletUtils.loadBip39Credentials(PASSWORD_1, wallet.getMnemonic()));
    }

    @Test
    public void testGenerateNewWalletFile() throws Exception {
        String fileName = WalletUtils.generateNewWalletFile(PASSWORD_1, tempDir);
        WalletFile walletFile = WalletUtils.loadWalletFile(tempDir.getAbsolutePath() + File.separatorChar + fileName);
        testGeneratedNewWalletFile(walletFile);
    }

    private void testGeneratedNewWalletFile(WalletFile walletFile) throws Exception {
        WalletUtils.loadCredentials(walletFile.getAccounts().stream().findFirst().get(), PASSWORD_1, walletFile);
    }

    @Test
    public void testGenerateWalletFile() throws Exception {
        String fileName = WalletUtils.generateWalletFile(PASSWORD_1, KEY_PAIR_1, tempDir);
        testGenerateWalletFile(fileName);
    }

    private void testGenerateWalletFile(String fileName) throws Exception {
        Credentials credentials = WalletUtils.loadCredentials(ADDRESS_1,
                PASSWORD_1, new File(tempDir, fileName));

        assertThat(credentials, equalTo(CREDENTIALS_1));
    }

    @Test
    public void testLoadCredentialsFromFile() throws Exception {
        Credentials credentials = WalletUtils.loadCredentials(
                ADDRESS_2,
                PASSWORD_2,
                new File(WalletUtilsTest.class.getResource(
                        "/keyfiles/"
                                + "neon-wallet1.json")
                        .getFile()));

        assertThat(credentials, equalTo(CREDENTIALS_2));
    }

    @Test
    public void testLoadCredentialsFromString() throws Exception {
        Credentials credentials = WalletUtils.loadCredentials(
                ADDRESS_2,
                PASSWORD_2,
                WalletUtilsTest.class.getResource(
                        "/keyfiles/"
                                + "UTC--2016-11-03T07-47-45."
                                + "988Z--4f9c1a1efaa7d81ba1cabf07f2c3a5ac5cf4f818").getFile());

        assertThat(credentials, equalTo(CREDENTIALS_2));
    }

    @Test
    public void testGetDefaultKeyDirectory() {
        assertTrue(WalletUtils.getDefaultKeyDirectory("Mac OS X")
                .endsWith(String.format("%sLibrary%sneo", File.separator, File.separator)));
        assertTrue(WalletUtils.getDefaultKeyDirectory("Windows")
                .endsWith(String.format("%sneo", File.separator)));
        assertTrue(WalletUtils.getDefaultKeyDirectory("Linux")
                .endsWith(String.format("%s.neo", File.separator)));
    }

    private static File createTempDir() throws Exception {
        return Files.createTempDirectory(
                WalletUtilsTest.class.getSimpleName() + "-testkeys").toFile();
    }

    @Test
    public void testIsValidPrivateKey() {
        assertTrue(isValidPrivateKey(SampleKeys.PRIVATE_KEY_STRING_1));
        assertTrue(isValidPrivateKey(Numeric.prependHexPrefix(SampleKeys.PRIVATE_KEY_STRING_1)));

        assertFalse(isValidPrivateKey(""));
        assertFalse(isValidPrivateKey(SampleKeys.PRIVATE_KEY_STRING_1 + "a"));
        assertFalse(isValidPrivateKey(SampleKeys.PRIVATE_KEY_STRING_1.substring(1)));
    }

    @Test
    public void testIsValidAddress() {
        assertTrue(isValidAddress(ADDRESS_1));

        assertFalse(isValidAddress(""));
        assertFalse(isValidAddress(ADDRESS_1 + 'a'));
        assertFalse(isValidAddress(ADDRESS_1.substring(1)));
    }
}
