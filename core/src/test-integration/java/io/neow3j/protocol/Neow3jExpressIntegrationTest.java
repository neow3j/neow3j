package io.neow3j.protocol;

import static io.neow3j.protocol.IntegrationTestHelper.GAS_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.test.NeoTestContainer.getNodeUrl;
import static io.neow3j.utils.Await.waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.protocol.core.response.ContractStorageEntry;
import io.neow3j.protocol.core.response.ExpressContractState;
import io.neow3j.protocol.core.response.Nep17Contract;
import io.neow3j.protocol.core.response.OracleRequest;
import io.neow3j.protocol.core.response.OracleResponse;
import io.neow3j.protocol.core.response.OracleResponseCode;
import io.neow3j.protocol.core.response.PopulatedBlocks;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.types.Hash160;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class Neow3jExpressIntegrationTest {
    protected static Neow3jExpress neow3jExpress;

    @ClassRule
    public static NeoTestContainer neoTestContainer = new NeoTestContainer(true);

    @BeforeClass
    public static void setUp() {
        neow3jExpress = Neow3jExpress.build(new HttpService(getNodeUrl(neoTestContainer)));
//        waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo("1", NEO_HASH, getNeow3jExpress());
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

        assertThat(populatedBlocks.getCacheId(), is("637628968740743949"));
        assertThat(populatedBlocks.getBlocks(), is(asList(0)));
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
        assertThat(nep17Contracts.get(0), is(neoContract));
        assertThat(nep17Contracts.get(1), is(gasContract));
    }

    @Test
    public void testExpressGetContractStorage() throws IOException {
        List<ContractStorageEntry> contractStorage = getNeow3jExpress()
                .expressGetContractStorage(GAS_HASH)
                .send()
                .getContractStorage();

        assertThat(contractStorage, hasSize(3));
        assertThat(contractStorage.get(0).getKey(), is("0b"));
        assertThat(contractStorage.get(0).getValue(), is("004c52b37da80a"));
        assertThat(contractStorage.get(2).getKey(),
                is("147f65d434362708b255f0e06856bdcb5ce99d8505"));
        assertThat(contractStorage.get(2).getValue(), is("41012107008053ee7ba80a"));
    }

    @Test
    public void testExpressListContracts() throws IOException {
        List<ExpressContractState> contracts = getNeow3jExpress()
                .expressListContracts()
                .send()
                .getContracts();

        assertThat(contracts, hasSize(9));
        assertThat(contracts.get(0).getManifest().getAbi().getMethods().get(2).getName(),
                is("setPrice"));
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

        assertThat(oracleRequests, hasSize(0));
        // TODO: 26.07.21 Michael: deploy contract with method that makes an oracle request,
        //  invoke that method and then call 'expressListOracleRequests'.
    }

    @Test
    public void testExpressCreateOracleResponseTx() throws IOException {
        String oracleResponseTx = getNeow3jExpress()
                .expressCreateOracleResponseTx(
                        new OracleResponse(1, OracleResponseCode.SUCCESS, "bmVvdzNq"))
                .send()
                .getOracleResponseTx();

        assertThat(oracleResponseTx,
                is("AAAAAAD+KXk7AAAAAAKgIQAAAAAA5BcAAAJYhxcRfgqoEHKvq3HS3Yn+fEuS/gDWpJ16ac8mblfxSXP0i4whCH8cRgABEQAAAAAAAAAAAAZuZW93M2olwh8MBmZpbmlzaAwUWIcXEX4KqBByr6tx0t2J/nxLkv5BYn1bUgIAAAAqEQwhAmB6OLgBCo9AHCXdAd8bdK8YJ90WuCH8B0UfLvfwLaYPEUGe0Nw6"));
    }

}
