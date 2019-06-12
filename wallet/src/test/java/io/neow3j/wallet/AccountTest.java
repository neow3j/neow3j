package io.neow3j.wallet;

import io.neow3j.crypto.Credentials;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Keys;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.WIF;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.wallet.nep6.NEP6Account;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccountTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testBuildGenericAccount() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Account a = Account.with().freshKeyPair().build();
        assertNotNull(a.getCredentials());
        assertNotNull(a.getCredentials());
        // TODO Claude 11.06.19 Implement
    }

    @Test
    public void testBuildAccountFromKeyPair() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        Credentials cred = new Credentials(ecKeyPair);
        Account a = Account.with().ecKeyPair(ecKeyPair).build();
        assertEquals(cred, a.getCredentials());
        // TODO Claude 11.06.19 Implement
    }

    @Test
    public void testBuildAccountFailing1() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        exceptionRule.expect(IllegalStateException.class);
        Account a = Account.with().ecKeyPair(ecKeyPair).nep6Account(new NEP6Account()).build();
    }

    @Test
    public void testBuildAccountFailing2() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        exceptionRule.expect(IllegalStateException.class);
        Account a = Account.with().nep6Account(new NEP6Account()).ecKeyPair(ecKeyPair).build();
    }

    @Test
    public void testCreateVerificationScriptContract() {
        // TODO Claude 11.06.19: Implement
    }

    @Test
    public void testCreateStandardAccount1() throws CipherException {

        byte[] privateKey = WIF.getPrivateKeyFromWIF("L44B5gGEpqEDRS9vVPz7QT35jcBG2r3CZwSwQ4fCewXAhAhqGVpP");
        ECKeyPair keyPair = ECKeyPair.create(privateKey);

        Account account = Account.with().ecKeyPair(keyPair).build();
        account.encryptPrivateKey("TestingOneTwoThree", NEP2.DEFAULT_SCRYPT_PARAMS);
        assertEquals("6PYVPVe1fQznphjbUxXP9KZJqPMVnVwCx5s5pr5axRJ8uHkMtZg97eT5kL", account.getEncryptedPrivateKey());
    }

    @Test
    public void testCreateStandardAccount2() throws CipherException {

        byte[] privateKey = WIF.getPrivateKeyFromWIF("KwYgW8gcxj1JWJXhPSu4Fqwzfhp5Yfi42mdYmMa4XqK7NJxXUSK7");
        ECKeyPair keyPair = ECKeyPair.create(privateKey);

        Account account = Account.with().ecKeyPair(keyPair).build();
        account.encryptPrivateKey("Satoshi", NEP2.DEFAULT_SCRYPT_PARAMS);
        assertEquals("6PYN6mjwYfjPUuYT3Exajvx25UddFVLpCw4bMsmtLdnKwZ9t1Mi3CfKe8S", account.getEncryptedPrivateKey());
    }

    @Test
    public void testDecryptStandard1() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        NEP6Account nep6Acct = new NEP6Account(
                "AStZHy8E6StCqYQbzMqi4poH7YNDHQKxvt",
                "",
                true,
                false,
                "6PYVPVe1fQznphjbUxXP9KZJqPMVnVwCx5s5pr5axRJ8uHkMtZg97eT5kL",
                null,
                null
        );

        Account a = Account.with().nep6Account(nep6Acct).build();
        Wallet w = Wallet.with().defaultValues().account(a).build();
        w.decryptAllAccounts("TestingOneTwoThree");
        assertEquals("L44B5gGEpqEDRS9vVPz7QT35jcBG2r3CZwSwQ4fCewXAhAhqGVpP", w.getAccounts().get(0).getEcKeyPair().exportAsWIF());
    }

    @Test
    public void testDecryptStandard2() throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {

        NEP6Account nep6Acct = new NEP6Account(
                "AXoxAX2eJfJ1shNpWqUxRh3RWNUJqvQvVa",
                "",
                true,
                false,
                "6PYN6mjwYfjPUuYT3Exajvx25UddFVLpCw4bMsmtLdnKwZ9t1Mi3CfKe8S",
                null,
                null
        );

        Account a = Account.with().nep6Account(nep6Acct).build();
        Wallet w = Wallet.with().defaultValues().account(a).build();
        w.decryptAllAccounts("Satoshi");
        assertEquals("KwYgW8gcxj1JWJXhPSu4Fqwzfhp5Yfi42mdYmMa4XqK7NJxXUSK7", w.getAccounts().get(0).getEcKeyPair().exportAsWIF());
    }

    @Test
    public void testDecryptStandard3() throws CipherException, NEP2InvalidFormat, NEP2InvalidPassphrase {

        NEP6Account nep6Acct = new NEP6Account(
                "AdGPiWRqqoFMauM6anTNFB7MyBwQhEANyZ",
                "",
                true,
                false,
                "6PYUNvLELtv66vFYgmHuu11je7h4hTZiLTVbRk4RNvJZo75PurR6z7JnoX",
                null,
                null
        );

        Account a = Account.with().nep6Account(nep6Acct).build();
        Wallet w = Wallet.with().defaultValues().account(a).build();
        w.decryptAllAccounts("q1w2e3!@#");
        assertEquals("L5fE7aDEiBLJwcf3Zr9NrUUuT9Rd8nc4kPkuJWqNhftdmx3xcyAd", w.getAccounts().get(0).getEcKeyPair().exportAsWIF());
    }
}
