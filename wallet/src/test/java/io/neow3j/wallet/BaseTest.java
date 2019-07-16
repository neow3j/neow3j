package io.neow3j.wallet;

import io.neow3j.crypto.Credentials;
import io.neow3j.wallet.nep6.NEP6Wallet;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.nio.file.Files;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class BaseTest {

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

    public File getTempDir() {
        return tempDir;
    }

    protected void testGeneratedNewWalletFile(NEP6Wallet nep6Wallet) throws Exception {
        WalletUtils.loadCredentials(nep6Wallet.getAccounts().stream().findFirst().get(), SampleKeys.PASSWORD_1, nep6Wallet);
    }

    protected void testGenerateWalletFile(String fileName) throws Exception {
        Credentials credentials = WalletUtils.loadCredentials(SampleKeys.ADDRESS_1,
                SampleKeys.PASSWORD_1, new File(tempDir, fileName));

        assertThat(credentials, equalTo(SampleKeys.CREDENTIALS_1));
    }

    protected static File createTempDir() throws Exception {
        return Files.createTempDirectory(
                WalletUtilsTest.class.getSimpleName() + "-testkeys").toFile();
    }

}
