package io.neow3j.protocol;

import io.neow3j.crypto.Base64;
import io.neow3j.protocol.core.response.ContractStorageEntry;
import io.neow3j.protocol.core.response.ExpressContractState;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.MethodName.class)
@Testcontainers
public class Neow3jExpressIntegrationTest {

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
    public void testExpressGetContractStorage() throws IOException {
        List<ContractStorageEntry> contractStorage = getNeow3jExpress()
                .expressGetContractStorage(IntegrationTestHelper.GAS_HASH)
                .send()
                .getContractStorage();

        assertThat(contractStorage, hasSize(4));
        assertThat(contractStorage.get(3).getKey(),
                is("147F65D434362708B255F0E06856BDCB5CE99D8505"));
        assertThat(contractStorage.get(3).getValue(), is(not(isEmptyString())));
    }

    @Test
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
    public void testExpressCreateCheckpoint() throws IOException {
        String filename = getNeow3jExpress()
                .expressCreateCheckpoint("checkpoint-1.neoxp-checkpoint")
                .send()
                .getFilename();

        assertThat(filename, is("checkpoint-1.neoxp-checkpoint"));
    }

    @Test
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

        assertThat(oracleRequests, hasSize(1));
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
    public void testExpressCreateOracleResponseTx() throws Exception {
        Hash256 txHash = new Hash256(container.enableOracle());
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3jExpress());
        String responseResult = "bmVvdzNq";
        String oracleResponseTx = getNeow3jExpress()
                .expressCreateOracleResponseTx(
                        new OracleResponse(0, OracleResponseCode.SUCCESS, responseResult))
                .send()
                .getOracleResponseTx();

        Transaction tx = NeoSerializableInterface.from(Base64.decode(oracleResponseTx),
                Transaction.class);
        OracleResponseAttribute attr = (OracleResponseAttribute) tx.getAttributes().get(0);

        assertThat(attr.getId(), is(BigInteger.ZERO));
        assertThat(attr.getCode(), is(OracleResponseCode.SUCCESS));
        assertThat(attr.getResult(), is(Base64.decode(responseResult)));
    }

    @Test
    public void testShutdown() throws IOException {
        // This test must be executed last!
        // If more tests are added, make sure that the name of this test is the last in
        // lexicographic order (according to the test order by FixMethodOrder).
        NeoExpressShutdown expressShutdown = getNeow3jExpress().expressShutdown().send();
        assertThat(expressShutdown.getExpressShutdown().getProcessId(), is(greaterThan(0)));
    }

}
