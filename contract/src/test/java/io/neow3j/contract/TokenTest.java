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
    private static final ScriptHash NEO_TOKEN_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final ScriptHash GAS_TOKEN_SCRIPT_HASH = GasToken.SCRIPT_HASH;

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);

        Neow3j neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        neoToken = new Token(NEO_TOKEN_SCRIPT_HASH, neow);
        gasToken = new Token(GAS_TOKEN_SCRIPT_HASH, neow);
    }

    @Test
    public void testGetName() throws IOException {
        setUpWireMockForInvokeFunction("name", "invokefunction_name.json");
        assertThat(neoToken.getName(), is("NEO"));
    }

    @Test
    public void testGetSymbol() throws IOException {
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        assertThat(neoToken.getSymbol(), is("neo"));
    }

    @Test
    public void testGetDecimals() throws Exception {
        setUpWireMockForInvokeFunction("decimals",
                "invokefunction_decimals_gas.json");
        assertThat(gasToken.getDecimals(), is(8));
    }

    @Test
    public void testGetTotalSupply() throws Exception {
        setUpWireMockForInvokeFunction("totalSupply",
                "invokefunction_totalSupply.json");
        assertThat(gasToken.getTotalSupply(), is(new BigInteger("3000000000000000")));
    }
}
