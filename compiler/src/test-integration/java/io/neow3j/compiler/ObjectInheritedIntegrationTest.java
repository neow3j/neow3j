package io.neow3j.compiler;

import io.neow3j.devpack.Block;
import io.neow3j.devpack.ExecutionEngine;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.Notification;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.Transaction;
import io.neow3j.devpack.contracts.LedgerContract;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.contract.ContractParameter.hash256;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// Test methods on devpack classes that are inherited from `Object`.
public class ObjectInheritedIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct =
            new ContractTestRule(ObjectInheritedIntegrationTestContract.class.getName());

    @Test
    public void blockEquals() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();
        // the comparison of Objects, which are Arrays on the neo-vm, is only by reference.
        // Therefore, only the comparison of the same reference will return true.
        assertTrue(boolArr.get(0).getBoolean());
        assertFalse(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void transactionEquals() throws Throwable {
        // Add a transaction to compare.
        io.neow3j.contract.Hash256 txHash =
                ct.transferGas(ct.getDefaultAccount().getScriptHash(), BigInteger.ONE);
        waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash256(ct.getDeployTxHash()),
                hash256(txHash));
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();

        // the comparison of Objects, which are Arrays on the neo-vm, is only by reference.
        // Therefore, only the comparison of the same reference will return true.
        assertTrue(boolArr.get(0).getBoolean());
        assertFalse(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void notificationEquals() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();

        // the comparison of Objects, which are Arrays on the neo-vm, is only by reference.
        // Therefore, only the comparison of the same reference will return true.
        assertTrue(boolArr.get(0).getBoolean());
        assertFalse(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    static class ObjectInheritedIntegrationTestContract {

        public static boolean[] blockEquals() throws Exception {
            Block block0 = LedgerContract.getBlock(0);
            Block block1 = LedgerContract.getBlock(1);
            Block block0_other = LedgerContract.getBlock(0);
            if (block0 == null || block1 == null || block0_other == null) {
                throw new Exception("Couldn't fetch blocks");
            }
            boolean[] b = new boolean[4];
            b[0] = block0.equals(block0);
            b[1] = block0.equals(block0_other);
            b[2] = block0.equals(block1);
            b[3] = block0.equals(2);
            return b;
        }

        public static boolean[] transactionEquals(Hash256 deployTxHash, Hash256 transferTxHash)
                throws Exception {
            Transaction tx0 = LedgerContract.getTransaction(deployTxHash);
            Transaction tx1 = LedgerContract.getTransaction(transferTxHash);
            Transaction tx0_other = LedgerContract.getTransaction(deployTxHash);
            if (tx0 == null || tx1 == null || tx0_other == null) {
                throw new Exception("Couldn't fetch transactions");
            }
            boolean[] b = new boolean[4];
            b[0] = tx0.equals(tx0);
            b[1] = tx0.equals(tx0_other);
            b[2] = tx0.equals(tx1);
            b[3] = tx0.equals(2);
            return b;
        }

        static Event1Arg<String> event1;
        static Event2Args<String, Integer> event2;

        public static boolean[] notificationEquals() throws Exception {
            event1.fire("event1");
            event2.fire("event2", 10);
            Notification[] notifications1 =
                    Runtime.getNotifications(ExecutionEngine.getExecutingScriptHash());
            Notification[] notifications2 =
                    Runtime.getNotifications(ExecutionEngine.getExecutingScriptHash());
            if (notifications1[0] == null || notifications1[1] == null) {
                throw new Exception("Couldn't fetch notifications");
            }
            boolean[] b = new boolean[4];
            b[0] = notifications1[0].equals(notifications1[0]);
            b[1] = notifications1[0].equals(notifications2[0]);
            b[2] = notifications1[0].equals(notifications1[1]);
            b[3] = notifications1[0].equals(2);
            return b;
        }

    }

}
