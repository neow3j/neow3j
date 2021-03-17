package io.neow3j.compiler;

import io.neow3j.compiler.utils.ContractTestRule;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.GasToken;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GasTokenTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            GasTokenTestContract.class.getName());

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(io.neow3j.contract.GasToken.SCRIPT_HASH.toString()));
    }

    static class GasTokenTestContract {

        public static Hash160 getHash() {
            return GasToken.getHash();
        }
    }

}


