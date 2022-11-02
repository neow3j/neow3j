package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnresolvableDomainNameException;
import io.neow3j.contract.types.NNSName;
import io.neow3j.helper.NeoNameServiceTestHelper;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash256;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.BigInteger;

import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_2;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithNeo;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
public class FungibleTokenIntegrationTest {

    private static Neow3j neow3j;
    private static FungibleToken fungibleToken;

    @Container
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeAll
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
        fungibleToken = new FungibleToken(NEO_HASH, neow3j);
        fundAccountsWithGas(neow3j, CLIENT_1, CLIENT_2);
        NeoNameServiceTestHelper.deployNNS(neow3j, COMMITTEE_ACCOUNT, DEFAULT_ACCOUNT);
    }

    private Neow3j getNeow3j() {
        return neow3j;
    }

    @Test
    public void testTransfer() throws Throwable {
        fundAccountsWithNeo(getNeow3j(), BigInteger.valueOf(15), CLIENT_1);
        BigInteger balance = fungibleToken.getBalanceOf(CLIENT_1);
        assertThat(balance, is(new BigInteger("15")));

        Hash256 txHash = fungibleToken
                .transfer(CLIENT_1, COMMITTEE_ACCOUNT.getScriptHash(), new BigInteger("14"))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

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

        TransactionBuilder b = new TransactionBuilder(getNeow3j())
                .script(transferScript1)
                .extendScript(transferScript2);

        byte[] script = b.getScript();
        assertThat(script, is(ArrayUtils.concatenate(transferScript1, transferScript2)));

        Transaction tx = b.signers(calledByEntry(COMMITTEE_ACCOUNT)).getUnsignedTransaction();

        tx.addMultiSigWitness(COMMITTEE_ACCOUNT.getVerificationScript(), DEFAULT_ACCOUNT);
        Hash256 txHash = tx.send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        balance1 = fungibleToken.getBalanceOf(account1);
        balance2 = fungibleToken.getBalanceOf(account2);
        assertThat(balance1, is(amount1));
        assertThat(balance2, is(amount2));
    }

    // region transfer to NNS recipient

    @Test
    public void testTransferTokenToNNSDomainName() throws Throwable {
        NNSName nnsName = new NNSName("transfertonns.neo");
        NeoNameServiceTestHelper.register(getNeow3j(), nnsName, CLIENT_2);
        NeoNameServiceTestHelper.setRecord(getNeow3j(), nnsName, RecordType.TXT, CLIENT_2.getAddress(), CLIENT_2);

        GasToken gasToken = new GasToken(getNeow3j());
        BigInteger balanceBefore = gasToken.getBalanceOf(CLIENT_2);

        BigInteger amount = gasToken.toFractions(BigDecimal.valueOf(5));
        NeoSendRawTransaction response = gasToken.transfer(CLIENT_1, nnsName, amount)
                .sign()
                .send();
        Hash256 txHash = response.getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        BigInteger balanceAfter = gasToken.getBalanceOf(CLIENT_2);
        assertThat(balanceAfter, is(balanceBefore.add(amount)));
    }

    @Test
    public void testTransferTokenToNNSDomainName_transferFromHashToFourthLevelDomainNNS() throws Throwable {
        NNSName nnsName = new NNSName("transfertonnslevels.neo");
        NeoNameServiceTestHelper.register(getNeow3j(), nnsName, CLIENT_2);
        NeoNameServiceTestHelper.setRecord(getNeow3j(), nnsName, RecordType.TXT, CLIENT_2.getAddress(), CLIENT_2);
        NNSName thirdLevelDomain = new NNSName("third." + nnsName.getName());
        NeoNameServiceTestHelper.setRecord(getNeow3j(), thirdLevelDomain, RecordType.CNAME, nnsName.getName(),
                CLIENT_2);
        NNSName fourthLevelDomain = new NNSName("fourth." + thirdLevelDomain.getName());
        NeoNameServiceTestHelper.setRecord(getNeow3j(), fourthLevelDomain, RecordType.CNAME,
                thirdLevelDomain.getName(), CLIENT_2);

        GasToken gasToken = new GasToken(getNeow3j());
        BigInteger balanceBefore = gasToken.getBalanceOf(CLIENT_2);

        BigInteger amount = gasToken.toFractions(BigDecimal.valueOf(5));
        NeoSendRawTransaction response = gasToken.transfer(CLIENT_1.getScriptHash(), fourthLevelDomain, amount)
                .signers(calledByEntry(CLIENT_1))
                .sign()
                .send();
        Hash256 txHash = response.getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        BigInteger balanceAfter = gasToken.getBalanceOf(CLIENT_2);
        assertThat(balanceAfter, is(balanceBefore.add(amount)));
    }

    @Test
    public void testTransferTokenToNNSDomainName_noAddressInTxtRecord() throws Throwable {
        NNSName nnsName = new NNSName("transfertonnsnoaddresstxtrecord.neo");
        NeoNameServiceTestHelper.register(getNeow3j(), nnsName, CLIENT_2);
        NeoNameServiceTestHelper.setRecord(getNeow3j(), nnsName, RecordType.TXT, "my name is jeff", CLIENT_2);

        GasToken gasToken = new GasToken(getNeow3j());
        BigInteger amount = gasToken.toFractions(BigDecimal.valueOf(5));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> gasToken.transfer(CLIENT_1, nnsName, amount));
        assertThat(thrown.getMessage(), is("Not a valid NEO address."));
    }

    @Test
    public void testTransferTokenToNNSDomainName_unresolvable() throws Throwable {
        NNSName nnsName = new NNSName("transfertonnsunresolvable.neo");
        NeoNameServiceTestHelper.register(getNeow3j(), nnsName, CLIENT_2);

        GasToken gasToken = new GasToken(getNeow3j());
        BigInteger amount = gasToken.toFractions(BigDecimal.valueOf(5));
        UnresolvableDomainNameException thrown = assertThrows(UnresolvableDomainNameException.class,
                () -> gasToken.transfer(CLIENT_1, nnsName, amount));
        assertThat(thrown.getMessage(),
                is(format("The provided domain name '%s' could not be resolved.", nnsName.getName())));
    }

    // endregion

}
