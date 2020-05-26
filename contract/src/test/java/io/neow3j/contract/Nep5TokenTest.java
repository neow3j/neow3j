package io.neow3j.contract;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Cosigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.WitnessScope;
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

    @Test
    public void transferToken() throws Exception {
        ContractTestUtils.setUpWireMockForSendRawTransaction();
        // Required for fetching of system fee of the invocation.
        ContractTestUtils.setUpWireMockForInvokeFunction(
                "transfer", "invokefunction_transfer_gas.json");
        // Required for fetching the token's decimals.
        ContractTestUtils.setUpWireMockForInvokeFunction(
                "decimals", "invokefunction_decimals_gas.json");
        // Required for fetching the block height used for setting the validUntilBlock.
        ContractTestUtils.setUpWireMockForGetBlockCount(1000);
        // Required when checking the senders token balance.
        ContractTestUtils.setUpWireMockForInvokeFunction("balanceOf",
                "invokefunction_balanceOf.json");

        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        Wallet w = Wallet.createWallet();
        ScriptHash gas = new ScriptHash("0x8c23f196d8a1bfd103a9dcb1f9ccf0c611377d3b");
        Invocation i = nep5.buildTransferInvocation(w, gas, BigDecimal.ONE);

        Transaction tx = i.getTransaction();
        assertThat(tx.getNetworkFee(), is(1268390L));
        assertThat(tx.getSystemFee(), is(9007810L));
        assertThat(tx.getSender(), is(w.getDefaultAccount().getScriptHash()));
        assertThat(tx.getAttributes(), hasSize(1));
        assertThat(tx.getCosigners(), hasSize(1));
        Cosigner c = tx.getCosigners().get(0);
        assertThat(c.getScriptHash(), is(w.getDefaultAccount().getScriptHash()));
        assertThat(c.getScopes().get(0), is(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getWitnesses(), hasSize(1));
        assertThat(tx.getWitnesses().get(0).getVerificationScript(),
                is(w.getDefaultAccount().getVerificationScript()));
    }

    @Test
    public void getName() throws IOException {
        ContractTestUtils.setUpWireMockForInvokeFunction("name", "invokefunction_name.json");
        Nep5Token nep5 = new Nep5Token(NeoToken.SCRIPT_HASH, this.neow);
        assertThat(nep5.getName(), is("NEO"));
    }

    @Test
    public void getSymbol() throws IOException {
        ContractTestUtils.setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getSymbol(), is("neo"));
    }

    @Test
    public void getDecimals() throws Exception {
        ContractTestUtils
                .setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
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
