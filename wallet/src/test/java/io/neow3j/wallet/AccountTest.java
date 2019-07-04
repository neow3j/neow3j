package io.neow3j.wallet;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Keys;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.nep6.NEP6Account;
import okhttp3.OkHttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;


public class AccountTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testBuildAccountFromKeyPair() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        Account a = Account.fromECKeyPair(ecKeyPair).build();
        assertEquals(ecKeyPair, a.getECKeyPair());
        // TODO Claude 11.06.19 Implement
    }

    @Test
    public void testCreateVerificationScriptContract() {
        // TODO Claude 11.06.19: Implement
    }

    @Test
    public void testCreateStandardAccount1() throws CipherException {
        Account account = Account.fromWIF("L44B5gGEpqEDRS9vVPz7QT35jcBG2r3CZwSwQ4fCewXAhAhqGVpP").build();
        account.encryptPrivateKey("TestingOneTwoThree", NEP2.DEFAULT_SCRYPT_PARAMS);
        assertEquals("6PYVPVe1fQznphjbUxXP9KZJqPMVnVwCx5s5pr5axRJ8uHkMtZg97eT5kL", account.getEncryptedPrivateKey());
    }

    @Test
    public void testCreateStandardAccount2() throws CipherException {
        Account account = Account.fromWIF("KwYgW8gcxj1JWJXhPSu4Fqwzfhp5Yfi42mdYmMa4XqK7NJxXUSK7").build();
        account.encryptPrivateKey("Satoshi", NEP2.DEFAULT_SCRYPT_PARAMS);
        assertEquals("6PYN6mjwYfjPUuYT3Exajvx25UddFVLpCw4bMsmtLdnKwZ9t1Mi3CfKe8S", account.getEncryptedPrivateKey());
    }

    @Test
    public void testDecryptStandard1() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        NEP6Account nep6Acct = new NEP6Account(
                "AStZHy8E6StCqYQbzMqi4poH7YNDHQKxvt", "", true, false,
                "6PYVPVe1fQznphjbUxXP9KZJqPMVnVwCx5s5pr5axRJ8uHkMtZg97eT5kL", null, null);

        Account a = Account.fromNEP6Account(nep6Acct).build();
        a.decryptPrivateKey("TestingOneTwoThree", NEP2.DEFAULT_SCRYPT_PARAMS);
        assertEquals("L44B5gGEpqEDRS9vVPz7QT35jcBG2r3CZwSwQ4fCewXAhAhqGVpP", a.getECKeyPair().exportAsWIF());
    }

    @Test
    public void testDecryptStandard2() throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {

        NEP6Account nep6Acct = new NEP6Account(
                "AXoxAX2eJfJ1shNpWqUxRh3RWNUJqvQvVa", "", true, false,
                "6PYN6mjwYfjPUuYT3Exajvx25UddFVLpCw4bMsmtLdnKwZ9t1Mi3CfKe8S", null, null);

        Account a = Account.fromNEP6Account(nep6Acct).build();
        a.decryptPrivateKey("Satoshi", NEP2.DEFAULT_SCRYPT_PARAMS);
        assertEquals("KwYgW8gcxj1JWJXhPSu4Fqwzfhp5Yfi42mdYmMa4XqK7NJxXUSK7", a.getECKeyPair().exportAsWIF());
    }

    @Test
    public void testDecryptStandard3() throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {

        NEP6Account nep6Acct = new NEP6Account(
                "AdGPiWRqqoFMauM6anTNFB7MyBwQhEANyZ", "", true, false,
                "6PYUNvLELtv66vFYgmHuu11je7h4hTZiLTVbRk4RNvJZo75PurR6z7JnoX", null, null);

        Account a = Account.fromNEP6Account(nep6Acct).build();
        Wallet w = new Wallet.Builder().account(a).build();
        a.decryptPrivateKey("q1w2e3!@#", NEP2.DEFAULT_SCRYPT_PARAMS);
        assertEquals("L5fE7aDEiBLJwcf3Zr9NrUUuT9Rd8nc4kPkuJWqNhftdmx3xcyAd", a.getECKeyPair().exportAsWIF());
    }

    @Test
    public void testUpdateAccountBalances() throws IOException, ErrorResponseException {
        String address = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new ResponseInterceptor(address)).build();
        HttpService httpService = new HttpService(httpClient);
        Neow3j neow3j = Neow3j.build(httpService);

        Account a = Account.fromAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y").build();
        a.updateAssetBalances(neow3j);
    }

    @Test
    public void testCreateGenericAccount() {
        Account a = Account.createGenericAccount();
        assertThat(a, notNullValue());
        assertThat(a.getAddress(), notNullValue());
        assertThat(a.getBalances(), notNullValue());
        assertThat(a.getContract(), notNullValue());
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
        assertThat(a.getContract(), notNullValue());
        assertThat(a.getECKeyPair(), notNullValue());
        assertThat(a.getEncryptedPrivateKey(), is(nullValue()));
        assertThat(a.getLabel(), notNullValue());
        assertThat(a.getPrivateKey(), notNullValue());
        assertThat(a.getPublicKey(), notNullValue());
        assertThat(a.isDefault(), is(true));
        assertThat(a.isLocked(), is(false));
    }

}
