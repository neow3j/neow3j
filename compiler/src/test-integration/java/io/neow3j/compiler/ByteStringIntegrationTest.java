package io.neow3j.compiler;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.ByteString;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ByteStringIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            ByteStringIntegrationTest.ByteStringIntegrationTestContract.class.getName());

    @Test
    public void createByteStringFromString() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is("hello, world!"));
    }

    @Test
    public void createByteStringFromByteArray() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x00, 0x01, 0x02, 0x03}));
    }

    @Test
    public void getElementsOfByteString() throws IOException {
        ContractParameter byteString = byteArray("00010203");
        InvocationResult res = ct.callInvokeFunction(testName, byteString, integer(0))
                .getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(0));

        res = ct.callInvokeFunction(testName, byteString, integer(3))
                .getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(3));
    }

    @Test
    public void getByteStringLength() throws IOException {
        ContractParameter byteString = byteArray("00010203");
        InvocationResult res = ct.callInvokeFunction(testName, byteString).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(4));

        byteString = string("hello, world!");
        res = ct.callInvokeFunction(testName, byteString).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(13));
    }

    @Test
    public void byteStringToInteger() throws IOException {
        ContractParameter byteString = byteArray("00010203");
        InvocationResult res = ct.callInvokeFunction(testName, byteString).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(50462976));
    }

    @Test
    public void byteStringToByteArray() throws IOException {
        ContractParameter byteString = string("hello, world!");
        InvocationResult res = ct.callInvokeFunction(testName, byteString).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is("68656c6c6f2c20776f726c6421"));
    }

    @Test
    public void byteStringAsString() throws IOException {
        ContractParameter byteString = string("hello, world!");
        InvocationResult res = ct.callInvokeFunction(testName, byteString).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is("hello, world!"));
    }

    static class ByteStringIntegrationTestContract {

        public static ByteString createByteStringFromString() {
            return new ByteString("hello, world!");
        }

        public static ByteString createByteStringFromByteArray() {
            return new ByteString(new byte[]{0x00, 0x01, 0x02, 0x03});
        }

        public static byte getElementsOfByteString(ByteString s, int index) {
            return s.get(index);
        }

        public static int getByteStringLength(ByteString s) {
            return s.length();
        }

        public static  String byteStringAsString(ByteString s) {
            return s.asString();
        }

        public static byte[] byteStringToByteArray(ByteString s) {
            return s.toByteArray();
        }

        public static int byteStringToInteger(ByteString s) {
            return s.toInteger();
        }

    }

}
