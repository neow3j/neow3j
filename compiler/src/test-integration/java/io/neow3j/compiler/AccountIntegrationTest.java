package io.neow3j.compiler;

import io.neow3j.devpack.Account;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static io.neow3j.TestProperties.committeeAccountAddress;
import static io.neow3j.TestProperties.defaultAccountAddress;
import static io.neow3j.TestProperties.defaultAccountPublicKey;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AccountIntegrationTest {

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            AccountIntegrationTestContract.class.getName());

    @Test
    public void createStandardAccount() throws IOException {
        NeoInvokeFunction res = ct.callInvokeFunction("createStandardAccount",
                publicKey(defaultAccountPublicKey()));
        assertThat(res.getInvocationResult().getStack().get(0).getAddress(),
                is(defaultAccountAddress()));
    }

    @Test
    public void createMultiSigAccount() throws IOException {
        NeoInvokeFunction res = ct.callInvokeFunction("createMultiSigAccount",
                integer(1), array(publicKey(defaultAccountPublicKey())));
        assertThat(res.getInvocationResult().getStack().get(0).getAddress(),
                is(committeeAccountAddress()));
    }

    static class AccountIntegrationTestContract {

        public static Hash160 createStandardAccount(ECPoint pubKey) {
            return Account.createStandardAccount(pubKey);
        }

        public static Hash160 createMultiSigAccount(int m, ECPoint[] pubKeys) {
            return Account.createMultiSigAccount(m, pubKeys);
        }

    }
}
