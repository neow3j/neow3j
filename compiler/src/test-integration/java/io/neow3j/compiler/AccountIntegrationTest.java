package io.neow3j.compiler;

import static io.neow3j.TestProperties.committeeAccountAddress;
import static io.neow3j.TestProperties.defaultAccountAddress;
import static io.neow3j.TestProperties.defaultAccountPublicKey;
import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.utils.ContractCompilationTestRule;
import io.neow3j.devpack.Account;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class AccountIntegrationTest extends ContractTest {

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(privateNetContainer)
            .around(
                    new ContractCompilationTestRule(
                            AccountIntegrationTestContract.class.getName(),
                            privateNetContainer
                    )
            );

    @Test
    public void createStandardAccount() throws IOException {
        NeoInvokeFunction res = callInvokeFunction(publicKey(defaultAccountPublicKey()));
        assertThat(res.getInvocationResult().getStack().get(0).getAddress(),
                is(defaultAccountAddress()));
    }

    @Test
    public void createMultiSigAccount() throws IOException {
        NeoInvokeFunction res = callInvokeFunction(
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
