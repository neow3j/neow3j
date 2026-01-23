package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.transaction.Witness;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigInteger;
import java.util.Collections;

import static io.neow3j.helper.FundingHelper.CLIENT_1;
import static io.neow3j.helper.FundingHelper.CLIENT_2;
import static io.neow3j.helper.FundingHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.helper.FundingHelper.DEFAULT_ACCOUNT;
import static io.neow3j.helper.FundingHelper.fundAccountsWithGas;
import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class PolicyContractIntegrationTest {

    private static Neow3j neow3j;
    private static PolicyContract policyContract;

    @Container
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeAll
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
        policyContract = new PolicyContract(neow3j);
        fundAccountsWithGas(neow3j, CLIENT_1, CLIENT_2);
    }

    @Test
    public void testSetAndGetFeePerByte() throws Throwable {
        BigInteger expectedInitialFeePerByte = new BigInteger("1000");
        BigInteger expectedNewFeePerByte = new BigInteger("2500");

        BigInteger feePerByte = policyContract.getFeePerByte();
        assertThat(feePerByte, is(expectedInitialFeePerByte));

        Transaction tx = policyContract.setFeePerByte(new BigInteger("2500"))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
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

        // Starting from the Faun hardfork, the exec fee factor is stored with additional 4 decimals compared to
        // before. The method getExecFeeFactor although returns the same decimal representation as before the
        // hardfork. Hence, the value returned by getExecFeeFactor is expected to be the new value divided by 10,000
        // (floor). In this test, the expected return value of getExecFeeFactor after setting it to 506388 is 50.
        Transaction tx = policyContract.setExecFeeFactor(new BigInteger("506388"))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
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

        Transaction tx = policyContract.setStoragePrice(new BigInteger("300000"))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        BigInteger newFeePerByte = policyContract.getStoragePrice();
        assertThat(newFeePerByte, is(expectedNewStoragePrice));
    }

    @Test
    public void testSetAndGetMillisecondsPerBlock() throws Throwable {
        BigInteger expectedInitialMilliseconds = new BigInteger("1000");
        BigInteger expectedNewMilliseconds = new BigInteger("20000");

        BigInteger milliseconds = policyContract.getMillisecondsPerBlock();
        assertThat(milliseconds, is(expectedInitialMilliseconds));

        Transaction tx = policyContract.setMillisecondsPerBlock(expectedNewMilliseconds)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                Collections.singletonList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        BigInteger newMilliseconds = policyContract.getMillisecondsPerBlock();
        assertThat(newMilliseconds, is(expectedNewMilliseconds));
    }

    @Test
    public void testSetAndGetMaxValidUntilBlockIncrement() throws Throwable {
        BigInteger expectedInitialIncrement = new BigInteger("5760");
        BigInteger expectedNewIncrement = new BigInteger("7000");

        BigInteger increment = policyContract.getMaxValidUntilBlockIncrement();
        assertThat(increment, is(expectedInitialIncrement));

        Transaction tx = policyContract.setMaxValidUntilBlockIncrement(expectedNewIncrement)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                Collections.singletonList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        BigInteger newIncrement = policyContract.getMaxValidUntilBlockIncrement();
        assertThat(newIncrement, is(expectedNewIncrement));
    }

    @Test
    public void testSetAndGetMaxTraceableBlocks() throws Throwable {
        BigInteger expectedInitialBlocks = new BigInteger("2102400");
        BigInteger expectedNewBlocks = new BigInteger("2000000");

        BigInteger blocks = policyContract.getMaxTraceableBlocks();
        assertThat(blocks, is(expectedInitialBlocks));

        Transaction tx = policyContract.setMaxTraceableBlocks(expectedNewBlocks)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                Collections.singletonList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        BigInteger newBlocks = policyContract.getMaxTraceableBlocks();
        assertThat(newBlocks, is(expectedNewBlocks));
    }

    @Test
    public void testSetAndGetAttributeFee() throws Throwable {
        BigInteger attributeFee = policyContract.getAttributeFee(TransactionAttributeType.CONFLICTS);
        assertThat(attributeFee, is(BigInteger.ZERO));

        Transaction tx = policyContract.setAttributeFee(TransactionAttributeType.CONFLICTS, new BigInteger("300000"))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        BigInteger newAttributeFee = policyContract.getAttributeFee(TransactionAttributeType.CONFLICTS);
        assertThat(newAttributeFee, is(new BigInteger("300000")));
    }

    @Test
    public void testBlockAndUnblockAccount() throws Throwable {
        Account blockAccount =
                Account.fromWIF("Kz7mT4rHmHg25k8SUzNhMoibJEwFxEHmq4cHWU6NygsJPh5zEhFK");

        boolean isBlocked = policyContract.isBlocked(blockAccount.getScriptHash());
        assertFalse(isBlocked);

        Transaction tx = policyContract.blockAccount(blockAccount.getScriptHash())
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        isBlocked = policyContract.isBlocked(blockAccount.getScriptHash());
        assertTrue(isBlocked);

        tx = policyContract.unblockAccount(blockAccount.getScriptHash())
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
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

        Transaction tx = policyContract.blockAccount(blockAccount.getAddress())
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        isBlocked = policyContract.isBlocked(blockAccount.getScriptHash());
        assertTrue(isBlocked);

        tx = policyContract.unblockAccount(blockAccount.getAddress())
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        isBlocked = policyContract.isBlocked(blockAccount.getScriptHash());
        assertFalse(isBlocked);
    }

}
