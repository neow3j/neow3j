package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.contract.IntegrationTestHelper.GAS_HASH;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

@Testcontainers
public class TokenIntegrationTest {

    private static Token token;

    @Container
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeAll
    public static void setUp() {
        Neow3j neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
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
