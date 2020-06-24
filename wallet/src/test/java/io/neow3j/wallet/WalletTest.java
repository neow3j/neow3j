package io.neow3j.wallet;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.exceptions.AccountStateException;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Wallet;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;

public class WalletTest {

    @Test
    public void testCreateDefaultWallet() {
        Wallet w = Wallet.createWallet();
        assertEquals(w.getName(), "neow3jWallet");
        assertEquals(w.getVersion(), Wallet.CURRENT_VERSION);
        assertFalse(w.getAccounts().isEmpty());
    }

    @Test
    public void testCreateWalletFromNEP6File() throws IOException {
        Wallet w = Wallet.fromNEP6Wallet("wallet.json");

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
    public void addAccount() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = Wallet.createWallet();
        Account acct = new Account(ECKeyPair.createEcKeyPair());
        w.addAccount(acct);
        assertEquals(2, w.getAccounts().size());
        assertEquals(w.getAccount(acct.getScriptHash()), acct);
    }

    @Test
    public void testAddDuplicateAccount() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = Wallet.createWallet();
        Account acct = new Account(ECKeyPair.createEcKeyPair());

        // Add account to wallet.
        w.addAccount(acct);
        assertTrue(w.getAccounts().contains(acct));
        // Adding an account twice does not change the wallet.
        assertThat(w.addAccount(acct), is(w));
    }

    @Test
    public void testRemoveAccounts() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        final String address = "AUcY65mkxygUB5bXZqYhNKsrq1khuncqr3";
        Wallet w = Wallet.createWallet();
        assertFalse(w.removeAccount(ScriptHash.fromAddress(address)));
        Account acct1 = new Account(ECKeyPair.createEcKeyPair());
        w.addAccount(acct1);
        Account acct2 = new Account(ECKeyPair.createEcKeyPair());
        w.addAccount(acct2);
        assertTrue(w.removeAccount(acct1.getScriptHash()));
        assertTrue(w.removeAccount(acct2.getScriptHash()));
    }

    @Test
    public void testDefaultWalletToNEP6Wallet() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, CipherException {

        String walletName = "TestWallet";
        Account a = new Account(ECKeyPair.createEcKeyPair());
        Wallet w = new Wallet()
                .addAccounts(a)
                .setName(walletName);
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

        Account a = new Account(ECKeyPair.createEcKeyPair());
        Wallet w = new Wallet().addAccounts(a);
        w.addAccount(a);
        w.toNEP6Wallet();
    }

    @Test
    public void fromNEP6WalletToNEP6Wallet() throws IOException, URISyntaxException {
        URL nep6WalletFile = WalletTest.class.getClassLoader().getResource("wallet.json");
        assertNotNull(nep6WalletFile);
        Wallet w = Wallet.fromNEP6Wallet(nep6WalletFile.toURI());

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
        assertThat(w1.getAccounts().get(0).getECKeyPair(), is(nullValue()));

        Wallet w2 = Wallet.fromNEP6Wallet(tempFile.toURI());
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
        assertThat(w1.getAccounts().get(0).getECKeyPair(), is(nullValue()));

        Wallet w2 = Wallet.fromNEP6Wallet(tempFile.toURI());
        w2.decryptAllAccounts("12345678");

        assertThat(w1.getName(), is(w2.getName()));
        assertThat(w1.getVersion(), is(w2.getVersion()));
        assertThat(w1.getScryptParams(), is(w2.getScryptParams()));
        assertThat(w1.getAccounts().size(), is(w2.getAccounts().size()));
        assertThat(w2.getAccounts().get(0).getECKeyPair(), is(notNullValue()));
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
        assertThat(w1.getAccounts().get(0).getECKeyPair(), is(nullValue()));

        w1.decryptAllAccounts("12345678");
        assertThat(w1.getAccounts().get(0).getECKeyPair(), notNullValue());
        assertThat(w1.getAccounts().get(0).getECKeyPair(), notNullValue());
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

    @Test(expected = IllegalArgumentException.class)
    public void failSettingDefaultAccountNotContainedInWallet() {
        Wallet w = Wallet.createWallet();
        Account a = Account.createAccount();
        w.setDefaultAccount(a.getScriptHash());
    }

    @Test
    public void encryptWallet() throws CipherException {
        Wallet w = Wallet.createWallet();
        w.addAccount(Account.createAccount());
        assertThat(w.getAccounts().get(0).getECKeyPair(), notNullValue());
        assertThat(w.getAccounts().get(1).getECKeyPair(), notNullValue());
        w.encryptAllAccounts("pw");
        assertThat(w.getAccounts().get(0).getECKeyPair(), nullValue());
        assertThat(w.getAccounts().get(1).getECKeyPair(), nullValue());
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Test
    public void getNep5Balances() throws IOException {
        WireMock.configure();
        Neow3j neow = Neow3j.build(new HttpService("http://localhost:8080"));
        Account a1 = Account.fromAddress("AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm");
        Account a2 = Account.fromAddress("Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ");
        WalletTestHelper.setUpWireMockForCall("getnep5balances",
                "getnep5balances_AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm.json",
                "AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm");
        WalletTestHelper.setUpWireMockForCall("getnep5balances",
                "getnep5balances_Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ.json",
                "Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ");
        Wallet w = new Wallet().addAccounts(a1, a2);
        Map<ScriptHash, BigInteger> balances = w.getNep5TokenBalances(neow);
        assertThat(balances.keySet(), contains(
                new ScriptHash("8c23f196d8a1bfd103a9dcb1f9ccf0c611377d3b"),
                new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789")));
        assertThat(balances.values(), containsInAnyOrder(
                new BigInteger("411285799730"),
                new BigInteger("50000000")));
    }

}
