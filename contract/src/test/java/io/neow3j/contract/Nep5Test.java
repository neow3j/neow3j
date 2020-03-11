package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.protocol.http.HttpService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Nep5Test {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow3j;
    private ScriptHash contract;
    private final ScriptHash ACCT_SCRIPTHASH = new ScriptHash("e9eed8dc39332032dc22e5d6e86332c50327ba23");

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and port "localhost:8080".
        WireMock.configure();
        neow3j = Neow3j.build(new HttpService("http://localhost:8080"));
        this.contract = new ScriptHash(ContractTestUtils.NEP5_CONTRACT_SCRIPT_HASH);
    }

    @Test
    public void name() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("name", "invokefunction_name.json");
        Nep5 nep5 = new Nep5.Builder(this.neow3j)
                .fromContract(this.contract)
                .build();
        assertThat(nep5.name(), is("Example"));
    }

    @Test
    public void totalSupply() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("totalSupply", "invokefunction_totalSupply.json");
        Nep5 nep5 = new Nep5.Builder(this.neow3j)
                .fromContract(this.contract)
                .build();
        assertThat(nep5.totalSupply(), is(new BigInteger("1000000000000000")));
    }

    @Test
    public void symbol() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        Nep5 nep5 = new Nep5.Builder(this.neow3j)
                .fromContract(this.contract)
                .build();
        assertThat(nep5.symbol(), is("EXP"));
    }

    @Test
    public void decimals() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        Nep5 nep5 = new Nep5.Builder(this.neow3j)
                .fromContract(this.contract)
                .build();
        assertThat(nep5.decimals(), is(new BigInteger("8")));

    }

    // TODO: 11.03.20 Michael
    //  Write tests for balanceOf and transfer methods.

//    @Test
//    public void balanceOf() {
//    }
//
//    @Test
//    public void transfer() {
//    }
}
