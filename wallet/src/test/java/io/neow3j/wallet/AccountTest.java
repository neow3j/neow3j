package io.neow3j.wallet;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.nep6.NEP6Account;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import okhttp3.OkHttpClient;
import org.junit.Test;


public class AccountTest {

    @Test
    public void testCreateGenericAccount() {
        Account a = Account.createAccount();
        assertThat(a, notNullValue());
        assertThat(a.getAddress(), notNullValue());
        assertThat(a.getBalances(), notNullValue());
        assertThat(a.getVerificationScript(), notNullValue());
        assertThat(a.getECKeyPair(), notNullValue());
        assertThat(a.getEncryptedPrivateKey(), is(nullValue()));
        assertThat(a.getLabel(), notNullValue());
        assertThat(a.getPrivateKey(), notNullValue());
        assertThat(a.getPublicKey(), notNullValue());
        assertThat(a.isDefault(), is(false));
        assertThat(a.isLocked(), is(false));
    }

    @Test
    public void testFromNewECKeyPair() {
        Account a = Account.fromNewECKeyPair()
                .isDefault(true)
                .isLocked(false)
                .build();

        assertThat(a, notNullValue());
        assertThat(a.getAddress(), notNullValue());
        assertThat(a.getBalances(), notNullValue());
        assertThat(a.getVerificationScript(), notNullValue());
        assertThat(a.getECKeyPair(), notNullValue());
        assertThat(a.getEncryptedPrivateKey(), is(nullValue()));
        assertThat(a.getLabel(), notNullValue());
        assertThat(a.getPrivateKey(), notNullValue());
        assertThat(a.getPublicKey(), notNullValue());
        assertThat(a.isDefault(), is(true));
        assertThat(a.isLocked(), is(false));
    }

    @Test
    public void testBuildAccountFromExistingKeyPair() {
        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(TestKeys.privKey1));
        Account a = Account.fromECKeyPair(pair).build();

        assertThat(a.isMultiSig(), is(false));
        assertThat(a.getECKeyPair(), is(pair));
        assertThat(a.getAddress(), is(TestKeys.address1));
        assertThat(a.getLabel(), is(TestKeys.address1));
        assertThat(a.getVerificationScript().getScript(),
                is(Numeric.hexStringToByteArray(TestKeys.verificationScript1)));
    }

    @Test
    public void testFromMultiSigKeys() {
        List<ECPublicKey> keys = Arrays.asList(
                new ECPublicKey(Numeric.hexStringToByteArray(TestKeys.pubKey2_1)),
                new ECPublicKey(Numeric.hexStringToByteArray(TestKeys.pubKey2_2)));

        Account a = Account.fromMultiSigKeys(keys, 2).build();

        assertThat(a.isMultiSig(), is(true));
        assertThat(a.getAddress(), is(TestKeys.address2));
        assertThat(a.getPublicKey2(), is(nullValue()));
        assertThat(a.getPrivateKey(), is(nullValue()));
        assertThat(a.getLabel(), is(a.getAddress()));
        assertThat(a.getVerificationScript().getScript(),
                is(Numeric.hexStringToByteArray(TestKeys.verificationScript2)));
    }

    @Test
    public void testEncryptPrivateKey() throws CipherException {
        // Test keys taken from City of Zion's neon-js test code.
        String wif = "L1QqQJnpBwbsPGAuutuzPTac8piqvbR1HRjrY5qHup48TBCBFe4g";
        String passphrase = "city of zion";
        String expected = "6PYLPLfpCw87u1t7TP14gkNweUkuqwpso8qmMt24Kp8aona6K7fvurdsDQ";
        Account account = Account.fromWIF(wif).build();
        account.encryptPrivateKey(passphrase, NEP2.DEFAULT_SCRYPT_PARAMS);
        assertEquals(expected, account.getEncryptedPrivateKey());

        wif = "KyKvWLZsNwBJx5j9nurHYRwhYfdQUu9tTEDsLCUHDbYBL8cHxMiG";
        passphrase = "MyL33tP@33w0rd";
        expected = "6PYQ5fKhgWtqs2y81eBVbt1GsEWx634cRHeJcuknwUW2PyVc9itQqfLhtR";
        ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        account = Account.fromWIF(wif).build();
        account.encryptPrivateKey(passphrase, nonDefaultScryptParams);
        assertEquals(expected, account.getEncryptedPrivateKey());
    }

    @Test
    public void decryptWithStandardScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        // Test keys taken from City of Zion's neon-js test code.
        final String address = "AVtX3Qw8McvUstNESaMUSE8Vujn7H4SkCB";
        final String nep2Encrypted = "6PYLPLfpCw87u1t7TP14gkNweUkuqwpso8qmMt24Kp8aona6K7fvurdsDQ";
        final String passphrase = "city of zion";
        final String wif = "L1QqQJnpBwbsPGAuutuzPTac8piqvbR1HRjrY5qHup48TBCBFe4g";

        NEP6Account nep6Acct = new NEP6Account(
                address, "", true, false, nep2Encrypted, null, null);
        Account a = Account.fromNEP6Account(nep6Acct).build();
        a.decryptPrivateKey(passphrase, NEP2.DEFAULT_SCRYPT_PARAMS);
        assertEquals(wif, a.getECKeyPair().exportAsWIF());
    }

    @Test
    public void decryptWithNonStandardScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        // Test keys taken from City of Zion's neon-js test code.
        final ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        final String address = "AVtX3Qw8McvUstNESaMUSE8Vujn7H4SkCB";
        final String nep2Encrypted = "6PYLPLfpCoGkGvVFeN9KjvvT6dNBoYag3c2co362y9Gge1GSjMewf5J6tc";
        final String passphrase = "city of zion";
        final String wif = "L1QqQJnpBwbsPGAuutuzPTac8piqvbR1HRjrY5qHup48TBCBFe4g";

        NEP6Account nep6Acct = new NEP6Account(
                address, "", true, false, nep2Encrypted, null, null);
        Account a = Account.fromNEP6Account(nep6Acct).build();
        a.decryptPrivateKey(passphrase, nonDefaultScryptParams);
        assertEquals(wif, a.getECKeyPair().exportAsWIF());
    }

    @Test
    public void testUpdateAccountBalances() throws IOException, ErrorResponseException {
        // TODO: Adapt to new account model
        String address = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new ResponseInterceptor(address)).build();
        HttpService httpService = new HttpService(httpClient);
        Neow3j neow3j = Neow3j.build(httpService);

        Account a = Account.fromAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y").build();
        a.updateAssetBalances(neow3j);
    }


}
