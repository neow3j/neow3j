package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private Token neoToken;
    private Token gasToken;
    private Token someToken;
    private static final ScriptHash NEO_TOKEN_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final ScriptHash GAS_TOKEN_SCRIPT_HASH = GasToken.SCRIPT_HASH;
    private static final ScriptHash SOME_TOKEN_SCRIPT_HASH =
            new ScriptHash("f7014e6d52fe8f94f7c57acd8cfb875b4ac2a1c6");

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);

        Neow3j neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        neoToken = new Token(NEO_TOKEN_SCRIPT_HASH, neow);
        gasToken = new Token(GAS_TOKEN_SCRIPT_HASH, neow);
        someToken = new Token(SOME_TOKEN_SCRIPT_HASH, neow);
    }

    @Test
    public void testGetName() throws IOException {
        setUpWireMockForInvokeFunction("name", "invokefunction_name.json");
        assertThat(someToken.getName(), is("ANT"));
    }

    @Test
    public void testGetName_Neo() throws IOException {
        assertThat(neoToken.getName(), is(NeoToken.NAME));
    }

    @Test
    public void testGetName_Gas() throws IOException {
        assertThat(gasToken.getName(), is(GasToken.NAME));
    }

    @Test
    public void testGetSymbol() throws IOException {
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        assertThat(someToken.getSymbol(), is("ant"));
    }

    @Test
    public void testGetSymbol_Neo() throws IOException {
        assertThat(neoToken.getSymbol(), is(NeoToken.SYMBOL));
    }

    @Test
    public void testGetSymbol_Gas() throws IOException {
        assertThat(gasToken.getSymbol(), is(GasToken.SYMBOL));
    }

    @Test
    public void testGetDecimals() throws Exception {
        setUpWireMockForInvokeFunction("decimals",
                "invokefunction_decimals_nep5.json");
        assertThat(someToken.getDecimals(), is(2));
    }

    @Test
    public void testGetDecimals_Neo() throws Exception {
        assertThat(neoToken.getDecimals(), is(NeoToken.DECIMALS));
    }

    @Test
    public void testGetDecimals_Gas() throws Exception {
        assertThat(gasToken.getDecimals(), is(GasToken.DECIMALS));
    }

    @Test
    public void testGetTotalSupply() throws Exception {
        setUpWireMockForInvokeFunction("totalSupply",
                "invokefunction_totalSupply.json");
        assertThat(someToken.getTotalSupply(), is(new BigInteger("3000000000000000")));
    }

    @Test
    public void testGetTotalSupply_Neo() throws Exception {
        assertThat(neoToken.getTotalSupply(), is(NeoToken.TOTAL_SUPPLY));
    }
}
