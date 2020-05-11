package io.neow3j.protocol.core;

import static io.neow3j.utils.Numeric.prependHexPrefix;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.model.types.NodePluginType;
import io.neow3j.model.types.StackItemType;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.model.types.TransactionType;
import io.neow3j.protocol.ResponseTester;
import io.neow3j.protocol.core.methods.response.*;
import io.neow3j.transaction.Cosigner;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Numeric;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Core Protocol Response tests.
 */
public class ResponseTest extends ResponseTester {

    public static final ScriptHash NEO_HASH = ScriptHash.fromScript(
            new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_TOKENS_NEO).toArray());

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
                        "                \"sys_fee\": \"0\",\n" +
                        "                \"net_fee\": \"0\",\n" +
                        "                \"valid_until_block\": 2107425,\n" +
                        "                \"attributes\": [],\n" +
                        "                \"cosigners\": [\n" +
                        "                    {\n" +
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
                        "                \"sys_fee\": \"0\",\n" +
                        "                \"net_fee\": \"0\",\n" +
                        "                \"valid_until_block\": 2107425,\n" +
                        "                \"attributes\": [],\n" +
                        "                \"cosigners\": [\n" +
                        "                    {\n" +
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
                                Arrays.asList(),
                                Arrays.asList(
                                        new TransactionCosigner(
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
                                Arrays.asList(),
                                Arrays.asList(
                                        new TransactionCosigner(
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
    public void testGetBlockSysFee() {
        buildResponse(
                "{\n" +
                        "  \"jsonrpc\":\"2.0\",\n" +
                        "  \"id\":67,\n" +
                        "  \"result\": \"200\"\n" +
                        "}"
        );

        NeoGetBlockSysFee getBlockSysFee = deserialiseResponse(NeoGetBlockSysFee.class);
        assertThat(getBlockSysFee.getFee(), is(notNullValue()));

        assertThat(getBlockSysFee.getFee(), is("200"));
    }

//    @Test
//    public void testGetContractState() {
//        buildResponse(
//                "{\n"
//                        + "  \"id\":1,\n"
//                        + "  \"jsonrpc\":\"2.0\",\n"
//                        + "  \"result\": {\n"
//                        + "      \"version\": 0,\n"
//                        + "      \"hash\": \"0xdc675afc61a7c0f7b3d2682bf6e1d8ed865a0e5f\",\n"
//                        + "      \"script\": "
//                        +
//                        "\"5fc56b6c766b00527ac46c766b51527ac46107576f6f6c6f6e676c766b52527ac403574e476c766b53527ac4006c766b54527ac4210354ae498221046c666efebbaee9bd0eb4823469c98e748494a92a71f346b1a6616c766b55527ac46c766b00c3066465706c6f79876c766b56527ac46c766b56c36416006c766b55c36165f2026c766b57527ac462d8016c766b55c36165d801616c766b00c30b746f74616c537570706c79876c766b58527ac46c766b58c36440006168164e656f2e53746f726167652e476574436f6e7465787406737570706c79617c680f4e656f2e53746f726167652e4765746c766b57527ac46270016c766b00c3046e616d65876c766b59527ac46c766b59c36412006c766b52c36c766b57527ac46247016c766b00c30673796d626f6c876c766b5a527ac46c766b5ac36412006c766b53c36c766b57527ac4621c016c766b00c308646563696d616c73876c766b5b527ac46c766b5bc36412006c766b54c36c766b57527ac462ef006c766b00c30962616c616e63654f66876c766b5c527ac46c766b5cc36440006168164e656f2e53746f726167652e476574436f6e746578746c766b51c351c3617c680f4e656f2e53746f726167652e4765746c766b57527ac46293006c766b51c300c36168184e656f2e52756e74696d652e436865636b5769746e657373009c6c766b5d527ac46c766b5dc3640e00006c766b57527ac46255006c766b00c3087472616e73666572876c766b5e527ac46c766b5ec3642c006c766b51c300c36c766b51c351c36c766b51c352c36165d40361527265c9016c766b57527ac4620e00006c766b57527ac46203006c766b57c3616c756653c56b6c766b00527ac4616168164e656f2e53746f726167652e476574436f6e746578746c766b00c3617c680f4e656f2e53746f726167652e4765746165700351936c766b51527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b51c361651103615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e7465787406737570706c79617c680f4e656f2e53746f726167652e4765746165f40251936c766b52527ac46168164e656f2e53746f726167652e476574436f6e7465787406737570706c796c766b52c361659302615272680f4e656f2e53746f726167652e50757461616c756653c56b6c766b00527ac461516c766b51527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b51c361654002615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e7465787406737570706c796c766b51c361650202615272680f4e656f2e53746f726167652e50757461516c766b52527ac46203006c766b52c3616c756659c56b6c766b00527ac46c766b51527ac46c766b52527ac4616168164e656f2e53746f726167652e476574436f6e746578746c766b00c3617c680f4e656f2e53746f726167652e4765746c766b53527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b51c3617c680f4e656f2e53746f726167652e4765746c766b54527ac46c766b53c3616576016c766b52c3946c766b55527ac46c766b54c3616560016c766b52c3936c766b56527ac46c766b55c300a2640d006c766b52c300a2620400006c766b57527ac46c766b57c364ec00616168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b55c36165d800615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e746578746c766b51c36c766b56c361659c00615272680f4e656f2e53746f726167652e5075746155c57600135472616e73666572205375636365737366756cc476516c766b00c3c476526c766b51c3c476536c766b52c3c476546168184e656f2e426c6f636b636861696e2e476574486569676874c46168124e656f2e52756e74696d652e4e6f7469667961516c766b58527ac4620e00006c766b58527ac46203006c766b58c3616c756653c56b6c766b00527ac4616c766b00c36c766b51527ac46c766b51c36c766b52527ac46203006c766b52c3616c756653c56b6c766b00527ac461516c766b00c36a527a527ac46c766b51c36c766b52527ac46203006c766b52c3616c7566\",\n"
//                        + "      \"parameters\": ["
//                        + "           \"ByteArray\"\n"
//                        + "      ],\n"
//                        + "      \"returntype\": \"ByteArray\",\n"
//                        + "      \"name\": \"Contract Name\",\n"
//                        + "      \"code_version\": \"0.0.1\",\n"
//                        + "      \"author\": \"Author Name\",\n"
//                        + "      \"email\": \"blah@blah.com\",\n"
//                        + "      \"description\": \"GO NEO!!!\",\n"
//                        + "      \"properties\": {"
//                        + "           \"storage\": true,\n"
//                        + "           \"dynamic_invoke\": false\n"
//                        + "      }\n"
//                        + "   }\n"
//                        + "}"
//        );
//
//        NeoGetContractState getContractState = deserialiseResponse(NeoGetContractState.class);
//        assertThat(getContractState.getContractState(), is(notNullValue()));
//        assertThat(getContractState.getContractState().getVersion(), is(0));
//        assertThat(getContractState.getContractState().getHash(),
//                is("0xdc675afc61a7c0f7b3d2682bf6e1d8ed865a0e5f"));
//        assertThat(getContractState.getContractState().getScript(),
//                is("5fc56b6c766b00527ac46c766b51527ac46107576f6f6c6f6e676c766b52527ac403574e476c766b53527ac4006c766b54527ac4210354ae498221046c666efebbaee9bd0eb4823469c98e748494a92a71f346b1a6616c766b55527ac46c766b00c3066465706c6f79876c766b56527ac46c766b56c36416006c766b55c36165f2026c766b57527ac462d8016c766b55c36165d801616c766b00c30b746f74616c537570706c79876c766b58527ac46c766b58c36440006168164e656f2e53746f726167652e476574436f6e7465787406737570706c79617c680f4e656f2e53746f726167652e4765746c766b57527ac46270016c766b00c3046e616d65876c766b59527ac46c766b59c36412006c766b52c36c766b57527ac46247016c766b00c30673796d626f6c876c766b5a527ac46c766b5ac36412006c766b53c36c766b57527ac4621c016c766b00c308646563696d616c73876c766b5b527ac46c766b5bc36412006c766b54c36c766b57527ac462ef006c766b00c30962616c616e63654f66876c766b5c527ac46c766b5cc36440006168164e656f2e53746f726167652e476574436f6e746578746c766b51c351c3617c680f4e656f2e53746f726167652e4765746c766b57527ac46293006c766b51c300c36168184e656f2e52756e74696d652e436865636b5769746e657373009c6c766b5d527ac46c766b5dc3640e00006c766b57527ac46255006c766b00c3087472616e73666572876c766b5e527ac46c766b5ec3642c006c766b51c300c36c766b51c351c36c766b51c352c36165d40361527265c9016c766b57527ac4620e00006c766b57527ac46203006c766b57c3616c756653c56b6c766b00527ac4616168164e656f2e53746f726167652e476574436f6e746578746c766b00c3617c680f4e656f2e53746f726167652e4765746165700351936c766b51527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b51c361651103615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e7465787406737570706c79617c680f4e656f2e53746f726167652e4765746165f40251936c766b52527ac46168164e656f2e53746f726167652e476574436f6e7465787406737570706c796c766b52c361659302615272680f4e656f2e53746f726167652e50757461616c756653c56b6c766b00527ac461516c766b51527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b51c361654002615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e7465787406737570706c796c766b51c361650202615272680f4e656f2e53746f726167652e50757461516c766b52527ac46203006c766b52c3616c756659c56b6c766b00527ac46c766b51527ac46c766b52527ac4616168164e656f2e53746f726167652e476574436f6e746578746c766b00c3617c680f4e656f2e53746f726167652e4765746c766b53527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b51c3617c680f4e656f2e53746f726167652e4765746c766b54527ac46c766b53c3616576016c766b52c3946c766b55527ac46c766b54c3616560016c766b52c3936c766b56527ac46c766b55c300a2640d006c766b52c300a2620400006c766b57527ac46c766b57c364ec00616168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b55c36165d800615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e746578746c766b51c36c766b56c361659c00615272680f4e656f2e53746f726167652e5075746155c57600135472616e73666572205375636365737366756cc476516c766b00c3c476526c766b51c3c476536c766b52c3c476546168184e656f2e426c6f636b636861696e2e476574486569676874c46168124e656f2e52756e74696d652e4e6f7469667961516c766b58527ac4620e00006c766b58527ac46203006c766b58c3616c756653c56b6c766b00527ac4616c766b00c36c766b51527ac46c766b51c36c766b52527ac46203006c766b52c3616c756653c56b6c766b00527ac461516c766b00c36a527a527ac46c766b51c36c766b52527ac46203006c766b52c3616c7566"));
//        assertThat(getContractState.getContractState().getContractParameters(), hasSize(1));
//        assertThat(getContractState.getContractState().getContractParameters(),
//                hasItems(ContractParameterType.BYTE_ARRAY));
//        assertThat(getContractState.getContractState().getReturnContractType(),
//                is(ContractParameterType.BYTE_ARRAY));
//        assertThat(getContractState.getContractState().getName(), is("Contract Name"));
//        assertThat(getContractState.getContractState().getCodeVersion(), is("0.0.1"));
//        assertThat(getContractState.getContractState().getAuthor(), is("Author Name"));
//        assertThat(getContractState.getContractState().getEmail(), is("blah@blah.com"));
//        assertThat(getContractState.getContractState().getDescription(), is("GO NEO!!!"));
//        assertThat(getContractState.getContractState().getProperties(), is(notNullValue()));
//        assertThat(getContractState.getContractState().getProperties(),
//                is(new NeoGetContractState.ContractStateProperties(true, false)));
//    }

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
                        "        \"sys_fee\": \"9007810\",\n" +
                        "        \"net_fee\": \"1267450\",\n" +
                        "        \"valid_until_block\": 2103622,\n" +
                        "        \"attributes\": [],\n" +
                        "        \"cosigners\": [\n" +
                        "            {\n" +
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
                        "        \"vm_state\": \"HALT\"\n" +
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
        assertThat(getTransaction.getTransaction().getAttributes(), hasSize(0));

        assertThat(getTransaction.getTransaction().getCosigners(), is(notNullValue()));
        assertThat(getTransaction.getTransaction().getCosigners(), hasSize(1));
        assertThat(
                getTransaction.getTransaction().getCosigners(),
                containsInAnyOrder(
                        new TransactionCosigner(
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
                        "        \"tcp_port\": 10333,\n" +
                        "        \"ws_port\": 10334,\n" +
                        "        \"nonce\": 1845610272,\n" +
                        "        \"user_agent\": \"/Neo:3.0.0-preview2-00/\"\n" +
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
                        "        \"gas_consumed\": \"2007570\",\n" +
                        "        \"stack\": [\n" +
                        "            {\n" +
                        "                \"type\": \"ByteArray\",\n" +
                        "                \"value\": \"576f6f6c6f6e67\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"type\": \"Map\",\n" +
                        "                \"value\": [\n" +
                        "                    {\n" +
                        "                        \"key\": {\n" +
                        "                            \"type\": \"ByteArray\",\n" +
                        "                            \"value\": \"6964\"\n" +
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
        HashMap<StackItem, StackItem> stackMap = new HashMap<>();
        stackMap.put(new ByteArrayStackItem(Numeric.hexStringToByteArray("6964")),
                new IntegerStackItem(new BigInteger("1")));
        assertThat(invokeFunction.getInvocationResult().getStack(),
                containsInAnyOrder(
                        new ByteArrayStackItem(Numeric.hexStringToByteArray("576f6f6c6f6e67")),
                        new MapStackItem(stackMap)
                )
        );
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
                        "        \"gas_consumed\": \"2.489\",\n" +
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
                        "        \"gas_consumed\": \"2007390\",\n" +
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
                        "        \"gas_consumed\": \"0.161\",\n" +
                        "        \"stack\": [\n" +
                        "            {\n" +
                        "                \"type\": \"ByteArray\",\n" +
                        "                \"value\": \"4e45503520474153\"\n" +
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
                        new ByteArrayStackItem(Numeric.hexStringToByteArray("4e45503520474153"))
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
                        "                \"IStoragePlugin\"\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"RocksDBStore\",\n" +
                        "            \"version\": \"3.0.0.0\",\n" +
                        "            \"interfaces\": [\n" +
                        "                \"IStoragePlugin\"\n" +
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
        assertThat(plugin.getInterfaces(), containsInAnyOrder("IStoragePlugin"));

        plugin = listPlugins.getPlugins().get(2);
        assertThat(NodePluginType.valueOfName(plugin.getName()),
                is(NodePluginType.ROCKS_DB_STORE));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(1));
        assertThat(plugin.getInterfaces(), containsInAnyOrder("IStoragePlugin"));

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
                        "        \"balance\": \"200\",\n" +
                        "        \"confirmed\": \"100\"" +
                        "    }\n" +
                        "}"
        );

        NeoGetBalance getBalance = deserialiseResponse(NeoGetBalance.class);
        assertThat(getBalance.getBalance(), is(notNullValue()));
        assertThat(getBalance.getBalance().getBalance(), is("200"));
        assertThat(getBalance.getBalance().getConfirmed(), is("100"));
    }

    @Test
    public void testGetBalance_UpperCase() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"Balance\": \"199999990.0\",\n"
                        + "      \"Confirmed\": \"99999990.0\"\n"
                        + "  }\n"
                        + "}"
        );

        NeoGetBalance getBalance = deserialiseResponse(NeoGetBalance.class);
        assertThat(getBalance.getBalance(), is(notNullValue()));
        assertThat(getBalance.getBalance().getBalance(), is("199999990.0"));
        assertThat(getBalance.getBalance().getConfirmed(), is("99999990.0"));
    }

    @Test
    public void testGetBalance_nullable() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"balance\": \"199999990.0\"\n"
                        + "  }\n"
                        + "}"
        );

        NeoGetBalance getBalance = deserialiseResponse(NeoGetBalance.class);
        assertThat(getBalance.getBalance(), is(notNullValue()));
        assertThat(getBalance.getBalance().getBalance(), is("199999990.0"));
        assertThat(getBalance.getBalance().getConfirmed(), is(nullValue()));
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
                        "        \"sys_fee\": \"9007810\",\n" +
                        "        \"net_fee\": \"1266450\",\n" +
                        "        \"valid_until_block\": 2106392,\n" +
                        "        \"attributes\": [],\n" +
                        "        \"cosigners\": [\n" +
                        "            {\n" +
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
        assertThat(sendFrom.getSendFrom().getAttributes(), hasSize(0));

        assertThat(sendFrom.getSendFrom().getCosigners(), is(notNullValue()));
        assertThat(sendFrom.getSendFrom().getCosigners(), hasSize(1));
        assertThat(sendFrom.getSendFrom().getCosigners(),
                containsInAnyOrder(
                        new TransactionCosigner("0xf68f181731a47036a99f04dad90043a744edec0f",
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
                        "        \"sys_fee\": \"18015620\",\n" +
                        "        \"net_fee\": \"1352450\",\n" +
                        "        \"valid_until_block\": 2106840,\n" +
                        "        \"attributes\": [],\n" + // TODO: 11.05.20 Michael: check for attributes/cosigners/witnesses in neo documentation and make second test - generally check for potential variations to test
                        "        \"cosigners\": [\n" +
                        "            {\n" +
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
        assertThat(sendMany.getSendMany().getAttributes(), hasSize(0));

        assertThat(sendMany.getSendMany().getCosigners(), is(notNullValue()));
        assertThat(sendMany.getSendMany().getCosigners(), hasSize(1));
        assertThat(sendMany.getSendMany().getCosigners(),
                containsInAnyOrder(
                        new TransactionCosigner("0xf68f181731a47036a99f04dad90043a744edec0f",
                                WitnessScope.CALLED_BY_ENTRY)
                ));
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
                        "        \"sys_fee\": \"9007810\",\n" +
                        "        \"net_fee\": \"2375840\",\n" +
                        "        \"valid_until_block\": 2106930,\n" +
                        "        \"attributes\": [],\n" +
                        "        \"cosigners\": [\n" +
                        "            {\n" +
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
        assertThat(sendToAddress.getSendToAddress().getAttributes(), hasSize(0));

        assertThat(sendToAddress.getSendToAddress().getCosigners(), is(notNullValue()));
        assertThat(sendToAddress.getSendToAddress().getCosigners(), hasSize(1));
        assertThat(sendToAddress.getSendToAddress().getCosigners(),
                containsInAnyOrder(
                        new TransactionCosigner(
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

    }

    @Test
    public void testGetNep5Balances() {

    }
    //    @Test
//    public void testGetNep5Balances() {
//        buildResponse(
//                "{\n"
//                        + "  \"id\":1,\n"
//                        + "  \"jsonrpc\":\"2.0\",\n"
//                        + "  \"result\": {\n"
//                        + "      \"balance\": [\n"
//                        + "           {\n"
//                        + "               \"asset_hash\": "
//                        + "\"a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8\",\n"
//                        + "               \"amount\": \"50000000000\",\n"
//                        + "               \"last_updated_block\": 251604\n"
//                        + "           },\n"
//                        + "           {\n"
//                        + "               \"asset_hash\": "
//                        + "\"1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3\",\n"
//                        + "               \"amount\": \"50000000000\",\n"
//                        + "               \"last_updated_block\": 251600\n"
//                        + "           }\n"
//                        + "      ],\n"
//                        + "      \"address\": \"AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y\"\n"
//                        + "   }\n"
//                        + "}"
//        );
//
//        NeoGetNep5Balances getNep5Balances = deserialiseResponse(NeoGetNep5Balances.class);
//        assertThat(getNep5Balances.getResult(), is(notNullValue()));
//        assertThat(
//                getNep5Balances.getResult().getAddress(),
//                is("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
//        );
//        assertThat(
//                getNep5Balances.getResult().getBalances(),
//                hasSize(2)
//        );
//        // First entry:
//        assertThat(
//                getNep5Balances.getResult().getBalances().get(0).getAssetHash(),
//                is("a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8")
//        );
//        assertThat(
//                getNep5Balances.getResult().getBalances().get(0).getAmount(),
//                is("50000000000")
//        );
//        assertThat(
//                getNep5Balances.getResult().getBalances().get(0).getLastUpdatedBlock(),
//                is(new BigInteger("251604"))
//        );
//        // Second entry:
//        assertThat(
//                getNep5Balances.getResult().getBalances().get(1).getAssetHash(),
//                is("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3")
//        );
//        assertThat(
//                getNep5Balances.getResult().getBalances().get(1).getAmount(),
//                is("50000000000")
//        );
//        assertThat(
//                getNep5Balances.getResult().getBalances().get(1).getLastUpdatedBlock(),
//                is(new BigInteger("251600"))
//        );
//    }

    // ApplicationLogs

    @Test
    public void testGetApplicationLog() {
        // TODO: 11.05.20 Michael: no method found
//        {
//            "jsonrpc": "2.0",
//                "id": 1,
//                "error": {
//            "code": -32601,
//                    "message": "Method not found"
//        }
    }
//    @Test
//    public void testApplicationLog() {
//        buildResponse(
//                "{\n" +
//                        "  \"jsonrpc\": \"2.0\",\n" +
//                        "  \"id\": 1,\n" +
//                        "  \"result\": {\n" +
//                        "    \"txid\": "
//                        + "\"0x420d1eb458c707d698c6d2ba0f91327918ddb3b7bae2944df070f3f4e579078b"
//                        + "\",\n"
//                        +
//                        "    \"executions\": [\n" +
//                        "      {\n" +
//                        "        \"trigger\": \"Application\",\n" +
//                        "        \"contract\": \"0x857477dd9457d09aff11fc4a791a247a42dbb17f\",\n" +
//                        "        \"vmstate\": \"HALT, BREAK\",\n" +
//                        "        \"gas_consumed\": \"0.173\",\n" +
//                        "        \"stack\": [\n" +
//                        "          {\n" +
//                        "            \"type\": \"ByteArray\",\n" +
//                        "            \"value\": \"b100\"\n" +
//                        "          }\n" +
//                        "        ],\n" +
//                        "        \"notifications\": [\n" +
//                        "          {\n" +
//                        "            \"contract\": \"0x43fa0777cf984faea46b954ec640a266bcbc3319"
//                        + "\",\n"
//                        +
//                        "            \"state\": {\n" +
//                        "              \"type\": \"Array\",\n" +
//                        "              \"value\": [\n" +
//                        "                {\n" +
//                        "                  \"type\": \"ByteArray\",\n" +
//                        "                  \"value\": \"72656164\"\n" +
//                        "                },\n" +
//                        "                {\n" +
//                        "                  \"type\": \"ByteArray\",\n" +
//                        "                  \"value\": "
//                        + "\"10d46912932d6ebcd1d3c4a27a1a8ea77e68ac95\"\n"
//                        +
//                        "                },\n" +
//                        "                {\n" +
//                        "                  \"type\": \"ByteArray\",\n" +
//                        "                  \"value\": \"b100\"\n" +
//                        "                }\n" +
//                        "              ]\n" +
//                        "            }\n" +
//                        "          },\n" +
//                        "          {\n" +
//                        "             \"contract\": \"0xef182f4977544adb207507b0c8c6c3ec1749c7df"
//                        + "\",\n"
//                        +
//                        "             \"state\": {\n" +
//                        "               \"type\": \"Map\",\n" +
//                        "               \"value\": [\n" +
//                        "                 {\n" +
//                        "                   \"key\": {\n" +
//                        "                     \"type\": \"ByteArray\",\n" +
//                        "                     \"value\": \"746573745f6b65795f61\"\n" +
//                        "                   },\n" +
//                        "                   \"value\": {\n" +
//                        "                     \"type\": \"ByteArray\",\n" +
//                        "                     \"value\": \"54657374206d657373616765\"\n" +
//                        "                   }\n" +
//                        "                 },\n" +
//                        "                 {\n" +
//                        "                   \"key\": {\n" +
//                        "                     \"type\": \"ByteArray\",\n" +
//                        "                     \"value\": \"746573745f6b65795f62\"\n" +
//                        "                   },\n" +
//                        "                   \"value\": {\n" +
//                        "                     \"type\": \"Integer\",\n" +
//                        "                     \"value\": \"12345\"\n" +
//                        "                   }\n" +
//                        "                 }\n" +
//                        "               ]\n" +
//                        "             }\n" +
//                        "           }" +
//                        "        ]\n" +
//                        "      }\n" +
//                        "    ]\n" +
//                        "  }\n" +
//                        "}"
//        );
//
//        NeoGetApplicationLog applicationLog = deserialiseResponse(NeoGetApplicationLog.class);
//        assertThat(applicationLog.getApplicationLog().getTransactionId(),
//                is("0x420d1eb458c707d698c6d2ba0f91327918ddb3b7bae2944df070f3f4e579078b"));
//        assertThat(applicationLog.getApplicationLog().getExecutions(), hasSize(1));
//
//        NeoApplicationLog.Execution execution = applicationLog.getApplicationLog().getExecutions()
//                .get(0);
//
//        assertThat(execution.getTrigger(), is("Application"));
//        assertThat(execution.getContract(), is("0x857477dd9457d09aff11fc4a791a247a42dbb17f"));
//        assertThat(execution.getState(), is("HALT, BREAK"));
//        assertThat(execution.getGasConsumed(), is("0.173"));
//        assertThat(execution.getStack(), hasSize(1));
//        assertThat(execution.getNotifications(), hasSize(2));
//
//        List<NeoApplicationLog.Notification> notifications = execution.getNotifications();
//
//        assertThat(notifications.get(0).getContract(),
//                is("0x43fa0777cf984faea46b954ec640a266bcbc3319"));
//        assertThat(notifications.get(0).getState().getType(), is(StackItemType.ARRAY));
//        assertTrue(notifications.get(0).getState() instanceof ArrayStackItem);
//
//        ArrayStackItem array = notifications.get(0).getState().asArray();
//
//        String eventName = array.get(0).asByteArray().getAsString();
//        String address = array.get(1).asByteArray().getAsAddress();
//        BigInteger amount = array.get(2).asByteArray().getAsNumber();
//
//        assertThat(eventName, is("read"));
//        assertThat(address, is("AHJrv6y6L6k9PfJvY7vtX3XTAmEprsd3Xn"));
//        assertThat(amount, is(BigInteger.valueOf(177)));
//
//        assertThat(notifications.get(1).getContract(),
//                is("0xef182f4977544adb207507b0c8c6c3ec1749c7df"));
//        assertTrue(notifications.get(1).getState() instanceof MapStackItem);
//        assertThat(notifications.get(1).getState().getType(), is(StackItemType.MAP));
//        MapStackItem map = notifications.get(1).getState().asMap();
//        assertThat(map.size(), is(2));
//
//        String textValue = map.get("test_key_a").asByteArray().getAsString();
//        BigInteger intValue = map.get("test_key_b").asInteger().getValue();
//
//        assertThat(textValue, is("Test message"));
//        assertThat(intValue, is(BigInteger.valueOf(12345)));
//    }































//    @Test
//    public void testGetWalletHeight() {
//        buildResponse(
//                "{\n"
//                        + "  \"id\":67,\n"
//                        + "  \"jsonrpc\":\"2.0\",\n"
//                        + "  \"result\": 1927636\n"
//                        + "}"
//        );
//
//        NeoGetWalletHeight getWalletHeight = deserialiseResponse(NeoGetWalletHeight.class);
//        assertThat(getWalletHeight.getHeight(), is(notNullValue()));
//        assertThat(getWalletHeight.getHeight(), is(BigInteger.valueOf(1927636)));
//    }
//
//
//
//
//    @Test
//    public void testGetTransaction() {
//        buildResponse(
//                "{\n"
//                        + "  \"id\":1,\n"
//                        + "  \"jsonrpc\":\"2.0\",\n"
//                        + "  \"result\": {\n"
//                        + "      \"txid\": "
//                        + "\"0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6"
//                        + "\",\n"
//                        + "      \"size\": 283,\n"
//                        + "      \"type\": \"ContractTransaction\",\n"
//                        + "      \"version\": 0,\n"
//                        + "      \"attributes\": ["
//                        + "           {"
//                        + "               \"usage\": 32,\n"
//                        + "               \"data\": \"23ba2703c53263e8d6e522dc32203339dcd8eee9\"\n"
//                        + "           }"
//                        + "      ],\n"
//                        + "      \"vin\": [\n"
//                        + "           {\n"
//                        + "               \"txid\": "
//                        + "\"0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff"
//                        + "\",\n"
//                        + "               \"vout\": 0\n"
//                        + "           }\n"
//                        + "      ],\n"
//                        + "      \"vout\": [\n"
//                        + "           {\n"
//                        + "               \"n\": 0,\n"
//                        + "               \"asset\": \"0x9bde8f209c88dd0e7ca3bf0af0f476cdd8207789"
//                        + "\",\n"
//                        + "               \"value\": \"10\",\n"
//                        + "               \"address\": \"AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ\"\n"
//                        + "           },\n"
//                        + "           {\n"
//                        + "               \"n\": 1,\n"
//                        + "               \"asset\": \"0x9bde8f209c88dd0e7ca3bf0af0f476cdd8207789"
//                        + "\",\n"
//                        + "               \"value\": \"99999990\",\n"
//                        + "               \"address\": \"AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y\"\n"
//                        + "           }\n"
//                        + "      ],\n"
//                        + "      \"sys_fee\": \"0\",\n"
//                        + "      \"net_fee\": \"0\",\n"
//                        + "      \"scripts\": [\n"
//                        + "           {\n"
//                        + "               \"invocation\": "
//                        +
//                        "\"405797c43807e098a78014ae6c0e0f7b3c2565791dedc6753b9e821a0c3a565bdb5eb117ff5218be932b6f616f3d195c1417128b75e366589a83845a1a982c29d0\",\n"
//                        + "               \"verification\": "
//                        +
//                        "\"21031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac"
//                        + "\"\n"
//                        + "           }\n"
//                        + "      ],\n"
//                        + "      \"blockhash\": "
//                        + "\"0x0c7ec8f8f952d7206b8ef82b6997a5f9ce44a88b356d3ca42a2a29457c608387"
//                        + "\",\n"
//                        + "      \"confirmations\": 200,\n"
//                        + "      \"blocktime\": 1548704299\n"
//                        + "   }\n"
//                        + "}"
//        );
//
//        NeoGetTransaction getTransaction = deserialiseResponse(NeoGetTransaction.class);
//        assertThat(getTransaction.getTransaction(), is(notNullValue()));
//        assertThat(
//                getTransaction.getTransaction().getTransactionId(),
//                is("0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6")
//        );
//        assertThat(
//                getTransaction.getTransaction().getSize(),
//                is(283L)
//        );
//        assertThat(
//                getTransaction.getTransaction().getType(),
//                is(TransactionType.CONTRACT_TRANSACTION)
//        );
//        assertThat(
//                getTransaction.getTransaction().getVersion(),
//                is(0)
//        );
//        assertThat(
//                getTransaction.getTransaction().getAttributes(),
//                hasItem(
//                        new TransactionAttribute(TransactionAttributeUsageType.SCRIPT,
//                                "23ba2703c53263e8d6e522dc32203339dcd8eee9")
//                )
//        );
//        assertThat(
//                getTransaction.getTransaction().getOutputs(),
//                hasItems(
//                        new TransactionOutput(0, prependHexPrefix(NEO_HASH.toString()), "10",
//                                "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ"),
//                        new TransactionOutput(1, prependHexPrefix(NEO_HASH.toString()), "99999990",
//                                "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
//                )
//        );
//        assertThat(
//                getTransaction.getTransaction().getInputs(),
//                hasItem(
//                        new TransactionInput(
//                                "0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff",
//                                0)
//                )
//        );
//        assertThat(
//                getTransaction.getTransaction().getSysFee(),
//                is("0")
//        );
//        assertThat(
//                getTransaction.getTransaction().getNetFee(),
//                is("0")
//        );
//        assertThat(
//                getTransaction.getTransaction().getScripts(),
//                hasItems(
//                        new NeoWitness(
//                                "405797c43807e098a78014ae6c0e0f7b3c2565791dedc6753b9e821a0c3a565bdb5eb117ff5218be932b6f616f3d195c1417128b75e366589a83845a1a982c29d0",
//                                "21031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac")
//                )
//        );
//        assertThat(
//                getTransaction.getTransaction().getBlockHash(),
//                is("0x0c7ec8f8f952d7206b8ef82b6997a5f9ce44a88b356d3ca42a2a29457c608387")
//        );
//        assertThat(
//                getTransaction.getTransaction().getConfirmations(),
//                is(200L)
//        );
//        assertThat(
//                getTransaction.getTransaction().getBlockTime(),
//                is(1548704299L)
//        );
//    }
//
}
