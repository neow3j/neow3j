package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import io.neow3j.protocol.core.methods.response.NFTokenProperties;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.exceptions.InsufficientFundsException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NFTokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private Account account1;
    private Account account2;
    private Account account3;
    public static final ScriptHash NF_TOKEN_SCRIPT_HASH = ScriptHash.fromAddress("ATv43G3Lv8uGPW7nVhXTUuL1T75qSajKSe");
    private static final byte[] TOKEN_ID = new byte[]{1, 2, 3};
    private static final String TRANSFER = "transfer";
    NFToken nfTestToken;

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);

        Neow3j neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        nfTestToken = new NFToken(NF_TOKEN_SCRIPT_HASH, neow);

        // APiZTA6Ym7EHpLK5PLpSLKn62qeMyCZEER
        account1 = new Account(ECKeyPair.create(
                Numeric.hexStringToByteArray(
                        "1dd37fba80fec4e6a6f13fd708d8dcb3b29def768017052f6c930fa1c5d90bbb")));
        account2 = new Account(ECKeyPair.create(
                Numeric.hexStringToByteArray(
                        "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9")));
        account3 = new Account(ECKeyPair.create(
                Numeric.hexStringToByteArray(
                        "3a100280baf46ea7db17bc01b53365891876b4a2db11028dbc1ccb8c782725f8")));
    }

    @Test
    public void testTransfer() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NF_TOKEN_SCRIPT_HASH, TRANSFER,
                Arrays.asList(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.byteArray(TOKEN_ID)))
                .toArray();

        Wallet wallet = Wallet.withAccounts(account1);
        TransactionBuilder b = nfTestToken.transfer(wallet, account1.getScriptHash(), TOKEN_ID);
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testTransfer_Divisible() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("method is only implemented on NF tokens that are indivisible.");
        nfTestToken.transfer(Wallet.create(), account1.getScriptHash(), TOKEN_ID);
    }

    @Test
    public void testTransfer_MultipleOwners() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof_multiple.json");

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("has 2 owners. To transfer fractions use the method " +
                "transferFractions.");
        nfTestToken.transfer(Wallet.create(), account1.getScriptHash(), TOKEN_ID);
    }

    @Test
    public void testTransfer_WalletDoesNotContainTokenOwner() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");

        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("The provided wallet does not contain the account");
        nfTestToken.transfer(Wallet.withAccounts(account2), account1.getScriptHash(), TOKEN_ID);
    }

    @Test
    public void testTransferFraction() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");
        setUpWireMockForInvokeFunction("balanceOf", "nft_balanceof.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NF_TOKEN_SCRIPT_HASH, TRANSFER,
                Arrays.asList(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(account2.getScriptHash()),
                        ContractParameter.integer(new BigInteger("244")),
                        ContractParameter.byteArray(TOKEN_ID)))
                .toArray();

        Wallet wallet = Wallet.withAccounts(account1);
        TransactionBuilder b = nfTestToken.transferFraction(wallet, account1.getScriptHash(),
                account2.getScriptHash(), new BigDecimal("0.00244"), TOKEN_ID);

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testTransferFractions_InsufficientFunds() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");
        setUpWireMockForInvokeFunction("balanceOf", "nft_balanceof.json");

        Wallet wallet = Wallet.withAccounts(account1);

        exceptionRule.expect(InsufficientFundsException.class);
        exceptionRule.expectMessage("The provided account can not cover the given amount.");
        nfTestToken.transferFraction(wallet, account1.getScriptHash(),
                account2.getScriptHash(), new BigDecimal("0.3"), TOKEN_ID);
    }

    @Test
    public void testTransferFractions_IllegalAmount() throws IOException {
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");
        Wallet wallet = Wallet.withAccounts(account1);

        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("must be in the range of 0 to 1");
        nfTestToken.transferFraction(wallet, account1.getScriptHash(),
                account2.getScriptHash(), new BigDecimal("2"), TOKEN_ID);
    }

    @Test
    public void testTransferFractions_IllegalAmount_ScaleHigherThanDecimals() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");

        Wallet wallet = Wallet.withAccounts(account1);

        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("scale of the provided amount is higher than the decimals");
        nfTestToken.transferFraction(wallet, account1.getScriptHash(),
                account2.getScriptHash(), new BigDecimal("0.00000001"), TOKEN_ID);
    }

    @Test
    public void testTransferFractions_IllegalAmount_Negative() throws IOException {
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");
        Wallet wallet = Wallet.withAccounts(account1);

        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("must be in the range of 0 to 1");
        nfTestToken.transferFraction(wallet, account1.getScriptHash(),
                account2.getScriptHash(), new BigDecimal("-0.1"), TOKEN_ID);
    }

    @Test
    public void testTransferFraction_FromIsNotAnOwner() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof_multiple.json");

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("is not an owner of the token with ID");
        nfTestToken.transferFraction(Wallet.create(), account3.getScriptHash(), account1.getScriptHash(),
                new BigDecimal("0.1"), TOKEN_ID);
    }

    @Test
    public void testTransferFraction_WalletDoesNotContainFromAccount() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");
        setUpWireMockForInvokeFunction("balanceOf", "nft_balanceof.json");

        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("The provided wallet does not contain the provided from account");
        nfTestToken.transferFraction(Wallet.withAccounts(account2), account1.getScriptHash(),
                account3.getScriptHash(), new BigDecimal("0.002"), TOKEN_ID);
    }

    @Test
    public void testOwnerOf() throws IOException {
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");
        List<ScriptHash> owners = nfTestToken.ownerOf(TOKEN_ID);

        assertThat(owners, hasSize(1));
        assertThat(owners, contains(account1.getScriptHash()));
    }

    @Test
    public void testBalanceOf() throws IOException {
        setUpWireMockForInvokeFunction("balanceOf", "nft_balanceof.json");
        BigInteger balance = nfTestToken.balanceOf(account1.getScriptHash(), TOKEN_ID);

        assertThat(balance, is(new BigInteger("244")));
    }

    @Test
    public void testTokensOf() throws IOException {
        setUpWireMockForInvokeFunction("tokensOf", "nft_tokensof.json");
        List<ScriptHash> ownedTokens = nfTestToken.tokensOf(account1.getScriptHash());

        assertThat(ownedTokens, hasSize(2));
        assertThat(ownedTokens, contains(
                ScriptHash.fromAddress("AQVFzBueoJ4qFcYtZmFLfoCoSs4HMHZ3jj"),
                ScriptHash.fromAddress("ALBWw6P15gWiCTDNKg8LxFdHDAYxYnt7dN")));
    }

    @Test
    public void testGetProperties() throws IOException {
        setUpWireMockForInvokeFunction("properties", "nft_properties.json");
        NFTokenProperties properties = nfTestToken.properties(new byte[]{1});

        assertThat(properties.getName(), is("A name"));
        assertThat(properties.getDescription(), is("A description"));
        assertThat(properties.getImage(), is("Some image URI"));
        assertThat(properties.getTokenURI(), is("Some URI"));
    }
}
