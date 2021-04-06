package io.neow3j.contract;

import static io.neow3j.NeoTestContainer.getNodeUrl;
import static io.neow3j.contract.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.contract.IntegrationTestHelper.client1;
import static io.neow3j.contract.IntegrationTestHelper.client2;
import static io.neow3j.contract.IntegrationTestHelper.committee;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithNeo;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.NeoTestContainer;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class FungibleTokenIntegrationTest {

    private static Neow3j neow3j;
    private static FungibleToken fungibleToken;

    @ClassRule
    public static NeoTestContainer neoTestContainer =
            new NeoTestContainer("/node-config/config.json");

    @BeforeClass
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(getNodeUrl(neoTestContainer)));
        fungibleToken = new FungibleToken(NEO_HASH, neow3j);
        // make a transaction that can be used for the tests
        fundAccountsWithGas(neow3j, client1, client2);
    }

    /*
     * In this test case 14 NEO should be transferred from all accounts.
     * Result: Account 1 should transfer 8 NEO and the remaining accounts should transfer 6 NEO.
     * In this test case either account 2 or account 3 should transfer 6 NEO - the order of the
     * remaining accounts is not enforced.
     */
    @Test
    public void testTransfer() throws Throwable {
        Account a1 = Account.create();
        Account a2 = Account.create();
        Account a3 = Account.create();
        fundAccountsWithGas(neow3j, a1, a2, a3);

        Wallet wallet = Wallet.withAccounts(a1, a2, a3);
        BigInteger balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(BigInteger.ZERO));

        fundAccountsWithNeo(neow3j, new BigDecimal(10), a1, a2, a3);
        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(new BigInteger("30")));

        Hash256 txHash =
                fungibleToken.transfer(wallet, committee.getAddress(), new BigDecimal("14"))
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        BigInteger balanceOf_a1 = fungibleToken.getBalanceOf(a1.getAddress());
        BigInteger balanceOf_a2 = fungibleToken.getBalanceOf(a2);
        BigInteger balanceOf_a3 = fungibleToken.getBalanceOf(a3);

        assertThat(balanceOf_wallet, is(new BigInteger("16")));
        assertThat(balanceOf_a1, is(BigInteger.ZERO));
        // The ordering of the non-default accounts is not fixed.
        if (balanceOf_a2.equals(new BigInteger("6"))) {
            assertThat(balanceOf_a3, is(new BigInteger("10")));
        } else {
            assertThat(balanceOf_a2, is(new BigInteger("10")));
            assertThat(balanceOf_a3, is(new BigInteger("6")));
        }
    }

    /*
     * In this test case 8 NEO should be transferred from all accounts.
     * Result: Account 1 should transfer 8 NEO.
     */
    @Test
    public void testTransfer_firstCanCoverFullAmount() throws Throwable {
        Account a1 = Account.create();
        Account a2 = Account.create();
        Account a3 = Account.create();
        fundAccountsWithGas(neow3j, a1, a2, a3);

        Wallet wallet = Wallet.withAccounts(a1, a2, a3);
        BigInteger balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(BigInteger.ZERO));

        fundAccountsWithNeo(neow3j, new BigDecimal(8), a1, a2, a3);
        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(new BigInteger("24")));

        Hash256 txHash =
                fungibleToken.transfer(wallet, committee.getScriptHash(), new BigDecimal("8"))
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        BigInteger balanceOf_a1 = fungibleToken.getBalanceOf(a1);
        BigInteger balanceOf_a2 = fungibleToken.getBalanceOf(a2);
        BigInteger balanceOf_a3 = fungibleToken.getBalanceOf(a3);

        assertThat(balanceOf_wallet, is(new BigInteger("16")));
        assertThat(balanceOf_a1, is(BigInteger.ZERO));
        assertThat(balanceOf_a2, is(new BigInteger("8")));
        assertThat(balanceOf_a3, is(new BigInteger("8")));
    }

    /*
     * In this test case 36 NEO should be transferred from all accounts.
     * Result: Account 1, 2 and 3 should transfer 8 NEO each.
     */
    @Test
    public void testTransfer_allFundsUsed_fromAddress() throws Throwable {
        Account a1 = Account.create();
        Account a2 = Account.create();
        Account a3 = Account.create();
        fundAccountsWithGas(neow3j, a1, a2, a3);

        Wallet wallet = Wallet.withAccounts(a1, a2, a3);
        BigInteger balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(BigInteger.ZERO));

        fundAccountsWithNeo(neow3j, new BigDecimal(12), a1, a2, a3);
        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(new BigInteger("36")));

        Hash256 txHash =
                fungibleToken.transfer(wallet, committee.getScriptHash(), new BigDecimal("36"))
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        BigInteger balanceOf_a1 = fungibleToken.getBalanceOf(a1);
        BigInteger balanceOf_a2 = fungibleToken.getBalanceOf(a2);
        BigInteger balanceOf_a3 = fungibleToken.getBalanceOf(a3);

        assertThat(balanceOf_wallet, is(BigInteger.ZERO));
        assertThat(balanceOf_a1, is(BigInteger.ZERO));
        assertThat(balanceOf_a2, is(BigInteger.ZERO));
        assertThat(balanceOf_a3, is(BigInteger.ZERO));
    }

    /*
     * In this test case 16 neo should be transferred from accounts 3 and 2 (order matters!).
     * Result: Account 3 should transfer 10 neo and account 2 should transfer 6 neo.
     */
    @Test
    public void testTransferFromSpecificAccounts() throws Throwable {
        Account a1 = Account.create();
        Account a2 = Account.create();
        Account a3 = Account.create();
        fundAccountsWithGas(neow3j, a1, a2, a3);

        Wallet wallet = Wallet.withAccounts(a1, a2, a3);
        BigInteger balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(BigInteger.ZERO));

        fundAccountsWithNeo(neow3j, new BigDecimal(10), a1, a2, a3);
        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(new BigInteger("30")));

        Hash256 txHash =
                fungibleToken.transferFromSpecificAccounts(wallet, committee.getScriptHash(),
                        new BigDecimal("14"), a3.getScriptHash(), a2.getScriptHash())
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        BigInteger balanceOf_a1 = fungibleToken.getBalanceOf(a1);
        BigInteger balanceOf_a2 = fungibleToken.getBalanceOf(a2);
        BigInteger balanceOf_a3 = fungibleToken.getBalanceOf(a3);

        assertThat(balanceOf_wallet, is(new BigInteger("16")));
        assertThat(balanceOf_a1, is(new BigInteger("10")));
        assertThat(balanceOf_a2, is(new BigInteger("6")));
        assertThat(balanceOf_a3, is(BigInteger.ZERO));
    }

    /*
     * In this test case 5 NEO should be transferred with the default account 1, whereas account
     * 2 and 3 should not be used.
     * Result: Account 1 should transfer 5 NEO.
     */
    @Test
    public void testTransferFromDefaultAccount() throws Throwable {
        Account a1 = Account.create();
        Account a2 = Account.create();
        Account a3 = Account.create();
        fundAccountsWithGas(neow3j, a1, a2, a3);

        Wallet wallet = Wallet.withAccounts(a1, a2, a3);
        BigInteger balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(BigInteger.ZERO));

        fundAccountsWithNeo(neow3j, new BigDecimal(5), a1, a2, a3);
        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(new BigInteger("15")));

        Hash256 txHash =
                fungibleToken.transferFromDefaultAccount(wallet, committee.getScriptHash(),
                        new BigDecimal("5"))
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        BigInteger balanceOf_a1 = fungibleToken.getBalanceOf(a1);
        BigInteger balanceOf_a2 = fungibleToken.getBalanceOf(a2);
        BigInteger balanceOf_a3 = fungibleToken.getBalanceOf(a3);

        assertThat(balanceOf_wallet, is(new BigInteger("10")));
        assertThat(balanceOf_a1, is(BigInteger.ZERO));
        assertThat(balanceOf_a2, is(new BigInteger("5")));
        assertThat(balanceOf_a3, is(new BigInteger("5")));
    }

    /*
     * Checks that the transfer method correctly uses the decimal amount to transfer.
     */
    @Test
    public void testTransfer_decimals() throws Throwable {
        Account a1 = Account.create();
        Account a2 = Account.create();
        fundAccountsWithGas(neow3j, a1);

        Wallet wallet = Wallet.withAccounts(a1); // funds the account with 10_000 Gas
        FungibleToken gasToken = new FungibleToken(GasToken.SCRIPT_HASH, neow3j);

        Hash256 txHash =
                gasToken.transfer(wallet, a2.getScriptHash(), new BigDecimal("1.00000001"))
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        BigInteger balanceOf_a2 = gasToken.getBalanceOf(a2);

        assertThat(balanceOf_a2, is(new BigInteger("100000001")));
    }

}
