package io.neow3j.protocol.core;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.model.types.NodePluginType;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.ByteStringStackItem;
import io.neow3j.protocol.core.methods.response.ConsensusData;
import io.neow3j.protocol.core.methods.response.NeoAddress;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoCloseWallet;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetBalance;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Balances;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Transfers;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetStorage;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoGetUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoGetValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoImportPrivKey;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoListPlugins;
import io.neow3j.protocol.core.methods.response.NeoOpenWallet;
import io.neow3j.protocol.core.methods.response.NeoSendFrom;
import io.neow3j.protocol.core.methods.response.NeoSendMany;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.NeoSubmitBlock;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.NeoWitness;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.neow3j.protocol.core.methods.response.TransactionSigner;
import io.neow3j.protocol.ResponseTester;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Numeric;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.nullValue;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Core Protocol Response tests.
 */
public class ResponseTest extends ResponseTester {

    public static final ScriptHash NEO_HASH = ScriptHash.fromScript(
            new ScriptBuilder().pushData("NEO").sysCall(InteropServiceCode.NEO_NATIVE_CALL).toArray());
    private static final String NEO_HASH_STRING = NEO_HASH.toString();

    @Test
    public void testErrorResponse() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"error\": {\n" +
                        "        \"code\": -32602,\n" +
                        "        \"message\": \"Invalid address length, expected 40 got 64 bytes\",\n" +
                        "        \"data\": null\n" +
                        "    }\n" +
                        "}"
        );

        NeoBlockCount ethBlock = deserialiseResponse(NeoBlockCount.class);
        assertTrue(ethBlock.hasError());
        assertThat(ethBlock.getError(), equalTo(
                new Response.Error(-32602, "Invalid address length, expected 40 got 64 bytes")));
    }

    @Test
    public void testErrorResponseComplexData() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"error\": {\n" +
                        "        \"code\":-32602,\n" +
                        "        \"message\":\"Invalid address length, expected 40 got 64 bytes\",\n" +
                        "        \"data\": {\n" +
                        "            \"foo\":\"bar\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );

        NeoBlockCount ethBlock = deserialiseResponse(NeoBlockCount.class);
        assertTrue(ethBlock.hasError());
        assertThat(ethBlock.getError().getData(), equalTo("{\"foo\":\"bar\"}"));
    }

    // Blockchain Methods

    @Test
    public void testGetBestBlockHash() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": \"0x3d1e051247f246f60dd2ba4f90f799578b5d394157b1f2b012c016b29536b899\"\n" +
                        "}"
        );

        NeoBlockHash blockHash = deserialiseResponse(NeoBlockHash.class);
        assertThat(blockHash.getBlockHash(),
                is("0x3d1e051247f246f60dd2ba4f90f799578b5d394157b1f2b012c016b29536b899"));
    }

    @Test
    public void testGetBlockHash() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": \"0x147ad6a26f1d5a9bb2bea3f0b2ca9fab3824873beaf8887e87d08c8fd98a81b3\"\n" +
                        "}"
        );

        NeoBlockHash neoBestBlockHash = deserialiseResponse(NeoBlockHash.class);
        assertThat(neoBestBlockHash.getBlockHash(),
                is("0x147ad6a26f1d5a9bb2bea3f0b2ca9fab3824873beaf8887e87d08c8fd98a81b3"));
    }

    @Test
    public void testGetBlock() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"hash\": \"0x1de7e5eaab0f74ac38f5191c038e009d3c93ef5c392d1d66fa95ab164ba308b8\",\n" +
                        "        \"size\": 1217,\n" +
                        "        \"version\": 0,\n" +
                        "        \"previousblockhash\": \"0x045cabde4ecbd50f5e4e1b141eaf0842c1f5f56517324c8dcab8ccac924e3a39\",\n" +
                        "        \"merkleroot\": \"0x6afa63201b88b55ad2213e5a69a1ad5f0db650bc178fc2bedd2fb301c1278bf7\",\n" +
                        "        \"time\": 1539968858,\n" +
                        "        \"index\": 1914006,\n" +
                        "        \"nextconsensus\": \"AWZo4qAxhT8fwKL93QATSjCYCgHmCY1XLB\",\n" +
                        "        \"witnesses\": [\n" +
                        "            {\n" +
                        "                \"invocation\": \"DEBJVWapboNkCDlH9uu+tStOgGnwODlolRifxTvQiBkhM0vplSPo4vMj9Jt3jvzztMlwmO75Ss5cptL8wUMxASjZ\",\n" +
                        "                \"verification\": \"EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw==\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"consensus_data\": {\n" +
                        "            \"primary\": 0,\n" +
                        "            \"nonce\": \"45fba5f11cb04667\"\n" +
                        "        },\n" +
                        "        \"tx\": [\n" +
                        "            {\n" +
                        "                \"hash\": \"0x46eca609a9a8c8340ee56b174b04bc9c9f37c89771c3a8998dc043f5a74ad510\",\n" +
                        "                \"size\": 267,\n" +
                        "                \"version\": 0,\n" +
                        "                \"nonce\": 565086327,\n" +
                        "                \"sender\": \"AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4\",\n" +
                        "                \"sysfee\": \"0\",\n" +
                        "                \"netfee\": \"0\",\n" +
                        "                \"validuntilblock\": 2107425,\n" +
                        "                \"attributes\": [\n" +
                        "                    {\n" +
                        "                        \"type\": \"Signer\",\n" +
                        "                        \"account\": \"0xf68f181731a47036a99f04dad90043a744edec0f\",\n" +
                        "                        \"scopes\": \"CalledByEntry\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"script\":\n" +
                        "                    \"AGQMFObBATZUrxE9ipaL3KUsmUioK5U9DBQP7O1Ep0MA2doEn6k2cKQxFxiP9hPADAh0cmFuc2ZlcgwUiXcg2M129PAKv6N8Dt2InCCP3ptBYn1bUjg\",\n" +
                        "                \"witnesses\": [\n" +
                        "                    {\n" +
                        "                        \"invocation\": \"DEBR7EQOb1NUjat1wrINzBNKOQtXoUmRVZU8h5c8K5CLMCUVcGkFVqAAGUJDh3mVcz6sTgXvmMuujWYrBveeM4q+\",\n" +
                        "                        \"verification\": \"EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw==\"\n" +
                        "                    }\n" +
                        "                ]\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"hash\": \"0x46eca609a9a8c8340ee56b174b04bc9c9f37c89771c3a8998dc043f5a74ad510\",\n" +
                        "                \"size\": 267,\n" +
                        "                \"version\": 0,\n" +
                        "                \"nonce\": 565086327,\n" +
                        "                \"sender\": \"AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4\",\n" +
                        "                \"sysfee\": \"0\",\n" +
                        "                \"netfee\": \"0\",\n" +
                        "                \"validuntilblock\": 2107425,\n" +
                        "                \"attributes\": [\n" +
                        "                    {\n" +
                        "                        \"type\": \"Signer\",\n" +
                        "                        \"account\": \"0xf68f181731a47036a99f04dad90043a744edec0f\",\n" +
                        "                        \"scopes\": \"CalledByEntry\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"script\": \"AGQMFObBATZUrxE9ipaL3KUsmUioK5U9DBQP7O1Ep0MA2doEn6k2cKQxFxiP9hPADAh0cmFuc2ZlcgwUiXcg2M129PAKv6N8Dt2InCCP3ptBYn1bUjg\",\n" +
                        "                \"witnesses\": [\n" +
                        "                    {\n" +
                        "                        \"invocation\": \"DEBR7EQOb1NUjat1wrINzBNKOQtXoUmRVZU8h5c8K5CLMCUVcGkFVqAAGUJDh3mVcz6sTgXvmMuujWYrBveeM4q+\",\n" +
                        "                        \"verification\": \"EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw==\"\n" +
                        "                    }\n" +
                        "                ]\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"confirmations\": 7878,\n" +
                        "        \"nextblockhash\": \"0x4a97ca89199627f877b6bffe865b8327be84b368d62572ef20953829c3501643\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetBlock getBlock = deserialiseResponse(NeoGetBlock.class);
        assertThat(getBlock.getBlock(), is(notNullValue()));

        assertThat(getBlock.getBlock().getHash(),
                is("0x1de7e5eaab0f74ac38f5191c038e009d3c93ef5c392d1d66fa95ab164ba308b8"));
        assertThat(getBlock.getBlock().getSize(), is(1217L));
        assertThat(getBlock.getBlock().getVersion(), is(0));
        assertThat(getBlock.getBlock().getPrevBlockHash(),
                is("0x045cabde4ecbd50f5e4e1b141eaf0842c1f5f56517324c8dcab8ccac924e3a39"));
        assertThat(getBlock.getBlock().getMerkleRootHash(),
                is("0x6afa63201b88b55ad2213e5a69a1ad5f0db650bc178fc2bedd2fb301c1278bf7"));
        assertThat(getBlock.getBlock().getTime(), is(1539968858L));
        assertThat(getBlock.getBlock().getIndex(), is(1914006L));
        assertThat(getBlock.getBlock().getNextConsensus(),
                is("AWZo4qAxhT8fwKL93QATSjCYCgHmCY1XLB"));

        assertThat(getBlock.getBlock().getWitnesses(), is(notNullValue()));
        assertThat(getBlock.getBlock().getWitnesses(), hasSize(1));

        assertThat(
                getBlock.getBlock().getWitnesses(),
                containsInAnyOrder(
                        new NeoWitness(
                                "DEBJVWapboNkCDlH9uu+tStOgGnwODlolRifxTvQiBkhM0vplSPo4vMj9Jt3jvzztMlwmO75Ss5cptL8wUMxASjZ",
                                "EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw=="
                        )
                )
        );

        assertThat(getBlock.getBlock().getConsensusData(), is(notNullValue()));
        assertThat(
                getBlock.getBlock().getConsensusData(),
                is(new ConsensusData(0,"45fba5f11cb04667"))
        );

        assertThat(getBlock.getBlock().getTransactions(), hasSize(2));

        assertThat(
                getBlock.getBlock().getTransactions(),
                containsInAnyOrder(
                        new Transaction(
                                "0x46eca609a9a8c8340ee56b174b04bc9c9f37c89771c3a8998dc043f5a74ad510",
                                267L,
                                0,
                                565086327L,
                                "AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4",
                                "0",
                                "0",
                                2107425L,
                                Arrays.asList(
                                        new TransactionSigner(
                                                "0xf68f181731a47036a99f04dad90043a744edec0f",
                                                WitnessScope.CALLED_BY_ENTRY
                                        )
                                ),
                                "AGQMFObBATZUrxE9ipaL3KUsmUioK5U9DBQP7O1Ep0MA2doEn6k2cKQxFxiP9hPADAh0cmFuc2ZlcgwUiXcg2M129PAKv6N8Dt2InCCP3ptBYn1bUjg",
                                Arrays.asList(
                                        new NeoWitness(
                                                "DEBR7EQOb1NUjat1wrINzBNKOQtXoUmRVZU8h5c8K5CLMCUVcGkFVqAAGUJDh3mVcz6sTgXvmMuujWYrBveeM4q+",
                                                "EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw=="
                                        )
                                )
                        ),
                        new Transaction(
                                "0x46eca609a9a8c8340ee56b174b04bc9c9f37c89771c3a8998dc043f5a74ad510",
                                267L,
                                0,
                                565086327L,
                                "AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4",
                                "0",
                                "0",
                                2107425L,
                                Arrays.asList(
                                        new TransactionSigner(
                                                "0xf68f181731a47036a99f04dad90043a744edec0f",
                                                WitnessScope.CALLED_BY_ENTRY
                                        )
                                ),
                                "AGQMFObBATZUrxE9ipaL3KUsmUioK5U9DBQP7O1Ep0MA2doEn6k2cKQxFxiP9hPADAh0cmFuc2ZlcgwUiXcg2M129PAKv6N8Dt2InCCP3ptBYn1bUjg",
                                Arrays.asList(
                                        new NeoWitness(
                                                "DEBR7EQOb1NUjat1wrINzBNKOQtXoUmRVZU8h5c8K5CLMCUVcGkFVqAAGUJDh3mVcz6sTgXvmMuujWYrBveeM4q+",
                                                "EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw=="
                                        )
                                )
                        )
                )
        );

        assertThat(getBlock.getBlock().getConfirmations(), is(7878));
        assertThat(getBlock.getBlock().getNextBlockHash(),
                is("0x4a97ca89199627f877b6bffe865b8327be84b368d62572ef20953829c3501643"));
    }

    @Test
    public void testGetBlock_BlockHeader() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"hash\": \"0x1de7e5eaab0f74ac38f5191c038e009d3c93ef5c392d1d66fa95ab164ba308b8\",\n" +
                        "        \"size\": 1217,\n" +
                        "        \"version\": 0,\n" +
                        "        \"previousblockhash\": \"0x045cabde4ecbd50f5e4e1b141eaf0842c1f5f56517324c8dcab8ccac924e3a39\",\n" +
                        "        \"merkleroot\": \"0x6afa63201b88b55ad2213e5a69a1ad5f0db650bc178fc2bedd2fb301c1278bf7\",\n" +
                        "        \"time\": 1539968858,\n" +
                        "        \"index\": 1914006,\n" +
                        "        \"nextconsensus\": \"AWZo4qAxhT8fwKL93QATSjCYCgHmCY1XLB\",\n" +
                        "        \"witnesses\": [\n" +
                        "            {\n" +
                        "                \"invocation\": \"DEBJVWapboNkCDlH9uu+tStOgGnwODlolRifxTvQiBkhM0vplSPo4vMj9Jt3jvzztMlwmO75Ss5cptL8wUMxASjZ\",\n" +
                        "                \"verification\": \"EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw==\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"confirmations\": 7878,\n" +
                        "        \"nextblockhash\": \"0x4a97ca89199627f877b6bffe865b8327be84b368d62572ef20953829c3501643\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetBlock getBlock = deserialiseResponse(NeoGetBlock.class);
        assertThat(getBlock.getBlock(), is(notNullValue()));

        assertThat(getBlock.getBlock().getHash(),
                is("0x1de7e5eaab0f74ac38f5191c038e009d3c93ef5c392d1d66fa95ab164ba308b8"));
        assertThat(getBlock.getBlock().getSize(), is(1217L));
        assertThat(getBlock.getBlock().getVersion(), is(0));
        assertThat(getBlock.getBlock().getPrevBlockHash(),
                is("0x045cabde4ecbd50f5e4e1b141eaf0842c1f5f56517324c8dcab8ccac924e3a39"));
        assertThat(getBlock.getBlock().getMerkleRootHash(),
                is("0x6afa63201b88b55ad2213e5a69a1ad5f0db650bc178fc2bedd2fb301c1278bf7"));
        assertThat(getBlock.getBlock().getTime(), is(1539968858L));
        assertThat(getBlock.getBlock().getIndex(), is(1914006L));
        assertThat(getBlock.getBlock().getNextConsensus(),
                is("AWZo4qAxhT8fwKL93QATSjCYCgHmCY1XLB"));

        assertThat(getBlock.getBlock().getWitnesses(), is(notNullValue()));
        assertThat(getBlock.getBlock().getWitnesses(), hasSize(1));

        assertThat(
                getBlock.getBlock().getWitnesses(),
                containsInAnyOrder(
                        new NeoWitness(
                                "DEBJVWapboNkCDlH9uu+tStOgGnwODlolRifxTvQiBkhM0vplSPo4vMj9Jt3jvzztMlwmO75Ss5cptL8wUMxASjZ",
                                "EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw=="
                        )
                )
        );

        assertThat(getBlock.getBlock().getConsensusData(), is(nullValue()));

        assertThat(getBlock.getBlock().getTransactions(), is(nullValue()));

        assertThat(getBlock.getBlock().getConfirmations(), is(7878));
        assertThat(getBlock.getBlock().getNextBlockHash(),
                is("0x4a97ca89199627f877b6bffe865b8327be84b368d62572ef20953829c3501643"));
    }


    @Test
    public void testGetRawBlock() {
        buildResponse(
                "{\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"id\": 67,\n" +
                        "  \"result\": \"00000000ebaa4ed893333db1ed556bb24145f4e7fe40b9c7c07ff2235c7d3d361ddb27e603da9da4c7420d090d0e29c588cfd701b3f81819375e537c634bd779ddc7e2e2c436cc5ba53f00001952d428256ad0cdbe48d3a3f5d10013ab9ffee489706078714f1ea201c340c44387d762d1bcb2ab0ec650628c7c674021f333ee7666e2a03805ad86df3b826b5dbf5ac607a361807a047d43cf6bba726dcb06a42662aee7e78886c72faef940e6cef9abab82e1e90c6683ac8241b3bf51a10c908f01465f19c3df1099ef5de5d43a648a6e4ab63cc7d5e88146bddbe950e8041e44a2b0b81f21ad706e88258540fd19314f46ad452b4cbedf58bf9d266c0c808374cd33ef18d9a0575b01e47f6bb04abe76036619787c457c49288aeb91ff23cdb85771c0209db184801d5bdd348b532102103a7f7dd016558597f7960d27c516a4394fd968b9e65155eb4b013e4040406e2102a7bc55fe8684e0119768d104ba30795bdcc86619e864add26156723ed185cd622102b3622bf4017bdfe317c58aed5f4c753f206b7db896046fa7d774bbc4bf7f8dc22103d90c07df63e690ce77912e10ab51acc944b66860237b608c4f8f8309e71ee69954ae0100001952d42800000000\"\n" +
                        "}"
        );

        NeoGetRawBlock getRawBlock = deserialiseResponse(NeoGetRawBlock.class);
        assertThat(getRawBlock.getRawBlock(), is(notNullValue()));

        assertThat(getRawBlock.getRawBlock(),
                is("00000000ebaa4ed893333db1ed556bb24145f4e7fe40b9c7c07ff2235c7d3d361ddb27e603da9da4c7420d090d0e29c588cfd701b3f81819375e537c634bd779ddc7e2e2c436cc5ba53f00001952d428256ad0cdbe48d3a3f5d10013ab9ffee489706078714f1ea201c340c44387d762d1bcb2ab0ec650628c7c674021f333ee7666e2a03805ad86df3b826b5dbf5ac607a361807a047d43cf6bba726dcb06a42662aee7e78886c72faef940e6cef9abab82e1e90c6683ac8241b3bf51a10c908f01465f19c3df1099ef5de5d43a648a6e4ab63cc7d5e88146bddbe950e8041e44a2b0b81f21ad706e88258540fd19314f46ad452b4cbedf58bf9d266c0c808374cd33ef18d9a0575b01e47f6bb04abe76036619787c457c49288aeb91ff23cdb85771c0209db184801d5bdd348b532102103a7f7dd016558597f7960d27c516a4394fd968b9e65155eb4b013e4040406e2102a7bc55fe8684e0119768d104ba30795bdcc86619e864add26156723ed185cd622102b3622bf4017bdfe317c58aed5f4c753f206b7db896046fa7d774bbc4bf7f8dc22103d90c07df63e690ce77912e10ab51acc944b66860237b608c4f8f8309e71ee69954ae0100001952d42800000000"));
    }

    @Test
    public void testGetBlockCount() {
        buildResponse(
                "{\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"id\": 67,\n" +
                        "  \"result\": 1234\n" +
                        "}"
        );

        NeoBlockCount neoBlockCount = deserialiseResponse(NeoBlockCount.class);
        assertThat(neoBlockCount.getBlockIndex(), is(notNullValue()));

        assertThat(neoBlockCount.getBlockIndex(), is(BigInteger.valueOf(1234)));
    }

    @Test
    public void testGetContractState() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"id\": -2,\n" +
                        "        \"hash\": \"0x668e0c1f9d7b70a99dd9e06eadd4c784d641afbc\",\n" +
                        "        \"script\": \"QetD9PQ=\",\n" +
                        "        \"manifest\": {\n" +
                        "            \"groups\": [\n" +
                        "                {\n" +
                        "                    \"pubKey\": \"03b209fd4f53a7170ea4444e0cb0a6bb6a53c2bd016926989cf85f9b0fba17a70c\",\n" +
                        "                    \"signature\": \"41414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"features\": {\n" +
                        "                \"storage\": true,\n" +
                        "                \"payable\": false\n" +
                        "            },\n" +
                        "            \"abi\": {\n" +
                        "                \"hash\": \"0x668e0c1f9d7b70a99dd9e06eadd4c784d641afbc\",\n" +
                        "                \"methods\": [\n" +
                        "                    {\n" +
                        "                        \"name\": \"name\",\n" +
                        "                        \"parameters\": [],\n" +
                        "                        \"offset\": 0,\n" +
                        "                        \"returnType\": \"String\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"name\": \"symbol\",\n" +
                        "                        \"parameters\": [],\n" +
                        "                        \"offset\": 0,\n" +
                        "                        \"returnType\": \"String\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"name\": \"decimals\",\n" +
                        "                        \"parameters\": [],\n" +
                        "                        \"offset\": 0,\n" +
                        "                        \"returnType\": \"Integer\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"name\": \"totalSupply\",\n" +
                        "                        \"parameters\": [],\n" +
                        "                        \"offset\": 0,\n" +
                        "                        \"returnType\": \"Integer\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"name\": \"balanceOf\",\n" +
                        "                        \"parameters\": [\n" +
                        "                            {\n" +
                        "                                \"name\": \"account\",\n" +
                        "                                \"type\": \"Hash160\"\n" +
                        "                            }\n" +
                        "                        ],\n" +
                        "                        \"offset\": 0,\n" +
                        "                        \"returnType\": \"Integer\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"events\": [\n" +
                        "                    {\n" +
                        "                        \"name\": \"Transfer\",\n" +
                        "                        \"parameters\": [\n" +
                        "                            {\n" +
                        "                                \"name\": \"from\",\n" +
                        "                                \"type\": \"Hash160\"\n" +
                        "                            },\n" +
                        "                            {\n" +
                        "                                \"name\": \"to\",\n" +
                        "                                \"type\": \"Hash160\"\n" +
                        "                            },\n" +
                        "                            {\n" +
                        "                                \"name\": \"amount\",\n" +
                        "                                \"type\": \"Integer\"\n" +
                        "                            }\n" +
                        "                        ],\n" +
                        "                        \"returnType\": \"Signature\"\n" +
                        "                    }\n" +
                        "                ]\n" +
                        "            },\n" +
                        "            \"permissions\": [\n" +
                        "                {\n" +
                        "                    \"contract\": \"0xde5f57d430d3dece511cf975a8d37848cb9e0525\",\n" +
                        "                    \"methods\": [\n" +
                        "                        \"name\",\n" +
                        "                        \"transfer\"\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"trusts\": [" +
                        "                \"0xde5f57d430d3dece511cf975a8d37848cb9e0525\"\n" +
                        "            ],\n" +
                        "            \"safeMethods\": [\n" +
                        "                \"name\",\n" +
                        "                \"symbol\",\n" +
                        "                \"decimals\",\n" +
                        "                \"totalSupply\",\n" +
                        "                \"balanceOf\",\n" +
                        "                \"supportedStandards\"\n" +
                        "            ],\n" +
                        "            \"extra\": null\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetContractState getContractState = deserialiseResponse(NeoGetContractState.class);
        assertThat(getContractState.getContractState(), is(notNullValue()));
        assertThat(getContractState.getContractState().getId(), is(-2));
        assertThat(getContractState.getContractState().getHash(),
                is("0x668e0c1f9d7b70a99dd9e06eadd4c784d641afbc"));
        assertThat(getContractState.getContractState().getScript(), is("QetD9PQ="));

        NeoGetContractState.ContractState.ContractManifest manifest = getContractState.getContractState().getManifest();
        assertThat(manifest, is(notNullValue()));
        assertThat(manifest.getGroups(), is(notNullValue()));
        assertThat(manifest.getGroups(), hasSize(1));
        assertThat(manifest.getGroups().get(0).getPubKey(),
                is("03b209fd4f53a7170ea4444e0cb0a6bb6a53c2bd016926989cf85f9b0fba17a70c"));
        assertThat(manifest.getGroups().get(0).getSignature(),
                is("41414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141"));

        assertThat(manifest.getFeatures(), is(notNullValue()));
        assertThat(manifest.getFeatures().getStorage(), is(true));
        assertThat(manifest.getFeatures().getPayable(), is(false));

        NeoGetContractState.ContractState.ContractManifest.ContractABI abi = manifest.getAbi();
        assertThat(abi, is(notNullValue()));
        assertThat(abi.getHash(), is("0x668e0c1f9d7b70a99dd9e06eadd4c784d641afbc"));

        assertThat(abi.getMethods(), is(notNullValue()));
        assertThat(abi.getMethods(), hasSize(5));
        assertThat(abi.getMethods().get(1).getName(), is("symbol"));
        assertThat(abi.getMethods().get(1).getParameters(), is(notNullValue()));
        assertThat(abi.getMethods().get(1).getParameters(), hasSize(0));
        assertThat(abi.getMethods().get(4).getName(), is("balanceOf"));
        assertThat(abi.getMethods().get(4).getParameters(), is(notNullValue()));
        assertThat(abi.getMethods().get(4).getParameters(), hasSize(1));
        assertThat(abi.getMethods().get(4).getReturnType(), is(ContractParameterType.INTEGER));

        assertThat(abi.getEvents(), is(notNullValue()));
        assertThat(abi.getEvents(), hasSize(1));
        assertThat(abi.getEvents().get(0).getName(), is("Transfer"));
        assertThat(abi.getEvents().get(0).getParameters(), is(notNullValue()));
        assertThat(abi.getEvents().get(0).getParameters(), hasSize(3));
        assertThat(abi.getEvents().get(0).getParameters().get(2).getParamName(), is("amount"));
        assertThat(abi.getEvents().get(0).getParameters().get(2).getParamType(), is(ContractParameterType.INTEGER));

        assertThat(manifest.getPermissions(), is(notNullValue()));
        assertThat(manifest.getPermissions(), hasSize(1));
        assertThat(manifest.getPermissions().get(0).getContract(),
                is("0xde5f57d430d3dece511cf975a8d37848cb9e0525"));
        assertThat(manifest.getPermissions().get(0).getMethods(), is(notNullValue()));
        assertThat(manifest.getPermissions().get(0).getMethods(), hasSize(2));
        assertThat(manifest.getPermissions().get(0).getMethods().get(1), is("transfer"));

        assertThat(manifest.getTrusts(), is(notNullValue()));
        assertThat(manifest.getTrusts(), hasSize(1));
        assertFalse(manifest.trusts_isWildCard());
        assertThat(manifest.getTrusts().get(0), is("0xde5f57d430d3dece511cf975a8d37848cb9e0525"));

        assertThat(manifest.getSafeMethods(), is(notNullValue()));
        assertThat(manifest.getSafeMethods(), hasSize(6));
        assertFalse(manifest.safeMethods_isWildCard());
        assertThat(manifest.getSafeMethods(),
                containsInAnyOrder(
                        "name",
                        "symbol",
                        "decimals",
                        "totalSupply",
                        "balanceOf",
                        "supportedStandards"
                ));

        assertThat(manifest.getExtra(), is(nullValue()));
    }

    @Test
    public void testGetContractState_wildCards() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"id\": -2,\n" +
                        "        \"hash\": \"0x668e0c1f9d7b70a99dd9e06eadd4c784d641afbc\",\n" +
                        "        \"script\": \"QetD9PQ=\",\n" +
                        "        \"manifest\": {\n" +
                        "            \"groups\": [\n" +
                        "                {\n" +
                        "                    \"pubKey\": \"03b209fd4f53a7170ea4444e0cb0a6bb6a53c2bd016926989cf85f9b0fba17a70c\",\n" +
                        "                    \"signature\": \"41414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"features\": {\n" +
                        "                \"storage\": true,\n" +
                        "                \"payable\": false\n" +
                        "            },\n" +
                        "            \"abi\": {\n" +
                        "                \"hash\": \"0x668e0c1f9d7b70a99dd9e06eadd4c784d641afbc\",\n" +
                        "                \"entryPoint\": {\n" +
                        "                    \"name\": \"Main\",\n" +
                        "                    \"parameters\": [\n" +
                        "                        {\n" +
                        "                            \"name\": \"operation\",\n" +
                        "                            \"type\": \"String\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"name\": \"args\",\n" +
                        "                            \"type\": \"Array\"\n" +
                        "                        }\n" +
                        "                    ],\n" +
                        "                    \"returnType\": \"Void\"\n" +
                        "                },\n" +
                        "                \"methods\": [\n" +
                        "                    {\n" +
                        "                        \"name\": \"name\",\n" +
                        "                        \"parameters\": [],\n" +
                        "                        \"returnType\": \"String\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"events\": [\n" +
                        "                    {\n" +
                        "                        \"name\": \"Transfer\",\n" +
                        "                        \"parameters\": [\n" +
                        "                            {\n" +
                        "                                \"name\": \"from\",\n" +
                        "                                \"type\": \"Hash160\"\n" +
                        "                            },\n" +
                        "                            {\n" +
                        "                                \"name\": \"to\",\n" +
                        "                                \"type\": \"Hash160\"\n" +
                        "                            },\n" +
                        "                            {\n" +
                        "                                \"name\": \"amount\",\n" +
                        "                                \"type\": \"Integer\"\n" +
                        "                            }\n" +
                        "                        ],\n" +
                        "                        \"returnType\": \"Signature\"\n" +
                        "                    }\n" +
                        "                ]\n" +
                        "            },\n" +
                        "            \"permissions\": [],\n" +
                        "            \"trusts\": \"*\",\n" +
                        "            \"safeMethods\": \"*\",\n" +
                        "            \"extra\": \"individual info\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetContractState getContractState = deserialiseResponse(NeoGetContractState.class);
        NeoGetContractState.ContractState.ContractManifest manifest = getContractState.getContractState().getManifest();
        assertTrue(manifest.trusts_isWildCard());
        assertTrue(manifest.safeMethods_isWildCard());
        assertThat(manifest.getExtra(), is("individual info"));
    }

    @Test
    public void testGetMemPool() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 67,\n" +
                        "    \"result\": {\n" +
                        "        \"height\": 5492,\n" +
                        "        \"verified\": [\n" +
                        "            \"0x9786cce0dddb524c40ddbdd5e31a41ed1f6b5c8a683c122f627ca4a007a7cf4e\",\n" +
                        "            \"0xb488ad25eb474f89d5ca3f985cc047ca96bc7373a6d3da8c0f192722896c1cd7\"\n" +
                        "        ],\n" +
                        "        \"unverified\": [\n" +
                        "            \"0x9786cce0dddb524c40ddbdd5e31a41ed1f6b5c8a683c122f627ca4a007a7cf4e\",\n" +
                        "            \"0xb488ad25eb474f89d5ca3f985cc047ca96bc7373a6d3da8c0f192722896c1cd7\"\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetMemPool getMemPool = deserialiseResponse(NeoGetMemPool.class);
        assertThat(getMemPool.getMemPoolDetails(), is(notNullValue()));

        assertThat(getMemPool.getMemPoolDetails().getHeight(), is(5492L));

        assertThat(getMemPool.getMemPoolDetails().getVerified(), notNullValue());
        assertThat(getMemPool.getMemPoolDetails().getVerified(), hasSize(2));
        assertThat(
                getMemPool.getMemPoolDetails().getVerified(),
                containsInAnyOrder(
                        "0x9786cce0dddb524c40ddbdd5e31a41ed1f6b5c8a683c122f627ca4a007a7cf4e",
                        "0xb488ad25eb474f89d5ca3f985cc047ca96bc7373a6d3da8c0f192722896c1cd7"
                )
        );

        assertThat(getMemPool.getMemPoolDetails().getUnverified(), notNullValue());
        assertThat(getMemPool.getMemPoolDetails().getUnverified(), hasSize(2));
        assertThat(
                getMemPool.getMemPoolDetails().getUnverified(),
                containsInAnyOrder(
                        "0x9786cce0dddb524c40ddbdd5e31a41ed1f6b5c8a683c122f627ca4a007a7cf4e",
                        "0xb488ad25eb474f89d5ca3f985cc047ca96bc7373a6d3da8c0f192722896c1cd7"
                )
        );
    }

    @Test
    public void testGetMemPool_Empty() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 67,\n" +
                        "    \"result\": {\n" +
                        "        \"height\": 5492,\n" +
                        "        \"verified\": [],\n" +
                        "        \"unverified\": []\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetMemPool getMemPool = deserialiseResponse(NeoGetMemPool.class);
        assertThat(getMemPool.getMemPoolDetails(), is(notNullValue()));

        assertThat(getMemPool.getMemPoolDetails().getHeight(), is(5492L));
        assertThat(getMemPool.getMemPoolDetails().getVerified(), notNullValue());
        assertThat(getMemPool.getMemPoolDetails().getVerified(), hasSize(0));
        assertThat(getMemPool.getMemPoolDetails().getUnverified(), notNullValue());
        assertThat(getMemPool.getMemPoolDetails().getUnverified(), hasSize(0));
    }

    @Test
    public void testGetRawMemPool() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 67,\n" +
                        "    \"result\": [\n" +
                        "        \"0x9786cce0dddb524c40ddbdd5e31a41ed1f6b5c8a683c122f627ca4a007a7cf4e\",\n" +
                        "        \"0xb488ad25eb474f89d5ca3f985cc047ca96bc7373a6d3da8c0f192722896c1cd7\",\n" +
                        "        \"0xf86f6f2c08fbf766ebe59dc84bc3b8829f1053f0a01deb26bf7960d99fa86cd6\"\n" +
                        "    ]\n" +
                        "}"
        );

        NeoGetRawMemPool getRawMemPool = deserialiseResponse(NeoGetRawMemPool.class);
        assertThat(getRawMemPool.getAddresses(), is(notNullValue()));

        assertThat(getRawMemPool.getAddresses(), hasSize(3));
        assertThat(
                getRawMemPool.getAddresses(),
                containsInAnyOrder(
                        "0x9786cce0dddb524c40ddbdd5e31a41ed1f6b5c8a683c122f627ca4a007a7cf4e",
                        "0xb488ad25eb474f89d5ca3f985cc047ca96bc7373a6d3da8c0f192722896c1cd7",
                        "0xf86f6f2c08fbf766ebe59dc84bc3b8829f1053f0a01deb26bf7960d99fa86cd6"
                )
        );
    }

    @Test
    public void testGetRawMemPool_Empty() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 67,\n" +
                        "    \"result\": []\n" +
                        "}"
        );
        NeoGetRawMemPool getRawMemPool = deserialiseResponse(NeoGetRawMemPool.class);
        assertThat(getRawMemPool.getAddresses(), notNullValue());
        assertThat(getRawMemPool.getAddresses(), hasSize(0));
    }

    @Test
    public void testGetTransaction() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"hash\": \"0x8b8b222ba4ae17eaf37d444210920690d0981b02c368f4f1973c8fd662438d89\",\n" +
                        "        \"size\": 267,\n" +
                        "        \"version\": 0,\n" +
                        "        \"nonce\": 1046354582,\n" +
                        "        \"sender\": \"AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4\",\n" +
                        "        \"sysfee\": \"9007810\",\n" +
                        "        \"netfee\": \"1267450\",\n" +
                        "        \"validuntilblock\": 2103622,\n" +
                        "        \"attributes\": [" +
                        "            {\n" +
                        "                \"type\": \"Signer\",\n" +
                        "                \"account\": \"0xf68f181731a47036a99f04dad90043a744edec0f\",\n" +
                        "                \"scopes\": \"CalledByEntry\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"script\": \"AGQMFObBATZUrxE9ipaL3KUsmUioK5U9DBQP7O1Ep0MA2doEn6k2cKQxFxiP9hPADAh0cmFuc2ZlcgwUiXcg2M129PAKv6N8Dt2InCCP3ptBYn1bUjg=\",\n" +
                        "        \"witnesses\": [\n" +
                        "            {\n" +
                        "                \"invocation\": \"DEBhsuS9LxQ2PKpx2XJJ/aGEr/pZ7qfZy77OyhDmWx+BobkQAnDPLg6ohOa9SSHa0OMDavUl7zpmJip3r8T5Dr1L\",\n" +
                        "                \"verification\": \"EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw==\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"blockhash\": \"0x8529cf7301d13cc13d85913b8367700080a6e96db045687b8db720e91e803299\",\n" +
                        "        \"confirmations\": 1388,\n" +
                        "        \"blocktime\": 1589019142879,\n" +
                        "        \"vmstate\": \"HALT\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetTransaction getTransaction = deserialiseResponse(NeoGetTransaction.class);
        assertThat(getTransaction.getTransaction(), is(notNullValue()));
        assertThat(getTransaction.getTransaction().getHash(),
                is("0x8b8b222ba4ae17eaf37d444210920690d0981b02c368f4f1973c8fd662438d89"));
        assertThat(getTransaction.getTransaction().getSize(), is(267L));
        assertThat(getTransaction.getTransaction().getVersion(), is(0));
        assertThat(getTransaction.getTransaction().getNonce(), is(1046354582L));
        assertThat(getTransaction.getTransaction().getSender(), is("AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4"));
        assertThat(getTransaction.getTransaction().getSysFee(), is("9007810"));
        assertThat(getTransaction.getTransaction().getNetFee(), is("1267450"));
        assertThat(getTransaction.getTransaction().getValidUntilBlock(), is(2103622L));
        assertThat(getTransaction.getTransaction().getAttributes(), is(notNullValue()));

        assertThat(getTransaction.getTransaction().getAttributes(), hasSize(1));
        assertThat(getTransaction.getTransaction().getAttributes(), is(notNullValue()));
        assertThat(getTransaction.getTransaction().getAttributes().get(0).getAsTransactionSigner().type,
                is(TransactionAttributeType.SIGNER));
        assertThat(getTransaction.getTransaction().getAttributes().get(0).getAsTransactionSigner().getAccount(),
                is("0xf68f181731a47036a99f04dad90043a744edec0f"));
        assertThat(getTransaction.getTransaction().getAttributes().get(0).getAsTransactionSigner().getScopes(),
                is(WitnessScope.CALLED_BY_ENTRY));
        assertThat(getTransaction.getTransaction().getAttributes(),
                containsInAnyOrder(
                        new TransactionSigner(
                                "0xf68f181731a47036a99f04dad90043a744edec0f",
                                WitnessScope.CALLED_BY_ENTRY
                        )
                ));

        assertThat(getTransaction.getTransaction().getScript(),
                is("AGQMFObBATZUrxE9ipaL3KUsmUioK5U9DBQP7O1Ep0MA2doEn6k2cKQxFxiP9hPADAh0cmFuc2ZlcgwUiXcg2M129PAKv6N8Dt2InCCP3ptBYn1bUjg="));
        assertThat(getTransaction.getTransaction().getWitnesses(), is(notNullValue()));
        assertThat(getTransaction.getTransaction().getWitnesses(), hasSize(1));
        assertThat(getTransaction.getTransaction().getWitnesses(),
                containsInAnyOrder(
                        new NeoWitness(
                                "DEBhsuS9LxQ2PKpx2XJJ/aGEr/pZ7qfZy77OyhDmWx+BobkQAnDPLg6ohOa9SSHa0OMDavUl7zpmJip3r8T5Dr1L",
                                "EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw=="
                        )
                ));
        assertThat(getTransaction.getTransaction().getBlockHash(),
                is("0x8529cf7301d13cc13d85913b8367700080a6e96db045687b8db720e91e803299"));
        assertThat(getTransaction.getTransaction().getConfirmations(), is(1388));
        assertThat(getTransaction.getTransaction().getBlockTime(), is(1589019142879L));
        assertThat(getTransaction.getTransaction().getVMState(), is("HALT"));
    }

    @Test
    public void testGetRawTransaction() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": \"00961a5e3e0feced44a74300d9da049fa93670a43117188ff6c272890000000000fa561300000000004619200000010feced44a74300d9da049fa93670a43117188ff6015600640c14e6c1013654af113d8a968bdca52c9948a82b953d0c140feced44a74300d9da049fa93670a43117188ff613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b523801420c4061b2e4bd2f14363caa71d97249fda184affa59eea7d9cbbececa10e65b1f81a1b9100270cf2e0ea884e6bd4921dad0e3036af525ef3a66262a77afc4f90ebd4b2b110c2103f1ec3c1e283e880de6e9c489f0f27c19007c53385aaa4c0c917c320079edadf2110b413073b3bb\"\n" +
                        "}"
        );

        NeoGetRawTransaction getRawTransaction = deserialiseResponse(NeoGetRawTransaction.class);
        assertThat(getRawTransaction.getRawTransaction(), is(notNullValue()));
        assertThat(getRawTransaction.getRawTransaction(),
                is("00961a5e3e0feced44a74300d9da049fa93670a43117188ff6c272890000000000fa561300000000004619200000010feced44a74300d9da049fa93670a43117188ff6015600640c14e6c1013654af113d8a968bdca52c9948a82b953d0c140feced44a74300d9da049fa93670a43117188ff613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b523801420c4061b2e4bd2f14363caa71d97249fda184affa59eea7d9cbbececa10e65b1f81a1b9100270cf2e0ea884e6bd4921dad0e3036af525ef3a66262a77afc4f90ebd4b2b110c2103f1ec3c1e283e880de6e9c489f0f27c19007c53385aaa4c0c917c320079edadf2110b413073b3bb")
        );
    }

    @Test
    public void testGetStorage() {
        buildResponse(
                "{\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"id\": 15,\n" +
                        "  \"result\": \"4c696e\"\n" +
                        "}"
        );

        NeoGetStorage getStorage = deserialiseResponse(NeoGetStorage.class);
        assertThat(getStorage.getStorage(), is(notNullValue()));
        assertThat(getStorage.getStorage(), is("4c696e"));
    }

    @Test
    public void testTransactionHeight() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": 1223\n" +
                        "}"
        );

        NeoGetTransactionHeight getTransactionHeight = deserialiseResponse(NeoGetTransactionHeight.class);
        assertThat(getTransactionHeight.getHeight(), is(new BigInteger("1223")));
    }

    @Test
    public void testGetValidators() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": [\n" +
                        "        {\n" +
                        "            \"publickey\": \"03f1ec3c1e283e880de6e9c489f0f27c19007c53385aaa4c0c917c320079edadf2\",\n" +
                        "            \"votes\": \"0\",\n" +
                        "            \"active\": false\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"publickey\": \"02494f3ff953e45ca4254375187004f17293f90a1aa4b1a89bc07065bc1da521f6\",\n" +
                        "            \"votes\": \"91600000\",\n" +
                        "            \"active\": true\n" +
                        "        }\n" +
                        "        " +
                        "    ]\n" +
                        "}"
        );

        NeoGetValidators getValidators = deserialiseResponse(NeoGetValidators.class);
        assertThat(getValidators.getValidators(), hasSize(2));
        assertThat(getValidators.getValidators(),
                containsInAnyOrder(
                        new NeoGetValidators.Validator(
                                "03f1ec3c1e283e880de6e9c489f0f27c19007c53385aaa4c0c917c320079edadf2",
                                "0", false),
                        new NeoGetValidators.Validator(
                                "02494f3ff953e45ca4254375187004f17293f90a1aa4b1a89bc07065bc1da521f6",
                                "91600000", true)
                )
        );
        assertThat(getValidators.getValidators().get(0).getPublicKey(),
                is("03f1ec3c1e283e880de6e9c489f0f27c19007c53385aaa4c0c917c320079edadf2"));
        assertThat(getValidators.getValidators().get(0).getVotes(), is("0"));
        assertThat(getValidators.getValidators().get(0).getVotesAsBigInteger(), is(BigInteger.valueOf(0)));
        assertThat(getValidators.getValidators().get(0).getActive(), is(false));
        assertThat(getValidators.getValidators().get(1).getActive(), is(true));
    }

    @Test
    public void testGetValidators_Empty() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 67,\n" +
                        "    \"result\": []\n" +
                        "}"
        );

        NeoGetValidators getValidators = deserialiseResponse(NeoGetValidators.class);
        assertThat(getValidators.getValidators(), is(notNullValue()));
        assertThat(getValidators.getValidators(), hasSize(0));
    }

    // Node Methods

    @Test
    public void testGetConnectionCount() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": 2\n" +
                        "}"
        );

        NeoConnectionCount connectionCount = deserialiseResponse(NeoConnectionCount.class);
        assertThat(connectionCount.getCount(), is(2));
    }

    @Test
    public void testGetPeers() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"unconnected\": [\n" +
                        "            {\n" +
                        "                \"address\": \"127.0.0.1\",\n" +
                        "                \"port\": 20335\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"address\": \"127.0.0.1\",\n" +
                        "                \"port\": 20336\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"address\": \"127.0.0.1\",\n" +
                        "                \"port\": 20337\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"bad\": [\n" +
                        "            {\n" +
                        "                \"address\": \"127.0.0.1\",\n" +
                        "                \"port\": 20333\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"connected\": [\n" +
                        "            {\n" +
                        "                \"address\": \"172.18.0.3\",\n" +
                        "                \"port\": 40333\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"address\": \"172.18.0.4\",\n" +
                        "                \"port\": 20333\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetPeers getPeers = deserialiseResponse(NeoGetPeers.class);
        assertThat(getPeers.getPeers(), is(notNullValue()));
        assertThat(getPeers.getPeers().getUnconnected(), is(notNullValue()));
        assertThat(getPeers.getPeers().getUnconnected(), hasSize(3));
        assertThat(getPeers.getPeers().getUnconnected(),
                containsInAnyOrder(
                        new NeoGetPeers.AddressEntry(
                                "127.0.0.1",
                                20335
                        ),
                        new NeoGetPeers.AddressEntry(
                                "127.0.0.1",
                                20336
                        ),
                        new NeoGetPeers.AddressEntry(
                                "127.0.0.1",
                                20337
                        )
                ));

        assertThat(getPeers.getPeers().getBad(), is(notNullValue()));
        assertThat(getPeers.getPeers().getBad(), hasSize(1));
        assertThat(getPeers.getPeers().getBad(),
                containsInAnyOrder(
                        new NeoGetPeers.AddressEntry(
                                "127.0.0.1",
                                20333
                        )
                ));

        assertThat(getPeers.getPeers().getConnected(), is(notNullValue()));
        assertThat(getPeers.getPeers().getConnected(), hasSize(2));
        assertThat(getPeers.getPeers().getConnected(),
                containsInAnyOrder(
                        new NeoGetPeers.AddressEntry(
                                "172.18.0.3",
                                40333
                        ),
                        new NeoGetPeers.AddressEntry(
                                "172.18.0.4",
                                20333
                        )
                )
        );
    }

    @Test
    public void testGetPeers_Empty() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"unconnected\": [],\n" +
                        "        \"bad\": [],\n" +
                        "        \"connected\": []\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetPeers getPeers = deserialiseResponse(NeoGetPeers.class);
        assertThat(getPeers.getPeers(), is(notNullValue()));
        assertThat(getPeers.getPeers().getUnconnected(), is(notNullValue()));
        assertThat(getPeers.getPeers().getBad(), is(notNullValue()));
        assertThat(getPeers.getPeers().getConnected(), is(notNullValue()));

        assertThat(getPeers.getPeers().getUnconnected(), hasSize(0));
        assertThat(getPeers.getPeers().getBad(), hasSize(0));
        assertThat(getPeers.getPeers().getConnected(), hasSize(0));
    }

    @Test
    public void testGetVersion() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"tcpPort\": 10333,\n" +
                        "        \"wsPort\": 10334,\n" +
                        "        \"nonce\": 1845610272,\n" +
                        "        \"userAgent\": \"/Neo:3.0.0-preview2-00/\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetVersion getVersion = deserialiseResponse(NeoGetVersion.class);
        assertThat(getVersion.getVersion(), is(notNullValue()));
        assertThat(getVersion.getVersion().getTCPPort(), is(10333));
        assertThat(getVersion.getVersion().getWSPort(), is(10334));
        assertThat(getVersion.getVersion().getNonce(), is(1845610272L));
        assertThat(getVersion.getVersion().getUserAgent(), is("/Neo:3.0.0-preview2-00/"));
    }

    @Test
    public void testSendRawTransaction() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"hash\": \"0xb0748d216c9c0d0498094cdb50407035917b350fc0338c254b78f944f723b770\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoSendRawTransaction sendRawTransaction = deserialiseResponse(NeoSendRawTransaction.class);
        assertThat(sendRawTransaction.getSendRawTransaction(), is(notNullValue()));
        assertThat(sendRawTransaction.getSendRawTransaction().getHash(),
                is("0xb0748d216c9c0d0498094cdb50407035917b350fc0338c254b78f944f723b770"));
    }

    @Test
    public void testSubmitBlock() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": true\n" +
                        "}"
        );

        NeoSubmitBlock submitBlock = deserialiseResponse(NeoSubmitBlock.class);
        assertThat(submitBlock.getSubmitBlock(), is(notNullValue()));
        assertThat(submitBlock.getSubmitBlock(), is(true));
    }

    // SmartContract Methods

    @Test
    public void testInvokeFunction() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"script\": \"0c14e6c1013654af113d8a968bdca52c9948a82b953d11c00c0962616c616e63654f660c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52\",\n" +
                        "        \"state\": \"HALT\",\n" +
                        "        \"gasConsumed\": \"2007570\",\n" +
                        "        \"stack\": [\n" +
                        "            {\n" +
                        "                \"type\": \"ByteString\",\n" +
                        "                \"value\": \"dHJhbnNmZXI=\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"type\": \"Map\",\n" +
                        "                \"value\": [\n" +
                        "                    {\n" +
                        "                        \"key\": {\n" +
                        "                            \"type\": \"ByteString\",\n" +
                        "                            \"value\": \"lBNDI5IT+g52XxAnznQvSNt3mpY=\"\n" +
                        "                        },\n" +
                        "                        \"value\": {\n" +
                        "                            \"type\": \"Integer\",\n" +
                        "                            \"value\": \"1\"\n" +
                        "                        }\n" +
                        "                    }\n" +
                        "                ]\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoInvokeFunction invokeFunction = deserialiseResponse(NeoInvokeFunction.class);
        assertThat(invokeFunction.getInvocationResult().getScript(),
                is("0c14e6c1013654af113d8a968bdca52c9948a82b953d11c00c0962616c616e63654f660c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52"));
        assertThat(invokeFunction.getInvocationResult().getState(), is("HALT"));
        assertThat(invokeFunction.getInvocationResult().getGasConsumed(), is("2007570"));

        assertThat(invokeFunction.getInvocationResult().getStack(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getStack(), hasSize(2));

        StackItem stackItem0 = invokeFunction.getInvocationResult().getStack().get(0);
        assertThat(stackItem0.getType(), is(StackItemType.BYTE_STRING));
        assertThat(stackItem0.asByteString().getAsString(), is("transfer"));

        StackItem stackItem1 = invokeFunction.getInvocationResult().getStack().get(1);
        assertThat(stackItem1.getType(), is(StackItemType.MAP));
        assertThat(stackItem1.asMap().size(), equalTo(1));
        BigInteger value = stackItem1.asMap()
                .get(Numeric.hexStringToByteArray("941343239213fa0e765f1027ce742f48db779a96"))
                .asInteger()
                .getValue();
        assertThat(value, is(new BigInteger("1")));
    }

    @Test
    public void testInvokeFunction_empty_Stack() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"script\": \"00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc\",\n" +
                        "        \"state\": \"HALT, BREAK\",\n" +
                        "        \"gasConsumed\": \"2.489\",\n" +
                        "        \"stack\": []\n" +
                        "    }\n" +
                        "}"
        );

        NeoInvokeFunction invokeFunction = deserialiseResponse(NeoInvokeFunction.class);
        assertThat(invokeFunction.getInvocationResult(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getScript(),
                is("00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc"));
        assertThat(invokeFunction.getInvocationResult().getState(), is("HALT, BREAK"));
        assertThat(invokeFunction.getInvocationResult().getGasConsumed(), is("2.489"));

        assertThat(invokeFunction.getInvocationResult().getStack(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getStack(), hasSize(0));
    }

    @Test
    public void testInvokeFunction_withoutOrEmpty_Params() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"script\": \"10c00c0962616c616e63654f660c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52\",\n" +
                        "        \"state\": \"FAULT\",\n" +
                        "        \"gasConsumed\": \"2007390\",\n" +
                        "        \"stack\": []\n" +
                        "    }\n" +
                        "}"
        );

        NeoInvokeFunction invokeFunction = deserialiseResponse(NeoInvokeFunction.class);
        assertThat(invokeFunction.getInvocationResult(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getScript(),
                is("10c00c0962616c616e63654f660c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52"));
        assertThat(invokeFunction.getInvocationResult().getState(), is("FAULT"));
        assertThat(invokeFunction.getInvocationResult().getGasConsumed(), is("2007390"));
        assertThat(invokeFunction.getInvocationResult().getStack(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getStack(), hasSize(0));
    }

    @Test
    public void testInvokeScript() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 3,\n" +
                        "    \"result\": {\n" +
                        "        \"script\": \"00046e616d656724058e5e1b6008847cd662728549088a9ee82191\",\n" +
                        "        \"state\": \"HALT, BREAK\",\n" +
                        "        \"gasConsumed\": \"0.161\",\n" +
                        "        \"stack\": [\n" +
                        "            {\n" +
                        "                \"type\": \"ByteString\",\n" +
                        "                \"value\": \"VHJhbnNmZXI=\"\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoInvokeScript invokeScript = deserialiseResponse(NeoInvokeScript.class);
        assertThat(invokeScript.getInvocationResult(), is(notNullValue()));
        assertThat(invokeScript.getInvocationResult().getScript(),
                is("00046e616d656724058e5e1b6008847cd662728549088a9ee82191"));
        assertThat(invokeScript.getInvocationResult().getState(), is("HALT, BREAK"));
        assertThat(invokeScript.getInvocationResult().getGasConsumed(), is("0.161"));
        assertThat(invokeScript.getInvocationResult().getStack(), is(notNullValue()));
        assertThat(invokeScript.getInvocationResult().getStack(), hasSize(1));
        assertThat(invokeScript.getInvocationResult().getStack(),
                hasItem(
                        new ByteStringStackItem("Transfer".getBytes())
                ));
    }

    // Utilities Methods

    @Test
    public void testListPlugins() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": [\n" +
                        "        {\n" +
                        "            \"name\": \"ApplicationLogs\",\n" +
                        "            \"version\": \"3.0.0.0\",\n" +
                        "            \"interfaces\": [\n" +
                        "                \"IPersistencePlugin\"\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"LevelDBStore\",\n" +
                        "            \"version\": \"3.0.0.0\",\n" +
                        "            \"interfaces\": [\n" +
                        "                \"IStorageProvider\"\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"RocksDBStore\",\n" +
                        "            \"version\": \"3.0.0.0\",\n" +
                        "            \"interfaces\": [\n" +
                        "                \"IStorageProvider\"\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"RpcNep5Tracker\",\n" +
                        "            \"version\": \"3.0.0.0\",\n" +
                        "            \"interfaces\": [\n" +
                        "                \"IPersistencePlugin\"\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"RpcServerPlugin\",\n" +
                        "            \"version\": \"3.0.0.0\",\n" +
                        "            \"interfaces\": []\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"StatesDumper\",\n" +
                        "            \"version\": \"3.0.0.0\",\n" +
                        "            \"interfaces\": [\n" +
                        "                \"IPersistencePlugin\"\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"SystemLog\",\n" +
                        "            \"version\": \"3.0.0.0\",\n" +
                        "            \"interfaces\": [\n" +
                        "                \"ILogPlugin\"\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}"
        );

        NeoListPlugins listPlugins = deserialiseResponse(NeoListPlugins.class);
        assertThat(listPlugins.getPlugins(), is(notNullValue()));
        assertThat(listPlugins.getPlugins(), hasSize(7));

        NeoListPlugins.Plugin plugin = listPlugins.getPlugins().get(0);
        assertThat(NodePluginType.valueOfName(plugin.getName()),
                is(NodePluginType.APPLICATION_LOGS));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(1));
        assertThat(plugin.getInterfaces(), containsInAnyOrder("IPersistencePlugin"));

        plugin = listPlugins.getPlugins().get(1);
        assertThat(NodePluginType.valueOfName(plugin.getName()),
                is(NodePluginType.LEVEL_DB_STORE));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(1));
        assertThat(plugin.getInterfaces(), containsInAnyOrder("IStorageProvider"));

        plugin = listPlugins.getPlugins().get(2);
        assertThat(NodePluginType.valueOfName(plugin.getName()),
                is(NodePluginType.ROCKS_DB_STORE));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(1));
        assertThat(plugin.getInterfaces(), containsInAnyOrder("IStorageProvider"));

        plugin = listPlugins.getPlugins().get(3);
        assertThat(NodePluginType.valueOfName(plugin.getName()),
                is(NodePluginType.RPC_NEP5_TRACKER));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(1));
        assertThat(plugin.getInterfaces(), containsInAnyOrder("IPersistencePlugin"));

        plugin = listPlugins.getPlugins().get(4);
        assertThat(NodePluginType.valueOfName(plugin.getName()),
                is(NodePluginType.RPC_SERVER_PLUGIN));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(0));

        plugin = listPlugins.getPlugins().get(5);
        assertThat(NodePluginType.valueOfName(plugin.getName()),
                is(NodePluginType.STATES_DUMPER));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(1));
        assertThat(plugin.getInterfaces(), containsInAnyOrder("IPersistencePlugin"));

        plugin = listPlugins.getPlugins().get(6);
        assertThat(NodePluginType.valueOfName(plugin.getName()),
                is(NodePluginType.SYSTEM_LOG));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(1));
        assertThat(plugin.getInterfaces(), containsInAnyOrder("ILogPlugin"));
    }

    @Test
    public void testValidateAddress() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"address\": \"AQVh2pG732YvtNaxEGkQUei3YA4cvo7d2i\",\n" +
                        "        \"isvalid\": true\n" +
                        "    }\n" +
                        "}"
        );

        NeoValidateAddress validateAddress = deserialiseResponse(NeoValidateAddress.class);
        assertThat(validateAddress.getValidation().getAddress(), is("AQVh2pG732YvtNaxEGkQUei3YA4cvo7d2i"));
        assertThat(validateAddress.getValidation().getValid(), is(true));
        assertThat(validateAddress.getValidation().isValid(), is(true));
    }

    @Test
    public void testCloseWallet() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": true\n" +
                        "}"
        );

        NeoCloseWallet closeWallet = deserialiseResponse(NeoCloseWallet.class);
        assertThat(closeWallet.getCloseWallet(), is(true));
    }

    @Test
    public void testOpenWallet() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": true\n" +
                        "}"
        );

        NeoOpenWallet openWallet = deserialiseResponse(NeoOpenWallet.class);
        assertThat(openWallet.getOpenWallet(), is(true));
    }

    @Test
    public void testDumpPrivKey() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": \"L1ZW4aRmy4MMG3x3wk9S6WEJJxcaZi72YxPx854Lspdo9jNFxEoJ\"\n" +
                        "}"
        );

        NeoDumpPrivKey dumpPrivKey = deserialiseResponse(NeoDumpPrivKey.class);
        assertThat(dumpPrivKey.getDumpPrivKey(), is(notNullValue()));
        assertThat(dumpPrivKey.getDumpPrivKey(), is("L1ZW4aRmy4MMG3x3wk9S6WEJJxcaZi72YxPx854Lspdo9jNFxEoJ"));
    }

    @Test
    public void testGetBalance() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"balance\": \"200\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetBalance getBalance = deserialiseResponse(NeoGetBalance.class);
        assertThat(getBalance.getBalance(), is(notNullValue()));
        assertThat(getBalance.getBalance().getBalance(), is("200"));
    }

    @Test
    public void testGetBalance_UpperCase() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"Balance\": \"199999990.0\"\n"
                        + "  }\n"
                        + "}"
        );

        NeoGetBalance getBalance = deserialiseResponse(NeoGetBalance.class);
        assertThat(getBalance.getBalance(), is(notNullValue()));
        assertThat(getBalance.getBalance().getBalance(), is("199999990.0"));
    }

    @Test
    public void testGetNewAddress() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": \"APuGosNQYQoRYYMxvay3yZsragzvfBMdNs\"\n" +
                        "}"
        );

        NeoGetNewAddress getNewAddress = deserialiseResponse(NeoGetNewAddress.class);
        assertThat(getNewAddress.getAddress(), is(notNullValue()));
        assertThat(getNewAddress.getAddress(), is("APuGosNQYQoRYYMxvay3yZsragzvfBMdNs"));
    }

    @Test
    public void testGetUnclaimedGas() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": \"289799420400\"\n" +
                        "}"
        );

        NeoGetUnclaimedGas getUnclaimedGas = deserialiseResponse(NeoGetUnclaimedGas.class);
        assertThat(getUnclaimedGas.getUnclaimedGas(), is(notNullValue()));
        assertThat(getUnclaimedGas.getUnclaimedGas(), is("289799420400"));
    }

    @Test
    public void testImportPrivKey() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"address\": \"AYhJaF5oqUscfNfH87KQHm1YwuKrwPgkMA\",\n" +
                        "        \"haskey\": true,\n" +
                        "        \"label\": null,\n" +
                        "        \"watchonly\": false\n" +
                        "    }\n" +
                        "}"
        );

        NeoImportPrivKey importPrivKey = deserialiseResponse(NeoImportPrivKey.class);
        assertThat(importPrivKey.getAddresses(), is(notNullValue()));
        assertThat(importPrivKey.getAddresses().getAddress(), is("AYhJaF5oqUscfNfH87KQHm1YwuKrwPgkMA"));
        assertThat(importPrivKey.getAddresses().getHasKey(), is(true));
        assertThat(importPrivKey.getAddresses().getLabel(), is(nullValue()));
        assertThat(importPrivKey.getAddresses().getWatchOnly(), is(false));
    }

    @Test
    public void testListAddress() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": [\n" +
                        "        {\n" +
                        "            \"address\": \"AK5AmzrrM3sw3kbCHXpHNeuK3kkjnneUrb\",\n" +
                        "            \"haskey\": true,\n" +
                        "            \"label\": \"hodl\",\n" +
                        "            \"watchonly\": false\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"address\": \"AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4\",\n" +
                        "            \"haskey\": false,\n" +
                        "            \"label\": null,\n" +
                        "            \"watchonly\": true\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}"
        );

        NeoListAddress listAddress = deserialiseResponse(NeoListAddress.class);
        assertThat(listAddress.getAddresses(), is(notNullValue()));
        assertThat(listAddress.getAddresses(), hasSize(2));

        NeoAddress neoAddress = listAddress.getAddresses().get(0);
        assertThat(neoAddress.getAddress(), is("AK5AmzrrM3sw3kbCHXpHNeuK3kkjnneUrb"));
        assertThat(neoAddress.getHasKey(), is(true));
        assertThat(neoAddress.getLabel(), is("hodl"));
        assertThat(neoAddress.getWatchOnly(), is(false));

        neoAddress = listAddress.getAddresses().get(1);
        assertThat(neoAddress.getAddress(), is("AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4"));
        assertThat(neoAddress.getHasKey(), is(false));
        assertThat(neoAddress.getLabel(), is(nullValue()));
        assertThat(neoAddress.getWatchOnly(), is(true));
    }

    @Test
    public void testSendFrom() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"hash\": \"0x6818f446c2e503998ac766a8a175f86d9a89885423f6b055aa123c984625833e\",\n" +
                        "        \"size\": 266,\n" +
                        "        \"version\": 0,\n" +
                        "        \"nonce\": 1762654532,\n" +
                        "        \"sender\": \"AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4\",\n" +
                        "        \"sysfee\": \"9007810\",\n" +
                        "        \"netfee\": \"1266450\",\n" +
                        "        \"validuntilblock\": 2106392,\n" +
                        "        \"attributes\": [" +
                        "            {\n" +
                        "                \"type\": \"Signer\",\n" +
                        "                \"account\": \"0xf68f181731a47036a99f04dad90043a744edec0f\",\n" +
                        "                \"scopes\": \"CalledByEntry\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"script\": \"GgwU5sEBNlSvET2KlovcpSyZSKgrlT0MFA/s7USnQwDZ2gSfqTZwpDEXGI/2E8AMCHRyYW5zZmVyDBSJdyDYzXb08Aq/o3wO3YicII/em0FifVtSOA==\",\n" +
                        "        \"witnesses\": [\n" +
                        "            {\n" +
                        "                \"invocation\": \"DEAZaoPvbyaQyUYqIBc4MyDCGxGhxlPCuBbcHn5cYMpHPi2JD4PX2I1EsDPNtrEESPo//WBnsKyl5o5ViR5YDcJR\",\n" +
                        "                \"verification\": \"EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw==\"\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoSendFrom sendFrom = deserialiseResponse(NeoSendFrom.class);
        assertThat(sendFrom.getSendFrom(), is(notNullValue()));

        assertThat(sendFrom.getSendFrom().getHash(),
                is("0x6818f446c2e503998ac766a8a175f86d9a89885423f6b055aa123c984625833e"));
        assertThat(sendFrom.getSendFrom().getSize(), is(266L));
        assertThat(sendFrom.getSendFrom().getVersion(), is(0));
        assertThat(sendFrom.getSendFrom().getNonce(), is(1762654532L));
        assertThat(sendFrom.getSendFrom().getSender(), is("AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4"));
        assertThat(sendFrom.getSendFrom().getSysFee(), is("9007810"));
        assertThat(sendFrom.getSendFrom().getNetFee(), is("1266450"));
        assertThat(sendFrom.getSendFrom().getValidUntilBlock(), is(2106392L));
        assertThat(sendFrom.getSendFrom().getAttributes(), is(notNullValue()));

        assertThat(sendFrom.getSendFrom().getAttributes(), hasSize(1));
        assertThat(sendFrom.getSendFrom().getAttributes(), is(notNullValue()));
        assertThat(sendFrom.getSendFrom().getAttributes().get(0).getType(), is(TransactionAttributeType.SIGNER));
        assertThat(sendFrom.getSendFrom().getAttributes().get(0).getAsTransactionSigner().type,
                is(TransactionAttributeType.SIGNER));
        assertThat(sendFrom.getSendFrom().getAttributes().get(0).getAsTransactionSigner().getAccount(),
                is("0xf68f181731a47036a99f04dad90043a744edec0f"));
        assertThat(sendFrom.getSendFrom().getAttributes().get(0).getAsTransactionSigner().getScopes(),
                is(WitnessScope.CALLED_BY_ENTRY));

        assertThat(sendFrom.getSendFrom().getAttributes(),
                containsInAnyOrder(
                        new TransactionSigner("0xf68f181731a47036a99f04dad90043a744edec0f",
                                WitnessScope.CALLED_BY_ENTRY)
                ));

        assertThat(sendFrom.getSendFrom().getScript(),
                is("GgwU5sEBNlSvET2KlovcpSyZSKgrlT0MFA/s7USnQwDZ2gSfqTZwpDEXGI/2E8AMCHRyYW5zZmVyDBSJdyDYzXb08Aq/o3wO3YicII/em0FifVtSOA=="));

        assertThat(sendFrom.getSendFrom().getWitnesses(), is(notNullValue()));
        assertThat(sendFrom.getSendFrom().getWitnesses(), hasSize(1));
        assertThat(sendFrom.getSendFrom().getWitnesses(),
                containsInAnyOrder(
                        new NeoWitness(
                                "DEAZaoPvbyaQyUYqIBc4MyDCGxGhxlPCuBbcHn5cYMpHPi2JD4PX2I1EsDPNtrEESPo//WBnsKyl5o5ViR5YDcJR",
                                "EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw=="
                        )
                ));
    }

    @Test
    public void testSendMany() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"hash\": \"0xf60ec3b0810fb8c17a9a05eaeb3b361ead889a38d3fd1bf2d561a6e7001bb2f5\",\n" +
                        "        \"size\": 352,\n" +
                        "        \"version\": 0,\n" +
                        "        \"nonce\": 1256822346,\n" +
                        "        \"sender\": \"AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4\",\n" +
                        "        \"sysfee\": \"18015620\",\n" +
                        "        \"netfee\": \"1352450\",\n" +
                        "        \"validuntilblock\": 2106840,\n" +
                        "        \"attributes\": [\n" +
                        "            {\n" +
                        "                \"type\": \"Signer\",\n" +
                        "                \"account\": \"0xbe175fb771d5782282b7598b56c26a2f5ebf2d24\",\n" +
                        "                \"scopes\": \"CalledByEntry\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"type\": \"Signer\",\n" +
                        "                \"account\": \"0xf68f181731a47036a99f04dad90043a744edec0f\",\n" +
                        "                \"scopes\": \"CalledByEntry\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"script\": \"AGQMFObBATZUrxE9ipaL3KUsmUioK5U9DBQP7O1Ep0MA2doEn6k2cKQxFxiP9hPADAh0cmFuc2ZlcgwUiXcg2M129PAKv6N8Dt2InCCP3ptBYn1bUjgaDBQP7O1Ep0MA2doEn6k2cKQxFxiP9gwUD+ztRKdDANnaBJ+pNnCkMRcYj/YTwAwIdHJhbnNmZXIMFIl3INjNdvTwCr+jfA7diJwgj96bQWJ9W1I4\",\n" +
                        "        \"witnesses\": [\n" +
                        "            {\n" +
                        "                \"invocation\": \"DEDjHdgTfdXKx1R9f4D1lRklhisjDOkkMt7t1fO1CPO31gVQZUiWJc7GvJqjkR35iDjJjGIwd3s/Lm7q71rwdVC4\",\n" +
                        "                \"verification\": \"EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw==\"\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoSendMany sendMany = deserialiseResponse(NeoSendMany.class);
        assertThat(sendMany.getSendMany(), is(notNullValue()));

        assertThat(sendMany.getSendMany().getHash(),
                is("0xf60ec3b0810fb8c17a9a05eaeb3b361ead889a38d3fd1bf2d561a6e7001bb2f5"));
        assertThat(sendMany.getSendMany().getSize(), is(352L));
        assertThat(sendMany.getSendMany().getVersion(), is(0));
        assertThat(sendMany.getSendMany().getNonce(), is(1256822346L));
        assertThat(sendMany.getSendMany().getSender(), is("AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4"));
        assertThat(sendMany.getSendMany().getSysFee(), is("18015620"));
        assertThat(sendMany.getSendMany().getNetFee(), is("1352450"));
        assertThat(sendMany.getSendMany().getValidUntilBlock(), is(2106840L));

        assertThat(sendMany.getSendMany().getAttributes(), is(notNullValue()));
        assertThat(sendMany.getSendMany().getAttributes(), hasSize(2));
        assertThat(sendMany.getSendMany().getAttributes(),
                containsInAnyOrder(
                        new TransactionSigner(
                                "0xbe175fb771d5782282b7598b56c26a2f5ebf2d24",
                                WitnessScope.CALLED_BY_ENTRY),
                        new TransactionSigner(
                                "0xf68f181731a47036a99f04dad90043a744edec0f",
                                WitnessScope.CALLED_BY_ENTRY
                        )
                ));
        assertThat(sendMany.getSendMany().getAttributes().get(0).getType(), is(TransactionAttributeType.SIGNER));
        assertThat(sendMany.getSendMany().getAttributes().get(0).getAsTransactionSigner().getAccount(),
                is("0xbe175fb771d5782282b7598b56c26a2f5ebf2d24"));
        assertThat(sendMany.getSendMany().getAttributes().get(0).getAsTransactionSigner().getScopes(),
                is(WitnessScope.CALLED_BY_ENTRY));

        assertThat(sendMany.getSendMany().getScript(),
                is("AGQMFObBATZUrxE9ipaL3KUsmUioK5U9DBQP7O1Ep0MA2doEn6k2cKQxFxiP9hPADAh0cmFuc2ZlcgwUiXcg2M129PAKv6N8Dt2InCCP3ptBYn1bUjgaDBQP7O1Ep0MA2doEn6k2cKQxFxiP9gwUD+ztRKdDANnaBJ+pNnCkMRcYj/YTwAwIdHJhbnNmZXIMFIl3INjNdvTwCr+jfA7diJwgj96bQWJ9W1I4"));

        assertThat(sendMany.getSendMany().getWitnesses(), is(notNullValue()));
        assertThat(sendMany.getSendMany().getWitnesses(), hasSize(1));
        assertThat(sendMany.getSendMany().getWitnesses(),
                containsInAnyOrder(
                        new NeoWitness(
                                "DEDjHdgTfdXKx1R9f4D1lRklhisjDOkkMt7t1fO1CPO31gVQZUiWJc7GvJqjkR35iDjJjGIwd3s/Lm7q71rwdVC4",
                                "EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw=="
                        )
                ));
    }

    @Test
    public void testSendMany_Empty_Transaction() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"error\": {\n" +
                        "        \"code\": -32602,\n" +
                        "        \"message\": \"Invalid params\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoSendMany sendMany = deserialiseResponse(NeoSendMany.class);
        assertTrue(sendMany.hasError());
    }

    @Test
    public void testSendToAddress() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"hash\": \"0xabd78548399bbe684fac50b6a71d0ce3f689497d4e79cb26a2b4dfb211782c39\",\n" +
                        "        \"size\": 375,\n" +
                        "        \"version\": 0,\n" +
                        "        \"nonce\": 1509730265,\n" +
                        "        \"sender\": \"AK5AmzrrM3sw3kbCHXpHNeuK3kkjnneUrb\",\n" +
                        "        \"sysfee\": \"9007810\",\n" +
                        "        \"netfee\": \"2375840\",\n" +
                        "        \"validuntilblock\": 2106930,\n" +
                        "        \"attributes\": [" +
                        "            {\n" +
                        "                \"type\": \"Signer\",\n" +
                        "                \"account\": \"0xf68f181731a47036a99f04dad90043a744edec0f\",\n" +
                        "                \"scopes\": \"CalledByEntry\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"script\": \"GgwU5sEBNlSvET2KlovcpSyZSKgrlT0MFA/s7USnQwDZ2gSfqTZwpDEXGI/2E8AMCHRyYW5zZmVyDBSJdyDYzXb08Aq/o3wO3YicII/em0FifVtSOA==\",\n" +
                        "        \"witnesses\": [\n" +
                        "            {\n" +
                        "                \"invocation\": \"DECstBmb75AW65NjA35fFlSxszuLRDUzd0nnbfyH8MlnSA02f6B1XlvItpZQBsAd7Pvqa7S+olPAKDO0qtq3ZtOB\",\n" +
                        "                \"verification\": \"DCED8ew8Hig+iA3m6cSJ8PJ8GQB8UzhaqkwMkXwyAHntrfILQQqQatQ=\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"invocation\": \"DED6KQHcomjUhyLcmIcPwM1iWkbOlgDnidWZP+PLDWLQRk2rKLg5B/sY1YD1bqylF0zmtDCSIQKeMivAGJyOSXi4\",\n" +
                        "                \"verification\": \"EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw==\"\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoSendToAddress sendToAddress = deserialiseResponse(NeoSendToAddress.class);
        assertThat(sendToAddress.getSendToAddress(), is(notNullValue()));
        assertThat(sendToAddress.getSendToAddress().getHash(),
                is("0xabd78548399bbe684fac50b6a71d0ce3f689497d4e79cb26a2b4dfb211782c39"));
        assertThat(sendToAddress.getSendToAddress().getSize(), is(375L));
        assertThat(sendToAddress.getSendToAddress().getVersion(), is(0));
        assertThat(sendToAddress.getSendToAddress().getNonce(), is(1509730265L));
        assertThat(sendToAddress.getSendToAddress().getSender(), is("AK5AmzrrM3sw3kbCHXpHNeuK3kkjnneUrb"));
        assertThat(sendToAddress.getSendToAddress().getSysFee(), is("9007810"));
        assertThat(sendToAddress.getSendToAddress().getNetFee(), is("2375840"));
        assertThat(sendToAddress.getSendToAddress().getValidUntilBlock(), is(2106930L));
        assertThat(sendToAddress.getSendToAddress().getAttributes(), is(notNullValue()));

        assertThat(sendToAddress.getSendToAddress().getAttributes(), is(notNullValue()));
        assertThat(sendToAddress.getSendToAddress().getAttributes(), hasSize(1));
        assertThat(sendToAddress.getSendToAddress().getAttributes().get(0).getType(),
                is(TransactionAttributeType.SIGNER));
        assertThat(sendToAddress.getSendToAddress().getAttributes().get(0).getAsTransactionSigner().type,
                is(TransactionAttributeType.SIGNER));
        assertThat(sendToAddress.getSendToAddress().getAttributes().get(0).getAsTransactionSigner().getAccount(),
                is("0xf68f181731a47036a99f04dad90043a744edec0f"));
        assertThat(sendToAddress.getSendToAddress().getAttributes().get(0).getAsTransactionSigner().getScopes(),
                is(WitnessScope.CALLED_BY_ENTRY));
        assertThat(sendToAddress.getSendToAddress().getAttributes(),
                containsInAnyOrder(
                        new TransactionSigner(
                                "0xf68f181731a47036a99f04dad90043a744edec0f",
                                WitnessScope.CALLED_BY_ENTRY
                        )
                ));

        assertThat(sendToAddress.getSendToAddress().getScript(),
                is("GgwU5sEBNlSvET2KlovcpSyZSKgrlT0MFA/s7USnQwDZ2gSfqTZwpDEXGI/2E8AMCHRyYW5zZmVyDBSJdyDYzXb08Aq/o3wO3YicII/em0FifVtSOA=="));
        assertThat(sendToAddress.getSendToAddress().getWitnesses(), is(notNullValue()));
        assertThat(sendToAddress.getSendToAddress().getWitnesses(), hasSize(2));
        assertThat(sendToAddress.getSendToAddress().getWitnesses(),
                containsInAnyOrder(
                        new NeoWitness(
                                "DECstBmb75AW65NjA35fFlSxszuLRDUzd0nnbfyH8MlnSA02f6B1XlvItpZQBsAd7Pvqa7S+olPAKDO0qtq3ZtOB",
                                "DCED8ew8Hig+iA3m6cSJ8PJ8GQB8UzhaqkwMkXwyAHntrfILQQqQatQ="
                        ),
                        new NeoWitness(
                                "DED6KQHcomjUhyLcmIcPwM1iWkbOlgDnidWZP+PLDWLQRk2rKLg5B/sY1YD1bqylF0zmtDCSIQKeMivAGJyOSXi4",
                                "EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw=="
                        )
                ));
    }

    // RpcNep5Tracker

    @Test
    public void testGetNep5Transfers() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"sent\": [\n" +
                        "            {\n" +
                        "                \"timestamp\": 1554283931,\n" +
                        "                \"assethash\": \"1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3\",\n" +
                        "                \"transferaddress\": \"AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis\",\n" +
                        "                \"amount\": \"100000000000\",\n" +
                        "                \"blockindex\": 368082,\n" +
                        "                \"transfernotifyindex\": 0,\n" +
                        "                \"txhash\": \"240ab1369712ad2782b99a02a8f9fcaa41d1e96322017ae90d0449a3ba52a564\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"timestamp\": 1554880287,\n" +
                        "                \"assethash\": \"1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3\",\n" +
                        "                \"transferaddress\": \"AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis\",\n" +
                        "                \"amount\": \"100000000000\",\n" +
                        "                \"blockindex\": 397769,\n" +
                        "                \"transfernotifyindex\": 0,\n" +
                        "                \"txhash\": \"12fdf7ce8b2388d23ab223854cb29e5114d8288c878de23b7924880f82dfc834\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"received\": [\n" +
                        "            {\n" +
                        "                \"timestamp\": 1555651816,\n" +
                        "                \"assethash\": \"600c4f5200db36177e3e8a09e9f18e2fc7d12a0f\",\n" +
                        "                \"transferaddress\": \"AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis\",\n" +
                        "                \"amount\": \"1000000\",\n" +
                        "                \"blockindex\": 436036,\n" +
                        "                \"transfernotifyindex\": 0,\n" +
                        "                \"txhash\": \"df7683ece554ecfb85cf41492c5f143215dd43ef9ec61181a28f922da06aba58\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"address\": \"AbHgdBaWEnHkCiLtDZXjhvhaAK2cwFh5pF\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetNep5Transfers getNep5Transfers = deserialiseResponse(NeoGetNep5Transfers.class);
        assertThat(getNep5Transfers.getNep5Transfer().getSent(), is(notNullValue()));
        assertThat(getNep5Transfers.getNep5Transfer().getSent(), hasSize(2));
        assertThat(getNep5Transfers.getNep5Transfer().getSent(),
                containsInAnyOrder(
                        new NeoGetNep5Transfers.Nep5Transfer(
                                1554283931L,
                                "1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3",
                                "AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis",
                                "100000000000",
                                368082L,
                                0L,
                                "240ab1369712ad2782b99a02a8f9fcaa41d1e96322017ae90d0449a3ba52a564"
                        ),
                        new NeoGetNep5Transfers.Nep5Transfer(
                                1554880287L,
                                "1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3",
                                "AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis",
                                "100000000000",
                                397769L,
                                0L,
                                "12fdf7ce8b2388d23ab223854cb29e5114d8288c878de23b7924880f82dfc834"
                        )
                ));

        assertThat(getNep5Transfers.getNep5Transfer().getReceived(), is(notNullValue()));
        assertThat(getNep5Transfers.getNep5Transfer().getReceived(), hasSize(1));
        assertThat(getNep5Transfers.getNep5Transfer().getReceived(),
                hasItem(
                        new NeoGetNep5Transfers.Nep5Transfer(
                                1555651816L,
                                "600c4f5200db36177e3e8a09e9f18e2fc7d12a0f",
                                "AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis",
                                "1000000",
                                436036L,
                                0L,
                                "df7683ece554ecfb85cf41492c5f143215dd43ef9ec61181a28f922da06aba58"
                        )
                ));

        // First Sent Entry
        assertThat(getNep5Transfers.getNep5Transfer().getSent().get(0).getTimestamp(), is(1554283931L));
        assertThat(getNep5Transfers.getNep5Transfer().getSent().get(0).getAssetHash(),
                is("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3"));
        assertThat(getNep5Transfers.getNep5Transfer().getSent().get(0).getTransferAddress(),
                is("AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis"));

        // Second Sent Entry
        assertThat(getNep5Transfers.getNep5Transfer().getSent().get(1).getAmount(), is("100000000000"));
        assertThat(getNep5Transfers.getNep5Transfer().getSent().get(1).getBlockIndex(), is(397769L));

        // Received Entry
        assertThat(getNep5Transfers.getNep5Transfer().getReceived().get(0).getTransferNotifyIndex(), is(0L));
        assertThat(getNep5Transfers.getNep5Transfer().getReceived().get(0).getTxHash(),
                is("df7683ece554ecfb85cf41492c5f143215dd43ef9ec61181a28f922da06aba58"));
    }

    @Test
    public void testGetNep5Balances() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"balance\": [\n" +
                        "            {\n" +
                        "                \"assethash\": \"a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8\",\n" +
                        "                \"amount\": \"50000000000\",\n" +
                        "                \"lastupdatedblock\": 251604\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"assethash\": \"1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3\",\n" +
                        "                \"amount\": \"50000000000\",\n" +
                        "                \"lastupdatedblock\": 251600\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"address\": \"AY6eqWjsUFCzsVELG7yG72XDukKvC34p2w\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetNep5Balances getNep5Balances = deserialiseResponse(NeoGetNep5Balances.class);
        assertThat(getNep5Balances.getBalances().getBalances(), is(notNullValue()));
        assertThat(getNep5Balances.getBalances().getBalances(), hasSize(2));
        assertThat(getNep5Balances.getBalances().getBalances(),
                containsInAnyOrder(
                        new NeoGetNep5Balances.Nep5Balance(
                                "a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8",
                                "50000000000",
                                BigInteger.valueOf(251604L)
                        ),
                        new NeoGetNep5Balances.Nep5Balance(
                                "1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3",
                                "50000000000",
                                BigInteger.valueOf(251600L)
                        )
                ));

        // First Entry
        assertThat(getNep5Balances.getBalances().getBalances().get(0).getAssetHash(),
                is("a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8"));
        assertThat(getNep5Balances.getBalances().getBalances().get(0).getAmount(), is("50000000000"));

        // Second Entry
        assertThat(getNep5Balances.getBalances().getBalances().get(1).getLastUpdatedBlock(),
                is(BigInteger.valueOf(251600L)));

        assertThat(getNep5Balances.getBalances().getAddress(), is(notNullValue()));
        assertThat(getNep5Balances.getBalances().getAddress(), is("AY6eqWjsUFCzsVELG7yG72XDukKvC34p2w"));
    }

    // ApplicationLogs

    @Test
    public void testGetApplicationLog() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"txid\": \"0x01bcf2edbd27abb8d660b6a06113b84d02f635fed836ce46a38b4d67eae80109\",\n" +
                        "        \"trigger\": \"Application\",\n" +
                        "        \"vmstate\": \"HALT\",\n" +
                        "        \"gasConsumed\": \"9007810\",\n" +
                        "        \"stack\": [\n" +
                        "            {\n" +
                        "                \"type\": \"Integer\",\n" +
                        "                \"value\": \"1\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"notifications\": [\n" +
                        "            {\n" +
                        "                \"contract\": \"0x668e0c1f9d7b70a99dd9e06eadd4c784d641afbc\",\n" +
                        "                \"state\": {\n" +
                        "                    \"type\": \"Array\",\n" +
                        "                    \"value\": [\n" +
                        "                        {\n" +
                        "                            \"type\": \"ByteString\",\n" +
                        "                            \"value\": \"VHJhbnNmZXI=\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"type\": \"Any\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"type\": \"ByteString\",\n" +
                        "                            \"value\": \"lBNDI5IT+g52XxAnznQvSNt3mpY=\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"type\": \"Integer\",\n" +
                        "                            \"value\": \"600000000\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"contract\": \"0xde5f57d430d3dece511cf975a8d37848cb9e0525\",\n" +
                        "                \"state\": {\n" +
                        "                    \"type\": \"Array\",\n" +
                        "                    \"value\": [\n" +
                        "                        {\n" +
                        "                            \"type\": \"ByteString\",\n" +
                        "                            \"value\": \"VHJhbnNmZXI=\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"type\": \"ByteString\",\n" +
                        "                            \"value\": \"lBNDI5IT+g52XxAnznQvSNt3mpY=\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"type\": \"ByteString\",\n" +
                        "                            \"value\": \"5sEBNlSvET2KlovcpSyZSKgrlT0=\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"type\": \"Integer\",\n" +
                        "                            \"value\": \"100\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetApplicationLog getApplicationLog = deserialiseResponse(NeoGetApplicationLog.class);
        NeoApplicationLog neoAppLog = getApplicationLog.getApplicationLog();
        assertThat(neoAppLog, is(notNullValue()));
        assertThat(neoAppLog.getTrigger(), is("Application"));
        assertThat(neoAppLog.getState(), is("HALT"));
        assertThat(neoAppLog.getGasConsumed(), is("9007810"));

        assertThat(neoAppLog.getStack(), is(notNullValue()));
        assertThat(neoAppLog.getStack(), hasSize(1));
        assertThat(neoAppLog.getStack().get(0).getType(),
                is(StackItemType.INTEGER));
        assertThat(neoAppLog.getStack().get(0).asInteger().getValue(),
                is(BigInteger.valueOf(1)));

        assertThat(neoAppLog.getNotifications(), is(notNullValue()));
        assertThat(neoAppLog.getNotifications(), hasSize(2));

        // Notification 0
        NeoApplicationLog.Notification notification0 = neoAppLog.getNotifications().get(0);

        assertThat(notification0.getContract(), is("0x668e0c1f9d7b70a99dd9e06eadd4c784d641afbc"));
        assertThat(notification0.getState().getType(), is(StackItemType.ARRAY));

        ArrayStackItem notification0Array = notification0.getState().asArray();

        String eventName0 = notification0Array.get(0).asByteString().getAsString();
        Object from0 = notification0Array.get(1).asAny();
        String to0 = notification0Array.get(2).asByteString().getAsAddress();
        BigInteger amount0 = notification0Array.get(3).asInteger().getValue();

        assertThat(eventName0, is("Transfer"));
        assertNotNull(from0);
        assertThat(to0, is("AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm"));
        assertThat(amount0, is(BigInteger.valueOf(600000000)));

        // Notification 1
        NeoApplicationLog.Notification notification1 = neoAppLog.getNotifications().get(1);

        assertThat(notification1.getContract(), is("0xde5f57d430d3dece511cf975a8d37848cb9e0525"));
        assertThat(notification1.getState().getType(), is(StackItemType.ARRAY));

        ArrayStackItem notification1Array = notification1.getState().asArray();

        String eventName1 = notification1Array.get(0).asByteString().getAsString();
        Object from1 = notification1Array.get(1).asByteString().getAsAddress();
        String to1 = notification1Array.get(2).asByteString().getAsAddress();
        BigInteger amount1 = notification1Array.get(3).asInteger().getValue();

        assertThat(eventName1, is("Transfer"));
        assertThat(from1, is("AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm"));
        assertThat(to1, is("AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb"));
        assertThat(amount1, is(BigInteger.valueOf(100)));
    }
}
