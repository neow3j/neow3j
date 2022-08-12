package io.neow3j.protocol.core;

import io.neow3j.protocol.ResponseTester;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.response.ContractManifest.ContractPermission;
import io.neow3j.protocol.core.response.ContractNef;
import io.neow3j.protocol.core.response.ContractState;
import io.neow3j.protocol.core.response.ContractStorageEntry;
import io.neow3j.protocol.core.response.Diagnostics;
import io.neow3j.protocol.core.response.ExpressContractState;
import io.neow3j.protocol.core.response.HighPriorityAttribute;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NativeContractState;
import io.neow3j.protocol.core.response.NeoAddress;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoBlockCount;
import io.neow3j.protocol.core.response.NeoBlockHash;
import io.neow3j.protocol.core.response.NeoBlockHeaderCount;
import io.neow3j.protocol.core.response.NeoCloseWallet;
import io.neow3j.protocol.core.response.NeoConnectionCount;
import io.neow3j.protocol.core.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.response.NeoExpressCreateCheckpoint;
import io.neow3j.protocol.core.response.NeoExpressCreateOracleResponseTx;
import io.neow3j.protocol.core.response.NeoExpressGetContractStorage;
import io.neow3j.protocol.core.response.NeoExpressGetNep17Contracts;
import io.neow3j.protocol.core.response.NeoExpressGetPopulatedBlocks;
import io.neow3j.protocol.core.response.NeoExpressListContracts;
import io.neow3j.protocol.core.response.NeoExpressListOracleRequests;
import io.neow3j.protocol.core.response.NeoExpressShutdown;
import io.neow3j.protocol.core.response.NeoFindStates;
import io.neow3j.protocol.core.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.protocol.core.response.NeoGetMemPool;
import io.neow3j.protocol.core.response.NeoGetNativeContracts;
import io.neow3j.protocol.core.response.NeoGetNep11Balances;
import io.neow3j.protocol.core.response.NeoGetNep11Balances.Nep11Balance.Nep11Token;
import io.neow3j.protocol.core.response.NeoGetNep11Properties;
import io.neow3j.protocol.core.response.NeoGetNep11Transfers;
import io.neow3j.protocol.core.response.NeoGetNep17Balances;
import io.neow3j.protocol.core.response.NeoGetNep17Transfers;
import io.neow3j.protocol.core.response.NeoGetNewAddress;
import io.neow3j.protocol.core.response.NeoGetNextBlockValidators;
import io.neow3j.protocol.core.response.NeoGetPeers;
import io.neow3j.protocol.core.response.NeoGetProof;
import io.neow3j.protocol.core.response.NeoGetRawBlock;
import io.neow3j.protocol.core.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.response.NeoGetState;
import io.neow3j.protocol.core.response.NeoGetStateHeight;
import io.neow3j.protocol.core.response.NeoGetStateRoot;
import io.neow3j.protocol.core.response.NeoGetStorage;
import io.neow3j.protocol.core.response.NeoGetTransaction;
import io.neow3j.protocol.core.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.response.NeoGetUnclaimedGas;
import io.neow3j.protocol.core.response.NeoGetVersion;
import io.neow3j.protocol.core.response.NeoGetWalletBalance;
import io.neow3j.protocol.core.response.NeoGetWalletUnclaimedGas;
import io.neow3j.protocol.core.response.NeoImportPrivKey;
import io.neow3j.protocol.core.response.NeoInvokeContractVerify;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoInvokeScript;
import io.neow3j.protocol.core.response.NeoListAddress;
import io.neow3j.protocol.core.response.NeoListPlugins;
import io.neow3j.protocol.core.response.NeoOpenWallet;
import io.neow3j.protocol.core.response.NeoSendFrom;
import io.neow3j.protocol.core.response.NeoSendMany;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.response.NeoSendToAddress;
import io.neow3j.protocol.core.response.NeoSubmitBlock;
import io.neow3j.protocol.core.response.NeoTerminateSession;
import io.neow3j.protocol.core.response.NeoTraverseIterator;
import io.neow3j.protocol.core.response.NeoValidateAddress;
import io.neow3j.protocol.core.response.NeoVerifyProof;
import io.neow3j.protocol.core.response.NeoWitness;
import io.neow3j.protocol.core.response.Nep17Contract;
import io.neow3j.protocol.core.response.Notification;
import io.neow3j.protocol.core.response.OracleRequest;
import io.neow3j.protocol.core.response.OracleResponse;
import io.neow3j.protocol.core.response.OracleResponseAttribute;
import io.neow3j.protocol.core.response.OracleResponseCode;
import io.neow3j.protocol.core.response.PopulatedBlocks;
import io.neow3j.protocol.core.response.Transaction;
import io.neow3j.protocol.core.response.TransactionAttribute;
import io.neow3j.protocol.core.response.TransactionSigner;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.protocol.core.stackitem.InteropInterfaceStackItem;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.core.witnessrule.WitnessRule;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.transaction.witnessrule.WitnessConditionType;
import io.neow3j.transaction.witnessrule.WitnessAction;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.types.NodePluginType;
import io.neow3j.types.StackItemType;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core Protocol Response tests.
 */
