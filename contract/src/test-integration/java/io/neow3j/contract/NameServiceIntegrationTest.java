package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.NameState;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.test.TestProperties;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.transaction.Witness;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_2;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NameServiceIntegrationTest {

    private static Neow3j neow3j;
    private static NeoNameService nameService;

    private static final String ROOT_DOMAIN = "neo";
    private static final String DOMAIN = "neow3j.neo";
    private static final String A_RECORD = "157.0.0.1";
    private static final String CNAME_RECORD = "cnamerecord.neow3j.neo";
    private static final String TXT_RECORD = "textrecord";
    private static final String AAAA_RECORD = "2001:2:3:4:5:6:7:8";
    private static final long ONE_YEAR_IN_MILLISECONDS = 365L * 24 * 3600 * 1000;
    private static final long BUFFER_MILLISECONDS = 3600 * 1000;

    @ClassRule
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeClass
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(getNeow3j());
        Hash160 nameServiceHash = deployNameServiceContract();
        nameService = new NeoNameService(nameServiceHash, getNeow3j());
        // make a transaction that can be used for the tests
        fundAccountsWithGas(getNeow3j(), DEFAULT_ACCOUNT, CLIENT_1, CLIENT_2);
        addRoot();
        registerDomainFromDefault(DOMAIN);
        setRecordFromDefault(DOMAIN, RecordType.A, A_RECORD);
    }

    private static Hash160 deployNameServiceContract() throws Throwable {
        byte[] manifestBytes = TestProperties.nameServiceManifest();
        byte[] nefBytes = TestProperties.nameServiceNef();

        Transaction tx = new ContractManagement(getNeow3j())
                .invokeFunction("deploy", byteArray(nefBytes), byteArray(manifestBytes))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
        return SmartContract.calcContractHash(COMMITTEE_ACCOUNT.getScriptHash(),
                NefFile.getCheckSumAsInteger(NefFile.computeChecksumFromBytes(nefBytes)),
                "NameService");
    }

    private static Neow3j getNeow3j() {
        return neow3j;
    }

    private static void addRoot() throws Throwable {
        Transaction tx = nameService.addRoot(ROOT_DOMAIN)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
    }

    private static void registerDomainFromDefault(String domain) throws Throwable {
        register(domain, DEFAULT_ACCOUNT);
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

    private static void setRecordFromDefault(String domain, RecordType type, String data)
            throws Throwable {

        setRecord(domain, type, data, DEFAULT_ACCOUNT);
    }

    private static void setRecord(String domain, RecordType type, String data, Account signer)
            throws Throwable {

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
        BigInteger price = nameService.getPrice();
        assertThat(price, is(new BigInteger("1000000000")));
    }

    @Test
    public void testIsAvailable() throws IOException {
        boolean isAvailable = nameService.isAvailable(DOMAIN);
        assertFalse(isAvailable);
    }

    @Test
    public void testOwnerOf() throws IOException {
        Hash160 owner = nameService.ownerOf(DOMAIN);
        assertThat(owner, is(DEFAULT_ACCOUNT.getScriptHash()));
    }

    @Test
    public void testBalanceOf() throws IOException {
        BigInteger bigInteger = nameService.balanceOf(DEFAULT_ACCOUNT.getScriptHash());
        assertThat(bigInteger, greaterThanOrEqualTo(BigInteger.ONE));
    }

    @Test
    public void testProperties() throws IOException {
        NameState properties = nameService.properties(DOMAIN);
        assertThat(properties.getName(), is(DOMAIN));
        long inOneYear = getNowInMilliSeconds() + ONE_YEAR_IN_MILLISECONDS;
        long lessThanInOneYear = getNowInMilliSeconds()
                + ONE_YEAR_IN_MILLISECONDS - BUFFER_MILLISECONDS;
        assertThat(properties.getExpiration(), lessThanOrEqualTo(inOneYear));
        assertThat(properties.getExpiration(), greaterThan(lessThanInOneYear));
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
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        boolean rootExists = false;
        try {
            // Every second-level domain name should still be available for the added root domain.
            rootExists = nameService.isAvailable("neow3j.root");
        } catch (IllegalArgumentException e) {
            fail();
        }
        assertTrue(rootExists);
    }

    @Test
    public void testSetPrice() throws Throwable {
        BigInteger newPrice = new BigInteger("12345");
        Transaction tx = nameService.setPrice(newPrice)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        BigInteger actualPrice = nameService.getPrice();
        assertThat(actualPrice, is(newPrice));
    }

    @Test
    public void testRegister() throws Throwable {
        String domain = "register.neo";
        boolean availableBefore = nameService.isAvailable(domain);
        assertTrue(availableBefore);

        Hash256 txHash = nameService.register(domain, DEFAULT_ACCOUNT.getScriptHash())
                .signers(calledByEntry(DEFAULT_ACCOUNT))
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
        long lessThanInOneYear = getNowInMilliSeconds() + ONE_YEAR_IN_MILLISECONDS -
                BUFFER_MILLISECONDS;
        NameState propertiesBefore = nameService.properties(domain);
        assertThat(propertiesBefore.getExpiration(), lessThanOrEqualTo(inOneYear));
        assertThat(propertiesBefore.getExpiration(), greaterThan(lessThanInOneYear));

        Hash256 txHash = nameService.renew(domain)
                .signers(calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        NameState propertiesAfter = nameService.properties(domain);
        long inTwoYears = getNowInMilliSeconds() + 2 * ONE_YEAR_IN_MILLISECONDS;
        long lessThanInTwoYears = getNowInMilliSeconds() + 2 * ONE_YEAR_IN_MILLISECONDS -
                BUFFER_MILLISECONDS;
        assertThat(propertiesAfter.getExpiration(), lessThanOrEqualTo(inTwoYears));
        assertThat(propertiesAfter.getExpiration(), greaterThan(lessThanInTwoYears));
    }

    @Test
    public void testSetAdmin() throws Throwable {
        String domain = "admin.neo";
        register(domain, CLIENT_1);
        try {
            setRecord(domain, RecordType.A, A_RECORD, CLIENT_2);
            fail();
        } catch (TransactionConfigurationException ignored) {
            // setRecord should throw an exception, since client2 should not be able to create a
            // record.
        }

        Hash256 txHash = nameService.setAdmin(domain, CLIENT_2.getScriptHash())
                .signers(calledByEntry(CLIENT_1), calledByEntry(CLIENT_2))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        // Now as admin, client2 should be able to set a record.
        setRecord(domain, RecordType.A, A_RECORD, CLIENT_2);
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
                .signers(calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        try {
            nameService.getRecord(domain, RecordType.TXT);
            fail();
        } catch (IllegalArgumentException exception) {
            // if getRecord throws an exception here, the record is deleted successfully.
            assertThat(exception.getMessage(),
                    containsString("No record of type " + RecordType.TXT.jsonValue()));
        }
    }

    @Test
    public void testTransfer() throws Throwable {
        String domainForTransfer = "transfer.neo";
        registerDomainFromDefault(domainForTransfer);
        Hash160 ownerBefore = nameService.ownerOf(domainForTransfer);
        assertThat(ownerBefore, is(DEFAULT_ACCOUNT.getScriptHash()));

        Hash256 txHash =
                nameService.transfer(DEFAULT_ACCOUNT, CLIENT_1.getScriptHash(), domainForTransfer)
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        Hash160 ownerAfter = nameService.ownerOf(domainForTransfer);
        assertThat(ownerAfter, is(CLIENT_1.getScriptHash()));
    }

}
