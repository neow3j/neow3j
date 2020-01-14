package io.neow3j.wallet;

import static io.neow3j.wallet.Bip39Account.fromBip39Mnemonic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import io.neow3j.crypto.ECKeyPair;
import org.junit.Test;

public class Bip39AccountTest {

    @Test
    public void testGenerateAndRecoverBip39Account() throws Exception {

        Bip39Account a1 = Bip39Account.createAccount(SampleKeys.PASSWORD_1);

        Bip39Account a2 = fromBip39Mnemonic(SampleKeys.PASSWORD_1, a1.getMnemonic()).build();

        assertThat(a1.getAddress(), is(a2.getAddress()));
        assertThat(a1.getECKeyPair(), is(a2.getECKeyPair()));
        assertThat(a1.getMnemonic(), notNullValue());
        assertThat(a1.getMnemonic().length(), greaterThan(0));
    }

    @Test
    public void testBuildBip39AccountFromKeyPair() throws Exception {
        ECKeyPair ecKeyPair = ECKeyPair.createEcKeyPair();
        Bip39Account a = Bip39Account.fromECKeyPair(ecKeyPair).build();
        assertEquals(ecKeyPair, a.getECKeyPair());
    }

}
