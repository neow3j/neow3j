package io.neow3j.compiler;

import io.neow3j.types.Hash256;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.NeoToken;
import io.neow3j.devpack.contracts.NeoToken.Candidate;
import io.neow3j.devpack.contracts.NeoToken.AccountState;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.transaction.Signer;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.Await;
import io.neow3j.utils.Numeric;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class NeoTokenIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            NeoTokenTestContract.class.getName());

    @BeforeClass
    public static void setUp() throws Throwable {
        Hash256 gasTxHash = ct.transferGas(ct.getDefaultAccount().getScriptHash(),
                new BigInteger("10000"));
        Hash256 neoTxHash = ct.transferNeo(ct.getDefaultAccount().getScriptHash(),
                new BigInteger("10000"));
        Await.waitUntilTransactionIsExecuted(gasTxHash, ct.getNeow3j());
        Await.waitUntilTransactionIsExecuted(neoTxHash, ct.getNeow3j());
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
                .wallet(ct.getWallet())
                .signers(Signer.calledByEntry(ct.getDefaultAccount().getScriptHash()))
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
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(ct.getClient1()));
        StackItem stateNoBalance = response.getInvocationResult().getStack().get(0);
        assertThat(stateNoBalance.getType(), is(StackItemType.ANY));
        assertNull(stateNoBalance.getValue());

        Hash256 txHash = ct.transferNeo(ct.getClient1().getScriptHash(), BigInteger.TEN);
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        response = ct.callInvokeFunction(testName, hash160(ct.getClient1()));
        List<StackItem> stateNoVote = response.getInvocationResult().getStack().get(0).getList();
        assertThat(stateNoVote.get(0).getInteger(), is(BigInteger.valueOf(10L)));
        assertThat(stateNoVote.get(1).getInteger(), greaterThanOrEqualTo(BigInteger.ONE));
        assertNull(stateNoVote.get(2).getValue());
    }

    @Test
    public void registerVoteAndGetAccountState() throws IOException {
        ct.signWithDefaultAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash160(ct.getDefaultAccount()),
                publicKey(ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true)));
        List<StackItem> stateStruct = response.getInvocationResult().getStack().get(0).getList();
        assertThat(stateStruct.get(0).getInteger(), is(BigInteger.valueOf(10000L)));
        assertThat(stateStruct.get(1).getInteger(), greaterThanOrEqualTo(BigInteger.ONE));
        assertThat(stateStruct.get(2).getByteArray(),
                is(ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true)));
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

    static class NeoTokenTestContract {

        public static int unclaimedGas(Hash160 scriptHash, int blockHeight) {
            return NeoToken.unclaimedGas(scriptHash, blockHeight);
        }

        public static boolean[] registerAndUnregisterCandidate(ECPoint publicKey) {
            boolean[] b = new boolean[2];
            b[0] = NeoToken.registerCandidate(publicKey);
            b[1] = NeoToken.unregisterCandidate(publicKey);
            return b;
        }

        public static Candidate[] registerAndGetCandidates(ECPoint publicKey) {
            NeoToken.registerCandidate(publicKey);
            return NeoToken.getCandidates();
        }

        public static ECPoint[] getNextBlockValidators() {
            return NeoToken.getNextBlockValidators();
        }

        public static ECPoint[] getCommittee() {
            return NeoToken.getCommittee();
        }

        public static int getGasPerBlock() {
            return NeoToken.getGasPerBlock();
        }

        public static void setGasPerBlock(int gasPerBlock) {
            NeoToken.setGasPerBlock(gasPerBlock);
        }

        public static boolean vote(Hash160 scriptHash, ECPoint pubKey) {
            return NeoToken.vote(scriptHash, pubKey);
        }

        public static Hash160 getHash() {
            return NeoToken.getHash();
        }

        public static int getRegisterPrice() {
            return NeoToken.getRegisterPrice();
        }

        public static void setRegisterPrice(int registerPrice) {
            NeoToken.setRegisterPrice(registerPrice);
        }

        public static AccountState getAccountStateWithoutVote(Hash160 scriptHash) {
            return NeoToken.getAccountState(scriptHash);
        }

        public static AccountState registerVoteAndGetAccountState(Hash160 voter,
                ECPoint publicKey) {
            NeoToken.registerCandidate(publicKey);
            NeoToken.vote(voter, publicKey);
            return NeoToken.getAccountState(voter);
        }

    }

}
