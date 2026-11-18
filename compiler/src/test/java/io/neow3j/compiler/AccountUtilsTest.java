package io.neow3j.compiler;

import io.neow3j.devpack.AccountUtils;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class AccountUtilsTest {

    @Test
    public void accountUtilsCompilesToSameScriptAsLegacyAccount() throws IOException {
        byte[] legacyScript = new Compiler().compile(LegacyAccountContract.class.getName()).getNefFile().getScript();
        byte[] accountUtilsScript = new Compiler().compile(AccountUtilsContract.class.getName()).getNefFile()
                .getScript();

        assertArrayEquals(legacyScript, accountUtilsScript);
    }

    @SuppressWarnings("deprecation")
    static class LegacyAccountContract {

        public static Hash160 standardAccountScriptHash(ECPoint pubKey) {
            return io.neow3j.devpack.Account.createStandardAccount(pubKey);
        }

        public static Hash160 multiSigAccountScriptHash(int m, ECPoint[] pubKeys) {
            return io.neow3j.devpack.Account.createMultiSigAccount(m, pubKeys);
        }

    }

    static class AccountUtilsContract {

        public static Hash160 standardAccountScriptHash(ECPoint pubKey) {
            return AccountUtils.deriveStandardAccountScriptHash(pubKey);
        }

        public static Hash160 multiSigAccountScriptHash(int m, ECPoint[] pubKeys) {
            return AccountUtils.deriveMultiSigAccountScriptHash(m, pubKeys);
        }

    }

}
