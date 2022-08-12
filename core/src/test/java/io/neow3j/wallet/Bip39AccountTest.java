package io.neow3j.wallet;

import io.neow3j.crypto.ECKeyPair;
import org.junit.jupiter.api.Test;

import static io.neow3j.wallet.Bip39Account.fromBip39Mnemonic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Bip39AccountTest {

    @Test
    public void testGenerateAndRecoverBip39Account() throws Exception {

        final String pw = "Insecure Pa55w0rd";

        Bip39Account a1 = Bip39Account.create(pw);

        Bip39Account a2 = fromBip39Mnemonic(pw, a1.getMnemonic());

        assertThat(a1.getAddress(), is(a2.getAddress()));
        assertThat(a1.getECKeyPair(), is(a2.getECKeyPair()));
        assertThat(a1.getMnemonic(), is(a2.getMnemonic()));
        assertThat(a1.getMnemonic().length(), greaterThan(0));
    }

    @Test
    public void testBuildBip39AccountFromKeyPair() throws Exception {
        ECKeyPair ecKeyPair = ECKeyPair.createEcKeyPair();
        Bip39Account a = new Bip39Account(ecKeyPair);
        assertEquals(ecKeyPair, a.getECKeyPair());
    }

}
