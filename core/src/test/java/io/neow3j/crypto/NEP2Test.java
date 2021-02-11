package io.neow3j.crypto;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.utils.Numeric;
import org.junit.Test;

// Used neo-core to generate the test data, i.e., NEP2 encrypted private keys. Look at method
// TestGetPrivateKeyFromNEP2() in test class UT_Wallet.cs for a hint on how to create example data
// for this test. Make sure to also set the AddressVersion in ProtocolSettings.cs to match the
// version in neow3j.
public class NEP2Test {

    @Test
    public void decryptWithDefaultScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        String nep2Encrypted = "6PYPzBQFFFdUVyMQahpcL7V139kd4TR8Y11gSF5dCYrxZ3oZhiEBoo3oay";
        String password = "neo";
        ECKeyPair pair = NEP2.decrypt(password, nep2Encrypted);
        String expected = "2b6be36d19b6b69915b4eae3e9a9ee3a06e30bdafc15042cd946f21400f50222";
        assertThat(pair.getPrivateKey().getBytes(), is(Numeric.hexStringToByteArray(expected)));
    }

    @Test
    public void decryptWithNonDefaultScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        String nep2Encrypted = "6PYPzBQFEPbQzsHw3GNK58FoxicDRb5XJ2NmMwjZDSgjXmihfsP3Ne9JBy";
        String password = "neo";
        ECKeyPair pair = NEP2.decrypt(password, nep2Encrypted, nonDefaultScryptParams);
        String expected = "2b6be36d19b6b69915b4eae3e9a9ee3a06e30bdafc15042cd946f21400f50222";
        assertThat(pair.getPrivateKey().getBytes(), is(Numeric.hexStringToByteArray(expected)));
    }

    @Test
    public void encryptWithDefaultScryptParams() throws CipherException {
        byte[] privKey = Numeric.hexStringToByteArray(
                "7fe9d4b69f85c1fe15387a76e79d2b95c4c9e3fe756de3435afbc077d99d5346");
        String pw = "neo";
        String expectedNep2Encrypted = "6PYS1FNdTfvfPH5aMSewcxqixy6hikrwk7P1cEpwu7V6eS4ormwgyhcwZh";
        ECKeyPair keyPair = ECKeyPair.create(privKey);
        assertThat(NEP2.encrypt(pw, keyPair), is(expectedNep2Encrypted));
    }

    @Test
    public void encryptWithNonDefaultScryptParams() throws CipherException {
        ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        byte[] privKey = Numeric.hexStringToByteArray(
                "7fe9d4b69f85c1fe15387a76e79d2b95c4c9e3fe756de3435afbc077d99d5346");
        String pw = "neo";
        String expected = "6PYS1FNdRvKpy6Gvtg5Z8NMpcSgYi4sk1rWzPybgXj3Lg4uRxdP52QFz6T";
        ECKeyPair keyPair = ECKeyPair.create(privKey);
        assertThat(NEP2.encrypt(pw, keyPair, nonDefaultScryptParams), is(expected));
    }

}