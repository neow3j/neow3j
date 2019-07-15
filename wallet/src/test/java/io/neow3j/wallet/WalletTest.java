package io.neow3j.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Wallet;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

        Wallet w = new Wallet.Builder().build();
        assertFalse(w.removeAccount(SampleKeys.ADDRESS_1));
        Account acct1 = Account.fromECKeyPair(ECKeyPair.createEcKeyPair()).build();
        w.addAccount(acct1);
        Account acct2 = Account.fromECKeyPair(ECKeyPair.createEcKeyPair()).build();
        w.addAccount(acct2);
        assertTrue(w.removeAccount(acct1.getAddress()));
        assertTrue(w.removeAccount(acct2.getAddress()));
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
                a.getEncryptedPrivateKey(), a.getContract(), null);
        NEP6Wallet nep6w = new NEP6Wallet(walletName, Wallet.CURRENT_VERSION,
                NEP2.DEFAULT_SCRYPT_PARAMS, Collections.singletonList(nep6acct), null);


        assertEquals(nep6w, w.toNEP6Wallet());
    }

    @Test(expected = IllegalStateException.class)
    public void testToNEP6WalletWithUnencryptedPrivateKey() throws InvalidAlgorithmParameterException,
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
    public void testCreateGenericWallet() {
        Wallet w = Wallet.createGenericWallet();
        assertThat(w.getName(), is("neow3jWallet"));
        assertThat(w.getVersion(), is(Wallet.CURRENT_VERSION));
        assertThat(w.getScryptParams(), is(NEP2.DEFAULT_SCRYPT_PARAMS));
        assertThat(w.getAccounts().size(), is(1));
        assertThat(w.getAccounts(), not(empty()));
        assertThat(w.getAccounts().get(0).getECKeyPair(), notNullValue());
    }

    @Test
    public void testGetAndSetDefaultAccount() {
        Wallet w = Wallet.createGenericWallet();
        assertThat(w.getDefaultAccount(), notNullValue());

        Account a = Account.createGenericAccount();
        w.addAccount(a);
        w.setDefaultAccount(1);
        assertThat(w.getDefaultAccount(), notNullValue());
        assertThat(w.getDefaultAccount(), is(a));
    }

}
