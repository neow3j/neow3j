package io.neow3j.compiler;

import io.neow3j.devpack.contracts.ContractInterface;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static io.neow3j.test.TestProperties.neoTokenHash;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ContractInterfacesIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            ContractInterfacesIntegrationTestContract.class.getName());

    @Test
    public void callSymbolMethodOfCustomNeoContractInterface() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("NEO"));
    }

    @Test
    public void getScriptHashOfCustomNeoContractInterface() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(neoTokenHash())));
    }

    static class ContractInterfacesIntegrationTestContract {

        public static String callSymbolMethodOfCustomNeoContractInterface() {
            return CustomNeoToken.symbol();
        }

        public static Hash160 getScriptHashOfCustomNeoContractInterface() {
            return CustomNeoToken.getHash();
        }

    }

    @ContractHash("ef4073a0f2b305a38ec4050e4d3d28bc40ea63f5") // NEO script hash
    static class CustomNeoToken extends ContractInterface {

        public static native String symbol();
    }
}
