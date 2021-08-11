package io.neow3j.contract;

import static io.neow3j.test.NeoTestContainer.getNodeUrl;
import static io.neow3j.contract.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithNeo;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.test.NeoTestContainer;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.types.Hash256;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

public class FungibleTokenIntegrationTest {

    private static Neow3j neow3j;
    private static FungibleToken fungibleToken;

    @ClassRule
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeClass
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(getNodeUrl(neoTestContainer)));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
        fungibleToken = new FungibleToken(NEO_HASH, neow3j);
        // make a transaction that can be used for the tests
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

}
