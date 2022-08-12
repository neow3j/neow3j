package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.GasToken;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.neow3j.test.TestProperties.gasTokenHash;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GasTokenIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(GasTokenIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

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
