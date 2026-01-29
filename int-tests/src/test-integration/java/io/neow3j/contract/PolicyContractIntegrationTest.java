package io.neow3j.contract;

import io.neow3j.contract.types.WhitelistFeeEntry;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.transaction.Witness;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
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
        BigInteger expectedNewExecPicoFeeFactor = new BigInteger("506388");

        BigInteger execFeeFactor = policyContract.getExecFeeFactor();
        assertThat(execFeeFactor, is(expectedInitialExecFeeFactor));

        // Starting with the Faun hardfork, the exec fee factor is stored with additional 4 decimals compared to
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

        BigInteger newExecPicoFeeFactor = policyContract.getExecPicoFeeFactor();
        assertThat(newExecPicoFeeFactor, is(expectedNewExecPicoFeeFactor));
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
        Account blockAccount = Account.fromWIF("Kz7mT4rHmHg25k8SUzNhMoibJEwFxEHmq4cHWU6NygsJPh5zEhFK");

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

        Iterator<Hash160> blockedAccountsIt = policyContract.getBlockedAccounts();
        List<Hash160> blockedAccounts = blockedAccountsIt.traverse(100);
        assertThat(blockedAccounts.size(), greaterThanOrEqualTo(1));
        assertTrue(blockedAccounts.stream().anyMatch(h -> h.equals(blockAccount.getScriptHash())));

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
        Account blockAccount = Account.fromWIF("Kz7mT4rHmHg25k8SUzNhMoibJEwFxEHmq4cHWU6NygsJPh5zEhFK");

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

        List<Hash160> blockedAccounts = policyContract.getBlockedAccountsUnwrapped();
        assertThat(blockedAccounts.size(), greaterThanOrEqualTo(1));
        assertTrue(blockedAccounts.stream().anyMatch(h -> h.equals(blockAccount.getScriptHash())));

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

    @Test
    public void testRecoverFund() throws Throwable {
        Hash160 accountToRecoverFrom = new Hash160("0x0123456789abcdef0123456789abcdef01234567");
        // Block the account.
        Transaction tx = policyContract.blockAccount(accountToRecoverFrom)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        // Try to recover funds from the blocked account. It has not been blocked for one year, so the operation
        // is expected to fail with a specific exception.
        neow3j.allowTransmissionOnFault();
        tx = policyContract.recoverFund(accountToRecoverFrom, NeoToken.SCRIPT_HASH)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        NeoApplicationLog log = neow3j.getApplicationLog(txHash).send().getApplicationLog();
        assertThat(log.getFirstExecution().getState(), is(NeoVMStateType.FAULT));
        assertThat(log.getFirstExecution().getException(),
                containsString("Request must be signed at least 1 year ago. Remaining time: 364d 23h"));

        tx = policyContract.unblockAccount(accountToRecoverFrom)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        boolean isBlocked = policyContract.isBlocked(accountToRecoverFrom);
        assertFalse(isBlocked);
    }

    /**
     * Whitelists the Neo and Gas transfer methods, assert the state before and after. Then, removes the Gas transfer
     * method from the whitelist and asserts the final state.
     */
    @Test
    public void testWhitelistFeeContract() throws Throwable {
        List<WhitelistFeeEntry> contractsBefore = policyContract.getWhitelistFeeContractsUnwrapped();
        assertThat(contractsBefore, hasSize(0));
        Iterator<WhitelistFeeEntry> itBefore = policyContract.getWhitelistFeeContracts();
        List<WhitelistFeeEntry> itContractsBefore = itBefore.traverse(100);
        assertThat(itContractsBefore, hasSize(0));

        // Whitelist the Neo and Gas transfer methods.
        Transaction txNeo = policyContract.setWhitelistFeeContract(NeoToken.SCRIPT_HASH, "transfer", 4,
                        BigInteger.ZERO)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitnessNeo = createMultiSigWitness(
                asList(signMessage(txNeo.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHashNeo = txNeo.addWitness(multiSigWitnessNeo).send().getSendRawTransaction().getHash();
        Transaction txGas = policyContract.setWhitelistFeeContract(GasToken.SCRIPT_HASH, "transfer", 4,
                        new BigInteger("1234"))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitnessGas = createMultiSigWitness(
                asList(signMessage(txGas.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHashGas = txGas.addWitness(multiSigWitnessGas).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHashNeo, neow3j);
        waitUntilTransactionIsExecuted(txHashGas, neow3j);

        WhitelistFeeEntry expectedWhitelistNeo =
                new WhitelistFeeEntry(NeoToken.SCRIPT_HASH, "transfer", 4, BigInteger.ZERO);
        WhitelistFeeEntry expectedWhitelistGas =
                new WhitelistFeeEntry(GasToken.SCRIPT_HASH, "transfer", 4, new BigInteger("1234"));

        List<WhitelistFeeEntry> contractsAfter = policyContract.getWhitelistFeeContractsUnwrapped();
        assertThat(contractsAfter, hasSize(2));
        assertThat(contractsAfter, containsInAnyOrder(expectedWhitelistNeo, expectedWhitelistGas));

        Iterator<WhitelistFeeEntry> itContractsAfter = policyContract.getWhitelistFeeContracts();
        List<WhitelistFeeEntry> itListContractsAfter = itContractsAfter.traverse(100);

        assertThat(itListContractsAfter, hasSize(2));
        assertThat(itListContractsAfter, containsInAnyOrder(expectedWhitelistNeo, expectedWhitelistGas));

        // Remove Gas transfer whitelist entry.
        Transaction txRemoveGas = policyContract.removeWhitelistFeeContract(GasToken.SCRIPT_HASH, "transfer", 4)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(txRemoveGas.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = txRemoveGas.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        List<WhitelistFeeEntry> contractsFinal = policyContract.getWhitelistFeeContractsUnwrapped();
        assertThat(contractsFinal, hasSize(1));
        // Only the Neo transfer method whitelist entry is expected to remain.
        assertThat(contractsFinal, contains(expectedWhitelistNeo));
    }

}
