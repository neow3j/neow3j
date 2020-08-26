package io.neow3j.wallet;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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

    private static final ScriptHash NEO_SCRIPT_HASH = new ScriptHash(
            "de5f57d430d3dece511cf975a8d37848cb9e0525");
    private static final ScriptHash GAS_SCRIPT_HASH = new ScriptHash(
            "668e0c1f9d7b70a99dd9e06eadd4c784d641afbc");

    @Test
    public void testCreateDefaultWallet() {
        Wallet w = Wallet.createWallet();
        assertEquals(w.getName(), "neow3jWallet");
        assertEquals(w.getVersion(), Wallet.CURRENT_VERSION);
        assertFalse(w.getAccounts().isEmpty());
    }

    @Test
    public void testCreateWalletWithAccounts() {
        Account acct1 = Account.createAccount();
        Account acct2 = Account.createAccount();
        Wallet wallet = Wallet.withAccounts(acct1, acct2);

        assertEquals(acct1, wallet.getDefaultAccount());
        assertThat(wallet.getAccounts(), containsInAnyOrder(acct1, acct2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWalletWithAccounts_noAccounts() {
        Wallet.withAccounts();
    }

    @Test
    public void testHoldsAccount() {
        Account account = Account.createAccount();
        Wallet wallet = Wallet.createWallet();
        wallet.addAccounts(account);

        assertTrue(wallet.holdsAccount(account.getScriptHash()));

        wallet.removeAccount(account.getScriptHash());
        assertFalse(wallet.holdsAccount(account.getScriptHash()));
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

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWalletFromNEP6File_noDefaultAccount() throws IOException {
        Wallet.fromNEP6Wallet("wallet_noDefaultAccount.json");
    }

    @Test
    public void addAccount() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = Wallet.createWallet();
        Account acct = new Account(ECKeyPair.createEcKeyPair());
        w.addAccounts(acct);
        assertEquals(2, w.getAccounts().size());
        assertEquals(w.getAccount(acct.getScriptHash()), acct);
    }

    @Test
    public void testAddDuplicateAccount() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = Wallet.createWallet();
        Account acct = new Account(ECKeyPair.createEcKeyPair());

        // Add account to wallet.
        w.addAccounts(acct);
        assertTrue(w.getAccounts().contains(acct));
        // Adding an account twice does not change the wallet.
        assertEquals(2, w.addAccounts(acct).getAccounts().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAccountNotInWallet() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {
        Account account = new Account(ECKeyPair.createEcKeyPair());
        Wallet wallet = Wallet.createWallet();

        wallet.getAccount(account.getScriptHash());
    }

    @Test
    public void testRemoveAccounts() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        final String address = "AUcY65mkxygUB5bXZqYhNKsrq1khuncqr3";
        Wallet w = Wallet.createWallet();
        assertFalse(w.removeAccount(ScriptHash.fromAddress(address)));
        Account acct1 = new Account(ECKeyPair.createEcKeyPair());
        w.addAccounts(acct1);
        Account acct2 = new Account(ECKeyPair.createEcKeyPair());
        w.addAccounts(acct2);
        assertTrue(w.removeAccount(acct1.getScriptHash()));
        assertTrue(w.removeAccount(acct2.getScriptHash()));
    }

    @Test
    public void testRemoveAccounts_defaultAccount() {
        Account acct1 = Account.createAccount();
        Account acct2 = Account.createAccount();
        Wallet wallet = Wallet.withAccounts(acct1, acct2);

        assertEquals(2, wallet.getAccounts().size());
        assertEquals(acct1, wallet.getDefaultAccount());

        wallet.removeAccount(acct1.getScriptHash());

        assertEquals(1, wallet.getAccounts().size());
        assertEquals(acct2, wallet.getDefaultAccount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAccounts_lastRemainingAccount() {

        Wallet w = Wallet.createWallet();
        Account lastRemainingAccount = w.getAccounts().get(0);

        assertEquals(w, lastRemainingAccount.getWallet());
        assertEquals(lastRemainingAccount, w.getDefaultAccount());
        w.removeAccount(lastRemainingAccount.getScriptHash());
    }

    @Test
    public void testDefaultWalletToNEP6Wallet() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, CipherException {

        String walletName = "TestWallet";
        Account a = new Account(ECKeyPair.createEcKeyPair());
        Wallet w = Wallet.withAccounts(a)
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
        Wallet w = Wallet.withAccounts(a);
        w.addAccounts(a);
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
        assertEquals("neow3jWallet", w.getName());
        assertEquals(Wallet.CURRENT_VERSION, w.getVersion());
        assertEquals(NEP2.DEFAULT_SCRYPT_PARAMS, w.getScryptParams());
        assertEquals(1, w.getAccounts().size());
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

        assertEquals("neow3jWallet", w1.getName());
        assertEquals(Wallet.CURRENT_VERSION, w1.getVersion());
        assertEquals(NEP2.DEFAULT_SCRYPT_PARAMS, w1.getScryptParams());
        assertEquals(1, w1.getAccounts().size());
        assertThat(w1.getAccounts(), not(empty()));
        assertTrue(tempFile.exists());
        assertThat(w1.getAccounts().get(0).getECKeyPair(), is(nullValue()));

        Wallet w2 = Wallet.fromNEP6Wallet(tempFile.toURI());
        w2.decryptAllAccounts("12345678");

        assertEquals(w2.toNEP6Wallet(), w1.toNEP6Wallet());
    }

    @Test
    public void testCreateGenericWalletAndSaveToFileWithPasswordAndDestination()
            throws CipherException, IOException, NEP2InvalidFormat, NEP2InvalidPassphrase {
        File tempFile = createTempFile();

        Wallet w1 = Wallet.createWallet("12345678", tempFile);

        assertEquals("neow3jWallet", w1.getName());
        assertEquals(Wallet.CURRENT_VERSION, w1.getVersion());
        assertEquals(NEP2.DEFAULT_SCRYPT_PARAMS, w1.getScryptParams());
        assertEquals(1, w1.getAccounts().size());
        assertThat(w1.getAccounts(), not(empty()));
        assertTrue(tempFile.exists());
        assertThat(w1.getAccounts().get(0).getECKeyPair(), is(nullValue()));

        Wallet w2 = Wallet.fromNEP6Wallet(tempFile.toURI());
        w2.decryptAllAccounts("12345678");

        assertEquals(w1.getName(), w2.getName());
        assertEquals(w1.getVersion(), w2.getVersion());
        assertEquals(w1.getScryptParams(), w2.getScryptParams());
        assertEquals(w1.getAccounts().size(), w2.getAccounts().size());
        assertThat(w2.getAccounts().get(0).getECKeyPair(), is(notNullValue()));
        assertTrue(tempFile.exists());

        assertEquals(w1.toNEP6Wallet(), w2.toNEP6Wallet());
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

        assertEquals("neow3jWallet", w1.getName());
        assertEquals(Wallet.CURRENT_VERSION, w1.getVersion());
        assertEquals(NEP2.DEFAULT_SCRYPT_PARAMS, w1.getScryptParams());
        assertEquals(1, w1.getAccounts().size());
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
        w.addAccounts(a);
        w.setDefaultAccount(a.getScriptHash());
        assertThat(w.getDefaultAccount(), notNullValue());
        assertEquals(a, w.getDefaultAccount());
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
        w.addAccounts(Account.createAccount());
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
        Account a2 = Account.fromAddress("AZt9DgwW8PKSEQsa9QLX86SyE1DSNjSbsS");
        WalletTestHelper.setUpWireMockForCall("getnep5balances",
                "getnep5balances_AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm.json",
                "AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm");
        WalletTestHelper.setUpWireMockForCall("getnep5balances",
                "getnep5balances_AZt9DgwW8PKSEQsa9QLX86SyE1DSNjSbsS.json",
                "AZt9DgwW8PKSEQsa9QLX86SyE1DSNjSbsS");
        Wallet w = Wallet.withAccounts(a1, a2);
        Map<ScriptHash, BigInteger> balances = w.getNep5TokenBalances(neow);
        assertThat(balances.keySet(), containsInAnyOrder(GAS_SCRIPT_HASH, NEO_SCRIPT_HASH));
        assertThat(balances.values(), containsInAnyOrder(
                new BigInteger("411285799730"),
                new BigInteger("50000000")));
    }
}
