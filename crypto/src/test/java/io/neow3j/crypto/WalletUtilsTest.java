package io.neow3j.crypto;

import io.neow3j.utils.Numeric;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

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
        Bip39Wallet wallet = WalletUtils.generateBip39Wallet(SampleKeys.PASSWORD_1, tempDir);
        byte[] seed = MnemonicUtils.generateSeed(wallet.getMnemonic(), SampleKeys.PASSWORD_1);
        Credentials credentials = Credentials.create(ECKeyPair.create(Hash.sha256(seed)));

        assertEquals(credentials, WalletUtils.loadBip39Credentials(SampleKeys.PASSWORD_1, wallet.getMnemonic()));
    }

    @Test
    public void testGenerateNewWalletFile() throws Exception {
        String fileName = WalletUtils.generateNewWalletFile(SampleKeys.PASSWORD_1, tempDir);
        WalletFile walletFile = WalletUtils.loadWalletFile(tempDir.getAbsolutePath() + File.separatorChar + fileName);
        testGeneratedNewWalletFile(walletFile);
    }

    private void testGeneratedNewWalletFile(WalletFile walletFile) throws Exception {
        WalletUtils.loadCredentials(walletFile.getAccounts().stream().findFirst().get(), SampleKeys.PASSWORD_1, walletFile);
    }

    @Test
    public void testGenerateWalletFile() throws Exception {
        String fileName = WalletUtils.generateWalletFile(SampleKeys.PASSWORD_1, SampleKeys.KEY_PAIR_1, tempDir);
        testGenerateWalletFile(fileName);
    }

    private void testGenerateWalletFile(String fileName) throws Exception {
        Credentials credentials = WalletUtils.loadCredentials(SampleKeys.ADDRESS_1,
                SampleKeys.PASSWORD_1, new File(tempDir, fileName));

        assertThat(credentials, equalTo(SampleKeys.CREDENTIALS_1));
    }

    @Test
    public void testLoadCredentialsFromFile() throws Exception {
        Credentials credentials = WalletUtils.loadCredentials(
                SampleKeys.ADDRESS_2,
                SampleKeys.PASSWORD_2,
                new File(WalletUtilsTest.class.getResource(
                        "/keyfiles/"
                                + "neon-wallet1.json")
                        .getFile()));

        assertThat(credentials, equalTo(SampleKeys.CREDENTIALS_2));
    }

    @Test
    public void testLoadCredentialsFromString() throws Exception {
        Credentials credentials = WalletUtils.loadCredentials(
                SampleKeys.ADDRESS_2,
                SampleKeys.PASSWORD_2,
                WalletUtilsTest.class.getResource(
                        "/keyfiles/"
                                + "UTC--2016-11-03T07-47-45."
                                + "988Z--4f9c1a1efaa7d81ba1cabf07f2c3a5ac5cf4f818").getFile());

        assertThat(credentials, equalTo(SampleKeys.CREDENTIALS_2));
    }

    @Test
    public void testGetDefaultKeyDirectory() {
        assertTrue(WalletUtils.getDefaultKeyDirectory("Mac OS X")
                .endsWith(String.format("%sLibrary%sneow3j", File.separator, File.separator)));
        assertTrue(WalletUtils.getDefaultKeyDirectory("Windows")
                .endsWith(String.format("%sneow3j", File.separator)));
        assertTrue(WalletUtils.getDefaultKeyDirectory("Linux")
                .endsWith(String.format("%s.neow3j", File.separator)));
    }

    private static File createTempDir() throws Exception {
        return Files.createTempDirectory(
                WalletUtilsTest.class.getSimpleName() + "-testkeys").toFile();
    }

    @Test
    public void testIsValidPrivateKey() {
        assertTrue(WalletUtils.isValidPrivateKey(SampleKeys.PRIVATE_KEY_STRING_1));
        assertTrue(WalletUtils.isValidPrivateKey(Numeric.prependHexPrefix(SampleKeys.PRIVATE_KEY_STRING_1)));

        assertFalse(WalletUtils.isValidPrivateKey(""));
        assertFalse(WalletUtils.isValidPrivateKey(SampleKeys.PRIVATE_KEY_STRING_1 + "a"));
        assertFalse(WalletUtils.isValidPrivateKey(SampleKeys.PRIVATE_KEY_STRING_1.substring(1)));
    }

    @Test
    public void testIsValidAddress() {
        assertTrue(WalletUtils.isValidAddress(SampleKeys.ADDRESS_1));

        assertFalse(WalletUtils.isValidAddress(""));
        assertFalse(WalletUtils.isValidAddress(SampleKeys.ADDRESS_1 + 'a'));
        assertFalse(WalletUtils.isValidAddress(SampleKeys.ADDRESS_1.substring(1)));
    }
}
