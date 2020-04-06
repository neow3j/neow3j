package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class NeoTokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow;

    @Before
    public void setUp() {
        // Configure WireMock to use default host and port "localhost:8080".
        WireMock.configure();
        neow = Neow3j.build(new HttpService("http://localhost:8080"));
    }

    @Test
    public void getName() {
        assertThat(new NeoToken(neow).getName(), is(NeoToken.NAME));
    }

    @Test
    public void getSymbol() {
        assertThat(new NeoToken(neow).getSymbol(), is(NeoToken.SYMBOL));
    }

    @Test
    public void getTotalSupply() {
        assertThat(new NeoToken(neow).getTotalSupply(), is(NeoToken.TOTAL_SUPPLY));
    }

    @Test
    public void getDecimals() {
        assertThat(new NeoToken(neow).getDecimals(), is(NeoToken.DECIMALS));
    }

    @Test
    public void getUnclaimedGas() throws IOException {
        String responseBody = ContractTestUtils.loadFile("/responses/invokefunction_unclaimedgas.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"9bde8f209c88dd0e7ca3bf0af0f476cdd8207789\"" // neo contract
                        + ".*\"unclaimedGas\"" // function
                        + ".*\"f68f181731a47036a99f04dad90043a744edec0f\"" // script hash
                        + ".*100.*" // block height
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        BigInteger result = new NeoToken(neow)
                .getUnclaimedGas(ScriptHash.fromAddress("AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4"), 100);
        assertThat(result, is(new BigInteger("60000000000")));
    }

    @Test
    public void registerValidator() {
        fail();
    }

    @Test
    public void getValidators() throws IOException {
        String responseBody = ContractTestUtils.loadFile("/responses/invokefunction_getvalidators.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"9bde8f209c88dd0e7ca3bf0af0f476cdd8207789\"" // neo contract
                        + ".*\"getValidators\".*" // function
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        List<ECPublicKey> result = new NeoToken(neow).getValidators();
        String expKeyHex = "03f1ec3c1e283e880de6e9c489f0f27c19007c53385aaa4c0c917c320079edadf2";
        ECPublicKey expKey = new ECPublicKey(Numeric.hexStringToByteArray(expKeyHex));
        assertThat(result, contains(expKey));
    }

    @Test
    public void getRegisteredValidators() throws IOException {
        String responseBody = ContractTestUtils.loadFile("/responses/invokefunction_getregisteredvalidators.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"9bde8f209c88dd0e7ca3bf0af0f476cdd8207789\"" // neo contract
                        + ".*\"getRegisteredValidators\".*" // function
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        Map<ECPublicKey, Integer> result = new NeoToken(neow).getRegisteredValidators();
        fail();
        // TODO: Implement test
//        String expKeyHex = "03f1ec3c1e283e880de6e9c489f0f27c19007c53385aaa4c0c917c320079edadf2";
//        ECPublicKey expKey = new ECPublicKey(Numeric.hexStringToByteArray(expKeyHex));
//        assertThat(result, contains(expKey));
    }
}