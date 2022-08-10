package io.neow3j.contract;

import io.neow3j.crypto.Sign;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.NameState;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.InvocationFaultStateException;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.transaction.Witness;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Files;
import io.neow3j.wallet.Account;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
public class NameServiceIntegrationTest {

    private static final String RESOURCE_DIR = "contract/";

    private static Neow3j neow3j;
    private static NeoNameService nameService;


    private static final String ROOT_DOMAIN = "neo";
    private static final String DOMAIN = "neow3j.neo";
    private static final String A_RECORD = "157.0.0.1";
    private static final String CNAME_RECORD = "cnamerecord.neow3j.neo";
    private static final String TXT_RECORD = "textrecord";
    private static final String AAAA_RECORD = "3001:2:3:4:5:6:7:8";
    private static final long ONE_YEAR_IN_MILLISECONDS = 365L * 24 * 3600 * 1000;
    private static final long BUFFER_MILLISECONDS = 3600 * 1000;

    private static final String NAMESERVICE_NEF = RESOURCE_DIR + "NameService.nef";
    private static final String NAMESERVICE_MANIFEST = RESOURCE_DIR + "NameService.manifest.json";

    @Container
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeAll
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(getNeow3j());
        Hash160 nameServiceHash = deployNameServiceContract();
        nameService = new NeoNameService(nameServiceHash, getNeow3j());
        // Make a transaction that can be used for the tests
        IntegrationTestHelper.fundAccountsWithGas(getNeow3j(), IntegrationTestHelper.DEFAULT_ACCOUNT, IntegrationTestHelper.CLIENT_1, IntegrationTestHelper.CLIENT_2);
        addRoot();
        registerDomainFromDefault(DOMAIN);
        setRecordFromDefault(DOMAIN, RecordType.A, A_RECORD);
    }

    private static Hash160 deployNameServiceContract() throws Throwable {
        URL r = NameServiceIntegrationTest.class.getClassLoader().getResource(NAMESERVICE_NEF);
        byte[] nefBytes = Files.readBytes(new File(r.toURI()));
        r = NameServiceIntegrationTest.class.getClassLoader().getResource(NAMESERVICE_MANIFEST);
        byte[] manifestBytes = Files.readBytes(new File(r.toURI()));

        Transaction tx = new ContractManagement(getNeow3j())
                .invokeFunction("deploy", byteArray(nefBytes), byteArray(manifestBytes))
                .signers(AccountSigner.calledByEntry(IntegrationTestHelper.COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                Arrays.asList(Sign.signMessage(tx.getHashData(), IntegrationTestHelper.DEFAULT_ACCOUNT.getECKeyPair())),
                IntegrationTestHelper.COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
        return SmartContract.calcContractHash(IntegrationTestHelper.COMMITTEE_ACCOUNT.getScriptHash(),
                NefFile.getCheckSumAsInteger(NefFile.computeChecksumFromBytes(nefBytes)), "NameService");
    }

    private static Neow3j getNeow3j() {
        return neow3j;
    }

    private static void addRoot() throws Throwable {
        Transaction tx = nameService.addRoot(ROOT_DOMAIN)
                .signers(AccountSigner.calledByEntry(IntegrationTestHelper.COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                Arrays.asList(Sign.signMessage(tx.getHashData(), IntegrationTestHelper.DEFAULT_ACCOUNT.getECKeyPair())),
                IntegrationTestHelper.COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
    }

    private static void registerDomainFromDefault(String domain) throws Throwable {
        register(domain, IntegrationTestHelper.DEFAULT_ACCOUNT);
    }

    private static void register(String domain, Account owner) throws Throwable {
        TransactionBuilder b = nameService.register(domain, owner.getScriptHash());
        b.signers(calledByEntry(owner));
        Transaction tx = b.sign();
        NeoSendRawTransaction response = tx.send();
        NeoSendRawTransaction.RawTransaction sendRawTransaction = response.getSendRawTransaction();
        Hash256 hash = sendRawTransaction.getHash();
        waitUntilTransactionIsExecuted(hash, getNeow3j());
    }

    private static void setRecordFromDefault(String domain, RecordType type, String data) throws Throwable {
        setRecord(domain, type, data, IntegrationTestHelper.DEFAULT_ACCOUNT);
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
    public void testGetPrice() throws IOException {
        BigInteger price = nameService.getPrice(10);
        assertThat(price, is(BigInteger.valueOf(1_00000000)));
    }

    @Test
    public void testIsAvailable() throws IOException {
        boolean isAvailable = nameService.isAvailable(DOMAIN);
        assertFalse(isAvailable);
    }

    @Test
    public void testOwnerOf() throws IOException {
        Hash160 owner = nameService.ownerOf(DOMAIN);
        MatcherAssert.assertThat(owner, Matchers.is(IntegrationTestHelper.DEFAULT_ACCOUNT.getScriptHash()));
    }

    @Test
    public void testBalanceOf() throws IOException {
        BigInteger bigInteger = nameService.balanceOf(IntegrationTestHelper.DEFAULT_ACCOUNT.getScriptHash());
        assertThat(bigInteger, greaterThanOrEqualTo(BigInteger.ONE));
    }

    @Test
    public void testProperties() throws IOException {
        NameState nameState = nameService.getNameState(DOMAIN);
        assertThat(nameState.getName(), is(DOMAIN));
        long inOneYear = getNowInMilliSeconds() + ONE_YEAR_IN_MILLISECONDS;
        long lessThanInOneYear = getNowInMilliSeconds() + ONE_YEAR_IN_MILLISECONDS - BUFFER_MILLISECONDS;
        assertThat(nameState.getExpiration(), lessThanOrEqualTo(inOneYear));
        assertThat(nameState.getExpiration(), greaterThan(lessThanInOneYear));
    }

    @Test
    public void testGetRecord() throws IOException {
        String ipv4 = nameService.getRecord(DOMAIN, RecordType.A);
        assertThat(ipv4, is(A_RECORD));
    }

    @Test
    public void testResolve() throws IOException {
        String ipv4 = nameService.resolve(DOMAIN, RecordType.A);
        assertThat(ipv4, is(A_RECORD));
    }

    @Test
    public void testAddRoot() throws Throwable {
        Transaction tx = nameService.addRoot("root")
                .signers(AccountSigner.calledByEntry(IntegrationTestHelper.COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                Arrays.asList(Sign.signMessage(tx.getHashData(), IntegrationTestHelper.DEFAULT_ACCOUNT.getECKeyPair())),
                IntegrationTestHelper.COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        boolean rootExists = false;
        try {
            // Any second-level domain name should still be available for the added root domain.
            rootExists = nameService.isAvailable("neow3j.root");
        } catch (IllegalArgumentException e) {
            fail();
        }
        assertTrue(rootExists);
    }

    @Test
    public void testSetPrice() throws Throwable {
        ArrayList<BigInteger> priceList = new ArrayList<>();
        priceList.add(BigInteger.valueOf(5_00000000));
        priceList.add(BigInteger.valueOf(1_00000000));
        priceList.add(BigInteger.valueOf(2_00000000));
        priceList.add(BigInteger.valueOf(3_00000000));
        priceList.add(BigInteger.valueOf(4_00000000));
        Transaction tx = nameService.setPrice(priceList)
                .signers(AccountSigner.calledByEntry(IntegrationTestHelper.COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                Arrays.asList(Sign.signMessage(tx.getHashData(), IntegrationTestHelper.DEFAULT_ACCOUNT.getECKeyPair())),
                IntegrationTestHelper.COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        BigInteger actualPrice = nameService.getPrice(1);
        assertThat(actualPrice, is(BigInteger.valueOf(1_00000000)));
        actualPrice = nameService.getPrice(2);
        assertThat(actualPrice, is(BigInteger.valueOf(2_00000000)));
        actualPrice = nameService.getPrice(3);
        assertThat(actualPrice, is(BigInteger.valueOf(3_00000000)));
        actualPrice = nameService.getPrice(4);
        assertThat(actualPrice, is(BigInteger.valueOf(4_00000000)));
        actualPrice = nameService.getPrice(5);
        assertThat(actualPrice, is(BigInteger.valueOf(5_00000000)));
        actualPrice = nameService.getPrice(50);
        assertThat(actualPrice, is(BigInteger.valueOf(5_00000000)));
    }

    @Test
    public void testRegister() throws Throwable {
        String domain = "register.neo";
        boolean availableBefore = nameService.isAvailable(domain);
        assertTrue(availableBefore);

        Hash256 txHash = nameService.register(domain, IntegrationTestHelper.DEFAULT_ACCOUNT.getScriptHash())
                .signers(AccountSigner.calledByEntry(IntegrationTestHelper.DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        boolean availableAfter = nameService.isAvailable(domain);
        assertFalse(availableAfter);
    }

    @Test
    public void testRenew() throws Throwable {
        String domain = "renew.neo";
        registerDomainFromDefault(domain);
        long inOneYear = getNowInMilliSeconds() + ONE_YEAR_IN_MILLISECONDS;
        long lessThanInOneYear = getNowInMilliSeconds() + ONE_YEAR_IN_MILLISECONDS - BUFFER_MILLISECONDS;
        NameState nameStateBefore = nameService.getNameState(domain);
        assertThat(nameStateBefore.getExpiration(), lessThanOrEqualTo(inOneYear));
        assertThat(nameStateBefore.getExpiration(), greaterThan(lessThanInOneYear));

        Hash256 txHash = nameService.renew(domain)
                .signers(AccountSigner.calledByEntry(IntegrationTestHelper.DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        NameState nameStateAfter = nameService.getNameState(domain);
        long inTwoYears = getNowInMilliSeconds() + 2 * ONE_YEAR_IN_MILLISECONDS;
        long lessThanInTwoYears = getNowInMilliSeconds() + 2 * ONE_YEAR_IN_MILLISECONDS - BUFFER_MILLISECONDS;
        assertThat(nameStateAfter.getExpiration(), lessThanOrEqualTo(inTwoYears));
        assertThat(nameStateAfter.getExpiration(), greaterThan(lessThanInTwoYears));
    }

    @Test
    public void testSetAdmin() throws Throwable {
        String domain = "admin.neo";
        register(domain, IntegrationTestHelper.CLIENT_1);

        // setRecord should throw an exception, since client2 should not be able to create a record.
        TransactionConfigurationException thrown = assertThrows(TransactionConfigurationException.class,
                () -> setRecord(domain, RecordType.A, A_RECORD, IntegrationTestHelper.CLIENT_2));
        assertThat(thrown.getMessage(), containsString("The vm exited"));

        Hash256 txHash = nameService.setAdmin(domain, IntegrationTestHelper.CLIENT_2.getScriptHash())
                .signers(AccountSigner.calledByEntry(IntegrationTestHelper.CLIENT_1), AccountSigner.calledByEntry(
                        IntegrationTestHelper.CLIENT_2))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        NameState nameState = nameService.getNameState(domain);
        MatcherAssert.assertThat(nameState.getAdmin(), Matchers.is(IntegrationTestHelper.CLIENT_2.getScriptHash()));

        // Now as admin, client2 should be able to set a record.
        setRecord(domain, RecordType.A, A_RECORD, IntegrationTestHelper.CLIENT_2);
        String aRecord = nameService.getRecord(domain, RecordType.A);
        assertThat(aRecord, is(A_RECORD));
    }

    @Test
    public void testSetRecord_A() throws Throwable {
        setRecordFromDefault(DOMAIN, RecordType.A, A_RECORD);
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.A);
        assertThat(cnameRecord, is(A_RECORD));
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
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.TXT);
        assertThat(cnameRecord, is(TXT_RECORD));
    }

    @Test
    public void testSetRecord_AAAA() throws Throwable {
        setRecordFromDefault(DOMAIN, RecordType.AAAA, AAAA_RECORD);
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.AAAA);
        assertThat(cnameRecord, is(AAAA_RECORD));
    }

    @Test
    public void testDeleteRecord() throws Throwable {
        String domain = "delete.neo";
        registerDomainFromDefault(domain);
        setRecordFromDefault(domain, RecordType.TXT, "textrecordfordelete");
        String textRecordForDelete = nameService.getRecord(domain, RecordType.TXT);
        assertThat(textRecordForDelete, is("textrecordfordelete"));

        Hash256 txHash = nameService.deleteRecord(domain, RecordType.TXT)
                .signers(AccountSigner.calledByEntry(IntegrationTestHelper.DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
        InvocationFaultStateException thrown =
                assertThrows(InvocationFaultStateException.class, () -> nameService.getRecord(domain, RecordType.TXT));
        assertThat(thrown.getMessage(),
                containsString("Could not get any record of type TXT for the domain name 'delete.neo'."));
    }

    @Test
    public void testTransfer() throws Throwable {
        String domainForTransfer = "transfer.neo";
        registerDomainFromDefault(domainForTransfer);
        Hash160 ownerBefore = nameService.ownerOf(domainForTransfer);
        MatcherAssert.assertThat(ownerBefore, Matchers.is(IntegrationTestHelper.DEFAULT_ACCOUNT.getScriptHash()));

        Hash256 txHash =
                nameService.transfer(IntegrationTestHelper.DEFAULT_ACCOUNT, IntegrationTestHelper.CLIENT_1.getScriptHash(), domainForTransfer)
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        Hash160 ownerAfter = nameService.ownerOf(domainForTransfer);
        MatcherAssert.assertThat(ownerAfter, Matchers.is(IntegrationTestHelper.CLIENT_1.getScriptHash()));
    }

}
