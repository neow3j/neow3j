package io.neow3j.wallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    protected static File createTempDir() throws Exception {
        return Files.createTempDirectory(
                WalletUtilsTest.class.getSimpleName() + "-testkeys").toFile();
    }

    @Test
    public void testGenerateWalletFile() throws Exception {
        String fileName = WalletUtils.generateWalletFile(SampleKeys.PASSWORD_1, SampleKeys.KEY_PAIR_1, this.tempDir);
        Wallet loadedWallet = Wallet.fromNEP6Wallet(fileName).build();
        loadedWallet.decryptAllAccounts(SampleKeys.PASSWORD_1);
        assertEquals(loadedWallet.getAccounts().get(0).getAddress(), SampleKeys.ADDRESS_1);
        assertEquals(loadedWallet.getAccounts().get(0).getECKeyPair(), SampleKeys.KEY_PAIR_1);
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

}
