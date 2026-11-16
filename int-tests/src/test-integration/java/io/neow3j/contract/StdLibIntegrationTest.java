package io.neow3j.contract;

import io.neow3j.compiler.ContractTestExtension;
import io.neow3j.protocol.core.stackitem.StackItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StdLibIntegrationTest {

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension();

    @Test
    public void serializeAndDeserializePreserveVmValues() throws IOException {
        StdLib stdLib = new StdLib(ct.getNeow3j());
        List<Object> value = Arrays.<Object>asList(true, BigInteger.valueOf(32069), "hello");

        byte[] serialized = stdLib.serialize(value);
        StackItem deserialized = stdLib.deserialize(serialized);

        assertTrue(serialized.length > 0);
        assertThat(deserialized.getList().get(0).getBoolean(), is(true));
        assertThat(deserialized.getList().get(1).getInteger(), is(BigInteger.valueOf(32069)));
        assertThat(deserialized.getList().get(2).getString(), is("hello"));
    }

    @Test
    public void jsonSerializeAndDeserializePreserveVmValues() throws IOException {
        StdLib stdLib = new StdLib(ct.getNeow3j());

        String json = stdLib.jsonSerialize(Arrays.<Object>asList("hello", 42));
        StackItem deserialized = stdLib.jsonDeserialize(json);

        assertThat(json, is("[\"hello\",42]"));
        assertThat(deserialized.getList().get(0).getString(), is("hello"));
        assertThat(deserialized.getList().get(1).getInteger(), is(BigInteger.valueOf(42)));
    }

    @Test
    public void encodesAndDecodesBytesAndStrings() throws IOException {
        StdLib stdLib = new StdLib(ct.getNeow3j());
        byte[] bytes = new byte[]{0, 1, 2, (byte) 0xff};

        assertThat(stdLib.base64Encode(bytes), is("AAEC/w=="));
        assertArrayEquals(bytes, stdLib.base64Decode("AAEC/w=="));
        assertThat(stdLib.base64UrlEncode("hello"), is("aGVsbG8"));
        assertThat(stdLib.base64UrlDecode("aGVsbG8"), is("hello"));
        assertArrayEquals(bytes, stdLib.base58Decode(stdLib.base58Encode(bytes)));
        assertArrayEquals(bytes, stdLib.base58CheckDecode(stdLib.base58CheckEncode(bytes)));
        assertThat(stdLib.hexEncode(bytes), is("000102ff"));
        assertArrayEquals(bytes, stdLib.hexDecode("000102ff"));
    }

    @Test
    public void convertsNumbersComparesAndSearchesBytesAndSplitsStrings() throws IOException {
        StdLib stdLib = new StdLib(ct.getNeow3j());
        byte[] bytes = new byte[]{1, 2, 3, 2};

        assertThat(stdLib.itoa(BigInteger.valueOf(255), 16), is("0ff"));
        assertThat(stdLib.itoa(BigInteger.valueOf(255)), is("255"));
        assertThat(stdLib.atoi("0ff", 16), is(BigInteger.valueOf(255)));
        assertThat(stdLib.atoi("255"), is(BigInteger.valueOf(255)));
        assertThat(stdLib.memoryCompare(new byte[]{1}, new byte[]{2}), is(-1));
        assertThat(stdLib.memorySearch(bytes, new byte[]{2}), is(1));
        assertThat(stdLib.memorySearch(bytes, new byte[]{2}, 2), is(3));
        assertThat(stdLib.memorySearch(bytes, new byte[]{2}, 4, true), is(3));
        assertThat(stdLib.stringSplit("a,,b", ","), contains("a", "", "b"));
        assertThat(stdLib.stringSplit("a,,b", ",", true), contains("a", "b"));
        assertThat(stdLib.strLen("hello"), is(5));
    }

}
