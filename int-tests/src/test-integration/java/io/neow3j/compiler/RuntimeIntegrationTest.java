package io.neow3j.compiler;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Helper;
import io.neow3j.devpack.Notification;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.Transaction;
import io.neow3j.devpack.constants.CallFlags;
import io.neow3j.devpack.constants.TriggerType;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.script.InteropService;
import io.neow3j.script.OpCode;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RuntimeIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(RuntimeIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void getTriggerType() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().byteValue(), is(TriggerType.Application));
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
        assertThat(res.getStack().get(0).getString(), is("NEO"));
    }

    @Test
    public void getCallingScriptHash() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString().length(),
                is(2 * NeoConstants.HASH160_SIZE));
        assertThat(res.getStack().get(0).getInteger(), is(not(BigInteger.ZERO)));
    }

    @Test
    public void getEntryScriptHash() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString().length(),
                is(2 * NeoConstants.HASH160_SIZE));
        assertThat(res.getStack().get(0).getInteger(), is(not(BigInteger.ZERO)));
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
    public void loadScript() throws IOException {
        byte[] script = new ScriptBuilder().pushInteger(100).pushInteger(42).opCode(OpCode.ADD).toArray();
        InvocationResult result = ct.callInvokeFunction(testName,
                        byteArray(script), integer(CallFlags.ReadOnly), array())
                .getInvocationResult();
        StackItem stackItem = result.getStack().get(0);
        assertThat(stackItem.getInteger().intValue(), is(142));
    }

    @Test
    public void checkMultiSigWitnessDynamicallyWithLoadScript() throws IOException {
        byte[] script = new ScriptBuilder()
                .sysCall(InteropService.SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT)
                .toArray();

        Account account1 = Account.create();
        ECKeyPair.ECPublicKey pubKey1 = account1.getECKeyPair().getPublicKey();
        Account account2 = Account.create();
        ECKeyPair.ECPublicKey pubKey2 = account2.getECKeyPair().getPublicKey();
        Account account3 = Account.create();
        ECKeyPair.ECPublicKey pubKey3 = account3.getECKeyPair().getPublicKey();
        Account multiSigAccount = Account.createMultiSigAccount(
                asList(
                        pubKey1,
                        pubKey2,
                        pubKey3
                ), 2);

        InvocationResult result = ct.getContract().callInvokeFunction(testName,
                asList(
                        byteArray(script),
                        array(2, array(publicKey(pubKey1), publicKey(pubKey2), publicKey(pubKey3)))
                ),
                AccountSigner.calledByEntry(multiSigAccount)).getInvocationResult();

        assertThat(result.getState(), is(NeoVMStateType.HALT));
        assertThat(result.getStack().get(0).getAddress(), is(multiSigAccount.getAddress()));
    }

    @Test
    public void getTime() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(greaterThan(BigInteger.ZERO)));
    }

    @Test
    public void getNotifications() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        List<StackItem> notifications = res.getStack().get(0).getList();
        List<StackItem> notif1 = notifications.get(0).getList();
        assertThat(notif1.get(1).getString(), is("event1")); // event name
        assertThat(notif1.get(2).getList().get(0).getString(), is("event1")); // event state

        List<StackItem> notif2 = notifications.get(1).getList();
        assertThat(notif2.get(1).getString(), is("event2")); // event name
        assertThat(notif2.get(2).getList().get(0).getString(), is("event2")); // event state
        assertThat(notif2.get(2).getList().get(1).getInteger(), is(BigInteger.TEN)); // event state
    }

    @Test
    public void burnGas() throws Throwable {
        BigInteger gasToBurn = BigInteger.TEN.pow(10);
        // Provide the default account with enough GAS.
        Hash256 txHash = ct.transferGas(ct.getDefaultAccount().getScriptHash(), gasToBurn);
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        ct.signWithDefaultAccount();
        txHash = ct.invokeFunctionAndAwaitExecution(testName);
        String usedGas = ct.getNeow3j().getTransaction(txHash).send().getTransaction().getSysFee();
        assertTrue(new BigInteger(usedGas).compareTo(gasToBurn) >= 0);
    }

    @Test
    public void getNetwork() throws Throwable {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        BigInteger magic1 = res.getStack().get(0).getInteger();
        long magic2 = ct.getNeow3j().getNetworkMagicNumber();
        assertEquals(magic1.longValue(), magic2);
    }

    @Test
    public void getRandom() throws Throwable {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        BigInteger random = res.getStack().get(0).getInteger();
        assertThat(random, is(greaterThanOrEqualTo(BigInteger.ZERO)));
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

        public static Object loadScript(ByteString script, byte callFlags, Object[] arguments) {
            return Runtime.loadScript(script, callFlags, arguments);
        }

        public static Hash160 checkMultiSigWitnessDynamicallyWithLoadScript(ByteString script, Object[] multiSigArgs) {
            Hash160 multiSig = (Hash160) Runtime.loadScript(script, CallFlags.None, multiSigArgs);
            if (!Runtime.checkWitness(multiSig)) {
                Helper.abort();
            }
            return multiSig;
        }

        public static int getTime() {
            return Runtime.getTime();
        }

        static Event1Arg<String> event1;
        static Event2Args<String, Integer> event2;

        public static Notification[] getNotifications() {
            event1.fire("event1");
            event2.fire("event2", 10);
            return Runtime.getNotifications(Runtime.getExecutingScriptHash());
        }

        public static void burnGas() {
            Runtime.burnGas(Helper.pow(10, 10)); // burn 100 GAS
        }

        public static int getNetwork() {
            return Runtime.getNetwork();
        }

        public static int getRandom() {
            return Runtime.getRandom();
        }
    }

}
