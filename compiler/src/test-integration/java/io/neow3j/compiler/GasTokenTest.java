package io.neow3j.compiler;

import io.neow3j.compiler.utils.ContractCompilationTestRule;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.GasToken;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GasTokenTest extends ContractTest {

    @ClassRule
    public static ContractCompilationTestRule c = new ContractCompilationTestRule(
            GasTokenTestContract.class.getName());

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(io.neow3j.contract.GasToken.SCRIPT_HASH.toString()));
    }

    static class GasTokenTestContract {

        public static Hash160 getHash() {
            return GasToken.getHash();
        }
    }

}


