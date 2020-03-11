package io.neow3j.crypto;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.utils.Numeric;
import org.junit.Test;

public class NEP2Test {

    @Test
    public void decryptWithDefaultScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        // Used neo-core with address version 0x17 to generate the encrypted key.
        String nep2Encrypted = "6PYX9GMW3WgtYcivcWgrqzk2igqY8jhnMcysgFw4npoLqRnxZ16yj8V6V1";
        String password = "pwd";
        // WIF created from private key
        // 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        ECKeyPair pair = NEP2.decrypt(password, nep2Encrypted);
        assertThat(pair.exportAsWIF(), is(wif));
    }

    @Test
    public void decryptWithNonDefaultScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        // Used neo-core with address version 0x17 to generate the encrypted key.
        String nep2Encrypted = "6PYX9GMW4X3KpzPss9CsQsTS4g8mvm2HuKtFNbW67jrG8CE7t5jEX8yVwA";
        String password = "pwd";
        // WIF created from private key
        // 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";

        ECKeyPair pair = NEP2.decrypt(password, nep2Encrypted, nonDefaultScryptParams);
        assertThat(pair.exportAsWIF(), is(wif));
    }

    @Test
    public void encryptWithDefaultScryptParams() throws CipherException {
        byte[] privKey = Numeric.hexStringToByteArray(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");
        String password = "pwd";
        // Used neo-core with address version 0x17 to generate the encrypted key.
        String expectedNep2Encrypted = "6PYX9GMW3WgtYcivcWgrqzk2igqY8jhnMcysgFw4npoLqRnxZ16yj8V6V1";
        ECKeyPair keyPair = ECKeyPair.create(privKey);
        assertThat(NEP2.encrypt(password, keyPair), is(expectedNep2Encrypted));
    }

    @Test
    public void encryptWithNonDefaultScryptParams() throws CipherException {
        ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        // Used neo-core with address version 0x17 to generate the encrypted key.
        byte[] privKey = Numeric.hexStringToByteArray(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");
        String pw = "pwd";
        String expectedNep2Encrypted = "6PYX9GMW4X3KpzPss9CsQsTS4g8mvm2HuKtFNbW67jrG8CE7t5jEX8yVwA";
        ECKeyPair keyPair = ECKeyPair.create(privKey);
        assertThat(NEP2.encrypt(pw, keyPair, nonDefaultScryptParams), is(expectedNep2Encrypted));
    }

}