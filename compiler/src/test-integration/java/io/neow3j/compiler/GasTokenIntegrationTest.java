package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.GasToken;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static io.neow3j.test.TestProperties.gasTokenHash;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GasTokenIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(GasTokenIntegrationTestContract.class.getName());

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(gasTokenHash())));
    }

    static class GasTokenIntegrationTestContract {

        public static Hash160 getHash() {
            return new GasToken().getHash();
        }
    }

}
