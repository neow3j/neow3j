package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.exceptions.StackItemCastException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StdLibTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private StdLib stdLib;

    @BeforeAll
    public void setUp() throws IOException {
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);
        setUpWireMockForCall("getversion", "getversion.json");
        Neow3j neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        stdLib = new StdLib(neow3j);
    }

    @Test
    public void hasNativeContractIdentity() {
        assertThat(StdLib.NAME, is("StdLib"));
        assertThat(StdLib.MAX_INPUT_LENGTH, is(1024));
        assertThat(stdLib.getScriptHash(), is(StdLib.SCRIPT_HASH));
    }

    @Test
    public void serializesContractParameterToRawBytes() throws IOException {
        stubResponse("ByteString", "\"AQID\"");

        byte[] bytes = stdLib.serialize(Arrays.asList(true, BigInteger.valueOf(32069)));

        assertArrayEquals(new byte[]{1, 2, 3}, bytes);
        verifyInvocation("serialize");
    }

    @Test
    public void deserializesBytesToStackItemWithoutChangingIntegerEndianness() throws IOException {
        stubResponse("Integer", "\"256\"");

        StackItem item = stdLib.deserialize(new byte[]{1, 2, 3});

        assertThat(item.getInteger(), is(BigInteger.valueOf(256)));
        verifyInvocation("deserialize");
        WireMock.verify(postRequestedFor(urlEqualTo("/"))
                .withRequestBody(containing("\"value\":\"AQID\"")));
    }

    @Test
    public void jsonSerializeReturnsUtf8Json() throws IOException {
        stubResponse("ByteString", "\"WyJoZWxsbyIsNDJd\"");

        assertThat(stdLib.jsonSerialize(Arrays.asList("hello", 42)), is("[\"hello\",42]"));
        verifyInvocation("jsonSerialize");
    }

    @Test
    public void jsonDeserializeReturnsStackItem() throws IOException {
        stubResponse("Array", "[{\"type\":\"ByteString\",\"value\":\"aGVsbG8=\"}," +
                "{\"type\":\"Integer\",\"value\":\"42\"}]");

        StackItem item = stdLib.jsonDeserialize("[\"hello\",42]");

        assertThat(item.getList().get(0).getString(), is("hello"));
        assertThat(item.getList().get(1).getInteger(), is(BigInteger.valueOf(42)));
        verifyInvocation("jsonDeserialize");
    }

    @Test
    public void base64Encode() throws IOException {
        stubResponse("ByteString", "\"QVFJRA==\"");
        assertThat(stdLib.base64Encode(new byte[]{1, 2, 3}), is("AQID"));
        verifyInvocation("base64Encode");
    }

    @Test
    public void base64Decode() throws IOException {
        stubResponse("ByteString", "\"AQID\"");
        assertArrayEquals(new byte[]{1, 2, 3}, stdLib.base64Decode("AQID"));
        verifyInvocation("base64Decode");
    }

    @Test
    public void base64UrlEncode() throws IOException {
        stubResponse("ByteString", "\"YUdWc2JHOA==\"");
        assertThat(stdLib.base64UrlEncode("hello"), is("aGVsbG8"));
        verifyInvocation("base64UrlEncode");
    }

    @Test
    public void base64UrlDecode() throws IOException {
        stubResponse("ByteString", "\"aGVsbG8=\"");
        assertThat(stdLib.base64UrlDecode("aGVsbG8"), is("hello"));
        verifyInvocation("base64UrlDecode");
    }

    @Test
    public void base58Encode() throws IOException {
        stubResponse("ByteString", "\"Mmc=\"");
        assertThat(stdLib.base58Encode(new byte[]{1}), is("2g"));
        verifyInvocation("base58Encode");
    }

    @Test
    public void base58Decode() throws IOException {
        stubResponse("ByteString", "\"AQ==\"");
        assertArrayEquals(new byte[]{1}, stdLib.base58Decode("2g"));
        verifyInvocation("base58Decode");
    }

    @Test
    public void base58CheckEncode() throws IOException {
        stubResponse("ByteString", "\"MTEx\"");
        assertThat(stdLib.base58CheckEncode(new byte[0]), is("111"));
        verifyInvocation("base58CheckEncode");
    }

    @Test
    public void base58CheckDecode() throws IOException {
        stubResponse("ByteString", "\"\"");
        assertArrayEquals(new byte[0], stdLib.base58CheckDecode("111"));
        verifyInvocation("base58CheckDecode");
    }

    @Test
    public void hexEncode() throws IOException {
        stubResponse("ByteString", "\"MDEwMjAz\"");
        assertThat(stdLib.hexEncode(new byte[]{1, 2, 3}), is("010203"));
        verifyInvocation("hexEncode");
    }

    @Test
    public void hexDecode() throws IOException {
        stubResponse("ByteString", "\"AQID\"");
        assertArrayEquals(new byte[]{1, 2, 3}, stdLib.hexDecode("010203"));
        verifyInvocation("hexDecode");
    }

    @Test
    public void itoa() throws IOException {
        stubResponse("ByteString", "\"MGZm\"");
        assertThat(stdLib.itoa(BigInteger.valueOf(255), 16), is("0ff"));
        verifyInvocation("itoa");
    }

    @Test
    public void itoaDefaultsToDecimal() throws IOException {
        stubResponse("ByteString", "\"NDI=\"");
        assertThat(stdLib.itoa(BigInteger.valueOf(42)), is("42"));
        verifyInvocation("itoa");
    }

    @Test
    public void atoi() throws IOException {
        stubResponse("Integer", "\"255\"");
        assertThat(stdLib.atoi("ff", 16), is(BigInteger.valueOf(255)));
        verifyInvocation("atoi");
    }

    @Test
    public void atoiDefaultsToDecimal() throws IOException {
        stubResponse("Integer", "\"42\"");
        assertThat(stdLib.atoi("42"), is(BigInteger.valueOf(42)));
        verifyInvocation("atoi");
    }

    @Test
    public void memoryCompare() throws IOException {
        stubResponse("Integer", "\"-1\"");
        assertThat(stdLib.memoryCompare(new byte[]{1}, new byte[]{2}), is(-1));
        verifyInvocation("memoryCompare");
    }

    @Test
    public void memorySearchOverloads() throws IOException {
        stubResponse("Integer", "\"2\"");
        assertThat(stdLib.memorySearch(new byte[]{1, 2, 3}, new byte[]{3}), is(2));
        assertThat(stdLib.memorySearch(new byte[]{1, 2, 3}, new byte[]{3}, 1), is(2));
        assertThat(stdLib.memorySearch(new byte[]{1, 2, 3}, new byte[]{3}, 2, true), is(2));
        verifyInvocation("memorySearch");
        WireMock.verify(3, postRequestedFor(urlEqualTo("/"))
                .withRequestBody(containing("\"memorySearch\"")));
    }

    @Test
    public void stringSplitOverloads() throws IOException {
        stubResponse("Array", "[{\"type\":\"ByteString\",\"value\":\"YQ==\"}," +
                "{\"type\":\"ByteString\",\"value\":\"Yg==\"}]");

        List<String> result1 = stdLib.stringSplit("a,b", ",");
        List<String> result2 = stdLib.stringSplit("a,b", ",", true);

        assertThat(result1, contains("a", "b"));
        assertThat(result2, contains("a", "b"));
        verifyInvocation("stringSplit");
        WireMock.verify(2, postRequestedFor(urlEqualTo("/"))
                .withRequestBody(containing("\"stringSplit\"")));
    }

    @Test
    public void strLen() throws IOException {
        stubResponse("Integer", "\"5\"");
        assertThat(stdLib.strLen("hello"), is(5));
        verifyInvocation("strLen");
    }

    @Test
    public void rejectsUnexpectedByteArrayReturnType() {
        stubResponse("Integer", "\"1\"");
        assertThrows(UnexpectedReturnTypeException.class, () -> stdLib.hexDecode("01"));
    }

    @Test
    public void stringSplit_wrapsInvalidElementValue() {
        stubResponse("Array", "[{\"type\":\"ByteString\",\"value\":null}]");

        UnexpectedReturnTypeException thrown = assertThrows(UnexpectedReturnTypeException.class,
                () -> stdLib.stringSplit("a,b", ","));
        assertInstanceOf(StackItemCastException.class, thrown.getCause());
    }

    private void stubResponse(String stackItemType, String stackItemValueJson) {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{" +
                "\"script\":\"\",\"state\":\"HALT\",\"gasconsumed\":\"0\",\"exception\":null," +
                "\"stack\":[{\"type\":\"" + stackItemType + "\",\"value\":" + stackItemValueJson + "}]}}";
        WireMock.stubFor(post(urlEqualTo("/")).willReturn(aResponse().withStatus(200).withBody(body)));
    }

    private void verifyInvocation(String function) {
        WireMock.verify(postRequestedFor(urlEqualTo("/"))
                .withRequestBody(containing("\"method\":\"invokefunction\""))
                .withRequestBody(containing("\"" + function + "\""))
                .withRequestBody(containing(toHexStringNoPrefix(StdLib.SCRIPT_HASH.toArray()))));
    }

}
