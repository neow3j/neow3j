package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.types.Hash256;
import io.neow3j.types.StackItemType;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithNeo;
import static io.neow3j.contract.IntegrationTestHelper.registerCandidateAndAwaitExecution;
import static io.neow3j.contract.NeoToken.GET_ALL_CANDIDATES;
import static io.neow3j.contract.NeoToken.candidateMapper;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

@Testcontainers
public class SmartContractIntegrationTest {

    private static Neow3j neow3j;
    private static NeoToken neoToken;

    private static final Account candidate1Acc = Account.fromWIF(
            "KxYQMD3vjyehMh2fXDKtdvVjnatppLSHzbbZPUc1GFQ1xKy6aHW6");
    private static final Account candidate2Acc =
            Account.fromWIF("KyaCDA83aiUNMByMdC5vFa5C2diEpG1PCui5fWbeWbsdUushX78p");

    private static final BigInteger candidate1Votes = BigInteger.ZERO;
    private static final BigInteger candidate2Votes = BigInteger.valueOf(80);

    private static final NeoToken.Candidate candidate1 =
            new NeoToken.Candidate(candidate1Acc.getECKeyPair().getPublicKey(), candidate1Votes);
    private static final NeoToken.Candidate candidate2 =
            new NeoToken.Candidate(candidate2Acc.getECKeyPair().getPublicKey(), candidate2Votes);

    @Container
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeAll
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
        neoToken = new NeoToken(neow3j);
        prepareCandidatesForTests();
    }

    public static void prepareCandidatesForTests() throws Throwable {
        fundAccountsWithGas(neow3j, new BigDecimal("1100"), candidate1Acc, candidate2Acc);

        registerCandidateAndAwaitExecution(neow3j, candidate1Acc);
        registerCandidateAndAwaitExecution(neow3j, candidate2Acc);

        Account voter = Account.create();
        BigInteger voterNeoBalance = candidate2Votes;
        fundAccountsWithGas(neow3j, voter);
        fundAccountsWithNeo(neow3j, voterNeoBalance, voter);

        Hash256 txHash = neoToken.vote(voter, candidate2Acc.getECKeyPair().getPublicKey())
                .signers(calledByEntry(voter))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
    }

    @Test
    public void testCallFunctionAndTraverseIterator() throws IOException {
        List<StackItem> candidatesItems = neoToken.callFunctionAndTraverseIterator(GET_ALL_CANDIDATES);

        assertThat(candidatesItems, hasSize(2));
        assertThat(candidatesItems.get(0).getType(), is(StackItemType.STRUCT));
        assertThat(candidatesItems.get(1).getList().get(0).getHexString(),
                is(candidate2Acc.getECKeyPair().getPublicKey().getEncodedCompressedHex()));
        assertThat(candidatesItems.get(1).getList().get(1).getInteger(), is(candidate2Votes));
    }

    @Test
    public void testCallFunctionAndTraverseIteratorWithMapper() throws IOException {
        List<NeoToken.Candidate> allCandidates = neoToken.callFunctionAndTraverseIterator(candidateMapper(),
                GET_ALL_CANDIDATES);

        assertThat(allCandidates, hasSize(2));
        assertThat(allCandidates.get(0), is(candidate1));
        assertThat(allCandidates.get(1), is(candidate2));
    }

}
