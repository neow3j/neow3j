package io.neow3j.contract;

import static io.neow3j.NeoTestContainer.getNodeUrl;
import static io.neow3j.contract.IntegrationTestHelper.client1;
import static io.neow3j.contract.IntegrationTestHelper.client2;
import static io.neow3j.contract.IntegrationTestHelper.committee;
import static io.neow3j.contract.IntegrationTestHelper.committeeWallet;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.transaction.Signer.calledByEntry;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.NeoTestContainer;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.Account;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

public class PolicyContractIntegrationTest {

    private static Neow3j neow3j;
    private static PolicyContract policyContract;

    @ClassRule
    public static NeoTestContainer neoTestContainer =
            new NeoTestContainer("/node-config/config.json");

    @BeforeClass
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(getNodeUrl(neoTestContainer)));
        policyContract = new PolicyContract(neow3j);
        // make a transaction that can be used for the tests
        fundAccountsWithGas(neow3j, client1, client2);
    }

    @Test
    public void testSetAndGetFeePerByte() throws Throwable {
        BigInteger expectedInitialFeePerByte = new BigInteger("1000");
        BigInteger expectedNewFeePerByte = new BigInteger("2500");

        BigInteger feePerByte = policyContract.getFeePerByte();
        assertThat(feePerByte, is(expectedInitialFeePerByte));

        Hash256 txHash = policyContract.setFeePerByte(2500)
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        BigInteger newFeePerByte = policyContract.getFeePerByte();
        assertThat(newFeePerByte, is(expectedNewFeePerByte));
    }

    @Test
    public void testSetAndGetExecFeeFactor() throws Throwable {
        BigInteger expectedInitialExecFeeFactor = new BigInteger("30");
        BigInteger expectedNewExecFeeFactor = new BigInteger("50");

        BigInteger execFeeFactor = policyContract.getExecFeeFactor();
        assertThat(execFeeFactor, is(expectedInitialExecFeeFactor));

        Hash256 txHash = policyContract.setExecFeeFactor(50)
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        BigInteger newExecFeeFactor = policyContract.getExecFeeFactor();
        assertThat(newExecFeeFactor, is(expectedNewExecFeeFactor));
    }

    @Test
    public void testSetAndGetStoragePrice() throws Throwable {
        BigInteger expectedInitialStoragePrice = new BigInteger("100000");
        BigInteger expectedNewStoragePrice = new BigInteger("300000");

        BigInteger feePerByte = policyContract.getStoragePrice();
        assertThat(feePerByte, is(expectedInitialStoragePrice));

        Hash256 txHash = policyContract.setStoragePrice(300000)
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        BigInteger newFeePerByte = policyContract.getStoragePrice();
        assertThat(newFeePerByte, is(expectedNewStoragePrice));
    }

    @Test
    public void testBlockAndUnblockAccount() throws Throwable {
        Account blockAccount =
                Account.fromWIF("Kz7mT4rHmHg25k8SUzNhMoibJEwFxEHmq4cHWU6NygsJPh5zEhFK");

        boolean isBlocked = policyContract.isBlocked(blockAccount.getScriptHash());
        assertFalse(isBlocked);

        Hash256 txHash = policyContract.blockAccount(blockAccount.getScriptHash())
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        isBlocked = policyContract.isBlocked(blockAccount.getScriptHash());
        assertTrue(isBlocked);

        txHash = policyContract.unblockAccount(blockAccount.getScriptHash())
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        isBlocked = policyContract.isBlocked(blockAccount.getScriptHash());
        assertFalse(isBlocked);
    }

    @Test
    public void testBlockAndUnblockAccount_fromAddress() throws Throwable {
        Account blockAccount =
                Account.fromWIF("Kz7mT4rHmHg25k8SUzNhMoibJEwFxEHmq4cHWU6NygsJPh5zEhFK");

        boolean isBlocked = policyContract.isBlocked(blockAccount.getScriptHash());
        assertFalse(isBlocked);

        Hash256 txHash = policyContract.blockAccount(blockAccount.getAddress())
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        isBlocked = policyContract.isBlocked(blockAccount.getScriptHash());
        assertTrue(isBlocked);

        txHash = policyContract.unblockAccount(blockAccount.getAddress())
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        isBlocked = policyContract.isBlocked(blockAccount.getScriptHash());
        assertFalse(isBlocked);
    }

}
