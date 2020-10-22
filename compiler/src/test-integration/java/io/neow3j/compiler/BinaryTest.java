package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.bool;
import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.neo.Binary;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class BinaryTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(BinaryContract.class.getName());
    }

    @Test
    public void serialize() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(bool(true), integer(32069));
        byte[] result = response.getInvocationResult().getStack().get(0).asByteString().getValue();
        assertThat(result[0], is(StackItemType.ARRAY.byteValue()));
        assertThat(result[1], is((byte) 0x02)); // Number of elements
        // TODO: This should be a Boolean but is a Integer.
        //  See https://github.com/neo-project/neo/issues/1912
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
        List<StackItem> res = response.getInvocationResult().getStack().get(0).asArray().getValue();
        // TODO: This should be a Boolean but is a Integer.
        //  See https://github.com/neo-project/neo/issues/1912
        assertThat(res.get(0).asInteger().getValue(), is(BigInteger.ONE));
        assertThat(res.get(1).asInteger().getValue(), is(BigInteger.valueOf(i)));
    }

    @Test
    public void base64Encode() throws IOException {
        String bytes =
                "54686520717569636b2062726f776e20666f78206a756d7073206f766572203133206c617a7920646f67732e";
        NeoInvokeFunction response = callInvokeFunction(byteArray(bytes));
        String encoded = response.getInvocationResult().getStack().get(0).asByteString()
                .getAsString();
        String expected = "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIDEzIGxhenkgZG9ncy4=";
        assertThat(encoded, is(expected));
    }

    @Test
    public void base64Decode() throws IOException {
        String encoded = "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIDEzIGxhenkgZG9ncy4=";
        NeoInvokeFunction response = callInvokeFunction(string(encoded));
        // TODO: This should be a Buffer but is a ByteString.
        //  See https://github.com/neo-project/neo/issues/1912
        String decoded = response.getInvocationResult().getStack().get(0).asByteString()
                .getAsHexString();
        String expected =
                "54686520717569636b2062726f776e20666f78206a756d7073206f766572203133206c617a7920646f67732e";
        assertThat(decoded, is(expected));
    }

    static class BinaryContract {

        public static Object serializeAndDeserialize(boolean b, int i) {
            byte[] ser = Binary.serialize(new SimpleClass(b, i));
            return Binary.deserialize(ser);
        }

        public static byte[] serialize(boolean b, int i) {
            return Binary.serialize(new SimpleClass(b, i));
        }

        public static String base64Encode(byte[] bytes) {
            return Binary.base64Encode(bytes);
        }

        public static byte[] base64Decode(String encoded) {
            return Binary.base64Decode(encoded);
        }

        static class SimpleClass {

            public boolean b;
            public int i;

            public SimpleClass(boolean b, int i) {
                this.b = b;
                this.i = i;
            }
        }

    }
}
