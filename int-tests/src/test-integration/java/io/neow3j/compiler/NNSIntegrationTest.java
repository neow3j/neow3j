package io.neow3j.compiler;

import io.neow3j.contract.SmartContract;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.List;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.contracts.NeoNameService;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NNSIntegrationTest {

    private static final io.neow3j.types.Hash160 dummyScriptHash =
            new io.neow3j.types.Hash160("3e2b5b33a98bdcf205c848dd3b2a3613d7e4b957");

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(TestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @BeforeAll
    public static void setUp() throws Throwable {
        SmartContract sc = ct.deployContract(ConcreteNeoNameService.class.getName());
        ct.setHash(sc.getScriptHash());
    }

    @Test
    public void testAddRoot() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("io/neow3j/test"));
        String exception = response.getInvocationResult().getException();
        assertNull(exception);
        assertNull(response.getInvocationResult().getStack().get(0).getValue());
    }

    @Test
    public void testSetPrice() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(12));
        String exception = response.getInvocationResult().getException();
        assertNull(exception);
        assertNull(response.getInvocationResult().getStack().get(0).getValue());
    }

    @Test
    public void testGetPrice() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(4));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger(), is(BigInteger.valueOf(8)));
    }

    @Test
    public void testIsAvailable() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("neo.anydomain"));
        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void testRegister() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("neo.anyDomain"), hash160(dummyScriptHash));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void testRenew() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("neo.domain"));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger(), is(BigInteger.valueOf(1625504018L)));
    }

    @Test
    public void testSetAdmin() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("domain"), hash160(dummyScriptHash));
        String exception = response.getInvocationResult().getException();
        assertNull(exception);
        assertNull(response.getInvocationResult().getStack().get(0).getValue());
    }

    @Test
    public void testSetRecord() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                string("neo.dummy"), integer(RecordType.CNAME.byteValue()), string("neo.dummy2"));
        String exception = response.getInvocationResult().getException();
        assertNull(exception);
        assertNull(response.getInvocationResult().getStack().get(0).getValue());
    }

    @Test
    public void testGetRecord() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                string("neo.domain"), integer(RecordType.CNAME.byteValue()));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("getRecordReturn"));
    }

    @Test
    public void testDeleteRecord() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                string("neo.domain"), integer(RecordType.TXT.byteValue()));
        String exception = response.getInvocationResult().getException();
        assertNull(exception);
        assertNull(response.getInvocationResult().getStack().get(0).getValue());
    }

    @Test
    public void testResolve() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                string("neo.domain"), integer(RecordType.TXT.byteValue()));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("resolveReturn"));
    }

    @Permission(contract = "*")
    static class TestContract {

        public static void testAddRoot(String root) {
            new NeoNameService(getHash()).addRoot(root);
        }

        public static void testSetPrice(List<Integer> priceList) {
            new NeoNameService(getHash()).setPrice(priceList);
        }

        public static int testGetPrice(int length) {
            return new NeoNameService(getHash()).getPrice(length);
        }

        public static boolean testIsAvailable(String name) {
            return new NeoNameService(getHash()).isAvailable(name);
        }

        public static boolean testRegister(String name, Hash160 owner) {
            return new NeoNameService(getHash()).register(name, owner);
        }

        public static int testRenew(String name) {
            return new NeoNameService(getHash()).renew(name);
        }

        public static void testSetAdmin(String name, Hash160 admin) {
            new NeoNameService(getHash()).setAdmin(name, admin);
        }

        public static void testSetRecord(String name, int type, String data) {
            new NeoNameService(getHash()).setRecord(name, type, data);
        }

        public static String testGetRecord(String name, int type) {
            return new NeoNameService(getHash()).getRecord(name, type);
        }

        public static void testDeleteRecord(String name, int type) {
            new NeoNameService(getHash()).deleteRecord(name, type);
        }

        public static String testResolve(String name, int type) {
            return new NeoNameService(getHash()).resolve(name, type);
        }

        public static void setHash(Hash160 contractHash) {
            Storage.put(Storage.getStorageContext(), 0xff, contractHash);
        }

        private static Hash160 getHash() {
            return Storage.getHash160(Storage.getReadOnlyContext(), 0xff);
        }

    }

    static class ConcreteNeoNameService {

        public static void addRoot(String root) {
        }

        public static void setPrice(int price) {
        }

        public static int getPrice(int length) {
            return length * 2;
        }

        public static boolean isAvailable(String name) {
            return false;
        }

        public static boolean register(String name, Hash160 owner) {
            return true;
        }

        public static int renew(String name) {
            return 1625504018;
        }

        public static void setAdmin(String name, Hash160 admin) {
        }

        public static void setRecord(String name, int type, String data) {
        }

        public static String getRecord(String name, int type) {
            return "getRecordReturn";
        }

        public static void deleteRecord(String name, int type) {
        }

        public static String resolve(String name, int type) {
            return "resolveReturn";
        }

    }

}
