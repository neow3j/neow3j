package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.bool;
import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.Helper;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class HelperIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(HelperIntegrationTestContract.class.getName());
    }

    @Test
    public void assertTrue() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(bool(true));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_HALT));

        response = callInvokeFunction(bool(false));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void abort() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(bool(false));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_HALT));

        response = callInvokeFunction(bool(true));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void toByteArrayFromByte() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(new byte[]{8}));
    }

    @Test
    public void toByteArrayFromString() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("hello, world!"));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("hello, world!"));
    }

    @Test
    public void asByte() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(integer(-128));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_HALT));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(-128));

        response = callInvokeFunction(integer(127));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_HALT));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(127));

        response = callInvokeFunction(integer(128));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));

        response = callInvokeFunction(integer(-129));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void asSignedByte() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(integer(127));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(127));

        response = callInvokeFunction(integer(128));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(-128));

        response = callInvokeFunction(integer(255));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(-1));

        response = callInvokeFunction(integer(0));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(0));

        response = callInvokeFunction(integer(-1));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));

        response = callInvokeFunction(integer(256));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void toInt() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(byteArray(new byte[]{(byte) 0x80}));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(-128));

        response = callInvokeFunction(byteArray(new byte[]{(byte) 0xff, (byte) 0x80}));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(-32513));

        response = callInvokeFunction(byteArray(new byte[]{}));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(0));

        response = callInvokeFunction(byteArray(new byte[]{(byte) 0xff, 0}));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(255));

        response = callInvokeFunction(byteArray(new byte[]{(byte) 0xfb, (byte) 0xa6, 0}));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(42747));
    }

    @Test
    public void toByteString() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(byteArray("68656c6c6f2c20776f726c6421"));
        assertThat(
                response.getInvocationResult().getStack().get(0).getString(),
                is("hello, world!"));
    }

    @Test
    public void within() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(integer(1), integer(0), integer(3));
        Assert.assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());

        response = callInvokeFunction(integer(1), integer(2), integer(3));
        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());

        response = callInvokeFunction(integer(1), integer(-20), integer(0));
        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void concatByteArray() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(byteArray("0102"), byteArray("0304"));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(Numeric.hexStringToByteArray("01020304")));
    }

    @Test
    public void concatByteString() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("hello"), string(", world!"));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BYTE_STRING));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("hello, world!"));
    }

    @Test
    public void rangeOfByteArray() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                byteArray("010203040506"), integer(1), integer(4));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(new byte[]{0x02, 0x03, 0x04, 0x05}));
    }

    @Test
    public void rangeOfByteString() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("hello, world!"), integer(1),
                integer(4));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BYTE_STRING));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("ello"));
    }

    @Test
    public void takeFromByteArray() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(byteArray("010203040506"), integer(2));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(new byte[]{0x01, 0x02}));

        response = callInvokeFunction(byteArray("010203040506"), integer(-1));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void takeFromByteString() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("hello, world!"), integer(7));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BYTE_STRING));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("hello, "));

        response = callInvokeFunction(string("hello, world!"), integer(-1));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void lastFromByteArray() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(byteArray("010203040506"), integer(2));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(new byte[]{0x05, 0x06}));

        response = callInvokeFunction(byteArray("010203040506"), integer(-1));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void lastFromByteString() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("hello, world!"), integer(7));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BYTE_STRING));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is(" world!"));

        response = callInvokeFunction(string("hello, world!"), integer(-1));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    @Ignore("This doesn't work yet because with neo3-preview3 the byte array parameter is not "
            + "a Buffer StackItem on the neo-vm (but a ByteString). The reverse method therefore "
            + "fails because it doesn't operate on ByteString. Test again with neo3-preview4.")
    public void reverseByteArray() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(byteArray("010203040506"));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(Numeric.hexStringToByteArray("060504030201")));
    }

    @Test
    public void reverseByteString() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("hello, world!"));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.BYTE_STRING));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("!dlrow ,olleh"));
    }

    @Test
    public void sqrt() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(integer(4));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(2));

        response = callInvokeFunction(integer(40401));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(201));

        response = callInvokeFunction(integer(10));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(3));
    }

    @Test
    public void pow() throws IOException {
        NeoInvokeFunction resp = callInvokeFunction(integer(2), integer(2));
        assertThat(resp.getInvocationResult().getStack().get(0).getInteger().intValue(), is(4));

        resp = callInvokeFunction(integer(10), integer(4));
        assertThat(resp.getInvocationResult().getStack().get(0).getInteger().intValue(), is(10000));
    }

    static class HelperIntegrationTestContract {

        public static void assertTrue(boolean bool) {
            Helper.assertTrue(bool);
        }

        public static void abort(boolean bool) {
            if (bool) {
                Helper.abort();
            }
        }

        public static byte[] toByteArrayFromByte() {
            return Helper.toByteArray((byte) 8);
        }

        public static byte[] toByteArrayFromString(String s) {
            return Helper.toByteArray(s);
        }

        public static byte asByte(int i) {
            return Helper.asByte(i);
        }

        public static byte asSignedByte(int i) {
            return Helper.asSignedByte(i);
        }

        public static int toInt(byte[] bytes) {
            return Helper.toInt(bytes);
        }

        public static String toByteString(byte[] bytes) {
            return Helper.toByteString(bytes);
        }

        public static boolean within(int i1, int i2, int i3) {
            return Helper.within(i1, i2, i3);
        }

        public static byte[] concatByteArray(byte[] b1, byte[] b2) {
            return Helper.concat(b1, b2);
        }

        public static String concatByteString(String s1, String s2) {
            return Helper.concat(s1, s2);
        }

        public static byte[] rangeOfByteArray(byte[] b, int i1, int i2) {
            return Helper.range(b, i1, i2);
        }

        public static String rangeOfByteString(String s, int i1, int i2) {
            return Helper.range(s, i1, i2);
        }

        public static byte[] takeFromByteArray(byte[] b, int i) {
            return Helper.take(b, i);
        }

        public static String takeFromByteString(String s, int i) {
            return Helper.take(s, i);
        }

        public static byte[] lastFromByteArray(byte[] b, int i) {
            return Helper.last(b, i);
        }

        public static String lastFromByteString(String s, int i) {
            return Helper.last(s, i);
        }

        public static byte[] reverseByteArray(byte[] b) {
            return Helper.reverse(b);
        }

        public static String reverseByteString(String s) {
            return Helper.reverse(s);
        }

        public static int sqrt(int x) {
            return Helper.sqrt(x);
        }

        public static int pow(int x, int y) {
            return Helper.pow(x, y);
        }
    }

}
