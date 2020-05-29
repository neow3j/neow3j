package io.neow3j.contract;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Cosigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
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
        this.contract = new ScriptHash(ContractTestHelper.CONTRACT_1_SCRIPT_HASH);
    }

    @Test
    public void transferGas() throws Exception {
        ContractTestHelper.setUpWireMockForSendRawTransaction();
        // Required for fetching of system fee of the invocation.
        ContractTestHelper.setUpWireMockForInvokeFunction(
                "transfer", "invokefunction_transfer_gas.json");
        // Required for fetching the token's decimals.
        ContractTestHelper.setUpWireMockForInvokeFunction(
                "decimals", "invokefunction_decimals_gas.json");
        // Required for fetching the block height used for setting the validUntilBlock.
        ContractTestHelper.setUpWireMockForGetBlockCount(1000);
        // Required when checking the senders token balance.
        ContractTestHelper.setUpWireMockForInvokeFunction("balanceOf",
                "invokefunction_balanceOf.json");

        Nep5Token gas = new Nep5Token(
                new ScriptHash("0x8c23f196d8a1bfd103a9dcb1f9ccf0c611377d3b"), this.neow);
        byte[] privateKey = Numeric.hexStringToByteArray(
                "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3");
        Account a = Account.fromECKeyPair(ECKeyPair.create(privateKey))
                .isDefault().build();
        Wallet w = new Wallet.Builder().accounts(a).build();
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");
        Invocation i = gas.buildTransferInvocation(w, receiver, BigDecimal.ONE);

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
        byte[] expectedScript = Numeric.hexStringToByteArray(
                "0200e1f5050c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c143b7d3711c6f0ccf9b1dca903d1bfa1d896f1238c41627d5b5238");
        assertThat(tx.getScript(), is(expectedScript));
        assertThat(tx.getWitnesses().get(0).getVerificationScript(),
                is(w.getDefaultAccount().getVerificationScript()));
    }

    @Test
    public void getName() throws IOException {
        ContractTestHelper.setUpWireMockForInvokeFunction("name", "invokefunction_name.json");
        Nep5Token nep5 = new Nep5Token(NeoToken.SCRIPT_HASH, this.neow);
        assertThat(nep5.getName(), is("NEO"));
    }

    @Test
    public void getSymbol() throws IOException {
        ContractTestHelper.setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getSymbol(), is("neo"));
    }

    @Test
    public void getDecimals() throws Exception {
        ContractTestHelper
                .setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getDecimals(), is(8));
    }

    @Test
    public void getTotalSupply() throws Exception {
        ContractTestHelper
                .setUpWireMockForInvokeFunction("totalSupply", "invokefunction_totalSupply.json");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getTotalSupply(), is(new BigInteger("3000000000000000")));
    }

    @Test
    public void getBalanceOfAccount() throws Exception {
        ScriptHash acc = ScriptHash.fromAddress("AMRZWegpH58nwY3iSDbmbBGg3kfGH6RgRt");
        ContractTestHelper.setUpWireMockForInvokeFunction("balanceOf",
                "invokefunction_balanceOf.json");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getBalanceOf(acc), is(new BigInteger("3000000000000000")));
    }

    @Test
    public void getBalanceOfWallet() throws Exception {
        Account a1 = Account.fromAddress("AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm").isDefault().build();
        Account a2 = Account.fromAddress("Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ").build();
        ContractTestHelper.setUpWireMockForBalanceOf(a1.getScriptHash(),
                "invokefunction_balanceOf_AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm.json");
        ContractTestHelper.setUpWireMockForBalanceOf(a2.getScriptHash(),
                "invokefunction_balanceOf_Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ.json");
        Wallet w = new Wallet.Builder().accounts(a1, a2).build();
        Nep5Token token = new Nep5Token(GasToken.SCRIPT_HASH, this.neow);
        assertThat(token.getBalanceOf(w), is(new BigInteger("411285799730")));
    }
}
