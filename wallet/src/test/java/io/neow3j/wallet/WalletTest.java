package io.neow3j.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.crypto.Keys;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Wallet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WalletTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testCreateDefaultWallet() {
        Wallet w = new Wallet.Builder().build();
        assertEquals(w.getName(), "neow3jWallet");
        assertEquals(w.getVersion(), Wallet.CURRENT_VERSION);
        assertTrue(w.getAccounts().isEmpty());
    }

    @Test
    public void testCreateWalletFromNEP6File() throws IOException {

        URL nep6WalletFile = Thread.currentThread().getContextClassLoader().getResource("wallet.json");
        Wallet w = Wallet.fromNEP6Wallet(nep6WalletFile).build();

        ObjectMapper mapper = new ObjectMapper();
        NEP6Wallet nep6Wallet = mapper.readValue(nep6WalletFile, NEP6Wallet.class);

        assertEquals("Wallet", w.getName());
        assertEquals(Wallet.CURRENT_VERSION, w.getVersion());
        assertEquals(2, w.getAccounts().size());
        assertEquals(NEP2.DEFAULT_SCRYPT_PARAMS, w.getScryptParams());

        Account a = w.getAccounts().get(0);
        assertEquals("AWUfbdLYUeJ5X6gvbPQYkjL4JZ78z2X9Pk", a.getAddress());
        assertEquals("Account1", a.getLabel());
        assertFalse(a.isDefault());
        assertFalse(a.isLocked());
        assertEquals("6PYUnzmokRh7JwfYntrMq6LYw4pF4QJ343fJHMKoKDvCqNgfV6msFGGcEH", a.getEncryptedPrivateKey());
        assertEquals(a.getContract(), nep6Wallet.getAccounts().get(0).getContract());

        a = w.getAccounts().get(1);
        assertEquals("AThCriBXLBQxyPNYHUwa8NVoKYM5JwL1Yg", a.getAddress());
        assertEquals("Account2", a.getLabel());
        assertFalse(a.isDefault());
        assertFalse(a.isLocked());
        assertEquals("6PYRUJuaSqrvkQVdfn9MBdzJDNDwXMdHNNiNAMYJhGk7MUgdiU4KshyuGX", a.getEncryptedPrivateKey());
        assertEquals(a.getContract(), nep6Wallet.getAccounts().get(1).getContract());
    }

    @Test
    public void testAddAccount() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = new Wallet.Builder().build();
        Account acct = Account.fromECKeyPair(Keys.createEcKeyPair()).build();
        w.addAccount(acct);
        assertTrue(!w.getAccounts().isEmpty());
        assertEquals(w.getAccounts().get(0),acct);
    }

    @Test
    public void testAddDuplicateAccount() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = new Wallet.Builder().build();
        Account acct = Account.fromECKeyPair(Keys.createEcKeyPair()).build();
        assertTrue(w.addAccount(acct));
        assertFalse(w.addAccount(acct));
    }

    @Test
    public void testRemoveAccounts() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = new Wallet.Builder().build();
        assertFalse(w.removeAccount(SampleKeys.ADDRESS_1));
        Account acct1 = Account.fromECKeyPair(Keys.createEcKeyPair()).build();
        w.addAccount(acct1);
        Account acct2 = Account.fromECKeyPair(Keys.createEcKeyPair()).build();
        w.addAccount(acct2);
        assertTrue(w.removeAccount(acct1.getAddress()));
        assertTrue(w.removeAccount(acct2.getAddress()));
    }

    @Test
    public void testDefaultWalletToNEP6Wallet() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, CipherException {

        String walletName = "TestWallet";
        Wallet w = new Wallet.Builder().name(walletName).build();
        Account a = Account.fromECKeyPair(Keys.createEcKeyPair()).build();
        w.addAccount(a);
        w.encryptAllAccounts("12345678");

        NEP6Account nep6acct = new NEP6Account(a.getAddress(), a.getLabel(), false, false,
                a.getEncryptedPrivateKey(), a.getContract(), null);
        NEP6Wallet nep6w = new NEP6Wallet(walletName, Wallet.CURRENT_VERSION,
                NEP2.DEFAULT_SCRYPT_PARAMS,  Collections.singletonList(nep6acct), null);


        assertEquals(nep6w, w.toNEP6Wallet());
    }

    @Test
    public void testToNEP6WalletWithUnencryptedPrivateKey() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Wallet w = new Wallet.Builder().build();
        Account a = Account.fromECKeyPair(Keys.createEcKeyPair()).build();
        w.addAccount(a);

        exceptionRule.expect(IllegalStateException.class);
        w.toNEP6Wallet();
    }


    @Test
    public void testFromNEP6WalletToNEP6Wallet() throws IOException {
        URL nep6WalletFile = Thread.currentThread().getContextClassLoader().getResource("wallet.json");
        ObjectMapper mapper = new ObjectMapper();
        NEP6Wallet nep6Wallet = mapper.readValue(nep6WalletFile, NEP6Wallet.class);

        Wallet w = Wallet.fromNEP6Wallet(nep6Wallet).build();

        assertEquals(nep6Wallet, w.toNEP6Wallet());
    }
}
