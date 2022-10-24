package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnresolvableDomainNameException;
import io.neow3j.crypto.Sign;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.NameState;
import io.neow3j.protocol.core.response.RecordState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Files;
import io.neow3j.wallet.Account;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_2;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
public class NameServiceIntegrationTest {

    private static final String RESOURCE_DIR = "contract/";

    private static Neow3j neow3j;
    private static NeoNameService nameService;

    private static final String ROOT_DOMAIN = "eth";
    private static final String NEO_DOMAIN = "neo.neo";
    private static final String NGD_DOMAIN = "ngd.neo";
    private static final String DOMAIN = "neow3j.neo";
    private static final String A_RECORD = "157.0.0.1";
    private static final String CNAME_RECORD = "cnamerecord.neow3j.neo";
    private static final String TXT_RECORD = "textrecord";
    private static final String AAAA_RECORD = "3001:2:3:4:5:6:7:8";
    private static final long ONE_YEAR = 365L * 24 * 3600 * 1000;
    private static final long TEN_MINUTES = 10 * 3600 * 1000;

    private static final String NAMESERVICE_NEF = RESOURCE_DIR + "NameService.nef";
    private static final String NAMESERVICE_MANIFEST = RESOURCE_DIR + "NameService.manifest.json";

    private static final Account ALICE = Account.create();
    private static final Account BOB = Account.create();

