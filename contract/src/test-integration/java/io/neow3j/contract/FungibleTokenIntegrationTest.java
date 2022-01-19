package io.neow3j.contract;

import io.neow3j.test.NeoTestContainer;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash256;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.wallet.Account;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithNeo;
import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigInteger;

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
        fundAccountsWithGas(neow3j, CLIENT_1);
    }

    @Test
    public void testTransfer() throws Throwable {
        fundAccountsWithNeo(neow3j, BigInteger.valueOf(15), CLIENT_1);
        BigInteger balance = fungibleToken.getBalanceOf(CLIENT_1);
        assertThat(balance, is(new BigInteger("15")));

        Hash256 txHash = fungibleToken
                .transfer(CLIENT_1, COMMITTEE_ACCOUNT.getScriptHash(), new BigInteger("14"))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        balance = fungibleToken.getBalanceOf(CLIENT_1);
        assertThat(balance, is(new BigInteger("1")));
    }

    @Test
    public void testBuildTransfers() throws Throwable {
        Account account1 = Account.fromWIF("L4tavAxwsrngrEWYc7HLcWyNKCBcKQcJA7nKToyu6U29TC75nWXd");
        Account account2 = Account.fromWIF("L3EJ7radVCA3a2rTqBjZS9JnRN6ptZ1oseqF8NKXM483iLm1ME1F");
        BigInteger balance1 = fungibleToken.getBalanceOf(account1);
        BigInteger balance2 = fungibleToken.getBalanceOf(account2);
        assertThat(balance1, is(BigInteger.ZERO));
        assertThat(balance2, is(BigInteger.ZERO));

        BigInteger amount1 = BigInteger.valueOf(133);
        BigInteger amount2 = BigInteger.valueOf(234);

        byte[] transferScript1 = fungibleToken
                .buildTransferScript(COMMITTEE_ACCOUNT.getScriptHash(), account1.getScriptHash(),
                        amount1, null);
        byte[] transferScript2 = fungibleToken
                .buildTransferScript(COMMITTEE_ACCOUNT.getScriptHash(), account2.getScriptHash(),
                        amount2, null);

        TransactionBuilder b = new TransactionBuilder(neow3j)
                .script(transferScript1)
                .extendScript(transferScript2);

        byte[] script = b.getScript();
        assertThat(script, is(ArrayUtils.concatenate(transferScript1, transferScript2)));

        Transaction tx = b.signers(calledByEntry(COMMITTEE_ACCOUNT)).getUnsignedTransaction();
        tx.addMultiSigWitness(COMMITTEE_ACCOUNT.getVerificationScript(),
                signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair()));
        Hash256 txHash = tx.send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        balance1 = fungibleToken.getBalanceOf(account1);
        balance2 = fungibleToken.getBalanceOf(account2);
        assertThat(balance1, is(amount1));
        assertThat(balance2, is(amount2));
    }

}
