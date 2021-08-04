package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_2;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithNeo;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FungibleTokenIntegrationTest {

    private static Neow3j neow3j;
    private static FungibleToken fungibleToken;

    @ClassRule
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeClass
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
        fungibleToken = new FungibleToken(NEO_HASH, neow3j);
        // make a transaction that can be used for the tests
        fundAccountsWithGas(neow3j, CLIENT_1, CLIENT_2);
    }

    /*
     * In this test case 14 NEO should be transferred from all accounts.
     * Result: Account 1 should transfer 10 NEO and the remaining accounts should transfer 4 NEO.
     * Either account 2 or account 3 should transfer 4 NEO - the order of the remaining accounts
     * is not enforced.
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

        fundAccountsWithNeo(neow3j, BigInteger.TEN, a1, a2, a3);
        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(new BigInteger("30")));

        Hash256 txHash = fungibleToken
                .transfer(wallet,
                        COMMITTEE_ACCOUNT.getScriptHash(),
                        new BigInteger("14"))
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

        fundAccountsWithNeo(neow3j, new BigInteger("8"), a1, a2, a3);
        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(new BigInteger("24")));

        Hash256 txHash = fungibleToken
                .transfer(wallet,
                        COMMITTEE_ACCOUNT.getScriptHash(),
                        new BigInteger("8"))
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
     * Result: Account 1, 2 and 3 should transfer 12 NEO each.
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

        fundAccountsWithNeo(neow3j, new BigInteger("12"), a1, a2, a3);
        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(new BigInteger("36")));

        Hash256 txHash = fungibleToken
                .transfer(wallet,
                        COMMITTEE_ACCOUNT.getScriptHash(),
                        new BigInteger("36"))
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
     * In this test case 16 NEO should be transferred from accounts 3 and 2 (order matters!).
     * Result: Account 3 should transfer 10 NEO and account 2 should transfer 6 NEO.
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

        fundAccountsWithNeo(neow3j, new BigInteger("10"), a1, a2, a3);
        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(new BigInteger("30")));

        Hash256 txHash = fungibleToken
                .transferFromSpecificAccounts(wallet,
                        COMMITTEE_ACCOUNT.getScriptHash(),
                        new BigInteger("16"),
                        a3.getScriptHash(),
                        a2.getScriptHash())
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        BigInteger balanceOf_a1 = fungibleToken.getBalanceOf(a1);
        BigInteger balanceOf_a2 = fungibleToken.getBalanceOf(a2);
        BigInteger balanceOf_a3 = fungibleToken.getBalanceOf(a3);

        assertThat(balanceOf_wallet, is(new BigInteger("14")));
        assertThat(balanceOf_a1, is(new BigInteger("10")));
        assertThat(balanceOf_a2, is(new BigInteger("4")));
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

        fundAccountsWithNeo(neow3j, new BigInteger("5"), a1, a2, a3);
        balanceOf_wallet = fungibleToken.getBalanceOf(wallet);
        assertThat(balanceOf_wallet, is(new BigInteger("15")));

        Hash256 txHash = fungibleToken
                .transferFromDefaultAccount(wallet,
                        COMMITTEE_ACCOUNT.getScriptHash(),
                        new BigInteger("5"))
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

}
