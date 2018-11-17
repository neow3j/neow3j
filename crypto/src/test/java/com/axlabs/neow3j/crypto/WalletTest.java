package com.axlabs.neow3j.crypto;

import com.axlabs.neow3j.crypto.exceptions.CipherException;
import com.axlabs.neow3j.crypto.exceptions.NEP2InvalidFormat;
import com.axlabs.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WalletTest {

    @Test
    public void testCreateStandardAccount1() throws CipherException {

        byte[] privateKey = WIF.getPrivateKeyFromWIF("L44B5gGEpqEDRS9vVPz7QT35jcBG2r3CZwSwQ4fCewXAhAhqGVpP");
        ECKeyPair keyPair = ECKeyPair.create(privateKey);

        WalletFile.Account account = Wallet.createStandardAccount("TestingOneTwoThree", keyPair);

        assertEquals("6PYVPVe1fQznphjbUxXP9KZJqPMVnVwCx5s5pr5axRJ8uHkMtZg97eT5kL", account.getKey());
    }

    @Test
    public void testCreateStandardAccount2() throws CipherException {

        byte[] privateKey = WIF.getPrivateKeyFromWIF("KwYgW8gcxj1JWJXhPSu4Fqwzfhp5Yfi42mdYmMa4XqK7NJxXUSK7");
        ECKeyPair keyPair = ECKeyPair.create(privateKey);

        WalletFile.Account account = Wallet.createStandardAccount("Satoshi", keyPair);

        assertEquals("6PYN6mjwYfjPUuYT3Exajvx25UddFVLpCw4bMsmtLdnKwZ9t1Mi3CfKe8S", account.getKey());
    }

    @Test
    public void testDecryptStandard1() throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {

        WalletFile.Account account = new WalletFile.Account(
                "AStZHy8E6StCqYQbzMqi4poH7YNDHQKxvt",
                "",
                true,
                false,
                "6PYVPVe1fQznphjbUxXP9KZJqPMVnVwCx5s5pr5axRJ8uHkMtZg97eT5kL",
                null,
                null
        );

        WalletFile wallet = Wallet.createStandardWallet();
        wallet.addAccount(account);

        ECKeyPair ecKeyPair = Wallet.decryptStandard("TestingOneTwoThree", wallet, account);

        assertEquals("L44B5gGEpqEDRS9vVPz7QT35jcBG2r3CZwSwQ4fCewXAhAhqGVpP", Credentials.create(ecKeyPair).exportAsWIF());
    }

    @Test
    public void testDecryptStandard2() throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {

        WalletFile.Account account = new WalletFile.Account(
                "AXoxAX2eJfJ1shNpWqUxRh3RWNUJqvQvVa",
                "",
                true,
                false,
                "6PYN6mjwYfjPUuYT3Exajvx25UddFVLpCw4bMsmtLdnKwZ9t1Mi3CfKe8S",
                null,
                null
        );

        WalletFile wallet = Wallet.createStandardWallet();
        wallet.addAccount(account);

        ECKeyPair ecKeyPair = Wallet.decryptStandard("Satoshi", wallet, account);

        assertEquals("KwYgW8gcxj1JWJXhPSu4Fqwzfhp5Yfi42mdYmMa4XqK7NJxXUSK7", Credentials.create(ecKeyPair).exportAsWIF());
    }

    @Test
    public void testDecryptStandard3() throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {

        WalletFile.Account account = new WalletFile.Account(
                "AdGPiWRqqoFMauM6anTNFB7MyBwQhEANyZ",
                "",
                true,
                false,
                "6PYUNvLELtv66vFYgmHuu11je7h4hTZiLTVbRk4RNvJZo75PurR6z7JnoX",
                null,
                null
        );

        WalletFile wallet = Wallet.createStandardWallet();
        wallet.addAccount(account);

        ECKeyPair ecKeyPair = Wallet.decryptStandard("q1w2e3!@#", wallet, account);

        assertEquals("L5fE7aDEiBLJwcf3Zr9NrUUuT9Rd8nc4kPkuJWqNhftdmx3xcyAd", Credentials.create(ecKeyPair).exportAsWIF());
    }


    // AXoxAX2eJfJ1shNpWqUxRh3RWNUJqvQvVa

//    @Test
//    public void testCreateStandard() throws Exception {
//        testCreate(Wallet.createStandard(SampleKeys.PASSWORD_1, SampleKeys.KEY_PAIR_1));
//    }
//
//    @Test
//    public void testCreateLight() throws Exception {
//        testCreate(Wallet.createLight(SampleKeys.PASSWORD_1, SampleKeys.KEY_PAIR_1));
//    }
//
//    @Test
//    public void testEncryptDecryptStandard() throws Exception {
//        testEncryptDecrypt(Wallet.createStandard(SampleKeys.PASSWORD_1, SampleKeys.KEY_PAIR_1));
//    }
//
//    @Test
//    public void testEncryptDecryptLight() throws Exception {
//        testEncryptDecrypt(Wallet.createLight(SampleKeys.PASSWORD_1, SampleKeys.KEY_PAIR_1));
//    }

    private void testCreate(WalletFile walletFile) throws Exception {
        assertThat(walletFile.getAccounts().stream().findFirst().get(), is(SampleKeys.ADDRESS_1));
    }

    @Test
    public void testGenerateRandomBytes() {
        assertThat(Wallet.generateRandomBytes(0), is(new byte[]{}));
        assertThat(Wallet.generateRandomBytes(10).length, is(10));
    }

//    private void testEncryptDecrypt(WalletFile walletFile) throws Exception {
//        assertThat(Wallet.decrypt(SampleKeys.PASSWORD_1, walletFile), equalTo(SampleKeys.KEY_PAIR_1));
//    }

    private WalletFile load(String source) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(source, WalletFile.class);
    }

}
