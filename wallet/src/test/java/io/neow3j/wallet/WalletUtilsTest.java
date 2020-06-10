package io.neow3j.wallet;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.utils.AddressUtils;
import io.neow3j.utils.Numeric;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        // Used neo-core with address version 0x17 to generate test data.
        String expectedAdr = "AMuDKuFCrHNtEg4jCV17ge4eyoa3JwD9fH";
        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"));
        String pw = "password";
        String fileName = WalletUtils.generateWalletFile(pw, pair, this.tempDir);
        Path p = Paths.get(this.tempDir.getPath(), fileName);
        Wallet loadedWallet = Wallet.fromNEP6Wallet(p.toFile()).build();
        loadedWallet.decryptAllAccounts(pw);
        assertThat(loadedWallet.getAccounts().get(0).getAddress(), is(expectedAdr));
        assertThat(loadedWallet.getAccounts().get(0).getECKeyPair(), is(pair));
    }

    @Test
    public void testGenerateWalletFileFreshEcKeyPair() throws Exception {
        String pw = "password";
        String fileName = WalletUtils.generateWalletFile(pw, this.tempDir);
        Path p = Paths.get(this.tempDir.getPath(), fileName);
        Wallet loadedWallet = Wallet.fromNEP6Wallet(p.toFile()).build();
        loadedWallet.decryptAllAccounts(pw);
        assertTrue(AddressUtils.isValidAddress(loadedWallet.getAccounts().get(0).getAddress()));
        assertThat(loadedWallet.getAccounts().get(0).getECKeyPair(), notNullValue());
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

    @Test
    public void loadWallet() {

    }

}
