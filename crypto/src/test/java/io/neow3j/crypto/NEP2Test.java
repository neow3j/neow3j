package io.neow3j.crypto;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import org.junit.Test;

public class NEP2Test {

    // Test keys and params taken from City of Zion's neon-js test code.
    private final String wif1 = "L1QqQJnpBwbsPGAuutuzPTac8piqvbR1HRjrY5qHup48TBCBFe4g";
    private final String pw1 = "city of zion";
    // Encrypted with the Scrypt params (256, 1, 1)
    private final String encrypted1a = "6PYLPLfpCoGkGvVFeN9KjvvT6dNBoYag3c2co362y9Gge1GSjMewf5J6tc";
    // Encrypted with the default Scrypt params (16384, 8, 8)
    private final String encrypted1b = "6PYLPLfpCw87u1t7TP14gkNweUkuqwpso8qmMt24Kp8aona6K7fvurdsDQ";

    private final String wif2 = "L2QTooFoDFyRFTxmtiVHt5CfsXfVnexdbENGDkkrrgTTryiLsPMG";
    // the string 我的密码 to encoded utf chars
    private final String pw2 = "\u6211\u7684\u5bc6\u7801";
    // Encrypted with the Scrypt params (256, 1, 1)
    private final String encrypted2 = "6PYQqmDYkjqD6D3wsh5YquFqtgWsxThLAZDni1oEXaEp1MTqJKPHzVJEaU";

    private final String wif3 = "KyKvWLZsNwBJx5j9nurHYRwhYfdQUu9tTEDsLCUHDbYBL8cHxMiG";
    private final String pw3 = "MyL33tP@33w0rd";
    // Encrypted with the Scrypt params (256, 1, 1)
    private final String encrypted3 = "6PYQ5fKhgWtqs2y81eBVbt1GsEWx634cRHeJcuknwUW2PyVc9itQqfLhtR";

    ScryptParams nonDefaultScryptParams = new ScryptParams(256, 1, 1);

    @Test
    public void decryptWithDefaultScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        ECKeyPair pair = NEP2.decrypt(pw1, encrypted1b);
        assertThat(pair.exportAsWIF(), is(wif1));
    }

    @Test
    public void decryptWithNonDefaultScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        ECKeyPair pair = NEP2.decrypt(pw1, encrypted1a, nonDefaultScryptParams);
        assertThat(pair.exportAsWIF(), is(wif1));

        pair = NEP2.decrypt(pw2, encrypted2, nonDefaultScryptParams);
        assertThat(pair.exportAsWIF(), is(wif2));

        pair = NEP2.decrypt(pw3, encrypted3, nonDefaultScryptParams);
        assertThat(pair.exportAsWIF(), is(wif3));
    }

    @Test
    public void encryptWithDefaultScryptParams() throws CipherException {
        ECKeyPair keyPair = ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif1));
        assertThat(NEP2.encrypt(pw1, keyPair), is(encrypted1b));
    }

    @Test
    public void encryptWithNonDefaultScryptParams() throws CipherException {
        ECKeyPair keyPair = ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif1));
        assertThat(NEP2.encrypt(pw1, keyPair, nonDefaultScryptParams), is(encrypted1a));

        keyPair = ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif2));
        assertThat(NEP2.encrypt(pw2, keyPair, nonDefaultScryptParams), is(encrypted2));

        keyPair = ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif3));
        assertThat(NEP2.encrypt(pw3, keyPair, nonDefaultScryptParams), is(encrypted3));
    }

}