public class ResponseTest extends ResponseTester {

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
                is(new Hash256("0x3d1e051247f246f60dd2ba4f90f799578b5d394157b1f2b012c016b29536b899")));
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
                is(new Hash256("0x147ad6a26f1d5a9bb2bea3f0b2ca9fab3824873beaf8887e87d08c8fd98a81b3")));
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
                        "                \"signers\": [" +
                        "                    {" +
                        "                        \"account\": \"0xf68f181731a47036a99f04dad90043a744edec0f\",\n" +
                        "                        \"scopes\": \"CalledByEntry\"\n" +
                        "                    }" +
                        "                ]," +
                        "                \"attributes\": [],\n" +
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
                        "                \"signers\": [" +
                        "                    {" +
                        "                        \"account\": \"0xf68f181731a47036a99f04dad90043a744edec0f\",\n" +
                        "                        \"scopes\": \"CalledByEntry\"\n" +
                        "                    }" +
                        "                ]," +
                        "                \"attributes\": [],\n" +
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
                is(new Hash256("0x1de7e5eaab0f74ac38f5191c038e009d3c93ef5c392d1d66fa95ab164ba308b8")));
        assertThat(getBlock.getBlock().getSize(), is(1217L));
        assertThat(getBlock.getBlock().getVersion(), is(0));
        assertThat(getBlock.getBlock().getPrevBlockHash(),
                is(new Hash256("0x045cabde4ecbd50f5e4e1b141eaf0842c1f5f56517324c8dcab8ccac924e3a39")));
        assertThat(getBlock.getBlock().getMerkleRootHash(),
                is(new Hash256("0x6afa63201b88b55ad2213e5a69a1ad5f0db650bc178fc2bedd2fb301c1278bf7")));
        assertThat(getBlock.getBlock().getTime(), is(1539968858L));
        assertThat(getBlock.getBlock().getIndex(), is(1914006L));
        assertThat(getBlock.getBlock().getNextConsensus(), is("AWZo4qAxhT8fwKL93QATSjCYCgHmCY1XLB"));

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

        assertThat(getBlock.getBlock().getTransactions(), hasSize(2));

        assertThat(
                getBlock.getBlock().getTransactions(),
                containsInAnyOrder(
                        new Transaction(
                                new Hash256("0x46eca609a9a8c8340ee56b174b04bc9c9f37c89771c3a8998dc043f5a74ad510"),
                                267L,
                                0,
                                565086327L,
                                "AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4",
                                "0",
                                "0",
                                2107425L,
                                asList(new TransactionSigner(
                                        new Hash160("0xf68f181731a47036a99f04dad90043a744edec0f"),
                                        asList(WitnessScope.CALLED_BY_ENTRY))
                                ),
                                emptyList(),
                                "AGQMFObBATZUrxE9ipaL3KUsmUioK5U9DBQP7O1Ep0MA2doEn6k2cKQxFxiP9hPADAh0cmFuc2ZlcgwUiXcg2M129PAKv6N8Dt2InCCP3ptBYn1bUjg",
                                asList(new NeoWitness(
                                                "DEBR7EQOb1NUjat1wrINzBNKOQtXoUmRVZU8h5c8K5CLMCUVcGkFVqAAGUJDh3mVcz6sTgXvmMuujWYrBveeM4q+",
                                                "EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw=="
                                        )
                                )
                        ),
                        new Transaction(
                                new Hash256("0x46eca609a9a8c8340ee56b174b04bc9c9f37c89771c3a8998dc043f5a74ad510"),
                                267L,
                                0,
                                565086327L,
                                "AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4",
                                "0",
                                "0",
                                2107425L,
                                asList(new TransactionSigner(
                                        new Hash160("0xf68f181731a47036a99f04dad90043a744edec0f"),
                                        asList(WitnessScope.CALLED_BY_ENTRY))
                                ),
                                emptyList(),
                                "AGQMFObBATZUrxE9ipaL3KUsmUioK5U9DBQP7O1Ep0MA2doEn6k2cKQxFxiP9hPADAh0cmFuc2ZlcgwUiXcg2M129PAKv6N8Dt2InCCP3ptBYn1bUjg",
                                asList(
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
                is(new Hash256("0x4a97ca89199627f877b6bffe865b8327be84b368d62572ef20953829c3501643")));
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
                is(new Hash256("0x1de7e5eaab0f74ac38f5191c038e009d3c93ef5c392d1d66fa95ab164ba308b8")));
        assertThat(getBlock.getBlock().getSize(), is(1217L));
        assertThat(getBlock.getBlock().getVersion(), is(0));
        assertThat(getBlock.getBlock().getPrevBlockHash(),
                is(new Hash256("0x045cabde4ecbd50f5e4e1b141eaf0842c1f5f56517324c8dcab8ccac924e3a39")));
        assertThat(getBlock.getBlock().getMerkleRootHash(),
                is(new Hash256("0x6afa63201b88b55ad2213e5a69a1ad5f0db650bc178fc2bedd2fb301c1278bf7")));
        assertThat(getBlock.getBlock().getTime(), is(1539968858L));
        assertThat(getBlock.getBlock().getIndex(), is(1914006L));
        assertThat(getBlock.getBlock().getNextConsensus(), is("AWZo4qAxhT8fwKL93QATSjCYCgHmCY1XLB"));

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

        assertThat(getBlock.getBlock().getTransactions(), is(nullValue()));

        assertThat(getBlock.getBlock().getConfirmations(), is(7878));
        assertThat(getBlock.getBlock().getNextBlockHash(),
                is(new Hash256("0x4a97ca89199627f877b6bffe865b8327be84b368d62572ef20953829c3501643")));
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
    public void testGetBlockHeaderCount() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": 543\n" +
                        "}"
        );

        NeoBlockHeaderCount neoBlockHeaderCount = deserialiseResponse(NeoBlockHeaderCount.class);
        assertThat(neoBlockHeaderCount.getCount(), is(notNullValue()));

        assertThat(neoBlockHeaderCount.getCount(), is(BigInteger.valueOf(543)));
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
        assertThat(neoBlockCount.getBlockCount(), is(notNullValue()));

        assertThat(neoBlockCount.getBlockCount(), is(BigInteger.valueOf(1234)));
    }

    @Test
    public void testGetNativeContracts() {
        buildResponse("{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": [\n" +
                        "        {\n" +
                        "            \"id\": -6,\n" +
                        "            \"hash\": \"0xd2a4cff31913016155e38e474a2c06d08be276cf\",\n" +
                        "            \"nef\": {\n" +
                        "                \"magic\": 860243278,\n" +
                        "                \"compiler\": \"neo-core-v3.0\",\n" +
                        "                \"source\": \"variable-size-source-gastoken\",\n" +
                        "                \"tokens\": [],\n" +
                        "                \"script\": \"EEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0A=\",\n" +
                        "                \"checksum\": 2663858513\n" +
                        "            },\n" +
                        "            \"manifest\": {\n" +
                        "                \"name\": \"GasToken\",\n" +
                        "                \"groups\": [],\n" +
                        "                \"supportedstandards\": [\n" +
                        "                    \"NEP-17\"\n" +
                        "                ],\n" +
                        "                \"abi\": {\n" +
                        "                    \"methods\": [\n" +
                        "                        {\n" +
                        "                            \"name\": \"balanceOf\",\n" +
                        "                            \"parameters\": [\n" +
                        "                                {\n" +
                        "                                    \"name\": \"account\",\n" +
                        "                                    \"type\": \"Hash160\"\n" +
                        "                                }\n" +
                        "                            ],\n" +
                        "                            \"returntype\": \"Integer\",\n" +
                        "                            \"offset\": 0,\n" +
                        "                            \"safe\": true\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"name\": \"decimals\",\n" +
                        "                            \"parameters\": [],\n" +
                        "                            \"returntype\": \"Integer\",\n" +
                        "                            \"offset\": 7,\n" +
                        "                            \"safe\": true\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"name\": \"symbol\",\n" +
                        "                            \"parameters\": [],\n" +
                        "                            \"returntype\": \"String\",\n" +
                        "                            \"offset\": 14,\n" +
                        "                            \"safe\": true\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"name\": \"totalSupply\",\n" +
                        "                            \"parameters\": [],\n" +
                        "                            \"returntype\": \"Integer\",\n" +
                        "                            \"offset\": 21,\n" +
                        "                            \"safe\": true\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"name\": \"transfer\",\n" +
                        "                            \"parameters\": [\n" +
                        "                                {\n" +
                        "                                    \"name\": \"from\",\n" +
                        "                                    \"type\": \"Hash160\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"to\",\n" +
                        "                                    \"type\": \"Hash160\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"amount\",\n" +
                        "                                    \"type\": \"Integer\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"data\",\n" +
                        "                                    \"type\": \"Any\"\n" +
                        "                                }\n" +
                        "                            ],\n" +
                        "                            \"returntype\": \"Boolean\",\n" +
                        "                            \"offset\": 28,\n" +
                        "                            \"safe\": false\n" +
                        "                        }\n" +
                        "                    ],\n" +
                        "                    \"events\": [\n" +
                        "                        {\n" +
                        "                            \"name\": \"Transfer\",\n" +
                        "                            \"parameters\": [\n" +
                        "                                {\n" +
                        "                                    \"name\": \"from\",\n" +
                        "                                    \"type\": \"Hash160\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"to\",\n" +
                        "                                    \"type\": \"Hash160\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"amount\",\n" +
                        "                                    \"type\": \"Integer\"\n" +
                        "                                }\n" +
                        "                            ]\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "                \"permissions\": [\n" +
                        "                    {\n" +
                        "                        \"contract\": \"*\",\n" +
                        "                        \"methods\": \"*\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"trusts\": [],\n" +
                        "                \"extra\": null\n" +
                        "            },\n" +
                        "            \"updatehistory\": [\n" +
                        "                0\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"id\": -8,\n" +
                        "            \"hash\": \"0x49cf4e5378ffcd4dec034fd98a174c5491e395e2\",\n" +
                        "            \"nef\": {\n" +
                        "                \"magic\": 860243278,\n" +
                        "                \"compiler\": \"neo-core-v3.0\",\n" +
                        "                \"source\": \"variable-size-source-rolemanagement\",\n" +
                        "                \"tokens\": [],\n" +
                        "                \"script\": \"EEEa93tnQBBBGvd7Z0A=\",\n" +
                        "                \"checksum\": 983638438\n" +
                        "            },\n" +
                        "            \"manifest\": {\n" +
                        "                \"name\": \"RoleManagement\",\n" +
                        "                \"groups\": [],\n" +
                        "                \"supportedstandards\": [],\n" +
                        "                \"abi\": {\n" +
                        "                    \"methods\": [\n" +
                        "                        {\n" +
                        "                            \"name\": \"designateAsRole\",\n" +
                        "                            \"parameters\": [\n" +
                        "                                {\n" +
                        "                                    \"name\": \"role\",\n" +
                        "                                    \"type\": \"Integer\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"nodes\",\n" +
                        "                                    \"type\": \"Array\"\n" +
                        "                                }\n" +
                        "                            ],\n" +
                        "                            \"returntype\": \"Void\",\n" +
                        "                            \"offset\": 0,\n" +
                        "                            \"safe\": false\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"name\": \"getDesignatedByRole\",\n" +
                        "                            \"parameters\": [\n" +
                        "                                {\n" +
                        "                                    \"name\": \"role\",\n" +
                        "                                    \"type\": \"Integer\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"index\",\n" +
                        "                                    \"type\": \"Integer\"\n" +
                        "                                }\n" +
                        "                            ],\n" +
                        "                            \"returntype\": \"Array\",\n" +
                        "                            \"offset\": 7,\n" +
                        "                            \"safe\": true\n" +
                        "                        }\n" +
                        "                    ],\n" +
                        "                    \"events\": []\n" +
                        "                },\n" +
                        "                \"permissions\": [\n" +
                        "                    {\n" +
                        "                        \"contract\": \"*\",\n" +
                        "                        \"methods\": \"*\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"trusts\": [],\n" +
                        "                \"extra\": null\n" +
                        "            },\n" +
                        "            \"updatehistory\": [\n" +
                        "                0\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"id\": -9,\n" +
                        "            \"hash\": \"0xfe924b7cfe89ddd271abaf7210a80a7e11178758\",\n" +
                        "            \"nef\": {\n" +
                        "                \"magic\": 860243278,\n" +
                        "                \"compiler\": \"neo-core-v3.0\",\n" +
                        "                \"source\": \"variable-size-source-oraclecontract\",\n" +
                        "                \"tokens\": [],\n" +
                        "                \"script\": \"EEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0A=\",\n" +
                        "                \"checksum\": 2663858513\n" +
                        "            },\n" +
                        "            \"manifest\": {\n" +
                        "                \"name\": \"OracleContract\",\n" +
                        "                \"groups\": [],\n" +
                        "                \"supportedstandards\": [],\n" +
                        "                \"abi\": {\n" +
                        "                    \"methods\": [\n" +
                        "                        {\n" +
                        "                            \"name\": \"finish\",\n" +
                        "                            \"parameters\": [],\n" +
                        "                            \"returntype\": \"Void\",\n" +
                        "                            \"offset\": 0,\n" +
                        "                            \"safe\": false\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"name\": \"getPrice\",\n" +
                        "                            \"parameters\": [],\n" +
                        "                            \"returntype\": \"Integer\",\n" +
                        "                            \"offset\": 7,\n" +
                        "                            \"safe\": true\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"name\": \"request\",\n" +
                        "                            \"parameters\": [\n" +
                        "                                {\n" +
                        "                                    \"name\": \"url\",\n" +
                        "                                    \"type\": \"String\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"filter\",\n" +
                        "                                    \"type\": \"String\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"callback\",\n" +
                        "                                    \"type\": \"String\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"userData\",\n" +
                        "                                    \"type\": \"Any\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"gasForResponse\",\n" +
                        "                                    \"type\": \"Integer\"\n" +
                        "                                }\n" +
                        "                            ],\n" +
                        "                            \"returntype\": \"Void\",\n" +
                        "                            \"offset\": 14,\n" +
                        "                            \"safe\": false\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"name\": \"setPrice\",\n" +
                        "                            \"parameters\": [\n" +
                        "                                {\n" +
                        "                                    \"name\": \"price\",\n" +
                        "                                    \"type\": \"Integer\"\n" +
                        "                                }\n" +
                        "                            ],\n" +
                        "                            \"returntype\": \"Void\",\n" +
                        "                            \"offset\": 21,\n" +
                        "                            \"safe\": false\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"name\": \"verify\",\n" +
                        "                            \"parameters\": [],\n" +
                        "                            \"returntype\": \"Boolean\",\n" +
                        "                            \"offset\": 28,\n" +
                        "                            \"safe\": true\n" +
                        "                        }\n" +
                        "                    ],\n" +
                        "                    \"events\": [\n" +
                        "                        {\n" +
                        "                            \"name\": \"OracleRequest\",\n" +
                        "                            \"parameters\": [\n" +
                        "                                {\n" +
                        "                                    \"name\": \"Id\",\n" +
                        "                                    \"type\": \"Integer\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"RequestContract\",\n" +
                        "                                    \"type\": \"Hash160\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"Url\",\n" +
                        "                                    \"type\": \"String\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"Filter\",\n" +
                        "                                    \"type\": \"String\"\n" +
                        "                                }\n" +
                        "                            ]\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"name\": \"OracleResponse\",\n" +
                        "                            \"parameters\": [\n" +
                        "                                {\n" +
                        "                                    \"name\": \"Id\",\n" +
                        "                                    \"type\": \"Integer\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"name\": \"OriginalTx\",\n" +
                        "                                    \"type\": \"Hash256\"\n" +
                        "                                }\n" +
                        "                            ]\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "                \"permissions\": [\n" +
                        "                    {\n" +
                        "                        \"contract\": \"*\",\n" +
                        "                        \"methods\": \"*\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"trusts\": [],\n" +
                        "                \"extra\": null\n" +
                        "            },\n" +
                        "            \"updatehistory\": [\n" +
                        "                0\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}"
        );

        NeoGetNativeContracts getNativeContracts = deserialiseResponse(NeoGetNativeContracts.class);
        List<NativeContractState> nativeContracts = getNativeContracts.getNativeContracts();
        assertThat(nativeContracts, hasSize(3));
        NativeContractState c1 = nativeContracts.get(0);
        assertThat(c1.getId(), is(-6));
        assertThat(c1.getHash(), is(new Hash160("0xd2a4cff31913016155e38e474a2c06d08be276cf")));
        ContractNef nef1 = c1.getNef();
        assertThat(nef1.getMagic(), is(860243278L));
        assertThat(nef1.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef1.getSource(), is("variable-size-source-gastoken"));
        assertThat(nef1.getTokens(), hasSize(0));
        assertThat(nef1.getScript(), is("EEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0A="));
        assertThat(nef1.getChecksum(), is(2663858513L));
        ContractManifest manifest1 = c1.getManifest();
        assertThat(manifest1.getName(), is("GasToken"));
        assertThat(manifest1.getGroups(), hasSize(0));
        assertThat(manifest1.getSupportedStandards(), hasSize(1));
        assertThat(manifest1.getSupportedStandards().get(0), is("NEP-17"));
        assertThat(manifest1.getAbi().getMethods(), hasSize(5));
        assertThat(manifest1.getAbi().getEvents(), hasSize(1));
        assertThat(c1.getUpdateHistory(), hasSize(1));
        assertThat(c1.getUpdateHistory(), contains(0));

        NativeContractState c2 = nativeContracts.get(1);
        assertThat(c2.getId(), is(-8));
        assertThat(c2.getHash(), is(new Hash160("0x49cf4e5378ffcd4dec034fd98a174c5491e395e2")));
        ContractNef nef2 = c2.getNef();
        assertThat(nef2.getMagic(), is(860243278L));
        assertThat(nef2.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef2.getSource(), is("variable-size-source-rolemanagement"));
        assertThat(nef2.getTokens(), hasSize(0));
        assertThat(nef2.getScript(), is("EEEa93tnQBBBGvd7Z0A="));
        assertThat(nef2.getChecksum(), is(983638438L));
        ContractManifest manifest2 = c2.getManifest();
        assertThat(manifest2.getName(), is("RoleManagement"));
        assertThat(manifest2.getGroups(), hasSize(0));
        assertThat(manifest2.getSupportedStandards(), hasSize(0));
        assertThat(manifest2.getAbi().getMethods(), hasSize(2));
        assertThat(manifest2.getAbi().getEvents(), hasSize(0));
        assertThat(c2.getUpdateHistory(), contains(0));

        NativeContractState c3 = nativeContracts.get(2);
        assertThat(c3.getId(), is(-9));
        assertThat(c3.getHash(), is(new Hash160("0xfe924b7cfe89ddd271abaf7210a80a7e11178758")));
        ContractNef nef3 = c3.getNef();
        assertThat(nef3.getMagic(), is(860243278L));
        assertThat(nef3.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef3.getSource(), is("variable-size-source-oraclecontract"));
        assertThat(nef3.getTokens(), hasSize(0));
        assertThat(nef3.getScript(), is("EEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0A="));
        assertThat(nef3.getChecksum(), is(2663858513L));
        ContractManifest manifest3 = c3.getManifest();
        assertThat(manifest3.getName(), is("OracleContract"));
        assertThat(manifest3.getGroups(), hasSize(0));
        assertThat(manifest3.getSupportedStandards(), hasSize(0));
        assertThat(manifest3.getAbi().getMethods(), hasSize(5));
        assertThat(manifest3.getAbi().getEvents(), hasSize(2));
        assertThat(c3.getUpdateHistory(), contains(0));
    }

    @Test
    public void testGetContractState() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": {\n" +
                "        \"id\": -4,\n" +
                "        \"updatecounter\": 0,\n" +
                "        \"hash\": \"0xda65b600f7124ce6c79950c1772a36403104f2be\",\n" +
                "        \"nef\": {\n" +
                "            \"magic\": 860243278,\n" +
                "            \"compiler\": \"neo-core-v3.0\",\n" +
                "            \"source\": \"variable-size-source-ledgercontract\",\n" +
                "            \"tokens\": [],\n" +
                "            \"script\": \"EEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0AQQRr3e2dA\",\n" +
                "            \"checksum\": 529571427\n" +
                "        },\n" +
                "        \"manifest\": {\n" +
                "            \"name\": \"LedgerContract\",\n" +
                "            \"groups\": [],\n" +
                "            \"features\": {},\n" +
                "            \"supportedstandards\": [],\n" +
                "            \"abi\": {\n" +
                "                \"methods\": [\n" +
                "                    {\n" +
                "                        \"name\": \"currentHash\",\n" +
                "                        \"parameters\": [],\n" +
                "                        \"returntype\": \"Hash256\",\n" +
                "                        \"offset\": 0,\n" +
                "                        \"safe\": true\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"name\": \"getTransactionHeight\",\n" +
                "                        \"parameters\": [\n" +
                "                            {\n" +
                "                                \"name\": \"hash\",\n" +
                "                                \"type\": \"Hash256\"\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"returntype\": \"Integer\",\n" +
                "                        \"offset\": 35,\n" +
                "                        \"safe\": true\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"events\": []\n" +
                "            },\n" +
                "            \"permissions\": [\n" +
                "                {\n" +
                "                    \"contract\": \"*\",\n" +
                "                    \"methods\": \"*\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"trusts\": [],\n" +
                "            \"extra\": null\n" +
                "        }\n" +
                "    }\n" +
                "}"
        );

        NeoGetContractState getContractState = deserialiseResponse(NeoGetContractState.class);
        ContractState contractState = getContractState.getContractState();
        assertThat(contractState, is(notNullValue()));
        assertThat(contractState.getId(), is(-4));
        assertThat(contractState.getUpdateCounter(), is(0));
        assertThat(contractState.getHash(), is(new Hash160("0xda65b600f7124ce6c79950c1772a36403104f2be")));
        assertThat(contractState.getNef().getMagic(), is(860243278L));
        assertThat(contractState.getNef().getCompiler(), is("neo-core-v3.0"));
        assertThat(contractState.getNef().getSource(), is("variable-size-source-ledgercontract"));
        assertThat(contractState.getNef().getScript(), is("EEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0AQQRr3e2dA"));
        assertThat(contractState.getNef().getTokens(), is(empty()));
        assertThat(contractState.getNef().getChecksum(), is(529571427L));

        ContractManifest manifest = contractState.getManifest();
        assertThat(manifest, is(notNullValue()));
        assertThat(manifest.getName(), is("LedgerContract"));
        assertThat(manifest.getGroups(), is(notNullValue()));
        assertThat(manifest.getGroups(), hasSize(0));
        assertThat(manifest.getSupportedStandards(), hasSize(0));

        ContractManifest.ContractABI abi = manifest.getAbi();
        assertThat(abi, is(notNullValue()));

        assertThat(abi.getMethods(), is(notNullValue()));
        assertThat(abi.getMethods(), hasSize(2));
        assertThat(abi.getMethods().get(0).getName(), is("currentHash"));
        assertThat(abi.getMethods().get(0).getParameters(), is(notNullValue()));
        assertThat(abi.getMethods().get(0).getParameters(), hasSize(0));
        assertThat(abi.getMethods().get(1).getName(), is("getTransactionHeight"));
        assertThat(abi.getMethods().get(1).getParameters(), is(notNullValue()));
        assertThat(abi.getMethods().get(1).getParameters(), hasSize(1));
        assertThat(abi.getMethods().get(1).getParameters(), hasSize(1));
        assertThat(abi.getMethods().get(1).getParameters().get(0).getName(), is("hash"));
        assertThat(abi.getMethods().get(1).getParameters().get(0).getType(), is(ContractParameterType.HASH256));
        assertThat(abi.getMethods().get(1).getReturnType(), is(ContractParameterType.INTEGER));

        assertThat(abi.getEvents(), is(notNullValue()));
        assertThat(abi.getEvents(), hasSize(0));

        assertThat(manifest.getPermissions(), is(notNullValue()));
        assertThat(manifest.getPermissions(), hasSize(1));
        assertThat(manifest.getPermissions().get(0).getContract(), is("*"));
        assertThat(manifest.getPermissions().get(0).getMethods(), is(notNullValue()));
        assertThat(manifest.getPermissions().get(0).getMethods(), hasSize(1));
        assertThat(manifest.getPermissions().get(0).getMethods().get(0), is("*"));

        assertThat(manifest.getTrusts(), is(notNullValue()));
        assertThat(manifest.getTrusts(), hasSize(0));

        assertThat(manifest.getExtra(), is(nullValue()));

        int id = -4;
        int updateCounter = 0;
        Hash160 hash = new Hash160("0xda65b600f7124ce6c79950c1772a36403104f2be");
        ContractNef nef = new ContractNef(860243278L, "neo-core-v3.0", "variable-size-source-ledgercontract",
                emptyList(), "EEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0AQQRr3e2dA", 529571427L);
        ContractMethod method1 = new ContractMethod("currentHash", emptyList(), 0, ContractParameterType.HASH256, true);
        ContractMethod method2 = new ContractMethod("getTransactionHeight",
                asList(new ContractParameter("hash", ContractParameterType.HASH256)), 35,
                ContractParameterType.INTEGER, true);
        ContractABI contractABI = new ContractABI(asList(method1, method2), emptyList());
        ContractPermission permission = new ContractPermission("*", asList("*"));
        ContractManifest contractManifest = new ContractManifest("LedgerContract", emptyList(), null, emptyList(),
                contractABI, asList(permission), emptyList(), null);
        ContractState expectedEqual = new ContractState(id, updateCounter, hash, nef, contractManifest);
        assertThat(contractState, is(expectedEqual));
    }

    @Test
    public void testExpressListContracts() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": [\n" +
                "        {\n" +
                "            \"hash\": \"0xda65b600f7124ce6c79950c1772a36403104f2be\",\n" +
                "            \"manifest\": {\n" +
                "                \"name\": \"LedgerContract\",\n" +
                "                \"groups\": [],\n" +
                "                \"features\": {},\n" +
                "                \"supportedstandards\": [],\n" +
                "                \"abi\": {\n" +
                "                    \"methods\": [\n" +
                "                        {\n" +
                "                            \"name\": \"currentHash\",\n" +
                "                            \"parameters\": [],\n" +
                "                            \"returntype\": \"Hash256\",\n" +
                "                            \"offset\": 0,\n" +
                "                            \"safe\": true\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"events\": []\n" +
                "                },\n" +
                "                \"permissions\": [\n" +
                "                    {\n" +
                "                        \"contract\": \"*\",\n" +
                "                        \"methods\": \"*\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"trusts\": [],\n" +
                "                \"extra\": null\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}"
        );

        NeoExpressListContracts listContracts = deserialiseResponse(NeoExpressListContracts.class);

        List<ExpressContractState> contracts = listContracts.getContracts();
        assertThat(contracts.size(), is(1));

        ExpressContractState expressContractState = contracts.get(0);
        assertThat(expressContractState.getHash(), is(new Hash160("0xda65b600f7124ce6c79950c1772a36403104f2be")));

        List<ContractMethod> methods = asList(new ContractMethod("currentHash", emptyList(), 0,
                ContractParameterType.HASH256, true));
        ContractABI abi = new ContractABI(methods, emptyList());
        ContractManifest manifest = new ContractManifest("LedgerContract", emptyList(), new HashMap<>(), emptyList(),
                abi, asList(new ContractPermission("*", asList("*"))), emptyList(), null);
        assertThat(expressContractState.getManifest(), is(manifest));
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
                        new Hash256("0x9786cce0dddb524c40ddbdd5e31a41ed1f6b5c8a683c122f627ca4a007a7cf4e"),
                        new Hash256("0xb488ad25eb474f89d5ca3f985cc047ca96bc7373a6d3da8c0f192722896c1cd7")
                )
        );

        assertThat(getMemPool.getMemPoolDetails().getUnverified(), notNullValue());
        assertThat(getMemPool.getMemPoolDetails().getUnverified(), hasSize(2));
        assertThat(
                getMemPool.getMemPoolDetails().getUnverified(),
                containsInAnyOrder(
                        new Hash256("0x9786cce0dddb524c40ddbdd5e31a41ed1f6b5c8a683c122f627ca4a007a7cf4e"),
                        new Hash256("0xb488ad25eb474f89d5ca3f985cc047ca96bc7373a6d3da8c0f192722896c1cd7")
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
                        new Hash256("0x9786cce0dddb524c40ddbdd5e31a41ed1f6b5c8a683c122f627ca4a007a7cf4e"),
                        new Hash256("0xb488ad25eb474f89d5ca3f985cc047ca96bc7373a6d3da8c0f192722896c1cd7"),
                        new Hash256("0xf86f6f2c08fbf766ebe59dc84bc3b8829f1053f0a01deb26bf7960d99fa86cd6")
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
                        "        \"signers\": [" +
                        "           {" +
                        "               \"account\": \"0x69ecca587293047be4c59159bf8bc399985c160d\"," +
                        "               \"scopes\": \"CustomContracts, CustomGroups, WitnessRules\"," +
                        "               \"allowedcontracts\": [" +
                        "                   \"0xd2a4cff31913016155e38e474a2c06d08be276cf\"," +
                        "                   \"0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5\"" +
                        "               ]," +
                        "               \"allowedgroups\": [" +
                        "                   \"033a4d051b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7857787f1b\"" +
                        "               ]," +
                        "               \"rules\": [" +
                        "                   {" +
                        "                       \"action\": \"Allow\",\n" +
                        "                       \"condition\": {\n" +
                        "                           \"type\":\"ScriptHash\",\n" +
                        "                           \"hash\":\"0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5\"\n" +
                        "                       }" +
                        "                   }" +
                        "               ]" +
                        "           }" +
                        "        ]," +
                        "        \"attributes\": [\n" +
                        "            {" +
                        "                \"type\": \"HighPriority\"" +
                        "            }," +
                        "            {" +
                        "                \"type\": \"OracleResponse\"," +
                        "                \"id\": 0," +
                        "                \"code\": \"Success\"," +
                        "                \"result\": \"EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw==\"" +
                        "            }" +
                        "        ]," +
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
        Transaction transaction = getTransaction.getTransaction();
        assertThat(transaction, is(notNullValue()));
        assertThat(transaction.getHash(),
                is(new Hash256("0x8b8b222ba4ae17eaf37d444210920690d0981b02c368f4f1973c8fd662438d89")));
        assertThat(transaction.getSize(), is(267L));
        assertThat(transaction.getVersion(), is(0));
        assertThat(transaction.getNonce(), is(1046354582L));
        assertThat(transaction.getSender(), is("AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4"));
        assertThat(transaction.getSysFee(), is("9007810"));
        assertThat(transaction.getNetFee(), is("1267450"));
        assertThat(transaction.getValidUntilBlock(), is(2103622L));

        List<TransactionSigner> signers = transaction.getSigners();
        assertThat(signers, is(notNullValue()));
        assertThat(signers, hasSize(1));

        assertThat(signers.get(0).getAccount(), is(new Hash160("69ecca587293047be4c59159bf8bc399985c160d")));
        assertThat(signers.get(0).getScopes(), hasSize(3));
        assertThat(signers.get(0).getScopes().get(0), is(WitnessScope.CUSTOM_CONTRACTS));
        assertThat(signers.get(0).getScopes().get(1), is(WitnessScope.CUSTOM_GROUPS));
        assertThat(signers.get(0).getScopes().get(2), is(WitnessScope.WITNESS_RULES));
        assertThat(signers.get(0).getAllowedContracts().get(0), is("0xd2a4cff31913016155e38e474a2c06d08be276cf"));
        assertThat(signers.get(0).getAllowedContracts().get(1), is("0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5"));
        assertThat(signers.get(0).getAllowedGroups().get(0), is("033a4d051b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7857787f1b"));
        WitnessRule rule = signers.get(0).getRules().get(0);
        assertThat(rule.getAction(), is(WitnessAction.ALLOW));
        assertThat(rule.getCondition().getType(), is(WitnessConditionType.SCRIPT_HASH));
        assertThat(rule.getCondition().getScriptHash(), is(new Hash160("0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5")));

        List<TransactionAttribute> attributes = transaction.getAttributes();
        assertThat(attributes, is(notNullValue()));
        assertThat(attributes.get(0).getType(), is(TransactionAttributeType.HIGH_PRIORITY));
        assertThat(attributes.get(0).asHighPriority(), is(instanceOf(HighPriorityAttribute.class)));
        assertThat(attributes.get(1).getType(), is(TransactionAttributeType.ORACLE_RESPONSE));
        OracleResponseAttribute oracleResponseAttribute = (OracleResponseAttribute) attributes.get(1);
        OracleResponse oracleResp = oracleResponseAttribute.getOracleResponse();
        assertThat(oracleResp.getResponseCode(), is(OracleResponseCode.SUCCESS));
        assertThat(oracleResp.getResult(), is("EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw=="));
        assertThat(oracleResp.getId(), is(0));

        assertThat(transaction.getScript(),
                is("AGQMFObBATZUrxE9ipaL3KUsmUioK5U9DBQP7O1Ep0MA2doEn6k2cKQxFxiP9hPADAh0cmFuc2ZlcgwUiXcg2M129PAKv6N8Dt2InCCP3ptBYn1bUjg="));
        assertThat(transaction.getWitnesses(), is(notNullValue()));
        assertThat(transaction.getWitnesses(), hasSize(1));
        assertThat(transaction.getWitnesses(),
                containsInAnyOrder(
                        new NeoWitness(
                                "DEBhsuS9LxQ2PKpx2XJJ/aGEr/pZ7qfZy77OyhDmWx+BobkQAnDPLg6ohOa9SSHa0OMDavUl7zpmJip3r8T5Dr1L",
                                "EQwhA/HsPB4oPogN5unEifDyfBkAfFM4WqpMDJF8MgB57a3yEQtBMHOzuw=="
                        )
                ));
        assertThat(transaction.getBlockHash(),
                is(new Hash256("0x8529cf7301d13cc13d85913b8367700080a6e96db045687b8db720e91e803299")));
        assertThat(transaction.getConfirmations(), is(1388));
        assertThat(transaction.getBlockTime(), is(1589019142879L));
        assertThat(transaction.getVMState(), is(NeoVMStateType.HALT));
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
    public void testGetNextBlockValidators() {
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

        NeoGetNextBlockValidators neoGetNextBlockValidators = deserialiseResponse(NeoGetNextBlockValidators.class);
        assertThat(neoGetNextBlockValidators.getNextBlockValidators(), hasSize(2));
        assertThat(neoGetNextBlockValidators.getNextBlockValidators(),
                containsInAnyOrder(
                        new NeoGetNextBlockValidators.Validator(
                                "03f1ec3c1e283e880de6e9c489f0f27c19007c53385aaa4c0c917c320079edadf2",
                                "0", false),
                        new NeoGetNextBlockValidators.Validator(
                                "02494f3ff953e45ca4254375187004f17293f90a1aa4b1a89bc07065bc1da521f6",
                                "91600000", true)
                )
        );
        assertThat(neoGetNextBlockValidators.getNextBlockValidators().get(0).getPublicKey(),
                is("03f1ec3c1e283e880de6e9c489f0f27c19007c53385aaa4c0c917c320079edadf2"));
        assertThat(neoGetNextBlockValidators.getNextBlockValidators().get(0).getVotes(), is("0"));
        assertThat(neoGetNextBlockValidators.getNextBlockValidators().get(0).getVotesAsBigInteger(), is(BigInteger.valueOf(0)));
        assertThat(neoGetNextBlockValidators.getNextBlockValidators().get(0).getActive(), is(false));
        assertThat(neoGetNextBlockValidators.getNextBlockValidators().get(1).getActive(), is(true));
    }

    @Test
    public void testGetNextBlockValidators_Empty() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 67,\n" +
                        "    \"result\": []\n" +
                        "}"
        );

        NeoGetNextBlockValidators neoGetNextBlockValidators = deserialiseResponse(NeoGetNextBlockValidators.class);
        assertThat(neoGetNextBlockValidators.getNextBlockValidators(), is(notNullValue()));
        assertThat(neoGetNextBlockValidators.getNextBlockValidators(), hasSize(0));
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
                        new NeoGetPeers.AddressEntry("127.0.0.1", 20335),
                        new NeoGetPeers.AddressEntry("127.0.0.1", 20336),
                        new NeoGetPeers.AddressEntry("127.0.0.1", 20337)
                ));

        assertThat(getPeers.getPeers().getBad(), is(notNullValue()));
        assertThat(getPeers.getPeers().getBad(), hasSize(1));
        assertThat(getPeers.getPeers().getBad(),
                containsInAnyOrder(
                        new NeoGetPeers.AddressEntry("127.0.0.1", 20333)
                ));

        assertThat(getPeers.getPeers().getConnected(), is(notNullValue()));
        assertThat(getPeers.getPeers().getConnected(), hasSize(2));
        assertThat(getPeers.getPeers().getConnected(),
                containsInAnyOrder(
                        new NeoGetPeers.AddressEntry("172.18.0.3", 40333),
                        new NeoGetPeers.AddressEntry("172.18.0.4", 20333)
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
                        "        \"tcpport\": 40333,\n" +
                        "        \"wsport\": 40334,\n" +
                        "        \"nonce\": 224036820,\n" +
                        "        \"useragent\": \"/Neo:3.0.0/\",\n" +
                        "        \"protocol\": {\n" +
                        "            \"network\": 769,\n" +
                        "            \"validatorscount\": 7,\n" +
                        "            \"msperblock\": 15000,\n" +
                        "            \"maxvaliduntilblockincrement\": 1,\n" +
                        "            \"maxtraceableblocks\": 3,\n" +
                        "            \"addressversion\": 22,\n" +
                        "            \"maxtransactionsperblock\": 150000,\n" +
                        "            \"memorypoolmaxtransactions\": 34000,\n" +
                        "            \"initialgasdistribution\": 14\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetVersion neoGetVersion = deserialiseResponse(NeoGetVersion.class);
        NeoGetVersion.NeoVersion version = neoGetVersion.getVersion();
        assertThat(version, is(notNullValue()));
        assertThat(version.getTCPPort(), is(40333));
        assertThat(version.getWSPort(), is(40334));
        assertThat(version.getNonce(), is(224036820L));
        assertThat(version.getUserAgent(), is("/Neo:3.0.0/"));

        NeoGetVersion.NeoVersion.Protocol protocol = version.getProtocol();
        assertThat(protocol.getAddressVersion(), is(22));
        assertThat(protocol.getNetwork(), is(769L));
        assertThat(protocol.getMilliSecondsPerBlock(), is(15000L));
        assertThat(protocol.getMaxTraceableBlocks(), is(3L));
        assertThat(protocol.getMaxValidUntilBlockIncrement(), is(1L));
        assertThat(protocol.getMaxTransactionsPerBlock(), is(150000L));
        assertThat(protocol.getMemoryPoolMaxTransactions(), is(34000));
        assertThat(protocol.getInitialGasDistribution(), is(BigInteger.valueOf(14)));
    }


    @Test
    public void testGetVersion_Network_Long() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"tcpport\": 40333,\n" +
                        "        \"wsport\": 40334,\n" +
                        "        \"nonce\": 224036820,\n" +
                        "        \"useragent\": \"/Neo:3.0.0/\",\n" +
                        "        \"protocol\": {\n" +
                        "            \"addressversion\": 22,\n" +
                        "            \"network\": 4232068425,\n" +
                        "            \"msperblock\": 15000,\n" +
                        "            \"maxtraceableblocks\": 3,\n" +
                        "            \"maxvaliduntilblockincrement\": 1,\n" +
                        "            \"maxtransactionsperblock\": 150000,\n" +
                        "            \"memorypoolmaxtransactions\": 34000,\n" +
                        "            \"initialgasdistribution\": 14\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetVersion neoGetVersion = deserialiseResponse(NeoGetVersion.class);
        NeoGetVersion.NeoVersion version = neoGetVersion.getVersion();
        assertThat(version, is(notNullValue()));
        assertThat(version.getTCPPort(), is(40333));
        assertThat(version.getWSPort(), is(40334));
        assertThat(version.getNonce(), is(224036820L));
        assertThat(version.getUserAgent(), is("/Neo:3.0.0/"));

        NeoGetVersion.NeoVersion.Protocol protocol = version.getProtocol();
        assertThat(protocol.getAddressVersion(), is(22));
        assertThat(protocol.getNetwork(), is(4232068425L));
        assertThat(protocol.getMilliSecondsPerBlock(), is(15000L));
        assertThat(protocol.getMaxTraceableBlocks(), is(3L));
        assertThat(protocol.getMaxValidUntilBlockIncrement(), is(1L));
        assertThat(protocol.getMaxTransactionsPerBlock(), is(150000L));
        assertThat(protocol.getMemoryPoolMaxTransactions(), is(34000));
        assertThat(protocol.getInitialGasDistribution(), is(BigInteger.valueOf(14)));
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
                is(new Hash256("0xb0748d216c9c0d0498094cdb50407035917b350fc0338c254b78f944f723b770")));
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
                        "        \"script\": \"wh8MFnRva2Vuc1dpdGhXaXRuZXNzQ2hlY2sMFFdiWCF05OK8ywVb+rl30RPV3+zlQWJ9W1I=\",\n" +
                        "        \"state\": \"HALT\",\n" +
                        "        \"gasconsumed\": \"12908980\",\n" +
                        "        \"exception\": null,\n" +
                        "        \"notifications\": [\n" +
                        "            {\n" +
                        "                \"eventname\": \"Mint\",\n" +
                        "                \"contract\": \"0xe5ecdfd513d177b9fa5b05cbbce2e47421586257\",\n" +
                        "                \"state\": {\n" +
                        "                    \"type\": \"Array\",\n" +
                        "                    \"value\": [\n" +
                        "                        {\n" +
                        "                            \"type\": \"Integer\",\n" +
                        "                            \"value\": \"1\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"type\": \"ByteString\",\n" +
                        "                            \"value\": \"dG9rZW4x\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"eventname\": \"StorageUpdate\",\n" +
                        "                \"contract\": \"0xe5ecdfd513d177b9fa5b05cbbce2e47421586257\",\n" +
                        "                \"state\": {\n" +
                        "                    \"type\": \"Array\",\n" +
                        "                    \"value\": [\n" +
                        "                        {\n" +
                        "                            \"type\": \"ByteString\",\n" +
                        "                            \"value\": \"dG9rZW4x\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"type\": \"ByteString\",\n" +
                        "                            \"value\": \"Y3JlYXRl\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"stack\": [\n" +
                        "            {\n" +
                        "                \"type\": \"InteropInterface\",\n" +
                        "                \"interface\": \"IIterator\",\n" +
                        "                \"id\": \"fcf7b800-192a-488f-95d3-c40ac7b30ef1\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"session\": \"6ecb0e24-ce7f-4550-9838-aeb8c9e08570\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoInvokeFunction invokeFunction = deserialiseResponse(NeoInvokeFunction.class);
        InvocationResult invocationResult = invokeFunction.getInvocationResult();
        assertThat(invocationResult.getScript(),
                is("wh8MFnRva2Vuc1dpdGhXaXRuZXNzQ2hlY2sMFFdiWCF05OK8ywVb+rl30RPV3+zlQWJ9W1I="));
        assertThat(invocationResult.getState(), is(NeoVMStateType.HALT));
        assertThat(invocationResult.getGasConsumed(), is("12908980"));
        assertNull(invocationResult.getException());

        List<Notification> notifications = invocationResult.getNotifications();
        assertThat(notifications, hasSize(2));
        assertThat(notifications.get(0).getContract(),
                is(new Hash160("0xe5ecdfd513d177b9fa5b05cbbce2e47421586257")));
        assertThat(notifications.get(0).getEventName(), is("Mint"));
        assertThat(notifications.get(0).getState().getType(), is(StackItemType.ARRAY));
        assertThat(notifications.get(0).getState().getList().get(0).getInteger(), is(BigInteger.ONE));
        assertThat(notifications.get(0).getState().getList().get(1).getString(), is("token1"));
        assertThat(notifications.get(1).getContract(),
                is(new Hash160("0xe5ecdfd513d177b9fa5b05cbbce2e47421586257")));
        assertThat(notifications.get(1).getEventName(), is("StorageUpdate"));
        assertThat(notifications.get(1).getState().getType(), is(StackItemType.ARRAY));
        assertThat(notifications.get(1).getState().getList().get(1).getString(), is("create"));

        assertThat(invocationResult.getStack(), hasSize(1));
        assertThat(invocationResult.getStack().get(0).getType(), is(StackItemType.INTEROP_INTERFACE));
        assertThat(invocationResult.getStack().get(0).getIteratorId(), is("fcf7b800-192a-488f-95d3-c40ac7b30ef1"));
        InteropInterfaceStackItem interopInterface = (InteropInterfaceStackItem) invocationResult.getStack().get(0);
        assertThat(interopInterface.getInterfaceName(), is("IIterator"));
        assertThat(interopInterface.getValue(), is("fcf7b800-192a-488f-95d3-c40ac7b30ef1"));
        assertThat(interopInterface.getIteratorId(), is("fcf7b800-192a-488f-95d3-c40ac7b30ef1"));

        assertThat(invocationResult.getSessionId(), is("6ecb0e24-ce7f-4550-9838-aeb8c9e08570"));
    }

    @Test
    public void testStackItem_invokeFunction() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"script\": \"0c14e6c1013654af113d8a968bdca52c9948a82b953d11c00c0962616c616e63654f660c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52\",\n" +
                        "        \"state\": \"HALT\",\n" +
                        "        \"gasconsumed\": \"2007570\",\n" +
                        "        \"exception\": null,\n" +
                        "        \"notifications\": [],\n" +
                        "        \"stack\": [\n" +
                        "            {\n" +
                        "                \"type\": \"Buffer\",\n" +
                        "                \"value\": \"dHJhbnNmZXI=\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"type\": \"Buffer\",\n" +
                        "                \"value\": \"lBNDI5IT+g52XxAnznQvSNt3mpY=\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"type\": \"Buffer\",\n" +
                        "                \"value\": \"wWq=\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"type\": \"Pointer\",\n" +
                        "                \"value\": \"123\"\n" +
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
                        "                            \"type\": \"Pointer\",\n" +
                        "                            \"value\": 12\n" +
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
        assertThat(invokeFunction.getInvocationResult().getState(), is(NeoVMStateType.HALT));
        assertThat(invokeFunction.getInvocationResult().getGasConsumed(), is("2007570"));
        assertNull(invokeFunction.getInvocationResult().getException());
        assertThat(invokeFunction.getInvocationResult().getNotifications(), hasSize(0));

        assertThat(invokeFunction.getInvocationResult().getStack(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getStack(), hasSize(5));

        StackItem stackItem0 = invokeFunction.getInvocationResult().getStack().get(0);
        assertThat(stackItem0.getType(), is(StackItemType.BUFFER));
        assertThat(stackItem0.getString(), is("transfer"));

        StackItem stackItem1 = invokeFunction.getInvocationResult().getStack().get(1);
        assertThat(stackItem1.getType(), is(StackItemType.BUFFER));
        assertThat(stackItem1.getAddress(), is("NZQvGWfSupuUAYtCH6pje72hdkWJH1jAZP"));

        StackItem stackItem2 = invokeFunction.getInvocationResult().getStack().get(2);
        assertThat(stackItem2.getType(), is(StackItemType.BUFFER));
        assertArrayEquals(hexStringToByteArray("c16a"), stackItem2.getByteArray());
        assertThat(stackItem2.getInteger(), is(new BigInteger("27329")));

        StackItem stackItem3 = invokeFunction.getInvocationResult().getStack().get(3);
        assertThat(stackItem3.getType(), is(StackItemType.POINTER));
        assertThat(stackItem3.getPointer(), is(new BigInteger("123")));

        StackItem stackItem4 = invokeFunction.getInvocationResult().getStack().get(4);
        assertThat(stackItem4.getType(), is(StackItemType.MAP));
        assertThat(stackItem4.getMap().size(), equalTo(1));
        BigInteger value = stackItem4.getMap()
                .get(new ByteStringStackItem(hexStringToByteArray("941343239213fa0e765f1027ce742f48db779a96")))
                .getPointer();
        assertThat(value, is(new BigInteger("12")));
    }

    @Test
    public void testInvokeFunction_pending_signatures() {
        buildResponse(
                "{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": {\n" +
                "        \"script\": \"00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc\",\n" +
                "        \"state\": \"HALT\",\n" +
                "        \"gasconsumed\": \"2.489\",\n" +
                "        \"stack\": [],\n" +
                "        \"pendingsignature\": {\n" +
                "            \"type\": \"Transaction\",\n" +
                "            \"data\": \"base64 string of the tx bytes\",\n" +
                "            \"network\": 305419896, \n" +
                "            \"items\": {\n" +
                "                \"0x69ecca587293047be4c59159bf8bc399985c160d\": {\n" +
                "                    \"script\": \"base64 script\",\n" +
                "                    \"parameters\": [\n" +
                "                        {\n" +
                "                            \"type\": \"Signature\",\n" +
                "                            \"value\": \"\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"signatures\": {\n" +
                "                        \"<033a4d051b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7857787f1b>\": \"base64 string of signature\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"0x05859de95ccbbd5668e0f055b208273634d4657f\": {\n" +
                "                    \"script\": \"base64 script\",\n" +
                "                    \"parameters\": [\n" +
                "                        {\n" +
                "                            \"type\": \"Signature\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"type\": \"Signature\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"signatures\": {\n" +
                "                        \"033a1d0a3b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7957783f81\": \"base64 string of signature\",\n" +
                "                        \"033a4c051b09b77c0230d2b1aaedfd5a84be279a5361a7358db665ad7d57787f10\": \"base64 string of signature\"\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}"
        );

        NeoInvokeFunction invokeFunction = deserialiseResponse(NeoInvokeFunction.class);
        assertThat(invokeFunction.getInvocationResult(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getScript(),
                is("00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc"));
        assertThat(invokeFunction.getInvocationResult().getState(), is(NeoVMStateType.HALT));
        assertThat(invokeFunction.getInvocationResult().getGasConsumed(), is("2.489"));

        assertThat(invokeFunction.getInvocationResult().getStack(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getStack(), hasSize(0));
        InvocationResult.PendingSignature pendingSig = invokeFunction.getInvocationResult().getPendingSignature();
        assertThat(pendingSig.getType(), is("Transaction"));
        assertThat(pendingSig.getData(), is("base64 string of the tx bytes"));
        assertThat(pendingSig.getNetwork(), is(305419896L));
        Map<String, InvocationResult.PendingSignature.Item> items = pendingSig.getItems();
        InvocationResult.PendingSignature.Item item = items.get("0x05859de95ccbbd5668e0f055b208273634d4657f");
        assertThat(item.getScript(), is("base64 script"));
        assertThat(item.getParameters().get(1).getType(), is(ContractParameterType.SIGNATURE));
        assertThat(item.getSignatures().get("033a1d0a3b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7957783f81"),
                is("base64 string of signature"));
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
                        "        \"gasconsumed\": \"2007390\",\n" +
                        "        \"exception\": null,\n" +
                        "        \"notifications\": [],\n" +
                        "        \"stack\": []\n" +
                        "    }\n" +
                        "}"
        );

        NeoInvokeFunction invokeFunction = deserialiseResponse(NeoInvokeFunction.class);
        assertThat(invokeFunction.getInvocationResult(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getScript(),
                is("10c00c0962616c616e63654f660c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52"));
        assertThat(invokeFunction.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
        assertThat(invokeFunction.getInvocationResult().getGasConsumed(), is("2007390"));
        assertThat(invokeFunction.getInvocationResult().getStack(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getStack(), hasSize(0));
        InvocationResult expectedResult = new InvocationResult(
                "10c00c0962616c616e63654f660c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52",
                NeoVMStateType.FAULT, "2007390", null, asList(), null, asList(), null, null, null);
        assertThat(invokeFunction.getInvocationResult(), is(expectedResult));
    }

    @Test
    public void testInvokeFunction_emptyState() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"script\": \"10c00c0962616c616e63654f660c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52\",\n" +
                        "        \"state\": \"\",\n" +
                        "        \"gasconsumed\": \"2007390\",\n" +
                        "        \"stack\": []\n" +
                        "    }\n" +
                        "}"
        );

        NeoInvokeFunction invokeFunction = deserialiseResponse(NeoInvokeFunction.class);
        assertThat(invokeFunction.getInvocationResult(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getScript(),
                is("10c00c0962616c616e63654f660c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52"));
        assertThat(invokeFunction.getInvocationResult().getState(), is(NeoVMStateType.NONE));
        assertThat(invokeFunction.getInvocationResult().getGasConsumed(), is("2007390"));
        assertThat(invokeFunction.getInvocationResult().getStack(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getStack(), hasSize(0));
    }

    @Test
    public void testInvokeFunction_diagnostics() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"script\": \"wh8MC2NhbGxTeW1ib2xzDBQ35AiF8REp1Iy5N6DbcAjECghSDkFifVtS\",\n" +
                        "        \"state\": \"HALT\",\n" +
                        "        \"gasconsumed\": \"4845600\",\n" +
                        "        \"exception\": null,\n" +
                        "        \"notifications\": [],\n" +
                        "        \"diagnostics\": {\n" +
                        "            \"invokedcontracts\": {\n" +
                        "                \"hash\": \"0x7df45ba2d3a0c0520ceef7a73f8d1c404cc59a48\",\n" +
                        "                \"call\": [\n" +
                        "                    {\n" +
                        "                        \"hash\": \"0x0e52080ac40870dba037b98cd42911f18508e437\",\n" +
                        "                        \"call\": [\n" +
                        "                            {\n" +
                        "                                \"hash\": \"0x0e52080ac40870dba037b98cd42911f18508e437\"\n" +
                        "                            },\n" +
                        "                            {\n" +
                        "                                \"hash\": \"0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5\"\n" +
                        "                            },\n" +
                        "                            {\n" +
                        "                                \"hash\": \"0xd2a4cff31913016155e38e474a2c06d08be276cf\"\n" +
                        "                            }\n" +
                        "                        ]\n" +
                        "                    }\n" +
                        "                ]\n" +
                        "            },\n" +
                        "            \"storagechanges\": [\n" +
                        "                {\n" +
                        "                    \"state\": \"Deleted\",\n" +
                        "                    \"key\": \"BgAAAP8=\",\n" +
                        "                    \"value\": \"DRZcmJnDi79ZkcXkewSTcljK7Gk=\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"state\": \"Changed\",\n" +
                        "                    \"key\": \"+v///xQNFlyYmcOLv1mRxeR7BJNyWMrsaQ==\",\n" +
                        "                    \"value\": \"QQEhBQAb1mAS\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"state\": \"Added\",\n" +
                        "                    \"key\": \"+v///xRjv+9gkFzYfFbaQGRkS+b3ro7EiA==\",\n" +
                        "                    \"value\": \"QQEhAQo=\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        \"stack\": [\n" +
                        "            {\n" +
                        "                \"type\": \"Array\",\n" +
                        "                \"value\": [\n" +
                        "                    {\n" +
                        "                        \"type\": \"ByteString\",\n" +
                        "                        \"value\": \"TkVP\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"type\": \"ByteString\",\n" +
                        "                        \"value\": \"R0FT\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"type\": \"ByteString\",\n" +
                        "                        \"value\": \"TkVP\"\n" +
                        "                    }\n" +
                        "                ]\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoInvokeFunction neoInvokeFunction = deserialiseResponse(NeoInvokeFunction.class);
        Diagnostics diagnostics = neoInvokeFunction.getInvocationResult().getDiagnostics();

        Diagnostics.InvokedContract invokedContracts = diagnostics.getInvokedContracts();
        Hash160 invokeFunctionScriptHash = new Hash160("0x7df45ba2d3a0c0520ceef7a73f8d1c404cc59a48");
        assertThat(invokedContracts.getHash(), is(invokeFunctionScriptHash));

        List<Diagnostics.InvokedContract> calls = invokedContracts.getInvokedContracts();
        assertThat(calls, hasSize(1));
        Hash160 calledContract = new Hash160("0x0e52080ac40870dba037b98cd42911f18508e437");
        assertThat(calls.get(0).getHash(), is(calledContract));

        List<Diagnostics.InvokedContract> nestedInvokedContracts = calls.get(0).getInvokedContracts();
        assertThat(nestedInvokedContracts, hasSize(3));
        assertThat(nestedInvokedContracts.get(0).getHash(), is(calledContract));
        assertThat(nestedInvokedContracts.get(0).getInvokedContracts(), hasSize(0));
        Hash160 neoToken = new Hash160("0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5");
        assertThat(nestedInvokedContracts.get(1).getHash(), is(neoToken));
        assertThat(nestedInvokedContracts.get(1).getInvokedContracts(), hasSize(0));
        Hash160 gasToken = new Hash160("0xd2a4cff31913016155e38e474a2c06d08be276cf");
        assertThat(nestedInvokedContracts.get(2).getHash(), is(gasToken));
        assertThat(nestedInvokedContracts.get(2).getInvokedContracts(), hasSize(0));

        List<Diagnostics.StorageChange> storageChanges = diagnostics.getStorageChanges();
        assertThat(storageChanges, hasSize(3));
        Diagnostics.StorageChange storageChange1 = storageChanges.get(0);
        Diagnostics.StorageChange expectedStorageChange1 = new Diagnostics.StorageChange("Deleted", "BgAAAP8=",
                "DRZcmJnDi79ZkcXkewSTcljK7Gk=");
        assertThat(storageChange1.getState(), is(expectedStorageChange1.getState()));
        assertThat(storageChange1.getKey(), is(expectedStorageChange1.getKey()));
        assertThat(storageChange1.getValue(), is(expectedStorageChange1.getValue()));
        Diagnostics.StorageChange expectedStorageChange2 = new Diagnostics.StorageChange("Changed",
                "+v///xQNFlyYmcOLv1mRxeR7BJNyWMrsaQ==", "QQEhBQAb1mAS");
        Diagnostics.StorageChange storageChange2 = storageChanges.get(1);
        assertThat(storageChange2.getState(), is(expectedStorageChange2.getState()));
        assertThat(storageChange2.getKey(), is(expectedStorageChange2.getKey()));
        assertThat(storageChange2.getValue(), is(expectedStorageChange2.getValue()));
        Diagnostics.StorageChange expectedStorageChange3 = new Diagnostics.StorageChange("Added",
                "+v///xRjv+9gkFzYfFbaQGRkS+b3ro7EiA==", "QQEhAQo=");
        Diagnostics.StorageChange storageChange3 = storageChanges.get(2);
        assertThat(storageChange3.getState(), is(expectedStorageChange3.getState()));
        assertThat(storageChange3.getKey(), is(expectedStorageChange3.getKey()));
        assertThat(storageChange3.getValue(), is(expectedStorageChange3.getValue()));

        Diagnostics.InvokedContract calledContractCall = new Diagnostics.InvokedContract(calledContract);
        Diagnostics.InvokedContract neoTokenCall = new Diagnostics.InvokedContract(neoToken);
        Diagnostics.InvokedContract gasTokenCall = new Diagnostics.InvokedContract(gasToken);
        List<Diagnostics.InvokedContract> call2 = asList(calledContractCall, neoTokenCall, gasTokenCall);
        Diagnostics.InvokedContract call1 = new Diagnostics.InvokedContract(calledContract, call2);
        Diagnostics.InvokedContract expectedInvokedContract =
                new Diagnostics.InvokedContract(invokeFunctionScriptHash, asList(call1));

        Diagnostics expectedDiagnostics = new Diagnostics(expectedInvokedContract,
                asList(expectedStorageChange1, expectedStorageChange2, expectedStorageChange3));
        assertThat(diagnostics, is(expectedDiagnostics));
    }

    @Test
    public void testInvokeScript() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 3,\n" +
                        "    \"result\": {\n" +
                        "        \"script\": \"10c00c08646563696d616c730c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b52\",\n" +
                        "        \"state\": \"HALT\",\n" +
                        "        \"gasconsumed\": \"0.161\",\n" +
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
                is("10c00c08646563696d616c730c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b52"));
        assertThat(invokeScript.getInvocationResult().getState(), is(NeoVMStateType.HALT));
        assertThat(invokeScript.getInvocationResult().getGasConsumed(), is("0.161"));
        assertThat(invokeScript.getInvocationResult().getStack(), is(notNullValue()));
        assertThat(invokeScript.getInvocationResult().getStack(), hasSize(1));
        assertThat(invokeScript.getInvocationResult().getStack(),
                hasItem(new ByteStringStackItem("Transfer".getBytes())));
    }

    @Test
    public void testTraverseIterator() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": [\n" +
                        "        {\n" +
                        "            \"type\": \"ByteString\",\n" +
                        "            \"value\": \"dG9rZW5PbmU=\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"ByteString\",\n" +
                        "            \"value\": \"dG9rZW5Ud28=\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"ByteString\",\n" +
                        "            \"value\": \"dG9rZW5UaHJlZQ==\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"ByteString\",\n" +
                        "            \"value\": \"dG9rZW5Gb3Vy\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}"
        );

        NeoTraverseIterator traverseIterator = deserialiseResponse(NeoTraverseIterator.class);
        List<StackItem> iteratorList = traverseIterator.getTraverseIterator();
        assertThat(iteratorList, hasSize(4));
        assertThat(iteratorList.get(3).getString(), is("tokenFour"));
    }

    @Test
    public void testTerminateSession() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": true\n" +
                        "}"
        );

        NeoTerminateSession terminateSession = deserialiseResponse(NeoTerminateSession.class);
        assertTrue(terminateSession.getTerminateSession());
    }

    @Test
    public void testNeoInvokeContractVerify() {
        buildResponse(
                "{\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"id\": 1,\n"
                        + "  \"result\": {\n"
                        + "    \"script\": \"VgEMFJOtFXKks1xLklSDzhcBt4dC3EYPYEBXAAIhXwAhQfgn7IxA\",\n"
                        + "    \"state\": \"FAULT\",\n"
                        + "    \"gasconsumed\": \"0.0103542\",\n"
                        + "    \"exception\": \"Specified argument was out of the range of valid values. (Parameter " +
                        "'index')\",\n"
                        + "    \"stack\": [\n"
                        + "            {\n"
                        + "                \"type\": \"Buffer\",\n"
                        + "                \"value\": \"dHJhbnNmZXI=\"\n"
                        + "            }\n"
                        + "  ]}\n"
                        + "}"
        );

        NeoInvokeContractVerify invokeFunction = deserialiseResponse(NeoInvokeContractVerify.class);
        assertThat(invokeFunction.getInvocationResult().getScript(),
                is("VgEMFJOtFXKks1xLklSDzhcBt4dC3EYPYEBXAAIhXwAhQfgn7IxA"));
        assertThat(invokeFunction.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
        assertThat(invokeFunction.getInvocationResult().getGasConsumed(), is("0.0103542"));
        assertThat(invokeFunction.getInvocationResult().getException(),
                is("Specified argument was out of the range of valid values. (Parameter 'index')"));
        StackItem stackItem0 = invokeFunction.getInvocationResult().getStack().get(0);
        assertThat(stackItem0.getType(), is(StackItemType.BUFFER));
        assertThat(stackItem0.getString(), is("transfer"));
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
                        "            \"interfaces\": []\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"RocksDBStore\",\n" +
                        "            \"version\": \"3.0.0.0\",\n" +
                        "            \"interfaces\": []\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"RpcNep17Tracker\",\n" +
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
        assertThat(NodePluginType.valueOfName(plugin.getName()), is(NodePluginType.APPLICATION_LOGS));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(1));
        assertThat(plugin.getInterfaces(), containsInAnyOrder("IPersistencePlugin"));

        plugin = listPlugins.getPlugins().get(1);
        assertThat(NodePluginType.valueOfName(plugin.getName()), is(NodePluginType.LEVEL_DB_STORE));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(0));

        plugin = listPlugins.getPlugins().get(2);
        assertThat(NodePluginType.valueOfName(plugin.getName()), is(NodePluginType.ROCKS_DB_STORE));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(0));

        plugin = listPlugins.getPlugins().get(3);
        assertThat(NodePluginType.valueOfName(plugin.getName()), is(NodePluginType.RPC_NEP17_TRACKER));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(1));
        assertThat(plugin.getInterfaces(), containsInAnyOrder("IPersistencePlugin"));

        plugin = listPlugins.getPlugins().get(4);
        assertThat(NodePluginType.valueOfName(plugin.getName()), is(NodePluginType.RPC_SERVER_PLUGIN));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(0));

        plugin = listPlugins.getPlugins().get(5);
        assertThat(NodePluginType.valueOfName(plugin.getName()), is(NodePluginType.STATES_DUMPER));
        assertThat(plugin.getVersion(), is("3.0.0.0"));
        assertThat(plugin.getInterfaces(), is(notNullValue()));
        assertThat(plugin.getInterfaces(), hasSize(1));
        assertThat(plugin.getInterfaces(), containsInAnyOrder("IPersistencePlugin"));

        plugin = listPlugins.getPlugins().get(6);
        assertThat(NodePluginType.valueOfName(plugin.getName()), is(NodePluginType.SYSTEM_LOG));
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
    public void testGetWalletBalance() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"balance\": \"200\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetWalletBalance getBalance = deserialiseResponse(NeoGetWalletBalance.class);
        assertThat(getBalance.getWalletBalance(), is(notNullValue()));
        assertThat(getBalance.getWalletBalance().getBalance(), is("200"));
    }

    @Test
    public void testGetWalletBalance_UpperCase() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"Balance\": \"199999990.0\"\n"
                        + "  }\n"
                        + "}"
        );

        NeoGetWalletBalance getBalance = deserialiseResponse(NeoGetWalletBalance.class);
        assertThat(getBalance.getWalletBalance(), is(notNullValue()));
        assertThat(getBalance.getWalletBalance().getBalance(), is("199999990.0"));
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
        buildResponse("{\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"id\": 1,\n"
                        + "  \"result\": {\n"
                        + "    \"unclaimed\": \"79199824176\",\n"
                        + "    \"address\": \"AGZLEiwUyCC4wiL5sRZA3LbxWPs9WrZeyN\"\n"
                        + "  }\n"
                        + "}"
        );

        NeoGetUnclaimedGas getUnclaimedGas = deserialiseResponse(NeoGetUnclaimedGas.class);
        assertThat(getUnclaimedGas.getUnclaimedGas().getUnclaimed(), is("79199824176"));
        assertThat(getUnclaimedGas.getUnclaimedGas().getAddress(), is("AGZLEiwUyCC4wiL5sRZA3LbxWPs9WrZeyN"));
    }

    @Test
    public void testGetWalletUnclaimedGas() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": \"289799420400\"\n" +
                        "}"
        );

        NeoGetWalletUnclaimedGas getUnclaimedGas = deserialiseResponse(NeoGetWalletUnclaimedGas.class);
        assertThat(getUnclaimedGas.getWalletUnclaimedGas(), is(notNullValue()));
        assertThat(getUnclaimedGas.getWalletUnclaimedGas(), is("289799420400"));
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
                        "        \"signers\": [\n" +
                        "            {\n" +
                        "                \"account\": \"0xf68f181731a47036a99f04dad90043a744edec0f\",\n" +
                        "                \"scopes\": \"CalledByEntry\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"attributes\": []," +
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
                is(new Hash256("0x6818f446c2e503998ac766a8a175f86d9a89885423f6b055aa123c984625833e")));
        assertThat(sendFrom.getSendFrom().getSize(), is(266L));
        assertThat(sendFrom.getSendFrom().getVersion(), is(0));
        assertThat(sendFrom.getSendFrom().getNonce(), is(1762654532L));
        assertThat(sendFrom.getSendFrom().getSender(), is("AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4"));
        assertThat(sendFrom.getSendFrom().getSysFee(), is("9007810"));
        assertThat(sendFrom.getSendFrom().getNetFee(), is("1266450"));
        assertThat(sendFrom.getSendFrom().getValidUntilBlock(), is(2106392L));

        assertThat(sendFrom.getSendFrom().getSigners(), is(notNullValue()));
        assertThat(sendFrom.getSendFrom().getSigners(), hasSize(1));
        assertThat(sendFrom.getSendFrom().getSigners().get(0).getAccount(),
                is(new Hash160("0xf68f181731a47036a99f04dad90043a744edec0f")));
        assertThat(sendFrom.getSendFrom().getSigners().get(0).getScopes(), hasSize(1));
        assertThat(sendFrom.getSendFrom().getSigners().get(0).getScopes().get(0), is(WitnessScope.CALLED_BY_ENTRY));

        assertThat(sendFrom.getSendFrom().getAttributes(), is(notNullValue()));
        assertThat(sendFrom.getSendFrom().getAttributes(), hasSize(0));

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
                        "        \"signers\": [\n" +
                        "            {\n" +
                        "                \"account\": \"0xbe175fb771d5782282b7598b56c26a2f5ebf2d24\",\n" +
                        "                \"scopes\": \"CalledByEntry\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"account\": \"0xf68f181731a47036a99f04dad90043a744edec0f\",\n" +
                        "                \"scopes\": \"CalledByEntry\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"attributes\": []," +
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
                is(new Hash256("0xf60ec3b0810fb8c17a9a05eaeb3b361ead889a38d3fd1bf2d561a6e7001bb2f5")));
        assertThat(sendMany.getSendMany().getSize(), is(352L));
        assertThat(sendMany.getSendMany().getVersion(), is(0));
        assertThat(sendMany.getSendMany().getNonce(), is(1256822346L));
        assertThat(sendMany.getSendMany().getSender(), is("AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4"));
        assertThat(sendMany.getSendMany().getSysFee(), is("18015620"));
        assertThat(sendMany.getSendMany().getNetFee(), is("1352450"));
        assertThat(sendMany.getSendMany().getValidUntilBlock(), is(2106840L));

        assertThat(sendMany.getSendMany().getSigners(), is(notNullValue()));
        assertThat(sendMany.getSendMany().getSigners(), hasSize(2));
        assertThat(sendMany.getSendMany().getSigners().get(0).getAccount(),
                is(new Hash160("0xbe175fb771d5782282b7598b56c26a2f5ebf2d24")));
        assertThat(sendMany.getSendMany().getSigners().get(0).getScopes(), hasSize(1));
        assertThat(sendMany.getSendMany().getSigners().get(0).getScopes().get(0), is(WitnessScope.CALLED_BY_ENTRY));
        assertThat(sendMany.getSendMany().getSigners(),
                containsInAnyOrder(
                        new TransactionSigner(
                                new Hash160("0xbe175fb771d5782282b7598b56c26a2f5ebf2d24"),
                                asList(WitnessScope.CALLED_BY_ENTRY)),
                        new TransactionSigner(
                                new Hash160("0xf68f181731a47036a99f04dad90043a744edec0f"),
                                asList(WitnessScope.CALLED_BY_ENTRY))
                ));

        assertThat(sendMany.getSendMany().getAttributes(), is(notNullValue()));
        assertThat(sendMany.getSendMany().getAttributes(), hasSize(0));

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
                        "        \"signers\": [\n" +
                        "            {\n" +
                        "                \"account\": \"0xf68f181731a47036a99f04dad90043a744edec0f\",\n" +
                        "                \"scopes\": \"CalledByEntry\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"attributes\": []," +
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
                is(new Hash256("0xabd78548399bbe684fac50b6a71d0ce3f689497d4e79cb26a2b4dfb211782c39")));
        assertThat(sendToAddress.getSendToAddress().getSize(), is(375L));
        assertThat(sendToAddress.getSendToAddress().getVersion(), is(0));
        assertThat(sendToAddress.getSendToAddress().getNonce(), is(1509730265L));
        assertThat(sendToAddress.getSendToAddress().getSender(), is("AK5AmzrrM3sw3kbCHXpHNeuK3kkjnneUrb"));
        assertThat(sendToAddress.getSendToAddress().getSysFee(), is("9007810"));
        assertThat(sendToAddress.getSendToAddress().getNetFee(), is("2375840"));
        assertThat(sendToAddress.getSendToAddress().getValidUntilBlock(), is(2106930L));

        assertThat(sendToAddress.getSendToAddress().getSigners(), is(notNullValue()));
        assertThat(sendToAddress.getSendToAddress().getSigners(), hasSize(1));
        assertThat(sendToAddress.getSendToAddress().getSigners().get(0).getAccount(),
                is(new Hash160("0xf68f181731a47036a99f04dad90043a744edec0f")));
        assertThat(sendToAddress.getSendToAddress().getSigners().get(0).getScopes(), hasSize(1));
        assertThat(sendToAddress.getSendToAddress().getSigners().get(0).getScopes().get(0),
                is(WitnessScope.CALLED_BY_ENTRY));
        assertThat(sendToAddress.getSendToAddress().getAttributes(), is(notNullValue()));
        assertThat(sendToAddress.getSendToAddress().getAttributes(), hasSize(0));
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

    // TokenTracker: Nep17

    @Test
    public void testGetNep17Transfers() {
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

        NeoGetNep17Transfers getNep17Transfers = deserialiseResponse(NeoGetNep17Transfers.class);

        List<NeoGetNep17Transfers.Nep17Transfer> sent = getNep17Transfers.getNep17Transfers().getSent();

        assertThat(sent, is(notNullValue()));
        assertThat(sent, hasSize(2));
        assertThat(sent,
                containsInAnyOrder(
                        new NeoGetNep17Transfers.Nep17Transfer(
                                1554283931L,
                                new Hash160("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3"),
                                "AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis",
                                new BigInteger("100000000000"),
                                368082L,
                                0L,
                                new Hash256("240ab1369712ad2782b99a02a8f9fcaa41d1e96322017ae90d0449a3ba52a564")
                        ),
                        new NeoGetNep17Transfers.Nep17Transfer(
                                1554880287L,
                                new Hash160("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3"),
                                "AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis",
                                new BigInteger("100000000000"),
                                397769L,
                                0L,
                                new Hash256("12fdf7ce8b2388d23ab223854cb29e5114d8288c878de23b7924880f82dfc834")
                        )
                ));

        List<NeoGetNep17Transfers.Nep17Transfer> received = getNep17Transfers.getNep17Transfers().getReceived();

        assertThat(received, is(notNullValue()));
        assertThat(received, hasSize(1));
        assertThat(received,
                hasItem(
                        new NeoGetNep17Transfers.Nep17Transfer(
                                1555651816L,
                                new Hash160("600c4f5200db36177e3e8a09e9f18e2fc7d12a0f"),
                                "AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis",
                                new BigInteger("1000000"),
                                436036L,
                                0L,
                                new Hash256("df7683ece554ecfb85cf41492c5f143215dd43ef9ec61181a28f922da06aba58")
                        )
                ));

        // First Sent Entry
        assertThat(sent.get(0).getTimestamp(), is(1554283931L));
        assertThat(sent.get(0).getAssetHash(), is(new Hash160("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3")));
        assertThat(sent.get(0).getTransferAddress(), is("AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis"));

        // Second Sent Entry
        assertThat(sent.get(1).getAmount(), is(new BigInteger("100000000000")));
        assertThat(sent.get(1).getBlockIndex(), is(397769L));

        // Received Entry
        assertThat(received.get(0).getTransferNotifyIndex(), is(0L));
        assertThat(received.get(0).getTxHash(),
                is(new Hash256("df7683ece554ecfb85cf41492c5f143215dd43ef9ec61181a28f922da06aba58")));
    }

    @Test
    public void testGetNep17Balances() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"address\": \"NXXazKH39yNFWWZF5MJ8tEN98VYHwzn7g3\",\n" +
                        "        \"balance\": [\n" +
                        "            {\n" +
                        "                \"assethash\": \"a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8\",\n" +
                        "                \"name\": \"SomeToken\",\n" +
                        "                \"symbol\": \"SOTO\",\n" +
                        "                \"decimals\": \"4\",\n" +
                        "                \"amount\": \"50000000000\",\n" +
                        "                \"lastupdatedblock\": 251604\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"assethash\": \"1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3\",\n" +
                        "                \"name\": \"RandomToken\",\n" +
                        "                \"symbol\": \"RATO\",\n" +
                        "                \"decimals\": \"2\",\n" +
                        "                \"amount\": \"100000000\",\n" +
                        "                \"lastupdatedblock\": 251600\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetNep17Balances getNep17Balances = deserialiseResponse(NeoGetNep17Balances.class);

        assertThat(getNep17Balances.getBalances().getAddress(), is(notNullValue()));
        assertThat(getNep17Balances.getBalances().getAddress(), is("NXXazKH39yNFWWZF5MJ8tEN98VYHwzn7g3"));

        List<NeoGetNep17Balances.Nep17Balance> balanceList = getNep17Balances.getBalances().getBalances();
        assertThat(balanceList, is(notNullValue()));
        assertThat(balanceList, hasSize(2));
        assertThat(balanceList,
                containsInAnyOrder(
                        new NeoGetNep17Balances.Nep17Balance(
                                new Hash160("a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8"),
                                "SomeToken",
                                "SOTO",
                                "4",
                                "50000000000",
                                BigInteger.valueOf(251604L)
                        ),
                        new NeoGetNep17Balances.Nep17Balance(
                                new Hash160("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3"),
                                "RandomToken",
                                "RATO",
                                "2",
                                "100000000",
                                BigInteger.valueOf(251600L)
                        )
                ));

        // First Entry
        NeoGetNep17Balances.Nep17Balance firstBalance = balanceList.get(0);
        assertThat(firstBalance.getName(), is("SomeToken"));
        assertThat(firstBalance.getSymbol(), is("SOTO"));
        assertThat(firstBalance.getDecimals(), is("4"));
        assertThat(firstBalance.getAssetHash(), is(new Hash160("a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8")));
        assertThat(firstBalance.getAmount(), is("50000000000"));
        assertThat(firstBalance.getLastUpdatedBlock(), is(BigInteger.valueOf(251604L)));

        // Second Entry
        NeoGetNep17Balances.Nep17Balance secondBalance = balanceList.get(1);
        assertThat(secondBalance.getName(), is("RandomToken"));
        assertThat(secondBalance.getSymbol(), is("RATO"));
        assertThat(secondBalance.getDecimals(), is("2"));
        assertThat(secondBalance.getAssetHash(), is(new Hash160("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3")));
        assertThat(secondBalance.getAmount(), is("100000000"));
        assertThat(secondBalance.getLastUpdatedBlock(), is(BigInteger.valueOf(251600L)));
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
                        "        \"executions\": [\n" +
                        "            {\n" +
                        "                \"trigger\": \"Application\",\n" +
                        "                \"vmstate\": \"HALT\",\n" +
                        "                \"exception\": \"asdf\",\n" +
                        "                \"gasconsumed\": \"9007810\",\n" +
                        "                \"stack\": [\n" +
                        "                    {\n" +
                        "                        \"type\": \"Integer\",\n" +
                        "                        \"value\": \"1\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"notifications\": [\n" +
                        "                    {\n" +
                        "                        \"contract\": \"0x70e2301955bf1e74cbb31d18c2f96972abadb328\",\n" +
                        "                        \"eventname\": \"Transfer\",\n" +
                        "                        \"state\": {\n" +
                        "                            \"type\": \"Array\",\n" +
                        "                            \"value\": [\n" +
                        "                                {\n" +
                        "                                    \"type\": \"Any\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"type\": \"ByteString\",\n" +
                        "                                    \"value\": \"ev0gMlXLKXK9CmqCfnTjh+0yK+w=\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"type\": \"Integer\",\n" +
                        "                                    \"value\": \"600000000\"\n" +
                        "                                }\n" +
                        "                            ]\n" +
                        "                        }\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"contract\": \"0xf61eebf573ea36593fd43aa150c055ad7906ab83\",\n" +
                        "                        \"eventname\": \"Transfer\",\n" +
                        "                        \"state\": {\n" +
                        "                            \"type\": \"Array\",\n" +
                        "                            \"value\": [\n" +
                        "                                {\n" +
                        "                                    \"type\": \"ByteString\",\n" +
                        "                                    \"value\": \"VHJhbnNmZXI=\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"type\": \"ByteString\",\n" +
                        "                                    \"value\": \"CaVYdMLaS4bl1J/1MKGxU+sSx9Y=\"\n" +
                        "                                },\n" +
                        "                                {\n" +
                        "                                    \"type\": \"Integer\",\n" +
                        "                                    \"value\": \"100\"\n" +
                        "                                }\n" +
                        "                            ]\n" +
                        "                        }\n" +
                        "                    }\n" +
                        "                ]\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetApplicationLog getApplicationLog = deserialiseResponse(NeoGetApplicationLog.class);
        NeoApplicationLog neoAppLog = getApplicationLog.getApplicationLog();
        assertThat(neoAppLog, is(notNullValue()));

        assertThat(neoAppLog.getTransactionId(),
                is(new Hash256("0x01bcf2edbd27abb8d660b6a06113b84d02f635fed836ce46a38b4d67eae80109")));

        assertThat(neoAppLog.getExecutions(), hasSize(1));
        NeoApplicationLog.Execution execution = neoAppLog.getExecutions().get(0);
        assertThat(execution.getTrigger(), is("Application"));
        assertThat(execution.getState(), is(NeoVMStateType.HALT));
        assertThat(execution.getGasConsumed(), is("9007810"));

        assertThat(execution.getStack(), is(notNullValue()));
        assertThat(execution.getStack(), hasSize(1));
        assertThat(execution.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(execution.getStack().get(0).getInteger(), is(BigInteger.valueOf(1)));

        assertThat(execution.getNotifications(), is(notNullValue()));
        assertThat(execution.getNotifications(), hasSize(2));

        // Notification 0
        Notification notification0 = execution.getNotifications().get(0);

        assertThat(notification0.getContract(), is(new Hash160("0x70e2301955bf1e74cbb31d18c2f96972abadb328")));
        assertThat(notification0.getState().getType(), is(StackItemType.ARRAY));
        assertThat(notification0.getEventName(), is("Transfer"));

        List<StackItem> notification0Array = notification0.getState().getList();

        StackItem from0 = notification0Array.get(0);
        String to0 = notification0Array.get(1).getAddress();
        BigInteger amount0 = notification0Array.get(2).getInteger();

        assertNotNull(from0);
        assertThat(to0, is("NX8GreRFGFK5wpGMWetpX93HmtrezGogzk"));
        assertThat(amount0, is(BigInteger.valueOf(600000000)));

        // Notification 1
        Notification notification1 = execution.getNotifications().get(1);

        assertThat(notification1.getContract(), is(new Hash160("0xf61eebf573ea36593fd43aa150c055ad7906ab83")));
        assertThat(notification1.getState().getType(), is(StackItemType.ARRAY));
        assertThat(notification1.getEventName(), is("Transfer"));

        List<StackItem> notification1Array = notification1.getState().getList();

        String eventName1 = notification1Array.get(0).getString();
        String from1 = notification1Array.get(1).getAddress();
        BigInteger amount1 = notification1Array.get(2).getInteger();

        assertThat(eventName1, is("Transfer"));
        assertThat(from1, is("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke"));
        assertThat(amount1, is(BigInteger.valueOf(100)));
    }

    // StateService

    @Test
    public void testGetStateRoot() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"result\": {\n" +
                "        \"version\": 0,\n" +
                "        \"index\": 160,\n" +
                "        \"roothash\": \"0x28870d1ed61ef167e99354249c622504b0d81d814eaa87dbf8612c91b9b303b7\",\n" +
                "        \"witnesses\": [\n" +
                "            {\n" +
                "                \"invocation\": \"DEDN8o6cmOUt/pfRIexVzO2shhX2vTYFd+cU8vZDQ2Dvn3pe/vHcYOSlY3lPRKecb5zBuLCqaKSvZsC1FAbT00dWDEDoPojyFw66R+pKQsOy0MFmeBBgaC6Z1XGLAigVDHi2VuhAxfpwFpXSTUv3Uv5cIOY+V5g40+2zpU19YQIAWyOJDEDPfitQTjK90KnrloPXKvgTNFPn1520dxDCzQxhl/Wfp7S8dW91/3x3GrF1EaIi32aJtF8W8jUH1Spr/ma66ISs\",\n" +
                "                \"verification\": \"EwwhAwAqLhjDnN7Qb8Yd2UoHuOnz+gNqcFvu+HZCUpVOgtDXDCECAM1gQDlYokm5qzKbbAjI/955zDMJc2eji/a1GIEJU2EMIQKXhyDsbFxYdeA0d+FsbZj5AQhamA13R64ysGgh19j6UwwhA8klCeQozdf3pP3UqXxniRC0DxRl3d5PBJ9zJa8zgHkpFAtBE43vrw==\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}"
        );

        NeoGetStateRoot neoGetStateRoot = deserialiseResponse(NeoGetStateRoot.class);
        NeoGetStateRoot.StateRoot stateRoot = neoGetStateRoot.getStateRoot();

        assertThat(stateRoot.getVersion(), is(0));
        assertThat(stateRoot.getIndex(), is(160L));
        assertThat(stateRoot.getRootHash(),
                is(new Hash256("0x28870d1ed61ef167e99354249c622504b0d81d814eaa87dbf8612c91b9b303b7")));
        assertNotNull(stateRoot.getWitnesses());
        assertThat(stateRoot.getWitnesses(), hasSize(1));
        assertThat(stateRoot.getWitnesses().get(0).getInvocation(),
                is("DEDN8o6cmOUt/pfRIexVzO2shhX2vTYFd+cU8vZDQ2Dvn3pe/vHcYOSlY3lPRKecb5zBuLCqaKSvZsC1FAbT00dWDEDoPojyFw66R+pKQsOy0MFmeBBgaC6Z1XGLAigVDHi2VuhAxfpwFpXSTUv3Uv5cIOY+V5g40+2zpU19YQIAWyOJDEDPfitQTjK90KnrloPXKvgTNFPn1520dxDCzQxhl/Wfp7S8dW91/3x3GrF1EaIi32aJtF8W8jUH1Spr/ma66ISs"));
        assertThat(stateRoot.getWitnesses().get(0).getVerification(),
                is("EwwhAwAqLhjDnN7Qb8Yd2UoHuOnz+gNqcFvu+HZCUpVOgtDXDCECAM1gQDlYokm5qzKbbAjI/955zDMJc2eji/a1GIEJU2EMIQKXhyDsbFxYdeA0d+FsbZj5AQhamA13R64ysGgh19j6UwwhA8klCeQozdf3pP3UqXxniRC0DxRl3d5PBJ9zJa8zgHkpFAtBE43vrw=="));
    }

    @Test
    public void testGetProof() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"result\": \"Bfv///8XBiQBAQ8DRzb6Vkdw0r5nxMBp6Z5nvbyXiupMvffwm0v5GdB6jHvyAAQEBAQEBAQEA7l84HFtRI5V11s58vA+8CZ5GArFLkGUYLO98RLaMaYmA5MEnx0upnVI45XTpoUDRvwrlPD59uWy9aIrdS4T0D2cA6Rwv/l3GmrctRzL1me+iTUFdDgooaz+esFHFXJdDANfA2bdshZMp5ox2goVAOMjvoxNIWWOqjJoRPu6ZOw2kdj6A8xovEK1Mp6cAG9z/jfFDrSEM60kuo97MNaVOP/cDZ1wA1nf4WdI+jksYz0EJgzBukK8rEzz8jE2cb2Zx2fytVyQBANC7v2RaLMCRF1XgLpSri12L2IwL9Zcjz5LZiaB5nHKNgQpAQYPDw8PDw8DggFffnsVMyqAfZjg+4gu97N/gKpOsAK8Q27s56tijRlSAAMm26DYxOdf/IjEgkE/u/CoRL6dDnzvs1dxCg/00esMvgPGioeOqQCkDOTfliOnCxYjbY/0XvVUOXkceuDm1W0FzQQEBAQEBAQEBAQEBAQEBJIABAPH1PnX/P8NOgV4KHnogwD7xIsD8KvNhkTcDxgCo7Ec6gPQs1zD4igSJB4M9jTREq+7lQ5PbTH/6d138yUVvtM8bQP9Df1kh7asXrYjZolKhLcQ1NoClQgEzbcJfYkCHXv6DQQEBAOUw9zNl/7FJrWD7rCv0mbOoy6nLlHWiWuyGsA12ohRuAQEBAQEBAQEBAYCBAIAAgA=\"\n" +
                "}"
        );

        NeoGetProof neoGetProof = deserialiseResponse(NeoGetProof.class);

        assertThat(neoGetProof.getProof(),
                is("Bfv///8XBiQBAQ8DRzb6Vkdw0r5nxMBp6Z5nvbyXiupMvffwm0v5GdB6jHvyAAQEBAQEBAQEA7l84HFtRI5V11s58vA+8CZ5GArFLkGUYLO98RLaMaYmA5MEnx0upnVI45XTpoUDRvwrlPD59uWy9aIrdS4T0D2cA6Rwv/l3GmrctRzL1me+iTUFdDgooaz+esFHFXJdDANfA2bdshZMp5ox2goVAOMjvoxNIWWOqjJoRPu6ZOw2kdj6A8xovEK1Mp6cAG9z/jfFDrSEM60kuo97MNaVOP/cDZ1wA1nf4WdI+jksYz0EJgzBukK8rEzz8jE2cb2Zx2fytVyQBANC7v2RaLMCRF1XgLpSri12L2IwL9Zcjz5LZiaB5nHKNgQpAQYPDw8PDw8DggFffnsVMyqAfZjg+4gu97N/gKpOsAK8Q27s56tijRlSAAMm26DYxOdf/IjEgkE/u/CoRL6dDnzvs1dxCg/00esMvgPGioeOqQCkDOTfliOnCxYjbY/0XvVUOXkceuDm1W0FzQQEBAQEBAQEBAQEBAQEBJIABAPH1PnX/P8NOgV4KHnogwD7xIsD8KvNhkTcDxgCo7Ec6gPQs1zD4igSJB4M9jTREq+7lQ5PbTH/6d138yUVvtM8bQP9Df1kh7asXrYjZolKhLcQ1NoClQgEzbcJfYkCHXv6DQQEBAOUw9zNl/7FJrWD7rCv0mbOoy6nLlHWiWuyGsA12ohRuAQEBAQEBAQEBAYCBAIAAgA="));
    }

    @Test
    public void testVerifyProof() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": \"QAFBAighAhY5RqEz49Lg2Yf7kMsBsGDtF4DxcY4too7fE7ll/StgIQA=\"\n" +
                "}"
        );

        NeoVerifyProof proof = deserialiseResponse(NeoVerifyProof.class);

        assertThat(proof.verifyProof(), is("QAFBAighAhY5RqEz49Lg2Yf7kMsBsGDtF4DxcY4too7fE7ll/StgIQA="));
    }

    @Test
    public void testGetStateHeight() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": {\n" +
                "        \"localrootindex\": 212,\n" +
                "        \"validatedrootindex\": 211\n" +
                "    }\n" +
                "}"
        );

        NeoGetStateHeight neoGetStateHeight = deserialiseResponse(NeoGetStateHeight.class);
        NeoGetStateHeight.StateHeight stateHeight = neoGetStateHeight.getStateHeight();

        assertThat(stateHeight.getLocalRootIndex(), is(212L));
        assertThat(stateHeight.getValidatedRootIndex(), is(211L));
    }

    @Test
    public void testGetState() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": \"QQEhBwAA1VhfeRI=\"\n" +
                "}"
        );

        NeoGetState neoGetState = deserialiseResponse(NeoGetState.class);
        String state = neoGetState.getState();

        assertThat(state, is("QQEhBwAA1VhfeRI="));
    }

    @Test
    public void testFindStates() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": {\n" +
                "        \"firstProof\": \"Gfr///8UDRZcmJnDi79ZkcXkewSTcljK7GkIJAEBDwOakA9CYtxDPpx00gKk0RCLmrtNtpTsY2rXB/RqfGHIPLIABAQEBAQEBAMe+jlFz2/5ZKl+ycxczvmS75mO9ssmFZef+WHov7XIHQQDiT6zHh/siCZ0c2bfBEymPmRNTiXSAKFIammjmnnBnJYDh0IX5YfZdqNZkfFN/6VaLZ6kX+N+bBGdlNVUyP7pwJ4DrpFUvhWA+kXVxDLE8qKtLcQimKQY1RcWw14bsjURuRYEBAQDsyA6/WuQyV98xH99kDVz3bhQHmUNBIQqJd0x0R/+TGwEKQEGDw8PDw8PAzKhCJmqIIilFwEfMQJDUEMXInq+AbRk8Jfnoi1weu8aUgADo6udX84sFVzKZLdtwtJ6TIMgQOrYZQ+7yKG+5TlliscDzboXdiwLKASBJeAVtNTl7NHqclD6UBe4XrwJQQYJIDQEBAQEBAQEBAQEBAQEBAQkAQEEA6pd1tKBerO8Qub4cvuKEpXDlGCJsktZ4Vk0xT+D6Av5UgADBr2ExYHjKsB15w2Ra40oWm7iPwdhWEVf6nHV6St/W8gEBAQEBAQDufefqjG8jPxPHOFpyF8LE16aXEzlFeuts4vaQ+wGCL4EBAQEBAQEBARKAScNAQYFDAkICQkMAwgLCw8FCQkBDAUOBAcLAAQJAwcCBQgMCg4MBgkDzXuGD6B7eZe7+IxNOv1j48vZn5A9qz4nzzvdSqSQRr8LAglBASEFACrPdwI=\",\n" +
                "        \"lastProof\": \"Gfr///8Uf2XUNDYnCLJV8OBoVr3LXOmdhQUIJAEBDwOakA9CYtxDPpx00gKk0RCLmrtNtpTsY2rXB/RqfGHIPLIABAQEBAQEBAMe+jlFz2/5ZKl+ycxczvmS75mO9ssmFZef+WHov7XIHQQDiT6zHh/siCZ0c2bfBEymPmRNTiXSAKFIammjmnnBnJYDh0IX5YfZdqNZkfFN/6VaLZ6kX+N+bBGdlNVUyP7pwJ4DrpFUvhWA+kXVxDLE8qKtLcQimKQY1RcWw14bsjURuRYEBAQDsyA6/WuQyV98xH99kDVz3bhQHmUNBIQqJd0x0R/+TGwEKQEGDw8PDw8PAzKhCJmqIIilFwEfMQJDUEMXInq+AbRk8Jfnoi1weu8aUgADo6udX84sFVzKZLdtwtJ6TIMgQOrYZQ+7yKG+5TlliscDzboXdiwLKASBJeAVtNTl7NHqclD6UBe4XrwJQQYJIDQEBAQEBAQEBAQEBAQEBAQkAQEEA6pd1tKBerO8Qub4cvuKEpXDlGCJsktZ4Vk0xT+D6Av5UgADBr2ExYHjKsB15w2Ra40oWm7iPwdhWEVf6nHV6St/W8gEBAQEBAQDufefqjG8jPxPHOFpyF8LE16aXEzlFeuts4vaQ+wGCL4EBAQEBAQEBARKAScPBgUNBAMEAwYCBwAICwIFBQ8ADgAGCAUGCw0MCwUMDgkJDQgFAAUDkvma2Sek54h+A0fdKAxoUjETufDdw3bX/Crnad92qPUNAgtBASEHAADVWF95Eg==\",\n" +
                "        \"truncated\": false,\n" +
                "        \"results\": [\n" +
                "            {\n" +
                "                \"key\": \"FA0WXJiZw4u/WZHF5HsEk3JYyuxp\",\n" +
                "                \"value\": \"QQEhBQAqz3cC\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"key\": \"FH9l1DQ2JwiyVfDgaFa9y1zpnYUF\",\n" +
                "                \"value\": \"QQEhBwAA1VhfeRI=\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}"
        );

        NeoFindStates neoFindStates = deserialiseResponse(NeoFindStates.class);
        NeoFindStates.States states = neoFindStates.getStates();

        String firstProof = "Gfr///8UDRZcmJnDi79ZkcXkewSTcljK7GkIJAEBDwOakA9CYtxDPpx00gKk0RCLmrtNtpTsY2rXB/RqfGHIPLIABAQEBAQEBAMe+jlFz2/5ZKl+ycxczvmS75mO9ssmFZef+WHov7XIHQQDiT6zHh/siCZ0c2bfBEymPmRNTiXSAKFIammjmnnBnJYDh0IX5YfZdqNZkfFN/6VaLZ6kX+N+bBGdlNVUyP7pwJ4DrpFUvhWA+kXVxDLE8qKtLcQimKQY1RcWw14bsjURuRYEBAQDsyA6/WuQyV98xH99kDVz3bhQHmUNBIQqJd0x0R/+TGwEKQEGDw8PDw8PAzKhCJmqIIilFwEfMQJDUEMXInq+AbRk8Jfnoi1weu8aUgADo6udX84sFVzKZLdtwtJ6TIMgQOrYZQ+7yKG+5TlliscDzboXdiwLKASBJeAVtNTl7NHqclD6UBe4XrwJQQYJIDQEBAQEBAQEBAQEBAQEBAQkAQEEA6pd1tKBerO8Qub4cvuKEpXDlGCJsktZ4Vk0xT+D6Av5UgADBr2ExYHjKsB15w2Ra40oWm7iPwdhWEVf6nHV6St/W8gEBAQEBAQDufefqjG8jPxPHOFpyF8LE16aXEzlFeuts4vaQ+wGCL4EBAQEBAQEBARKAScNAQYFDAkICQkMAwgLCw8FCQkBDAUOBAcLAAQJAwcCBQgMCg4MBgkDzXuGD6B7eZe7+IxNOv1j48vZn5A9qz4nzzvdSqSQRr8LAglBASEFACrPdwI=";
        assertThat(states.getFirstProof(), is(firstProof));
        String lastProof = "Gfr///8Uf2XUNDYnCLJV8OBoVr3LXOmdhQUIJAEBDwOakA9CYtxDPpx00gKk0RCLmrtNtpTsY2rXB/RqfGHIPLIABAQEBAQEBAMe+jlFz2/5ZKl+ycxczvmS75mO9ssmFZef+WHov7XIHQQDiT6zHh/siCZ0c2bfBEymPmRNTiXSAKFIammjmnnBnJYDh0IX5YfZdqNZkfFN/6VaLZ6kX+N+bBGdlNVUyP7pwJ4DrpFUvhWA+kXVxDLE8qKtLcQimKQY1RcWw14bsjURuRYEBAQDsyA6/WuQyV98xH99kDVz3bhQHmUNBIQqJd0x0R/+TGwEKQEGDw8PDw8PAzKhCJmqIIilFwEfMQJDUEMXInq+AbRk8Jfnoi1weu8aUgADo6udX84sFVzKZLdtwtJ6TIMgQOrYZQ+7yKG+5TlliscDzboXdiwLKASBJeAVtNTl7NHqclD6UBe4XrwJQQYJIDQEBAQEBAQEBAQEBAQEBAQkAQEEA6pd1tKBerO8Qub4cvuKEpXDlGCJsktZ4Vk0xT+D6Av5UgADBr2ExYHjKsB15w2Ra40oWm7iPwdhWEVf6nHV6St/W8gEBAQEBAQDufefqjG8jPxPHOFpyF8LE16aXEzlFeuts4vaQ+wGCL4EBAQEBAQEBARKAScPBgUNBAMEAwYCBwAICwIFBQ8ADgAGCAUGCw0MCwUMDgkJDQgFAAUDkvma2Sek54h+A0fdKAxoUjETufDdw3bX/Crnad92qPUNAgtBASEHAADVWF95Eg==";
        assertThat(states.getLastProof(), is(lastProof));
        assertFalse(states.isTruncated());
        List<NeoFindStates.States.Result> results = states.getResults();
        assertThat(results, hasSize(2));
        String key1 = "FA0WXJiZw4u/WZHF5HsEk3JYyuxp";
        String value1 = "QQEhBQAqz3cC";
        String key2 = "FH9l1DQ2JwiyVfDgaFa9y1zpnYUF";
        String value2 = "QQEhBwAA1VhfeRI=";
        assertThat(results.get(0).getKey(), is(key1));
        assertThat(results.get(0).getValue(), is(value1));

        NeoFindStates.States.Result expectedResult1 = new NeoFindStates.States.Result(key1, value1);
        NeoFindStates.States.Result expectedResult2 = new NeoFindStates.States.Result(key2, value2);
        List<NeoFindStates.States.Result> expectedResults = asList(expectedResult1, expectedResult2);
        NeoFindStates.States expectedStates = new NeoFindStates.States(firstProof, lastProof, false, expectedResults);
        assertThat(states, is(expectedStates));
    }

    @Test
    public void testFindStates_singleResult() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": {\n" +
                "        \"firstProof\": \"Bfr///8LBiQBAQ8DqDawCFNqYkkQC+no3z6WbmuP8DJmy9e4MMK+QzHITdGyAAQEBAQEBAQDHvo5Rc9v+WSpfsnMXM75ku+ZjvbLJhWXn/lh6L+1yB0EA4k+sx4f7IgmdHNm3wRMpj5kTU4l0gChSGppo5p5wZyWA7QRkH8fw1R6WnCQfRWk96ZKPBPSeOU+gvwQuwjznHjfA66RVL4VgPpF1cQyxPKirS3EIpikGNUXFsNeG7I1EbkWBAQEA7MgOv1rkMlffMR/fZA1c924UB5lDQSEKiXdMdEf/kxsBCkBBg8PDw8PDwMJqhMyRWjael2lcsob2BXims/yMjMrrSkkWY/MsReC7lIAAzP6dmF3DTZHkfcXYHO6On6KQucSwUv9UryMqImoBKrLA27ebHC45rpr3EGcLJ7D7EAm/JihcES3pIzYVxgh6hSrBAQEBAQEBAQEBAQEBAQEJAEBCwMWm2J/uEa8sf+ET9RUiBXqOLuLQ/dr4V494mGlwcp9DAkCBwCY0uJieRI=\",\n" +
                "        \"truncated\": true,\n" +
                "        \"results\": [\n" +
                "            {\n" +
                "                \"key\": \"Cw==\",\n" +
                "                \"value\": \"AJjS4mJ5Eg==\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}"
        );

        NeoFindStates neoFindStates = deserialiseResponse(NeoFindStates.class);
        NeoFindStates.States states = neoFindStates.getStates();

        assertThat(states.getFirstProof(), is("Bfr///8LBiQBAQ8DqDawCFNqYkkQC+no3z6WbmuP8DJmy9e4MMK+QzHITdGyAAQEBAQEBAQDHvo5Rc9v+WSpfsnMXM75ku+ZjvbLJhWXn/lh6L+1yB0EA4k+sx4f7IgmdHNm3wRMpj5kTU4l0gChSGppo5p5wZyWA7QRkH8fw1R6WnCQfRWk96ZKPBPSeOU+gvwQuwjznHjfA66RVL4VgPpF1cQyxPKirS3EIpikGNUXFsNeG7I1EbkWBAQEA7MgOv1rkMlffMR/fZA1c924UB5lDQSEKiXdMdEf/kxsBCkBBg8PDw8PDwMJqhMyRWjael2lcsob2BXims/yMjMrrSkkWY/MsReC7lIAAzP6dmF3DTZHkfcXYHO6On6KQucSwUv9UryMqImoBKrLA27ebHC45rpr3EGcLJ7D7EAm/JihcES3pIzYVxgh6hSrBAQEBAQEBAQEBAQEBAQEJAEBCwMWm2J/uEa8sf+ET9RUiBXqOLuLQ/dr4V494mGlwcp9DAkCBwCY0uJieRI="));
        assertNull(states.getLastProof());
        assertThat(states.getResults(), hasSize(1));
    }

    @Test
    public void testFindStates_emptyResults() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": {\n" +
                "        \"truncated\": true,\n" +
                "        \"results\": []\n" +
                "    }\n" +
                "}"
        );

        NeoFindStates neoFindStates = deserialiseResponse(NeoFindStates.class);
        NeoFindStates.States states = neoFindStates.getStates();

        assertNull(states.getFirstProof());
        assertNull(states.getLastProof());
        assertTrue(states.isTruncated());
        assertThat(states.getResults(), hasSize(0));
    }

    // Neo-express related tests

    @Test
    public void testExpressGetPopulatedBlocks() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": {\n" +
                "        \"cacheId\": \"637613615288087170\",\n" +
                "        \"blocks\": [\n" +
                "            1129,\n" +
                "            1127,\n" +
                "            0\n" +
                "        ]\n" +
                "    }\n" +
                "}"
        );

        NeoExpressGetPopulatedBlocks expressGetPopulatedBlocks =
                deserialiseResponse(NeoExpressGetPopulatedBlocks.class);
        PopulatedBlocks populatedBlocks = expressGetPopulatedBlocks.getPopulatedBlocks();

        assertThat(populatedBlocks.getCacheId(), is("637613615288087170"));
        assertThat(populatedBlocks.getBlocks(), hasSize(3));
        assertThat(populatedBlocks.getBlocks().get(0), is(1129));
        assertThat(populatedBlocks.getBlocks().get(1), is(1127));
        assertThat(populatedBlocks.getBlocks().get(2), is(0));
    }

    @Test
    public void testExpressGetNep17Contracts() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": [\n" +
                "        {\n" +
                "            \"scriptHash\": \"0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5\",\n" +
                "            \"symbol\": \"NEO\",\n" +
                "            \"decimals\": 0\n" +
                "        },\n" +
                "        {\n" +
                "            \"scriptHash\": \"0xd2a4cff31913016155e38e474a2c06d08be276cf\",\n" +
                "            \"symbol\": \"GAS\",\n" +
                "            \"decimals\": 8\n" +
                "        }\n" +
                "    ]\n" +
                "}"
        );

        NeoExpressGetNep17Contracts expressGetNep17Contracts = deserialiseResponse(NeoExpressGetNep17Contracts.class);

        List<Nep17Contract> nep17Contracts = expressGetNep17Contracts.getNep17Contracts();
        assertThat(nep17Contracts, hasSize(2));
        assertThat(nep17Contracts.get(0).getScriptHash(),
                is(new Hash160("0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5")));
        assertThat(nep17Contracts.get(0).getSymbol(), is("NEO"));
        assertThat(nep17Contracts.get(0).getDecimals(), is(0));
        assertThat(nep17Contracts.get(1).getScriptHash(),
                is(new Hash160("0xd2a4cff31913016155e38e474a2c06d08be276cf")));
        assertThat(nep17Contracts.get(1).getSymbol(), is("GAS"));
        assertThat(nep17Contracts.get(1).getDecimals(), is(8));

        assertThat(nep17Contracts.get(0),
                is(new Nep17Contract(new Hash160("0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5"), "NEO", 0)));
    }

    @Test
    public void testGetContractStorage() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": [\n" +
                "        {\n" +
                "            \"key\": \"01\",\n" +
                "            \"value\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"key\": \"0b\",\n" +
                "            \"value\": \"00e1f505\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"key\": \"0d\",\n" +
                "            \"value\": \"00e8764817\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"key\": \"0e\",\n" +
                "            \"value\": \"40014102282102c2f3870c8805f83881e93cddaac2b2130ad4a2ca44a327ac64e18322862b19ee2100\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"key\": \"14b65d362f086196286c2cd6868afbe0cf75f732a3\",\n" +
                "            \"value\": \"4103210400e1f505210000\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"key\": \"1d00000000\",\n" +
                "            \"value\": \"0065cd1d\"\n" +
                "        }\n" +
                "    ]\n" +
                "}"
        );

        NeoExpressGetContractStorage expressGetContractStorage =
                deserialiseResponse(NeoExpressGetContractStorage.class);

        List<ContractStorageEntry> contractStorage = expressGetContractStorage.getContractStorage();
        assertThat(contractStorage, hasSize(6));

        ContractStorageEntry storageEntry3 = contractStorage.get(2);
        assertThat(storageEntry3.getKey(), is("0d"));
        assertThat(storageEntry3.getValue(), is("00e8764817"));

        ContractStorageEntry storageEntry6 = contractStorage.get(5);
        assertThat(storageEntry6.getKey(), is("1d00000000"));
        assertThat(storageEntry6.getValue(), is("0065cd1d"));
    }

    @Test
    public void testExpressCreateCheckpoint() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": \"checkpoint-1.neoxp-checkpoint\"\n" +
                "}"
        );

        NeoExpressCreateCheckpoint expressCreateCheckpoint = deserialiseResponse(NeoExpressCreateCheckpoint.class);

        String filename = expressCreateCheckpoint.getFilename();
        assertThat(filename, is("checkpoint-1.neoxp-checkpoint"));
    }

    @Test
    public void testExpressListOracleRequests() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": [\n" +
                "        {\n" +
                "            \"requestid\": 0,\n" +
                "            \"originaltxid\": \"0x0b2327b9c4a6445a3e1d85ae9f99184a9cf5d7234602be54800057968332180a\",\n" +
                "            \"gasforresponse\": 1000000000,\n" +
                "            \"url\": \"https://www.neow3j.io\",\n" +
                "            \"filter\": \"$.nftinfo\",\n" +
                "            \"callbackcontract\": \"0xf18a0ccda4947ba1cbeaf5a7f579c385ed2cf87f\",\n" +
                "            \"callbackmethod\": \"storeResponse\",\n" +
                "            \"userdata\": \"KAA=\"\n" +
                "        }\n" +
                "    ]\n" +
                "}"
        );

        NeoExpressListOracleRequests neoExpressListOracleRequests =
                deserialiseResponse(NeoExpressListOracleRequests.class);
        List<OracleRequest> oracleRequests = neoExpressListOracleRequests.getOracleRequests();

        assertThat(oracleRequests, hasSize(1));
        OracleRequest expectedRequest = new OracleRequest(BigInteger.ZERO,
                new Hash256("0x0b2327b9c4a6445a3e1d85ae9f99184a9cf5d7234602be54800057968332180a"),
                BigInteger.valueOf(1000000000), "https://www.neow3j.io", "$.nftinfo",
                new Hash160("0xf18a0ccda4947ba1cbeaf5a7f579c385ed2cf87f"), "storeResponse", "KAA=");

        OracleRequest oracleRequest = oracleRequests.get(0);
        assertThat(oracleRequest, is(expectedRequest));
        assertThat(oracleRequest.getRequestId(), is(BigInteger.ZERO));
        assertThat(oracleRequest.getOriginalTransactionHash(),
                is(new Hash256("0x0b2327b9c4a6445a3e1d85ae9f99184a9cf5d7234602be54800057968332180a")));
        assertThat(oracleRequest.getGasForResponse(), is(BigInteger.valueOf(1000000000)));
        assertThat(oracleRequest.getUrl(), is("https://www.neow3j.io"));
        assertThat(oracleRequest.getFilter(), is("$.nftinfo"));
        assertThat(oracleRequest.getCallbackContract(),
                is(new Hash160("0xf18a0ccda4947ba1cbeaf5a7f579c385ed2cf87f")));
        assertThat(oracleRequest.getCallbackMethod(), is("storeResponse"));
        assertThat(oracleRequest.getUserData(), is("KAA="));
    }

    @Test
    public void testExpressCreateOracleResponseTx() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": \"AAAAAAD+KXk7AAAAAAKgIQAAAAAA5BcAAAJYhxcRfgqoEHKvq3HS3Yn+fEuS/gDWpJ16ac8mblfxSXP0i4whCH8cRgABEQAAAAAAAAAAAAZuZW93M2olwh8MBmZpbmlzaAwUWIcXEX4KqBByr6tx0t2J/nxLkv5BYn1bUgIAAAAqEQwhAmB6OLgBCo9AHCXdAd8bdK8YJ90WuCH8B0UfLvfwLaYPEUGe0Nw6\"\n" +
                "}");

        NeoExpressCreateOracleResponseTx neoExpressCreateOracleResponseTx =
                deserialiseResponse(NeoExpressCreateOracleResponseTx.class);
        String oracleResponseTx = neoExpressCreateOracleResponseTx.getOracleResponseTx();
        assertThat(oracleResponseTx, is("AAAAAAD+KXk7AAAAAAKgIQAAAAAA5BcAAAJYhxcRfgqoEHKvq3HS3Yn+fEuS/gDWpJ16ac8mblfxSXP0i4whCH8cRgABEQAAAAAAAAAAAAZuZW93M2olwh8MBmZpbmlzaAwUWIcXEX4KqBByr6tx0t2J/nxLkv5BYn1bUgIAAAAqEQwhAmB6OLgBCo9AHCXdAd8bdK8YJ90WuCH8B0UfLvfwLaYPEUGe0Nw6"));
    }

    @Test
    public void testExpressShutdown() {
        buildResponse("{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"id\": 1,\n" +
                "    \"result\": {\n" +
                "        \"process-id\": 73625\n" +
                "    }\n" +
                "}");

        NeoExpressShutdown neoExpressShutdown = deserialiseResponse(NeoExpressShutdown.class);
        assertThat(neoExpressShutdown.getExpressShutdown().getProcessId(), is(73625));
    }

    // TokenTracker: Nep11

    @Test
    public void testGetNep11Balances() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"address\": \"NXXazKH39yNFWWZF5MJ8tEN98VYHwzn7g3\",\n" +
                        "        \"balance\": [\n" +
                        "            {\n" +
                        "                \"assethash\": \"a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8\",\n" +
                        "                \"name\": \"FunnyCats\",\n" +
                        "                \"symbol\": \"FCS\",\n" +
                        "                \"decimals\": \"0\",\n" +
                        "                \"tokens\": [\n" +
                        "                    {\n" +
                        "                        \"tokenid\": \"1\",\n" +
                        "                        \"amount\": \"1\",\n" +
                        "                        \"lastupdatedblock\": 12345\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"tokenid\": \"2\",\n" +
                        "                        \"amount\": \"1\",\n" +
                        "                        \"lastupdatedblock\": 123456\n" +
                        "                    }\n" +
                        "                ]\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"assethash\": \"1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3\",\n" +
                        "                \"name\": \"CuteNeoKittens\",\n" +
                        "                \"symbol\": \"CNKS\",\n" +
                        "                \"decimals\": \"4\",\n" +
                        "                \"tokens\": [\n" +
                        "                    {\n" +
                        "                        \"tokenid\": \"4\",\n" +
                        "                        \"amount\": \"10000\",\n" +
                        "                        \"lastupdatedblock\": 12345\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"tokenid\": \"10\",\n" +
                        "                        \"amount\": \"6500\",\n" +
                        "                        \"lastupdatedblock\": 654321\n" +
                        "                    }\n" +
                        "                ]\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetNep11Balances getNep11Balances = deserialiseResponse(NeoGetNep11Balances.class);

        assertThat(getNep11Balances.getBalances().getAddress(), is(notNullValue()));
        assertThat(getNep11Balances.getBalances().getAddress(), is("NXXazKH39yNFWWZF5MJ8tEN98VYHwzn7g3"));

        List<NeoGetNep11Balances.Nep11Balance> nep11Balances = getNep11Balances.getBalances().getBalances();
        assertThat(nep11Balances, is(notNullValue()));
        assertThat(nep11Balances, hasSize(2));
        assertThat(nep11Balances,
                containsInAnyOrder(
                        new NeoGetNep11Balances.Nep11Balance(
                                new Hash160("a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8"),
                                "FunnyCats",
                                "FCS",
                                "0",
                                asList(
                                     new NeoGetNep11Balances.Nep11Balance.Nep11Token("1", "1", 12345L),
                                     new NeoGetNep11Balances.Nep11Balance.Nep11Token("2", "1", 123456L)
                                )
                        ),
                        new NeoGetNep11Balances.Nep11Balance(
                                new Hash160("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3"),
                                "CuteNeoKittens",
                                "CNKS",
                                "4",
                                asList(
                                        new Nep11Token("4", "10000", 12345L),
                                        new Nep11Token("10", "6500", 654321L)
                                )
                        )
                ));

        // First Entry
        NeoGetNep11Balances.Nep11Balance firstBalance = nep11Balances.get(0);
        assertThat(firstBalance.getAssetHash(), is(new Hash160("a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8")));
        assertThat(firstBalance.getName(), is("FunnyCats"));
        assertThat(firstBalance.getSymbol(), is("FCS"));
        assertThat(firstBalance.getDecimals(), is("0"));
        assertThat(firstBalance.getTokens().get(0).getTokenId(), is("1"));
        assertThat(firstBalance.getTokens().get(0).getAmount(), is("1"));
        assertThat(firstBalance.getTokens().get(0).getLastUpdatedBlock(), is(12345L));
        assertThat(firstBalance.getTokens().get(1).getTokenId(), is("2"));
        assertThat(firstBalance.getTokens().get(1).getAmount(), is("1"));
        assertThat(firstBalance.getTokens().get(1).getLastUpdatedBlock(), is(123456L));

        // Second Entry
        NeoGetNep11Balances.Nep11Balance secondBalance = nep11Balances.get(1);
        assertThat(secondBalance.getAssetHash(), is(new Hash160("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3")));
        assertThat(secondBalance.getName(), is("CuteNeoKittens"));
        assertThat(secondBalance.getSymbol(), is("CNKS"));
        assertThat(secondBalance.getDecimals(), is("4"));
        assertThat(secondBalance.getTokens().get(0).getTokenId(), is("4"));
        assertThat(secondBalance.getTokens().get(0).getAmount(), is("10000"));
        assertThat(secondBalance.getTokens().get(0).getLastUpdatedBlock(), is(12345L));
        assertThat(secondBalance.getTokens().get(1).getTokenId(), is("10"));
        assertThat(secondBalance.getTokens().get(1).getAmount(), is("6500"));
        assertThat(secondBalance.getTokens().get(1).getLastUpdatedBlock(), is(654321L));
    }

    @Test
    public void testGetNep11Transfers() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"sent\": [\n" +
                        "            {\n" +
                        "                \"tokenid\": \"1\",\n" +
                        "                \"timestamp\": 1554283931,\n" +
                        "                \"assethash\": \"1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3\",\n" +
                        "                \"transferaddress\": \"AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis\",\n" +
                        "                \"amount\": \"100000000000\",\n" +
                        "                \"blockindex\": 368082,\n" +
                        "                \"transfernotifyindex\": 0,\n" +
                        "                \"txhash\": \"240ab1369712ad2782b99a02a8f9fcaa41d1e96322017ae90d0449a3ba52a564\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"tokenid\": \"2\",\n" +
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
                        "                \"tokenid\": \"3\",\n" +
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

        NeoGetNep11Transfers getNep11Transfers = deserialiseResponse(NeoGetNep11Transfers.class);

        List<NeoGetNep11Transfers.Nep11Transfer> sent = getNep11Transfers.getNep11Transfers().getSent();

        assertThat(sent, is(notNullValue()));
        assertThat(sent, hasSize(2));
        assertThat(sent,
                containsInAnyOrder(
                        new NeoGetNep11Transfers.Nep11Transfer(
                                1554283931L,
                                new Hash160("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3"),
                                "AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis",
                                new BigInteger("100000000000"),
                                368082L,
                                0L,
                                new Hash256("240ab1369712ad2782b99a02a8f9fcaa41d1e96322017ae90d0449a3ba52a564"),
                                "1"
                        ),
                        new NeoGetNep11Transfers.Nep11Transfer(
                                1554880287L,
                                new Hash160("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3"),
                                "AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis",
                                new BigInteger("100000000000"),
                                397769L,
                                0L,
                                new Hash256("12fdf7ce8b2388d23ab223854cb29e5114d8288c878de23b7924880f82dfc834"),
                                "2"
                        )
                ));

        List<NeoGetNep11Transfers.Nep11Transfer> received = getNep11Transfers.getNep11Transfers().getReceived();

        assertThat(received, is(notNullValue()));
        assertThat(received, hasSize(1));
        assertThat(received,
                hasItem(
                        new NeoGetNep11Transfers.Nep11Transfer(
                                1555651816L,
                                new Hash160("600c4f5200db36177e3e8a09e9f18e2fc7d12a0f"),
                                "AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis",
                                new BigInteger("1000000"),
                                436036L,
                                0L,
                                new Hash256("df7683ece554ecfb85cf41492c5f143215dd43ef9ec61181a28f922da06aba58"),
                                "3"
                        )
                ));

        // First Sent Entry
        assertThat(sent.get(0).getTimestamp(), is(1554283931L));
        assertThat(sent.get(0).getAssetHash(), is(new Hash160("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3")));
        assertThat(sent.get(0).getTransferAddress(), is("AYwgBNMepiv5ocGcyNT4mA8zPLTQ8pDBis"));
        assertThat(sent.get(0).getTokenId(), is("1"));

        // Second Sent Entry
        assertThat(sent.get(1).getAmount(), is(new BigInteger("100000000000")));
        assertThat(sent.get(1).getBlockIndex(), is(397769L));
        assertThat(sent.get(1).getTokenId(), is("2"));

        // Received Entry
        assertThat(received.get(0).getTransferNotifyIndex(), is(0L));
        assertThat(received.get(0).getTxHash(),
                is(new Hash256("df7683ece554ecfb85cf41492c5f143215dd43ef9ec61181a28f922da06aba58")));
        assertThat(received.get(0).getTokenId(), is("3"));
    }

    @Test
    public void testGetNep11Properties() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"keyProp1\": \"valueProp1\"," +
                        "        \"keyProp2\": \"valueProp2\"" +
                        "    }\n" +
                        "}"
        );

        NeoGetNep11Properties getNep11Properties = deserialiseResponse(NeoGetNep11Properties.class);
        assertThat(getNep11Properties.getProperties(), is(notNullValue()));
        assertThat(getNep11Properties.getProperties().size(), is(2));
        assertThat(getNep11Properties.getProperties().get("keyProp1"), is("valueProp1"));
        assertThat(getNep11Properties.getProperties().get("keyProp2"), is("valueProp2"));
    }

}
