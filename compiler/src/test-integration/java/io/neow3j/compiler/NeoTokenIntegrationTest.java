package io.neow3j.compiler;

import io.neow3j.contract.Hash256;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.NeoToken;
import io.neow3j.devpack.contracts.NeoToken.Candidate;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Await;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class NeoTokenIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(NeoTokenTestContract.class.getName());
        Hash256 gasTxHash = transferGas(defaultAccount.getScriptHash(), "10000");
        Hash256 neoTxHash = transferNeo(defaultAccount.getScriptHash(), "10000");
        Await.waitUntilTransactionIsExecuted(gasTxHash, neow3j);
        Await.waitUntilTransactionIsExecuted(neoTxHash, neow3j);
    }

    @Test
    public void unclaimedGas() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash160(defaultAccount.getScriptHash()),
                integer(1));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(0));
    }

    @Test
    public void registerAndUnregisterCandidate() throws IOException {
        signWithDefaultAccount();
        NeoInvokeFunction response = callInvokeFunction(
                publicKey(defaultAccount.getECKeyPair().getPublicKey().getEncoded(true)));

        List<StackItem> l = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(l.get(0).getBoolean());
        assertTrue(l.get(1).getBoolean());
    }

    @Test
    public void registerAndGetCandidates() throws IOException {
        signWithDefaultAccount();
        NeoInvokeFunction response = callInvokeFunction(
                publicKey(defaultAccount.getECKeyPair().getPublicKey().getEncoded(true)));

        List<StackItem> l = response.getInvocationResult().getStack().get(0).getList();
        assertThat(l, hasSize(1));
        assertThat(l.get(0).getList().get(0).getByteArray(),
                is(defaultAccount.getECKeyPair().getPublicKey().getEncoded(true)));
    }

    @Test
    public void getNextBlockValidators() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        List<StackItem> l = response.getInvocationResult().getStack().get(0).getList();
        assertThat(l.get(0).getByteArray(),
                is(defaultAccount.getECKeyPair().getPublicKey().getEncoded(true)));
    }

    @Test
    public void getCommittee() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        List<StackItem> l = response.getInvocationResult().getStack().get(0).getList();
        assertThat(l.get(0).getByteArray(),
                is(defaultAccount.getECKeyPair().getPublicKey().getEncoded(true)));
    }

    @Test
    public void setAndGetGasPerBlock() throws Throwable {
        signAsCommittee();
        NeoInvokeFunction res = callInvokeFunction("getGasPerBlock");
        int gasPerBlock = res.getInvocationResult().getStack().get(0).getInteger().intValue();
        assertThat(gasPerBlock, is(500_000_000));

        invokeFunctionAndAwaitExecution("setGasPerBlock", integer(50_000));

        res = callInvokeFunction("getGasPerBlock");
        gasPerBlock = res.getInvocationResult().getStack().get(0).getInteger().intValue();
        assertThat(gasPerBlock, is(50_000));
    }

    @Test
    public void vote() throws Throwable {
        signWithDefaultAccount();
        // Add the default account as a candidate
        Hash256 txHash = new io.neow3j.contract.NeoToken(neow3j)
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

        assertTrue(res.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(io.neow3j.contract.NeoToken.SCRIPT_HASH.toString()));
    }

    @Test
    public void setAndGetRegisterPrice() throws Throwable {
        signAsCommittee();
        NeoInvokeFunction res = callInvokeFunction("getRegisterPrice");
        int gasPerBlock = res.getInvocationResult().getStack().get(0).getInteger().intValue();
        assertThat(gasPerBlock, is(500_000_000));

        invokeFunctionAndAwaitExecution("setRegisterPrice", integer(50_000));

        res = callInvokeFunction("getRegisterPrice");
        gasPerBlock = res.getInvocationResult().getStack().get(0).getInteger().intValue();
        assertThat(gasPerBlock, is(50_000));
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

    }

}


