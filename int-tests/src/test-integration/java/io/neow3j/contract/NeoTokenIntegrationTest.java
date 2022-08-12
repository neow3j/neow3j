package io.neow3j.contract;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoAccountState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.transaction.Witness;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithNeo;
import static io.neow3j.contract.IntegrationTestHelper.registerCandidateAndAwaitExecution;
import static io.neow3j.contract.IntegrationTestHelper.unregisterCandidateAndAwaitExecution;
import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Note, that the default committee account in the NeoTestContainer does not count as a candidate in the following
// tests, since it was not elected through an actual vote on the NEO token contract.
@Testcontainers
public class NeoTokenIntegrationTest {

    private static Neow3j neow3j;
    private static NeoToken neoToken;

    @Container
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeAll
    public static void setUp() {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
        neoToken = new NeoToken(neow3j);
    }

    @Test
    public void testUnclaimedGas() throws IOException {
        long blockCount = neow3j.getBlockCount().send().getBlockCount().longValue();
        BigInteger client1UnclaimedGas = neoToken.unclaimedGas(COMMITTEE_ACCOUNT, blockCount);
        assertThat(client1UnclaimedGas, greaterThanOrEqualTo(BigInteger.ZERO));
    }

    @Test
    public void testRegisterAndUnregisterCandidate() throws Throwable {
        Account candidate = Account.create();
        ECKeyPair.ECPublicKey candidateKey = candidate.getECKeyPair().getPublicKey();
        fundAccountsWithGas(neow3j, new BigDecimal("1010"), candidate);
        assertFalse(neoToken.isCandidate(candidateKey));

        List<NeoToken.Candidate> candidates = neoToken.getCandidates();
        assertTrue(candidates.stream().noneMatch(c -> c.getPublicKey().equals(candidateKey)));

        registerAsCandidate(candidate);
        assertTrue(neoToken.isCandidate(candidateKey));

        candidates = neoToken.getCandidates();
        assertTrue(candidates.stream().anyMatch(c -> c.getPublicKey().equals(candidateKey)));
        assertThat(candidates.stream().filter(c -> c.getPublicKey().equals(candidateKey))
                        .findFirst().get().getVotes(), greaterThanOrEqualTo(BigInteger.ZERO));

        unregisterAsCandidate(candidate);
        assertFalse(neoToken.isCandidate(candidateKey));

        candidates = neoToken.getCandidates();
        assertTrue(candidates.stream().noneMatch(c -> c.getPublicKey().equals(candidateKey)));
    }

    @Test
    public void testGetAllCandidates() throws Throwable {
        Account candidate1 = Account.fromWIF("KxYQMD3vjyehMh2fXDKtdvVjnatppLSHzbbZPUc1GFQ1xKy6aHW6");
        Account candidate2 = Account.fromWIF("KyaCDA83aiUNMByMdC5vFa5C2diEpG1PCui5fWbeWbsdUushX78p");
        fundAccountsWithGas(neow3j, new BigDecimal("1010"), candidate1, candidate2);

        List<NeoToken.Candidate> allCandidates = neoToken.getAllCandidatesIterator().traverse(2);
        assertThat(allCandidates, hasSize(0));

        registerAsCandidate(candidate1);
        registerAsCandidate(candidate2);

        Account voter = Account.create();
        BigInteger voterNeoBalance = BigInteger.valueOf(80);
        fundAccountsWithGas(neow3j, voter);
        fundAccountsWithNeo(neow3j, voterNeoBalance, voter);

        allCandidates = neoToken.getAllCandidatesIterator().traverse(2);
        assertThat(allCandidates, hasSize(2));
        assertThat(allCandidates.get(0).getPublicKey(), is(candidate1.getECKeyPair().getPublicKey()));
        assertThat(allCandidates.get(0).getVotes(), is(BigInteger.ZERO));
        assertThat(allCandidates.get(1).getPublicKey(), is(candidate2.getECKeyPair().getPublicKey()));
        assertThat(allCandidates.get(1).getVotes(), is(BigInteger.ZERO));

        Hash256 txHash = neoToken.vote(voter, candidate2.getECKeyPair().getPublicKey())
                .signers(calledByEntry(voter))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        allCandidates = neoToken.getAllCandidatesIterator().traverse(2);
        assertThat(allCandidates.get(0).getPublicKey(), is(candidate1.getECKeyPair().getPublicKey()));
        assertThat(allCandidates.get(0).getVotes(), is(BigInteger.ZERO));
        assertThat(allCandidates.get(1).getPublicKey(), is(candidate2.getECKeyPair().getPublicKey()));
        assertThat(allCandidates.get(1).getVotes(), is(voterNeoBalance));

        unregisterAsCandidate(candidate1);
        unregisterAsCandidate(candidate2);

        allCandidates = neoToken.getAllCandidatesIterator().traverse(2);
        assertThat(allCandidates, hasSize(0));
    }

