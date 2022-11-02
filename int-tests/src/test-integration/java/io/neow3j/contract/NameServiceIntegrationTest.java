package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnresolvableDomainNameException;
import io.neow3j.contract.types.NNSName;
import io.neow3j.crypto.Sign;
import io.neow3j.helper.NeoNameServiceTestHelper;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.NameState;
import io.neow3j.protocol.core.response.RecordState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigInteger;
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

@Testcontainers
public class NameServiceIntegrationTest {

    private static Neow3j neow3j;
    private static NeoNameService nameService;

    private static final String A_RECORD = "157.0.0.1";
    private static final String CNAME_RECORD = "cnamerecord.neow3j.neo";
    private static final String TXT_RECORD = "textrecord";
    private static final String AAAA_RECORD = "3001:2:3:4:5:6:7:8";
    private static final long ONE_YEAR = 365L * 24 * 3600 * 1000;
    private static final long ONE_DAY = 24 * 3600 * 1000;

    private static final Account ALICE = Account.create();
    private static final Account BOB = Account.create();

    private static NNSName.NNSRoot ethRoot;
    private static NNSName neoDomain;
    private static NNSName ngdDomain;
    private static NNSName neow3jDomain;

    @Container
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeAll
    public static void setUp() throws Throwable {
        ethRoot = new NNSName.NNSRoot("eth");
        neoDomain = new NNSName("neo.neo");
        ngdDomain = new NNSName("ngd.neo");
        neow3jDomain = new NNSName("neow3j.neo");

        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl(), true));
        waitUntilBlockCountIsGreaterThanZero(getNeow3j());

        NeoNameServiceTestHelper.deployNNS(getNeow3j(), COMMITTEE_ACCOUNT, DEFAULT_ACCOUNT);
        NeoNameServiceTestHelper.addNNSRoot(getNeow3j(), ethRoot, COMMITTEE_ACCOUNT, DEFAULT_ACCOUNT);
        nameService = new NeoNameService(getNeow3j());

        fundAccountsWithGas(getNeow3j(), DEFAULT_ACCOUNT, CLIENT_1, CLIENT_2, ALICE, BOB);

        registerDomainFromDefault(neow3jDomain);
    }

    // region private helper methods

    private static Neow3j getNeow3j() {
        return neow3j;
    }

    private static void registerDomainFromDefault(NNSName nnsName) throws Throwable {
        register(nnsName, DEFAULT_ACCOUNT);
    }

    private static void register(NNSName nnsName, Account owner) throws Throwable {
        NeoNameServiceTestHelper.register(getNeow3j(), nnsName, owner);
    }

    private static void setAdminFromDefault(NNSName nnsName, Account admin) throws Throwable {
        NeoNameServiceTestHelper.setAdmin(getNeow3j(), nnsName, admin, DEFAULT_ACCOUNT);
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
        assertThat(nameService.ownerOf(neow3jDomain), is(DEFAULT_ACCOUNT.getScriptHash()));
    }

    @Test
    public void testProperties() throws IOException {
        Map<String, String> map = nameService.properties(neow3jDomain);

        assertThat(map.get("name"), is(neow3jDomain.getName()));
        assertThat(new BigInteger(map.get("expiration")), greaterThan(BigInteger.valueOf(getNowInMilliSeconds())));
        assertNull(map.get("admin"));
        assertThat(map.get("image"), is("https://neo3.azureedge.net/images/neons.png"));
    }

    @Test
    public void testTokens() throws IOException {
        List<byte[]> list = nameService.tokens().traverse(50);
        assertThat(list, hasItem(neow3jDomain.getBytes()));
    }

    @Test
    public void testTokensOf() throws Throwable {
        NNSName testTokenOf1 = new NNSName("testtokensof1.neo");
        NNSName testTokenOf2 = new NNSName("testtokensof2.neo");
        NNSName testTokenOf3 = new NNSName("testtokensof3.neo");
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
        NNSName domainForTransfer = new NNSName("transfer.neo");
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
        Transaction tx = nameService.addRoot(new NNSName.NNSRoot("root"))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(Sign.signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        boolean rootExists = nameService.getRootsUnwrapped().stream().anyMatch("root"::equals);
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
        List<String> roots = nameService.getRootsUnwrapped();
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
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
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
        boolean isAvailable = nameService.isAvailable(ngdDomain);
        assertFalse(isAvailable);
        isAvailable = nameService.isAvailable(neoDomain);
        assertFalse(isAvailable);
        isAvailable = nameService.isAvailable(new NNSName("available.neo"));
        assertTrue(isAvailable);
    }

    @Test
    public void testRegister() throws Throwable {
        NNSName nnsName = new NNSName("register.neo");
        boolean availableBeforeRegistration = nameService.isAvailable(nnsName);
        assertTrue(availableBeforeRegistration);

        Hash256 txHash = nameService.register(nnsName, DEFAULT_ACCOUNT.getScriptHash())
                .signers(calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        boolean availableAfterRegistration = nameService.isAvailable(nnsName);
        assertFalse(availableAfterRegistration);
    }

    @Test
    public void testRenew() throws Throwable {
        NNSName nnsName = new NNSName("renew.neo");
        registerDomainFromDefault(nnsName);
        long moreThanInOneYear = getNowInMilliSeconds() + ONE_YEAR + ONE_DAY;
        long lessThanInOneYear = getNowInMilliSeconds() + ONE_YEAR - ONE_DAY;
        Long expirationBefore = nameService.getNameState(nnsName).getExpiration();
        assertThat(expirationBefore, lessThanOrEqualTo(moreThanInOneYear));
        assertThat(expirationBefore, greaterThan(lessThanInOneYear));

        Hash256 txHash = nameService.renew(nnsName)
                .signers(calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        Long expirationAfter = nameService.getNameState(nnsName).getExpiration();
        long inTwoYears = getNowInMilliSeconds() + 2 * ONE_YEAR;
        long lessThanInTwoYears = inTwoYears - ONE_DAY;
        assertThat(expirationAfter, lessThanOrEqualTo(inTwoYears));
        assertThat(expirationAfter, greaterThan(lessThanInTwoYears));
    }

    @Test
    public void testRenew_years() throws Throwable {
        NNSName nnsName = new NNSName("renewyears.neo");
        registerDomainFromDefault(nnsName);
        Long expirationBeforeRenew = nameService.getNameState(nnsName).getExpiration();

        long moreThanInOneYear = getNowInMilliSeconds() + ONE_YEAR + ONE_DAY;
        long lessThanInOneYear = getNowInMilliSeconds() + ONE_YEAR - ONE_DAY;
        assertThat(expirationBeforeRenew, lessThanOrEqualTo(moreThanInOneYear));
        assertThat(expirationBeforeRenew, greaterThan(lessThanInOneYear));

        int renewYears = 9;

        Hash256 txHash = nameService.renew(nnsName, renewYears)
                .signers(calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        Long expirationAfterRenew = nameService.getNameState(nnsName).getExpiration();

        long renewedExpiration = expirationBeforeRenew + renewYears * ONE_YEAR;
        assertThat(expirationAfterRenew, is(renewedExpiration));
    }

    @Test
    public void testRenew_invalidYears() {
        Matcher<String> expectedMatcher = containsString("can only be renewed by at least 1, and at most 10 years.");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.renew(neow3jDomain, 0));
        assertThat(thrown.getMessage(), expectedMatcher);

        thrown = assertThrows(IllegalArgumentException.class, () -> nameService.renew(neow3jDomain, 11));
        assertThat(thrown.getMessage(), expectedMatcher);
    }

    @Test
    public void testSetAdmin() throws Throwable {
        NNSName nnsName = new NNSName("admin.neo");
        register(nnsName, CLIENT_1);

        // setRecord should throw an exception, since client2 should not be able to create a record.
        TransactionConfigurationException thrown = assertThrows(TransactionConfigurationException.class,
                () -> setRecord(nnsName, RecordType.A, A_RECORD, CLIENT_2));
        assertThat(thrown.getMessage(), containsString("The vm exited"));

        Hash256 txHash = nameService.setAdmin(nnsName, CLIENT_2.getScriptHash())
                .signers(calledByEntry(CLIENT_1), calledByEntry(CLIENT_2))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        NameState nameState = nameService.getNameState(nnsName);
        assertThat(nameState.getAdmin(), is(CLIENT_2.getScriptHash()));

        // Now as admin, client2 should be able to set a record.
        setRecord(nnsName, RecordType.A, A_RECORD, CLIENT_2);
        String aRecord = nameService.getRecord(nnsName, RecordType.A);
        assertThat(aRecord, is(A_RECORD));
    }

    @Test
    public void testCheckDomainNameAvailability() throws Throwable {
        NNSName notRegisteredName = new NNSName("notregistered.neo");
        NNSName registeredName = new NNSName("registered.neo");
        registerDomainFromDefault(registeredName);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.checkDomainNameAvailability(notRegisteredName, false));
        assertThat(thrown.getMessage(),
                is(format("The domain name '%s' is not registered.", notRegisteredName.getName())));

        thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.checkDomainNameAvailability(registeredName, true));
        assertThat(thrown.getMessage(), is(format("The domain name '%s' is already taken.", registeredName.getName())));
    }

    @Test
    public void testSetRecord_A() throws Throwable {
        setRecordFromDefault(neow3jDomain, RecordType.A, A_RECORD);
        String aRecord = nameService.getRecord(neow3jDomain, RecordType.A);
        assertThat(aRecord, is(A_RECORD));
    }

    @Test
    public void testSetRecord_CNAME() throws Throwable {
        setRecordFromDefault(neow3jDomain, RecordType.CNAME, CNAME_RECORD);
        String cnameRecord = nameService.getRecord(neow3jDomain, RecordType.CNAME);
        assertThat(cnameRecord, is(CNAME_RECORD));
    }

    @Test
    public void testSetRecord_TXT() throws Throwable {
        setRecordFromDefault(neow3jDomain, RecordType.TXT, TXT_RECORD);
        String txtRecord = nameService.getRecord(neow3jDomain, RecordType.TXT);
        assertThat(txtRecord, is(TXT_RECORD));
    }

    @Test
    public void testSetRecord_AAAA() throws Throwable {
        setRecordFromDefault(neow3jDomain, RecordType.AAAA, AAAA_RECORD);
        String aaaaRecord = nameService.getRecord(neow3jDomain, RecordType.AAAA);
        assertThat(aaaaRecord, is(AAAA_RECORD));
    }

    @Test
    public void testGetRecord_notRegistered() {
        NNSName nnsName = new NNSName("getrecordnotregistered.neo");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.getRecord(nnsName, RecordType.TXT));
        assertThat(thrown.getMessage(), containsString(
                format("The domain name '%s' might not be registered or", nnsName.getName())));
    }

    @Test
    public void testGetRecord_noRecordOfType() throws Throwable {
        NNSName nnsName = new NNSName("getrecordnorecordoftype.neo");
        registerDomainFromDefault(nnsName);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.getRecord(nnsName, RecordType.TXT));
        assertThat(thrown.getMessage(), containsString(format("Could not get a record of type '%s'", RecordType.TXT)));
    }

    @Test
    public void testGetAllRecords() throws Throwable {
        NNSName nnsName = new NNSName("getallrecords.neo");
        registerDomainFromDefault(nnsName);
        setRecordFromDefault(nnsName, RecordType.CNAME, neow3jDomain.getName());

        String txtRecord = "getAllRecordsTXT";
        setRecordFromDefault(nnsName, RecordType.TXT, txtRecord);

        Iterator<RecordState> allRecordsIter = nameService.getAllRecords(nnsName);
        List<RecordState> list = allRecordsIter.traverse(3);

        RecordState recordState1 = list.get(0);
        assertThat(recordState1.getName(), is(nnsName.getName()));
        assertThat(recordState1.getRecordType(), is(RecordType.CNAME));
        assertThat(recordState1.getData(), is(neow3jDomain.getName()));

        RecordState recordState2 = list.get(1);
        assertThat(recordState2.getName(), is(nnsName.getName()));
        assertThat(recordState2.getRecordType(), is(RecordType.TXT));
        assertThat(recordState2.getData(), is(txtRecord));
    }

    @Test
    public void testUnwrapAllRecords() throws Throwable {
        NNSName nnsName = new NNSName("unwrapallrecords.neo");
        registerDomainFromDefault(nnsName);
        setRecordFromDefault(nnsName, RecordType.CNAME, neow3jDomain.getName());
        String txtRecord = "unwrapAllRecordsTXT";
        setRecordFromDefault(nnsName, RecordType.TXT, txtRecord);

        List<RecordState> allRecords = nameService.getAllRecordsUnwrapped(nnsName);

        RecordState recordState1 = allRecords.get(0);
        assertThat(recordState1.getName(), is(nnsName.getName()));
        assertThat(recordState1.getRecordType(), is(RecordType.CNAME));
        assertThat(recordState1.getData(), is(neow3jDomain.getName()));

        RecordState recordState2 = allRecords.get(1);
        assertThat(recordState2.getName(), is(nnsName.getName()));
        assertThat(recordState2.getRecordType(), is(RecordType.TXT));
        assertThat(recordState2.getData(), is(txtRecord));
    }

    @Test
    public void testDeleteRecord() throws Throwable {
        NNSName nnsName = new NNSName("deleterecord.neo");
        registerDomainFromDefault(nnsName);

        String txtRecordVal = "textrecordfordelete";
        setRecordFromDefault(nnsName, RecordType.TXT, txtRecordVal);

        String textRecordForDelete = nameService.getRecord(nnsName, RecordType.TXT);
        assertThat(textRecordForDelete, is(txtRecordVal));

        Hash256 txHash = nameService.deleteRecord(nnsName, RecordType.TXT)
                .signers(calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.getRecord(nnsName, RecordType.TXT));
        assertThat(thrown.getMessage(), containsString("Could not get a record of type"));
    }

    @Test
    public void testSetAndResolveThirdLevelDomain() throws Throwable {
        NNSName secondLevelDomain = new NNSName("setandresolvethirdleveldomain.neo");
        registerDomainFromDefault(secondLevelDomain);
        NNSName thirdLevelDomain = new NNSName("test." + secondLevelDomain.getName());

        assertThrows(UnresolvableDomainNameException.class,
                () -> nameService.resolve(secondLevelDomain, RecordType.TXT));

        String address = Account.create().getAddress();
        setRecordFromDefault(secondLevelDomain, RecordType.TXT, address);
        assertThrows(UnresolvableDomainNameException.class,
                () -> nameService.resolve(thirdLevelDomain, RecordType.TXT));

        setRecordFromDefault(thirdLevelDomain, RecordType.CNAME, secondLevelDomain.getName());

        String resolved = nameService.resolve(thirdLevelDomain, RecordType.TXT);
        assertThat(resolved, is(address));
    }

    @Test
    public void testGetNameState() throws Throwable {
        NNSName nnsName = new NNSName("getnamestatewithbytes.neo");
        registerDomainFromDefault(nnsName);
        setAdminFromDefault(nnsName, CLIENT_1);

        NameState nameState = nameService.getNameState(nnsName);
        assertThat(nameState.getName(), is(nnsName.getName()));
        assertThat(nameState.getAdmin(), is(CLIENT_1.getScriptHash()));
        assertThat(nameState.getExpiration(), greaterThan(getNowInMilliSeconds()));

        NameState nameStateFromString = nameService.getNameState(nnsName);
        assertEquals(nameState, nameStateFromString);
    }

    // endregion

}
