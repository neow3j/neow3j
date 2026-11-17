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
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
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
    public void setUp() throws IOException {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);
        setUpWireMockForCall("getversion", "getversion.json");
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
    public void testToBaseUnits() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_nep17.json");
        BigInteger baseUnits = someToken.toBaseUnits(new BigDecimal("1.0200"));
        assertThat(baseUnits, is(new BigInteger("102")));
    }

    @Test
    public void testToBaseUnits_tooHighScale() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_nep17.json");
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> someToken.toBaseUnits(new BigDecimal("1.023")));
        assertThat(thrown.getMessage(), containsString("The provided amount has too many decimal points."));
    }

    @Test
    public void testToBaseUnitsWithSpecificDecimals() {
        BigInteger baseUnits = Token.toBaseUnits(new BigDecimal("25.5"), 4);
        assertThat(baseUnits, is(new BigInteger("255000")));
    }

    @Test
    public void testFromBaseUnits() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        BigDecimal amount = someToken.fromBaseUnits(new BigInteger("123456789"));
        assertThat(amount, is(new BigDecimal("1.23456789")));
    }

    @Test
    public void testFromBaseUnitsWithSpecificDecimals() {
        BigDecimal amount = Token.fromBaseUnits(new BigInteger("255000"), 4);
        assertThat(amount, is(new BigDecimal("25.5000")));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedConversions() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_nep17.json");
        BigDecimal amount = new BigDecimal("1.02");
        BigInteger baseUnits = BigInteger.valueOf(102);

        assertThat(someToken.toFractions(amount), is(baseUnits));
        assertThat(Token.toFractions(amount, 2), is(baseUnits));
        assertThat(someToken.toDecimals(baseUnits), is(amount));
        assertThat(Token.toDecimals(baseUnits, 2), is(amount));
    }

}
