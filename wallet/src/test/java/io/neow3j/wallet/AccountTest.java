package io.neow3j.wallet;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPrivateKey;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.nep6.NEP6Account;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;


public class AccountTest {

    @Test
    public void testCreateGenericAccount() {
        Account a = Account.createAccount();
        assertThat(a, notNullValue());
        assertThat(a.getAddress(), notNullValue());
        assertThat(a.getVerificationScript(), notNullValue());
        assertThat(a.getECKeyPair(), notNullValue());
        assertThat(a.getEncryptedPrivateKey(), is(nullValue()));
        assertThat(a.getLabel(), notNullValue());
        assertThat(a.getECKeyPair(), notNullValue());
        assertThat(a.isDefault(), is(false));
        assertThat(a.isLocked(), is(false));
    }

    @Test
    public void testFromNewECKeyPair() {
        Account a = Account.fromNewECKeyPair()
                .isDefault()
                .build();

        assertThat(a, notNullValue());
        assertThat(a.getAddress(), notNullValue());
        assertThat(a.getVerificationScript(), notNullValue());
        assertThat(a.getECKeyPair(), notNullValue());
        assertThat(a.getEncryptedPrivateKey(), is(nullValue()));
        assertThat(a.getLabel(), notNullValue());
        assertThat(a.getECKeyPair(), notNullValue());
        assertThat(a.isDefault(), is(true));
        assertThat(a.isLocked(), is(false));
    }

    @Test
    public void testBuildAccountFromExistingKeyPair() {
        // Used neo-core with address version 0x17 to generate test data.
        String expectedAdr = "AMuDKuFCrHNtEg4jCV17ge4eyoa3JwD9fH";
        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"));
        String verScirpt = "0c21"
                + "027a593180860c4037c83c12749845c8ee1424dd297fadcb895e358255d2c7d2b2"
                + OpCode.PUSHNULL
                + OpCode.SYSCALL.toString()
                + InteropServiceCode.NEO_CRYPTO_ECDSA_SECP256R1_VERIFY.getHash();

        Account a = Account.fromECKeyPair(pair).build();
        assertThat(a.isMultiSig(), is(false));
        assertThat(a.getECKeyPair(), is(pair));
        assertThat(a.getAddress(), is(expectedAdr));
        assertThat(a.getLabel(), is(expectedAdr));
        assertThat(a.getVerificationScript().getScript(),
                is(Numeric.hexStringToByteArray(verScirpt)));
    }

    @Test
    public void testFromMultiSigKeys() {
        // Used neo-core with address version 0x17 to generate test data.
        String adr = "AKmZGyN7AmQDvH6Q9eEbBPuG1nDgCRyrcP";
        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"));
        List<ECPublicKey> keys = Arrays.asList(pair.getPublicKey(), pair.getPublicKey());
        Account a = Account.fromMultiSigKeys(keys, 2).build();
        byte[] verScript = Numeric.hexStringToByteArray(
                "120c21027a593180860c4037c83c12749845c8ee1424dd297fadcb895e358255d2c7d2b20c21027a593180860c4037c83c12749845c8ee1424dd297fadcb895e358255d2c7d2b2120b41c330181e");
        assertThat(a.isMultiSig(), is(true));
        assertThat(a.getAddress(), is(adr));
        assertThat(a.getECKeyPair(), is(nullValue()));
        assertThat(a.getLabel(), is(adr));
        assertThat(a.getVerificationScript().getScript(), is(verScript));
    }

    @Test
    public void testEncryptPrivateKey() throws CipherException {
        String privKeyString = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f";
        ECKeyPair keyPair = ECKeyPair.create(Numeric.hexStringToByteArray(privKeyString));
        String password = "pwd";
        // Used neo-core with address version 0x17 to generate the encrypted key.
        String expectedNep2Encrypted = "6PYMGfNyeJAf8bLXmPh8MbJxLB8uvQqtnZje1RUhhUcDDucj55dZsvbk8k";
        Account a = Account.fromECKeyPair(keyPair).build();
        a.encryptPrivateKey(password);
        assertThat(a.getEncryptedPrivateKey(), is(expectedNep2Encrypted));
    }

    @Test
    public void decryptWithStandardScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        final ECPrivateKey privateKey = new ECPrivateKey(Numeric.toBigInt(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"));
        String password = "pwd";
        // Used neo-core with address version 0x17 to generate the encrypted key.
        String nep2Encrypted = "6PYMGfNyeJAf8bLXmPh8MbJxLB8uvQqtnZje1RUhhUcDDucj55dZsvbk8k";

        NEP6Account nep6Acct = new NEP6Account("", "", true, false, nep2Encrypted, null, null);
        Account a = Account.fromNEP6Account(nep6Acct).build();
        a.decryptPrivateKey(password);
        assertThat(a.getECKeyPair().getPrivateKey(), is(privateKey));
    }

    @Test
    public void loadAccountFromNEP6Account() throws URISyntaxException, IOException {
        URL nep6AccountFileUrl = AccountTest.class.getClassLoader().getResource("account.json");
        FileInputStream stream = new FileInputStream(new File(nep6AccountFileUrl.toURI()));
        NEP6Account nep6Acc = new ObjectMapper().readValue(stream, NEP6Account.class);
        Account a = Account.fromNEP6Account(nep6Acc).build();
        assertFalse(a.isDefault());
        assertFalse(a.isLocked());
        assertThat(a.getAddress(), is("AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm"));
        String privKey = "6PYV39zSDnpCb9ecybeL3z6XrLTpKy1AugUGd6DYFFNELHv9aLj6M7KGD2";
        assertThat(a.getEncryptedPrivateKey(), is(privKey));
        byte[] expectedScript = Numeric.hexStringToByteArray(
                "0c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380b418a6b1e75");
        assertThat(a.getVerificationScript().getScript(), is(expectedScript));
    }

}
