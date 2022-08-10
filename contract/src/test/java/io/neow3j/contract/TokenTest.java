package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.types.Hash160;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForInvokeFunction;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TokenTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private Token someToken;
    private static final Hash160 SOME_TOKEN_SCRIPT_HASH = new Hash160("f7014e6d52fe8f94f7c57acd8cfb875b4ac2a1c6");

    @BeforeEach
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);

        Neow3j neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        someToken = new Token(SOME_TOKEN_SCRIPT_HASH, neow);
    }

    @Test
    public void testGetSymbol() throws IOException {
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        assertThat(someToken.getSymbol(), is("ant"));
    }

    @Test
    public void testGetDecimals() throws Exception {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_nep17.json");
        assertThat(someToken.getDecimals(), is(2));
    }

    @Test
    public void testGetTotalSupply() throws Exception {
        setUpWireMockForInvokeFunction("totalSupply", "invokefunction_totalSupply.json");
        assertThat(someToken.getTotalSupply(), is(new BigInteger("3000000000000000")));
    }

    @Test
    public void testToFractions() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_nep17.json");
        BigInteger fractions = someToken.toFractions(new BigDecimal("1.02"));
        assertThat(fractions, is(new BigInteger("102")));
    }

    @Test
    public void testToFractions_tooHighScale() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_nep17.json");
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> someToken.toFractions(new BigDecimal("1.023")));
        assertThat(thrown.getMessage(), containsString("The provided amount has too many decimal points."));
    }

    @Test
    public void testToFractionsWithSpecificDecimals() {
        BigInteger fractions = Token.toFractions(new BigDecimal("1.014"), 6);
        assertThat(fractions, is(new BigInteger("1014000")));
    }

    @Test
    public void testToDecimals() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        BigDecimal decimals = someToken.toDecimals(new BigInteger("123456789"));
        assertThat(decimals, is(new BigDecimal("1.23456789")));
    }

    @Test
    public void testToDecimalsWithSpecificDecimals() {
        BigDecimal decimals = Token.toDecimals(new BigInteger("123456"), 3);
        assertThat(decimals, is(new BigDecimal("123.456")));
    }

}
