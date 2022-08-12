package io.neow3j.wallet;

import static io.neow3j.test.TestProperties.defaultAccountAddress;
import static io.neow3j.test.TestProperties.defaultAccountPassword;
import static io.neow3j.test.TestProperties.defaultAccountPrivateKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.utils.AddressUtils;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WalletUtilsTest {

    private File tempDir;

    @BeforeAll
    public void setUp() throws Exception {
        tempDir = createTempDir();
    }

    @AfterAll
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
        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(defaultAccountPrivateKey()));
        String fileName = WalletUtils.generateWalletFile(defaultAccountPassword(), pair, this.tempDir);
        Path p = Paths.get(this.tempDir.getPath(), fileName);
        Wallet loadedWallet = Wallet.fromNEP6Wallet(p.toFile());
        loadedWallet.decryptAllAccounts(defaultAccountPassword());
        assertThat(loadedWallet.getAccounts().get(0).getAddress(), is(defaultAccountAddress()));
        assertThat(loadedWallet.getAccounts().get(0).getECKeyPair(),
                is(ECKeyPair.create(Numeric.hexStringToByteArray(defaultAccountPrivateKey()))));
    }

    @Test
    public void testGenerateWalletFileFreshEcKeyPair() throws Exception {
        String pw = "password";
        String fileName = WalletUtils.generateWalletFile(pw, this.tempDir);
        Path p = Paths.get(this.tempDir.getPath(), fileName);
        Wallet loadedWallet = Wallet.fromNEP6Wallet(p.toFile());
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
