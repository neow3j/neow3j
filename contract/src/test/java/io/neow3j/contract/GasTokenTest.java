package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class GasTokenTest {

    private Neow3j neow;

    private final static String GASTOKEN_SCRIPTHASH = "70e2301955bf1e74cbb31d18c2f96972abadb328";

    @Before
    public void setUp() {
        neow = Neow3j.build(new HttpService("http://127.0.0.1:8080"));
    }

    @Test
    public void getName() {
        assertThat(new GasToken(neow).getName(), is("GasToken"));
    }

    @Test
    public void getSymbol() {
        assertThat(new GasToken(neow).getSymbol(), is("GAS"));
    }

    @Test
    public void getDecimals() {
        assertThat(new GasToken(neow).getDecimals(), is(8));
    }

    @Test
    public void scriptHash() {
        assertThat(new GasToken(neow).getScriptHash().toString(), is(GASTOKEN_SCRIPTHASH));
    }
}
