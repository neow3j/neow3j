package io.neow3j.wallet;

import io.neow3j.crypto.Credentials;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class WalletUtilsTest extends BaseTest {

    @Test
    public void testGenerateWalletFile() throws Exception {
        String fileName = WalletUtils.generateWalletFile(SampleKeys.PASSWORD_1, SampleKeys.KEY_PAIR_1, getTempDir());
        testGenerateWalletFile(fileName);
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

}
