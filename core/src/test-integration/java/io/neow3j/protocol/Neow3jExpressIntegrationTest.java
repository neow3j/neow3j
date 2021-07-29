package io.neow3j.protocol;

import static io.neow3j.protocol.IntegrationTestHelper.GAS_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.test.NeoTestContainer.getNodeUrl;
import static io.neow3j.test.NeoTestContainer.neoExpressTestContainer;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import io.neow3j.protocol.core.response.ContractStorageEntry;
import io.neow3j.protocol.core.response.ExpressContractState;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.response.Nep17Contract;
import io.neow3j.protocol.core.response.OracleRequest;
import io.neow3j.protocol.core.response.OracleResponse;
import io.neow3j.protocol.core.response.OracleResponseCode;
import io.neow3j.protocol.core.response.PopulatedBlocks;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Await;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class Neow3jExpressIntegrationTest {

    protected static Neow3jExpress neow3jExpress;

    @ClassRule
    public static NeoTestContainer neoTestContainer = neoExpressTestContainer(1);

    @BeforeClass
    public static void setUp() throws IOException {
        neow3jExpress = Neow3jExpress.build(new HttpService(getNodeUrl(neoTestContainer)));
        deployContractAndCreateOracleRequest();
    }

    private static Neow3jExpress getNeow3jExpress() {
        return neow3jExpress;
    }

    private static void deployContractAndCreateOracleRequest() throws IOException {
        // Deploy OracleTestContract
        // The raw transaction was created by deploying the contract 'OracleTestContract', signed
        // by the multi-sig committee account.
        NeoSendRawTransaction response = neow3jExpress
                .sendRawTransaction(
                        "00da452f632af9aa3b00000000247d1d000000000081160000017f65d434362708b255f0e06856bdcb5ce99d85050000fd13030d2e027b226e616d65223a224f7261636c6554657374436f6e7472616374222c2267726f757073223a5b5d2c226665617475726573223a7b7d2c22737570706f727465647374616e6461726473223a5b5d2c22616269223a7b226d6574686f6473223a5b7b226e616d65223a2272657175657374222c22706172616d6574657273223a5b7b226e616d65223a22676173466f72526573706f6e7365222c2274797065223a22496e7465676572227d5d2c226f6666736574223a302c2272657475726e74797065223a22566f6964222c2273616665223a66616c73657d2c7b226e616d65223a2263616c6c6261636b222c22706172616d6574657273223a5b7b226e616d65223a2275726c222c2274797065223a22537472696e67227d2c7b226e616d65223a227573657244617461222c2274797065223a22537472696e67227d2c7b226e616d65223a22636f6465222c2274797065223a22496e7465676572227d2c7b226e616d65223a22726573756c74222c2274797065223a22537472696e67227d5d2c226f6666736574223a35342c2272657475726e74797065223a224172726179222c2273616665223a66616c73657d5d2c226576656e7473223a5b5d7d2c227065726d697373696f6e73223a5b7b22636f6e7472616374223a22307866653932346237636665383964646432373161626166373231306138306137653131313738373538222c226d6574686f6473223a222a227d5d2c22747275737473223a5b5d2c226578747261223a7b7d7d0cba4e4546336e656f77336a2d332e31312e32000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001588717117e0aa81072afab71d2dd89fe7c4b92fe07726571756573740500000f00004c5700010c1668747470733a2f2f7777772e61786c6162732e636f6d0c06242e696e666f0c0863616c6c6261636b0c0078155537000040570104c2707a250e0000006878cf6879cf687bcf68400301e1ea12c01f0c066465706c6f790c14fda3fa4346ea532a258fc497ddaddb6437c9fdff41627d5b5201420c401e24db71a317b0b6dd75dfc01b4e990e169d444609292e0f0a78fc1c75012b04d358a1e6ea6e646a511d6407ed828aca981e2d46e67f4fab9f95145f7078656e2a110c21033a4d051b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7857787f1b11419ed0dc3a")
                .send();
        Hash256 txHash = response.getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3jExpress);

        // Invoke method with oracle request
        // The raw transaction was created by invoking the method 'request' on the contract
        // 'OracleTestContract' with one integer parameter set to 10_00000000, signed by the
        // multi-sig committee account.
        response = neow3jExpress
                .sendRawTransaction(
                        "00df2a922c52bab43e00000000fc1e12000000000082160000017f65d434362708b255f0e06856bdcb5ce99d850580002c0200ca9a3b11c01f0c07726571756573740c143888dc71418bf96643cb279b3f63c5d9d7a8938641627d5b5201420c4023e035fc0f5db314daf62f279edd82fdc873709029b5179696bd90919e25ed561a58d4847ff3174f648539d1b629d4df4fd4895bb4d7119297341b1eaed2ce382a110c21033a4d051b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7857787f1b11419ed0dc3a")
                .send();
        txHash = response.getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3jExpress);
    }

    @Test
    public void testExpressGetPopulatedBlocks() throws IOException {
        PopulatedBlocks populatedBlocks = getNeow3jExpress()
                .expressGetPopulatedBlocks()
                .send()
                .getPopulatedBlocks();

        assertNotNull(populatedBlocks.getCacheId());
        // genesis block, contract deployment and contract invocation
        assertThat(populatedBlocks.getBlocks(), hasSize(3));
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
        assertThat(contractStorage.get(3).getValue(), is("4101210764b045de5e7912"));
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

        assertThat(oracleRequests, hasSize(1));
        OracleRequest request = new OracleRequest(BigInteger.ZERO,
                new Hash256("93f60604c993ee5ea1cd0e58c1960dfa07487c505fb73f35f41d9b8f9ff448d9"),
                BigInteger.valueOf(10_00000000),
                "https://www.axlabs.com",
                "$.info",
                new Hash160("8693a8d7d9c5633f9b27cb4366f98b4171dc8838"),
                "callback",
                "KAA=");
        assertThat(oracleRequests.get(0), is(request));
        // TODO: 26.07.21 Michael: deploy contract with method that makes an oracle request,
        //  invoke that method and then call 'expressListOracleRequests'.
    }

    @Test
    public void testExpressCreateOracleResponseTx() throws IOException {
        String oracleResponseTx = getNeow3jExpress()
                .expressCreateOracleResponseTx(
                        new OracleResponse(0, OracleResponseCode.SUCCESS, "bmVvdzNq"))
                .send()
                .getOracleResponseTx();

        assertThat(oracleResponseTx,
                is("AAAAAAD+KXk7AAAAAAKgIQAAAAAA5BcAAAJYhxcRfgqoEHKvq3HS3Yn+fEuS" +
                        "/gDWpJ16ac8mblfxSXP0i4whCH8cRgABEQAAAAAAAAAAAAZuZW93M2olwh8MBmZpbmlzaAwUWIcXEX4KqBByr6tx0t2J/nxLkv5BYn1bUgIAAAAqEQwhAmB6OLgBCo9AHCXdAd8bdK8YJ90WuCH8B0UfLvfwLaYPEUGe0Nw6"));
    }

}
