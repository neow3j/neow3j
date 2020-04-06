package io.neow3j.wallet;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.wallet.exceptions.AccountStateException;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Wallet;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;
import org.junit.Test;

public class WalletTest {

    @Test
    public void testCreateDefaultWallet() {
        Wallet w = new Wallet.Builder().build();
        assertEquals(w.getName(), "neow3jWallet");
        assertEquals(w.getVersion(), Wallet.CURRENT_VERSION);
        assertTrue(w.getAccounts().isEmpty());
    }

    @Test
    public void testCreateWalletFromNEP6File() throws IOException {
        Wallet w = Wallet.fromNEP6Wallet("wallet.json").build();

        ObjectMapper mapper = new ObjectMapper();
        URL nep6WalletFile = WalletTest.class.getClassLoader().getResource("wallet.json");
        NEP6Wallet nep6Wallet = mapper.readValue(nep6WalletFile, NEP6Wallet.class);

        assertEquals("Wallet", w.getName());
        assertEquals(Wallet.CURRENT_VERSION, w.getVersion());
        assertEquals(2, w.getAccounts().size());
        assertEquals(NEP2.DEFAULT_SCRYPT_PARAMS, w.getScryptParams());

        Account a = w.getAccount(ScriptHash.fromAddress("AHCkToUT1eFMdf2fnXpRXygk8nhyhrRdZN"));
        assertEquals("AHCkToUT1eFMdf2fnXpRXygk8nhyhrRdZN", a.getAddress());
        assertEquals("Account1", a.getLabel());
        assertTrue(a.isDefault());
        assertFalse(a.isLocked());
        assertEquals("6PYVmzptUSqkpw1YRPrNwuhCVGF5BvUNWCRB9XwrQuJJmcE4soABybYWxq",
                a.getEncryptedPrivateKey());
        assertEquals(
                "DCEDGY4G2y1nT0NiREFiPxAlSv38eRgqmNCp2HocRQVX7OgLQQqQatQ=",
                nep6Wallet.getAccounts().get(0).getContract().getScript()
        );

        a = w.getAccount(ScriptHash.fromAddress("AaSsb7k1mFPKqhJynyr4qQybtQrRBub21Q"));
        assertEquals("AaSsb7k1mFPKqhJynyr4qQybtQrRBub21Q", a.getAddress());
        assertEquals("Account2", a.getLabel());
        assertFalse(a.isDefault());
        assertFalse(a.isLocked());
        assertEquals("6PYSMtdYvx6vXK21AAc2NBvbYuBusCxre59uy1EhnbRysSmhgMkTk37Qez",
                a.getEncryptedPrivateKey());
        assertEquals(
                "DCEDmd53lLYbIBx/SYdaYgQ13PvUFcEsgudizzN2B2kpAEkLQQqQatQ=",
                nep6Wallet.getAccounts().get(1).getContract().getScript()
        );
    }

