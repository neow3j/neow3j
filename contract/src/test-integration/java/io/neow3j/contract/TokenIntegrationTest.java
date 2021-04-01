package io.neow3j.contract;

import static io.neow3j.NeoTestContainer.getNodeUrl;
import static io.neow3j.contract.IntegrationTestHelper.GAS_HASH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import io.neow3j.NeoTestContainer;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

public class TokenIntegrationTest {

    private static Token token;

    @ClassRule
    public static NeoTestContainer neoTestContainer =
            new NeoTestContainer("/node-config/config.json");

    @BeforeClass
    public static void setUp() {
        Neow3j neow3j = Neow3j.build(new HttpService(getNodeUrl(neoTestContainer)));
        token = new Token(GAS_HASH, neow3j);
    }

    @Test
    public void testTotalSupply() throws IOException {
        BigInteger totalSupply = token.getTotalSupply();
        assertThat(totalSupply, greaterThanOrEqualTo(new BigInteger("3000000000000000")));
    }

    @Test
    public void testSymbol() throws IOException {
        String symbol = token.getSymbol();
        assertThat(symbol, is(GasToken.SYMBOL));
    }

    @Test
    public void testDecimals() throws IOException {
        int decimals = token.getDecimals();
        assertThat(decimals, is(GasToken.DECIMALS));
    }

}
