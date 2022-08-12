package io.neow3j.protocol.core;

import io.neow3j.protocol.ResponseTester;
import io.neow3j.protocol.core.response.NeoGetVersion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Raw Response tests.
 */
public class RawResponseTest extends ResponseTester {

    private static final String RAW_RESPONSE = "{\n"
            + "  \"id\":67,\n"
            + "  \"jsonrpc\":\"2.0\",\n"
            + "  \"result\": { "
            + "    \"port\": 1234,\n"
            + "    \"nonce\": 12345678,\n"
            + "    \"useragent\": \"\\/NEO:2.7.6\\/\"\n"
            + "   }"
            + "}";

    @Test
    public void testRawResponseEnabled() {
        configureWeb3Service(true);
        final NeoGetVersion web3ClientVersion = deserialiseWeb3ClientVersionResponse();
        assertThat(web3ClientVersion.getRawResponse(), is(RAW_RESPONSE));
    }

    @Test
    public void testRawResponseDisabled() {
        configureWeb3Service(false);
        final NeoGetVersion web3ClientVersion = deserialiseWeb3ClientVersionResponse();
        assertThat(web3ClientVersion.getRawResponse(), nullValue());
    }

    private NeoGetVersion deserialiseWeb3ClientVersionResponse() {
        buildResponse(RAW_RESPONSE);

        return deserialiseResponse(NeoGetVersion.class);
    }

}
