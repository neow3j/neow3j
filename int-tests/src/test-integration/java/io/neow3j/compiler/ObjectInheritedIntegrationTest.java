package io.neow3j.compiler;

import io.neow3j.devpack.Block;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Contract;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Notification;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StorageMap;
import io.neow3j.devpack.Transaction;
import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.devpack.contracts.ContractManagement;
import io.neow3j.devpack.contracts.LedgerContract;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.test.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.hash256;
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test methods on devpack classes that are inherited from `Object`.
public class ObjectInheritedIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(ObjectInheritedIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void blockEquals() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();
        // The Block is an ArrayStackItem on the neo-vm, thus, the EQUALS opcode only
        // compares it by reference. But the separate equals(Block) implementation makes a
        // comparison by value.
        assertTrue(boolArr.get(0).getBoolean());
        assertTrue(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void transactionEquals() throws Throwable {
        // Add a transaction to compare.
        io.neow3j.types.Hash256 txHash =
                ct.transferGas(ct.getDefaultAccount().getScriptHash(), BigInteger.ONE);
        waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash256(ct.getDeployTxHash()),
                hash256(txHash));
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();
        // The Transaction is an ArrayStackItem on the neo-vm, thus, the EQUALS opcode only
        // compares it by reference. But the separate equals(Transaction) implementation makes a
        // comparison by value.
        assertTrue(boolArr.get(0).getBoolean());
        assertTrue(boolArr.get(1).getBoolean());
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

    @Test
    public void ecPointEquals() throws Throwable {
        String point1 = "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
        String point2 = "02249425a06b5a1f8e6133fc79afa2c2b8430bf9327297f176761df79e8d8929c5";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, publicKey(point1),
                publicKey(point1), publicKey(point2));
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();

        // ECPoints are ByteStringStackItems on the neo-vm and are therefore compared by reference
        // and by value.
        assertTrue(boolArr.get(0).getBoolean());
        assertTrue(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void byteStringEquals() throws Throwable {
        String bs1 = "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
        String bs2 = "02249425a06b5a1f8e6133fc79afa2c2b8430bf9327297f176761df79e8d8929c5";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(bs1),
                byteArray(bs1), byteArray(bs2));
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();

        // ByteStrings are ByteStringStackItems on the neo-vm and are therefore compared by
        // reference and by value.
        assertTrue(boolArr.get(0).getBoolean());
        assertTrue(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void stringEquals() throws Throwable {
        String s1 = "string1";
        String s2 = "string2";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string(s1), string(s1),
                string(s2));
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();

        // Strings are ByteStringStackItems on the neo-vm and are therefore compared by
        // reference and by value.
        assertTrue(boolArr.get(0).getBoolean());
        assertTrue(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void hash160Equals() throws Throwable {
        io.neow3j.types.Hash160 h1 =
                new io.neow3j.types.Hash160("ec2b32ed87e3747e826a0abd7229cb553220fd7a");
        io.neow3j.types.Hash160 h2 =
                new io.neow3j.types.Hash160("3d255cc204f151498dcac95da244babb895e7175");
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(h1), hash160(h1),
                hash160(h2));
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();

        // Hash160 are ByteStringStackItems on the neo-vm and are therefore compared by reference
        // and by value.
        assertTrue(boolArr.get(0).getBoolean());
        assertTrue(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void hash256Equals() throws Throwable {
        io.neow3j.types.Hash256 h1 = new io.neow3j.types.Hash256(
                        "ec2b32ed87e3747e826a0abd7229cb553220fd7a7229cb553220fd7a0fd7a0fd");
        io.neow3j.types.Hash256 h2 = new io.neow3j.types.Hash256(
                        "3d255cc204f151498dcac95da244babb895e7175d255cc204f151498dcac7175");
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash256(h1), hash256(h1),
                hash256(h2));
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();

        // Hash256 are ByteStringStackItems on the neo-vm and are therefore compared by reference
        // and by value.
        assertTrue(boolArr.get(0).getBoolean());
        assertTrue(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void iteratorEquals() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();
        // the comparison of InteropInterfaces on the neo-vm, which Iterators are, is only by
        // reference. Therefore, only the comparison of the same reference will return true.
        assertTrue(boolArr.get(0).getBoolean());
        assertFalse(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void mapEquals() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();
        // the comparison of InteropInterfaces on the neo-vm, which Maps are, is only by
        // reference. Therefore, only the comparison of the same reference will return true.
        assertTrue(boolArr.get(0).getBoolean());
        assertFalse(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void listEquals() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();
        // The devpack's List objet is represented by the ArrayStackItem on the neo-vm. Thus, it
        // is only compared by reference.
        assertTrue(boolArr.get(0).getBoolean());
        assertFalse(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void storageMapEquals() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();
        // The StorageMap is an ArrayStackItem on the neo-vm, thus, the EQUALS opcode only
        // compares it by reference. But the separate equals(StorageMap) implementation makes a
        // comparison by value.
        assertTrue(boolArr.get(0).getBoolean());
        assertTrue(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    @Test
    public void storageContextEquals() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();
        // The StorageContext is an InteropInterfaceStackItem on the neo-vm, thus, it is only
        // compared by reference.
        assertTrue(boolArr.get(0).getBoolean());
        assertFalse(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
    }

    @Test
    public void contractEquals() throws IOException {
        io.neow3j.types.Hash160 c1 =
                new io.neow3j.types.Hash160(TestProperties.gasTokenHash());
        io.neow3j.types.Hash160 c2 =
                new io.neow3j.types.Hash160(TestProperties.neoTokenHash());
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(c1), hash160(c2));
        List<StackItem> boolArr = response.getInvocationResult().getStack().get(0).getList();
        // the comparison of Objects, which are Arrays on the neo-vm, is only by reference.
        // Therefore, only the comparison of the same reference will return true.
        assertTrue(boolArr.get(0).getBoolean());
        assertTrue(boolArr.get(1).getBoolean());
        assertFalse(boolArr.get(2).getBoolean());
        assertFalse(boolArr.get(3).getBoolean());
    }

    static class ObjectInheritedIntegrationTestContract {

        public static boolean[] blockEquals() throws Exception {
            LedgerContract ledgerContract = new LedgerContract();
            Block block0 = ledgerContract.getBlock(0);
            Block block1 = ledgerContract.getBlock(1);
            Block block0_other = ledgerContract.getBlock(0);
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

        public static boolean[] transactionEquals(Hash256 deployTxHash, Hash256 transferTxHash) throws Exception {
            LedgerContract ledgerContract = new LedgerContract();
            Transaction tx0 = ledgerContract.getTransaction(deployTxHash);
            Transaction tx1 = ledgerContract.getTransaction(transferTxHash);
            Transaction tx0_other = ledgerContract.getTransaction(deployTxHash);
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
                    Runtime.getNotifications(Runtime.getExecutingScriptHash());
            Notification[] notifications2 =
                    Runtime.getNotifications(Runtime.getExecutingScriptHash());
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

        public static boolean[] ecPointEquals(ECPoint p1, ECPoint p1_other, ECPoint p2) {
            boolean[] b = new boolean[4];
            b[0] = p1.equals(p1);
            b[1] = p1.equals(p1_other);
            b[2] = p1.equals(p2);
            b[3] = p1.equals(2);
            return b;
        }

        public static boolean[] byteStringEquals(ByteString bs1, ByteString bs1_other, ByteString bs2) {
            boolean[] b = new boolean[4];
            b[0] = bs1.equals(bs1);
            b[1] = bs1.equals(bs1_other);
            b[2] = bs1.equals(bs2);
            b[3] = bs1.equals(2);
            return b;
        }

        public static boolean[] stringEquals(String s1, String s1_other, String s2) {
            boolean[] b = new boolean[4];
            b[0] = s1.equals(s1);
            b[1] = s1.equals(s1_other);
            b[2] = s1.equals(s2);
            b[3] = s1.equals(2);
            return b;
        }

        public static boolean[] hash160Equals(Hash160 h1, Hash160 h1_other, Hash160 h2) {
            boolean[] b = new boolean[4];
            b[0] = h1.equals(h1);
            b[1] = h1.equals(h1_other);
            b[2] = h1.equals(h2);
            b[3] = h1.equals(2);
            return b;
        }

        public static boolean[] hash256Equals(Hash256 h1, Hash256 h1_other, Hash256 h2) {
            boolean[] b = new boolean[4];
            b[0] = h1.equals(h1);
            b[1] = h1.equals(h1_other);
            b[2] = h1.equals(h2);
            b[3] = h1.equals(2);
            return b;
        }

        public static boolean[] iteratorEquals() {
            StorageContext ctx = Storage.getStorageContext();
            Iterator it1 = Storage.find(ctx, "prefix1", FindOptions.None);
            Iterator it1_other = Storage.find(ctx, "prefix1", FindOptions.None);
            Iterator it2 = Storage.find(ctx, "prefix2", FindOptions.None);
            boolean[] b = new boolean[4];
            b[0] = it1.equals(it1);
            b[1] = it1.equals(it1_other);
            b[2] = it1.equals(it2);
            b[3] = it1.equals(2);
            return b;
        }

        public static boolean[] mapEquals() {
            Map<String, String> map1 = new Map<>();
            Map<String, String> map1_other = new Map<>();
            Map<String, String> map2 = new Map<>();

            boolean[] b = new boolean[4];
            b[0] = map1.equals(map1);
            b[1] = map1.equals(map1_other);
            b[2] = map1.equals(map2);
            b[3] = map1.equals(2);
            return b;
        }

        public static boolean[] listEquals() {
            io.neow3j.devpack.List<String> l1 = new io.neow3j.devpack.List<>();
            io.neow3j.devpack.List<String> l1_other = new io.neow3j.devpack.List<>();
            io.neow3j.devpack.List<String> l2 = new io.neow3j.devpack.List<>();

            boolean[] b = new boolean[4];
            b[0] = l1.equals(l1);
            b[1] = l1.equals(l1_other);
            b[2] = l1.equals(l2);
            b[3] = l1.equals(2);
            return b;
        }

        public static boolean[] storageMapEquals() {
            StorageContext ctx = Storage.getStorageContext();
            StorageMap m1 = new StorageMap(ctx, new byte[]{0x01, 0x02});
            StorageMap m1_other = new StorageMap(ctx, new byte[]{0x01, 0x02});
            StorageMap m2 = new StorageMap(ctx, new byte[]{0x03});

            boolean[] b = new boolean[4];
            b[0] = m1.equals(m1);
            b[1] = m1.equals(m1_other);
            b[2] = m1.equals(m2);
            b[3] = m1.equals(2);
            return b;
        }

        public static boolean[] storageContextEquals() {
            StorageContext ctx1 = Storage.getStorageContext();
            StorageContext ctx2 = Storage.getStorageContext();

            boolean[] b = new boolean[3];
            b[0] = ctx1.equals(ctx1);
            b[1] = ctx1.equals(ctx2);
            b[2] = ctx1.equals(2);
            return b;
        }

        public static boolean[] contractEquals(Hash160 c1, Hash160 c2) {
            Contract contr1 = new ContractManagement().getContract(c1);
            Contract contr1_other = new ContractManagement().getContract(c1);
            Contract contr2 = new ContractManagement().getContract(c2);

            boolean[] b = new boolean[4];
            b[0] = contr1.equals(contr1);
            b[1] = contr1.equals(contr1_other);
            b[2] = contr1.equals(contr2);
            b[3] = contr1.equals(2);
            return b;
        }

    }

}
