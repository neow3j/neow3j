package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.contracts.NeoToken;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import org.junit.BeforeClass;
import org.junit.Test;

public class NeoTokenTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(NeoTokenTestContract.class.getName());
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(io.neow3j.contract.NeoToken.SCRIPT_HASH.toString()));
    }

    static class NeoTokenTestContract {

        public static byte[] getHash() {
            return NeoToken.getHash();
        }

    }

}


