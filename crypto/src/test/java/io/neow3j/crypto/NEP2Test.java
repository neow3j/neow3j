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

        String nep2Encrypted = "6PYMGfNyeJAf8bLXmPh8MbJxLB8uvQqtnZje1RUhhUcDDucj55dZsvbk8k";
        String password = "pwd";
        ECKeyPair pair = NEP2.decrypt(password, nep2Encrypted);
        String expected = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f";
        assertThat(pair.getPrivateKey().getBytes(), is(Numeric.hexStringToByteArray(expected)));
    }

    @Test
    public void decryptWithNonDefaultScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        String nep2Encrypted = "6PYMGfNyeTuzqgST1hwqKtHo8EKCQGD2vG4gihSg6EskG9DMDE3x1Xd4si";
        String password = "pwd";
        ECKeyPair pair = NEP2.decrypt(password, nep2Encrypted, nonDefaultScryptParams);
        String expected = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f";
        assertThat(pair.getPrivateKey().getBytes(), is(Numeric.hexStringToByteArray(expected)));
    }

    @Test
    public void encryptWithDefaultScryptParams() throws CipherException {
        byte[] privKey = Numeric.hexStringToByteArray(
                "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3");
        String pw = "neo";
        String expectedNep2Encrypted = "6PYV39zSDnpCb9ecybeL3z6XrLTpKy1AugUGd6DYFFNELHv9aLj6M7KGD2";
        ECKeyPair keyPair = ECKeyPair.create(privKey);
        assertThat(NEP2.encrypt(pw, keyPair), is(expectedNep2Encrypted));
    }

    @Test
    public void encryptWithNonDefaultScryptParams() throws CipherException {
        ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        byte[] privKey = Numeric.hexStringToByteArray(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");
        String pw = "pwd";
        String expected = "6PYMGfNyeTuzqgST1hwqKtHo8EKCQGD2vG4gihSg6EskG9DMDE3x1Xd4si";
        ECKeyPair keyPair = ECKeyPair.create(privKey);
        assertThat(NEP2.encrypt(pw, keyPair, nonDefaultScryptParams), is(expected));
    }

    @Test
    public void test() throws NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {
        System.out.println(WIF.privateKeyToWIF(Numeric.hexStringToByteArray(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f")));
        ECKeyPair keyPair = NEP2
                .decrypt("neo", "6PYV39zSDnpCb9ecybeL3z6XrLTpKy1AugUGd6DYFFNELHv9aLj6M7KGD2");
        System.out.println(Numeric.toHexStringNoPrefix(keyPair.getPrivateKey().getBytes()));
        System.out.println(WIF.privateKeyToWIF(keyPair.getPrivateKey().getBytes()));
    }

}