    @Container
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeAll
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl(), true));
        waitUntilBlockCountIsGreaterThanZero(getNeow3j());
        Hash160 nameServiceHash = deployNameServiceContract();
        nameService = new NeoNameService(nameServiceHash, getNeow3j());
        // Make a transaction that can be used for the tests
        fundAccountsWithGas(getNeow3j(), DEFAULT_ACCOUNT, CLIENT_1, CLIENT_2);
        addRoot();
        registerDomainFromDefault(DOMAIN);
        setRecordFromDefault(DOMAIN, RecordType.A, A_RECORD);
        fundAccountsWithGas(neow3j, BigDecimal.valueOf(50), ALICE, BOB);
    }

    private static Hash160 deployNameServiceContract() throws Throwable {
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

    private static Neow3j getNeow3j() {
        return neow3j;
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

    // region NEP-11 methods

    @Test
    public void testTotalSupply() throws IOException {
        assertThat(nameService.getTotalSupply().intValue(), greaterThanOrEqualTo(3));
    }

    @Test
    public void testBalanceOf() throws IOException {
        int balance = nameService.balanceOf(DEFAULT_ACCOUNT.getScriptHash()).intValue();
        assertThat(balance, greaterThanOrEqualTo(1));
    }

    @Test
    public void testOwnerOf() throws IOException {
        assertThat(nameService.ownerOf(DOMAIN), is(DEFAULT_ACCOUNT.getScriptHash()));
    }

    @Test
    public void testProperties() throws IOException {
        Map<String, String> map = nameService.properties(DOMAIN);

        assertThat(map.get("name"), is(DOMAIN));
        assertThat(new BigInteger(map.get("expiration")), greaterThan(BigInteger.valueOf(getNowInMilliSeconds())));
        assertNull(map.get("admin"));
        assertThat(map.get("image"), is("https://neo3.azureedge.net/images/neons.png"));
    }

    @Test
    public void testTokens() throws IOException {
        List<byte[]> list = nameService.tokens().traverse(10);
        assertThat(list, hasItem(DOMAIN.getBytes()));
    }

    @Test
    public void testTokensOf() throws Throwable {
        String testTokenOf1 = "testtokensof1.neo";
        String testTokenOf2 = "testtokensof2.neo";
        String testTokenOf3 = "testtokensof3.neo";
        register(testTokenOf1, ALICE);
        register(testTokenOf2, ALICE);
        register(testTokenOf3, ALICE);

        List<byte[]> list = nameService.tokensOf(ALICE.getScriptHash()).traverse(10);
        assertThat(list, hasSize(3));
        assertThat(list, hasItems(
                testTokenOf1.getBytes(),
                testTokenOf2.getBytes(),
                testTokenOf3.getBytes()
        ));
    }

    @Test
    public void testTransfer() throws Throwable {
        String domainForTransfer = "transfer.neo";
        registerDomainFromDefault(domainForTransfer);
        Hash160 ownerBefore = nameService.ownerOf(domainForTransfer);
        assertThat(ownerBefore, is(DEFAULT_ACCOUNT.getScriptHash()));

        Hash256 txHash = nameService.transfer(DEFAULT_ACCOUNT, CLIENT_1.getScriptHash(), domainForTransfer)
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        Hash160 ownerAfter = nameService.ownerOf(domainForTransfer);
        assertThat(ownerAfter, is(CLIENT_1.getScriptHash()));
    }

    // endregion NEP-11 methods
    // region Custom NNS methods

    @Test
    public void testAddRoot() throws Throwable {
        Transaction tx = nameService.addRoot("root")
                .signers(AccountSigner.calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(Sign.signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        boolean rootExists = nameService.unwrapRoots().stream().anyMatch("root"::equals);
        assertTrue(rootExists);
    }

    @Test
    public void testGetRoots() throws IOException {
        Iterator<String> rootsIterator = nameService.getRoots();
        List<String> roots = rootsIterator.traverse(3);

        assertThat(roots.size(), isOneOf(2, 3));
        assertThat(roots.get(0), is("eth"));
        assertThat(roots.get(1), is("neo"));
    }

    @Test
    public void testUnwrapRoots() throws IOException {
        List<String> roots = nameService.unwrapRoots();
        assertThat(roots.size(), isOneOf(2, 3));
        assertThat(roots.get(0), is("eth"));
        assertThat(roots.get(1), is("neo"));
    }

    @Test
    public void testSetPrice() throws Throwable {
        ArrayList<BigInteger> priceList = new ArrayList<>();
        priceList.add(BigInteger.valueOf(2_00000000L));
        priceList.add(BigInteger.valueOf(-1));
        priceList.add(BigInteger.valueOf(-1));
        priceList.add(BigInteger.valueOf(120_00000000L));
        priceList.add(BigInteger.valueOf(40_00000000L));
        Transaction tx = nameService.setPrice(priceList)
                .signers(AccountSigner.calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(Sign.signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        BigInteger actualPrice = nameService.getPrice(1);
        assertThat(actualPrice, is(BigInteger.valueOf(-1)));
        actualPrice = nameService.getPrice(2);
        assertThat(actualPrice, is(BigInteger.valueOf(-1)));
        actualPrice = nameService.getPrice(3);
        assertThat(actualPrice, is(BigInteger.valueOf(120_00000000L)));
        actualPrice = nameService.getPrice(4);
        assertThat(actualPrice, is(BigInteger.valueOf(40_00000000L)));
        actualPrice = nameService.getPrice(5);
        assertThat(actualPrice, is(BigInteger.valueOf(2_00000000L)));
        actualPrice = nameService.getPrice(50);
        assertThat(actualPrice, is(BigInteger.valueOf(2_00000000L)));
    }

    @Test
    public void testGetPrice() throws IOException {
        BigInteger price = nameService.getPrice(10);
        assertThat(price, is(BigInteger.valueOf(2_00000000)));
    }

    @Test
    public void testIsAvailable() throws IOException {
        // Note: neo.neo and ngd.neo are hard coded and set in the deployment of the NameService contract.
        boolean isAvailable = nameService.isAvailable(NGD_DOMAIN);
        assertFalse(isAvailable);
        isAvailable = nameService.isAvailable(NEO_DOMAIN);
        assertFalse(isAvailable);
        isAvailable = nameService.isAvailable("available.neo");
        assertTrue(isAvailable);
    }

    @Test
    public void testRegister() throws Throwable {
        String domain = "register.neo";
        boolean availableBeforeRegistration = nameService.isAvailable(domain);
        assertTrue(availableBeforeRegistration);

        Hash256 txHash = nameService.register(domain, DEFAULT_ACCOUNT.getScriptHash())
                .signers(AccountSigner.calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        boolean availableAfterRegistration = nameService.isAvailable(domain);
        assertFalse(availableAfterRegistration);
    }

    @Test
    public void testRenew() throws Throwable {
        String domain = "renew.neo";
        registerDomainFromDefault(domain);
        long inOneYear = getNowInMilliSeconds() + ONE_YEAR;
        long lessThanInOneYear = inOneYear - TEN_MINUTES;
        Long expirationBefore = nameService.getNameState(domain).getExpiration();
        assertThat(expirationBefore, lessThanOrEqualTo(inOneYear));
        assertThat(expirationBefore, greaterThan(lessThanInOneYear));

        Hash256 txHash = nameService.renew(domain)
                .signers(AccountSigner.calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        Long expirationAfter = nameService.getNameState(domain).getExpiration();
        long inTwoYears = getNowInMilliSeconds() + 2 * ONE_YEAR;
        long lessThanInTwoYears = inTwoYears - TEN_MINUTES;
        assertThat(expirationAfter, lessThanOrEqualTo(inTwoYears));
        assertThat(expirationAfter, greaterThan(lessThanInTwoYears));
    }

    @Test
    public void testRenew_years() throws Throwable {
        String domain = "renewyears.neo";
        registerDomainFromDefault(domain);
        Long expirationBeforeRenew = nameService.getNameState(domain).getExpiration();

        long inOneYear = getNowInMilliSeconds() + ONE_YEAR;
        assertThat(expirationBeforeRenew, lessThanOrEqualTo(inOneYear));
        assertThat(expirationBeforeRenew, greaterThan(inOneYear - TEN_MINUTES));

        int renewYears = 9;

        Hash256 txHash = nameService.renew(domain, renewYears)
                .signers(AccountSigner.calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        Long expirationAfterRenew = nameService.getNameState(domain).getExpiration();

        long renewedExpiration = expirationBeforeRenew + renewYears * ONE_YEAR;
        assertThat(expirationAfterRenew, is(renewedExpiration));
    }

    @Test
    public void testRenew_invalidYears() {
        Matcher<String> expectedMatcher = containsString("can only be renewed by at least 1, and at most 10 years.");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.renew(DOMAIN, 0));
        assertThat(thrown.getMessage(), expectedMatcher);

        thrown = assertThrows(IllegalArgumentException.class, () -> nameService.renew(DOMAIN, 11));
        assertThat(thrown.getMessage(), expectedMatcher);
    }

    @Test
    public void testSetAdmin() throws Throwable {
        String domain = "admin.neo";
        register(domain, CLIENT_1);

        // setRecord should throw an exception, since client2 should not be able to create a record.
        TransactionConfigurationException thrown = assertThrows(TransactionConfigurationException.class,
                () -> setRecord(domain, RecordType.A, A_RECORD, CLIENT_2));
        assertThat(thrown.getMessage(), containsString("The vm exited"));

        Hash256 txHash = nameService.setAdmin(domain, CLIENT_2.getScriptHash())
                .signers(AccountSigner.calledByEntry(CLIENT_1), AccountSigner.calledByEntry(CLIENT_2))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        NameState nameState = nameService.getNameState(domain);
        MatcherAssert.assertThat(nameState.getAdmin(), Matchers.is(CLIENT_2.getScriptHash()));

        // Now as admin, client2 should be able to set a record.
        setRecord(domain, RecordType.A, A_RECORD, CLIENT_2);
        String aRecord = nameService.getRecord(domain, RecordType.A);
        assertThat(aRecord, is(A_RECORD));
    }

    @Test
    public void testCheckDomainNameAvailability() throws Throwable {
        String notRegisteredName = "notregistered.neo";
        String registeredName = "registered.neo";
        registerDomainFromDefault(registeredName);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.checkDomainNameAvailability(notRegisteredName, false));
        assertThat(thrown.getMessage(), is(format("The domain name '%s' is not registered.", notRegisteredName)));

        thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.checkDomainNameAvailability(registeredName, true));
        assertThat(thrown.getMessage(), is(format("The domain name '%s' is already taken.", registeredName)));
    }

    @Test
    public void testSetRecord_A() throws Throwable {
        setRecordFromDefault(DOMAIN, RecordType.A, A_RECORD);
        String aRecord = nameService.getRecord(DOMAIN, RecordType.A);
        assertThat(aRecord, is(A_RECORD));
    }

    @Test
    public void testSetRecord_CNAME() throws Throwable {
        setRecordFromDefault(DOMAIN, RecordType.CNAME, CNAME_RECORD);
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.CNAME);
        assertThat(cnameRecord, is(CNAME_RECORD));
    }

    @Test
    public void testSetRecord_TXT() throws Throwable {
        setRecordFromDefault(DOMAIN, RecordType.TXT, TXT_RECORD);
        String txtRecord = nameService.getRecord(DOMAIN, RecordType.TXT);
        assertThat(txtRecord, is(TXT_RECORD));
    }

    @Test
    public void testSetRecord_AAAA() throws Throwable {
        setRecordFromDefault(DOMAIN, RecordType.AAAA, AAAA_RECORD);
        String aaaaRecord = nameService.getRecord(DOMAIN, RecordType.AAAA);
        assertThat(aaaaRecord, is(AAAA_RECORD));
    }

    @Test
    public void testGetRecord() throws IOException {
        String ipv4 = nameService.getRecord(DOMAIN, RecordType.A);
        assertThat(ipv4, is(A_RECORD));
    }

    @Test
    public void testGetRecord_notRegistered() {
        String domain = "getrecordnotregistered.neo";
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.getRecord(domain, RecordType.TXT));
        assertThat(thrown.getMessage(), containsString(
                format("The domain name '%s' might not be registered or", domain)));
    }

    @Test
    public void testGetRecord_noRecordOfType() throws Throwable {
        String domain = "getrecordnorecordoftype.neo";
        registerDomainFromDefault(domain);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.getRecord(domain, RecordType.TXT));
        assertThat(thrown.getMessage(), containsString(format("Could not get a record of type '%s'", RecordType.TXT)));
    }

    @Test
    public void testGetAllRecords() throws Throwable {
        String domain = "getallrecords.neo";
        registerDomainFromDefault(domain);
        setRecordFromDefault(domain, RecordType.CNAME, DOMAIN);

        String record = nameService.getRecord(domain, RecordType.TXT);

        String txtRecord = "getAllRecordsTXT";
        setRecordFromDefault(domain, RecordType.TXT, txtRecord);

        Iterator<RecordState> allRecordsIter = nameService.getAllRecords(domain);
        List<RecordState> list = allRecordsIter.traverse(3);

        RecordState recordState1 = list.get(0);
        assertThat(recordState1.getName(), is(domain));
        assertThat(recordState1.getRecordType(), is(RecordType.CNAME));
        assertThat(recordState1.getData(), is(DOMAIN));

        RecordState recordState2 = list.get(1);
        assertThat(recordState2.getName(), is(domain));
        assertThat(recordState2.getRecordType(), is(RecordType.TXT));
        assertThat(recordState2.getData(), is(txtRecord));
    }

    @Test
    public void testUnwrapAllRecords() throws Throwable {
        String domain = "unwrapallrecords.neo";
        registerDomainFromDefault(domain);
        setRecordFromDefault(domain, RecordType.CNAME, DOMAIN);
        String txtRecord = "unwrapAllRecordsTXT";
        setRecordFromDefault(domain, RecordType.TXT, txtRecord);

        List<RecordState> allRecords = nameService.unwrapAllRecords(domain);

        RecordState recordState1 = allRecords.get(0);
        assertThat(recordState1.getName(), is(domain));
        assertThat(recordState1.getRecordType(), is(RecordType.CNAME));
        assertThat(recordState1.getData(), is(DOMAIN));

        RecordState recordState2 = allRecords.get(1);
        assertThat(recordState2.getName(), is(domain));
        assertThat(recordState2.getRecordType(), is(RecordType.TXT));
        assertThat(recordState2.getData(), is(txtRecord));
    }

    @Test
    public void testDeleteRecord() throws Throwable {
        String domain = "deleterecord.neo";
        registerDomainFromDefault(domain);

        String txtRecordVal = "textrecordfordelete";
        setRecordFromDefault(domain, RecordType.TXT, txtRecordVal);

        String textRecordForDelete = nameService.getRecord(domain, RecordType.TXT);
        assertThat(textRecordForDelete, is(txtRecordVal));

        Hash256 txHash = nameService.deleteRecord(domain, RecordType.TXT)
                .signers(AccountSigner.calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.getRecord(domain, RecordType.TXT));
        assertThat(thrown.getMessage(), containsString("Could not get a record of type"));
    }

    @Test
    public void testSetAndResolveThirdLevelDomain() throws Throwable {
        String secondLevelDomain = "setandresolvethirdleveldomain.neo";
        registerDomainFromDefault(secondLevelDomain);
        String thirdLevelDomain = "test." + secondLevelDomain;

        assertThrows(UnresolvableDomainNameException.class,
                () -> nameService.resolve(secondLevelDomain, RecordType.TXT));

        String address = Account.create().getAddress();
        setRecordFromDefault(secondLevelDomain, RecordType.TXT, address);
        assertThrows(UnresolvableDomainNameException.class,
                () -> nameService.resolve(thirdLevelDomain, RecordType.TXT));

        setRecordFromDefault(thirdLevelDomain, RecordType.CNAME, secondLevelDomain);

        String resolved = nameService.resolve(thirdLevelDomain, RecordType.TXT);
        assertThat(resolved, is(address));
    }

    @Test
    public void testGetNameState() throws Throwable {
        String domain = "getnamestatewithbytes.neo";
        registerDomainFromDefault(domain);
        setAdminFromDefault(domain, CLIENT_1);

        NameState nameState = nameService.getNameState(domain.getBytes());
        assertThat(nameState.getName(), is(domain));
        assertThat(nameState.getAdmin(), is(CLIENT_1.getScriptHash()));
        assertThat(nameState.getExpiration(), greaterThan(getNowInMilliSeconds()));

        NameState nameStateFromString = nameService.getNameState(domain);
        assertEquals(nameState, nameStateFromString);
    }

    // endregion Custom NNS methods

}