    @Test
    public void testGetCandidateVotes() throws Throwable {
        Account candidate1 = Account.create();
        fundAccountsWithGas(neow3j, new BigDecimal("1010"), candidate1);
        registerAsCandidate(candidate1);

        Account voter1 = Account.create();
        fundAccountsWithGas(neow3j, voter1);
        BigInteger voter1NeoBalance = BigInteger.valueOf(32);
        fundAccountsWithNeo(neow3j, voter1NeoBalance, voter1);

        BigInteger votes = neoToken.getCandidateVotes(candidate1.getECKeyPair().getPublicKey());
        assertThat(votes, is(BigInteger.ZERO));

        Hash256 txHash = neoToken.vote(voter1, candidate1.getECKeyPair().getPublicKey())
                .signers(calledByEntry(voter1))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        votes = neoToken.getCandidateVotes(candidate1.getECKeyPair().getPublicKey());
        assertThat(votes, is(voter1NeoBalance));

        unregisterAsCandidate(candidate1);
    }

    @Test
    public void testBuildVoteScript() throws Throwable {
        Account candidate1 = Account.create();
        Account candidate2 = Account.create();
        fundAccountsWithGas(neow3j, new BigDecimal("1010"), candidate1, candidate2);
        registerAsCandidate(candidate1);
        registerAsCandidate(candidate2);

        Account voter1 = Account.create();
        Account voter2 = Account.create();
        fundAccountsWithGas(neow3j, voter1, voter2);
        fundAccountsWithNeo(neow3j, BigInteger.valueOf(105), voter1);
        fundAccountsWithNeo(neow3j, BigInteger.valueOf(42), voter2);

        byte[] voteScript1 = neoToken.buildVoteScript(voter1.getScriptHash(), candidate1.getECKeyPair().getPublicKey());
        byte[] voteScript2 = neoToken.buildVoteScript(voter2.getScriptHash(), candidate2.getECKeyPair().getPublicKey());

        Hash256 txHash = new TransactionBuilder(neow3j)
                .script(voteScript1)
                .extendScript(voteScript2)
                .signers(calledByEntry(voter1), calledByEntry(voter2))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        ECKeyPair.ECPublicKey votingTargetVoter1 = neoToken.getAccountState(voter1.getScriptHash()).getPublicKey();
        ECKeyPair.ECPublicKey votingTargetVoter2 = neoToken.getAccountState(voter2.getScriptHash()).getPublicKey();

        assertThat(votingTargetVoter1, is(candidate1.getECKeyPair().getPublicKey()));
        assertThat(votingTargetVoter2, is(candidate2.getECKeyPair().getPublicKey()));

        unregisterAsCandidate(candidate1);
        unregisterAsCandidate(candidate2);
    }

    @Test
    public void testRegisterAndVote() throws Throwable {
        Account candidate = Account.create();
        fundAccountsWithGas(neow3j, new BigDecimal("1010"), candidate);
        registerAsCandidate(candidate);

        List<NeoToken.Candidate> candidates = neoToken.getCandidates();
        assertTrue(candidates.stream().anyMatch(c -> c.getPublicKey().equals(candidate.getECKeyPair().getPublicKey())));

        Account voterAccount = Account.create();
        fundAccountsWithGas(neow3j, voterAccount);
        fundAccountsWithNeo(neow3j, new BigInteger("22"), voterAccount);

        Hash256 txHash = neoToken.vote(voterAccount, candidate.getECKeyPair().getPublicKey())
                .signers(calledByEntry(voterAccount))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        BigInteger voteCount = neoToken.getBalanceOf(voterAccount);
        candidates = neoToken.getCandidates();
        assertThat(candidates.stream().filter(c -> c.getPublicKey().equals(candidate.getECKeyPair().getPublicKey()))
                        .findFirst().get().getVotes(), is(voteCount));
        unregisterAsCandidate(candidate);
    }

