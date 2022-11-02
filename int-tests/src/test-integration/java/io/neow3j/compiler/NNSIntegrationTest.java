package io.neow3j.compiler;

import io.neow3j.contract.types.NNSName;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.List;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.contracts.NeoNameService;
import io.neow3j.helper.NeoNameServiceTestHelper;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.stream.Collectors;

import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_2;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.transaction.AccountSigner.global;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.Numeric.reverseHexString;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NNSIntegrationTest {

    private static final long ONE_YEAR = 365L * 24 * 3600 * 1000;

    private static final Account ALICE = Account.create();
    private static final Account BOB = Account.create();

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(TestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @BeforeAll
    public static void setUp() throws Throwable {
        NeoNameServiceTestHelper.deployNNS(getNeow3j(), COMMITTEE_ACCOUNT, DEFAULT_ACCOUNT);
        NeoNameServiceTestHelper.addNNSRoot(getNeow3j(), new NNSName.NNSRoot("eth"), COMMITTEE_ACCOUNT, DEFAULT_ACCOUNT);
        ct.setHash(getNeow3j().getNNSResolver());

        fundAccountsWithGas(getNeow3j(), DEFAULT_ACCOUNT, CLIENT_1, CLIENT_2);
        fundAccountsWithGas(getNeow3j(), BigDecimal.valueOf(50), ALICE, BOB);
    }

    // region private helper methods

    private static Neow3j getNeow3j() {
        return ct.getNeow3j();
    }

    private static void registerDomainFromDefault(NNSName name) throws Throwable {
        register(name, DEFAULT_ACCOUNT);
    }

    private static void register(NNSName nnsName, Account owner) throws Throwable {
        NeoNameServiceTestHelper.register(getNeow3j(), nnsName, owner);
    }

    private static void setAdminFromDefault(NNSName nnsName, Account admin) throws Throwable {
        setAdmin(nnsName, admin, DEFAULT_ACCOUNT);
    }

    private static void setAdmin(NNSName nnsName, Account admin, Account owner) throws Throwable {
        NeoNameServiceTestHelper.setAdmin(getNeow3j(), nnsName, admin, owner);
    }

    private static void setRecordFromDefault(NNSName nnsName, RecordType type, String data) throws Throwable {
        setRecord(nnsName, type, data, DEFAULT_ACCOUNT);
    }

    private static void setRecord(NNSName nnsName, RecordType type, String data, Account signer) throws Throwable {
        NeoNameServiceTestHelper.setRecord(getNeow3j(), nnsName, type, data, signer);
    }

    private static long getNowInMilliSeconds() {
        return new Date().getTime();
    }

    // endregion

    @Test
    public void testAddRoot() throws IOException {
        ct.signWithCommitteeAccount();
        InvocationResult result = ct.callInvokeFunction(testName, string("root")).getInvocationResult();
        assertNull(result.getException());
        assertNull(result.getStack().get(0).getValue());
    }

    @Test
    public void testRoots() throws IOException {
        java.util.List<String> roots = ct.callAndTraverseIterator(testName).stream()
                .map(StackItem::getString)
                .collect(Collectors.toList());
        assertThat(roots, containsInAnyOrder("neo", "eth"));
    }

    @Test
    public void testSetGetPrice() throws Throwable {
        BigInteger price = ct.callInvokeFunction("getPrice", integer(3)).getInvocationResult().getStack().get(0)
                .getInteger();
        assertThat(price, is(BigInteger.valueOf(200_00000000L)));

        ct.signWithCommitteeAccount();
        ct.invokeFunctionAndAwaitExecution("setPrice",
                array(BigInteger.valueOf(1_00000000L),
                        BigInteger.valueOf(-1),
                        BigInteger.valueOf(-1),
                        BigInteger.valueOf(30_00000000L),
                        BigInteger.valueOf(2_00000000L)));
        BigInteger newPrice = ct.callInvokeFunction("getPrice", integer(3)).getInvocationResult().getStack().get(0)
                .getInteger();
        assertThat(newPrice, is(BigInteger.valueOf(30_00000000L)));

        ct.invokeFunctionAndAwaitExecution("setPrice",
                array(BigInteger.valueOf(2_00000000L),
                        BigInteger.valueOf(-1),
                        BigInteger.valueOf(-1),
                        BigInteger.valueOf(200_00000000L),
                        BigInteger.valueOf(70_00000000L)));
        BigInteger oldPrice = ct.callInvokeFunction("getPrice", integer(3)).getInvocationResult().getStack().get(0)
                .getInteger();
        assertThat(oldPrice, is(BigInteger.valueOf(200_00000000L)));
    }

    @Test
    public void testIsAvailable() throws Throwable {
        NNSName nnsName = new NNSName("isavailable.neo");
        InvocationResult result = ct.callInvokeFunction(testName, string(nnsName.getName())).getInvocationResult();
        assertTrue(result.getStack().get(0).getBoolean());

        registerDomainFromDefault(nnsName);

        result = ct.callInvokeFunction(testName, string(nnsName.getName())).getInvocationResult();
        assertFalse(result.getStack().get(0).getBoolean());
    }

    @Test
    public void testRegister() throws Throwable {
        Hash256 hash = ct.invokeFunctionAndAwaitExecution(testName, asList(string("register.neo"),
                hash160(ALICE)), global(ALICE));
        Await.waitUntilTransactionIsExecuted(hash, getNeow3j());
        assertTrue(getNeow3j().getApplicationLog(hash).send().getApplicationLog().getExecutions().get(0)
                .getStack().get(0).getBoolean());
    }

    @Test
    public void testRenew() throws Throwable {
        NNSName nnsName = new NNSName("testrenew.neo");
        register(nnsName, DEFAULT_ACCOUNT);

        Hash256 hash = ct.invokeFunctionAndAwaitExecution(testName, string(nnsName.getName()));
        BigInteger expiration = getNeow3j().getApplicationLog(hash).send()
                .getApplicationLog().getExecutions().get(0).getStack().get(0).getInteger();
        assertThat(expiration.longValue(), lessThan(getNowInMilliSeconds() + 2 * ONE_YEAR));
    }

    @Test
    public void testRenewYears() throws Throwable {
        NNSName nnsName = new NNSName("testrenewyears.neo");
        register(nnsName, DEFAULT_ACCOUNT);

        Hash256 hash = ct.invokeFunctionAndAwaitExecution(testName, string(nnsName.getName()), integer(4));
        BigInteger expiration = getNeow3j().getApplicationLog(hash).send()
                .getApplicationLog().getExecutions().get(0).getStack().get(0).getInteger();
        assertThat(expiration.longValue(), lessThan(getNowInMilliSeconds() + 5 * ONE_YEAR));
    }

    @Test
    public void testSetAdmin() throws Throwable {
        NNSName nnsName = new NNSName("setadmin.neo");
        register(nnsName, DEFAULT_ACCOUNT);

        ct.signWithDefaultAccount();
        ct.invokeFunctionAndAwaitExecution(testName, asList(string(nnsName.getName()), hash160(CLIENT_1)),
                global(CLIENT_1));
        InvocationResult result = ct.callInvokeFunction("getAdmin", string(nnsName.getName())).getInvocationResult();

        assertThat(result.getStack().get(0).getAddress(), is(CLIENT_1.getAddress()));
    }

    @Test
    public void testRecord() throws Throwable {
        NNSName nnsName = new NNSName("testrecord.neo");
        register(nnsName, DEFAULT_ACCOUNT);

        String aRecord = "157.255.0.1";
        setRecordFromDefault(nnsName, RecordType.A, aRecord);
        InvocationResult result = ct.callInvokeFunction("getRecord", string(nnsName.getName()),
                        integer(RecordType.A.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(aRecord));

        String cnameRecord = "hello.testrecord.neo";
        setRecordFromDefault(nnsName, RecordType.CNAME, cnameRecord);
        result = ct.callInvokeFunction("getRecord", string(nnsName.getName()),
                        integer(RecordType.CNAME.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(cnameRecord));

        String txtRecord = "txt-record";
        setRecordFromDefault(nnsName, RecordType.TXT, txtRecord);
        result = ct.callInvokeFunction("getRecord", string(nnsName.getName()),
                        integer(RecordType.TXT.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(txtRecord));

        String aaaaRecord = "2001:1db8:0::";
        setRecordFromDefault(nnsName, RecordType.AAAA, aaaaRecord);
        result = ct.callInvokeFunction("getRecord", string(nnsName.getName()),
                        integer(RecordType.AAAA.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(aaaaRecord));

        // Test retrieving all records
        java.util.List<java.util.List<StackItem>> allRecords = ct.callAndTraverseIterator("getAllRecords",
                        string(nnsName.getName()))
                .stream().map(StackItem::getList).collect(Collectors.toList());

        java.util.List<StackItem> aRecordRegistered = allRecords.get(0);
        assertThat(aRecordRegistered.get(2).getString(), is(aRecord));

        java.util.List<StackItem> cnameRecordRegistered = allRecords.get(1);
        assertThat(cnameRecordRegistered.get(2).getString(), is(cnameRecord));

        java.util.List<StackItem> txtRecordRegistered = allRecords.get(2);
        assertThat(txtRecordRegistered.get(2).getString(), is(txtRecord));

        java.util.List<StackItem> aaaaRecordRegistered = allRecords.get(3);
        assertThat(aaaaRecordRegistered.get(2).getString(), is(aaaaRecord));
    }

    @Test
    public void testDeleteRecord() throws Throwable {
        NNSName nnsName = new NNSName("testdeleterecord.neo");
        register(nnsName, DEFAULT_ACCOUNT);

        String aRecord = "157.255.0.1";
        setRecordFromDefault(nnsName, RecordType.A, aRecord);
        InvocationResult result = ct.callInvokeFunction("getRecord", string(nnsName.getName()),
                        integer(RecordType.A.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(aRecord));

        ct.signWithDefaultAccount();
        ct.invokeFunctionAndAwaitExecution("deleteRecord", string(nnsName.getName()),
                integer(RecordType.A.byteValue()));
        result = ct.callInvokeFunction("getRecord", string(nnsName.getName()), integer(RecordType.A.byteValue()))
                .getInvocationResult();

        assertNull(result.getStack().get(0).getValue());
    }

    @Test
    public void testResolveThirdLevelDomain() throws Throwable {
        NNSName nnsName = new NNSName("testresolvethirdleveldomain.neo");
        register(nnsName, DEFAULT_ACCOUNT);

        NNSName thirdLevelNNSName = new NNSName("level." + nnsName.getName());

        setRecordFromDefault(thirdLevelNNSName, RecordType.CNAME, nnsName.getName());
        setRecordFromDefault(nnsName, RecordType.TXT, CLIENT_1.getAddress());

        InvocationResult result = ct.callInvokeFunction("resolve", string(thirdLevelNNSName.getName()),
                        integer(RecordType.TXT.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(CLIENT_1.getAddress()));
    }

    @Test
    public void testProperties() throws Throwable {
        NNSName nnsName = new NNSName("testproperties.neo");
        register(nnsName, DEFAULT_ACCOUNT);

        setAdminFromDefault(nnsName, CLIENT_1);
        InvocationResult result = ct.callInvokeFunction("properties", string(nnsName.getName()))
                .getInvocationResult();
        java.util.Map<String, StackItem> propertiesMap = result.getStack().get(0).getMap()
                .entrySet().stream().collect(Collectors.toMap(
                        e -> e.getKey().getString(),
                        java.util.Map.Entry::getValue
                ));
        assertThat(propertiesMap.get("admin").getAddress(), is(CLIENT_1.getAddress()));
        assertThat(propertiesMap.get("expiration").getInteger().longValue(),
                lessThan(getNowInMilliSeconds() + ONE_YEAR));
        assertThat(propertiesMap.get("image").getString(), is("https://neo3.azureedge.net/images/neons.png"));
        assertThat(propertiesMap.get("name").getString(), is(nnsName.getName()));
    }

    @Test
    public void testTransfer() throws Throwable {
        NNSName nnsName = new NNSName("transfer.neo");
        register(nnsName, DEFAULT_ACCOUNT);

        InvocationResult result = ct.callInvokeFunction("ownerOf", string(nnsName.getName()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getAddress(), is(DEFAULT_ACCOUNT.getAddress()));

        ct.signWithDefaultAccount();
        ct.invokeFunctionAndAwaitExecution("transfer", hash160(CLIENT_1), string(nnsName.getName()));

        result = ct.callInvokeFunction("ownerOf", string(nnsName.getName())).getInvocationResult();
        assertThat(result.getStack().get(0).getAddress(), is(CLIENT_1.getAddress()));
    }

    @Test
    public void testContractHash() throws IOException {
        InvocationResult result = ct.callInvokeFunction("getOfficialNNSHash").getInvocationResult();
        io.neow3j.types.Hash160 actualHash = new io.neow3j.types.Hash160(
                reverseHexString(result.getStack().get(0).getHexString()));
        assertThat(actualHash, is(new io.neow3j.types.Hash160("0x50ac1c37690cc2cfc594472833cf57505d5f46de")));
    }

    @Permission(contract = "*")
    static class TestContract {

        public static void testAddRoot(String root) {
            getNeoNameService().addRoot(root);
        }

        public static Iterator<String> testRoots() {
            return getNeoNameService().roots();
        }

        public static void setPrice(List<Integer> priceList) {
            getNeoNameService().setPrice(priceList);
        }

        public static int getPrice(int length) {
            return getNeoNameService().getPrice(length);
        }

        public static boolean testIsAvailable(String name) {
            return getNeoNameService().isAvailable(name);
        }

        public static boolean testRegister(String name, Hash160 owner) {
            return getNeoNameService().register(name, owner);
        }

        public static int testRenew(String name) {
            return getNeoNameService().renew(name);
        }

        public static int testRenewYears(String name, int years) {
            return getNeoNameService().renew(name, years);
        }

        public static void testSetAdmin(String name, Hash160 admin) {
            getNeoNameService().setAdmin(name, admin);
        }

        public static Hash160 getAdmin(String name) {
            return (Hash160) getNeoNameService().properties(name).get("admin");
        }

        public static Map<String, Object> properties(String name) {
            return getNeoNameService().properties(name);
        }

        public static void setRecord(String name, int type, String data) {
            getNeoNameService().setRecord(name, type, data);
        }

        public static String getRecord(String name, int type) {
            return getNeoNameService().getRecord(name, type);
        }

        public static Iterator<String> getAllRecords(String domain) {
            return getNeoNameService().getAllRecords(domain);
        }

        public static void deleteRecord(String name, int type) {
            getNeoNameService().deleteRecord(name, type);
        }

        public static String resolve(String name, int type) {
            return getNeoNameService().resolve(name, type);
        }

        public static boolean transfer(Hash160 to, String name) {
            return getNeoNameService().transfer(to, name, null);
        }

        public static Hash160 ownerOf(String name) {
            return getNeoNameService().ownerOf(name);
        }

        public static void setHash(Hash160 contractHash) {
            Storage.put(Storage.getStorageContext(), 0xff, contractHash);
        }

        private static NeoNameService getNeoNameService() {
            return new NeoNameService(Storage.getHash160(Storage.getReadOnlyContext(), 0xff));
        }

        public static Hash160 getOfficialNNSHash() {
            return new NeoNameService().getHash();
        }

    }

}
