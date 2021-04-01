package io.neow3j.contract;

import static io.neow3j.NeoTestContainer.getNodeUrl;
import static io.neow3j.contract.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.contract.IntegrationTestHelper.NODE_WALLET_PASSWORD;
import static io.neow3j.contract.IntegrationTestHelper.NODE_WALLET_PATH;
import static io.neow3j.contract.IntegrationTestHelper.client1;
import static io.neow3j.contract.IntegrationTestHelper.client2;
import static io.neow3j.contract.IntegrationTestHelper.committee;
import static io.neow3j.contract.IntegrationTestHelper.committeeWallet;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.contract.IntegrationTestHelper.walletClients12;
import static io.neow3j.transaction.Signer.calledByEntry;
import static io.neow3j.utils.Await.waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.neow3j.NeoTestContainer;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.methods.response.NameState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

public class NameServiceIntegrationTest {

    private static Neow3j neow3j;
    private static NeoNameService nameService;

    private static final String ROOT_DOMAIN = "neo";
    private static final String DOMAIN = "neow3j.neo";
    private static final String A_RECORD = "127.0.0.1";
    private static final String CNAME_RECORD = "cnamerecord.neow3j.neo";
    private static final String TXT_RECORD = "textrecord";
    private static final String AAAA_RECORD = "1:2:3:4:5:6:7:8";
    private static final long ONE_YEAR_IN_SECONDS = 365 * 24 * 3600;
    private static final long BUFFER_SECONDS = 3600;

    @ClassRule
    public static NeoTestContainer neoTestContainer =
            new NeoTestContainer("/node-config/config.json");

