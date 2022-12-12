package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Struct;
import io.neow3j.devpack.contracts.StdLib;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.StackItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.test.TestProperties.stdLibHash;
import static io.neow3j.types.ContractParameter.bool;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.Numeric.reverseHexString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StdLibIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(StdLibIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void serialize() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, bool(true), integer(32069));
        byte[] result = response.getInvocationResult().getStack().get(0).getByteArray();
        assertThat(result[0], is(StackItemType.ARRAY.byteValue()));
        assertThat(result[1], is((byte) 0x02)); // Number of elements
        assertThat(result[2], is(StackItemType.BOOLEAN.byteValue())); // type of first element
        assertThat(result[3], is((byte) 0x01)); // the boolean value
        assertThat(result[4], is(StackItemType.INTEGER.byteValue())); // type of first element
        assertThat(result[5], is((byte) 0x02)); // size of value
        assertThat(result[6], is((byte) 0x45)); // part 1 of the value (little-endian)
        assertThat(result[7], is((byte) 0x7D)); // part 2 of the value (little-endian)
    }

    @Test
    public void serializeAndDeserialize() throws IOException {
        int i = 32069;
        NeoInvokeFunction response = ct.callInvokeFunction(testName, bool(true), integer(i));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(res.get(0).getBoolean());
        assertThat(res.get(1).getInteger(), is(BigInteger.valueOf(i)));
    }

    @Test
    public void jsonSerialize() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, bool(true), integer(5), string("hello, world!"));
        String res = response.getInvocationResult().getStack().get(0).getString();
        assertThat(res, is("[true,5,\"hello, world!\"]"));
    }

    @Test
    public void jsonDeserialize() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("[\"true\", 5, \"hello, world!\"]"));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(res.get(0).getBoolean());
        assertThat(res.get(1).getInteger().intValue(), is(5));
        assertThat(res.get(2).getString(), is("hello, world!"));
    }

    @Test
    public void base58Encode() throws IOException {
        String bytes = "54686520717569";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(bytes));
        String encoded = response.getInvocationResult().getStack().get(0).getString();
        String expected = "4CXMH7EgaC";
        assertThat(encoded, is(expected));
    }

    @Test
    public void base58Decode() throws IOException {
        String encoded = "4CXMH7EgaC";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string(encoded));
        String decoded = response.getInvocationResult().getStack().get(0).getHexString();
        String expected = "54686520717569";
        assertThat(decoded, is(expected));
    }

    @Test
    public void base58CheckEncode() throws IOException {
        String bytes = "54686520717569";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(bytes));
        String encoded = response.getInvocationResult().getStack().get(0).getString();
        String expected = "MvzwCLE8dynR7Yn"; // Base58(concat(bytes, sha256(sha256(bytes))))
        assertThat(encoded, is(expected));
    }

    @Test
    public void base58CheckDecode() throws IOException {
        String encoded = "MvzwCLE8dynR7Yn";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string(encoded));
        String decoded = response.getInvocationResult().getStack().get(0).getHexString();
        String expected = "54686520717569";
        assertThat(decoded, is(expected));
    }

    @Test
    public void base64Encode() throws IOException {
        String bytes = "54686520717569636b2062726f776e20666f78206a756d7073206f766572203133206c617a7920646f67732e";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(bytes));
        String encoded = response.getInvocationResult().getStack().get(0).getString();
        String expected = "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIDEzIGxhenkgZG9ncy4=";
        assertThat(encoded, is(expected));
    }

    @Test
    public void base64Decode() throws IOException {
        String encoded = "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIDEzIGxhenkgZG9ncy4=";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string(encoded));
        String decoded = response.getInvocationResult().getStack().get(0).getHexString();
        String expected = "54686520717569636b2062726f776e20666f78206a756d7073206f766572203133206c617a7920646f67732e";
        assertThat(decoded, is(expected));
    }

    @Test
    public void itoa() throws IOException {
        // With base 10
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(100), integer(10));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("100"));

        response = ct.callInvokeFunction(testName, integer(-1), integer(10));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("-1"));

        // With base 16
        response = ct.callInvokeFunction(testName, integer(105), integer(16));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("69"));

        // With base 16
        response = ct.callInvokeFunction(testName, integer(-1), integer(16));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("f"));
    }

    @Test
    public void atoi() throws IOException {
        // With base 10
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("100"), integer(10));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(100));

        response = ct.callInvokeFunction(testName, string("-1"), integer(10));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(-1));

        // With base 16
        response = ct.callInvokeFunction(testName, string("69"), integer(16));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(105));

        response = ct.callInvokeFunction(testName, string("ff"), integer(16));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(-1));
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(reverseHexString(stdLibHash())));
    }

    @Test
    public void memoryCompare() throws Throwable {
        String b1 = "010203";
        String b2 = "010203";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(b1), byteArray(b2));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(0));

        b1 = "000203";
        b2 = "010203";
        response = ct.callInvokeFunction(testName, byteArray(b1), byteArray(b2));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(-1));

        b1 = "020203";
        b2 = "010203";
        response = ct.callInvokeFunction(testName, byteArray(b1), byteArray(b2));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(1));
    }

    @Test
    public void memorySearch() throws Throwable {
        String b = "0102030405060708090a0b0c0d0e0f";
        String value = "040506";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(b), byteArray(value));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(3));

        value = "050406";
        response = ct.callInvokeFunction(testName, byteArray(b), byteArray(value));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(-1));
    }

    @Test
    public void memorySearchWithStart() throws Throwable {
        String b = "0102030405060708090a0b0c0d0e0f";
        String value = "060708";
        int start = 4;
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(b), byteArray(value), integer(start));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(5));

        value = "030405";
        start = 4;
        response = ct.callInvokeFunction(testName, byteArray(b), byteArray(value), integer(start));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(-1));
    }

    @Test
    public void memorySearchWithStartAndBackwards() throws Throwable {
        String b = "0102030405060708090a0b0c0d0e0f";
        String value = "060708";
        int start = 14;
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(b), byteArray(value), integer(start));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(5));

        // start index doesn't cover the whole value.
        start = 6;
        response = ct.callInvokeFunction(testName, byteArray(b), byteArray(value), integer(start));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(-1));

        // start index doesn't cover the whole value.
        value = "010203";
        start = 6;
        response = ct.callInvokeFunction(testName, byteArray(b), byteArray(value), integer(start));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(0));
    }

    @Test
    public void stringSplit() throws Throwable {
        String s = "hello,world,,hello,world";
        String sep = ",";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string(s), string(sep));
        List<StackItem> strings = response.getInvocationResult().getStack().get(0).getList();
        assertThat(strings.get(0).getString(), is("hello"));
        assertThat(strings.get(1).getString(), is("world"));
        assertThat(strings.get(2).getString(), is(""));
        assertThat(strings.get(3).getString(), is("hello"));
        assertThat(strings.get(4).getString(), is("world"));

        s = "helloworldworldhelloworld";
        sep = "world";
        response = ct.callInvokeFunction(testName, string(s), string(sep));
        strings = response.getInvocationResult().getStack().get(0).getList();
        assertThat(strings.get(0).getString(), is("hello"));
        assertThat(strings.get(1).getString(), is(""));
        assertThat(strings.get(2).getString(), is("hello"));
    }

    @Test
    public void stringSplitRemoveEmptyEntries() throws Throwable {
        String s = "hello,world,,hello,world";
        String sep = ",";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string(s), string(sep));
        List<StackItem> strings = response.getInvocationResult().getStack().get(0).getList();
        assertThat(strings.get(0).getString(), is("hello"));
        assertThat(strings.get(1).getString(), is("world"));
        assertThat(strings.get(2).getString(), is("hello"));
        assertThat(strings.get(3).getString(), is("world"));
    }

    static class StdLibIntegrationTestContract {

        static StdLib stdLib = new StdLib();

        public static Object serializeAndDeserialize(boolean b, int i) {
            ByteString ser = stdLib.serialize(new SimpleClass(b, i));
            return stdLib.deserialize(ser);
        }

        public static ByteString serialize(boolean b, int i) {
            return stdLib.serialize(new SimpleClass(b, i));
        }

        public static String jsonSerialize(boolean b, int i, String s) {
            return stdLib.jsonSerialize(new OtherClass(b, i, s));
        }

        public static Object jsonDeserialize(String json) {
            return stdLib.jsonDeserialize(json);
        }

        public static String base64Encode(ByteString bytes) {
            return stdLib.base64Encode(bytes);
        }

        public static ByteString base64Decode(String encoded) {
            return stdLib.base64Decode(encoded);
        }

        public static String base58Encode(ByteString bytes) {
            return stdLib.base58Encode(bytes);
        }

        public static ByteString base58Decode(String encoded) {
            return stdLib.base58Decode(encoded);
        }

        public static String base58CheckEncode(ByteString bytes) {
            return stdLib.base58CheckEncode(bytes);
        }

        public static ByteString base58CheckDecode(String encoded) {
            return stdLib.base58CheckDecode(encoded);
        }

        public static String itoa(int i, int base) {
            return stdLib.itoa(i, base);
        }

        public static int atoi(String s, int base) {
            return stdLib.atoi(s, base);
        }

        public static Hash160 getHash() {
            return stdLib.getHash();
        }

        public static int memoryCompare(ByteString b1, ByteString b2) {
            return stdLib.memoryCompare(b1, b2);
        }

        public static int memorySearch(ByteString b, ByteString value) {
            return stdLib.memorySearch(b, value);
        }

        public static int memorySearchWithStart(ByteString b, ByteString value, int start) {
            return stdLib.memorySearch(b, value, start);
        }

        public static int memorySearchWithStartAndBackwards(ByteString b, ByteString value, int start) {
            return stdLib.memorySearch(b, value, start, true);
        }

        public static String[] stringSplit(String s, String sep) {
            return stdLib.stringSplit(s, sep);
        }

        public static String[] stringSplitRemoveEmptyEntries(String s, String sep) {
            return stdLib.stringSplit(s, sep, true);
        }

        @Struct
        static class SimpleClass {

            boolean b;
            int i;

            public SimpleClass(boolean b, int i) {
                this.b = b;
                this.i = i;
            }
        }

        @Struct
        static class OtherClass {

            boolean b;
            int i;
            String s;

            public OtherClass(boolean b, int i, String s) {
                this.b = b;
                this.i = i;
                this.s = s;
            }

        }

    }

}
