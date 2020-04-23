package io.neow3j.wallet;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPrivateKey;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.NEP2;
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
        // Used neo-core with address version 0x17 to generate test data.
        String expectedAdr = "AMEr3rD5jUBRdkkmqmEU1WhhtVsAPXG2q9";
        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"));
        String verScirpt =
                "0c21027a593180860c4037c83c12749845c8ee1424dd297fadcb895e358255d2c7d2b20b410a906ad4";

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
        String adr = "AQ3ZPRnoBGBkfjgxGa9gZkELb8knQSU5xe";
        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"));
        List<ECPublicKey> keys = Arrays.asList(pair.getPublicKey(), pair.getPublicKey());
        Account a = Account.fromMultiSigKeys(keys, 2).build();
        byte[] verScript = Numeric.hexStringToByteArray(
                "120c21027a593180860c4037c83c12749845c8ee1424dd297fadcb895e358255d2c7d2b20c21027a593180860c4037c83c12749845c8ee1424dd297fadcb895e358255d2c7d2b2120b413073b3bb");
        assertThat(a.isMultiSig(), is(true));
        assertThat(a.getAddress(), is(adr));
        assertThat(a.getPublicKey(), is(nullValue()));
        assertThat(a.getPrivateKey(), is(nullValue()));
        assertThat(a.getLabel(), is(adr));
        assertThat(a.getVerificationScript().getScript(), is(verScript));
    }

    // TODO: This test needs examples generated with the newest neo core implementation.
    //  The current test data is not up to date and therefore fails.
    @Test
    public void testEncryptPrivateKey() throws CipherException {
        // WIF created from private key
        // 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        String password = "pwd";
        // Used neo-core with address version 0x17 to generate the encrypted key.
        String expectedNep2Encrypted = "6PYX9GMW3WgtYcivcWgrqzk2igqY8jhnMcysgFw4npoLqRnxZ16yj8V6V1";
        Account a = Account.fromWIF(wif).build();
        a.encryptPrivateKey(password, NEP2.DEFAULT_SCRYPT_PARAMS);
        assertThat(a.getEncryptedPrivateKey(), is(expectedNep2Encrypted));
    }

    @Test
    public void decryptWithStandardScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        final ECPrivateKey privateKey = new ECPrivateKey(Numeric.toBigInt(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"));
        String password = "pwd";
        // Used neo-core with address version 0x17 to generate the encrypted key.
        String nep2Encrypted = "6PYX9GMW3WgtYcivcWgrqzk2igqY8jhnMcysgFw4npoLqRnxZ16yj8V6V1";

        NEP6Account nep6Acct = new NEP6Account("", "", true, false, nep2Encrypted, null, null);
        Account a = Account.fromNEP6Account(nep6Acct).build();
        a.decryptPrivateKey(password, NEP2.DEFAULT_SCRYPT_PARAMS);
        assertThat(a.getPrivateKey(), is(privateKey));
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
        fail();
    }


}
