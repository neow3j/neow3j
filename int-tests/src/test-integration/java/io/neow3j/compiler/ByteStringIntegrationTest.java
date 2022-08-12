package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.types.StackItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.byteArrayFromString;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ByteStringIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            ByteStringIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

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
    public void createByteStringFromInteger() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x64, 0x1b, 0x40}));
    }

    @Test
    public void getElementsOfByteString() throws IOException {
        ContractParameter byteString = byteArray("00010203");
        InvocationResult res = ct.callInvokeFunction(testName, byteString, integer(0)).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(0));

        res = ct.callInvokeFunction(testName, byteString, integer(3)).getInvocationResult();
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
        StackItem item = res.getStack().get(0);
        assertThat(item.getType(), is(StackItemType.INTEGER));
        assertThat(item.getInteger(), is(new BigInteger("50462976")));
    }

    @Test
    public void byteStringToIntegerNull() throws IOException {
        // Test that instructions return null if no value was found for the provided key.
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack(), hasSize(0));
        assertThat(res.getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void byteStringToIntOrZero() throws IOException {
        ContractParameter byteString = byteArray("0001020304");
        InvocationResult res = ct.callInvokeFunction(testName, byteString).getInvocationResult();
        StackItem item = res.getStack().get(0);
        assertThat(item.getType(), is(StackItemType.INTEGER));
        assertThat(item.getInteger(), is(new BigInteger("17230332160")));
    }

    @Test
    public void byteStringToIntOrZeroNull() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        StackItem item = res.getStack().get(0);
        assertThat(item.getType(), is(StackItemType.INTEGER));
        assertThat(item.getInteger(), is(BigInteger.ZERO));
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

    @Test
    public void concatenateByteStrings() throws IOException {
        ContractParameter s1 = string("hello, ");
        ContractParameter s2 = string("world!");
        InvocationResult res = ct.callInvokeFunction(testName, s1, s2).getInvocationResult();
        StackItem item = res.getStack().get(0);
        assertThat(item.getType(), is(StackItemType.BYTE_STRING));
        assertThat(item.getString(), is("hello, world!"));
    }

    @Test
    public void concatenateWithByteArray() throws IOException {
        ContractParameter s = byteArray("00010203");
        InvocationResult res = ct.callInvokeFunction(testName, s).getInvocationResult();
        StackItem item = res.getStack().get(0);
        assertThat(item.getType(), is(StackItemType.BYTE_STRING));
        assertThat(item.getHexString(), is("00010203040506"));
    }

    @Test
    public void concatenateWithString() throws IOException {
        ContractParameter s = byteArrayFromString("hello, ");
        InvocationResult res = ct.callInvokeFunction(testName, s).getInvocationResult();
        StackItem item = res.getStack().get(0);
        assertThat(item.getType(), is(StackItemType.BYTE_STRING));
        assertThat(item.getString(), is("hello, moon!"));
    }

    @Test
    public void concatenateWithInteger() throws IOException {
        ContractParameter s = byteArrayFromString("hello number ");
        InvocationResult res = ct.callInvokeFunction(testName, s).getInvocationResult();
        StackItem item = res.getStack().get(0);
        assertThat(item.getType(), is(StackItemType.BYTE_STRING));
        byte[] concatenated = concatenate("hello number ".getBytes(),
                reverseArray(BigInteger.valueOf(456).toByteArray()));
        assertThat(item.getHexString(), is(toHexStringNoPrefix(concatenated)));
    }

    @Test
    public void getRangeOfByteString() throws IOException {
        ContractParameter s = byteArray("0001020304");
        InvocationResult res = ct.callInvokeFunction(testName, s, integer(2), integer(3)).getInvocationResult();
        StackItem item = res.getStack().get(0);
        assertThat(item.getType(), is(StackItemType.BYTE_STRING));
        assertThat(item.getHexString(), is("020304"));
    }

    @Test
    public void takeNFirstBytesOfByteString() throws IOException {
        ContractParameter s = byteArray("0001020304");
        InvocationResult res = ct.callInvokeFunction(testName, s, integer(2)).getInvocationResult();
        StackItem item = res.getStack().get(0);
        assertThat(item.getType(), is(StackItemType.BYTE_STRING));
        assertThat(item.getHexString(), is("0001"));
    }

    @Test
    public void takeNLastBytesOfByteString() throws IOException {
        ContractParameter s = byteArray("0001020304");
        InvocationResult res = ct.callInvokeFunction(testName, s, integer(2)).getInvocationResult();
        StackItem item = res.getStack().get(0);
        assertThat(item.getType(), is(StackItemType.BYTE_STRING));
        assertThat(item.getHexString(), is("0304"));
    }

    static class ByteStringIntegrationTestContract {

        public static ByteString createByteStringFromString() {
            return new ByteString("hello, world!");
        }

        public static ByteString createByteStringFromByteArray() {
            return new ByteString(new byte[]{0x00, 0x01, 0x02, 0x03});
        }

        public static ByteString createByteStringFromInteger() {
            return new ByteString(4201316);
        }

        public static byte getElementsOfByteString(ByteString s, int index) {
            return s.get(index);
        }

        public static int getByteStringLength(ByteString s) {
            return s.length();
        }

        public static String byteStringAsString(ByteString s) {
            return s.toString();
        }

        public static byte[] byteStringToByteArray(ByteString s) {
            return s.toByteArray();
        }

        public static int byteStringToInteger(ByteString s) {
            return s.toInt();
        }

        public static int byteStringToIntegerNull() {
            ByteString s = null;
            return s.toInt();
        }

        public static int byteStringToIntOrZero(ByteString s) {
            return s.toIntOrZero();
        }

        public static int byteStringToIntOrZeroNull() {
            ByteString s = null;
            return s.toIntOrZero();
        }

        public static ByteString concatenateByteStrings(ByteString s1, ByteString s2) {
            return s1.concat(s2);
        }

        public static ByteString concatenateWithByteArray(ByteString s) {
            byte[] bs = new byte[]{0x04, 0x05, 0x06};
            return s.concat(bs);
        }

        public static ByteString concatenateWithString(ByteString s) {
            return s.concat("moon!");
        }

        public static ByteString concatenateWithInteger(ByteString s) {
            return s.concat(456);
        }

        public static ByteString getRangeOfByteString(ByteString s, int start, int n) {
            return s.range(start, n);
        }

        public static ByteString takeNFirstBytesOfByteString(ByteString s, int n) {
            return s.take(n);
        }

        public static ByteString takeNLastBytesOfByteString(ByteString s, int n) {
            return s.last(n);
        }

    }

}
