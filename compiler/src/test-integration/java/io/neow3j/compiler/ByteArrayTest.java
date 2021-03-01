package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ByteArrayTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ByteArrayTestContract.class.getName());
    }

    @Test
    public void constructEmptyArray() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.asBuffer().getValue(), is(new byte[]{}));
    }

    @Test
    public void initializeArrayWithZeros() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.asBuffer().getValue(), is(new byte[100]));
    }

    @Test
    public void initializeArrayWithNumbers() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.asBuffer().getValue(), is(new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
                0x06, 0x07, 0x08, 0x09}));
    }

    @Test
    public void getAndSetSingleByte() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(ContractParameter.byteArray("01020304"));
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.asBuffer().getValue(), is(new byte[]{0x00, 0x00, 0x01, 0x00, 0x00}));
    }

    @Test
    public void dynamicallyInitializeArray1() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(ContractParameter.integer(5));
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.asBuffer().getValue(), is(new byte[]{0x01, 0x00, 0x00, 0x00, 0x00}));
    }

    @Test
    public void dynamicallyInitializeArray2() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(ContractParameter.string("hello"));
        StackItem result = response.getInvocationResult().getStack().get(0);
        assertThat(result.getType(), is(StackItemType.BUFFER));
        assertThat(result.asBuffer().getValue(), is(new byte[]{0x01, 0x00, 0x00, 0x00, 0x00}));
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
