package io.neow3j.compiler;

import io.neow3j.compiler.utils.ContractTestRule;
import io.neow3j.contract.ContractParameter;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ObjectLengthTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            ObjectLengthTestContract.class.getName());

    @Test
    public void lengthOfTwoStrings() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.string("one"),
                ContractParameter.string("two"));

        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(6));
    }

    @Test
    public void lengthOfTwoArrays() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.byteArray(Numeric.hexStringToByteArray("01020304")),
                ContractParameter.byteArray(new byte[100000]));

        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(100004));
    }

    @Test
    public void lengthComparison() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, array(string("hello"),
                string("world")), integer(2));

        assertThat(response.getInvocationResult().getStack().get(0).getInteger(),
                is(BigInteger.ONE));
    }

    static class ObjectLengthTestContract {

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

