package io.neow3j.contract;

import static io.neow3j.NeoTestContainer.getNodeUrl;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_2;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_WALLET;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.CLIENTS_WALLET;
import static io.neow3j.transaction.Signer.calledByEntry;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.NeoTestContainer;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class NeoTokenIntegrationTest {

    private static Neow3j neow3j;
    private static NeoToken neoToken;

    @ClassRule
    public static NeoTestContainer neoTestContainer =
            new NeoTestContainer("/node-config/config.json");

    @BeforeClass
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(getNodeUrl(neoTestContainer)));
        neoToken = new NeoToken(neow3j);
        fundAccountsWithGas(neow3j, CLIENT_1, CLIENT_2);
    }

    @Test
    public void testUnclaimedGas() throws IOException {
        long blockHeight = neow3j.getBlockCount().send().getBlockIndex().longValue();
        BigInteger client1UnclaimedGas = neoToken.unclaimedGas(COMMITTEE_ACCOUNT, blockHeight);
        assertThat(client1UnclaimedGas, greaterThanOrEqualTo(new BigInteger("100000000")));
    }

    @Test
    public void testRegisterAndUnregisterCandidate() throws Throwable {
        Map<ECKeyPair.ECPublicKey, Integer> candidates = neoToken.getCandidates();
        assertThat(candidates.entrySet(), hasSize(0));

        Hash256 txHash = neoToken.registerCandidate(CLIENT_1.getECKeyPair().getPublicKey())
                .wallet(CLIENTS_WALLET)
                .signers(calledByEntry(CLIENT_1.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        candidates = neoToken.getCandidates();
        assertThat(candidates.entrySet(), hasSize(1));
        assertThat(candidates.get(CLIENT_1.getECKeyPair().getPublicKey()), greaterThanOrEqualTo(0));

        txHash = neoToken.unregisterCandidate(CLIENT_1.getECKeyPair().getPublicKey())
                .wallet(CLIENTS_WALLET)
                .signers(calledByEntry(CLIENT_1.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        candidates = neoToken.getCandidates();
        assertThat(candidates.entrySet(), hasSize(0));
    }

    @Test
    public void testRegisterAndVote() throws Throwable {
        Hash256 txHash = neoToken.registerCandidate(CLIENT_1.getECKeyPair().getPublicKey())
                .wallet(CLIENTS_WALLET)
                .signers(calledByEntry(CLIENT_1.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        txHash = neoToken.vote(CLIENT_2, CLIENT_1.getECKeyPair().getPublicKey())
                .wallet(CLIENTS_WALLET)
                .signers(calledByEntry(CLIENT_2.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        txHash = neoToken.unregisterCandidate(CLIENT_1.getECKeyPair().getPublicKey())
                .wallet(CLIENTS_WALLET)
                .signers(calledByEntry(CLIENT_1.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
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
        assertThat(nextBlockValidators.get(0).getEncoded(true),
                is(DEFAULT_ACCOUNT.getECKeyPair().getPublicKey().getEncoded(true)));
    }

    @Test
    public void testSetAndGetGasPerBlock() throws Throwable {
        BigInteger expectedInitialGasPerBlock = new BigInteger("500000000");
        BigInteger expectedNewGasPerBlock = new BigInteger("250000000");

        BigInteger gasPerBlock = neoToken.getGasPerBlock();
        assertThat(gasPerBlock, is(expectedInitialGasPerBlock));

        Hash256 txHash = neoToken.setGasPerBlock(new BigInteger("250000000"))
                .wallet(COMMITTEE_WALLET)
                .signers(calledByEntry(COMMITTEE_ACCOUNT.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        gasPerBlock = neoToken.getGasPerBlock();
        assertThat(gasPerBlock, is(expectedNewGasPerBlock));
    }

    @Test
    public void testSetAndGetRegisterPrice() throws Throwable {
        BigInteger expectedInitialRegisterPrice = new BigInteger("100000000000");
        BigInteger expectedNewRegisterPrice = new BigInteger("80000000000");

        BigInteger registerPrice = neoToken.getRegisterPrice();
        assertThat(registerPrice, is(expectedInitialRegisterPrice));

        Hash256 txHash = neoToken.setRegisterPrice(new BigInteger("80000000000"))
                .wallet(COMMITTEE_WALLET)
                .signers(calledByEntry(COMMITTEE_ACCOUNT.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        registerPrice = neoToken.getRegisterPrice();
        assertThat(registerPrice, is(expectedNewRegisterPrice));
    }

}
