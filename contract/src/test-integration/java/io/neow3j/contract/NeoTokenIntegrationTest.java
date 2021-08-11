package io.neow3j.contract;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoAccountState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_2;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithNeo;
import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class NeoTokenIntegrationTest {

    private static Neow3j neow3j;
    private static NeoToken neoToken;

    @ClassRule
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeClass
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
        neoToken = new NeoToken(neow3j);
        fundAccountsWithGas(neow3j, CLIENT_1, CLIENT_2);
    }

    @Test
    public void testUnclaimedGas() throws IOException {
        long blockCount = neow3j.getBlockCount().send().getBlockCount().longValue();
        BigInteger client1UnclaimedGas = neoToken.unclaimedGas(COMMITTEE_ACCOUNT, blockCount);
        assertThat(client1UnclaimedGas, greaterThanOrEqualTo(new BigInteger("100000000")));
    }

    @Test
    public void testRegisterAndUnregisterCandidate() throws Throwable {
        Map<ECKeyPair.ECPublicKey, BigInteger> candidates = neoToken.getCandidates();
        assertThat(candidates.keySet(), not(hasItem(CLIENT_1.getECKeyPair().getPublicKey())));

        registerClient1AsCandidate();

        candidates = neoToken.getCandidates();
        assertThat(candidates.keySet(), hasItem(CLIENT_1.getECKeyPair().getPublicKey()));
        assertThat(candidates.get(CLIENT_1.getECKeyPair().getPublicKey()),
                greaterThanOrEqualTo(BigInteger.ZERO));

        unregisterClient1AsCandidate();

        candidates = neoToken.getCandidates();
        assertThat(candidates.keySet(), not(hasItem(CLIENT_1.getECKeyPair().getPublicKey())));
    }

    @Test
    public void testRegisterAndVote() throws Throwable {
        registerClient1AsCandidate();

        Map<ECKeyPair.ECPublicKey, BigInteger> candidates = neoToken.getCandidates();
        assertThat(candidates.keySet(), hasItem(CLIENT_1.getECKeyPair().getPublicKey()));
        BigInteger initialVotes = candidates.get(CLIENT_1.getECKeyPair().getPublicKey());

        Account voterAccount = Account.create();
        fundAccountsWithGas(neow3j, voterAccount);
        fundAccountsWithNeo(neow3j, new BigInteger("22"), voterAccount);

        Hash256 txHash = neoToken.vote(voterAccount, CLIENT_1.getECKeyPair().getPublicKey())
                .signers(calledByEntry(voterAccount))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        BigInteger voteCount = neoToken.getBalanceOf(voterAccount);
        candidates = neoToken.getCandidates();
        assertThat(candidates.get(CLIENT_1.getECKeyPair().getPublicKey()),
                is(initialVotes.add(voteCount)));

        unregisterClient1AsCandidate();
    }

    @Test
    public void testGetCommittee() throws IOException {
        List<ECKeyPair.ECPublicKey> committeeList = neoToken.getCommittee();
        assertThat(committeeList, hasSize(1));
        assertThat(committeeList.get(0),
                is(DEFAULT_ACCOUNT.getECKeyPair().getPublicKey()));
    }

    @Test
    public void testGetNextBlockValidators() throws IOException {
        List<ECKeyPair.ECPublicKey> nextBlockValidators = neoToken.getNextBlockValidators();
        assertThat(nextBlockValidators, hasSize(1));
        assertThat(nextBlockValidators.get(0),
                is(DEFAULT_ACCOUNT.getECKeyPair().getPublicKey()));
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
        Account account1 = Account.fromWIF("L3LFTJ49PiVo7ZpphkBCv6isnygsgNyh3pKwvJAEkDmmHM8PNiYu");

        NeoAccountState initialState = neoToken.getAccountState(account1.getScriptHash());
        assertThat(initialState, is(NeoAccountState.withNoBalance()));

        // Fund accounts with Neo and Gas
        fundAccountsWithNeo(neow3j, BigInteger.valueOf(2000), account1);
        fundAccountsWithGas(neow3j, account1);

        NeoAccountState state = neoToken.getAccountState(account1.getScriptHash());
        assertThat(state.getBalance(), is(BigInteger.valueOf(2000)));
        assertNotNull(state.getBalanceHeight());
        assertThat(state.getBalanceHeight(), greaterThanOrEqualTo(BigInteger.ONE));
        assertNull(state.getPublicKey());

        // Register client1 as candidate and vote for it with account1
        registerClient1AsCandidate();

        Hash256 txHash = neoToken.vote(account1.getScriptHash(),
                        CLIENT_1.getECKeyPair().getPublicKey())
                .signers(calledByEntry(account1))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        NeoAccountState stateWithVote = neoToken.getAccountState(account1.getScriptHash());
        assertThat(stateWithVote.getBalance(), is(BigInteger.valueOf(2000)));
        assertThat(state.getBalanceHeight(), greaterThanOrEqualTo(BigInteger.ONE));
        assertThat(stateWithVote.getPublicKey(), is(CLIENT_1.getECKeyPair().getPublicKey()));

        // Unregister candidate
        unregisterClient1AsCandidate();
    }

    private void registerClient1AsCandidate() throws Throwable {
        Hash256 txHash = neoToken.registerCandidate(CLIENT_1.getECKeyPair().getPublicKey())
                .signers(calledByEntry(CLIENT_1))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
    }

    private void unregisterClient1AsCandidate() throws Throwable {
        Hash256 txHash = neoToken.unregisterCandidate(CLIENT_1.getECKeyPair().getPublicKey())
                .signers(calledByEntry(CLIENT_1))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
    }

}
