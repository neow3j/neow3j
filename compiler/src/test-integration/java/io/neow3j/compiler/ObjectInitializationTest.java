package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.Helper;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ObjectInitializationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ObjectInitialization.class.getName());
    }

    @Test
    public void initializeCustomClass() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.string("Neo"), // 0x4e656f
                ContractParameter.integer(1));

        assertThat(response.getInvocationResult().getStack().get(0).asBuffer().getValue(),
                is(new byte[]{(byte) 0x4e, (byte) 0x65, (byte) 0x6f, 0x01}));
    }

    static class ObjectInitialization {

        public static byte[] initializeCustomClass(String s, int i) {
            MyClass c = new MyClass(s, i);
            return Helper.concat(Helper.toByteArray(c.s), Helper.toByteArray(Helper.asByte(c.i)));
        }
    }

    static class MyClass {

        public String s;
        public int i;

        public MyClass(String s, int i) {
            this.s = s;
            this.i = i;
        }
    }
}

