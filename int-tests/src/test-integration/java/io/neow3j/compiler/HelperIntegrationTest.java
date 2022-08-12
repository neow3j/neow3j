package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Helper;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.types.StackItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.types.ContractParameter.bool;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelperIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(HelperIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void testAssertTrue() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, bool(true));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, bool(false));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void abort() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, bool(false));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, bool(true));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void toByteArrayFromByte() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(), is(new byte[]{8}));
    }

    @Test
    public void toByteArrayFromString() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("hello, world!"));
        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("hello, world!"));
    }

    @Test
    public void toByteArrayFromInteger() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(new BigInteger("12345678901234567890")));
        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is("d20a1feb8ca954ab00"));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger(),
                is(new BigInteger("12345678901234567890")));

        response = ct.callInvokeFunction(testName, integer(new BigInteger("-12345678901234567890")));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is("2ef5e0147356ab54ff"));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger(),
                is(new BigInteger("-12345678901234567890")));
    }

    @Test
    public void asByte() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(-128));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(-128));

        response = ct.callInvokeFunction(testName, integer(127));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(127));

        response = ct.callInvokeFunction(testName, integer(128));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, integer(-129));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void asSignedByte() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(127));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(127));

        response = ct.callInvokeFunction(testName, integer(128));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(-128));

        response = ct.callInvokeFunction(testName, integer(255));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(-1));

        response = ct.callInvokeFunction(testName, integer(0));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(0));

        response = ct.callInvokeFunction(testName, integer(-1));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, integer(256));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void toInt() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(new byte[]{(byte) 0x80}));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(-128));

        response = ct.callInvokeFunction(testName, byteArray(new byte[]{(byte) 0xff, (byte) 0x80}));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(-32513));

        response = ct.callInvokeFunction(testName, byteArray(new byte[]{}));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(0));

        response = ct.callInvokeFunction(testName, byteArray(new byte[]{(byte) 0xff, 0}));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(255));

        response = ct.callInvokeFunction(testName, byteArray(new byte[]{(byte) 0xfb, (byte) 0xa6, 0}));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(42747));
    }

    @Test
    public void toIntNull() throws IOException {
        // Test that instructions return null if no value was found for the provided key.
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack(), hasSize(0));
        assertThat(response.getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void byteArrayToString() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray("68656c6c6f2c20776f726c6421"));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("hello, world!"));
    }

    @Test
    public void within() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(1), integer(0), integer(3));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());

        response = ct.callInvokeFunction(testName, integer(1), integer(2), integer(3));
        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());

        response = ct.callInvokeFunction(testName, integer(1), integer(-20), integer(0));
        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void concatByteArray() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray("0102"), byteArray("0304"));
        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(hexStringToByteArray("01020304")));
    }

    @Test
    public void rangeOfByteArray() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, byteArray("010203040506"), integer(1), integer(4));
        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(new byte[]{0x02, 0x03, 0x04, 0x05}));
    }

    @Test
    public void rangeOfByteString() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, string("hello, world!"), integer(1), integer(4));
        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("ello"));
    }

    @Test
    public void takeFromByteArray() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray("010203040506"), integer(2));
        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(), is(new byte[]{0x01, 0x02}));

        response = ct.callInvokeFunction(testName, byteArray("010203040506"), integer(-1));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void takeFromByteString() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("hello, world!"), integer(7));
        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("hello, "));

        response = ct.callInvokeFunction(testName, string("hello, world!"), integer(-1));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void lastFromByteArray() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray("010203040506"), integer(2));
        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(), is(new byte[]{0x05, 0x06}));

        response = ct.callInvokeFunction(testName, byteArray("010203040506"), integer(-1));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void lastFromByteString() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("hello, world!"), integer(7));
        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is(" world!"));

        response = ct.callInvokeFunction(testName, string("hello, world!"), integer(-1));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    @Disabled("This doesn't work because the byte array parameter is not a Buffer stack item on the NeoVM (but a " +
            "ByteString). The reverse method therefore fails because it doesn't operate on ByteStrings. Test this " +
            "again once that case can be handled, e.g., with a compiler configuration.")
    public void reverseByteArray() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray("010203040506"));
        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(hexStringToByteArray("060504030201")));
    }

    @Test
    public void reverseByteArrayWithConversionToBuffer() throws IOException {
        String hexString = "68656c6c6f"; // hello
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(hexString));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(hexStringToByteArray("6f6c6c6568")));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("olleh"));
    }

    @Test
    public void sqrt() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(4));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(2));

        response = ct.callInvokeFunction(testName, integer(40401));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(201));

        response = ct.callInvokeFunction(testName, integer(10));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(3));
    }

    @Test
    public void pow() throws IOException {
        NeoInvokeFunction resp = ct.callInvokeFunction(testName, integer(2), integer(2));
        assertThat(resp.getInvocationResult().getStack().get(0).getInteger().intValue(), is(4));

        resp = ct.callInvokeFunction(testName, integer(10), integer(4));
        assertThat(resp.getInvocationResult().getStack().get(0).getInteger().intValue(), is(10000));
    }

    @Test
    public void modMul() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(19), integer(5), integer(11));
        // (19 * 5) % 11 = 7
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(7));
    }

    @Test
    public void modPow() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(3), integer(9), integer(23));
        // (3 ^ 9) % 23 = 18
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(18));
    }

    @Test
    public void memcpyOfByteArray() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(
                        testName,
                        byteArray("000000000000"),
                        integer(0),
                        byteArray("010203040506"),
                        integer(3),
                        integer(3));

        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(new byte[]{0x04, 0x05, 0x06, 0x00, 0x00, 0x00}));
    }

    @Test
    public void memcpyOfByteString() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(
                        testName,
                        byteArray("0000000000000000000000000000000000000000"),
                        integer(17),
                        byteArray("000102030405060708090A0B0C0D0E0F10111213"),
                        integer(10),
                        integer(3));

        assertThat(response.getInvocationResult().getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(new byte[]{
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x0A, 0x0B, 0x0C
                })
        );
    }

    static class HelperIntegrationTestContract {

        public static void testAssertTrue(boolean bool) {
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

        public static byte[] toByteArrayFromInteger(int i) {
            return Helper.toByteArray(i);
        }

        public static byte asByte(int i) {
            return Helper.asByte(i);
        }

        public static byte asSignedByte(int i) {
            return Helper.asSignedByte(i);
        }

        public static int toInt(byte[] bytes) {
            return Helper.toInteger(bytes);
        }

        public static int toIntNull() {
            return Helper.toInteger(null);
        }

        public static String byteArrayToString(byte[] bytes) {
            return Helper.toString(bytes);
        }

        public static boolean within(int i1, int i2, int i3) {
            return Helper.within(i1, i2, i3);
        }

        public static byte[] concatByteArray(byte[] b1, byte[] b2) {
            return Helper.concat(b1, b2);
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
            Helper.reverse(b);
            return b;
        }

        public static byte[] reverseByteArrayWithConversionToBuffer(ByteString b) {
            byte[] buffer = b.toByteArray();
            Helper.reverse(buffer);
            return buffer;
        }

        public static int sqrt(int x) {
            return Helper.sqrt(x);
        }

        public static int pow(int x, int y) {
            return Helper.pow(x, y);
        }

        public static int modMul(int factor1, int factor2, int modulus) {
            return Helper.modMul(factor1, factor2, modulus);
        }

        public static int modPow(int factor, int exponent, int modulus) {
            return Helper.modPow(factor, exponent, modulus);
        }

        public static byte[] memcpyOfByteArray(ByteString d, int di, byte[] s, int si, int n) {
            byte[] destination = d.toByteArray();
            Helper.memcpy(destination, di, s, si, n);
            return destination;
        }

        public static byte[] memcpyOfByteString(ByteString d, int di, ByteString s, int si, int n) {
            byte[] destination = d.toByteArray();
            Helper.memcpy(destination, di, s, si, n);
            return destination;
        }

    }

}
