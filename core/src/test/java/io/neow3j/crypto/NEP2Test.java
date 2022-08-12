package io.neow3j.crypto;

import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.Test;

import static io.neow3j.test.TestProperties.defaultAccountEncryptedPrivateKey;
import static io.neow3j.test.TestProperties.defaultAccountPassword;
import static io.neow3j.test.TestProperties.defaultAccountPrivateKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NEP2Test {

    @Test
    public void decryptWithDefaultScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        ECKeyPair pair = NEP2.decrypt(defaultAccountPassword(),
                defaultAccountEncryptedPrivateKey());
        assertThat(pair.getPrivateKey().getBytes(),
                is(Numeric.hexStringToByteArray(defaultAccountPrivateKey())));
    }

    @Test
    public void decryptWithNonDefaultScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        String nep2Encrypted = "6PYM7jHL3uwhP8uuHP9fMGMfJxfyQbanUZPQEh1772iyb7vRnUkbkZmdRT";
        ECKeyPair pair = NEP2.decrypt(defaultAccountPassword(), nep2Encrypted,
                nonDefaultScryptParams);
        assertThat(pair.getPrivateKey().getBytes(),
                is(Numeric.hexStringToByteArray(defaultAccountPrivateKey())));
    }

    @Test
    public void encryptWithDefaultScryptParams() throws CipherException {
        ECKeyPair keyPair = ECKeyPair.create(
                Numeric.hexStringToByteArray(defaultAccountPrivateKey()));
        assertThat(NEP2.encrypt(defaultAccountPassword(), keyPair),
                is(defaultAccountEncryptedPrivateKey()));
    }

    @Test
    public void encryptWithNonDefaultScryptParams() throws CipherException {
        ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);
        String expected = "6PYM7jHL3uwhP8uuHP9fMGMfJxfyQbanUZPQEh1772iyb7vRnUkbkZmdRT";
        ECKeyPair keyPair =
                ECKeyPair.create(Numeric.hexStringToByteArray(defaultAccountPrivateKey()));
        assertThat(NEP2.encrypt(defaultAccountPassword(), keyPair, nonDefaultScryptParams),
                is(expected));
    }

}