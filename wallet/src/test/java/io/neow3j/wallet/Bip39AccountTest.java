package io.neow3j.wallet;

import io.neow3j.crypto.Credentials;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Hash;
import io.neow3j.crypto.MnemonicUtils;
import io.neow3j.wallet.nep6.NEP6Wallet;
import org.junit.Test;

import java.io.File;

import static io.neow3j.wallet.Bip39Account.fromBip39Mnemonic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class Bip39AccountTest extends BaseTest {

    @Test
    public void testGenerateBip39Account() throws Exception {

        Bip39Account a1 = Bip39Account.createBip39Account(SampleKeys.PASSWORD_1);
        String mnemonic = a1.getMnemonic();

        Bip39Account.Builder builder = fromBip39Mnemonic(SampleKeys.PASSWORD_1, mnemonic);
        Bip39Account a2 = builder.build();

        assertThat(a1.getAddress(), is(a2.getAddress()));
        assertThat(a1.getECKeyPair(), is(a2.getECKeyPair()));
    }

}
