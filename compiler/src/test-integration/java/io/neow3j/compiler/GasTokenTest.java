package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.GasToken;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import org.junit.BeforeClass;
import org.junit.Test;

public class GasTokenTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(GasTokenTestContract.class.getName());
    }

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


