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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.testcontainers.containers.Container;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.protocol.IntegrationTestHelper.GAS_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Neow3jExpressIntegrationTest {

    static final String NEF_FILE_SOURCE = "/OracleTestContract.nef";
    static final String NEF_FILE_DESTINATION = "/app/OracleTestContract.nef";
    static final String MANIFEST_FILE_SOURCE = "/OracleTestContract.manifest.json";
    static final String MANIFEST_FILE_DESTINATION = "/app/OracleTestContract.manifest.json";
    static final String INVOKE_FILE_SOURCE = "/oracle-request-invoke.json";
    static final String INVOKE_FILE_DESTINATION = "/app/oracle-request-invoke.json";

    static final Hash160 oracleTestContractHash = new Hash160(
            "8693a8d7d9c5633f9b27cb4366f98b4171dc8838");

    static Hash256 oracleRequestTx;

    protected static Neow3jExpress neow3jExpress;

    @ClassRule
    public static NeoExpressTestContainer neoTestContainer = new NeoExpressTestContainer(1,
            NEF_FILE_SOURCE, NEF_FILE_DESTINATION,
            MANIFEST_FILE_SOURCE, MANIFEST_FILE_DESTINATION,
            INVOKE_FILE_SOURCE, INVOKE_FILE_DESTINATION
    );

    @BeforeClass
    public static void setUp() throws Exception {
        neow3jExpress = Neow3jExpress.build(new HttpService(neoTestContainer.getNodeUrl()));
        Await.waitUntilTransactionIsExecuted(deployContract(NEF_FILE_DESTINATION),
                getNeow3jExpress());

        oracleRequestTx = invokeContract(INVOKE_FILE_DESTINATION);
        Await.waitUntilTransactionIsExecuted(oracleRequestTx, getNeow3jExpress());
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
        Nep17Contract neoContract = new Nep17Contract(NEO_HASH, "NEO", 0);
        Nep17Contract gasContract = new Nep17Contract(GAS_HASH, "GAS", 8);
        assertThat(nep17Contracts.get(0), is(gasContract));
        assertThat(nep17Contracts.get(1), is(neoContract));
    }

    @Test
    public void testExpressGetContractStorage() throws IOException {
        List<ContractStorageEntry> contractStorage = getNeow3jExpress()
                .expressGetContractStorage(GAS_HASH)
                .send()
                .getContractStorage();

        assertThat(contractStorage, hasSize(4));
        assertThat(contractStorage.get(3).getKey(),
                is("147f65d434362708b255f0e06856bdcb5ce99d8505"));
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
        Hash256 originalTxHash = oracleRequestTx;
        BigInteger gasForResponse = BigInteger.valueOf(10_00000000);
        String url = "https://www.axlabs.com";
        String filter = "$.info";
        Hash160 callbackContract = oracleTestContractHash;
        String callbackMethod = "callback";
        String userData = "KAA=";

        assertThat(oracleRequests, hasSize(1));
        OracleRequest oracleRequest = oracleRequests.get(0);
        assertThat(oracleRequest.getRequestId(), is(requestId));
        assertThat(oracleRequest.getOriginalTransactionHash(), is(originalTxHash));
        assertThat(oracleRequest.getGasForResponse(), is(gasForResponse));
        assertThat(oracleRequest.getUrl(), is(url));
        assertThat(oracleRequest.getFilter(), is(filter));
        assertThat(oracleRequest.getCallbackContract(), is(callbackContract));
        assertThat(oracleRequest.getCallbackMethod(), is(callbackMethod));
        assertThat(oracleRequest.getUserData(), is(userData));

        OracleRequest expectedRequest = new OracleRequest(requestId, originalTxHash, gasForResponse,
                url, filter, callbackContract, callbackMethod, userData);
        assertThat(oracleRequest, is(expectedRequest));
    }

    @Test
    public void testExpressCreateOracleResponseTx() throws Exception {
        Await.waitUntilTransactionIsExecuted(enableOracle(), getNeow3jExpress());
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
        assertThat(expressShutdown.getExpressShutdown().getProcessId(), is(1));
    }

    private static Hash256 deployContract(String nefFilePath) throws Exception {
        Container.ExecResult execResult = neoTestContainer.execInContainer(
                "neoxp", "contract", "deploy", nefFilePath, "genesis");
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
        return new Hash256(execResult.getStdout().split(" ")[2]);
    }

    private static Hash256 invokeContract(String invokeFile) throws Exception {
        Container.ExecResult execResult = neoTestContainer.execInContainer(
                "neoxp", "contract", "invoke", invokeFile, "genesis");
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
        return new Hash256(execResult.getStdout().split(" ")[2]);
    }

    private static Hash256 enableOracle() throws Exception {
        Container.ExecResult execResult = neoTestContainer.execInContainer(
                "neoxp", "oracle", "enable", "genesis");
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
        return new Hash256(execResult.getStdout().split(" ")[3]);
    }

    private static void loadCheckpoint(String checkpointFile) throws Exception {
        Container.ExecResult res = neoTestContainer.execInContainer(
                "neoxp", "checkpoint", "restore", checkpointFile);
        if (res.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    res.getStderr());
        }
        neoTestContainer.execInContainer("neoxp", "run");
    }

    private static void applyBatchFile(String fileName, boolean resetToGenesisBlock)
            throws Exception {

        Container.ExecResult execResult = null;
        if (resetToGenesisBlock) {
            execResult = neoTestContainer.execInContainer("neoxp", "batch", fileName, "--reset");
        } else {
            execResult = neoTestContainer.execInContainer("neoxp", "batch", fileName);
        }
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
    }

}
