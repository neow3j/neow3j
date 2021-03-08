package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.BeforeClass;
import org.junit.Test;

public class ObjectLengthTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(Lengths.class.getName());
    }

    @Test
    public void lengthOfTwoStrings() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.string("one"),
                ContractParameter.string("two"));

        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(6));
    }

    @Test
    public void lengthOfTwoArrays() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.byteArray(Numeric.hexStringToByteArray("01020304")),
                ContractParameter.byteArray(new byte[100000]));

        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(100004));
    }

    @Test
    public void lengthComparison() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(array(string("hello"), string("world")),
                integer(2));

        assertThat(response.getInvocationResult().getStack().get(0).getInteger(),
                is(BigInteger.ONE));
    }

    static class Lengths {

        public static int lengthOfTwoStrings(String s1, String s2) {
            return s1.length() + s2.length();
        }

        public static int lengthOfTwoArrays(byte[] b, int[] i) {
            return b.length + i.length;
        }

        public static boolean lengthComparison(String[] s, int i) {
            return s.length == i;
        }

    }
}

