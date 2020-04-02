package io.neow3j.contract;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.Wallet;
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

//    @Ignore
    @Test
    public void transferToken() throws Exception {
        ContractTestUtils.setUpWireMockForSendRawTransaction();
        ContractTestUtils
                .setUpWireMockForInvokeFunction("transfer", "invokefunction_transfer.json");
        ContractTestUtils
                .setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        ContractTestUtils.setUpWireMockForGetBlockCount();

        // Block count will be NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT
        // + ContractTestUtils.GETBLOCKCOUNT_RESPONSE;

        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        Wallet w = Wallet.createWallet();
        nep5.transfer(w, new ScriptHash("0f2b7a6ee34db32d9151c6028960ab2a8babea52"),
                BigDecimal.ONE);
        // TODO: Setup a raw transaction that contains the expected transaction bytes for a
        //  transfer.
        fail();
    }

    @Test
    public void getName() throws IOException {
        ContractTestUtils.setUpWireMockForInvokeFunction("name", "invokefunction_name.json");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getName(), is("Example"));
    }

    @Test
    public void getSymbol() throws IOException {
        ContractTestUtils.setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getSymbol(), is("EXP"));
    }

    @Test
    public void getDecimals() throws Exception {
        ContractTestUtils
                .setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getDecimals(), is(8));
    }

    @Test
    public void getTotalSupply() throws Exception {
        ContractTestUtils
                .setUpWireMockForInvokeFunction("totalSupply", "invokefunction_totalSupply.json");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getTotalSupply(), is(new BigInteger("3000000000000000")));
    }

    @Test
    public void getBalanceOf() throws Exception {
        ScriptHash acc = ScriptHash.fromAddress("AMRZWegpH58nwY3iSDbmbBGg3kfGH6RgRt");
        ContractTestUtils.setUpWireMockForInvokeFunction("balanceOf",
                "invokefunction_balanceOf.json");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getBalanceOf(acc), is(new BigInteger("3000000000000000")));
    }

}
