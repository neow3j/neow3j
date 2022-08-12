package io.neow3j.compiler;

import io.neow3j.contract.FungibleToken;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.constants.NativeContract;
import io.neow3j.devpack.contracts.NeoToken;
import io.neow3j.devpack.contracts.NeoToken.AccountState;
import io.neow3j.devpack.contracts.NeoToken.Candidate;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.types.Hash256;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.Await;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NeoTokenIntegrationTest {

    private static io.neow3j.contract.NeoToken neoToken;
    private static BigInteger client1NeoFunded;

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(NeoTokenTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @BeforeAll
    public void setUp() throws Throwable {
        neoToken = new io.neow3j.contract.NeoToken(ct.getNeow3j());
        Hash256 fundHash1 = ct.transferGas(ct.getDefaultAccount().getScriptHash(), new BigInteger("1000000000"));
        Hash256 fundHash2 = ct.transferNeo(ct.getDefaultAccount().getScriptHash(), new BigInteger("10000"));
        Hash256 fundHash3 = ct.transferGas(ct.getClient1().getScriptHash(),
                FungibleToken.toFractions(new BigDecimal("10000"), 8));
        client1NeoFunded = new BigInteger("10000");
        Hash256 fundHash4 = ct.transferNeo(ct.getClient1().getScriptHash(), client1NeoFunded);
        Await.waitUntilTransactionIsExecuted(fundHash1, ct.getNeow3j());
        Await.waitUntilTransactionIsExecuted(fundHash2, ct.getNeow3j());
        Await.waitUntilTransactionIsExecuted(fundHash3, ct.getNeow3j());
        Await.waitUntilTransactionIsExecuted(fundHash4, ct.getNeow3j());
    }

    @Test
    public void unclaimedGas() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash160(ct.getDefaultAccount().getScriptHash()), integer(1));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(0));
    }

    @Test
    public void registerAndUnregisterCandidate() throws IOException {
        ct.signWithDefaultAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                publicKey(ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true)));

        List<StackItem> l = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(l.get(0).getBoolean());
        assertTrue(l.get(1).getBoolean());
    }

    @Test
    public void registerAndGetCandidates() throws IOException {
        ct.signWithDefaultAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                publicKey(ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true)));

        List<StackItem> l = response.getInvocationResult().getStack().get(0).getList();
        assertThat(l, hasSize(1));
        List<StackItem> candidateStruct = l.get(0).getList();
        assertThat(candidateStruct.get(0).getByteArray(),
                is(ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true)));
        assertThat(candidateStruct.get(1).getInteger(), greaterThanOrEqualTo(BigInteger.ZERO));
    }

    @Test
    public void getAllCandidates() throws Throwable {
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        String sessionId = response.getSessionId();
        String iteratorId = response.getStack().get(0).getIteratorId();

        List<StackItem> allCandidates = ct.traverseIterator(sessionId, iteratorId);
        assertThat(allCandidates, hasSize(0));

        io.neow3j.contract.NeoToken neoToken = new io.neow3j.contract.NeoToken(ct.getNeow3j());
        registerCandidate(ct.getClient1());
        voteForCandidate(ct.getClient1(), ct.getClient1());

        assertThat(neoToken.getBalanceOf(ct.getClient1()), is(client1NeoFunded));
        response = ct.callInvokeFunction(testName).getInvocationResult();
        sessionId = response.getSessionId();
        iteratorId = response.getStack().get(0).getIteratorId();

        allCandidates = ct.traverseIterator(sessionId, iteratorId);
        assertThat(allCandidates, hasSize(1));
        assertThat(allCandidates.get(0).getType(), is(StackItemType.STRUCT));
        List<StackItem> candStruct = allCandidates.get(0).getList();
        assertThat(candStruct.get(0).getHexString(),
                is(ct.getClient1().getECKeyPair().getPublicKey().getEncodedCompressedHex()));
        assertThat(candStruct.get(1).getInteger(), is(client1NeoFunded));

        unregisterCandidate(ct.getClient1());
    }

    private void registerCandidate(Account cand) throws Throwable {
        Hash256 hash = neoToken.registerCandidate(cand.getECKeyPair().getPublicKey())
                .signers(AccountSigner.calledByEntry(cand))
                .sign().send().getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(hash, ct.getNeow3j());
    }

    private void unregisterCandidate(Account cand) throws Throwable {
        Hash256 hash = neoToken.unregisterCandidate(cand.getECKeyPair().getPublicKey())
                .signers(AccountSigner.calledByEntry(cand))
                .sign().send().getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(hash, ct.getNeow3j());
    }

    private void voteForCandidate(Account cand, Account voter) throws Throwable {
        Hash256 hash = neoToken.vote(voter, cand.getECKeyPair().getPublicKey())
                .signers(AccountSigner.calledByEntry(voter))
                .sign().send().getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(hash, ct.getNeow3j());
    }

    @Test
    public void getCandidateVotes() throws Throwable {
        InvocationResult response = ct.callInvokeFunction(testName,
                publicKey(ct.getClient1().getECKeyPair().getPublicKey())).getInvocationResult();
        StackItem stackItem = response.getStack().get(0);
        assertThat(stackItem.getType(), is(StackItemType.INTEGER));
        assertThat(stackItem.getInteger(), is(BigInteger.valueOf(-1)));

        registerCandidate(ct.getClient1());
        voteForCandidate(ct.getClient1(), ct.getClient1());

        response = ct.callInvokeFunction(testName,
                publicKey(ct.getClient1().getECKeyPair().getPublicKey())).getInvocationResult();
        stackItem = response.getStack().get(0);
        assertThat(stackItem.getType(), is(StackItemType.INTEGER));
        assertThat(stackItem.getInteger(), is(client1NeoFunded));

        unregisterCandidate(ct.getClient1());
    }

    @Test
    public void getNextBlockValidators() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);

        List<StackItem> l = response.getInvocationResult().getStack().get(0).getList();
        assertThat(l.get(0).getByteArray(),
                is(ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true)));
    }

    @Test
    public void getCommittee() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);

        List<StackItem> l = response.getInvocationResult().getStack().get(0).getList();
        assertThat(l.get(0).getByteArray(),
                is(ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true)));
    }

    @Test
    public void setAndGetGasPerBlock() throws Throwable {
        ct.signWithCommitteeAccount();
        NeoInvokeFunction res = ct.callInvokeFunction("getGasPerBlock");
        int gasPerBlock = res.getInvocationResult().getStack().get(0).getInteger().intValue();
        assertThat(gasPerBlock, is(500_000_000));

        ct.invokeFunctionAndAwaitExecution("setGasPerBlock", integer(50_000));

        res = ct.callInvokeFunction("getGasPerBlock");
        gasPerBlock = res.getInvocationResult().getStack().get(0).getInteger().intValue();
        assertThat(gasPerBlock, is(50_000));
    }

    @Test
    public void vote() throws Throwable {
        ct.signWithDefaultAccount();
        // Add the default account as a candidate
        Hash256 txHash = new io.neow3j.contract.NeoToken(ct.getNeow3j())
                .registerCandidate(ct.getDefaultAccount().getECKeyPair().getPublicKey())
                .signers(AccountSigner.calledByEntry(ct.getDefaultAccount()))
                .sign()
                .send()
                .getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        ct.signWithDefaultAccount();
        NeoInvokeFunction res = ct.callInvokeFunction(testName,
                hash160(ct.getDefaultAccount().getScriptHash()),
                publicKey(ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true)));

        assertTrue(res.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void getAccountStateWithoutVote() throws Throwable {
        Account acc = Account.create();
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(acc));
        StackItem stateNoBalance = response.getInvocationResult().getStack().get(0);
        assertThat(stateNoBalance.getType(), is(StackItemType.ANY));
        assertNull(stateNoBalance.getValue());

        Hash256 txHash = ct.transferNeo(acc.getScriptHash(), BigInteger.TEN);
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        response = ct.callInvokeFunction(testName, hash160(acc));
        List<StackItem> stateNoVote = response.getInvocationResult().getStack().get(0).getList();
        assertThat(stateNoVote.get(0).getInteger(), is(BigInteger.valueOf(10L)));
        assertThat(stateNoVote.get(1).getInteger(), greaterThanOrEqualTo(BigInteger.ONE));
        assertNull(stateNoVote.get(2).getValue());
    }

    @Test
    public void registerVoteAndGetAccountState() throws Throwable {
        ct.signWithDefaultAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash160(ct.getDefaultAccount()),
                publicKey(ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true)));
        List<StackItem> stateStruct = response.getInvocationResult().getStack().get(0).getList();
        assertThat(stateStruct.get(0).getInteger(), is(BigInteger.valueOf(10000L)));
        assertThat(stateStruct.get(1).getInteger(), greaterThanOrEqualTo(BigInteger.ONE));
        assertThat(stateStruct.get(2).getByteArray(),
                is(ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true)));

        unregisterCandidate(ct.getDefaultAccount());
    }

    @Test
    public void getHash() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(neoTokenHash())));
    }

    @Test
    public void setAndGetRegisterPrice() throws Throwable {
        ct.signWithCommitteeAccount();
        NeoInvokeFunction res = ct.callInvokeFunction("getRegisterPrice");
        BigInteger gasPerBlock = res.getInvocationResult().getStack().get(0).getInteger();
        assertThat(gasPerBlock, is(new BigInteger("100000000000")));
        ct.invokeFunctionAndAwaitExecution("setRegisterPrice", integer(50_000));

        res = ct.callInvokeFunction("getRegisterPrice");
        gasPerBlock = res.getInvocationResult().getStack().get(0).getInteger();
        assertThat(gasPerBlock, is(new BigInteger("50000")));
    }

    @Permission(nativeContract = NativeContract.NeoToken)
    static class NeoTokenTestContract {
        static NeoToken neoToken = new NeoToken();

        public static int unclaimedGas(Hash160 scriptHash, int blockHeight) {
            return neoToken.unclaimedGas(scriptHash, blockHeight);
        }

        public static boolean[] registerAndUnregisterCandidate(ECPoint publicKey) {
            boolean[] b = new boolean[2];
            b[0] = neoToken.registerCandidate(publicKey);
            b[1] = neoToken.unregisterCandidate(publicKey);
            return b;
        }

        public static Candidate[] registerAndGetCandidates(ECPoint publicKey) {
            neoToken.registerCandidate(publicKey);
            return neoToken.getCandidates();
        }

        public static Iterator<Iterator.Struct<ECPoint, Integer>> getAllCandidates() {
            return neoToken.getAllCandidates();
        }

        public static int getCandidateVotes(ECPoint candidatePubKey) {
            return neoToken.getCandidateVote(candidatePubKey);
        }

        public static ECPoint[] getNextBlockValidators() {
            return neoToken.getNextBlockValidators();
        }

        public static ECPoint[] getCommittee() {
            return neoToken.getCommittee();
        }

        public static int getGasPerBlock() {
            return neoToken.getGasPerBlock();
        }

        public static void setGasPerBlock(int gasPerBlock) {
            neoToken.setGasPerBlock(gasPerBlock);
        }

        public static boolean vote(Hash160 scriptHash, ECPoint pubKey) {
            return neoToken.vote(scriptHash, pubKey);
        }

        public static Hash160 getHash() {
            return neoToken.getHash();
        }

        public static int getRegisterPrice() {
            return neoToken.getRegisterPrice();
        }

        public static void setRegisterPrice(int registerPrice) {
            neoToken.setRegisterPrice(registerPrice);
        }

        public static AccountState getAccountStateWithoutVote(Hash160 scriptHash) {
            return neoToken.getAccountState(scriptHash);
        }

        public static AccountState registerVoteAndGetAccountState(Hash160 voter, ECPoint publicKey) {
            neoToken.registerCandidate(publicKey);
            neoToken.vote(voter, publicKey);
            return neoToken.getAccountState(voter);
        }

    }

}
