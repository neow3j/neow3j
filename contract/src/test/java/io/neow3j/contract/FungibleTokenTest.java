package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.TestProperties.gasTokenHash;
import static io.neow3j.TestProperties.neoTokenHash;
import static io.neow3j.contract.ContractParameter.any;
import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForBalanceOf;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForGetBlockCount;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.wallet.Account.createMultiSigAccount;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FungibleTokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private FungibleToken neoToken;
    private FungibleToken gasToken;
    private Account account1;
    private Account account2;
    private Account account3;
    private Account multiSigAccount;
    private static final Hash160 RECIPIENT_SCRIPT_HASH =
            new Hash160("969a77db482f74ce27105f760efa139223431394");

    private static final String NEP17_TRANSFER = "transfer";

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);
        Neow3j neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));

        neoToken = new FungibleToken(new Hash160(neoTokenHash()), neow);
        gasToken = new FungibleToken(new Hash160(gasTokenHash()), neow);

        account1 = new Account(ECKeyPair.create(
                Numeric.hexStringToByteArray(
                        "1dd37fba80fec4e6a6f13fd708d8dcb3b29def768017052f6c930fa1c5d90bbb")));
        account2 = new Account(ECKeyPair.create(
                Numeric.hexStringToByteArray(
                        "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9")));
        account3 = new Account(ECKeyPair.create(
                Numeric.hexStringToByteArray(
                        "3a100280baf46ea7db17bc01b53365891876b4a2db11028dbc1ccb8c782725f8")));
        multiSigAccount = createMultiSigAccount(
                asList(account1.getECKeyPair().getPublicKey(),
                        account2.getECKeyPair().getPublicKey(),
                        account3.getECKeyPair().getPublicKey()
                ), 2);
    }

    @Test
    public void transferFromDefaultAccountShouldAddAccountAsSigner() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_300000000.json");
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");

        Transaction tx = gasToken.transferFromDefaultAccount(
                Wallet.withAccounts(account1), RECIPIENT_SCRIPT_HASH, BigDecimal.ONE)
                .buildTransaction();

        assertThat(tx.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes().get(0), is(WitnessScope.CALLED_BY_ENTRY));
        assertThat(tx.getSender(), is(account1.getScriptHash()));
    }

    @Test
    public void transferFromDefaultAccountShouldCreateTheCorrectScript() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        setUpWireMockForInvokeFunction("balanceOf", "invokefunction_balanceOf_300000000.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(gasTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account1.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(100000000),
                                any(null)))
                .toArray();

        Transaction tx = gasToken.transferFromDefaultAccount(
                Wallet.withAccounts(account1, account2), RECIPIENT_SCRIPT_HASH, BigDecimal.ONE)
                .buildTransaction();

        assertThat(tx.getScript(), is(expectedScript));
    }

    @Test
    public void transferFromDefaultAccountShouldCreateTheCorrectScript_dataParam()
            throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        setUpWireMockForInvokeFunction("balanceOf", "invokefunction_balanceOf_300000000.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(gasTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account1.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(100000000),
                                integer(42)))
                .toArray();

        Transaction tx = gasToken.transferFromDefaultAccount(
                Wallet.withAccounts(account1, account2),
                RECIPIENT_SCRIPT_HASH,
                BigDecimal.ONE, integer(42))
                .buildTransaction();

        assertThat(tx.getScript(), is(expectedScript));
    }

    @Test
    public void transferFromSpecificAccount_withDataParam() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        setUpWireMockForInvokeFunction("balanceOf", "invokefunction_balanceOf_300000000.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(gasTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account1.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(100000000), // 1 GAS
                                hash160(account1.getScriptHash())))
                .toArray();

        Transaction tx = gasToken.transferFromSpecificAccounts(
                Wallet.withAccounts(account1, account2),
                RECIPIENT_SCRIPT_HASH,
                BigDecimal.ONE,
                hash160(account1.getScriptHash()),
                account1.getScriptHash())
                .buildTransaction();

        assertThat(tx.getScript(), is(expectedScript));
    }

    @Test
    public void testGetBalanceOfAccount() throws Exception {
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_300000000.json");
        assertThat(gasToken.getBalanceOf(account1.getScriptHash()),
                is(new BigInteger("300000000")));
    }

    @Test
    public void testGetBalanceOfAccount_address() throws Exception {
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_300000000.json");
        assertThat(gasToken.getBalanceOf(account1), is(new BigInteger("300000000")));
    }

    @Test
    public void testGetBalanceOfAccount_account() throws Exception {
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_300000000.json");
        assertThat(gasToken.getBalanceOf(account1), is(new BigInteger("300000000")));
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

    @Test
    public void testFailTransferFromDefaultAccount_InsufficientBalance() throws Exception {
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_300000000.json");
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");

        exceptionRule.expect(InsufficientFundsException.class);
        exceptionRule.expectMessage("default account does not hold enough tokens");
        gasToken.transferFromDefaultAccount(Wallet.withAccounts(account1), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("4"));
    }

    @Test
    public void testTransferFromDefaultAccount_negativeAmount() throws IOException {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("amount must be greater than or equal to 0");
        neoToken.transferFromDefaultAccount(Wallet.withAccounts(account1), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("-1"));
    }

    @Test
    public void testTransferInvalidDecimalsInAmount() throws Throwable {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("amount contains more decimal places than this token can " +
                "handle");
        neoToken.transferFromDefaultAccount(Wallet.withAccounts(account1), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("0.1"));
    }

    @Test
    public void testTransferInvalidDecimalsInAmount_trailingZeros() throws IOException {
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_300000000.json");
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");

        try {
            // Trailing zeros should be ignored - this code should not produce any exception.
            neoToken.transferFromDefaultAccount(Wallet.withAccounts(account1),
                    RECIPIENT_SCRIPT_HASH, new BigDecimal("1.0"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testInvalidDecimals_TransferFromDefaultAccount() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("amount contains more decimal places than this token can " +
                "handle");
        neoToken.transferFromDefaultAccount(Wallet.withAccounts(account1), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("1.1"));
    }

    @Test
    public void testInvalidDecimals_TransferFromSpecificAccounts() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("amount contains more decimal places than this token can " +
                "handle");
        gasToken.transferFromSpecificAccounts(Wallet.withAccounts(account1), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("0.0000000002"), account1.getScriptHash());
    }

    @Test
    public void testInvalidDecimals_Transfer() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("amount contains more decimal places than this token can " +
                "handle");
        neoToken.transfer(Wallet.withAccounts(account1), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("0.2"));
    }

    /*
     * The following test cases use a wallet that contains three accounts with the following
     * balances (unless otherwise declared):
     *     1. NTrezR3C4X8aMLVg7vozt5wguyNfFhwuFx: 5 neo (default account in the wallet)
     *     2. NT8qbZozQoSHwTkTE2TEYQP4vMt7pD9QWg: 4 neo
     *     3. Ng1xVxVM1mfbEv8xaqS3mcKPvxcgb9AbpC: 3 neo
     */

    /*
     * In this test case, 7 NEO should be transferred.
     * Result: Account 1 should transfer 5 NEO and Account 3 should transfer the rest (2 NEO).
     * Note: The account used for transferring the remaining 2 NEO is not fixed. In this test
     * account 3 is used, because the accounts are sorted by their Hash160 and account 3 comes
     * before account 2.
     */
    @Test
    public void testTransferWithTheFirstTwoAccountsNeededToCoverAmount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_5.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_4.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account1.getScriptHash()), // from
                                hash160(RECIPIENT_SCRIPT_HASH), // to
                                integer(5), // amount
                                any(null))) // data
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account3.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(2),
                                any(null)))
                .toArray();

        TransactionBuilder b = neoToken.transfer(Wallet.withAccounts(account1,
                account2, account3), RECIPIENT_SCRIPT_HASH, new BigDecimal("7"));

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testTransferWithTheFirstTwoAccountsNeededToCoverAmount_RecipientAsAddress()
            throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_5.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_4.json");

        // The accounts are ordered by script hash (but the default account is always first) and
        // then used in that order to cover the amount.
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account1.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(5),
                                any(null)))
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account3.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(2),
                                any(null)))
                .toArray();

        TransactionBuilder b = neoToken.transfer(Wallet.withAccounts(account1, account2, account3),
                RECIPIENT_SCRIPT_HASH, new BigDecimal("7"));

        assertThat(b.getScript(), is(expectedScript));
    }

    /*
     * In this test case, 12 NEO should be transferred.
     * Result: Account 1 should transfer 5 NEO, 2 should transfer 4 NEO and 3 should transfer 3
     * NEO.
     */
    @Test
    public void testTransfer_allAccountsNeededToCoverAmount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_5.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_4.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_3.json");

        // The accounts are ordered by script hash (but the default account is always first) and
        // then used in that order to cover the amount.
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER, asList(
                        hash160(account1.getScriptHash()),
                        hash160(RECIPIENT_SCRIPT_HASH),
                        integer(5),
                        any(null)))
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER, asList(
                        hash160(account3.getScriptHash()),
                        hash160(RECIPIENT_SCRIPT_HASH),
                        integer(3),
                        any(null)))
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER, asList(
                        hash160(account2.getScriptHash()),
                        hash160(RECIPIENT_SCRIPT_HASH),
                        integer(4),
                        any(null)))
                .toArray();

        TransactionBuilder b = neoToken.transfer(Wallet.withAccounts(account1, account2, account3),
                RECIPIENT_SCRIPT_HASH, new BigDecimal("12"));

        assertThat(b.getScript(), is(expectedScript));
    }

    /*
     * In this test case, 4 NEO should be transferred.
     * Result: Account 1 should transfer 5 NEO.
     */
    @Test
    public void testTransfer_defaultAccountCoversAmount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_5.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        new Hash160(neoTokenHash()),
                        NEP17_TRANSFER,
                        asList(hash160(account1.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(4),
                                any(null)))
                .toArray();

        TransactionBuilder b = neoToken.transfer(Wallet.withAccounts(account1, account2),
                RECIPIENT_SCRIPT_HASH, new BigDecimal("4"));

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testTransfer_defaultAccountCoversAmount_dataParam() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_5.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(
                        new Hash160(neoTokenHash()),
                        NEP17_TRANSFER,
                        asList(hash160(account1.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(4),
                                byteArray(new byte[]{0x42})))
                .toArray();

        TransactionBuilder b = neoToken.transfer(
                Wallet.withAccounts(account1, account2),
                RECIPIENT_SCRIPT_HASH,
                new BigDecimal("4"),
                byteArray(new byte[]{0x42}));

        assertThat(b.getScript(), is(expectedScript));
    }

    /*
     * In this test case, 1 NEO should be transferred.
     * Only for this test, the default and the second account are not holding any NEO.
     * Result: Account 3 should transfer 1 NEO.
     */
    @Test
    public void testTransfer_defaultAccountHasNoBalance() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_0.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_0.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_3.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account3.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(1),
                                any(null)))
                .toArray();

        TransactionBuilder b = neoToken.transfer(Wallet.withAccounts(account1,
                account2, account3), RECIPIENT_SCRIPT_HASH, new BigDecimal("1"));

        assertThat(b.getScript(), is(expectedScript));
    }

    /*
     * In this test case, 3 NEO should be transferred.
     * For this test, the wallet contains a multi-sig account (created from account 4, account 5
     * and account 6 with threshold 2) and only account 4 additionally. The multi-sig account is
     * the default account in this wallet.
     * Result: Multi-sig account should transfer 2 NEO and account 4 should transfer 1 NEO.
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
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(multiSigAccount.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(3),
                                any(null)))
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account1.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(2),
                                any(null)))
                .toArray();

        Wallet w = Wallet.withAccounts(multiSigAccount, account1, account2, account3);
        TransactionBuilder b = neoToken.buildMultiTransferInvocation(w, RECIPIENT_SCRIPT_HASH,
                new BigDecimal("5"), asList(multiSigAccount, account1), null);

        assertThat(b.getScript(), is(expectedScript));
    }

    /*
     * In this test case, 2 NEO should be transferred.
     * For this test, the wallet contains a multi-sig account (created from account 4, account 5
     * and account 6 with threshold 2) and only account 4 additionally.
     * The multi-sig account is the default account in this wallet.
     * Result: Account 4 should transfer 2 NEO.
     */
    @Test
    public void testTransfer_MultiSig_NotEnoughSignersPresent() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForInvokeFunction("decimals",
                "invokefunction_decimals.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_4.json");
        setUpWireMockForBalanceOf(multiSigAccount.getScriptHash(),
                "invokefunction_balanceOf_3.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account1.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(2),
                                any(null)))
                .toArray();

        TransactionBuilder b = neoToken.transfer(Wallet.withAccounts(multiSigAccount, account1),
                RECIPIENT_SCRIPT_HASH, new BigDecimal("2"));

        assertThat(b.getScript(), is(expectedScript));
    }

    /*
     * For this test, the wallet contains only a multi-sig account.
     */
    @Test
    public void testTransfer_MultiSigNotEnoughSignersPresent_NoOtherAccountPresent()
            throws IOException {
        setUpWireMockForBalanceOf(multiSigAccount.getScriptHash(),
                "invokefunction_balanceOf_3.json");
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol_neo.json");
        Wallet wallet = Wallet.withAccounts(multiSigAccount);

        exceptionRule.expect(InsufficientFundsException.class);
        exceptionRule.expectMessage("wallet does not hold enough tokens");
        neoToken.transfer(wallet, RECIPIENT_SCRIPT_HASH, new BigDecimal("2"));
    }

    @Test
    public void testTransfer_InvalidAmount() throws IOException {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("amount must be greater than or equal to 0");
        neoToken.transfer(Wallet.create(), RECIPIENT_SCRIPT_HASH, new BigDecimal(-1));
    }

    @Test
    public void testTransfer_insufficientBalance() throws IOException {
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_5.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_4.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_3.json");
        setUpWireMockForInvokeFunction("decimals",
                "invokefunction_decimals.json");
        setUpWireMockForInvokeFunction("symbol",
                "invokefunction_symbol_neo.json");

        exceptionRule.expect(InsufficientFundsException.class);
        exceptionRule.expectMessage("wallet does not hold enough tokens");
        neoToken.transfer(Wallet.withAccounts(account1, account2, account3),
                RECIPIENT_SCRIPT_HASH, new BigDecimal("20"));
    }

    /*
     * In this test case 5 NEO should be transferred from accounts 3 and 2 (order matters!).
     * Result: Account 3 should transfer 3 NEO and account 2 should transfer 2 NEO.
     */
    @Test
    public void testTransferFromSpecificAccounts() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals",
                "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_4.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_3.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account3.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(3),
                                any(null)))
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account2.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(2),
                                any(null)))
                .toArray();

        Wallet w = Wallet.withAccounts(account1, account2, account3);
        TransactionBuilder b = neoToken.buildMultiTransferInvocation(w, RECIPIENT_SCRIPT_HASH,
                new BigDecimal("5"), asList(account3, account2), null);

        assertThat(b.getScript(), is(expectedScript));
    }

    /*
     * In this test case 4 NEO should be transferred with accounts 2 and 3 (order matters!).
     * Result: Account 2 should transfer 4 NEO.
     */
    @Test
    public void testTransferFromSpecificAccounts_firstAccountCoversAmount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account2.getScriptHash(), "invokefunction_balanceOf_4.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account2.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(4),
                                any(null)))
                .toArray();

        Wallet w = Wallet.withAccounts(account1, account2, account3);
        TransactionBuilder b = neoToken.buildMultiTransferInvocation(w, RECIPIENT_SCRIPT_HASH,
                new BigDecimal("4"), asList(account2, account3), null);

        assertThat(b.getScript(), is(expectedScript));
    }

    /*
     * In this test case 1 NEO should be transferred with accounts 2 and 3, whereas account 2
     * holds no NEO.
     * Result: Account 3 should transfer 1 NEO.
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
                .contractCall(new Hash160(neoTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account3.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(1),
                                any(null)))
                .toArray();

        Wallet w = Wallet.withAccounts(account1, account2, account3);
        TransactionBuilder b = neoToken.buildMultiTransferInvocation(w, RECIPIENT_SCRIPT_HASH,
                new BigDecimal("1"), asList(account2, account3), null);

        assertThat(b.getScript(), is(expectedScript));
    }

    /*
     * For this test, the wallet contains only a multi-sig account.
     */
    @Test
    public void
    testTransferFromSpecificAccounts_MultiSigNotEnoughSignersPresent_NoOtherAccountPresent()
            throws IOException {
        setUpWireMockForBalanceOf(multiSigAccount.getScriptHash(),
                "invokefunction_balanceOf_3.json");
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        Wallet wallet = Wallet.withAccounts(multiSigAccount);

        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("does not have the corresponding private keys in the " +
                "wallet");
        neoToken.transferFromSpecificAccounts(wallet, RECIPIENT_SCRIPT_HASH, new BigDecimal("2"),
                multiSigAccount.getScriptHash());
    }

    /*
     * In this test case, 12 NEO should be transferred from only accounts 1 and 3.
     * Result: This should fail, since accounts 1 and 3 only hold 8 NEO in total.
     */
    @Test
    public void testTransferFromSpecificAccounts_insufficientBalance() throws IOException {
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(), "invokefunction_balanceOf_5.json");
        setUpWireMockForBalanceOf(account3.getScriptHash(), "invokefunction_balanceOf_3.json");
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol_neo.json");

        Wallet w = Wallet.withAccounts(account1, account2, account3);

        exceptionRule.expect(InsufficientFundsException.class);
        exceptionRule.expectMessage("wallet does not hold enough tokens");
        neoToken.buildMultiTransferInvocation(w, RECIPIENT_SCRIPT_HASH,
                new BigDecimal("12"), asList(account1, account3), null);
    }

    @Test
    public void testTransferFromSpecificAccounts_noAccountProvided() throws IOException {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("account address must be provided to build an invocation");
        neoToken.transferFromSpecificAccounts(Wallet.create(), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("1"));
    }

    @Test
    public void testTransferFromSpecificAccounts_illegalAmountProvided() throws IOException {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("amount must be greater than or equal to 0");
        neoToken.transferFromSpecificAccounts(Wallet.create(), RECIPIENT_SCRIPT_HASH,
                new BigDecimal("-2"), account1.getScriptHash());
    }

}
