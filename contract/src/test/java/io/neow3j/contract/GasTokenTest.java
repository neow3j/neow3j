package io.neow3j.contract;

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
        assertThat(new GasToken(neow).getName(), is("GAS"));
    }

    @Test
    public void getSymbol() {
        assertThat(new GasToken(neow).getSymbol(), is("gas"));
    }

    @Test
    public void getDecimals() {
        assertThat(new GasToken(neow).getDecimals(), is(8));
    }
}
