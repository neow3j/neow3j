package io.neow3j.protocol.core;

import io.neow3j.model.types.AssetType;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.model.types.NodePluginType;
import io.neow3j.model.types.StackItemType;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.model.types.TransactionType;
import io.neow3j.protocol.ResponseTester;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.ByteArrayStackItem;
import io.neow3j.protocol.core.methods.response.IntegerStackItem;
import io.neow3j.protocol.core.methods.response.MapStackItem;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetAccountState;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetAssetState;
import io.neow3j.protocol.core.methods.response.NeoGetBalance;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetBlockSysFee;
import io.neow3j.protocol.core.methods.response.NeoGetClaimable;
import io.neow3j.protocol.core.methods.response.NeoGetClaimable.Claim;
import io.neow3j.protocol.core.methods.response.NeoGetClaimable.Claimables;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Balances;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetStorage;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTxOut;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents;
import io.neow3j.protocol.core.methods.response.NeoGetValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetWalletHeight;
import io.neow3j.protocol.core.methods.response.NeoInvoke;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoListPlugins;
import io.neow3j.protocol.core.methods.response.NeoSendMany;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.NeoSubmitBlock;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.Script;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.neow3j.protocol.core.methods.response.TransactionAttribute;
import io.neow3j.protocol.core.methods.response.TransactionInput;
import io.neow3j.protocol.core.methods.response.TransactionOutput;
import io.neow3j.utils.Numeric;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.neow3j.utils.Numeric.prependHexPrefix;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Core Protocol Response tests.
 */
public class ResponseTest extends ResponseTester {

    @Test
    public void testErrorResponse() {
        buildResponse(
                "{"
                        + "  \"jsonrpc\":\"2.0\","
                        + "  \"id\":1,"
                        + "  \"error\":{"
                        + "    \"code\":-32602,"
                        + "    \"message\":\"Invalid address length, expected 40 got 64 bytes\","
                        + "    \"data\":null"
                        + "  }"
                        + "}"
        );

        NeoBlockCount ethBlock = deserialiseResponse(NeoBlockCount.class);
        assertTrue(ethBlock.hasError());
        assertThat(ethBlock.getError(), equalTo(
                new Response.Error(-32602, "Invalid address length, expected 40 got 64 bytes")));
    }

    @Test
    public void testErrorResponseComplexData() {
        buildResponse(
                "{"
                        + "  \"jsonrpc\":\"2.0\","
                        + "  \"id\":1,"
                        + "  \"error\":{"
                        + "    \"code\":-32602,"
                        + "    \"message\":\"Invalid address length, expected 40 got 64 bytes\","
                        + "    \"data\":{\"foo\":\"bar\"}"
                        + "  }"
                        + "}"
        );

        NeoBlockCount ethBlock = deserialiseResponse(NeoBlockCount.class);
        assertTrue(ethBlock.hasError());
        assertThat(ethBlock.getError().getData(), equalTo("{\"foo\":\"bar\"}"));
    }

