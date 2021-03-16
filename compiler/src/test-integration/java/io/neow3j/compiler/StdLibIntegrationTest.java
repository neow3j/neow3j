package io.neow3j.compiler;

import static io.neow3j.TestProperties.stdLibHash;
import static io.neow3j.contract.ContractParameter.bool;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.compiler.utils.ContractCompilationTestRule;
import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.StdLib;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class StdLibIntegrationTest extends ContractTest {

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(privateNetContainer)
            .around(
                    new ContractCompilationTestRule(
                            StdLibIntegrationTestContract.class.getName(),
                            privateNetContainer
                    )
            );

    @Test
    public void serialize() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(bool(true), integer(32069));
        byte[] result = response.getInvocationResult().getStack().get(0).getByteArray();
        assertThat(result[0], is(StackItemType.ARRAY.byteValue()));
        assertThat(result[1], is((byte) 0x02)); // Number of elements
        assertThat(result[2], is(StackItemType.INTEGER.byteValue())); // type of first element
        assertThat(result[3], is((byte) 0x01)); // size of value
        assertThat(result[4], is((byte) 0x01)); // the value
        assertThat(result[5], is(StackItemType.INTEGER.byteValue())); // type of first element
        assertThat(result[6], is((byte) 0x02)); // size of value
        assertThat(result[7], is((byte) 0x45)); // part 1 of the value (little-endian)
        assertThat(result[8], is((byte) 0x7D)); // part 2 of the value (little-endian)
    }

    @Test
    public void serializeAndDeserialize() throws IOException {
        int i = 32069;
        NeoInvokeFunction response = callInvokeFunction(bool(true), integer(i));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(res.get(0).getBoolean());
        assertThat(res.get(1).getInteger(), is(BigInteger.valueOf(i)));
    }

    @Test
    public void jsonSerialize() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(bool(true), integer(5),
                string("hello, world!"));
        String res = response.getInvocationResult().getStack().get(0).getString();
        assertThat(res, is("[1,5,\"hello, world!\"]"));
    }

    @Test
    public void jsonDeserialize() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("[\"true\", 5, \"hello, world!\"]"));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(res.get(0).getBoolean());
        assertThat(res.get(1).getInteger().intValue(), is(5));
        assertThat(res.get(2).getString(), is("hello, world!"));
    }

    @Test
    public void base58Encode() throws IOException {
        String bytes = "54686520717569";
        NeoInvokeFunction response = callInvokeFunction(ContractParameter.byteArray(bytes));
        String encoded = response.getInvocationResult().getStack().get(0).getString();
        String expected = "4CXMH7EgaC";
        assertThat(encoded, is(expected));
    }

    @Test
    public void base58Decode() throws IOException {
        String encoded = "4CXMH7EgaC";
        NeoInvokeFunction response = callInvokeFunction(string(encoded));
        String decoded = response.getInvocationResult().getStack().get(0).getHexString();
        String expected = "54686520717569";
        assertThat(decoded, is(expected));
    }

    @Test
    public void base64Encode() throws IOException {
        String bytes =
                "54686520717569636b2062726f776e20666f78206a756d7073206f766572203133206c617a7920646f67732e";
        NeoInvokeFunction response = callInvokeFunction(ContractParameter.byteArray(bytes));
        String encoded = response.getInvocationResult().getStack().get(0).getString();
        String expected = "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIDEzIGxhenkgZG9ncy4=";
        assertThat(encoded, is(expected));
    }

    @Test
    public void base64Decode() throws IOException {
        String encoded = "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIDEzIGxhenkgZG9ncy4=";
        NeoInvokeFunction response = callInvokeFunction(string(encoded));
        String decoded = response.getInvocationResult().getStack().get(0).getHexString();
        String expected =
                "54686520717569636b2062726f776e20666f78206a756d7073206f766572203133206c617a7920646f67732e";
        assertThat(decoded, is(expected));
    }

    @Test
    public void itoa() throws IOException {
        // With base 10
        NeoInvokeFunction response = callInvokeFunction(integer(100), integer(10));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("100"));

        response = callInvokeFunction(integer(-1), integer(10));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("-1"));

        // With base 16
        response = callInvokeFunction(integer(105), integer(16));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("69"));

        // With base 16
        response = callInvokeFunction(integer(-1), integer(16));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("f"));
    }

    @Test
    public void atoi() throws IOException {
        // With base 10
        NeoInvokeFunction response = callInvokeFunction(string("100"), integer(10));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(100));

        response = callInvokeFunction(string("-1"), integer(10));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(-1));

        // With base 16
        response = callInvokeFunction(string("69"), integer(16));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(105));

        response = callInvokeFunction(string("ff"), integer(16));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(-1));
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(stdLibHash()));
    }

    static class StdLibIntegrationTestContract {

        public static Object serializeAndDeserialize(boolean b, int i) {
            byte[] ser = StdLib.serialize(new SimpleClass(b, i));
            return StdLib.deserialize(ser);
        }

        public static byte[] serialize(boolean b, int i) {
            return StdLib.serialize(new SimpleClass(b, i));
        }

        public static String jsonSerialize(boolean b, int i, String s) {
            return StdLib.jsonSerialize(new OtherClass(b, i, s));
        }

        public static Object jsonDeserialize(String json) {
            return StdLib.jsonDeserialize(json);
        }

        public static String base64Encode(byte[] bytes) {
            return StdLib.base64Encode(bytes);
        }

        public static byte[] base64Decode(String encoded) {
            return StdLib.base64Decode(encoded);
        }

        public static String base58Encode(byte[] bytes) {
            return StdLib.base58Encode(bytes);
        }

        public static byte[] base58Decode(String encoded) {
            return StdLib.base58Decode(encoded);
        }

        public static String itoa(int i, int base) {
            return StdLib.itoa(i, base);
        }

        public static int atoi(String s, int base) {
            return StdLib.atoi(s, base);
        }

        public static Hash160 getHash() {
            return StdLib.getHash();
        }

        static class SimpleClass {

            boolean b;
            int i;

            public SimpleClass(boolean b, int i) {
                this.b = b;
                this.i = i;
            }
        }

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
