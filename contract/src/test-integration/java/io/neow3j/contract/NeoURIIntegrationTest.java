package io.neow3j.contract;

import static io.neow3j.NeoTestContainer.getNodeUrl;
import static io.neow3j.contract.IntegrationTestHelper.GAS_HASH;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_2;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_WALLET;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithNeo;
import static io.neow3j.contract.IntegrationTestHelper.CLIENTS_WALLET;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.NeoTestContainer;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.Account;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NeoURIIntegrationTest {

    private static Neow3j neow3j;
    private static NeoToken neoToken;
    private static GasToken gasToken;

    @ClassRule
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeClass
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(getNodeUrl(neoTestContainer)));
        neoToken = new NeoToken(neow3j);
        gasToken = new GasToken(neow3j);
        fundAccountsWithGas(neow3j, CLIENT_1, CLIENT_2);
    }

    @Test
    public void testTransferAssetFromBuiltURI() throws Throwable {
        Account account = Account.create();
        BigInteger gasBalance = gasToken.getBalanceOf(account);
        assertThat(gasBalance, is(BigInteger.ZERO));

        Hash256 txHash = new NeoURI(neow3j)
                .token(GAS_HASH)
                .wallet(COMMITTEE_WALLET)
                .to(account.getScriptHash())
                .amount("0.00000001")
                .buildTransfer()
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        gasBalance = gasToken.getBalanceOf(account);
        assertThat(gasBalance, is(new BigInteger("1")));
    }

    @Test
    public void testCreateTransactionFromURIString() throws Throwable {
        Account account = Account.create();
        BigInteger gasBalance = gasToken.getBalanceOf(account);
        assertThat(gasBalance, is(BigInteger.ZERO));

        String uriString = "neo:" + account.getAddress() + "?asset=gas&amount=0.0000005";

        Hash256 txHash = NeoURI.fromURI(uriString)
                .neow3j(neow3j)
                .wallet(COMMITTEE_WALLET)
                .buildTransfer()
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        gasBalance = gasToken.getBalanceOf(account);
        assertThat(gasBalance, is(new BigInteger("50")));
    }

    @Test
    public void testCreateTransactionFromURIString_neo() throws Throwable {
        Account account = Account.create();
        BigInteger neoBalance = neoToken.getBalanceOf(account);
        assertThat(neoBalance, is(BigInteger.ZERO));

        fundAccountsWithNeo(neow3j, BigDecimal.TEN, CLIENT_2);

        String uriString = "neo:" + account.getAddress() + "?asset=neo&amount=1";

        Hash256 txHash = NeoURI.fromURI(uriString)
                .neow3j(neow3j)
                .wallet(CLIENTS_WALLET)
                .buildTransfer()
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        neoBalance = neoToken.getBalanceOf(account);
        assertThat(neoBalance, is(new BigInteger("1")));
    }

}
