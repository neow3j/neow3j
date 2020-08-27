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

        String nep2Encrypted = "6PYSAe53CzgXq9yg7hmHQogZHxasVY2gYGB9rLXNbdggFyy2HBDdJhXZSp";
        String password = "neo";
        ECKeyPair pair = NEP2.decrypt(password, nep2Encrypted);
        String expected = "a7038726c5a127989d78593c423e3dad93b2d74db90a16c0a58468c9e6617a87";
        assertThat(pair.getPrivateKey().getBytes(), is(Numeric.hexStringToByteArray(expected)));
    }

    @Test
    public void decryptWithNonDefaultScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        String nep2Encrypted = "6PYSAe53E2MXoxZpr3Fmqtjf7FT3BBQU1pS55iYW1hrjU6xhXXHhYnTjkn";
        String password = "neo";
        ECKeyPair pair = NEP2.decrypt(password, nep2Encrypted, nonDefaultScryptParams);
        String expected = "a7038726c5a127989d78593c423e3dad93b2d74db90a16c0a58468c9e6617a87";
        assertThat(pair.getPrivateKey().getBytes(), is(Numeric.hexStringToByteArray(expected)));
    }

    @Test
    public void encryptWithDefaultScryptParams() throws CipherException {
        byte[] privKey = Numeric.hexStringToByteArray(
                "7fe9d4b69f85c1fe15387a76e79d2b95c4c9e3fe756de3435afbc077d99d5346");
        String pw = "neo";
        String expectedNep2Encrypted = "6PYLykbKcbwnCuTJiQQ5PYu5uH9NgwGYLoMyTUabRxRJUsiA9GP8NgorUV";
        ECKeyPair keyPair = ECKeyPair.create(privKey);
        assertThat(NEP2.encrypt(pw, keyPair), is(expectedNep2Encrypted));
    }

    @Test
    public void encryptWithNonDefaultScryptParams() throws CipherException {
        ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        byte[] privKey = Numeric.hexStringToByteArray(
                "7fe9d4b69f85c1fe15387a76e79d2b95c4c9e3fe756de3435afbc077d99d5346");
        String pw = "neo";
        String expected = "6PYLykbKbcpcZTuaSyTqBmauUkwVN4cQHjQAufT8y5YE6vCP44nxtf9r3P";
        ECKeyPair keyPair = ECKeyPair.create(privKey);
        assertThat(NEP2.encrypt(pw, keyPair, nonDefaultScryptParams), is(expected));
    }

}