    @BeforeClass
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(getNodeUrl(neoTestContainer)));
        nameService = new NeoNameService(getNeow3j());
        // open the wallet for JSON-RPC calls
        getNeow3j().openWallet(NODE_WALLET_PATH, NODE_WALLET_PASSWORD).send();
        // ensure that the wallet with NEO/GAS is initialized for the tests
        waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo("1", NEO_HASH, getNeow3j());
        // make a transaction that can be used for the tests
        fundAccountsWithGas(neow3j, client1, client2);
        addRoot();
        registerDomainFromCommittee(DOMAIN);
        setRecordFromCommittee(DOMAIN, RecordType.A, A_RECORD);
    }

    private static Neow3j getNeow3j() {
        return neow3j;
    }

    private static void addRoot() throws Throwable {
        Hash256 txHash = nameService.addRoot(ROOT_DOMAIN)
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());
    }

    private static void registerDomainFromCommittee(String domain) throws Throwable {
        register(domain, committee, committeeWallet);
    }

    private static void register(String domain, Account owner, Wallet w) throws Throwable {
        Hash256 txHash = nameService.register(domain, owner.getScriptHash())
                .wallet(w)
                .signers(calledByEntry(owner.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());
    }

    private static void setRecordFromCommittee(String domain, RecordType type, String data)
            throws Throwable {
        setRecord(domain, type, data, committeeWallet, committee);
    }

    private static void setRecord(String domain, RecordType type, String data, Wallet w,
            Account signer) throws Throwable {
        Hash256 txHash = nameService.setRecord(domain, type, data)
                .wallet(w)
                .signers(calledByEntry(signer.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());
    }

    private static long getNowInSeconds() {
        return new Date().getTime() / 1000L;
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
        assertThat(owner, is(committee.getScriptHash()));
    }

    @Test
    public void testBalanceOf() throws IOException {
        BigInteger bigInteger = nameService.balanceOf(committee.getScriptHash());
        assertThat(bigInteger, greaterThanOrEqualTo(BigInteger.ONE));
    }

    @Test
    public void testProperties() throws IOException {
        NameState properties = nameService.properties(DOMAIN);
        assertThat(properties.getName(), is(DOMAIN));
        long inOneYear = getNowInSeconds() + ONE_YEAR_IN_SECONDS;
        long lessThanInOneYear = getNowInSeconds() + ONE_YEAR_IN_SECONDS - BUFFER_SECONDS;
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
        Hash256 txHash = nameService.addRoot("root")
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());

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
        Hash256 txHash = nameService.setPrice(newPrice)
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());

        BigInteger actualPrice = nameService.getPrice();
        assertThat(actualPrice, is(newPrice));
    }

    @Test
    public void testRegister() throws Throwable {
        String domain = "register.neo";
        boolean availableBefore = nameService.isAvailable(domain);
        assertTrue(availableBefore);

        Hash256 txHash = nameService.register(domain, committee.getScriptHash())
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());

        boolean availableAfter = nameService.isAvailable(domain);
        assertFalse(availableAfter);
    }

    @Test
    public void testRenew() throws Throwable {
        String domain = "renew.neo";
        registerDomainFromCommittee(domain);
        long inOneYear = getNowInSeconds() + ONE_YEAR_IN_SECONDS;
        long lessThanInOneYear = getNowInSeconds() + ONE_YEAR_IN_SECONDS - BUFFER_SECONDS;
        NameState propertiesBefore = nameService.properties(domain);
        assertThat(propertiesBefore.getExpiration(), lessThanOrEqualTo(inOneYear));
        assertThat(propertiesBefore.getExpiration(), greaterThan(lessThanInOneYear));

        Hash256 txHash = nameService.renew(domain)
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());

        NameState propertiesAfter = nameService.properties(domain);
        long inTwoYears = getNowInSeconds() + 2 * ONE_YEAR_IN_SECONDS;
        long lessThanInTwoYears = getNowInSeconds() + 2 * ONE_YEAR_IN_SECONDS - BUFFER_SECONDS;
        assertThat(propertiesAfter.getExpiration(), lessThanOrEqualTo(inTwoYears));
        assertThat(propertiesAfter.getExpiration(), greaterThan(lessThanInTwoYears));
    }

    @Test
    public void testSetAdmin() throws Throwable {
        String domain = "admin.neo";
        register(domain, client1, walletClients12);
        try {
            setRecord(domain, RecordType.A, A_RECORD, walletClients12, client2);
            fail();
        } catch (TransactionConfigurationException ignored) {
            // setRecord should throw an exception, since client2 should not be able to create a
            // record.
        }

        Hash256 txHash = nameService.setAdmin(domain, client2.getScriptHash())
                .wallet(walletClients12)
                .signers(calledByEntry(client1.getScriptHash()),
                        calledByEntry(client2.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());

        // Now as admin, client2 should be able to set a record.
        setRecord(domain, RecordType.A, A_RECORD, walletClients12, client2);
        String aRecord = nameService.getRecord(domain, RecordType.A);
        assertThat(aRecord, is(A_RECORD));
    }

    @Test
    public void testSetRecord_A() throws Throwable {
        setRecordFromCommittee(DOMAIN, RecordType.A, A_RECORD);
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.A);
        assertThat(cnameRecord, is(A_RECORD));
    }

    @Test
    public void testSetRecord_CNAME() throws Throwable {
        setRecordFromCommittee(DOMAIN, RecordType.CNAME, CNAME_RECORD);
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.CNAME);
        assertThat(cnameRecord, is(CNAME_RECORD));
    }

    @Test
    public void testSetRecord_TXT() throws Throwable {
        setRecordFromCommittee(DOMAIN, RecordType.TXT, TXT_RECORD);
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.TXT);
        assertThat(cnameRecord, is(TXT_RECORD));
    }

    @Test
    public void testSetRecord_AAAA() throws Throwable {
        setRecordFromCommittee(DOMAIN, RecordType.AAAA, AAAA_RECORD);
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.AAAA);
        assertThat(cnameRecord, is(AAAA_RECORD));
    }

    @Test
    public void testDeleteRecord() throws Throwable {
        String domain = "delete.neo";
        registerDomainFromCommittee(domain);
        setRecordFromCommittee(domain, RecordType.TXT, "textrecordfordelete");
        String textRecordForDelete = nameService.getRecord(domain, RecordType.TXT);
        assertThat(textRecordForDelete, is("textrecordfordelete"));

        Hash256 txHash = nameService.deleteRecord(domain, RecordType.TXT)
                .wallet(committeeWallet)
                .signers(calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());

        try {
            nameService.getRecord(domain, RecordType.TXT);
            fail();
        } catch (IllegalArgumentException ignored) {
            // if getRecord throws an exception here, the record is deleted successfully.
        }
    }

    @Test
    public void testTransfer() throws Throwable {
        String domainForTransfer = "transfer.neo";
        registerDomainFromCommittee(domainForTransfer);
        Hash160 ownerBefore = nameService.ownerOf(domainForTransfer);
        assertThat(ownerBefore, is(committee.getScriptHash()));

        Hash256 txHash =
                nameService.transfer(committeeWallet, client1.getScriptHash(), domainForTransfer)
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());

        Hash160 ownerAfter = nameService.ownerOf(domainForTransfer);
        assertThat(ownerAfter, is(client1.getScriptHash()));
    }

}