    @Test
    public void testGetVersion() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": { "
                        + "    \"port\": 1234,\n"
                        + "    \"nonce\": 12345678,\n"
                        + "    \"useragent\": \"\\/NEO:2.7.6\\/\"\n"
                        + "   }"
                        + "}"
        );

        NeoGetVersion neoGetVersion = deserialiseResponse(NeoGetVersion.class);
        assertThat(neoGetVersion.getVersion().getUserAgent(), is("/NEO:2.7.6/"));
        assertThat(neoGetVersion.getVersion().getPort(), is(1234));
        assertThat(neoGetVersion.getVersion().getNonce(), is(12345678L));
    }


    @Test
    public void testGetBlockHash() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"0x147ad6a26f1d5a9bb2bea3f0b2ca9fab3824873beaf8887e87d08c8fd98a81b3\"\n"
                        + "}"
        );

        NeoBlockHash neoBestBlockHash = deserialiseResponse(NeoBlockHash.class);
        assertThat(neoBestBlockHash.getBlockHash(), is("0x147ad6a26f1d5a9bb2bea3f0b2ca9fab3824873beaf8887e87d08c8fd98a81b3"));
    }

    @Test
    public void testGetConnectionCount() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": 3\n"
                        + "}"
        );

        NeoConnectionCount neoConnectionCount = deserialiseResponse(NeoConnectionCount.class);
        assertThat(neoConnectionCount.getCount(), is(3));
    }

    @Test
    public void testListAddress() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": [\n"
                        + "      {\n"
                        + "        \"address\": \"AKkkumHbBipZ46UMZJoFynJMXzSRnBvKcs\",\n"
                        + "        \"haskey\": true,\n"
                        + "        \"label\": \"blah\",\n"
                        + "        \"watchonly\": false\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"address\": \"AZ81H31DMWzbSnFDLFkzh9vHwaDLayV7fU\",\n"
                        + "        \"haskey\": false,\n"
                        + "        \"label\": null,\n"
                        + "        \"watchonly\": true\n"
                        + "      }\n"
                        + "   ]\n"
                        + "}"
        );

        NeoListAddress listAddress = deserialiseResponse(NeoListAddress.class);
        assertThat(listAddress.getAddresses(), hasSize(2));
        assertThat(listAddress.getAddresses().get(0).getAddress(), is("AKkkumHbBipZ46UMZJoFynJMXzSRnBvKcs"));
        assertThat(listAddress.getAddresses().get(0).getHasKey(), is(true));
        assertThat(listAddress.getAddresses().get(0).getLabel(), is("blah"));
        assertThat(listAddress.getAddresses().get(0).getWatchOnly(), is(false));
        assertThat(listAddress.getAddresses().get(1).getAddress(), is("AZ81H31DMWzbSnFDLFkzh9vHwaDLayV7fU"));
        assertThat(listAddress.getAddresses().get(1).getHasKey(), is(false));
        assertThat(listAddress.getAddresses().get(1).getLabel(), is(nullValue()));
        assertThat(listAddress.getAddresses().get(1).getWatchOnly(), is(true));
    }

    @Test
    public void testGetPeers() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"unconnected\": [\n"
                        + "          {\n"
                        + "              \"address\": \"::ffff:127.0.0.1\",\n"
                        + "              \"port\": 20335\n"
                        + "          },\n"
                        + "          {\n"
                        + "              \"address\": \"::ffff:127.0.0.1\",\n"
                        + "              \"port\": 20336\n"
                        + "          },\n"
                        + "          {\n"
                        + "              \"address\": \"::ffff:127.0.0.1\",\n"
                        + "              \"port\": 20334\n"
                        + "          }\n"
                        + "      ],\n"
                        + "      \"bad\": [\n"
                        + "          {\n"
                        + "              \"address\": \"::ffff:127.0.0.1\",\n"
                        + "              \"port\": 20335\n"
                        + "          },\n"
                        + "          {\n"
                        + "              \"address\": \"::ffff:127.0.0.1\",\n"
                        + "              \"port\": 20336\n"
                        + "          },\n"
                        + "          {\n"
                        + "              \"address\": \"::ffff:127.0.0.1\",\n"
                        + "              \"port\": 20334\n"
                        + "          }\n"
                        + "      ],\n"
                        + "      \"connected\": [\n"
                        + "          {\n"
                        + "              \"address\": \"::ffff:127.0.0.1\",\n"
                        + "              \"port\": 20335\n"
                        + "          },\n"
                        + "          {\n"
                        + "              \"address\": \"::ffff:127.0.0.1\",\n"
                        + "              \"port\": 20336\n"
                        + "          },\n"
                        + "          {\n"
                        + "              \"address\": \"::ffff:127.0.0.1\",\n"
                        + "              \"port\": 20334\n"
                        + "          }\n"
                        + "      ]\n"
                        + "   }\n"
                        + "}"
        );

        NeoGetPeers getPeers = deserialiseResponse(NeoGetPeers.class);

        assertThat(getPeers.getPeers(), not(nullValue()));
        assertThat(getPeers.getPeers().getBad(), not(nullValue()));
        assertThat(getPeers.getPeers().getConnected(), not(nullValue()));
        assertThat(getPeers.getPeers().getUnconnected(), not(nullValue()));

        assertThat(getPeers.getPeers().getBad(), hasSize(3));
        assertThat(getPeers.getPeers().getConnected(), hasSize(3));
        assertThat(getPeers.getPeers().getUnconnected(), hasSize(3));

        assertThat(
                getPeers.getPeers().getBad(),
                containsInAnyOrder(
                        new NeoGetPeers.AddressEntry("::ffff:127.0.0.1", 20335),
                        new NeoGetPeers.AddressEntry("::ffff:127.0.0.1", 20336),
                        new NeoGetPeers.AddressEntry("::ffff:127.0.0.1", 20334)
                )
        );

        assertThat(
                getPeers.getPeers().getUnconnected(),
                containsInAnyOrder(
                        new NeoGetPeers.AddressEntry("::ffff:127.0.0.1", 20335),
                        new NeoGetPeers.AddressEntry("::ffff:127.0.0.1", 20336),
                        new NeoGetPeers.AddressEntry("::ffff:127.0.0.1", 20334)
                )
        );

        assertThat(
                getPeers.getPeers().getConnected(),
                containsInAnyOrder(
                        new NeoGetPeers.AddressEntry("::ffff:127.0.0.1", 20335),
                        new NeoGetPeers.AddressEntry("::ffff:127.0.0.1", 20336),
                        new NeoGetPeers.AddressEntry("::ffff:127.0.0.1", 20334)
                )
        );

    }

    @Test
    public void testGetPeers_Empty() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"unconnected\": [\n"
                        + "      ],\n"
                        + "      \"bad\": [\n"
                        + "      ],\n"
                        + "      \"connected\": [\n"
                        + "      ]\n"
                        + "   }\n"
                        + "}"
        );

        NeoGetPeers getPeers = deserialiseResponse(NeoGetPeers.class);

        assertThat(getPeers.getPeers(), not(nullValue()));
        assertThat(getPeers.getPeers().getBad(), not(nullValue()));
        assertThat(getPeers.getPeers().getConnected(), not(nullValue()));
        assertThat(getPeers.getPeers().getUnconnected(), not(nullValue()));

        assertThat(getPeers.getPeers().getBad(), hasSize(0));
        assertThat(getPeers.getPeers().getConnected(), hasSize(0));
        assertThat(getPeers.getPeers().getUnconnected(), hasSize(0));
    }

    @Test
    public void testGetRawMemPool() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": [\n"
                        + "      \"0x9786cce0dddb524c40ddbdd5e31a41ed1f6b5c8a683c122f627ca4a007a7cf4e\",\n"
                        + "      \"0xb488ad25eb474f89d5ca3f985cc047ca96bc7373a6d3da8c0f192722896c1cd7\",\n"
                        + "      \"0xf86f6f2c08fbf766ebe59dc84bc3b8829f1053f0a01deb26bf7960d99fa86cd6\"\n"
                        + "   ]\n"
                        + "}"
        );

        NeoGetRawMemPool getRawMemPool = deserialiseResponse(NeoGetRawMemPool.class);
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
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": [\n"
                        + "   ]\n"
                        + "}"
        );
        NeoGetRawMemPool getRawMemPool = deserialiseResponse(NeoGetRawMemPool.class);
        assertThat(getRawMemPool.getAddresses(), is(notNullValue()));
        assertThat(getRawMemPool.getAddresses(), hasSize(0));
    }

    @Test
    public void testGetValidators() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": [\n"
                        + "      {\n"
                        + "          \"publickey\": \"02494f3ff953e45ca4254375187004f17293f90a1aa4b1a89bc07065bc1da521f6\",\n"
                        + "          \"votes\": \"0\",\n"
                        + "          \"active\": false\n"
                        + "      },\n"
                        + "      {\n"
                        + "          \"publickey\": \"025bdf3f181f53e9696227843950deb72dcd374ded17c057159513c3d0abe20b64\",\n"
                        + "          \"votes\": \"91600000\",\n"
                        + "          \"active\": true\n"
                        + "      },\n"
                        + "      {\n"
                        + "          \"publickey\": \"0266b588e350ab63b850e55dbfed0feeda44410a30966341b371014b803a15af07\",\n"
                        + "          \"votes\": \"91600000\",\n"
                        + "          \"active\": true\n"
                        + "      }"
                        + "   ]\n"
                        + "}"
        );

        NeoGetValidators getValidators = deserialiseResponse(NeoGetValidators.class);
        assertThat(getValidators.getValidators(), hasSize(3));
        assertThat(
                getValidators.getValidators(),
                containsInAnyOrder(
                        new NeoGetValidators.Validator("02494f3ff953e45ca4254375187004f17293f90a1aa4b1a89bc07065bc1da521f6", "0", false),
                        new NeoGetValidators.Validator("025bdf3f181f53e9696227843950deb72dcd374ded17c057159513c3d0abe20b64", "91600000", true),
                        new NeoGetValidators.Validator("0266b588e350ab63b850e55dbfed0feeda44410a30966341b371014b803a15af07", "91600000", true)
                )
        );
        assertThat(getValidators.getValidators().get(0).getVotesAsBigInteger(), is(BigInteger.valueOf(0)));
    }

    @Test
    public void testGetValidators_Empty() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": [\n"
                        + "   ]\n"
                        + "}"
        );

        NeoGetValidators getValidators = deserialiseResponse(NeoGetValidators.class);
        assertThat(getValidators.getValidators(), is(notNullValue()));
        assertThat(getValidators.getValidators(), hasSize(0));
    }

    @Test
    public void testValidateAddress() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"address\": \"AQVh2pG732YvtNaxEGkQUei3YA4cvo7d2i\",\n"
                        + "      \"isvalid\": true\n"
                        + "   }\n"
                        + "}"
        );

        NeoValidateAddress validateAddress = deserialiseResponse(NeoValidateAddress.class);
        assertThat(validateAddress.getValidation(), is(notNullValue()));
        assertThat(validateAddress.getValidation().getAddress(), is("AQVh2pG732YvtNaxEGkQUei3YA4cvo7d2i"));
        assertThat(validateAddress.getValidation().getValid(), is(true));
    }

    @Test
    public void testGetBlock() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"hash\": \"0x498b16db3fba92448fac63caeecb91ce38cb4b565de7d717d473f0dd37a1e816\",\n" +
                        "        \"size\": 1217,\n" +
                        "        \"version\": 0,\n" +
                        "        \"previousblockhash\": \"0x045cabde4ecbd50f5e4e1b141eaf0842c1f5f56517324c8dcab8ccac924e3a39\",\n" +
                        "        \"merkleroot\": \"0x6afa63201b88b55ad2213e5a69a1ad5f0db650bc178fc2bedd2fb301c1278bf7\",\n" +
                        "        \"time\": 1539968858,\n" +
                        "        \"index\": 1914006,\n" +
                        "        \"nonce\": \"44ed38ca21ae8877\",\n" +
                        "        \"nextconsensus\": \"AWZo4qAxhT8fwKL93QATSjCYCgHmCY1XLB\",\n" +
                        "        \"script\": {\n" +
                        "            \"invocation\": \"4038f080f920e30fc2b08903788fa53c262a05f92a8e7ae8a8e6ebd45d6c035e15441675d037359dcec8e010b00ae0b9c5d2f51aa9e0a27b79e36b8d65c365fea0407b9618d2ca30b3dcf14422212fe8dd9d2b126f72a8d84d9dcd523ebf75ffc308495e8ddf13111216c1076b36fd1c1fa1c45c974e3d59427305fe4a44041dc93b4000a7c634b6e5fc7d5d543a4fe072530f114a823b9b4fdd069c1aacd0f4c7aa1ba166dadc6a1755b2485646331457ba1f1a62e915172a878dc5dad49958410bcc406fffecc421608a2ad257311e5b4ef7b86b3d7a207116e6b7f1b8e1b657093ea37b2c469d733213bce1099942e82ce2ef6663d4f244769acdb04696e7446afed4404bcd150392baec630b96da0954e06a9bf4bef3f9f6983ea6604482ec6762493a596189aad0e1c30bb0a4b05048c524b354bf449077e58d6cb6f91bbd49e280f7\",\n" +
                        "            \"verification\": \"5521030ef96257401b803da5dd201233e2be828795672b775dd674d69df83f7aec1e36210327da12b5c40200e9f65569476bbff2218da4f32548ff43b6387ec1416a231ee821025bdf3f181f53e9696227843950deb72dcd374ded17c057159513c3d0abe20b64210266b588e350ab63b850e55dbfed0feeda44410a30966341b371014b803a15af0721026ce35b29147ad09e4afe4ec4a7319095f08198fa8babbe3c56e970b143528d222103c089d7122b840a4935234e82e26ae5efd0c2acb627239dc9f207311337b6f2c12103fd95a9cb3098e6447d0de9f76cc97fd5e36830f9c7044457c15a0e81316bf28f57ae\"\n" +
                        "        },\n" +
                        "        \"tx\": [\n" +
                        "            {\n" +
                        "                \"txid\": \"0x96ff8b13809f9ad38b165545f4d6c723faf7ca4d3a8d88297726532caa89a21c\",\n" +
                        "                \"size\": 10,\n" +
                        "                \"type\": \"MinerTransaction\",\n" +
                        "                \"version\": 0,\n" +
                        "                \"attributes\": [],\n" +
                        "                \"vin\": [],\n" +
                        "                \"vout\": [],\n" +
                        "                \"sys_fee\": \"0\",\n" +
                        "                \"net_fee\": \"0\",\n" +
                        "                \"scripts\": [],\n" +
                        "                \"nonce\": 565086327\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"txid\": \"0x93c569cbe33e918f7a5392025fbdeab5f6c97c8e5897fafc466694b6e8e1b0d2\",\n" +
                        "                \"size\": 322,\n" +
                        "                \"type\": \"ContractTransaction\",\n" +
                        "                \"version\": 0,\n" +
                        "                \"attributes\": [],\n" +
                        "                \"vin\": [\n" +
                        "                    {\n" +
                        "                        \"txid\": \"0x5b0b51b63f476fbc8080b5450a20703b7af23c9125cfae45215953529e13bb32\",\n" +
                        "                        \"vout\": 1\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"vout\": [\n" +
                        "                    {\n" +
                        "                        \"n\": 0,\n" +
                        "                        \"asset\": \"0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7\",\n" +
                        "                        \"value\": \"10\",\n" +
                        "                        \"address\": \"AHb4HXonuseHsAztd97GZTtmNvwEoMDQg7\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"n\": 1,\n" +
                        "                        \"asset\": \"0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7\",\n" +
                        "                        \"value\": \"10\",\n" +
                        "                        \"address\": \"AYL1UwhA1J8zpHK8X4hSmjuFSLa49XUhFe\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"n\": 2,\n" +
                        "                        \"asset\": \"0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7\",\n" +
                        "                        \"value\": \"28056.999\",\n" +
                        "                        \"address\": \"APVdDEtthapuaPedMHCgrDR5Vyc22fns9m\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"sys_fee\": \"0\",\n" +
                        "                \"net_fee\": \"0\",\n" +
                        "                \"scripts\": [\n" +
                        "                    {\n" +
                        "                        \"invocation\": \"407cf160d0e7c4e82383c3d3f28e26d894d8498507625e4a23cee34915128edd5713f3eb8db2ba30f8e8d47686af2481baa40e8e452a3e983a3209246087f83040\",\n" +
                        "                        \"verification\": \"210293cd2efa68906ef5839afd332cf6817a27f8474d64c799647e4438dfcd1bcab0ac\"\n" +
                        "                    }\n" +
                        "                ]\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"txid\": \"0xb6d5eccf7cea2c21f23c27ca1ad08d1eb6095decd8225b980d45059dbb0713ce\",\n" +
                        "                \"size\": 209,\n" +
                        "                \"type\": \"InvocationTransaction\",\n" +
                        "                \"version\": 1,\n" +
                        "                \"attributes\": [\n" +
                        "                    {\n" +
                        "                        \"usage\": \"Script\",\n" +
                        "                        \"data\": \"1b574e7c412bf48304c1d359805f078487878735\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                        \"usage\": \"Remark\",\n" +
                        "                        \"data\": \"313533393936383835353131366463336361623763\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"vin\": [],\n" +
                        "                \"vout\": [],\n" +
                        "                \"sys_fee\": \"0\",\n" +
                        "                \"net_fee\": \"0\",\n" +
                        "                \"scripts\": [\n" +
                        "                    {\n" +
                        "                        \"invocation\": \"4076513172004c2337f47094120e25a6c71fb9bfc4e12ca91babd4f8110523ae52a53f3ca4839ae8d3276df0120a9fe6b9385271ce4c1f0195d5249fa133ef718a\",\n" +
                        "                        \"verification\": \"21021012fef0dd6437c25d1b1f437d8828ccc37e9c21543b31de27cac97dce987947ac\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "                \"script\": \"0127141b574e7c412bf48304c1d359805f07848787873552c10974616b654f7264657267f9c7d7248356eba19eae6ff828e2bcf26cf985d5\",\n" +
                        "                \"gas\": \"0\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"confirmations\": 7878,\n" +
                        "        \"nextblockhash\": \"0x4a97ca89199627f877b6bffe865b8327be84b368d62572ef20953829c3501643\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetBlock getBlock = deserialiseResponse(NeoGetBlock.class);
        assertThat(getBlock.getBlock(), is(notNullValue()));

        assertThat(getBlock.getBlock().getHash(), is("0x498b16db3fba92448fac63caeecb91ce38cb4b565de7d717d473f0dd37a1e816"));
        assertThat(getBlock.getBlock().getSize(), is(1217L));
        assertThat(getBlock.getBlock().getVersion(), is(0));
        assertThat(getBlock.getBlock().getPrevBlockHash(), is("0x045cabde4ecbd50f5e4e1b141eaf0842c1f5f56517324c8dcab8ccac924e3a39"));
        assertThat(getBlock.getBlock().getMerkleRootHash(), is("0x6afa63201b88b55ad2213e5a69a1ad5f0db650bc178fc2bedd2fb301c1278bf7"));
        assertThat(getBlock.getBlock().getTime(), is(1539968858L));
        assertThat(getBlock.getBlock().getIndex(), is(1914006L));
        assertThat(getBlock.getBlock().getNonce(), is("44ed38ca21ae8877"));
        assertThat(getBlock.getBlock().getNextConsensus(), is("AWZo4qAxhT8fwKL93QATSjCYCgHmCY1XLB"));
        assertThat(getBlock.getBlock().getScript(), is(notNullValue()));
        assertThat(
                getBlock.getBlock().getScript(),
                CoreMatchers.is(
                        new Script(
                                "4038f080f920e30fc2b08903788fa53c262a05f92a8e7ae8a8e6ebd45d6c035e15441675d037359dcec8e010b00ae0b9c5d2f51aa9e0a27b79e36b8d65c365fea0407b9618d2ca30b3dcf14422212fe8dd9d2b126f72a8d84d9dcd523ebf75ffc308495e8ddf13111216c1076b36fd1c1fa1c45c974e3d59427305fe4a44041dc93b4000a7c634b6e5fc7d5d543a4fe072530f114a823b9b4fdd069c1aacd0f4c7aa1ba166dadc6a1755b2485646331457ba1f1a62e915172a878dc5dad49958410bcc406fffecc421608a2ad257311e5b4ef7b86b3d7a207116e6b7f1b8e1b657093ea37b2c469d733213bce1099942e82ce2ef6663d4f244769acdb04696e7446afed4404bcd150392baec630b96da0954e06a9bf4bef3f9f6983ea6604482ec6762493a596189aad0e1c30bb0a4b05048c524b354bf449077e58d6cb6f91bbd49e280f7",
                                "5521030ef96257401b803da5dd201233e2be828795672b775dd674d69df83f7aec1e36210327da12b5c40200e9f65569476bbff2218da4f32548ff43b6387ec1416a231ee821025bdf3f181f53e9696227843950deb72dcd374ded17c057159513c3d0abe20b64210266b588e350ab63b850e55dbfed0feeda44410a30966341b371014b803a15af0721026ce35b29147ad09e4afe4ec4a7319095f08198fa8babbe3c56e970b143528d222103c089d7122b840a4935234e82e26ae5efd0c2acb627239dc9f207311337b6f2c12103fd95a9cb3098e6447d0de9f76cc97fd5e36830f9c7044457c15a0e81316bf28f57ae"
                        )
                )
        );

        assertThat(getBlock.getBlock().getTransactions(), hasSize(3));

        assertThat(
                getBlock.getBlock().getTransactions(),
                containsInAnyOrder(
                        new Transaction(
                                "0x96ff8b13809f9ad38b165545f4d6c723faf7ca4d3a8d88297726532caa89a21c",
                                10L,
                                TransactionType.MINER_TRANSACTION,
                                0,
                                Arrays.asList(),
                                Arrays.asList(),
                                Arrays.asList(),
                                "0",
                                "0",
                                Arrays.asList(),
                                null,
                                null,
                                new Long(565086327)
                        ),
                        new Transaction(
                                "0x93c569cbe33e918f7a5392025fbdeab5f6c97c8e5897fafc466694b6e8e1b0d2",
                                322L,
                                TransactionType.CONTRACT_TRANSACTION,
                                0,
                                Arrays.asList(),
                                Arrays.asList(
                                        new TransactionInput("0x5b0b51b63f476fbc8080b5450a20703b7af23c9125cfae45215953529e13bb32", 1)
                                ),
                                Arrays.asList(
                                        new TransactionOutput(
                                                0,
                                                "0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7",
                                                "10",
                                                "AHb4HXonuseHsAztd97GZTtmNvwEoMDQg7"
                                        ),
                                        new TransactionOutput(
                                                1,
                                                "0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7",
                                                "10",
                                                "AYL1UwhA1J8zpHK8X4hSmjuFSLa49XUhFe"
                                        ),
                                        new TransactionOutput(
                                                2,
                                                "0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7",
                                                "28056.999",
                                                "APVdDEtthapuaPedMHCgrDR5Vyc22fns9m"
                                        )
                                ),
                                "0",
                                "0",
                                Arrays.asList(
                                        new Script(
                                                "407cf160d0e7c4e82383c3d3f28e26d894d8498507625e4a23cee34915128edd5713f3eb8db2ba30f8e8d47686af2481baa40e8e452a3e983a3209246087f83040",
                                                "210293cd2efa68906ef5839afd332cf6817a27f8474d64c799647e4438dfcd1bcab0ac"
                                        )
                                ),
                                null,
                                null,
                                null
                        ),
                        new Transaction(
                                "0xb6d5eccf7cea2c21f23c27ca1ad08d1eb6095decd8225b980d45059dbb0713ce",
                                209L,
                                TransactionType.INVOCATION_TRANSACTION,
                                1,
                                Arrays.asList(
                                        new TransactionAttribute(
                                                TransactionAttributeUsageType.SCRIPT,
                                                "1b574e7c412bf48304c1d359805f078487878735"
                                        ),
                                        new TransactionAttribute(
                                                TransactionAttributeUsageType.REMARK,
                                                "313533393936383835353131366463336361623763"
                                        )
                                ),
                                Arrays.asList(),
                                Arrays.asList(),
                                "0",
                                "0",
                                Arrays.asList(
                                        new Script(
                                                "4076513172004c2337f47094120e25a6c71fb9bfc4e12ca91babd4f8110523ae52a53f3ca4839ae8d3276df0120a9fe6b9385271ce4c1f0195d5249fa133ef718a",
                                                "21021012fef0dd6437c25d1b1f437d8828ccc37e9c21543b31de27cac97dce987947ac"
                                        )
                                ),
                                "0127141b574e7c412bf48304c1d359805f07848787873552c10974616b654f7264657267f9c7d7248356eba19eae6ff828e2bcf26cf985d5",
                                "0",
                                null
                        )
                )
        );

        assertThat(getBlock.getBlock().getConfirmations(), is(7878));
        assertThat(getBlock.getBlock().getNextBlockHash(), is("0x4a97ca89199627f877b6bffe865b8327be84b368d62572ef20953829c3501643"));
    }

    @Test
    public void testGetBlock_BlockHeader() {
        buildResponse(
                "{\n" +
                        "    \"jsonrpc\": \"2.0\",\n" +
                        "    \"id\": 1,\n" +
                        "    \"result\": {\n" +
                        "        \"hash\": \"0x498b16db3fba92448fac63caeecb91ce38cb4b565de7d717d473f0dd37a1e816\",\n" +
                        "        \"size\": 1217,\n" +
                        "        \"version\": 0,\n" +
                        "        \"previousblockhash\": \"0x045cabde4ecbd50f5e4e1b141eaf0842c1f5f56517324c8dcab8ccac924e3a39\",\n" +
                        "        \"merkleroot\": \"0x6afa63201b88b55ad2213e5a69a1ad5f0db650bc178fc2bedd2fb301c1278bf7\",\n" +
                        "        \"time\": 1539968858,\n" +
                        "        \"index\": 1914006,\n" +
                        "        \"nonce\": \"44ed38ca21ae8877\",\n" +
                        "        \"nextconsensus\": \"AWZo4qAxhT8fwKL93QATSjCYCgHmCY1XLB\",\n" +
                        "        \"script\": {\n" +
                        "            \"invocation\": \"4038f080f920e30fc2b08903788fa53c262a05f92a8e7ae8a8e6ebd45d6c035e15441675d037359dcec8e010b00ae0b9c5d2f51aa9e0a27b79e36b8d65c365fea0407b9618d2ca30b3dcf14422212fe8dd9d2b126f72a8d84d9dcd523ebf75ffc308495e8ddf13111216c1076b36fd1c1fa1c45c974e3d59427305fe4a44041dc93b4000a7c634b6e5fc7d5d543a4fe072530f114a823b9b4fdd069c1aacd0f4c7aa1ba166dadc6a1755b2485646331457ba1f1a62e915172a878dc5dad49958410bcc406fffecc421608a2ad257311e5b4ef7b86b3d7a207116e6b7f1b8e1b657093ea37b2c469d733213bce1099942e82ce2ef6663d4f244769acdb04696e7446afed4404bcd150392baec630b96da0954e06a9bf4bef3f9f6983ea6604482ec6762493a596189aad0e1c30bb0a4b05048c524b354bf449077e58d6cb6f91bbd49e280f7\",\n" +
                        "            \"verification\": \"5521030ef96257401b803da5dd201233e2be828795672b775dd674d69df83f7aec1e36210327da12b5c40200e9f65569476bbff2218da4f32548ff43b6387ec1416a231ee821025bdf3f181f53e9696227843950deb72dcd374ded17c057159513c3d0abe20b64210266b588e350ab63b850e55dbfed0feeda44410a30966341b371014b803a15af0721026ce35b29147ad09e4afe4ec4a7319095f08198fa8babbe3c56e970b143528d222103c089d7122b840a4935234e82e26ae5efd0c2acb627239dc9f207311337b6f2c12103fd95a9cb3098e6447d0de9f76cc97fd5e36830f9c7044457c15a0e81316bf28f57ae\"\n" +
                        "        },\n" +
                        "        \"confirmations\": 7878,\n" +
                        "        \"nextblockhash\": \"0x4a97ca89199627f877b6bffe865b8327be84b368d62572ef20953829c3501643\"\n" +
                        "    }\n" +
                        "}"
        );

        NeoGetBlock getBlock = deserialiseResponse(NeoGetBlock.class);
        assertThat(getBlock.getBlock(), is(notNullValue()));

        assertThat(getBlock.getBlock().getHash(), is("0x498b16db3fba92448fac63caeecb91ce38cb4b565de7d717d473f0dd37a1e816"));
        assertThat(getBlock.getBlock().getSize(), is(1217L));
        assertThat(getBlock.getBlock().getVersion(), is(0));
        assertThat(getBlock.getBlock().getPrevBlockHash(), is("0x045cabde4ecbd50f5e4e1b141eaf0842c1f5f56517324c8dcab8ccac924e3a39"));
        assertThat(getBlock.getBlock().getMerkleRootHash(), is("0x6afa63201b88b55ad2213e5a69a1ad5f0db650bc178fc2bedd2fb301c1278bf7"));
        assertThat(getBlock.getBlock().getTime(), is(1539968858L));
        assertThat(getBlock.getBlock().getIndex(), is(1914006L));
        assertThat(getBlock.getBlock().getNonce(), is("44ed38ca21ae8877"));
        assertThat(getBlock.getBlock().getNextConsensus(), is("AWZo4qAxhT8fwKL93QATSjCYCgHmCY1XLB"));
        assertThat(getBlock.getBlock().getScript(), is(notNullValue()));
        assertThat(
                getBlock.getBlock().getScript(),
                is(
                        new Script(
                                "4038f080f920e30fc2b08903788fa53c262a05f92a8e7ae8a8e6ebd45d6c035e15441675d037359dcec8e010b00ae0b9c5d2f51aa9e0a27b79e36b8d65c365fea0407b9618d2ca30b3dcf14422212fe8dd9d2b126f72a8d84d9dcd523ebf75ffc308495e8ddf13111216c1076b36fd1c1fa1c45c974e3d59427305fe4a44041dc93b4000a7c634b6e5fc7d5d543a4fe072530f114a823b9b4fdd069c1aacd0f4c7aa1ba166dadc6a1755b2485646331457ba1f1a62e915172a878dc5dad49958410bcc406fffecc421608a2ad257311e5b4ef7b86b3d7a207116e6b7f1b8e1b657093ea37b2c469d733213bce1099942e82ce2ef6663d4f244769acdb04696e7446afed4404bcd150392baec630b96da0954e06a9bf4bef3f9f6983ea6604482ec6762493a596189aad0e1c30bb0a4b05048c524b354bf449077e58d6cb6f91bbd49e280f7",
                                "5521030ef96257401b803da5dd201233e2be828795672b775dd674d69df83f7aec1e36210327da12b5c40200e9f65569476bbff2218da4f32548ff43b6387ec1416a231ee821025bdf3f181f53e9696227843950deb72dcd374ded17c057159513c3d0abe20b64210266b588e350ab63b850e55dbfed0feeda44410a30966341b371014b803a15af0721026ce35b29147ad09e4afe4ec4a7319095f08198fa8babbe3c56e970b143528d222103c089d7122b840a4935234e82e26ae5efd0c2acb627239dc9f207311337b6f2c12103fd95a9cb3098e6447d0de9f76cc97fd5e36830f9c7044457c15a0e81316bf28f57ae"
                        )
                )
        );

        assertThat(getBlock.getBlock().getTransactions(), is(nullValue()));

        assertThat(getBlock.getBlock().getConfirmations(), is(7878));
        assertThat(getBlock.getBlock().getNextBlockHash(), is("0x4a97ca89199627f877b6bffe865b8327be84b368d62572ef20953829c3501643"));
    }


    @Test
    public void testGetRawBlock() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"00000000ebaa4ed893333db1ed556bb24145f4e7fe40b9c7c07ff2235c7d3d361ddb27e603da9da4c7420d090d0e29c588cfd701b3f81819375e537c634bd779ddc7e2e2c436cc5ba53f00001952d428256ad0cdbe48d3a3f5d10013ab9ffee489706078714f1ea201c340c44387d762d1bcb2ab0ec650628c7c674021f333ee7666e2a03805ad86df3b826b5dbf5ac607a361807a047d43cf6bba726dcb06a42662aee7e78886c72faef940e6cef9abab82e1e90c6683ac8241b3bf51a10c908f01465f19c3df1099ef5de5d43a648a6e4ab63cc7d5e88146bddbe950e8041e44a2b0b81f21ad706e88258540fd19314f46ad452b4cbedf58bf9d266c0c808374cd33ef18d9a0575b01e47f6bb04abe76036619787c457c49288aeb91ff23cdb85771c0209db184801d5bdd348b532102103a7f7dd016558597f7960d27c516a4394fd968b9e65155eb4b013e4040406e2102a7bc55fe8684e0119768d104ba30795bdcc86619e864add26156723ed185cd622102b3622bf4017bdfe317c58aed5f4c753f206b7db896046fa7d774bbc4bf7f8dc22103d90c07df63e690ce77912e10ab51acc944b66860237b608c4f8f8309e71ee69954ae0100001952d42800000000\"\n"
                        + "}"
        );

        NeoGetRawBlock getRawBlock = deserialiseResponse(NeoGetRawBlock.class);
        assertThat(getRawBlock.getRawBlock(), is(notNullValue()));
        assertThat(getRawBlock.getRawBlock(), is("00000000ebaa4ed893333db1ed556bb24145f4e7fe40b9c7c07ff2235c7d3d361ddb27e603da9da4c7420d090d0e29c588cfd701b3f81819375e537c634bd779ddc7e2e2c436cc5ba53f00001952d428256ad0cdbe48d3a3f5d10013ab9ffee489706078714f1ea201c340c44387d762d1bcb2ab0ec650628c7c674021f333ee7666e2a03805ad86df3b826b5dbf5ac607a361807a047d43cf6bba726dcb06a42662aee7e78886c72faef940e6cef9abab82e1e90c6683ac8241b3bf51a10c908f01465f19c3df1099ef5de5d43a648a6e4ab63cc7d5e88146bddbe950e8041e44a2b0b81f21ad706e88258540fd19314f46ad452b4cbedf58bf9d266c0c808374cd33ef18d9a0575b01e47f6bb04abe76036619787c457c49288aeb91ff23cdb85771c0209db184801d5bdd348b532102103a7f7dd016558597f7960d27c516a4394fd968b9e65155eb4b013e4040406e2102a7bc55fe8684e0119768d104ba30795bdcc86619e864add26156723ed185cd622102b3622bf4017bdfe317c58aed5f4c753f206b7db896046fa7d774bbc4bf7f8dc22103d90c07df63e690ce77912e10ab51acc944b66860237b608c4f8f8309e71ee69954ae0100001952d42800000000"));
    }

    @Test
    public void testGetBlockCount() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": 1234\n"
                        + "}"
        );

        NeoBlockCount neoBlockCount = deserialiseResponse(NeoBlockCount.class);
        assertThat(neoBlockCount.getBlockIndex(), is(notNullValue()));
        assertThat(neoBlockCount.getBlockIndex(), is(BigInteger.valueOf(1234)));
    }

    @Test
    public void testGetAccountState() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"version\": 0,\n"
                        + "      \"script_hash\": \"0x1cfacc3e315977329c11ca50fe753730939da95f\",\n"
                        + "      \"frozen\": false,\n"
                        + "      \"votes\": [\n"
                        + "          \"03b209fd4f53a7170ea4444e0cb0a6bb6a53c2bd016926989cf85f9b0fba17a70c\",\n"
                        + "          \"02df48f60e8f3e01c48ff40b9b7f1310d7a8b2a193188befe1c2e3df740e895093\",\n"
                        + "          \"03b8d9d5771d8f513aa0869b9cc8d50986403b78c6da36890638c3d46a5adce04a\",\n"
                        + "          \"02ca0e27697b9c248f6f16e085fd0061e26f44da85b58ee835c110caa5ec3ba554\",\n"
                        + "          \"024c7b7fb6c310fccf1ba33b082519d82964ea93868d676662d4a59ad548df0e7d\",\n"
                        + "          \"035e819642a8915a2572f972ddbdbe3042ae6437349295edce9bdc3b8884bbf9a3\",\n"
                        + "          \"025bdf3f181f53e9696227843950deb72dcd374ded17c057159513c3d0abe20b64\"\n"
                        + "      ],\n"
                        + "      \"balances\": [\n"
                        + "          {\n"
                        + "              \"asset\": \"0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7\",\n"
                        + "              \"value\": \"0.00001058\"\n"
                        + "          },\n"
                        + "          {\n"
                        + "              \"asset\": \"0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\n"
                        + "              \"value\": \"35000001\"\n"
                        + "          }\n"
                        + "      ]\n"
                        + "   }\n"
                        + "}"
        );

        NeoGetAccountState getAccountState = deserialiseResponse(NeoGetAccountState.class);
        assertThat(getAccountState.getAccountState(), is(notNullValue()));
        assertThat(getAccountState.getAccountState().getVersion(), is(0));
        assertThat(getAccountState.getAccountState().getScriptHash(), is("0x1cfacc3e315977329c11ca50fe753730939da95f"));
        assertThat(getAccountState.getAccountState().getFrozen(), is(false));
        assertThat(getAccountState.getAccountState().getVotes(), hasSize(7));
        assertThat(
                getAccountState.getAccountState().getVotes(),
                containsInAnyOrder(
                        "03b209fd4f53a7170ea4444e0cb0a6bb6a53c2bd016926989cf85f9b0fba17a70c",
                        "02df48f60e8f3e01c48ff40b9b7f1310d7a8b2a193188befe1c2e3df740e895093",
                        "03b8d9d5771d8f513aa0869b9cc8d50986403b78c6da36890638c3d46a5adce04a",
                        "02ca0e27697b9c248f6f16e085fd0061e26f44da85b58ee835c110caa5ec3ba554",
                        "024c7b7fb6c310fccf1ba33b082519d82964ea93868d676662d4a59ad548df0e7d",
                        "035e819642a8915a2572f972ddbdbe3042ae6437349295edce9bdc3b8884bbf9a3",
                        "025bdf3f181f53e9696227843950deb72dcd374ded17c057159513c3d0abe20b64"
                )
        );

        assertThat(getAccountState.getAccountState().getBalances(), hasSize(2));
        assertThat(
                getAccountState.getAccountState().getBalances(),
                containsInAnyOrder(
                        new NeoGetAccountState.Balance("0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7", "0.00001058"),
                        new NeoGetAccountState.Balance("0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "35000001")
                )
        );
    }

    @Test
    public void testGetNewAddress() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"APBQE5gV89oRm1K1RWTvLwNNxAAQ8vLmd6\"\n"
                        + "}"
        );

        NeoGetNewAddress getNewAddress = deserialiseResponse(NeoGetNewAddress.class);
        assertThat(getNewAddress.getAddress(), is(notNullValue()));
        assertThat(getNewAddress.getAddress(), is("APBQE5gV89oRm1K1RWTvLwNNxAAQ8vLmd6"));
    }

    @Test
    public void testGetWalletHeight() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": 1927636\n"
                        + "}"
        );

        NeoGetWalletHeight getWalletHeight = deserialiseResponse(NeoGetWalletHeight.class);
        assertThat(getWalletHeight.getHeight(), is(notNullValue()));
        assertThat(getWalletHeight.getHeight(), is(BigInteger.valueOf(1927636)));
    }

    @Test
    public void testGetBlockSysFee() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"200\"\n"
                        + "}"
        );

        NeoGetBlockSysFee getBlockSysFee = deserialiseResponse(NeoGetBlockSysFee.class);
        assertThat(getBlockSysFee.getFee(), is(notNullValue()));
        assertThat(getBlockSysFee.getFee(), is("200"));
    }

    @Test
    public void testGetTxOut() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"n\": 0,\n"
                        + "      \"asset\": \"0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7\",\n"
                        + "      \"value\": \"10\",\n"
                        + "      \"address\": \"AHb4HXonuseHsAztd97GZTtmNvwEoMDQg7\"\n"
                        + "  }\n"
                        + "}"
        );

        NeoGetTxOut getTxOut = deserialiseResponse(NeoGetTxOut.class);
        assertThat(getTxOut.getTransaction(), is(notNullValue()));
        assertThat(
                getTxOut.getTransaction(),
                is(
                        new TransactionOutput(
                                0,
                                "0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7",
                                "10",
                                "AHb4HXonuseHsAztd97GZTtmNvwEoMDQg7"
                        )
                )
        );
    }

    @Test
    public void testGetTxOut_JsonAttribute_Alias() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"N\": 0,\n"
                        + "      \"Asset\": \"0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7\",\n"
                        + "      \"Value\": \"10\",\n"
                        + "      \"Address\": \"AHb4HXonuseHsAztd97GZTtmNvwEoMDQg7\"\n"
                        + "  }\n"
                        + "}"
        );

        NeoGetTxOut getTxOut = deserialiseResponse(NeoGetTxOut.class);
        assertThat(getTxOut.getTransaction(), is(notNullValue()));
        assertThat(
                getTxOut.getTransaction(),
                is(
                        new TransactionOutput(
                                0,
                                "0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7",
                                "10",
                                "AHb4HXonuseHsAztd97GZTtmNvwEoMDQg7"
                        )
                )
        );
    }

    @Test
    public void testSendRawTransaction() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}"
        );

        NeoSendRawTransaction sendRawTransaction = deserialiseResponse(NeoSendRawTransaction.class);
        assertThat(sendRawTransaction.getSendRawTransaction(), is(notNullValue()));
        assertThat(
                sendRawTransaction.getSendRawTransaction(),
                is(true)
        );
    }

    @Test
    public void testSendToAddress() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"txid\": \"0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6\",\n"
                        + "      \"size\": 283,\n"
                        + "      \"type\": \"ContractTransaction\",\n"
                        + "      \"version\": 0,\n"
                        + "      \"attributes\": ["
                        + "           {"
                        + "               \"usage\": 32,\n"
                        + "               \"data\": \"23ba2703c53263e8d6e522dc32203339dcd8eee9\"\n"
                        + "           }"
                        + "      ],\n"
                        + "      \"vin\": [\n"
                        + "           {\n"
                        + "               \"txid\": \"0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff\",\n"
                        + "               \"vout\": 0\n"
                        + "           }\n"
                        + "      ],\n"
                        + "      \"vout\": [\n"
                        + "           {\n"
                        + "               \"n\": 0,\n"
                        + "               \"asset\": \"0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\n"
                        + "               \"value\": \"10\",\n"
                        + "               \"address\": \"AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ\"\n"
                        + "           },\n"
                        + "           {\n"
                        + "               \"n\": 1,\n"
                        + "               \"asset\": \"0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\n"
                        + "               \"value\": \"99999990\",\n"
                        + "               \"address\": \"AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y\"\n"
                        + "           }\n"
                        + "      ],\n"
                        + "      \"sys_fee\": \"0\",\n"
                        + "      \"net_fee\": \"0\",\n"
                        + "      \"scripts\": [\n"
                        + "           {\n"
                        + "               \"invocation\": \"405797c43807e098a78014ae6c0e0f7b3c2565791dedc6753b9e821a0c3a565bdb5eb117ff5218be932b6f616f3d195c1417128b75e366589a83845a1a982c29d0\",\n"
                        + "               \"verification\": \"21031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac\"\n"
                        + "           }\n"
                        + "      ]\n"
                        + "   }\n"
                        + "}"
        );

        NeoSendToAddress sendToAddress = deserialiseResponse(NeoSendToAddress.class);
        assertThat(sendToAddress.getSendToAddress(), is(notNullValue()));
        assertThat(
                sendToAddress.getSendToAddress().getOutputs(),
                hasItems(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "10", "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ"),
                        new TransactionOutput(1, prependHexPrefix(NEOAsset.HASH_ID), "99999990", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
                )
        );
        assertThat(
                sendToAddress.getSendToAddress().getInputs(),
                hasItem(
                        new TransactionInput("0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0)
                )
        );
        assertThat(
                sendToAddress.getSendToAddress().getType(),
                is(TransactionType.CONTRACT_TRANSACTION)
        );
    }

    @Test
    public void testGetTransaction() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"txid\": \"0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6\",\n"
                        + "      \"size\": 283,\n"
                        + "      \"type\": \"ContractTransaction\",\n"
                        + "      \"version\": 0,\n"
                        + "      \"attributes\": ["
                        + "           {"
                        + "               \"usage\": 32,\n"
                        + "               \"data\": \"23ba2703c53263e8d6e522dc32203339dcd8eee9\"\n"
                        + "           }"
                        + "      ],\n"
                        + "      \"vin\": [\n"
                        + "           {\n"
                        + "               \"txid\": \"0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff\",\n"
                        + "               \"vout\": 0\n"
                        + "           }\n"
                        + "      ],\n"
                        + "      \"vout\": [\n"
                        + "           {\n"
                        + "               \"n\": 0,\n"
                        + "               \"asset\": \"0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\n"
                        + "               \"value\": \"10\",\n"
                        + "               \"address\": \"AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ\"\n"
                        + "           },\n"
                        + "           {\n"
                        + "               \"n\": 1,\n"
                        + "               \"asset\": \"0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\n"
                        + "               \"value\": \"99999990\",\n"
                        + "               \"address\": \"AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y\"\n"
                        + "           }\n"
                        + "      ],\n"
                        + "      \"sys_fee\": \"0\",\n"
                        + "      \"net_fee\": \"0\",\n"
                        + "      \"scripts\": [\n"
                        + "           {\n"
                        + "               \"invocation\": \"405797c43807e098a78014ae6c0e0f7b3c2565791dedc6753b9e821a0c3a565bdb5eb117ff5218be932b6f616f3d195c1417128b75e366589a83845a1a982c29d0\",\n"
                        + "               \"verification\": \"21031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac\"\n"
                        + "           }\n"
                        + "      ],\n"
                        + "      \"blockhash\": \"0x0c7ec8f8f952d7206b8ef82b6997a5f9ce44a88b356d3ca42a2a29457c608387\",\n"
                        + "      \"confirmations\": 200,\n"
                        + "      \"blocktime\": 1548704299\n"
                        + "   }\n"
                        + "}"
        );

        NeoGetTransaction getTransaction = deserialiseResponse(NeoGetTransaction.class);
        assertThat(getTransaction.getTransaction(), is(notNullValue()));
        assertThat(
                getTransaction.getTransaction().getTransactionId(),
                is("0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6")
        );
        assertThat(
                getTransaction.getTransaction().getSize(),
                is(283L)
        );
        assertThat(
                getTransaction.getTransaction().getType(),
                is(TransactionType.CONTRACT_TRANSACTION)
        );
        assertThat(
                getTransaction.getTransaction().getVersion(),
                is(0)
        );
        assertThat(
                getTransaction.getTransaction().getAttributes(),
                hasItem(
                        new TransactionAttribute(TransactionAttributeUsageType.SCRIPT, "23ba2703c53263e8d6e522dc32203339dcd8eee9")
                )
        );
        assertThat(
                getTransaction.getTransaction().getOutputs(),
                hasItems(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "10", "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ"),
                        new TransactionOutput(1, prependHexPrefix(NEOAsset.HASH_ID), "99999990", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
                )
        );
        assertThat(
                getTransaction.getTransaction().getInputs(),
                hasItem(
                        new TransactionInput("0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0)
                )
        );
        assertThat(
                getTransaction.getTransaction().getSysFee(),
                is("0")
        );
        assertThat(
                getTransaction.getTransaction().getNetFee(),
                is("0")
        );
        assertThat(
                getTransaction.getTransaction().getScripts(),
                hasItems(
                        new Script("405797c43807e098a78014ae6c0e0f7b3c2565791dedc6753b9e821a0c3a565bdb5eb117ff5218be932b6f616f3d195c1417128b75e366589a83845a1a982c29d0", "21031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac")
                )
        );
        assertThat(
                getTransaction.getTransaction().getBlockHash(),
                is("0x0c7ec8f8f952d7206b8ef82b6997a5f9ce44a88b356d3ca42a2a29457c608387")
        );
        assertThat(
                getTransaction.getTransaction().getConfirmations(),
                is(200L)
        );
        assertThat(
                getTransaction.getTransaction().getBlockTime(),
                is(1548704299L)
        );
    }

    @Test
    public void testGetRawTransaction() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"8000012023ba2703c53263e8d6e522dc32203339dcd8eee901ff8c509a090d440c0e3471709ef536f8e8d32caa2488ed8c64c6f7acf1d1a44b0000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b00000000295f83f83fc439f56e6e1fb062d89c6f538263d79b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500362634f286230023ba2703c53263e8d6e522dc32203339dcd8eee90141405797c43807e098a78014ae6c0e0f7b3c2565791dedc6753b9e821a0c3a565bdb5eb117ff5218be932b6f616f3d195c1417128b75e366589a83845a1a982c29d02321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac\"\n"
                        + "}"
        );

        NeoGetRawTransaction getRawTransaction = deserialiseResponse(NeoGetRawTransaction.class);
        assertThat(getRawTransaction.getRawTransaction(), is(notNullValue()));
        assertThat(
                getRawTransaction.getRawTransaction(),
                is("8000012023ba2703c53263e8d6e522dc32203339dcd8eee901ff8c509a090d440c0e3471709ef536f8e8d32caa2488ed8c64c6f7acf1d1a44b0000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b00000000295f83f83fc439f56e6e1fb062d89c6f538263d79b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500362634f286230023ba2703c53263e8d6e522dc32203339dcd8eee90141405797c43807e098a78014ae6c0e0f7b3c2565791dedc6753b9e821a0c3a565bdb5eb117ff5218be932b6f616f3d195c1417128b75e366589a83845a1a982c29d02321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac")
        );
    }

    @Test
    public void testGetBalance() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"balance\": \"199999990.0\",\n"
                        + "      \"confirmed\": \"99999990.0\"\n"
                        + "  }\n"
                        + "}"
        );

        NeoGetBalance getBalance = deserialiseResponse(NeoGetBalance.class);
        assertThat(getBalance.getBalance(), is(notNullValue()));
        assertThat(
                getBalance.getBalance().getBalance(),
                is("199999990.0")
        );
        assertThat(
                getBalance.getBalance().getConfirmed(),
                is("99999990.0")
        );
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
        assertThat(
                getBalance.getBalance().getBalance(),
                is("199999990.0")
        );
        assertThat(
                getBalance.getBalance().getConfirmed(),
                is("99999990.0")
        );
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
        assertThat(
                getBalance.getBalance().getBalance(),
                is("199999990.0")
        );
        assertThat(
                getBalance.getBalance().getConfirmed(),
                is(nullValue())
        );
    }

    @Test
    public void testGetAssetState_NeoCLI() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"version\": 0,\n"
                        + "      \"id\": \"0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\n"
                        + "      \"type\": \"GoverningToken\",\n"
                        + "      \"name\": [\n"
                        + "           {\n"
                        + "               \"lang\": \"zh-CN\",\n"
                        + "               \"name\": \"小蚁股\"\n"
                        + "           },\n"
                        + "           {\n"
                        + "               \"lang\": \"en\",\n"
                        + "               \"name\": \"AntShare\"\n"
                        + "           }\n"
                        + "      ],\n"
                        + "      \"amount\": \"100000000\",\n"
                        + "      \"available\": \"100000000\",\n"
                        + "      \"precision\": 0,\n"
                        + "      \"owner\": \"00\",\n"
                        + "      \"admin\": \"Abf2qMs1pzQb8kYk9RuxtUb9jtRKJVuBJt\",\n"
                        + "      \"issuer\": \"Abf2qMs1pzQb8kYk9RuxtUb9jtRKJVuBJt\",\n"
                        + "      \"expiration\": 4000000,\n"
                        + "      \"frozen\": false\n"
                        + "  }\n"
                        + "}"
        );

        NeoGetAssetState getAssetState = deserialiseResponse(NeoGetAssetState.class);
        assertThat(getAssetState.getAssetState(), is(notNullValue()));
        assertThat(
                getAssetState.getAssetState().getVersion(),
                is(0)
        );
        assertThat(
                getAssetState.getAssetState().getId(),
                is("0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b")
        );
        assertThat(
                getAssetState.getAssetState().getType(),
                is(AssetType.GOVERNING_TOKEN)
        );
        assertThat(
                getAssetState.getAssetState().getNames(),
                hasItems(
                        new NeoGetAssetState.AssetName("zh-CN", "小蚁股"),
                        new NeoGetAssetState.AssetName("en", "AntShare")
                )
        );
        assertThat(
                getAssetState.getAssetState().getAmount(),
                is("100000000")
        );
        assertThat(
                getAssetState.getAssetState().getAvailable(),
                is("100000000")
        );
        assertThat(
                getAssetState.getAssetState().getPrecision(),
                is(0)
        );
        assertThat(
                getAssetState.getAssetState().getFee(),
                is(nullValue())
        );
        assertThat(
                getAssetState.getAssetState().getAddress(),
                is(nullValue())
        );
        assertThat(
                getAssetState.getAssetState().getOwner(),
                is("00")
        );
        assertThat(
                getAssetState.getAssetState().getAdmin(),
                is("Abf2qMs1pzQb8kYk9RuxtUb9jtRKJVuBJt")
        );
        assertThat(
                getAssetState.getAssetState().getIssuer(),
                is("Abf2qMs1pzQb8kYk9RuxtUb9jtRKJVuBJt")
        );
        assertThat(
                getAssetState.getAssetState().getExpiration(),
                is(4000000L)
        );
        assertThat(
                getAssetState.getAssetState().getFrozen(),
                is(false)
        );
    }

    @Test
    public void testGetAssetState_NeoPython() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"assetId\": \"0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\n"
                        + "      \"assetType\": 0,\n"
                        + "      \"name\": \"NEO\",\n"
                        + "      \"amount\": 10000000000000000,\n"
                        + "      \"available\": 10000000000000000,\n"
                        + "      \"precision\": 0,\n"
                        + "      \"fee\": 0,\n"
                        + "      \"address\": \"0000000000000000000000000000000000000000\",\n"
                        + "      \"owner\": \"00\",\n"
                        + "      \"admin\": \"Abf2qMs1pzQb8kYk9RuxtUb9jtRKJVuBJt\",\n"
                        + "      \"issuer\": \"Abf2qMs1pzQb8kYk9RuxtUb9jtRKJVuBJt\",\n"
                        + "      \"expiration\": 4000000,\n"
                        + "      \"is_frozen\": false\n"
                        + "  }\n"
                        + "}"
        );

        NeoGetAssetState getAssetState = deserialiseResponse(NeoGetAssetState.class);
        assertThat(getAssetState.getAssetState(), is(notNullValue()));
        assertThat(
                getAssetState.getAssetState().getVersion(),
                is(nullValue())
        );
        assertThat(
                getAssetState.getAssetState().getId(),
                is("0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b")
        );
        assertThat(
                getAssetState.getAssetState().getType(),
                is(AssetType.GOVERNING_TOKEN)
        );
        assertThat(
                getAssetState.getAssetState().getNames(),
                hasItem(new NeoGetAssetState.AssetName("en", "NEO"))
        );
        assertThat(
                getAssetState.getAssetState().getAmount(),
                is("10000000000000000")
        );
        assertThat(
                getAssetState.getAssetState().getAvailable(),
                is("10000000000000000")
        );
        assertThat(
                getAssetState.getAssetState().getPrecision(),
                is(0)
        );
        assertThat(
                getAssetState.getAssetState().getFee(),
                is(0)
        );
        assertThat(
                getAssetState.getAssetState().getAddress(),
                is("0000000000000000000000000000000000000000")
        );
        assertThat(
                getAssetState.getAssetState().getOwner(),
                is("00")
        );
        assertThat(
                getAssetState.getAssetState().getAdmin(),
                is("Abf2qMs1pzQb8kYk9RuxtUb9jtRKJVuBJt")
        );
        assertThat(
                getAssetState.getAssetState().getIssuer(),
                is("Abf2qMs1pzQb8kYk9RuxtUb9jtRKJVuBJt")
        );
        assertThat(
                getAssetState.getAssetState().getExpiration(),
                is(4000000L)
        );
        assertThat(
                getAssetState.getAssetState().getFrozen(),
                is(false)
        );
    }

    @Test
    public void testSendMany() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"txid\": \"0x5fd1d2d417a1b26404827672c49953f4d517ba3893954cc9f08daa3c231b4c8d\",\n"
                        + "      \"size\": 343,\n"
                        + "      \"type\": \"ContractTransaction\",\n"
                        + "      \"version\": 0,\n"
                        + "      \"attributes\": ["
                        + "           {"
                        + "               \"usage\": 32,\n"
                        + "               \"data\": \"23ba2703c53263e8d6e522dc32203339dcd8eee9\"\n"
                        + "           }"
                        + "      ],\n"
                        + "      \"vin\": [\n"
                        + "           {\n"
                        + "               \"txid\": \"0xe728209ebbacf28c956d695b7f03221fe6760b7aa8c52fd34656bb56c8ae70da\",\n"
                        + "               \"vout\": 2\n"
                        + "           }\n"
                        + "      ],\n"
                        + "      \"vout\": [\n"
                        + "           {\n"
                        + "               \"n\": 0,\n"
                        + "               \"asset\": \"0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\n"
                        + "               \"value\": \"100\",\n"
                        + "               \"address\": \"AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2\"\n"
                        + "           },\n"
                        + "           {\n"
                        + "               \"n\": 1,\n"
                        + "               \"asset\": \"0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\n"
                        + "               \"value\": \"10\",\n"
                        + "               \"address\": \"AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2\"\n"
                        + "           },\n"
                        + "           {\n"
                        + "               \"n\": 2,\n"
                        + "               \"asset\": \"0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\n"
                        + "               \"value\": \"99999550\",\n"
                        + "               \"address\": \"AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y\"\n"
                        + "           }\n"
                        + "      ],\n"
                        + "      \"sys_fee\": \"0\",\n"
                        + "      \"net_fee\": \"0\",\n"
                        + "      \"scripts\": [\n"
                        + "           {\n"
                        + "               \"invocation\": \"4038a01e2354e0f80513cad7c0fe6a5e4bb92e34ebe9b4143e7ef1a0e03a2f56513277d9cf65483bd3286c4a357ae23cc36e4fbd3e19ad356a42ce90dee7ac232d\",\n"
                        + "               \"verification\": \"21031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac\"\n"
                        + "           }\n"
                        + "      ]\n"
                        + "   }\n"
                        + "}"
        );

        NeoSendMany sendMany = deserialiseResponse(NeoSendMany.class);
        assertThat(sendMany.getSendMany(), is(notNullValue()));
        assertThat(
                sendMany.getSendMany().getOutputs(),
                hasItems(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "100", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput(1, prependHexPrefix(NEOAsset.HASH_ID), "10", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput(2, prependHexPrefix(NEOAsset.HASH_ID), "99999550", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
                )
        );
        assertThat(
                sendMany.getSendMany().getInputs(),
                hasItem(
                        new TransactionInput("0xe728209ebbacf28c956d695b7f03221fe6760b7aa8c52fd34656bb56c8ae70da", 2)
                )
        );
        assertThat(
                sendMany.getSendMany().getType(),
                is(TransactionType.CONTRACT_TRANSACTION)
        );
    }

    @Test
    public void testDumpPrivKey() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK\"\n"
                        + "}"
        );

        NeoDumpPrivKey dumpPrivKey = deserialiseResponse(NeoDumpPrivKey.class);
        assertThat(dumpPrivKey.getDumpPrivKey(), is(notNullValue()));
        assertThat(
                dumpPrivKey.getDumpPrivKey(),
                is("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK")
        );
    }

    @Test
    public void testGetStorage() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"616e797468696e67\"\n"
                        + "}"
        );

        NeoGetStorage getStorage = deserialiseResponse(NeoGetStorage.class);
        assertThat(getStorage.getStorage(), is(notNullValue()));
        assertThat(getStorage.getStorage(), is("616e797468696e67"));
    }

    @Test
    public void testInvoke() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"script\": \"00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc\",\n"
                        + "      \"state\": \"HALT, BREAK\",\n"
                        + "      \"gas_consumed\": \"2.489\",\n"
                        + "      \"stack\": ["
                        + "           {"
                        + "               \"type\": \"ByteArray\",\n"
                        + "               \"value\": \"576f6f6c6f6e67\"\n"
                        + "           }"
                        + "      ],\n"
                        + "      \"tx\": \"d1011b00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc000000000000000000000000\"\n"
                        + "   }\n"
                        + "}"
        );

        NeoInvoke invoke = deserialiseResponse(NeoInvoke.class);
        assertThat(invoke.getInvocationResult(), is(notNullValue()));
        assertThat(invoke.getInvocationResult().getScript(), is("00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc"));
        assertThat(invoke.getInvocationResult().getState(), is("HALT, BREAK"));
        assertThat(invoke.getInvocationResult().getGasConsumed(), is("2.489"));
        assertThat(invoke.getInvocationResult().getStack(), hasSize(1));
        assertThat(
                invoke.getInvocationResult().getStack(),
                hasItems(
                        new ByteArrayStackItem(Numeric.hexStringToByteArray("576f6f6c6f6e67"))
                )
        );
        assertThat(invoke.getInvocationResult().getTx(), is("d1011b00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc000000000000000000000000"));
    }

    @Test
    public void testInvoke_empty_Stack() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"script\": \"00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc\",\n"
                        + "      \"state\": \"HALT, BREAK\",\n"
                        + "      \"gas_consumed\": \"2.489\",\n"
                        + "      \"stack\": [],\n"
                        + "      \"tx\": \"d1011b00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc000000000000000000000000\"\n"
                        + "   }\n"
                        + "}"
        );

        NeoInvoke invoke = deserialiseResponse(NeoInvoke.class);
        assertThat(invoke.getInvocationResult(), is(notNullValue()));
        assertThat(invoke.getInvocationResult().getScript(), is("00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc"));
        assertThat(invoke.getInvocationResult().getState(), is("HALT, BREAK"));
        assertThat(invoke.getInvocationResult().getGasConsumed(), is("2.489"));
        assertThat(invoke.getInvocationResult().getStack(), hasSize(0));
        assertThat(invoke.getInvocationResult().getTx(), is("d1011b00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc000000000000000000000000"));
    }

    @Test
    public void testInvokeFunction() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"script\": \"00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc\",\n"
                        + "      \"state\": \"HALT, BREAK\",\n"
                        + "      \"gas_consumed\": \"2.489\",\n"
                        + "      \"stack\": ["
                        + "           {"
                        + "               \"type\": \"ByteArray\",\n"
                        + "               \"value\": \"576f6f6c6f6e67\"\n"
                        + "           },"
                        + "           {"
                        + "               \"type\": \"Map\",\n"
                        + "               \"value\": ["
                        + "                   {"
                        + "                     \"key\": {"
                        + "                         \"type\": \"ByteArray\",\n"
                        + "                         \"value\": \"6964\"\n"
                        + "                     },"
                        + "                     \"value\": {"
                        + "                         \"type\": \"Integer\",\n"
                        + "                         \"value\": \"1\"\n"
                        + "                     }"
                        + "                   }"
                        + "               ]"
                        + "           }"
                        + "      ],\n"
                        + "      \"tx\": \"d1011b00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc000000000000000000000000\"\n"
                        + "   }\n"
                        + "}"
        );

        NeoInvokeFunction invokeFunction = deserialiseResponse(NeoInvokeFunction.class);
        assertThat(invokeFunction.getInvocationResult(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getScript(), is("00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc"));
        assertThat(invokeFunction.getInvocationResult().getState(), is("HALT, BREAK"));
        assertThat(invokeFunction.getInvocationResult().getGasConsumed(), is("2.489"));
        assertThat(invokeFunction.getInvocationResult().getStack(), hasSize(2));
        Map<StackItem, StackItem> stackMap = new HashMap<>();
        stackMap.put(new ByteArrayStackItem(Numeric.hexStringToByteArray("6964")), new IntegerStackItem(new BigInteger("1")));
        assertThat(
                invokeFunction.getInvocationResult().getStack(),
                hasItems(
                        new ByteArrayStackItem(Numeric.hexStringToByteArray("576f6f6c6f6e67")),
                        new MapStackItem(stackMap)
                )
        );

        assertThat(invokeFunction.getInvocationResult().getTx(), is("d1011b00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc000000000000000000000000"));
    }

    @Test
    public void testInvokeFunction_empty_Stack() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"script\": \"00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc\",\n"
                        + "      \"state\": \"HALT, BREAK\",\n"
                        + "      \"gas_consumed\": \"2.489\",\n"
                        + "      \"stack\": [],\n"
                        + "      \"tx\": \"d1011b00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc000000000000000000000000\"\n"
                        + "   }\n"
                        + "}"
        );

        NeoInvokeFunction invokeFunction = deserialiseResponse(NeoInvokeFunction.class);
        assertThat(invokeFunction.getInvocationResult(), is(notNullValue()));
        assertThat(invokeFunction.getInvocationResult().getScript(), is("00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc"));
        assertThat(invokeFunction.getInvocationResult().getState(), is("HALT, BREAK"));
        assertThat(invokeFunction.getInvocationResult().getGasConsumed(), is("2.489"));
        assertThat(invokeFunction.getInvocationResult().getStack(), hasSize(0));
        assertThat(invokeFunction.getInvocationResult().getTx(), is("d1011b00046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc000000000000000000000000"));
    }

    @Test
    public void testInvokeScript() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"script\": \"00046e616d656724058e5e1b6008847cd662728549088a9ee82191\",\n"
                        + "      \"state\": \"HALT, BREAK\",\n"
                        + "      \"gas_consumed\": \"0.161\",\n"
                        + "      \"stack\": ["
                        + "           {"
                        + "               \"type\": \"ByteArray\",\n"
                        + "               \"value\": \"4e45503520474153\"\n"
                        + "           }"
                        + "      ],\n"
                        + "      \"tx\": \"d1011b00046e616d656724058e5e1b6008847cd662728549088a9ee82191000000000000000000000000\"\n"
                        + "   }\n"
                        + "}"
        );

        NeoInvokeScript invokeScript = deserialiseResponse(NeoInvokeScript.class);
        assertThat(invokeScript.getInvocationResult(), is(notNullValue()));
        assertThat(invokeScript.getInvocationResult().getScript(), is("00046e616d656724058e5e1b6008847cd662728549088a9ee82191"));
        assertThat(invokeScript.getInvocationResult().getState(), is("HALT, BREAK"));
        assertThat(invokeScript.getInvocationResult().getGasConsumed(), is("0.161"));
        assertThat(invokeScript.getInvocationResult().getStack(), hasSize(1));
        assertThat(
                invokeScript.getInvocationResult().getStack(),
                hasItems(
                        new ByteArrayStackItem(Numeric.hexStringToByteArray("4e45503520474153"))
                )
        );
        assertThat(invokeScript.getInvocationResult().getTx(), is("d1011b00046e616d656724058e5e1b6008847cd662728549088a9ee82191000000000000000000000000"));
    }

    @Test
    public void testGetContractState() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"version\": 0,\n"
                        + "      \"hash\": \"0xdc675afc61a7c0f7b3d2682bf6e1d8ed865a0e5f\",\n"
                        + "      \"script\": \"5fc56b6c766b00527ac46c766b51527ac46107576f6f6c6f6e676c766b52527ac403574e476c766b53527ac4006c766b54527ac4210354ae498221046c666efebbaee9bd0eb4823469c98e748494a92a71f346b1a6616c766b55527ac46c766b00c3066465706c6f79876c766b56527ac46c766b56c36416006c766b55c36165f2026c766b57527ac462d8016c766b55c36165d801616c766b00c30b746f74616c537570706c79876c766b58527ac46c766b58c36440006168164e656f2e53746f726167652e476574436f6e7465787406737570706c79617c680f4e656f2e53746f726167652e4765746c766b57527ac46270016c766b00c3046e616d65876c766b59527ac46c766b59c36412006c766b52c36c766b57527ac46247016c766b00c30673796d626f6c876c766b5a527ac46c766b5ac36412006c766b53c36c766b57527ac4621c016c766b00c308646563696d616c73876c766b5b527ac46c766b5bc36412006c766b54c36c766b57527ac462ef006c766b00c30962616c616e63654f66876c766b5c527ac46c766b5cc36440006168164e656f2e53746f726167652e476574436f6e746578746c766b51c351c3617c680f4e656f2e53746f726167652e4765746c766b57527ac46293006c766b51c300c36168184e656f2e52756e74696d652e436865636b5769746e657373009c6c766b5d527ac46c766b5dc3640e00006c766b57527ac46255006c766b00c3087472616e73666572876c766b5e527ac46c766b5ec3642c006c766b51c300c36c766b51c351c36c766b51c352c36165d40361527265c9016c766b57527ac4620e00006c766b57527ac46203006c766b57c3616c756653c56b6c766b00527ac4616168164e656f2e53746f726167652e476574436f6e746578746c766b00c3617c680f4e656f2e53746f726167652e4765746165700351936c766b51527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b51c361651103615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e7465787406737570706c79617c680f4e656f2e53746f726167652e4765746165f40251936c766b52527ac46168164e656f2e53746f726167652e476574436f6e7465787406737570706c796c766b52c361659302615272680f4e656f2e53746f726167652e50757461616c756653c56b6c766b00527ac461516c766b51527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b51c361654002615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e7465787406737570706c796c766b51c361650202615272680f4e656f2e53746f726167652e50757461516c766b52527ac46203006c766b52c3616c756659c56b6c766b00527ac46c766b51527ac46c766b52527ac4616168164e656f2e53746f726167652e476574436f6e746578746c766b00c3617c680f4e656f2e53746f726167652e4765746c766b53527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b51c3617c680f4e656f2e53746f726167652e4765746c766b54527ac46c766b53c3616576016c766b52c3946c766b55527ac46c766b54c3616560016c766b52c3936c766b56527ac46c766b55c300a2640d006c766b52c300a2620400006c766b57527ac46c766b57c364ec00616168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b55c36165d800615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e746578746c766b51c36c766b56c361659c00615272680f4e656f2e53746f726167652e5075746155c57600135472616e73666572205375636365737366756cc476516c766b00c3c476526c766b51c3c476536c766b52c3c476546168184e656f2e426c6f636b636861696e2e476574486569676874c46168124e656f2e52756e74696d652e4e6f7469667961516c766b58527ac4620e00006c766b58527ac46203006c766b58c3616c756653c56b6c766b00527ac4616c766b00c36c766b51527ac46c766b51c36c766b52527ac46203006c766b52c3616c756653c56b6c766b00527ac461516c766b00c36a527a527ac46c766b51c36c766b52527ac46203006c766b52c3616c7566\",\n"
                        + "      \"parameters\": ["
                        + "           \"ByteArray\"\n"
                        + "      ],\n"
                        + "      \"returntype\": \"ByteArray\",\n"
                        + "      \"name\": \"Contract Name\",\n"
                        + "      \"code_version\": \"0.0.1\",\n"
                        + "      \"author\": \"Author Name\",\n"
                        + "      \"email\": \"blah@blah.com\",\n"
                        + "      \"description\": \"GO NEO!!!\",\n"
                        + "      \"properties\": {"
                        + "           \"storage\": true,\n"
                        + "           \"dynamic_invoke\": false\n"
                        + "      }\n"
                        + "   }\n"
                        + "}"
        );

        NeoGetContractState getContractState = deserialiseResponse(NeoGetContractState.class);
        assertThat(getContractState.getContractState(), is(notNullValue()));
        assertThat(getContractState.getContractState().getVersion(), is(0));
        assertThat(getContractState.getContractState().getHash(), is("0xdc675afc61a7c0f7b3d2682bf6e1d8ed865a0e5f"));
        assertThat(getContractState.getContractState().getScript(), is("5fc56b6c766b00527ac46c766b51527ac46107576f6f6c6f6e676c766b52527ac403574e476c766b53527ac4006c766b54527ac4210354ae498221046c666efebbaee9bd0eb4823469c98e748494a92a71f346b1a6616c766b55527ac46c766b00c3066465706c6f79876c766b56527ac46c766b56c36416006c766b55c36165f2026c766b57527ac462d8016c766b55c36165d801616c766b00c30b746f74616c537570706c79876c766b58527ac46c766b58c36440006168164e656f2e53746f726167652e476574436f6e7465787406737570706c79617c680f4e656f2e53746f726167652e4765746c766b57527ac46270016c766b00c3046e616d65876c766b59527ac46c766b59c36412006c766b52c36c766b57527ac46247016c766b00c30673796d626f6c876c766b5a527ac46c766b5ac36412006c766b53c36c766b57527ac4621c016c766b00c308646563696d616c73876c766b5b527ac46c766b5bc36412006c766b54c36c766b57527ac462ef006c766b00c30962616c616e63654f66876c766b5c527ac46c766b5cc36440006168164e656f2e53746f726167652e476574436f6e746578746c766b51c351c3617c680f4e656f2e53746f726167652e4765746c766b57527ac46293006c766b51c300c36168184e656f2e52756e74696d652e436865636b5769746e657373009c6c766b5d527ac46c766b5dc3640e00006c766b57527ac46255006c766b00c3087472616e73666572876c766b5e527ac46c766b5ec3642c006c766b51c300c36c766b51c351c36c766b51c352c36165d40361527265c9016c766b57527ac4620e00006c766b57527ac46203006c766b57c3616c756653c56b6c766b00527ac4616168164e656f2e53746f726167652e476574436f6e746578746c766b00c3617c680f4e656f2e53746f726167652e4765746165700351936c766b51527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b51c361651103615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e7465787406737570706c79617c680f4e656f2e53746f726167652e4765746165f40251936c766b52527ac46168164e656f2e53746f726167652e476574436f6e7465787406737570706c796c766b52c361659302615272680f4e656f2e53746f726167652e50757461616c756653c56b6c766b00527ac461516c766b51527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b51c361654002615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e7465787406737570706c796c766b51c361650202615272680f4e656f2e53746f726167652e50757461516c766b52527ac46203006c766b52c3616c756659c56b6c766b00527ac46c766b51527ac46c766b52527ac4616168164e656f2e53746f726167652e476574436f6e746578746c766b00c3617c680f4e656f2e53746f726167652e4765746c766b53527ac46168164e656f2e53746f726167652e476574436f6e746578746c766b51c3617c680f4e656f2e53746f726167652e4765746c766b54527ac46c766b53c3616576016c766b52c3946c766b55527ac46c766b54c3616560016c766b52c3936c766b56527ac46c766b55c300a2640d006c766b52c300a2620400006c766b57527ac46c766b57c364ec00616168164e656f2e53746f726167652e476574436f6e746578746c766b00c36c766b55c36165d800615272680f4e656f2e53746f726167652e507574616168164e656f2e53746f726167652e476574436f6e746578746c766b51c36c766b56c361659c00615272680f4e656f2e53746f726167652e5075746155c57600135472616e73666572205375636365737366756cc476516c766b00c3c476526c766b51c3c476536c766b52c3c476546168184e656f2e426c6f636b636861696e2e476574486569676874c46168124e656f2e52756e74696d652e4e6f7469667961516c766b58527ac4620e00006c766b58527ac46203006c766b58c3616c756653c56b6c766b00527ac4616c766b00c36c766b51527ac46c766b51c36c766b52527ac46203006c766b52c3616c756653c56b6c766b00527ac461516c766b00c36a527a527ac46c766b51c36c766b52527ac46203006c766b52c3616c7566"));
        assertThat(getContractState.getContractState().getContractParameters(), hasSize(1));
        assertThat(getContractState.getContractState().getContractParameters(), hasItems(ContractParameterType.BYTE_ARRAY));
        assertThat(getContractState.getContractState().getReturnContractType(), is(ContractParameterType.BYTE_ARRAY));
        assertThat(getContractState.getContractState().getName(), is("Contract Name"));
        assertThat(getContractState.getContractState().getCodeVersion(), is("0.0.1"));
        assertThat(getContractState.getContractState().getAuthor(), is("Author Name"));
        assertThat(getContractState.getContractState().getEmail(), is("blah@blah.com"));
        assertThat(getContractState.getContractState().getDescription(), is("GO NEO!!!"));
        assertThat(getContractState.getContractState().getProperties(), is(notNullValue()));
        assertThat(getContractState.getContractState().getProperties(), is(new NeoGetContractState.ContractStateProperties(true, false)));
    }

    @Test
    public void testSubmitBlock() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}"
        );

        NeoSubmitBlock submitBlock = deserialiseResponse(NeoSubmitBlock.class);
        assertThat(submitBlock.getSubmitBlock(), is(notNullValue()));
        assertThat(
                submitBlock.getSubmitBlock(),
                is(true)
        );
    }

    @Test
    public void testGetUnspents() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"balance\": [\n"
                        + "           {\n"
                        + "               \"unspent\": [\n"
                        + "                    {\n"
                        + "                         \"txid\": \"4ee4af75d5aa60598fbae40ce86fb9a23ffec5a75dfa8b59d259d15f9e304319\",\n"
                        + "                         \"n\": 0,\n"
                        + "                         \"value\": 27844.821\n"
                        + "                    },\n"
                        + "                    {\n"
                        + "                         \"txid\": \"9906bf2a9f531ac523aad5e9507bd6540acc1c65ae9144918ccc891188578253\",\n"
                        + "                         \"n\": 0,\n"
                        + "                         \"value\": 0.987\n"
                        + "                    },\n"
                        + "                    {\n"
                        + "                         \"txid\": \"184e34eb3f9550d07d03563391d73eb6c438130c7fdca37f0700d5d52ad7deb1\",\n"
                        + "                         \"n\": 0,\n"
                        + "                         \"value\": 243.95598\n"
                        + "                    },\n"
                        + "                    {\n"
                        + "                         \"txid\": \"448abc64412284fb21c9625ac9edd2100090367a551c18ce546c1eded61e77c3\",\n"
                        + "                         \"n\": 0,\n"
                        + "                         \"value\": 369.84904\n"
                        + "                    },\n"
                        + "                    {\n"
                        + "                         \"txid\": \"bd454059e58da4221aaf4effa3278660b231e9af7cea97912f4ac5c4995bb7e4\",\n"
                        + "                         \"n\": 0,\n"
                        + "                         \"value\": 600.41014479\n"
                        + "                    }\n"
                        + "               ],\n"
                        + "               \"asset_hash\": \"602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7\",\n"
                        + "               \"asset\": \"GAS\",\n"
                        + "               \"asset_symbol\": \"GAS\",\n"
                        + "               \"amount\": 29060.02316479\n"
                        + "           },\n"
                        + "           {\n"
                        + "               \"unspent\": [\n"
                        + "                    {\n"
                        + "                         \"txid\": \"c3182952855314b3f4b1ecf01a03b891d4627d19426ce841275f6d4c186e729a\",\n"
                        + "                         \"n\": 0,\n"
                        + "                         \"value\": 800000\n"
                        + "                    }\n"
                        + "               ],\n"
                        + "               \"asset_hash\": \"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\n"
                        + "               \"asset\": \"NEO\",\n"
                        + "               \"asset_symbol\": \"NEO\",\n"
                        + "               \"amount\": 800000\n"
                        + "           }\n"
                        + "      ],\n"
                        + "      \"address\": \"AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y\"\n"
                        + "   }\n"
                        + "}"
        );

        NeoGetUnspents getUnspents = deserialiseResponse(NeoGetUnspents.class);
        assertThat(getUnspents.getUnspents(), is(notNullValue()));
        assertThat(
                getUnspents.getUnspents().getAddress(),
                is("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
        );
        assertThat(
                getUnspents.getUnspents().getBalances(),
                hasSize(2)
        );
        // First balance entry:
        assertThat(
                getUnspents.getUnspents().getBalances().get(0).getAssetHash(),
                is("602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7")
        );
        assertThat(
                getUnspents.getUnspents().getBalances().get(0).getAssetName(),
                is("GAS")
        );
        assertThat(
                getUnspents.getUnspents().getBalances().get(0).getAssetSymbol(),
                is("GAS")
        );
        assertThat(
                getUnspents.getUnspents().getBalances().get(0).getAmount(),
                is(new BigDecimal("29060.02316479"))
        );
        assertThat(
                getUnspents.getUnspents().getBalances().get(0).getUnspentTransactions(),
                hasItems(
                        new NeoGetUnspents.UnspentTransaction("4ee4af75d5aa60598fbae40ce86fb9a23ffec5a75dfa8b59d259d15f9e304319", 0, new BigDecimal("27844.821")),
                        new NeoGetUnspents.UnspentTransaction("9906bf2a9f531ac523aad5e9507bd6540acc1c65ae9144918ccc891188578253", 0, new BigDecimal("0.987")),
                        new NeoGetUnspents.UnspentTransaction("184e34eb3f9550d07d03563391d73eb6c438130c7fdca37f0700d5d52ad7deb1", 0, new BigDecimal("243.95598")),
                        new NeoGetUnspents.UnspentTransaction("448abc64412284fb21c9625ac9edd2100090367a551c18ce546c1eded61e77c3", 0, new BigDecimal("369.84904")),
                        new NeoGetUnspents.UnspentTransaction("bd454059e58da4221aaf4effa3278660b231e9af7cea97912f4ac5c4995bb7e4", 0, new BigDecimal("600.41014479"))
                )
        );
        // Second balance entry:
        assertThat(
                getUnspents.getUnspents().getBalances().get(1).getAssetHash(),
                is("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b")
        );
        assertThat(
                getUnspents.getUnspents().getBalances().get(1).getAssetName(),
                is("NEO")
        );
        assertThat(
                getUnspents.getUnspents().getBalances().get(1).getAssetSymbol(),
                is("NEO")
        );
        assertThat(
                getUnspents.getUnspents().getBalances().get(1).getAmount(),
                is(new BigDecimal("800000"))
        );
        assertThat(
                getUnspents.getUnspents().getBalances().get(1).getUnspentTransactions(),
                hasItems(
                        new NeoGetUnspents.UnspentTransaction("c3182952855314b3f4b1ecf01a03b891d4627d19426ce841275f6d4c186e729a", 0, new BigDecimal("800000"))
                )
        );
    }

    @Test
    public void testGetNep5Balances() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": {\n"
                        + "      \"balance\": [\n"
                        + "           {\n"
                        + "               \"asset_hash\": \"a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8\",\n"
                        + "               \"amount\": \"50000000000\",\n"
                        + "               \"last_updated_block\": 251604\n"
                        + "           },\n"
                        + "           {\n"
                        + "               \"asset_hash\": \"1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3\",\n"
                        + "               \"amount\": \"50000000000\",\n"
                        + "               \"last_updated_block\": 251600\n"
                        + "           }\n"
                        + "      ],\n"
                        + "      \"address\": \"AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y\"\n"
                        + "   }\n"
                        + "}"
        );

        NeoGetNep5Balances getNep5Balances = deserialiseResponse(NeoGetNep5Balances.class);
        assertThat(getNep5Balances.getResult(), is(notNullValue()));
        assertThat(
                getNep5Balances.getResult().getAddress(),
                is("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
        );
        assertThat(
                getNep5Balances.getResult().getBalances(),
                hasSize(2)
        );
        // First entry:
        assertThat(
                getNep5Balances.getResult().getBalances().get(0).getAssetHash(),
                is("a48b6e1291ba24211ad11bb90ae2a10bf1fcd5a8")
        );
        assertThat(
                getNep5Balances.getResult().getBalances().get(0).getAmount(),
                is("50000000000")
        );
        assertThat(
                getNep5Balances.getResult().getBalances().get(0).getLastUpdatedBlock(),
                is(new BigInteger("251604"))
        );
        // Second entry:
        assertThat(
                getNep5Balances.getResult().getBalances().get(1).getAssetHash(),
                is("1aada0032aba1ef6d1f07bbd8bec1d85f5380fb3")
        );
        assertThat(
                getNep5Balances.getResult().getBalances().get(1).getAmount(),
                is("50000000000")
        );
        assertThat(
                getNep5Balances.getResult().getBalances().get(1).getLastUpdatedBlock(),
                is(new BigInteger("251600"))
        );
    }

    @Test
    public void testGetClaimable() {
        buildResponse(
                "{\n"
                        + "    \"jsonrpc\": \"2.0\",\n"
                        + "    \"id\": 1,\n"
                        + "    \"result\": {\n"
                        + "        \"claimable\": [\n"
                        + "            {\n"
                        + "                \"txid\": \"2d7c565c19bfcd288856d6ac16ab69cf17fbf14d7d848f00ea32532c7db6a506\",\n"
                        + "                \"n\": 2,\n"
                        + "                \"value\": 700,\n"
                        + "                \"start_height\": 2025847,\n"
                        + "                \"end_height\": 2532062,\n"
                        + "                \"generated\": 24.804535,\n"
                        + "                \"sys_fee\": 2.229262,\n"
                        + "                \"unclaimed\": 27.033797\n"
                        + "            },\n"
                        + "            {\n"
                        + "                \"txid\": \"c78eb5a5b148af6a0ef04bf36d5da2d84b17eb2a1d27669b41be95e6f264dd5b\",\n"
                        + "                \"n\": 1,\n"
                        + "                \"value\": 795,\n"
                        + "                \"start_height\": 1866619,\n"
                        + "                \"end_height\": 2025847,\n"
                        + "                \"generated\": 9.92141715,\n"
                        + "                \"sys_fee\": 1.4571873,\n"
                        + "                \"unclaimed\": 11.37860445\n"
                        + "            }\n"
                        + "        ],\n"
                        + "        \"address\": \"ATBTRWX8v8teMHCvPXovir3Hy92RPnwdEi\",\n"
                        + "        \"unclaimed\": 38.41240145\n"
                        + "    }\n"
                        + "}"
        );

        Claimables claimables = deserialiseResponse(NeoGetClaimable.class).getResult();
        assertThat(claimables, is(notNullValue()));
        assertThat(claimables.getAddress(), is("ATBTRWX8v8teMHCvPXovir3Hy92RPnwdEi"));
        assertThat(claimables.getTotalUnclaimed(), is("38.41240145"));
        assertThat(claimables.getClaims(), hasSize(2));

        Claim claim1 = claimables.getClaims().get(0);
        assertThat(claim1.getTxId(), is("2d7c565c19bfcd288856d6ac16ab69cf17fbf14d7d848f00ea32532c7db6a506"));
        assertThat(claim1.getIndex(), is(2));
        assertThat(claim1.getNeoValue(), is(new BigInteger("700")));
        assertThat(claim1.getStartHeight(), is(new BigInteger("2025847")));
        assertThat(claim1.getEndHeight(), is(new BigInteger("2532062")));
        assertThat(claim1.getGeneratedGas(), is("24.804535"));
        assertThat(claim1.getSystemFee(), is("2.229262"));
        assertThat(claim1.getUnclaimedGas(), is("27.033797"));

        Claim claim2 = claimables.getClaims().get(1);
        assertThat(claim2.getTxId(), is("c78eb5a5b148af6a0ef04bf36d5da2d84b17eb2a1d27669b41be95e6f264dd5b"));
        assertThat(claim2.getIndex(), is(1));
        assertThat(claim2.getNeoValue(), is(new BigInteger("795")));
        assertThat(claim2.getStartHeight(), is(new BigInteger("1866619")));
        assertThat(claim2.getEndHeight(), is(new BigInteger("2025847")));
        assertThat(claim2.getGeneratedGas(), is("9.92141715"));
        assertThat(claim2.getSystemFee(), is("1.4571873"));
        assertThat(claim2.getUnclaimedGas(), is("11.37860445"));
    }

    @Test
    public void testApplicationLog() {
        buildResponse(
                "{\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"id\": 1,\n" +
                        "  \"result\": {\n" +
                        "    \"txid\": \"0x420d1eb458c707d698c6d2ba0f91327918ddb3b7bae2944df070f3f4e579078b\",\n" +
                        "    \"executions\": [\n" +
                        "      {\n" +
                        "        \"trigger\": \"Application\",\n" +
                        "        \"contract\": \"0x857477dd9457d09aff11fc4a791a247a42dbb17f\",\n" +
                        "        \"vmstate\": \"HALT, BREAK\",\n" +
                        "        \"gas_consumed\": \"0.173\",\n" +
                        "        \"stack\": [\n" +
                        "          {\n" +
                        "            \"type\": \"ByteArray\",\n" +
                        "            \"value\": \"b100\"\n" +
                        "          }\n" +
                        "        ],\n" +
                        "        \"notifications\": [\n" +
                        "          {\n" +
                        "            \"contract\": \"0x43fa0777cf984faea46b954ec640a266bcbc3319\",\n" +
                        "            \"state\": {\n" +
                        "              \"type\": \"Array\",\n" +
                        "              \"value\": [\n" +
                        "                {\n" +
                        "                  \"type\": \"ByteArray\",\n" +
                        "                  \"value\": \"72656164\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                  \"type\": \"ByteArray\",\n" +
                        "                  \"value\": \"10d46912932d6ebcd1d3c4a27a1a8ea77e68ac95\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                  \"type\": \"ByteArray\",\n" +
                        "                  \"value\": \"b100\"\n" +
                        "                }\n" +
                        "              ]\n" +
                        "            }\n" +
                        "          },\n" +
                        "          {\n" +
                        "             \"contract\": \"0xef182f4977544adb207507b0c8c6c3ec1749c7df\",\n" +
                        "             \"state\": {\n" +
                        "               \"type\": \"Map\",\n" +
                        "               \"value\": [\n" +
                        "                 {\n" +
                        "                   \"key\": {\n" +
                        "                     \"type\": \"ByteArray\",\n" +
                        "                     \"value\": \"746573745f6b65795f61\"\n" +
                        "                   },\n" +
                        "                   \"value\": {\n" +
                        "                     \"type\": \"ByteArray\",\n" +
                        "                     \"value\": \"54657374206d657373616765\"\n" +
                        "                   }\n" +
                        "                 },\n" +
                        "                 {\n" +
                        "                   \"key\": {\n" +
                        "                     \"type\": \"ByteArray\",\n" +
                        "                     \"value\": \"746573745f6b65795f62\"\n" +
                        "                   },\n" +
                        "                   \"value\": {\n" +
                        "                     \"type\": \"Integer\",\n" +
                        "                     \"value\": \"12345\"\n" +
                        "                   }\n" +
                        "                 }\n" +
                        "               ]\n" +
                        "             }\n" +
                        "           }" +
                        "        ]\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}"
        );

        NeoGetApplicationLog applicationLog = deserialiseResponse(NeoGetApplicationLog.class);
        assertThat(applicationLog.getApplicationLog().getTransactionId(), is("0x420d1eb458c707d698c6d2ba0f91327918ddb3b7bae2944df070f3f4e579078b"));
        assertThat(applicationLog.getApplicationLog().getExecutions(), hasSize(1));

        NeoApplicationLog.Execution execution = applicationLog.getApplicationLog().getExecutions().get(0);

        assertThat(execution.getTrigger(), is("Application"));
        assertThat(execution.getContract(), is("0x857477dd9457d09aff11fc4a791a247a42dbb17f"));
        assertThat(execution.getState(), is("HALT, BREAK"));
        assertThat(execution.getGasConsumed(), is("0.173"));
        assertThat(execution.getStack(), hasSize(1));
        assertThat(execution.getNotifications(), hasSize(2));

        List<NeoApplicationLog.Notification> notifications = execution.getNotifications();

        assertThat(notifications.get(0).getContract(), is("0x43fa0777cf984faea46b954ec640a266bcbc3319"));
        assertThat(notifications.get(0).getState().getType(), is(StackItemType.ARRAY));
        assertTrue(notifications.get(0).getState() instanceof ArrayStackItem);

        ArrayStackItem array = notifications.get(0).getState().asArray();

        String eventName = array.get(0).asByteArray().getAsString();
        String address = array.get(1).asByteArray().getAsAddress();
        BigInteger amount = array.get(2).asByteArray().getAsNumber();

        assertThat(eventName, is("read"));
        assertThat(address, is("AHJrv6y6L6k9PfJvY7vtX3XTAmEprsd3Xn"));
        assertThat(amount, is(BigInteger.valueOf(177)));

        assertThat(notifications.get(1).getContract(), is("0xef182f4977544adb207507b0c8c6c3ec1749c7df"));
        assertTrue(notifications.get(1).getState() instanceof MapStackItem);
        assertThat(notifications.get(1).getState().getType(), is(StackItemType.MAP));
        MapStackItem map = notifications.get(1).getState().asMap();
        assertThat(map.size(), is(2));

        String textValue = map.get("test_key_a").asByteArray().getAsString();
        BigInteger intValue = map.get("test_key_b").asInteger().getValue();

        assertThat(textValue, is("Test message"));
        assertThat(intValue, is(BigInteger.valueOf(12345)));
    }

    @Test
    public void testListPlugins() {
        buildResponse(
            "{\n"
                + "    \"jsonrpc\": \"2.0\",\n"
                + "    \"id\": 1,\n"
                + "    \"result\": [\n"
                + "        {\n"
                + "            \"name\": \"ApplicationLogs\",\n"
                + "            \"version\": \"2.10.2.0\",\n"
                + "            \"interfaces\": [\n"
                + "                \"IRpcPlugin\",\n"
                + "                \"IPersistencePlugin\"\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"RpcSystemAssetTrackerPlugin\",\n"
                + "            \"version\": \"2.10.2.0\",\n"
                + "            \"interfaces\": [\n"
                + "                \"IPersistencePlugin\",\n"
                + "                \"IRpcPlugin\"\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"SimplePolicyPlugin\",\n"
                + "            \"version\": \"2.10.2.0\",\n"
                + "            \"interfaces\": [\n"
                + "                \"ILogPlugin\",\n"
                + "                \"IPolicyPlugin\"\n"
                + "            ]\n"
                + "        }\n"
                + "    ]\n"
                + "}"
        );

        List<NeoListPlugins.Plugin> plugins = deserialiseResponse(NeoListPlugins.class).getPlugins();
        assertNotNull(plugins);
        assertEquals(3, plugins.size());

        NeoListPlugins.Plugin plugin = plugins.get(0);
        assertEquals(NodePluginType.APPLICATION_LOGS, NodePluginType.valueOfName(plugin.getName()));
        assertEquals("2.10.2.0", plugin.getVersion());
        assertNotNull(plugin.getInterfaces());
        assertEquals(2, plugin.getInterfaces().size());
        assertEquals("IRpcPlugin", plugin.getInterfaces().get(0));
        assertEquals("IPersistencePlugin", plugin.getInterfaces().get(1));

        plugin = plugins.get(1);
        assertEquals(NodePluginType.RPC_SYSTEM_ASSET_TRACKER, NodePluginType.valueOfName(plugin.getName()));
        assertEquals("2.10.2.0", plugin.getVersion());
        assertNotNull(plugin.getInterfaces());
        assertEquals(2, plugin.getInterfaces().size());
        assertEquals("IPersistencePlugin", plugin.getInterfaces().get(0));
        assertEquals("IRpcPlugin", plugin.getInterfaces().get(1));

        plugin = plugins.get(2);
        assertEquals(NodePluginType.SIMPLE_POLICY, NodePluginType.valueOfName(plugin.getName()));
        assertEquals("2.10.2.0", plugin.getVersion());
        assertNotNull(plugin.getInterfaces());
        assertEquals(2, plugin.getInterfaces().size());
        assertEquals("ILogPlugin", plugin.getInterfaces().get(0));
        assertEquals("IPolicyPlugin", plugin.getInterfaces().get(1));
    }
}
