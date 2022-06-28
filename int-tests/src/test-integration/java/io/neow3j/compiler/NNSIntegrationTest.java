package io.neow3j.compiler;

import io.neow3j.contract.SmartContract;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.List;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.contracts.NeoNameService;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.devpack.Helper.reverse;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NNSIntegrationTest {

    private static final io.neow3j.types.Hash160 dummyScriptHash =
            new io.neow3j.types.Hash160("3e2b5b33a98bdcf205c848dd3b2a3613d7e4b957");

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(NNSTestContract.class.getName());

    @BeforeClass
    public static void setUp() throws Throwable {
        SmartContract sc = ct.deployContract(ConcreteNeoNameService.class.getName());
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
    static class NNSTestContract {

        static NeoNameService nameService = new NeoNameService(
                new Hash160(reverse(hexToBytes("4a65798c64826ac26765ae7a1bc4ddcc78e35ea5").toByteArray())));

        public static void testAddRoot(String root) {
            nameService.addRoot(root);
        }

        public static void testSetPrice(List<Integer> priceList) {
            nameService.setPrice(priceList);
        }

        public static int testGetPrice(int length) {
            return nameService.getPrice(length);
        }

        public static boolean testIsAvailable(String name) {
            return nameService.isAvailable(name);
        }

        public static boolean testRegister(String name, Hash160 owner) {
            return nameService.register(name, owner);
        }

        public static int testRenew(String name) {
            return nameService.renew(name);
        }

        public static void testSetAdmin(String name, Hash160 admin) {
            nameService.setAdmin(name, admin);
        }

        public static void testSetRecord(String name, int type, String data) {
            nameService.setRecord(name, type, data);
        }

        public static String testGetRecord(String name, int type) {
            return nameService.getRecord(name, type);
        }

        public static void testDeleteRecord(String name, int type) {
            nameService.deleteRecord(name, type);
        }

        public static String testResolve(String name, int type) {
            return nameService.resolve(name, type);
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
