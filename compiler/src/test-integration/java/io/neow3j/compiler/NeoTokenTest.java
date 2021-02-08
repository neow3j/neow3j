package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.NeoToken;
import io.neow3j.devpack.contracts.NeoToken.Candidate;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Await;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class NeoTokenTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(NeoTokenTestContract.class.getName());
    }

    @Test
    public void unclaimedGas() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash160(defaultAccount.getScriptHash()),
                integer(1));

        assertThat(response.getInvocationResult().getStack().get(0)
                .asInteger().getValue().intValue(), is(0));
    }

    @Test
    public void registerAndUnregisterCandidate() throws IOException {
        signWithDefaultAccount();
        NeoInvokeFunction response = callInvokeFunction(
                publicKey(defaultAccount.getECKeyPair().getPublicKey().getEncoded(true)));

        List<StackItem> l = response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertTrue(l.get(0).asBoolean().getValue());
        assertTrue(l.get(1).asBoolean().getValue());
    }

    @Test
    public void registerAndGetCandidates() throws IOException {
        signWithDefaultAccount();
        NeoInvokeFunction response = callInvokeFunction(
                publicKey(defaultAccount.getECKeyPair().getPublicKey().getEncoded(true)));

        List<StackItem> l = response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(l, hasSize(1));
        assertThat(l.get(0).asStruct().get(0).asByteString().getValue(),
                is(defaultAccount.getECKeyPair().getPublicKey().getEncoded(true)));
    }

    @Test
    public void getNextBlockValidators() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        List<StackItem> l = response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(l.get(0).asByteString().getValue(),
                is(defaultAccount.getECKeyPair().getPublicKey().getEncoded(true)));
    }

    @Test
    public void getCommittee() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        List<StackItem> l = response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(l.get(0).asByteString().getValue(),
                is(defaultAccount.getECKeyPair().getPublicKey().getEncoded(true)));
    }

    @Test
    public void setAndGetGasPerBlock() throws Throwable {
        signAsCommittee();
        NeoInvokeFunction res = callInvokeFunction("getGasPerBlock");
        int gasPerBlock = res.getInvocationResult().getStack().get(0).asInteger().getValue()
                .intValue();
        assertThat(gasPerBlock, is(500_000_000));

        invokeFunctionAndAwaitExecution("setGasPerBlock", integer(50_000));

        res = callInvokeFunction("getGasPerBlock");
        gasPerBlock = res.getInvocationResult().getStack().get(0).asInteger().getValue().intValue();
        assertThat(gasPerBlock, is(50_000));
    }

    @Test
    public void vote() throws Throwable {
        // Needs GAS for the transaction costs.
        Await.waitUntilTransactionIsExecuted(
                transferGas(defaultAccount.getScriptHash(), "100"), neow3j);
        // Needs NEo to be able to vote.
        Await.waitUntilTransactionIsExecuted(
                transferNeo(defaultAccount.getScriptHash(), "100"), neow3j);

        // Add the default account as a candidate
        String txHash = new io.neow3j.contract.NeoToken(neow3j)
                .registerCandidate(defaultAccount.getECKeyPair().getPublicKey())
                .wallet(wallet)
                .signers(Signer.calledByEntry(defaultAccount.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);

        signWithDefaultAccount();
        NeoInvokeFunction res = callInvokeFunction(hash160(defaultAccount.getScriptHash()),
                publicKey(defaultAccount.getECKeyPair().getPublicKey().getEncoded(true)));

        assertTrue(res.getInvocationResult().getStack().get(0).asBoolean().getValue());
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(io.neow3j.contract.NeoToken.SCRIPT_HASH.toString()));
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

        public static boolean setGasPerBlock(int gasPerBlock) {
            return NeoToken.setGasPerBlock(gasPerBlock);
        }

        public static boolean vote(Hash160 scriptHash, ECPoint pubKey) {
            return NeoToken.vote(scriptHash, pubKey);
        }

        public static Hash160 getHash() {
            return NeoToken.getHash();
        }

    }

}


