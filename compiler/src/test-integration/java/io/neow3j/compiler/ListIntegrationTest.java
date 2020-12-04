package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.List;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ListIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ListTestContract.class.getName());
    }

    @Test
    public void createList() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asArray().size(), is(0));
    }

    @Test
    public void createListFromByteArray() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.byteArray(new byte[]{0x00, 0x01, 0x02, 0x03}));
        assertThat(response.getInvocationResult().getStack().get(0).asArray().size(), is(0));
    }

    @Test
    public void createListAndAddElements() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asArray().size(), is(0));
    }

    static class ListTestContract {

        public static List<Byte> createList() {
            return new List<>();
        }

        public static byte[] createListFromByteArray(byte[] bytes) {
            return bytes;
        }

    }

}
