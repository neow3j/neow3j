package io.neow3j.compiler;

import io.neow3j.contract.NeoToken;
import io.neow3j.devpack.Contract;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.constants.CallFlags;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ContractIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(ContractIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void callWithoutArguments() throws IOException {
        NeoInvokeFunction resp = ct.callInvokeFunction("call",
                hash160(NeoToken.SCRIPT_HASH), string("symbol"), integer(CallFlags.All));
        assertThat(resp.getInvocationResult().getStack().get(0).getString(), is("NEO"));
    }

    @Test
    public void callWithArgument() throws IOException {
        NeoInvokeFunction resp =
                ct.callInvokeFunction("call", hash160(NeoToken.SCRIPT_HASH), string("balanceOf"),
                        integer(CallFlags.All), hash160(ct.getCommittee().getScriptHash()));
        assertThat(resp.getInvocationResult().getStack().get(0).getInteger().intValue(), is(100_000_000));
    }

    @Test
    public void getCallFlags() throws IOException {
        NeoInvokeFunction resp = ct.callInvokeFunction(testName);
        assertThat(resp.getInvocationResult().getStack().get(0).getInteger().intValue(), is(15)); // CallFlag ALL
    }

    @Test
    public void callGasTokenSymbol() throws IOException {
        NeoInvokeFunction resp = ct.callInvokeFunction(testName);
        assertThat(resp.getInvocationResult().getStack().get(0).getString(), is("GAS"));
    }

    static class ContractIntegrationTestContract {

        public static Object call(Hash160 hash, String method, byte callFlags, Object param) {
            return Contract.call(hash, method, callFlags, new Object[]{param});
        }

        public static Object call(Hash160 hash, String method, byte callFlags) {
            return Contract.call(hash, method, callFlags, new Object[0]);
        }

        public static Object callGasTokenSymbol() {
            Hash160 contractHash = new Hash160(StringLiteralHelper.hexToBytes(
                    "cf76e28bd0062c4a478ee35561011319f3cfa4d2")); // little-endian GAS hash
            return Contract.call(contractHash, "symbol", CallFlags.All, new Object[]{});
        }

        public static byte getCallFlags() {
            return Contract.getCallFlags();
        }

    }

}
