package io.neow3j.wallet;

import io.neow3j.crypto.Credentials;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.WIF;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WalletTest {

    @Test
    public void testCreateStandardAccount1() throws CipherException {

        byte[] privateKey = WIF.getPrivateKeyFromWIF("L44B5gGEpqEDRS9vVPz7QT35jcBG2r3CZwSwQ4fCewXAhAhqGVpP");
        ECKeyPair keyPair = ECKeyPair.create(privateKey);

        Account account = Wallet.createStandardAccount("TestingOneTwoThree", keyPair);

        assertEquals("6PYVPVe1fQznphjbUxXP9KZJqPMVnVwCx5s5pr5axRJ8uHkMtZg97eT5kL", account.getKey());
    }

    @Test
    public void testCreateStandardAccount2() throws CipherException {

        byte[] privateKey = WIF.getPrivateKeyFromWIF("KwYgW8gcxj1JWJXhPSu4Fqwzfhp5Yfi42mdYmMa4XqK7NJxXUSK7");
        ECKeyPair keyPair = ECKeyPair.create(privateKey);

        Account account = Wallet.createStandardAccount("Satoshi", keyPair);

        assertEquals("6PYN6mjwYfjPUuYT3Exajvx25UddFVLpCw4bMsmtLdnKwZ9t1Mi3CfKe8S", account.getKey());
    }

    @Test
    public void testDecryptStandard1() throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {

        Account account = new Account(
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

        Account account = new Account(
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

        Account account = new Account(
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

    @Test
    public void testGenerateRandomBytes() {
        assertThat(Wallet.generateRandomBytes(0), is(new byte[]{}));
        assertThat(Wallet.generateRandomBytes(10).length, is(10));
    }

}
