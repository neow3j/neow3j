package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForBalanceOf;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForGetBlockCount;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForSendRawTransaction;
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
import io.neow3j.wallet.exceptions.InsufficientFundsException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Nep5TokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());
    private Neow3j neow;
    private Nep5Token neoToken;
    private Nep5Token gasToken;

    private static final ScriptHash NEO_TOKEN_SCRIPT_HASH = new ScriptHash("0x9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
    private static final ScriptHash GAS_TOKEN_SCRIPT_HASH = new ScriptHash("0x8c23f196d8a1bfd103a9dcb1f9ccf0c611377d3b");

    // Configuring accounts and wallet
    private static final Account ACCOUNT_1 = new Account(ECKeyPair.create(
            Numeric.hexStringToByteArray("1dd37fba80fec4e6a6f13fd708d8dcb3b29def768017052f6c930fa1c5d90bbb")));
    private static final Account ACCOUNT_2 = new Account(ECKeyPair.create(
            Numeric.hexStringToByteArray("b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9")));
    private static final Account ACCOUNT_3 = new Account(ECKeyPair.create(
            Numeric.hexStringToByteArray("3a100280baf46ea7db17bc01b53365891876b4a2db11028dbc1ccb8c782725f8")));
    private static final Wallet WALLET = Wallet.withAccounts(ACCOUNT_1, ACCOUNT_2, ACCOUNT_3);

    private static final ScriptHash RECIPIENT_SCRIPT_HASH = new ScriptHash("969a77db482f74ce27105f760efa139223431394");

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);
        neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));

        neoToken = new Nep5Token(NEO_TOKEN_SCRIPT_HASH, neow);
        gasToken = new Nep5Token(GAS_TOKEN_SCRIPT_HASH, neow);
    }

    @Test
    public void transferGas() throws Exception {
        setUpWireMockForSendRawTransaction();
        String script =
                "0200e1f5050c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c143b7d3711c6f0ccf9b1dca903d1bfa1d896f1238c41627d5b5238";
        setUpWireMockForCall("invokescript", "invokescript_transfer_1_gas.json",
                script, "969a77db482f74ce27105f760efa139223431394");
        // Required for fetching the token's decimals.
        setUpWireMockForInvokeFunction(
                "decimals", "invokefunction_decimals_gas.json");
        // Required for fetching the block height used for setting the validUntilBlock.
        setUpWireMockForGetBlockCount(1000);
        // Required when checking the senders token balance.
        setUpWireMockForInvokeFunction("balanceOf",
                "invokefunction_balanceOf.json");

        byte[] privateKey = Numeric.hexStringToByteArray(
                "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3");
        Account a = new Account(ECKeyPair.create(privateKey));
        Wallet w = Wallet.withAccounts(a);
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");
        Invocation i = gasToken.buildTransferInvocation(w, receiver, BigDecimal.ONE);

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
        assertThat(tx.getScript(), is(Numeric.hexStringToByteArray(script)));
        assertThat(tx.getWitnesses().get(0).getVerificationScript(),
                is(w.getDefaultAccount().getVerificationScript()));
    }

    @Test
    public void getName() throws IOException {
        setUpWireMockForInvokeFunction("name", "invokefunction_name.json");
        assertThat(neoToken.getName(), is("NEO"));
    }

    @Test
    public void getSymbol() throws IOException {
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        assertThat(neoToken.getSymbol(), is("neo"));
    }

    @Test
    public void getDecimals() throws Exception {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        assertThat(gasToken.getDecimals(), is(8));
    }

    @Test
    public void getTotalSupply() throws Exception {
        setUpWireMockForInvokeFunction("totalSupply", "invokefunction_totalSupply.json");
        assertThat(gasToken.getTotalSupply(), is(new BigInteger("3000000000000000")));
    }

    @Test
    public void getBalanceOfAccount() throws Exception {
        ScriptHash acc = ScriptHash.fromAddress("AMRZWegpH58nwY3iSDbmbBGg3kfGH6RgRt");
        setUpWireMockForInvokeFunction("balanceOf", "invokefunction_balanceOf.json");
        assertThat(gasToken.getBalanceOf(acc), is(new BigInteger("3000000000000000")));
    }

    @Test
    public void getBalanceOfWallet() throws Exception {
        Account a1 = Account.fromAddress("AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm");
        Account a2 = Account.fromAddress("Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ");
        setUpWireMockForBalanceOf(a1.getScriptHash(),
                "invokefunction_balanceOf_AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm.json");
        setUpWireMockForBalanceOf(a2.getScriptHash(),
                "invokefunction_balanceOf_Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ.json");
        Wallet w = Wallet.withAccounts(a1, a2);
        assertThat(gasToken.getBalanceOf(w), is(new BigInteger("411285799730")));
    }

    @Test(expected = InsufficientFundsException.class)
    public void failTransferringGasBecauseOfInsufficientBalance() throws Exception {
        setUpWireMockForSendRawTransaction();
        // Required for fetching of system fee of the invocation.
        setUpWireMockForInvokeFunction(
                "transfer", "invokescript_transfer_1_gas.json");
        // Required for fetching the token's decimals.
        setUpWireMockForInvokeFunction(
                "decimals", "invokefunction_decimals_gas.json");
        // Required for fetching the block height used for setting the validUntilBlock.
        setUpWireMockForGetBlockCount(1000);
        // Required for checking the senders token balance.
        setUpWireMockForCall("invokefunction",
                "invokefunction_balanceOf_Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ.json",
                "8c23f196d8a1bfd103a9dcb1f9ccf0c611377d3b",
                "balanceOf",
                "df133e846b1110843ac357fc8bbf05b4a32e17c8");

        byte[] privateKey = Numeric.hexStringToByteArray(
                "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9");
        Account a = new Account(ECKeyPair.create(privateKey));
        Wallet w = Wallet.withAccounts(a);
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");
        gasToken.buildTransferInvocation(w, receiver, new BigDecimal("4"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransfer_negativeAmount() throws IOException {
        neoToken.transfer(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("-1"));
    }

    /*
     *  The following test cases use a wallet that contains three accounts with the following balances (unless otherwise declared):
     *      1. AXC48TRb62MQQFXWcnUXwbra2MpTNBHNyG: 5 neo (default account in the wallet)
     *      2. Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ: 4 neo
     *      3. ATpVyfpFwE2SzNGSvXDNrtRyfVLajhn7yN: 3 neo
     */

    /*
     *  In this test case, 7 neo should be paid using the full wallet.
     *  Result: Account 1 should pay 5 neo and Account 2 should pay the rest (2 neo).
     */
    @Test
    public void testTransferUsingFullWallet() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_fullWallet.json");
        // Required for fetching the block height used for setting the validUntilBlock.
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(ACCOUNT_1.getScriptHash(),
                "invokefunction_balanceOf_account1.json");
        setUpWireMockForBalanceOf(ACCOUNT_2.getScriptHash(),
                "invokefunction_balanceOf_account2.json");

        String script = "150c14941343239213fa0e765f1027ce742f48db779a960c14a91c9eab5efcdf4970e793c92f9db2beac065f8213c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52120c14941343239213fa0e765f1027ce742f48db779a960c14c8172ea3b405bf8bfc57c33a8410116b843e13df13c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52";
        Invocation invocation = neoToken.buildTransactionScript(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("7"));

        assertThat(Numeric.toHexStringNoPrefix(invocation.getTransaction().getScript()), is(script));
    }

    /*
     *  In this test case, 12 neo should be paid using the full wallet.
     *  Result: Account 1 should pay 5 neo, 2 should pay 4 neo and 3 pays the rest (3 neo).
     */
    @Test
    public void testTransferUsingFullWallet_allAccountsNeededToCoverAmount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_fullWallet_allAccounts.json");
        // Required for fetching the block height used for setting the validUntilBlock.
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(ACCOUNT_1.getScriptHash(),
                "invokefunction_balanceOf_account1.json");
        setUpWireMockForBalanceOf(ACCOUNT_2.getScriptHash(),
                "invokefunction_balanceOf_account2.json");
        setUpWireMockForBalanceOf(ACCOUNT_3.getScriptHash(),
                "invokefunction_balanceOf_account3.json");

        String script = "150c14941343239213fa0e765f1027ce742f48db779a960c14a91c9eab5efcdf4970e793c92f9db2beac065f8213c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52140c14941343239213fa0e765f1027ce742f48db779a960c14c8172ea3b405bf8bfc57c33a8410116b843e13df13c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52130c14941343239213fa0e765f1027ce742f48db779a960c148420ab25923dd9556240e98794423193cd07daf613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52";
        Invocation invocation = neoToken.buildTransactionScript(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("12"));

        assertThat(Numeric.toHexStringNoPrefix(invocation.getTransaction().getScript()), is(script));
    }

    /*
     *  In this test case, 5 neo should be paid using the full wallet.
     *  Result: Account 1 should pay 5 neo.
     */
    @Test
    public void testTransferUsingFullWallet_defaultAccountCoversAmount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_fullWallet_defaultAccount.json");
        // Required for fetching the block height used for setting the validUntilBlock.
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(ACCOUNT_1.getScriptHash(),
                "invokefunction_balanceOf_account1.json");

        String script = "150c14941343239213fa0e765f1027ce742f48db779a960c14a91c9eab5efcdf4970e793c92f9db2beac065f8213c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52";
        Invocation invocation = neoToken.buildTransactionScript(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("5"));

        assertThat(Numeric.toHexStringNoPrefix(invocation.getTransaction().getScript()), is(script));
    }

    /*
     *  In this test case, 1 neo should be paid using the full wallet.
     *  Only for this test, the default and the second account are not holding any neo.
     *  Result: Account 3 should pay 1 neo.
     */
    @Test
    public void testTransferUsingFullWallet_defaultAccountHasNoBalance() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_fullWallet_defaultNoBalance.json");
        // Required for fetching the block height used for setting the validUntilBlock.
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(ACCOUNT_1.getScriptHash(),
                "invokefunction_balanceOf_account1_noBalance.json");
        setUpWireMockForBalanceOf(ACCOUNT_2.getScriptHash(),
                "invokefunction_balanceOf_account2_noBalance.json");
        setUpWireMockForBalanceOf(ACCOUNT_3.getScriptHash(),
                "invokefunction_balanceOf_account3.json");

        String script = "110c14941343239213fa0e765f1027ce742f48db779a960c148420ab25923dd9556240e98794423193cd07daf613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52";
        Invocation invocation = neoToken.buildTransactionScript(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("1"));

        assertThat(Numeric.toHexStringNoPrefix(invocation.getTransaction().getScript()), is(script));
    }

    @Test(expected = InsufficientFundsException.class)
    public void testTransferUsingFullWallet_insufficientBalance() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        setUpWireMockForBalanceOf(ACCOUNT_1.getScriptHash(),
                "invokefunction_balanceOf_account1.json");
        setUpWireMockForBalanceOf(ACCOUNT_2.getScriptHash(),
                "invokefunction_balanceOf_account2.json");
        setUpWireMockForBalanceOf(ACCOUNT_3.getScriptHash(),
                "invokefunction_balanceOf_account3.json");

        neoToken.buildTransactionScript(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("20"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferUsingFullWallet_illegalAmountProvided() throws IOException {
        neoToken.transferUsingFullWallet(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("-2"));
    }

    /*
     *  In this test case 5 neo should be paid with accounts 3 and 2 (order matters!).
     *  Result: Account 3 should pay 3 neo and account 2 should pay 2 neo.
     */
    @Test
    public void testTransferUsingSpecificAddresses() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_specificAddresses.json");
        // Required for fetching the block height used for setting the validUntilBlock.
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        setUpWireMockForBalanceOf(ACCOUNT_2.getScriptHash(),
                "invokefunction_balanceOf_account2.json");
        setUpWireMockForBalanceOf(ACCOUNT_3.getScriptHash(),
                "invokefunction_balanceOf_account3.json");

        String script = "130c14941343239213fa0e765f1027ce742f48db779a960c148420ab25923dd9556240e98794423193cd07daf613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52120c14941343239213fa0e765f1027ce742f48db779a960c14c8172ea3b405bf8bfc57c33a8410116b843e13df13c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52";
        Invocation invocation = neoToken.buildTransactionScript(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("5"),
                ACCOUNT_3.getScriptHash(), ACCOUNT_2.getScriptHash());

        assertThat(Numeric.toHexStringNoPrefix(invocation.getTransaction().getScript()), is(script));
    }

    /*
     *  In this test case 4 neo should be paid with accounts 2 and 3 (order matters!).
     *  Result: Account 2 should pay 4 neo.
     */
    @Test
    public void testTransferUsingSpecificAddresses_firstAccountCoversAmount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_specificAddresses_firstCoversAmount.json");
        // Required for fetching the block height used for setting the validUntilBlock.
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        setUpWireMockForBalanceOf(ACCOUNT_2.getScriptHash(),
                "invokefunction_balanceOf_account2.json");
        setUpWireMockForBalanceOf(ACCOUNT_3.getScriptHash(),
                "invokefunction_balanceOf_account3.json");

        String script = "140c14941343239213fa0e765f1027ce742f48db779a960c14c8172ea3b405bf8bfc57c33a8410116b843e13df13c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52";
        Invocation invocation = neoToken.buildTransactionScript(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("4"),
                ACCOUNT_2.getScriptHash(), ACCOUNT_3.getScriptHash());

        assertThat(Numeric.toHexStringNoPrefix(invocation.getTransaction().getScript()), is(script));
    }

    /*
     *  In this test case 1 neo should be paid with accounts 2 and 3, whereas account 2 holds no neo.
     *  Result: Account 3 should pay 1 neo.
     */
    @Test
    public void testTransferUsingSpecificAddresses_firstConsideredAccountHasNoBalance() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_specificAddresses_firstNoBalance.json");
        // Required for fetching the block height used for setting the validUntilBlock.
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        setUpWireMockForBalanceOf(ACCOUNT_2.getScriptHash(),
                "invokefunction_balanceOf_account2_noBalance.json");
        setUpWireMockForBalanceOf(ACCOUNT_3.getScriptHash(),
                "invokefunction_balanceOf_account3.json");

        String script = "110c14941343239213fa0e765f1027ce742f48db779a960c148420ab25923dd9556240e98794423193cd07daf613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52";
        Invocation invocation = neoToken.buildTransactionScript(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("1"),
                ACCOUNT_2.getScriptHash(), ACCOUNT_3.getScriptHash());

        assertThat(Numeric.toHexStringNoPrefix(invocation.getTransaction().getScript()), is(script));
    }

    /*
     *  In this test case, 12 neo should be paid using only accounts 1 and 3.
     *  Result: This should fail, since accounts 1 and 3 only hold 8 neo in total.
     */
    @Test(expected = InsufficientFundsException.class)
    public void testTransferUsingSpecificAddresses_insufficientBalance() throws IOException {
        // Required for fetching the block height used for setting the validUntilBlock.
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        setUpWireMockForBalanceOf(ACCOUNT_1.getScriptHash(),
                "invokefunction_balanceOf_account1.json");
        setUpWireMockForBalanceOf(ACCOUNT_3.getScriptHash(),
                "invokefunction_balanceOf_account3.json");

        neoToken.buildTransactionScript(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("12"),
                ACCOUNT_1.getScriptHash(), ACCOUNT_3.getScriptHash());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferUsingSpecificAddresses_noAddressProvided() throws IOException {
        neoToken.transferUsingSpecificAddresses(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferUsingSpecificAddresses_illegalAmountProvided() throws IOException {
        neoToken.transferUsingSpecificAddresses(WALLET, RECIPIENT_SCRIPT_HASH, new BigDecimal("-2"),
                ACCOUNT_1.getScriptHash());
    }
}
