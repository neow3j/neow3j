package io.neow3j.compiler;

import io.neow3j.contract.NeoToken;
import io.neow3j.devpack.CallFlags;
import io.neow3j.devpack.Contract;
import io.neow3j.devpack.Hash160;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ContractIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            ContractIntegrationTestContract.class.getName());

    @Test
    public void callWithoutArguments() throws IOException {
        NeoInvokeFunction resp = ct.callInvokeFunction("call",
                hash160(NeoToken.SCRIPT_HASH), string("symbol"), integer(CallFlags.ALL));
        assertThat(resp.getInvocationResult().getStack().get(0).getString(),
                is("NEO"));
    }

    @Test
    public void callWithArgument() throws IOException {
        NeoInvokeFunction resp =
                ct.callInvokeFunction("call", hash160(NeoToken.SCRIPT_HASH), string(
                        "balanceOf"), integer(CallFlags.ALL),
                        hash160(ct.getCommittee().getScriptHash()));
        assertThat(resp.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(100_000_000));
    }

    @Test
    public void getCallFlags() throws IOException {
        NeoInvokeFunction resp = ct.callInvokeFunction(testName);
        assertThat(resp.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(15)); // CallFlag ALL
    }

    static class ContractIntegrationTestContract {

        public static Object call(Hash160 hash, String method, byte callFlags, Object param) {
            return Contract.call(hash, method, callFlags, new Object[]{param});
        }

        public static Object call(Hash160 hash, String method, byte callFlags) {
            return Contract.call(hash, method, callFlags, new Object[0]);
        }

        public static byte getCallFlags() {
            return Contract.getCallFlags();
        }
    }
}