    @Test
    public void cancelVote() throws Throwable {
        Account candidate = Account.create();
        ECKeyPair.ECPublicKey candidateKey = candidate.getECKeyPair().getPublicKey();
        fundAccountsWithGas(neow3j, new BigDecimal("1010"), candidate);

        Account voter = Account.create();
        registerAsCandidate(candidate);
        fundAccountsWithGas(neow3j, voter);
        fundAccountsWithNeo(neow3j, new BigInteger("22"), voter);

        List<NeoToken.Candidate> candidates = neoToken.getCandidates();
        BigInteger initialVotes = candidates.stream().filter(c -> c.getPublicKey().equals(candidateKey))
                .findFirst().get().getVotes();

        Hash256 txHash = neoToken.vote(voter, candidateKey)
                .signers(calledByEntry(voter))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        candidates = neoToken.getCandidates();
        assertThat(candidates.stream().filter(c -> c.getPublicKey().equals(candidateKey))
                .findFirst().get().getVotes().intValue(), is(initialVotes.intValue() + 22));

        txHash = neoToken.cancelVote(voter)
                .signers(calledByEntry(voter))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        candidates = neoToken.getCandidates();
        assertThat(candidates.stream().filter(c -> c.getPublicKey().equals(candidateKey)).findFirst().get().getVotes(),
                is(initialVotes));

        unregisterAsCandidate(candidate);
    }

    @Test
    public void testGetCommittee() throws IOException {
        List<ECKeyPair.ECPublicKey> committeeList = neoToken.getCommittee();
        assertThat(committeeList, hasSize(1));
        assertThat(committeeList.get(0), is(DEFAULT_ACCOUNT.getECKeyPair().getPublicKey()));
    }

    @Test
    public void testGetNextBlockValidators() throws IOException {
        List<ECKeyPair.ECPublicKey> nextBlockValidators = neoToken.getNextBlockValidators();
        assertThat(nextBlockValidators, hasSize(1));
        assertThat(nextBlockValidators.get(0), is(DEFAULT_ACCOUNT.getECKeyPair().getPublicKey()));
    }

    @Test
    public void testSetAndGetGasPerBlock() throws Throwable {
        BigInteger expectedInitialGasPerBlock = new BigInteger("500000000");
        BigInteger expectedNewGasPerBlock = new BigInteger("250000000");

        BigInteger gasPerBlock = neoToken.getGasPerBlock();
        assertThat(gasPerBlock, is(expectedInitialGasPerBlock));

        Transaction tx = neoToken.setGasPerBlock(new BigInteger("250000000"))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();

        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());

        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        gasPerBlock = neoToken.getGasPerBlock();
        assertThat(gasPerBlock, is(expectedNewGasPerBlock));
    }

    @Test
    public void testSetAndGetRegisterPrice() throws Throwable {
        BigInteger expectedInitialRegisterPrice = new BigInteger("100000000000");
        BigInteger expectedNewRegisterPrice = new BigInteger("50000000000");

        BigInteger registerPrice = neoToken.getRegisterPrice();
        assertThat(registerPrice, is(expectedInitialRegisterPrice));

        Transaction tx = neoToken.setRegisterPrice(new BigInteger("50000000000"))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();

        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());

        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        registerPrice = neoToken.getRegisterPrice();
        assertThat(registerPrice, is(expectedNewRegisterPrice));
    }

    @Test
    public void testGetAccountState() throws Throwable {
        Account candidate = Account.create();
        Account voter = Account.create();

        NeoAccountState initialState = neoToken.getAccountState(voter.getScriptHash());
        assertThat(initialState, is(NeoAccountState.withNoBalance()));

        fundAccountsWithNeo(neow3j, BigInteger.valueOf(2000), voter);
        fundAccountsWithGas(neow3j, voter);
        fundAccountsWithGas(neow3j, new BigDecimal("1010") , candidate);

        NeoAccountState state = neoToken.getAccountState(voter.getScriptHash());
        assertThat(state.getBalance(), is(BigInteger.valueOf(2000)));
        assertNotNull(state.getBalanceHeight());
        assertThat(state.getBalanceHeight(), greaterThanOrEqualTo(BigInteger.ONE));
        assertNull(state.getPublicKey());

        registerAsCandidate(candidate);

        Hash256 txHash = neoToken.vote(voter.getScriptHash(), candidate.getECKeyPair().getPublicKey())
                .signers(calledByEntry(voter))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        NeoAccountState stateWithVote = neoToken.getAccountState(voter.getScriptHash());
        assertThat(stateWithVote.getBalance(), is(BigInteger.valueOf(2000)));
        assertThat(state.getBalanceHeight(), greaterThanOrEqualTo(BigInteger.ONE));
        assertThat(stateWithVote.getPublicKey(), is(candidate.getECKeyPair().getPublicKey()));

        unregisterAsCandidate(candidate);
    }

    private void registerAsCandidate(Account candidate) throws Throwable {
        registerCandidateAndAwaitExecution(neow3j, candidate);
    }

    protected void unregisterAsCandidate(Account candidate) throws Throwable {
        unregisterCandidateAndAwaitExecution(neow3j, candidate);
    }

}
