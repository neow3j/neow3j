package io.neow3j.wallet;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Files;
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
import org.junit.rules.ExpectedException;

public class WalletTest {

    private static final ScriptHash NEO_SCRIPT_HASH = new ScriptHash(
            "de5f57d430d3dece511cf975a8d37848cb9e0525");
    private static final ScriptHash GAS_SCRIPT_HASH = new ScriptHash(
            "668e0c1f9d7b70a99dd9e06eadd4c784d641afbc");

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testCreateDefaultWallet() {
        Wallet w = Wallet.create();
        assertEquals(w.getName(), "neow3jWallet");
        assertEquals(w.getVersion(), Wallet.CURRENT_VERSION);
        assertFalse(w.getAccounts().isEmpty());
    }

    @Test
    public void testCreateWalletWithAccounts() {
        Account acct1 = Account.create();
        Account acct2 = Account.create();
        Wallet wallet = Wallet.withAccounts(acct1, acct2);

        assertEquals(acct1, wallet.getDefaultAccount());
        assertThat(wallet.getAccounts(), containsInAnyOrder(acct1, acct2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWalletWithAccounts_noAccounts() {
        Wallet.withAccounts();
    }

    @Test
    public void testIsDefault_account() {
        Account account = Account.create();
        Wallet wallet = Wallet.withAccounts(account);
        assertTrue(wallet.isDefault(account));
    }

    @Test
    public void testHoldsAccount() {
        Account account = Account.create();
        Wallet wallet = Wallet.create();
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

        Account a = w.getAccount(ScriptHash.fromAddress("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke"));
        assertEquals("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke", a.getAddress());
        assertEquals("Account1", a.getLabel());
        assertFalse(a.isLocked());
        assertEquals("6PYVEi6ZGdsLoCYbbGWqoYef7VWMbKwcew86m5fpxnZRUD8tEjainBgQW1",
                a.getEncryptedPrivateKey());
        assertEquals(
                "DCECJJQloGtaH45hM/x5r6LCuEML+TJyl/F2dh33no2JKcULQZVEDXg=",
                nep6Wallet.getAccounts().get(0).getContract().getScript()
        );

        a = w.getAccount(ScriptHash.fromAddress("NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmy"));
        assertEquals("NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmy", a.getAddress());
        assertEquals("Account2", a.getLabel());
        assertFalse(a.isDefault());
        assertFalse(a.isLocked());
        assertEquals("6PYSQWBqZE5oEFdMGCJ3xR7bz6ezz814oKE7GqwB9i5uhtUzkshe9B6YGB",
                a.getEncryptedPrivateKey());
        assertEquals(
                "DCEDHMqqRt98SU9EJpjIwXwJMR42FcLcBCy9Ov6rpg+kB0ALQZVEDXg=",
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

        Wallet w = Wallet.create();
        Account acct = new Account(ECKeyPair.createEcKeyPair());
        w.addAccounts(acct);
        assertEquals(2, w.getAccounts().size());
        assertEquals(w.getAccount(acct.getScriptHash()), acct);
    }

    @Test
    public void testAddDuplicateAccount() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = Wallet.create();
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
        Wallet wallet = Wallet.create();

        wallet.getAccount(account.getScriptHash());
    }

    @Test
    public void testRemoveAccounts() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        final String address = "NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmy";
        Wallet w = Wallet.create();
        assertFalse(w.removeAccount(ScriptHash.fromAddress(address)));
        Account acct1 = new Account(ECKeyPair.createEcKeyPair());
        w.addAccounts(acct1);
        Account acct2 = new Account(ECKeyPair.createEcKeyPair());
        w.addAccounts(acct2);
        assertTrue(w.removeAccount(acct1.getScriptHash()));
        assertTrue(w.removeAccount(acct2.getScriptHash()));
    }

    @Test
    public void testRemoveAccounts_accountParam() {
        Account a1 = Account.create();
        Account a2 = Account.create();
        Wallet wallet = Wallet.withAccounts(a1, a2);

        assertThat(wallet.getAccounts(), hasSize(2));
        assertTrue(wallet.removeAccount(a2));
        assertThat(wallet.getAccounts(), hasSize(1));
        assertThat(wallet.getAccounts().get(0), is(a1));
    }

    @Test
    public void testRemoveAccounts_defaultAccount() {
        Account acct1 = Account.create();
        Account acct2 = Account.create();
        Wallet wallet = Wallet.withAccounts(acct1, acct2);

        assertEquals(2, wallet.getAccounts().size());
        assertEquals(acct1, wallet.getDefaultAccount());

        wallet.removeAccount(acct1.getScriptHash());

        assertEquals(1, wallet.getAccounts().size());
        assertEquals(acct2, wallet.getDefaultAccount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAccounts_lastRemainingAccount() {

        Wallet w = Wallet.create();
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
                .name(walletName);
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
        Wallet w = Wallet.create();
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

        Wallet w1 = Wallet.create();
        w1.encryptAllAccounts("12345678");
        w1.saveNEP6Wallet(tempFile);

        assertEquals("neow3jWallet", w1.getName());
        assertEquals(Wallet.CURRENT_VERSION, w1.getVersion());
        assertEquals(NEP2.DEFAULT_SCRYPT_PARAMS, w1.getScryptParams());
        assertEquals(1, w1.getAccounts().size());
        assertThat(w1.getAccounts(), not(empty()));
        assertTrue(tempFile.exists());

        Wallet w2 = Wallet.fromNEP6Wallet(tempFile.toURI());
        w2.decryptAllAccounts("12345678");

        assertEquals(w2.toNEP6Wallet(), w1.toNEP6Wallet());
    }

    @Test
    public void testCreateGenericWalletAndSaveItToDir_withCustomName()
            throws CipherException, IOException, NEP2InvalidFormat, NEP2InvalidPassphrase {
        File tempDir = createTempDir();

        Wallet w1 = Wallet.create()
                .name("customWalletName");
        w1.encryptAllAccounts("12345678");
        w1.saveNEP6Wallet(tempDir);

        assertEquals("customWalletName", w1.getName());
        assertEquals(Wallet.CURRENT_VERSION, w1.getVersion());
        assertEquals(NEP2.DEFAULT_SCRYPT_PARAMS, w1.getScryptParams());
        assertEquals(1, w1.getAccounts().size());
        assertThat(w1.getAccounts(), not(empty()));
        assertTrue(tempDir.exists());

        File file = new File(tempDir, "customWalletName.json");
        Wallet w2 = Wallet.fromNEP6Wallet(file.toURI());
        w2.decryptAllAccounts("12345678");

        assertEquals(w2.toNEP6Wallet(), w1.toNEP6Wallet());
    }

    @Test
    public void testCreateGenericWalletAndSaveItToDir_withDefaultName()
            throws CipherException, IOException, NEP2InvalidFormat, NEP2InvalidPassphrase {
        File tempDir = createTempDir();

        Wallet w1 = Wallet.create();
        w1.encryptAllAccounts("12345678");
        w1.saveNEP6Wallet(tempDir);

        assertEquals("neow3jWallet", w1.getName());
        assertEquals(Wallet.CURRENT_VERSION, w1.getVersion());
        assertEquals(NEP2.DEFAULT_SCRYPT_PARAMS, w1.getScryptParams());
        assertEquals(1, w1.getAccounts().size());
        assertThat(w1.getAccounts(), not(empty()));
        assertTrue(tempDir.exists());

        File file = new File(tempDir, "neow3jWallet.json");
        Wallet w2 = Wallet.fromNEP6Wallet(file.toURI());
        w2.decryptAllAccounts("12345678");

        assertEquals(w2.toNEP6Wallet(), w1.toNEP6Wallet());
    }

    @Test
    public void testCreateGenericWalletAndSaveToFileWithPasswordAndDestination()
            throws CipherException, IOException, NEP2InvalidFormat, NEP2InvalidPassphrase {
        File tempFile = createTempFile();

        Wallet w1 = Wallet.create("12345678", tempFile);

        assertEquals("neow3jWallet", w1.getName());
        assertEquals(Wallet.CURRENT_VERSION, w1.getVersion());
        assertEquals(NEP2.DEFAULT_SCRYPT_PARAMS, w1.getScryptParams());
        assertEquals(1, w1.getAccounts().size());
        assertThat(w1.getAccounts(), not(empty()));
        assertTrue(tempFile.exists());

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

    private File createTempDir() {
        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        return tempDir;
    }

    @Test
    public void testCreateGenericWalletWithPassword()
            throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {

        Wallet w1 = Wallet.create("12345678");

        assertEquals("neow3jWallet", w1.getName());
        assertEquals(Wallet.CURRENT_VERSION, w1.getVersion());
        assertEquals(NEP2.DEFAULT_SCRYPT_PARAMS, w1.getScryptParams());
        assertEquals(1, w1.getAccounts().size());
        assertThat(w1.getAccounts(), not(empty()));
        assertThat(w1.getAccounts().get(0).getEncryptedPrivateKey(), notNullValue());

        w1.decryptAllAccounts("12345678");
        assertThat(w1.getAccounts().get(0).getECKeyPair(), notNullValue());
        assertThat(w1.getAccounts().get(0).getECKeyPair(), notNullValue());
        assertThat(w1.getAccounts().get(0).getEncryptedPrivateKey(), notNullValue());
    }

    @Test
    public void testGetAndSetDefaultAccount() {
        Wallet w = Wallet.create();
        assertThat(w.getDefaultAccount(), notNullValue());

        Account a = Account.create();
        w.addAccounts(a);
        w.defaultAccount(a.getScriptHash());
        assertThat(w.getDefaultAccount(), notNullValue());
        assertEquals(a, w.getDefaultAccount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failSettingDefaultAccountNotContainedInWallet() {
        Wallet w = Wallet.create();
        Account a = Account.create();
        w.defaultAccount(a.getScriptHash());
    }

    @Test
    public void encryptWallet() throws CipherException {
        Wallet w = Wallet.create();
        w.addAccounts(Account.create());
        assertThat(w.getAccounts().get(0).getECKeyPair(), notNullValue());
        assertThat(w.getAccounts().get(1).getECKeyPair(), notNullValue());
        w.encryptAllAccounts("pw");

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("This account does not hold an EC key pair.");
        assertThat(w.getAccounts().get(0).getECKeyPair(), nullValue());
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    @Test
    public void getNep17Balances() throws IOException {
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);
        Neow3j neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));

        Account a1 = Account.fromAddress("NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmy");
        Account a2 = Account.fromAddress("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke");
        WalletTestHelper.setUpWireMockForCall("getnep17balances",
                "getnep17balances_NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmy.json",
                "NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmy");
        WalletTestHelper.setUpWireMockForCall("getnep17balances",
                "getnep17balances_NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke.json",
                "NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke");
        Wallet w = Wallet.withAccounts(a1, a2);
        Map<ScriptHash, BigInteger> balances = w.getNep17TokenBalances(neow);
        assertThat(balances.keySet(), containsInAnyOrder(GAS_SCRIPT_HASH, NEO_SCRIPT_HASH));
        assertThat(balances.values(), containsInAnyOrder(
                new BigInteger("411285799730"),
                new BigInteger("50000000")));
    }
}
