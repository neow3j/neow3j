package io.neow3j.compiler;

import io.neow3j.contract.ContractManagement;
import io.neow3j.contract.NameServiceIntegrationTest;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.SmartContract;
import io.neow3j.crypto.Sign;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.List;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.contracts.NeoNameService;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Await;
import io.neow3j.utils.Files;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Date;
import java.util.stream.Collectors;

import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_2;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.AccountSigner.global;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
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

    private static final String ROOT_DOMAIN = "eth";
    private static final String DOMAIN = "neow3j.neo";
    private static final String A_RECORD = "157.0.0.1";
    private static final long ONE_YEAR = 365L * 24 * 3600 * 1000;

    private static final String RESOURCE_DIR = "contract/";
    private static final String NAMESERVICE_NEF = RESOURCE_DIR + "NameService.nef";
    private static final String NAMESERVICE_MANIFEST = RESOURCE_DIR + "NameService.manifest.json";

    private static final Account ALICE = Account.create();
    private static final Account BOB = Account.create();

    private static io.neow3j.contract.NeoNameService nameService;

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(TestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @BeforeAll
    public static void setUp() throws Throwable {
        io.neow3j.types.Hash160 nameServiceHash = deployNameServiceContract();
        nameService = new io.neow3j.contract.NeoNameService(nameServiceHash, getNeow3j());
        ct.setHash(nameService.getScriptHash());

        // Make a transaction that can be used for the tests
        fundAccountsWithGas(getNeow3j(), DEFAULT_ACCOUNT, CLIENT_1, CLIENT_2);
        addRoot();
        registerDomainFromDefault(DOMAIN);
        setRecordFromDefault(DOMAIN, RecordType.A, A_RECORD);
        fundAccountsWithGas(getNeow3j(), BigDecimal.valueOf(50), ALICE, BOB);
    }

    private static Neow3j getNeow3j() {
        return ct.getNeow3j();
    }

    private static io.neow3j.types.Hash160 deployNameServiceContract() throws Throwable {
        URL r = NameServiceIntegrationTest.class.getClassLoader().getResource(NAMESERVICE_NEF);
        byte[] nefBytes = Files.readBytes(new File(r.toURI()));
        r = NameServiceIntegrationTest.class.getClassLoader().getResource(NAMESERVICE_MANIFEST);
        byte[] manifestBytes = Files.readBytes(new File(r.toURI()));

        Transaction tx = new ContractManagement(getNeow3j())
                .invokeFunction("deploy", byteArray(nefBytes), byteArray(manifestBytes))
                .signers(AccountSigner.calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(Sign.signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        long checksum = NefFile.getCheckSumAsInteger(NefFile.computeChecksumFromBytes(nefBytes));
        return SmartContract.calcContractHash(COMMITTEE_ACCOUNT.getScriptHash(), checksum, "NameService");
    }

    private static void addRoot() throws Throwable {
        Transaction tx = nameService.addRoot(ROOT_DOMAIN)
                .signers(AccountSigner.calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(Sign.signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
    }

    private static void registerDomainFromDefault(String domain) throws Throwable {
        register(domain, DEFAULT_ACCOUNT);
    }

    private static void register(String domain, Account owner) throws Throwable {
        Hash256 txHash = nameService.register(domain, owner.getScriptHash())
                .signers(calledByEntry(owner))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
    }

    private static void setAdminFromDefault(String domain, Account admin) throws Throwable {
        setAdmin(domain, admin, DEFAULT_ACCOUNT);
    }

    private static void setAdmin(String domain, Account admin, Account owner) throws Throwable {
        Hash256 txHash = nameService.setAdmin(domain, admin.getScriptHash())
                .signers(AccountSigner.calledByEntry(owner),
                        AccountSigner.calledByEntry(admin))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
    }

    private static void setRecordFromDefault(String domain, RecordType type, String data) throws Throwable {
        setRecord(domain, type, data, DEFAULT_ACCOUNT);
    }

    private static void setRecord(String domain, RecordType type, String data, Account signer) throws Throwable {
        Hash256 txHash = nameService.setRecord(domain, type, data)
                .signers(calledByEntry(signer))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
    }

    private static long getNowInMilliSeconds() {
        return new Date().getTime();
    }

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
    public void testIsAvailable() throws IOException {
        InvocationResult result = ct.callInvokeFunction(testName, string("isavailable.neo")).getInvocationResult();
        assertTrue(result.getStack().get(0).getBoolean());

        result = ct.callInvokeFunction(testName, string(DOMAIN)).getInvocationResult();
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
        String domain = "testrenew.neo";
        register(domain, DEFAULT_ACCOUNT);

        Hash256 hash = ct.invokeFunctionAndAwaitExecution(testName, string(domain));
        BigInteger expiration = getNeow3j().getApplicationLog(hash).send()
                .getApplicationLog().getExecutions().get(0).getStack().get(0).getInteger();
        assertThat(expiration.longValue(), lessThan(getNowInMilliSeconds() + 2 * ONE_YEAR));
    }

    @Test
    public void testRenewYears() throws Throwable {
        String domain = "testrenewyears.neo";
        register(domain, DEFAULT_ACCOUNT);

        Hash256 hash = ct.invokeFunctionAndAwaitExecution(testName, string(domain), integer(4));
        BigInteger expiration = getNeow3j().getApplicationLog(hash).send()
                .getApplicationLog().getExecutions().get(0).getStack().get(0).getInteger();
        assertThat(expiration.longValue(), lessThan(getNowInMilliSeconds() + 5 * ONE_YEAR));
    }

    @Test
    public void testSetAdmin() throws Throwable {
        String domain = "setadmin.neo";
        register(domain, DEFAULT_ACCOUNT);

        ct.signWithDefaultAccount();
        ct.invokeFunctionAndAwaitExecution(testName, asList(string(domain), hash160(CLIENT_1)), global(CLIENT_1));
        InvocationResult result = ct.callInvokeFunction("getAdmin", string(domain)).getInvocationResult();

        assertThat(result.getStack().get(0).getAddress(), is(CLIENT_1.getAddress()));
    }

    @Test
    public void testRecord() throws Throwable {
        String domain = "testrecord.neo";
        register(domain, DEFAULT_ACCOUNT);

        String aRecord = "157.255.0.1";
        setRecordFromDefault(domain, RecordType.A, aRecord);
        InvocationResult result = ct.callInvokeFunction("getRecord", string(domain),
                        integer(RecordType.A.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(aRecord));

        String cnameRecord = "hello.testrecord.neo";
        setRecordFromDefault(domain, RecordType.CNAME, cnameRecord);
        result = ct.callInvokeFunction("getRecord", string(domain),
                        integer(RecordType.CNAME.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(cnameRecord));

        String txtRecord = "txt-record";
        setRecordFromDefault(domain, RecordType.TXT, txtRecord);
        result = ct.callInvokeFunction("getRecord", string(domain),
                        integer(RecordType.TXT.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(txtRecord));

        String aaaaRecord = "2001:1db8:0::";
        setRecordFromDefault(domain, RecordType.AAAA, aaaaRecord);
        result = ct.callInvokeFunction("getRecord", string(domain),
                        integer(RecordType.AAAA.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(aaaaRecord));

        // Test retrieving all records
        java.util.List<java.util.List<StackItem>> allRecords = ct.callAndTraverseIterator("getAllRecords",
                        string(domain))
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
        String domain = "testdeleterecord.neo";
        register(domain, DEFAULT_ACCOUNT);

        String aRecord = "157.255.0.1";
        setRecordFromDefault(domain, RecordType.A, aRecord);
        InvocationResult result = ct.callInvokeFunction("getRecord", string(domain),
                        integer(RecordType.A.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(aRecord));

        ct.signWithDefaultAccount();
        ct.invokeFunctionAndAwaitExecution("deleteRecord", string(domain), integer(RecordType.A.byteValue()));
        result = ct.callInvokeFunction("getRecord", string(domain), integer(RecordType.A.byteValue()))
                .getInvocationResult();

        assertNull(result.getStack().get(0).getValue());
    }

    @Test
    public void testResolveThirdLevelDomain() throws Throwable {
        String domain = "testresolvethirdleveldomain.neo";
        register(domain, DEFAULT_ACCOUNT);

        String thirdLevelDomain = "level." + domain;

        setRecordFromDefault(thirdLevelDomain, RecordType.CNAME, domain);
        setRecordFromDefault(domain, RecordType.TXT, CLIENT_1.getAddress());

        InvocationResult result = ct.callInvokeFunction("resolve", string(thirdLevelDomain),
                        integer(RecordType.TXT.byteValue()))
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is(CLIENT_1.getAddress()));
    }

    @Test
    public void testProperties() throws Throwable {
        String domain = "testproperties.neo";
        register(domain, DEFAULT_ACCOUNT);

        setAdminFromDefault(domain, CLIENT_1);
        InvocationResult result = ct.callInvokeFunction("properties", string(domain)).getInvocationResult();
        java.util.Map<String, StackItem> propertiesMap = result.getStack().get(0).getMap()
                .entrySet().stream().collect(Collectors.toMap(
                        e -> e.getKey().getString(),
                        java.util.Map.Entry::getValue
                ));
        assertThat(propertiesMap.get("admin").getAddress(), is(CLIENT_1.getAddress()));
        assertThat(propertiesMap.get("expiration").getInteger().longValue(),
                lessThan(getNowInMilliSeconds() + ONE_YEAR));
        assertThat(propertiesMap.get("image").getString(), is("https://neo3.azureedge.net/images/neons.png"));
        assertThat(propertiesMap.get("name").getString(), is(domain));
    }

    @Test
    public void testTransfer() throws Throwable {
        String domain = "transfer.neo";
        register(domain, DEFAULT_ACCOUNT);

        InvocationResult result = ct.callInvokeFunction("ownerOf", string(domain)).getInvocationResult();
        assertThat(result.getStack().get(0).getAddress(), is(DEFAULT_ACCOUNT.getAddress()));

        ct.signWithDefaultAccount();
        ct.invokeFunctionAndAwaitExecution("transfer", hash160(CLIENT_1), string(domain));

        result = ct.callInvokeFunction("ownerOf", string(domain)).getInvocationResult();
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
