package com.axlabs.neow3j.protocol.core;

import com.axlabs.neow3j.protocol.Neow3j;
import com.axlabs.neow3j.protocol.RequestTester;
import com.axlabs.neow3j.protocol.http.HttpService;
import org.junit.Test;

public class RequestTest extends RequestTester {

    private Neow3j neow3j;

    @Override
    protected void initWeb3Client(HttpService httpService) {
        neow3j = Neow3j.build(httpService);
    }

    @Test
    public void testWeb3ClientVersion() throws Exception {
        neow3j.getVersion().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getversion\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testGetBestBlockHash() throws Exception {
        neow3j.getBestBlockHash().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getbestblockhash\",\"params\":[],\"id\":1}");
    }


    @Test
    public void testGetBlockHash() throws Exception {
        neow3j.getBlockHash(new BlockParameterIndex(16293)).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getblockhash\","
                        + "\"params\":[16293],\"id\":1}");
    }

    @Test
    public void testGetConnectionCount() throws Exception {
        neow3j.getConnectionCount().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getconnectioncount\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testListAddress() throws Exception {
        neow3j.listAddress().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"listaddress\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testGetPeers() throws Exception {
        neow3j.getPeers().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getpeers\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testGetRawMemPool() throws Exception {
        neow3j.getRawMemPool().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getrawmempool\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testGetValidators() throws Exception {
        neow3j.getValidators().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getvalidators\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testValidateAddress() throws Exception {
        neow3j.validateAddress("AQVh2pG732YvtNaxEGkQUei3YA4cvo7d2i").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"validateaddress\","
                        + "\"params\":[\"AQVh2pG732YvtNaxEGkQUei3YA4cvo7d2i\"],\"id\":1}");
    }

    @Test
    public void testGetBlock_Index() throws Exception {
        neow3j.getBlock(new BlockParameterIndex(12345), true).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getblock\","
                        + "\"params\":[12345,1],\"id\":1}");
    }

    @Test
    public void testGetRawBlock_Index() throws Exception {
        neow3j.getRawBlock(new BlockParameterIndex(12345)).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getblock\","
                        + "\"params\":[12345,0],\"id\":1}");
    }

    @Test
    public void testGetBlock_Hash() throws Exception {
        neow3j.getBlock("0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d", true).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getblock\","
                        + "\"params\":[\"0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d\",1],\"id\":1}");
    }

    @Test
    public void testGetRawBlock_Hash() throws Exception {
        neow3j.getRawBlock("0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getblock\","
                        + "\"params\":[\"0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d\",0],\"id\":1}");
    }

    @Test
    public void testGetAccountState() throws Exception {
        neow3j.getAccountState("AQVh2pG732YvtNaxEGkQUei3YA4cvo7d2i").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getaccountstate\","
                        + "\"params\":[\"AQVh2pG732YvtNaxEGkQUei3YA4cvo7d2i\"],\"id\":1}");
    }

    @Test
    public void testGetBlockHeader_Hash() throws Exception {
        neow3j.getBlockHeader("0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getblockheader\","
                        + "\"params\":[\"0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d\",1],\"id\":1}");
    }

    @Test
    public void testGetBlockHeader_Index() throws Exception {
        neow3j.getBlockHeader(new BlockParameterIndex(12345)).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getblockheader\","
                        + "\"params\":[12345,1],\"id\":1}");
    }

    @Test
    public void testGetRawBlockHeader_Hash() throws Exception {
        neow3j.getRawBlockHeader("0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getblockheader\","
                        + "\"params\":[\"0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d\",0],\"id\":1}");
    }

    @Test
    public void testGetNewAddress() throws Exception {
        neow3j.getNewAddress().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getnewaddress\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testGetWalletHeight() throws Exception {
        neow3j.getWalletHeight().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getwalletheight\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testGetBlockSysFee() throws Exception {
        neow3j.getBlockSysFee(new BlockParameterIndex(12345)).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getblocksysfee\","
                        + "\"params\":[12345],\"id\":1}");
    }

    @Test
    public void testGetTxOut() throws Exception {
        neow3j.getTxOut("0x93c569cbe33e918f7a5392025fbdeab5f6c97c8e5897fafc466694b6e8e1b0d2", 0).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"gettxout\","
                        + "\"params\":[\"0x93c569cbe33e918f7a5392025fbdeab5f6c97c8e5897fafc466694b6e8e1b0d2\",0],\"id\":1}");
    }

}
