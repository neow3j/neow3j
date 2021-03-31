package io.neow3j.compiler;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Notification;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.Transaction;
import io.neow3j.devpack.TriggerType;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.StackItem;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.publicKey;
import static io.neow3j.devpack.Helper.assertTrue;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RuntimeIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            RuntimeIntegrationTestContract.class.getName());

    @Test
    public void getTriggerType() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().byteValue(), is(TriggerType.APPLICATION));
    }

    @Test
    public void getGasLeft() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(greaterThan(0)));
    }

    @Test
    public void getInvocationCounter() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(greaterThan(0)));
    }

    @Test
    public void checkWitnessWithHash() throws IOException {
        ContractParameter hash = hash160(ct.getDefaultAccount().getScriptHash());
        ct.signWithDefaultAccount();
        InvocationResult res = ct.callInvokeFunction(testName, hash).getInvocationResult();
        assertTrue(res.getStack().get(0).getBoolean());
    }

    @Test
    public void checkWitnessWithECPoint() throws IOException {
        ContractParameter pubKey = publicKey(ct.getDefaultAccount().getECKeyPair().getPublicKey()
                .getEncoded(true));
        ct.signWithDefaultAccount();
        InvocationResult res = ct.callInvokeFunction(testName, pubKey).getInvocationResult();
        assertTrue(res.getStack().get(0).getBoolean());
    }

    @Test
    public void getPlatform() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is("the platform"));
    }

    @Test
    public void getCallingScriptHash() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is("is this the transaction hash?"));
    }

    @Test
    public void getEntryScriptHash() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is("is this the transaction hash?"));
    }

    @Test
    public void getExecutingScriptHash() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(),
                is(ct.getContract().getScriptHash().toAddress()));
    }

    @Test
    public void getScriptContainer() throws IOException {
        ct.signWithDefaultAccount();
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        List<StackItem> tx = res.getStack().get(0).getList();
        assertThat(tx, hasSize(8));
        assertThat(tx.get(3).getAddress(), is(ct.getDefaultAccount().getAddress()));
        // Transaction attributes:
//        Hash256 hash;
//        byte version;
//        int nonce;
//        Hash160 sender;
//        int systemFee;
//        int networkFee;
//        int validUntilBlock;
//        ByteString script;
    }

    @Test
    public void getTime() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(greaterThan(0)));
    }

    @Test
    public void getNotifications() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        List<StackItem> notifications = res.getStack().get(0).getList();
        // TODO: check what notifications are available.
        fail();
    }

    static class RuntimeIntegrationTestContract {

        public static byte getTriggerType() {
            return Runtime.getTrigger();
        }

        public static int getGasLeft() {
            return Runtime.getGasLeft();
        }

        public static int getInvocationCounter() {
            return Runtime.getInvocationCounter();
        }

        public static boolean checkWitnessWithHash(Hash160 hash) {
            return Runtime.checkWitness(hash);
        }

        public static boolean checkWitnessWithECPoint(ECPoint ecpoint) {
            return Runtime.checkWitness(ecpoint);
        }

        public static String getPlatform() {
            return Runtime.getPlatform();
        }

        public static Hash160 getCallingScriptHash() {
            return Runtime.getCallingScriptHash();
        }

        public static Hash160 getEntryScriptHash() {
            return Runtime.getEntryScriptHash();
        }

        public static Hash160 getExecutingScriptHash() {
            return Runtime.getExecutingScriptHash();
        }

        public static Transaction getScriptContainer() {
            return (Transaction) Runtime.getScriptContainer();
        }

        public static int getTime() {
            return Runtime.getTime();
        }

        public static Notification[] getNotifications() {
            return Runtime.getNotifications(new Hash160(
                    hexToBytes("0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5")));
        }

    }
}