    @Test
    public void testAddAccount() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = new Wallet.Builder().build();
        Account acct = Account.fromECKeyPair(ECKeyPair.createEcKeyPair()).build();
        w.addAccount(acct);
        assertTrue(!w.getAccounts().isEmpty());
        assertEquals(w.getAccounts().get(0), acct);
    }

    @Test
    public void testAddDuplicateAccount() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = new Wallet.Builder().build();
        Account acct = Account.fromECKeyPair(ECKeyPair.createEcKeyPair()).build();
        assertTrue(w.addAccount(acct));
        assertFalse(w.addAccount(acct));
    }

    @Test
    public void testRemoveAccounts() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        final String address = "AUcY65mkxygUB5bXZqYhNKsrq1khuncqr3";
        Wallet w = new Wallet.Builder().build();
        assertFalse(w.removeAccount(ScriptHash.fromAddress(address)));
        Account acct1 = Account.fromECKeyPair(ECKeyPair.createEcKeyPair()).build();
        w.addAccount(acct1);
        Account acct2 = Account.fromECKeyPair(ECKeyPair.createEcKeyPair()).build();
        w.addAccount(acct2);
        assertTrue(w.removeAccount(acct1.getScriptHash()));
        assertTrue(w.removeAccount(acct2.getScriptHash()));
    }

    @Test
    public void testDefaultWalletToNEP6Wallet() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, CipherException {

        String walletName = "TestWallet";
        Wallet w = new Wallet.Builder().name(walletName).build();
        Account a = Account.fromECKeyPair(ECKeyPair.createEcKeyPair()).build();
        w.addAccount(a);
        w.encryptAllAccounts("12345678");

        NEP6Account nep6acct = new NEP6Account(a.getAddress(), a.getLabel(), false, false,
                a.getEncryptedPrivateKey(), null, null);
        NEP6Wallet nep6w = new NEP6Wallet(walletName, Wallet.CURRENT_VERSION,
                NEP2.DEFAULT_SCRYPT_PARAMS, Collections.singletonList(nep6acct), null);

        assertEquals(nep6w, w.toNEP6Wallet());
    }

    @Test(expected = AccountStateException.class)
    public void testToNEP6WalletWithUnencryptedPrivateKey()
            throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = new Wallet.Builder().build();
        Account a = Account.fromECKeyPair(ECKeyPair.createEcKeyPair()).build();
        w.addAccount(a);
        w.toNEP6Wallet();
    }

    @Test
    public void testFromNEP6WalletToNEP6Wallet() throws IOException, URISyntaxException {
        URL nep6WalletFile = WalletTest.class.getClassLoader().getResource("wallet.json");
        Wallet w = Wallet.fromNEP6Wallet(nep6WalletFile.toURI()).build();

        ObjectMapper mapper = new ObjectMapper();
        NEP6Wallet nep6Wallet = mapper.readValue(nep6WalletFile, NEP6Wallet.class);

        assertEquals(nep6Wallet, w.toNEP6Wallet());
    }

    @Test
    public void testFromNEP6WalletFileToNEP6Wallet() throws IOException, URISyntaxException {
        // TODO: Update the wallet file. It's not up to date with Neo 3.
        URL nep6WalletFileUrl = WalletTest.class.getClassLoader().getResource("wallet.json");
        File nep6WalletFile = new File(nep6WalletFileUrl.toURI());
        Wallet w = Wallet.fromNEP6Wallet(nep6WalletFile).build();

        ObjectMapper mapper = new ObjectMapper();
        NEP6Wallet nep6Wallet = mapper.readValue(nep6WalletFile, NEP6Wallet.class);

        assertEquals(nep6Wallet, w.toNEP6Wallet());
    }

    @Test
    public void testCreateGenericWallet() {
        Wallet w = Wallet.createWallet();
        assertThat(w.getName(), is("neow3jWallet"));
        assertThat(w.getVersion(), is(Wallet.CURRENT_VERSION));
        assertThat(w.getScryptParams(), is(NEP2.DEFAULT_SCRYPT_PARAMS));
        assertThat(w.getAccounts().size(), is(1));
        assertThat(w.getAccounts(), not(empty()));
        assertThat(w.getAccounts().get(0).getECKeyPair(), notNullValue());
    }

    @Test
    public void testCreateGenericWalletAndSaveToFile()
            throws CipherException, IOException, NEP2InvalidFormat, NEP2InvalidPassphrase {
        File tempFile = createTempFile();

        Wallet w1 = Wallet.createWallet();
        w1.encryptAllAccounts("12345678");
        w1.saveNEP6Wallet(tempFile);

        assertThat(w1.getName(), is("neow3jWallet"));
        assertThat(w1.getVersion(), is(Wallet.CURRENT_VERSION));
        assertThat(w1.getScryptParams(), is(NEP2.DEFAULT_SCRYPT_PARAMS));
        assertThat(w1.getAccounts().size(), is(1));
        assertThat(w1.getAccounts(), not(empty()));
        assertThat(tempFile.exists(), is(true));
        assertThat(w1.getAccounts().get(0).getPrivateKey(), is(nullValue()));

        Wallet w2 = Wallet.fromNEP6Wallet(tempFile.toURI()).build();
        w2.decryptAllAccounts("12345678");

        assertThat(w2.toNEP6Wallet(), is(w1.toNEP6Wallet()));
    }

    @Test
    public void testCreateGenericWalletAndSaveToFileWithPasswordAndDestination()
            throws CipherException, IOException, NEP2InvalidFormat, NEP2InvalidPassphrase {
        File tempFile = createTempFile();

        Wallet w1 = Wallet.createWallet("12345678", tempFile);

        assertThat(w1.getName(), is("neow3jWallet"));
        assertThat(w1.getVersion(), is(Wallet.CURRENT_VERSION));
        assertThat(w1.getScryptParams(), is(NEP2.DEFAULT_SCRYPT_PARAMS));
        assertThat(w1.getAccounts().size(), is(1));
        assertThat(w1.getAccounts(), not(empty()));
        assertThat(tempFile.exists(), is(true));
        assertThat(w1.getAccounts().get(0).getPrivateKey(), is(nullValue()));

        Wallet w2 = Wallet.fromNEP6Wallet(tempFile.toURI()).build();
        w2.decryptAllAccounts("12345678");

        assertThat(w1.getName(), is(w2.getName()));
        assertThat(w1.getVersion(), is(w2.getVersion()));
        assertThat(w1.getScryptParams(), is(w2.getScryptParams()));
        assertThat(w1.getAccounts().size(), is(w2.getAccounts().size()));
        assertThat(w1.getAccounts().get(0).getPublicKey(),
                is(w2.getAccounts().get(0).getPublicKey()));
        assertThat(tempFile.exists(), is(true));

        assertThat(w2.toNEP6Wallet(), is(w1.toNEP6Wallet()));
    }

    private File createTempFile() throws IOException {
        File testFile = File.createTempFile("neow3j", "-test");
        testFile.deleteOnExit();
        return testFile;
    }

    @Test
    public void testCreateGenericWalletWithPassword()
            throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {

        Wallet w1 = Wallet.createWallet("12345678");

        assertThat(w1.getName(), is("neow3jWallet"));
        assertThat(w1.getVersion(), is(Wallet.CURRENT_VERSION));
        assertThat(w1.getScryptParams(), is(NEP2.DEFAULT_SCRYPT_PARAMS));
        assertThat(w1.getAccounts().size(), is(1));
        assertThat(w1.getAccounts(), not(empty()));
        assertThat(w1.getAccounts().get(0).getEncryptedPrivateKey(), notNullValue());
        assertThat(w1.getAccounts().get(0).getPrivateKey(), is(nullValue()));

        w1.decryptAllAccounts("12345678");
        assertThat(w1.getAccounts().get(0).getECKeyPair(), notNullValue());
        assertThat(w1.getAccounts().get(0).getPrivateKey(), notNullValue());
        assertThat(w1.getAccounts().get(0).getEncryptedPrivateKey(), notNullValue());
    }

    @Test
    public void testGetAndSetDefaultAccount() {
        Wallet w = Wallet.createWallet();
        assertThat(w.getDefaultAccount(), notNullValue());

        Account a = Account.createAccount();
        w.addAccount(a);
        w.setDefaultAccount(a.getScriptHash());
        assertThat(w.getDefaultAccount(), notNullValue());
        assertThat(w.getDefaultAccount(), is(a));
    }

    @Test
    public void encryptWallet() throws CipherException {
        Wallet w = Wallet.createWallet();
        w.addAccount(Account.createAccount());
        assertThat(w.getAccounts().get(0).getPrivateKey().getInt(), notNullValue());
        assertThat(w.getAccounts().get(1).getPrivateKey().getInt(), notNullValue());
        w.encryptAllAccounts("pw");
        assertThat(w.getAccounts().get(0).getPrivateKey(), nullValue());
        assertThat(w.getAccounts().get(1).getPrivateKey(), nullValue());
    }

}
