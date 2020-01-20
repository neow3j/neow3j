package io.neow3j.contract;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Nep5TokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow;
    private ScriptHash contract;

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and port "localhost:8080".
        WireMock.configure();
        neow = Neow3j.build(new HttpService("http://localhost:8080"));
        this.contract = new ScriptHash(ContractTestUtils.CONTRACT_1_SCRIPT_HASH);
    }

    @Test
    public void transferToken() throws Exception {
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        nep5.transfer(new ScriptHash("e9eed8dc39332032dc22e5d6e86332c50327ba23"),
            new ScriptHash("0f2b7a6ee34db32d9151c6028960ab2a8babea52"), BigDecimal.ONE);
    }

    @Test
    public void getName() throws IOException {
        ContractTestUtils.setUpWireMockForInvokeFunction("name");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getName(), is("Example"));
    }

    @Test
    public void getSymbol() throws IOException {
        ContractTestUtils.setUpWireMockForInvokeFunction("symbol");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getSymbol(), is("EXP"));
    }

    @Test
    public void getDecimals() throws Exception {
        ContractTestUtils.setUpWireMockForInvokeFunction("decimals");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getDecimals(), is(8));
    }

    @Test
    public void getTotalSupply() throws Exception {
        ContractTestUtils.setUpWireMockForInvokeFunction("totalSupply");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getTotalSupply(), is(new BigInteger("1000000000000000")));
    }
}
