package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForBalanceOf;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForGetBlockCount;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.exceptions.InsufficientFundsException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Nep5TokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private Nep5Token neoToken;
    private Nep5Token gasToken;
    private Account account1;
    private Account account2;
    private Account account3;
    private Account multiSigAccount;
    private static final ScriptHash RECIPIENT_SCRIPT_HASH =
            new ScriptHash("969a77db482f74ce27105f760efa139223431394");

    private static final ScriptHash NEO_TOKEN_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final ScriptHash GAS_TOKEN_SCRIPT_HASH = GasToken.SCRIPT_HASH;
    private static final String NEP5_TRANSFER = "transfer";


    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);

        Neow3j neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        neoToken = new Nep5Token(NEO_TOKEN_SCRIPT_HASH, neow);
        gasToken = new Nep5Token(GAS_TOKEN_SCRIPT_HASH, neow);

        account1 = new Account(ECKeyPair.create(
                Numeric.hexStringToByteArray(
                        "1dd37fba80fec4e6a6f13fd708d8dcb3b29def768017052f6c930fa1c5d90bbb")));
        account2 = new Account(ECKeyPair.create(
                Numeric.hexStringToByteArray(
                        "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9")));
        account3 = new Account(ECKeyPair.create(
                Numeric.hexStringToByteArray(
                        "3a100280baf46ea7db17bc01b53365891876b4a2db11028dbc1ccb8c782725f8")));
        multiSigAccount = Account.createMultiSigAccount(Arrays.asList(
                account1.getECKeyPair().getPublicKey(),
                account2.getECKeyPair().getPublicKey(),
                account3.getECKeyPair().getPublicKey()), 2);
    }

    @Test
    public void transferFromDefaultAccountShouldAddAccountAsSigner() throws Exception {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_300000000.json");

        Invocation i = gasToken.buildTransferInvocation(Wallet.withAccounts(account1),
                RECIPIENT_SCRIPT_HASH, BigDecimal.ONE);

        Transaction tx = i.getTransaction();
        assertThat(tx.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes().get(0), is(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getSender().getScriptHash(), is(account1.getScriptHash()));
    }

    @Test
    public void transferFromDefaultAccountShouldCreateTheCorrectScript() throws Exception {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("balanceOf", "invokefunction_balanceOf_300000000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(GAS_TOKEN_SCRIPT_HASH,
                NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                        ContractParameter.integer(100000000))).toArray(); // 1 GAS

        Invocation i = gasToken.buildTransferInvocation(Wallet.withAccounts(account1, account2),
                RECIPIENT_SCRIPT_HASH, BigDecimal.ONE);

        assertThat(i.getTransaction().getScript(), is(expectedScript));
    }

    @Test
    public void testGetName() throws IOException {
        setUpWireMockForInvokeFunction("name", "invokefunction_name.json");
        assertThat(neoToken.getName(), is("NEO"));
    }

    @Test
    public void testGetSymbol() throws IOException {
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        assertThat(neoToken.getSymbol(), is("neo"));
    }

    @Test
    public void testGetDecimals() throws Exception {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        assertThat(gasToken.getDecimals(), is(8));
    }

    @Test
    public void testGetTotalSupply() throws Exception {
        setUpWireMockForInvokeFunction("totalSupply", "invokefunction_totalSupply.json");
        assertThat(gasToken.getTotalSupply(), is(new BigInteger("3000000000000000")));
    }

    @Test
    public void testGetBalanceOfAccount() throws Exception {
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_300000000.json");
        assertThat(gasToken.getBalanceOf(account1.getScriptHash()),
                is(new BigInteger("300000000")));
    }

    @Test
    public void testGetBalanceOfWallet() throws Exception {
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_300000000.json",
                "balanceOf", account1.getScriptHash().toString());
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_300000000.json",
                "balanceOf", account2.getScriptHash().toString());
        assertThat(gasToken.getBalanceOf(Wallet.withAccounts(account1, account2)),
                is(new BigInteger("600000000")));
    }

    @Test(expected = InsufficientFundsException.class)
    public void testFailTransferFromDefaultAccount_InsufficientBalance() throws Exception {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_300000000.json");
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");

        gasToken.buildTransferInvocation(Wallet.withAccounts(account1), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("4"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferFromDefaultAccount_negativeAmount() throws IOException {
        neoToken.transferFromDefaultAccount(Wallet.withAccounts(account1), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("-1"));
    }

    /*
     *  The following test cases use a wallet that contains three accounts with the following
     * balances (unless otherwise declared):
     *      1. AUnZ8SnrxFUm2esBseNyTGpHQmF9i67Ae7: 5 neo (default account in the wallet)
     *      2. AKvnACo3j78bcP8dCerxh3zEAjZVxPmJUU: 4 neo
     *      3. ANy3dJorWjWquU7EoncPM1cjZdqA2hzwHj: 3 neo
     */

    /*
     *  In this test case, 7 neo should be transferred.
     *  Result: Account 1 should transfer 5 neo and Account 2 should transfer the rest (2 neo).
     */
    @Test
    public void testTransferWithTheFirstTwoAccountsNeededToCoverAmount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_5.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_4.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NEO_TOKEN_SCRIPT_HASH,
                        NEP5_TRANSFER, Arrays.asList(
                                ContractParameter.hash160(account1.getScriptHash()),
                                ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                                ContractParameter.integer(5)))
                .contractCall(NEO_TOKEN_SCRIPT_HASH, NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account2.getScriptHash()),
                        ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                        ContractParameter.integer(2))).toArray();

        Invocation invocation = neoToken.buildTransactionScript(Wallet.withAccounts(account1,
                account2, account3), RECIPIENT_SCRIPT_HASH, new BigDecimal("7"));

        assertThat(invocation.getTransaction().getScript(), is(expectedScript));
    }

    /*
     *  In this test case, 12 neo should be transferred.
     *  Result: Account 1 should transfer 5 neo, 2 should transfer 4 neo and 3 should transfer 3
     * neo.
     */
    @Test
    public void testTransfer_allAccountsNeededToCoverAmount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_5.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_4.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_3.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NEO_TOKEN_SCRIPT_HASH, NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                        ContractParameter.integer(5)))
                .contractCall(NEO_TOKEN_SCRIPT_HASH, NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account2.getScriptHash()),
                        ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                        ContractParameter.integer(4)))
                .contractCall(NEO_TOKEN_SCRIPT_HASH, NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account3.getScriptHash()),
                        ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                        ContractParameter.integer(3)))
                .toArray();

        Invocation invocation = neoToken.buildTransactionScript(Wallet.withAccounts(account1,
                account2, account3), RECIPIENT_SCRIPT_HASH, new BigDecimal("12"));

        assertThat(invocation.getTransaction().getScript(), is(expectedScript));
    }

    /*
     *  In this test case, 5 neo should be transferred.
     *  Result: Account 1 should transfer 5 neo.
     */
    @Test
    public void testTransfer_defaultAccountCoversAmount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_5.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH,
                NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                        ContractParameter.integer(4))).toArray();

        Invocation invocation = neoToken.buildTransactionScript(Wallet.withAccounts(account1,
                account2), RECIPIENT_SCRIPT_HASH, new BigDecimal("4"));

        assertThat(invocation.getTransaction().getScript(), is(expectedScript));
    }

    /*
     *  In this test case, 1 neo should be transferred.
     *  Only for this test, the default and the second account are not holding any neo.
     *  Result: Account 3 should transfer 1 neo.
     */
    @Test
    public void testTransfer_defaultAccountHasNoBalance() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_0.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_0.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_3.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH,
                NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account3.getScriptHash()),
                        ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                        ContractParameter.integer(1))).toArray();

        Invocation invocation = neoToken.buildTransactionScript(Wallet.withAccounts(account1,
                account2, account3), RECIPIENT_SCRIPT_HASH, new BigDecimal("1"));

        assertThat(invocation.getTransaction().getScript(), is(expectedScript));
    }

    /*
     *  In this test case, 3 neo should be transferred.
     *  For this test, the wallet contains a multi-sig account (created from account 4, account 5
     *  and account 6 with
     *  threshold 2) and only account 4 additionally. The multi-sig account is the default
     * account in this wallet.
     *  Result: Multi-sig account should transfer 2 neo and account 4 should transfer 1 neo.
     */
    @Test
    public void testTransfer_MultiSig() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(multiSigAccount.getScriptHash(),
                "invokefunction_balanceOf_3.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_4.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NEO_TOKEN_SCRIPT_HASH,
                        NEP5_TRANSFER, Arrays.asList(
                                ContractParameter.hash160(multiSigAccount.getScriptHash()),
                                ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                                ContractParameter.integer(3)))
                .contractCall(NEO_TOKEN_SCRIPT_HASH,
                        NEP5_TRANSFER, Arrays.asList(
                                ContractParameter.hash160(account1.getScriptHash()),
                                ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                                ContractParameter.integer(2))).toArray();

        Invocation invocation = neoToken.buildTransactionScript(Wallet.withAccounts(multiSigAccount,
                account1, account2, account3), RECIPIENT_SCRIPT_HASH, new BigDecimal("5"),
                multiSigAccount.getScriptHash(), account1.getScriptHash());

        assertThat(invocation.getTransaction().getScript(), is(expectedScript));
    }

    /*
     *  In this test case, 2 neo should be transferred.
     *  For this test, the wallet contains a multi-sig account (created from account 4, account 5
     *  and account 6 with
     *  threshold 2) and only account 4 additionally. The multi-sig account is the default
     * account in this wallet.
     *  Result: Account 4 should transfer 2 neo.
     */
    @Test
    public void testTransfer_MultiSig_NotEnoughSignersPresent() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_4.json");
        setUpWireMockForBalanceOf(multiSigAccount.getScriptHash(),
                "invokefunction_balanceOf_3.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH,
                NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                        ContractParameter.integer(2))).toArray();

        Invocation invocation = neoToken.buildTransactionScript(Wallet.withAccounts(multiSigAccount,
                account1), RECIPIENT_SCRIPT_HASH, new BigDecimal("2"));

        assertThat(invocation.getTransaction().getScript(), is(expectedScript));
    }

    /*
     *  For this test, the wallet contains only a multi-sig account.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTransfer_MultiSigNotEnoughSignersPresent_NoOtherAccountPresent()
            throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(multiSigAccount.getScriptHash(),
                "invokefunction_balanceOf_3.json");
        Wallet wallet = Wallet.withAccounts(multiSigAccount);
        neoToken.transfer(wallet, RECIPIENT_SCRIPT_HASH, new BigDecimal("2"));
    }

    @Test(expected = InsufficientFundsException.class)
    public void testTransfer_insufficientBalance() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_5.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_4.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_3.json");

        neoToken.buildTransactionScript(Wallet.withAccounts(account1, account2, account3),
                RECIPIENT_SCRIPT_HASH, new BigDecimal("20"));
    }

    /*
     *  In this test case 5 neo should be transferred from accounts 3 and 2 (order matters!).
     *  Result: Account 3 should transfer 3 neo and account 2 should transfer 2 neo.
     */
    @Test
    public void testTransferFromSpecificAccounts() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_4.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_3.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NEO_TOKEN_SCRIPT_HASH,
                        NEP5_TRANSFER, Arrays.asList(
                                ContractParameter.hash160(account3.getScriptHash()),
                                ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                                ContractParameter.integer(3)))
                .contractCall(NEO_TOKEN_SCRIPT_HASH,
                        NEP5_TRANSFER, Arrays.asList(
                                ContractParameter.hash160(account2.getScriptHash()),
                                ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                                ContractParameter.integer(2))).toArray();

        Invocation invocation = neoToken.buildTransactionScript(Wallet.withAccounts(account1,
                account2, account3), RECIPIENT_SCRIPT_HASH, new BigDecimal("5"),
                account3.getScriptHash(), account2.getScriptHash());

        assertThat(invocation.getTransaction().getScript(), is(expectedScript));
    }

    /*
     *  In this test case 4 neo should be transferred with accounts 2 and 3 (order matters!).
     *  Result: Account 2 should transfer 4 neo.
     */
    @Test
    public void testTransferFromSpecificAccounts_firstAccountCoversAmount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_4.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH,
                NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account2.getScriptHash()),
                        ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                        ContractParameter.integer(4))).toArray();

        Invocation invocation = neoToken.buildTransactionScript(Wallet.withAccounts(account1,
                account2, account3), RECIPIENT_SCRIPT_HASH, new BigDecimal("4"),
                account2.getScriptHash(), account3.getScriptHash());

        assertThat(invocation.getTransaction().getScript(), is(expectedScript));
    }

    /*
     * In this test case 1 neo should be transferred with accounts 2 and 3, whereas account 2
     * holds no neo.
     * Result: Account 3 should transfer 1 neo.
     */
    @Test
    public void testTransferFromSpecificAccounts_firstConsideredAccountHasNoBalance()
            throws IOException {

        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_0.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_3.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NEO_TOKEN_SCRIPT_HASH, NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account3.getScriptHash()),
                        ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                        ContractParameter.integer(1))).toArray();

        Invocation invocation = neoToken.buildTransactionScript(Wallet.withAccounts(account1,
                account2, account3), RECIPIENT_SCRIPT_HASH, new BigDecimal("1"),
                account2.getScriptHash(), account3.getScriptHash());

        assertThat(invocation.getTransaction().getScript(), is(expectedScript));
    }

    @Test
    public void testTransferFromSpecificAccounts_firstAccountHasNoBalance() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_0.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_3.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH,
                NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account3.getScriptHash()),
                        ContractParameter.hash160(RECIPIENT_SCRIPT_HASH),
                        ContractParameter.integer(1))).toArray();

        Invocation invocation = neoToken.buildTransactionScript(Wallet.withAccounts(account1,
                account2, account3), RECIPIENT_SCRIPT_HASH, new BigDecimal("1"),
                account2.getScriptHash(), account3.getScriptHash());

        assertThat(invocation.getTransaction().getScript(), is(expectedScript));
    }

    /*
     *  For this test, the wallet contains only a multi-sig account.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTransferFromSpecificAccounts_MultiSigNotEnoughSignersPresent_NoOtherAccountPresent()
            throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(multiSigAccount.getScriptHash(),
                "invokefunction_balanceOf_3.json");
        Wallet wallet = Wallet.withAccounts(multiSigAccount);
        neoToken.transferFromSpecificAccounts(wallet, RECIPIENT_SCRIPT_HASH, new BigDecimal("2"),
                multiSigAccount.getScriptHash());
    }

    /*
     *  In this test case, 12 neo should be transferred from only accounts 1 and 3.
     *  Result: This should fail, since accounts 1 and 3 only hold 8 neo in total.
     */
    @Test(expected = InsufficientFundsException.class)
    public void testTransferFromSpecificAccounts_insufficientBalance() throws IOException {
        // Required for fetching the block height used for setting the validUntilBlock.
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_5.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(),
                "invokefunction_balanceOf_3.json");

        neoToken.buildTransactionScript(Wallet.withAccounts(account1, account2, account3),
                RECIPIENT_SCRIPT_HASH, new BigDecimal("12"), account1.getScriptHash(),
                account3.getScriptHash());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferFromSpecificAccounts_noAccountProvided() throws IOException {
        neoToken.transferFromSpecificAccounts(Wallet.createWallet(), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferFromSpecificAccounts_illegalAmountProvided() throws IOException {
        neoToken.transferFromSpecificAccounts(Wallet.createWallet(), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("-2"), account1.getScriptHash());
    }
}
