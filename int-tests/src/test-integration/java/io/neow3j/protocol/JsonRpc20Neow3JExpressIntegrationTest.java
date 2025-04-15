package io.neow3j.protocol;

import io.neow3j.crypto.Base64;
import io.neow3j.protocol.core.response.ExpressContractState;
import io.neow3j.protocol.core.response.ExpressContractStorageEntry;
import io.neow3j.protocol.core.response.NeoExpressShutdown;
import io.neow3j.protocol.core.response.Nep17Contract;
import io.neow3j.protocol.core.response.OracleRequest;
import io.neow3j.protocol.core.response.OracleResponse;
import io.neow3j.protocol.core.response.OracleResponseCode;
import io.neow3j.protocol.core.response.PopulatedBlocks;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.test.NeoExpressTestContainer;
import io.neow3j.transaction.OracleResponseAttribute;
import io.neow3j.transaction.Transaction;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Await;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class JsonRpc20Neow3JExpressIntegrationTest {

    private static final String RESOURCE_DIR = "core/";
    private static final String NEF_FILE_SOURCE = RESOURCE_DIR + "OracleTestContract.nef";
    private static final String MANIFEST_FILE_SOURCE = RESOURCE_DIR + "OracleTestContract.manifest.json";
    private static final String BATCH_SOURCE = RESOURCE_DIR + "setup.batch";

    static final Hash160 oracleTestContractHash = new Hash160("8693a8d7d9c5633f9b27cb4366f98b4171dc8838");

    protected static Neow3jExpress neow3jExpress;

    @Container
    public static NeoExpressTestContainer container = new NeoExpressTestContainer()
            .withSecondsPerBlock(1)
            .withConfigFile(NeoExpressTestContainer.DEFAULT_NEOXP_CONFIG)
            .withBatchFile(BATCH_SOURCE)
            .withNefAndManifestFiles(NEF_FILE_SOURCE, MANIFEST_FILE_SOURCE);

    @BeforeAll
    public static void setUp() {
        neow3jExpress = Neow3jExpress.build(new HttpService(container.getNodeUrl()));
    }

    private static Neow3jExpress getNeow3jExpress() {
        return neow3jExpress;
    }

    @Test
    @Order(1)
    public void testExpressGetPopulatedBlocks() throws IOException {
        PopulatedBlocks populatedBlocks = getNeow3jExpress()
                .expressGetPopulatedBlocks()
                .send()
                .getPopulatedBlocks();

        assertNotNull(populatedBlocks.getCacheId());
        // genesis block, contract deployment and contract invocation
        assertThat(populatedBlocks.getBlocks(), hasSize(greaterThanOrEqualTo(3)));
    }

    @Test
    @Order(1)
    public void testExpressGetNep17Contracts() throws IOException {
        List<Nep17Contract> nep17Contracts = getNeow3jExpress()
                .expressGetNep17Contracts()
                .send()
                .getNep17Contracts();

        assertThat(nep17Contracts, hasSize(2));
        Nep17Contract neoContract = new Nep17Contract(IntegrationTestHelper.NEO_HASH, "NEO", 0);
        Nep17Contract gasContract = new Nep17Contract(IntegrationTestHelper.GAS_HASH, "GAS", 8);
        assertThat(nep17Contracts.get(0), is(gasContract));
        assertThat(nep17Contracts.get(1), is(neoContract));
    }

    @Test
    @Order(1)
    public void testExpressGetContractStorage() throws IOException {
        List<ExpressContractStorageEntry> contractStorage = getNeow3jExpress()
                .expressGetContractStorage(IntegrationTestHelper.GAS_HASH)
                .send()
                .getContractStorage();

        assertThat(contractStorage, hasSize(4));
        assertThat(contractStorage.get(3).getKeyHex(), is("0x147f65d434362708b255f0e06856bdcb5ce99d8505"));
        assertThat(contractStorage.get(3).getValueHex(), is(not(isEmptyString())));
    }

    @Test
    @Order(1)
    public void testExpressListContracts() throws IOException {
        List<ExpressContractState> contracts = getNeow3jExpress()
                .expressListContracts()
                .send()
                .getContracts();

        assertThat(contracts, hasSize(10));
        assertThat(contracts.get(0).getManifest().getAbi().getMethods().get(2).getName(),
                is("request"));
        assertThat(contracts.get(3).getManifest().getName(), is("GasToken"));
        assertThat(contracts.get(8).getHash(),
                is(new Hash160("0xfffdc93764dbaddd97c48f252a53ea4643faa3fd")));
    }

    @Test
    @Order(1)
    public void testExpressCreateCheckpoint() throws IOException {
        String filename = getNeow3jExpress()
                .expressCreateCheckpoint("checkpoint-1.neoxp-checkpoint")
                .send()
                .getFilename();

        assertThat(filename, is("checkpoint-1.neoxp-checkpoint"));
    }

    @Test
    @Order(1)
    public void testExpressListOracleRequests() throws IOException {
        List<OracleRequest> oracleRequests = getNeow3jExpress()
                .expressListOracleRequests()
                .send()
                .getOracleRequests();

        BigInteger requestId = BigInteger.ZERO;
        BigInteger gasForResponse = BigInteger.valueOf(10_00000000);
        String url = "https://www.axlabs.com";
        String filter = "$.info";
        Hash160 callbackContract = oracleTestContractHash;
        String callbackMethod = "callback";
        String userData = "KAA=";

        assertThat(oracleRequests, hasSize(2));
        OracleRequest oracleRequest = oracleRequests.get(0);
        assertThat(oracleRequest.getRequestId(), is(requestId));
        assertThat(oracleRequest.getOriginalTransactionHash(), is(notNullValue()));
        assertThat(oracleRequest.getGasForResponse(), is(gasForResponse));
        assertThat(oracleRequest.getUrl(), is(url));
        assertThat(oracleRequest.getFilter(), is(filter));
        assertThat(oracleRequest.getCallbackContract(), is(callbackContract));
        assertThat(oracleRequest.getCallbackMethod(), is(callbackMethod));
        assertThat(oracleRequest.getUserData(), is(userData));
    }

    @Test
    @Order(1)
    public void testExpressCreateOracleResponseTx() throws Exception {
        Hash256 txHash = new Hash256(container.enableOracle());
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3jExpress());
        byte[] responseResult = "neow3j".getBytes();
        String oracleResponseTx = getNeow3jExpress()
                .expressCreateOracleResponseTx(
                        new OracleResponse(BigInteger.ONE, OracleResponseCode.SUCCESS, Base64.encode(responseResult)))
                .send()
                .getOracleResponseTx();

        Transaction tx = NeoSerializableInterface.from(Base64.decode(oracleResponseTx), Transaction.class);
        OracleResponseAttribute attr = (OracleResponseAttribute) tx.getAttributes().get(0);

        OracleResponseAttribute expectedAttribute = new OracleResponseAttribute(
                BigInteger.ONE, OracleResponseCode.SUCCESS, responseResult);
        assertEquals(attr, expectedAttribute);
    }

    @Test
    @Order(1)
    public void testExecCommand_failingToExecute() {
        Exception thrown = assertThrows(Exception.class, () -> container.execCommand("neoxp", "invalid-command"));
        assertThat(thrown.getMessage(), containsString("Failed executing command in container. Error was: \n " +
                "Unrecognized command or argument 'invalid-command'"));
    }

    @Test
    @Order(10)
    public void testStopAndResume() throws Exception {
        BigInteger blockCountBeforeStopping = getNeow3jExpress().getBlockCount().send().getBlockCount();
        assertThat(blockCountBeforeStopping.intValue(), is(greaterThan(0)));

        String haltMessage = container.halt();
        assertThat(haltMessage, containsString("node 0 stopped"));

        IOException thrown = assertThrows(IOException.class, () -> getNeow3jExpress().getBlockCount().send());
        assertThat(thrown.getMessage(), containsString("unexpected end of stream"));

        String resumeMessage = container.resume();
        assertThat(resumeMessage, containsString("Neo-express started.\nNeo express is running"));

        BigInteger blockCountAfterResuming = getNeow3jExpress().getBlockCount().send().getBlockCount();
        assertThat(blockCountAfterResuming.intValue(), is(greaterThanOrEqualTo(blockCountBeforeStopping.intValue())));
    }

    @Test
    @Order(20)
    public void testShutdown() throws IOException {
        // This test must be executed last!
        // If more tests are added, make sure that the name of this test is the last in
        // lexicographic order (according to the test order by FixMethodOrder).
        NeoExpressShutdown expressShutdown = getNeow3jExpress().expressShutdown().send();
        assertThat(expressShutdown.getExpressShutdown().getProcessId(), is(greaterThan(0)));
    }

}
