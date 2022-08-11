package io.neow3j.compiler;

import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.StackItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ByteArrayTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(ByteArrayTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void constructEmptyArray() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.getByteArray(), is(new byte[]{}));
    }

    @Test
    public void initializeArrayWithZeros() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.getByteArray(), is(new byte[100]));
    }

    @Test
    public void initializeArrayWithNumbers() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.getByteArray(), is(new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09}));
    }

    @Test
    public void getAndSetSingleByte() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray("01020304"));
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.getByteArray(), is(new byte[]{0x00, 0x00, 0x01, 0x00, 0x00}));
    }

    @Test
    public void dynamicallyInitializeArray1() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(5));
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.getByteArray(), is(new byte[]{0x01, 0x00, 0x00, 0x00, 0x00}));
    }

    @Test
    public void dynamicallyInitializeArray2() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("hello"));
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.getByteArray(), is(new byte[]{0x01, 0x00, 0x00, 0x00, 0x00}));
    }

    static class ByteArrayTestContract {

        public static byte[] constructEmptyArray() {
            return new byte[]{};
        }

        public static byte[] initializeArrayWithZeros() {
            return new byte[100];
        }

        public static byte[] initializeArrayWithNumbers() {
            return new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
        }

        public static byte[] getAndSetSingleByte(byte[] bytes1) {
            byte b = bytes1[0];
            byte[] bytes2 = new byte[5];
            bytes2[2] = b;
            return bytes2;
        }

        public static byte[] dynamicallyInitializeArray1(int i) {
            byte[] b = new byte[i];
            b[0] = 1;
            return b;
        }

        public static byte[] dynamicallyInitializeArray2(String s) {
            byte[] b = new byte[s.length()];
            b[0] = 1;
            return b;
        }

    }

}
