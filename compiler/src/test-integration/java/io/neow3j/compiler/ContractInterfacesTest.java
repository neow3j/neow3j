package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.ContractInterface;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContractInterfacesTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ContractInterfacesTestContract.class.getName());
    }

    @Test
    public void callSymbolMethodOfCustomNeoContractInterface() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("neo"));
    }

    @Test
    public void getScriptHashOfCustomNeoContractInterface() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is("de5f57d430d3dece511cf975a8d37848cb9e0525"));
    }

    static class ContractInterfacesTestContract {

        public static String callSymbolMethodOfCustomNeoContractInterface() {
            return TheNeoToken.symbol();
        }

        public static byte[] getScriptHashOfCustomNeoContractInterface() {
            return TheNeoToken.getHash();
        }

        @ContractHash("0xde5f57d430d3dece511cf975a8d37848cb9e0525") // NEO script hash
        static class TheNeoToken extends ContractInterface {

            public static native String symbol();
        }
    }
}
