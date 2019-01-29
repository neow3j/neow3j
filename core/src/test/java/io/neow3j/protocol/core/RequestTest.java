package io.neow3j.protocol.core;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.RequestTester;
import io.neow3j.protocol.core.methods.response.TransactionOutput;
import io.neow3j.protocol.http.HttpService;
import org.junit.Test;

import java.util.Arrays;

public class RequestTest extends RequestTester {

    private Neow3j neow3j;

    @Override
    protected void initWeb3Client(HttpService httpService) {
        neow3j = Neow3j.build(httpService);
    }

    @Test
    public void testGetVersion() throws Exception {
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
    public void testGetBlockCount() throws Exception {
        neow3j.getBlockCount().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getblockcount\","
                        + "\"params\":[],\"id\":1}");
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
    public void testGetRawBlockHeader_Index() throws Exception {
        neow3j.getRawBlockHeader(new BlockParameterIndex(12345)).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getblockheader\","
                        + "\"params\":[12345,0],\"id\":1}");
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

    @Test
    public void testSendRawTransaction() throws Exception {
        neow3j.sendRawTransaction("80000001d405ab03e736a01ca277d94b1377113c7e961bb4550511fe1d408f30c77a82650000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b0000000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc5001a711802000000295f83f83fc439f56e6e1fb062d89c6f538263d70141403711e366fc99e77a110b6c96b5f8828ef956a6d5cfa5cb63273419149011b0f30dc5458faa59e4867d0ac7537e324c98124bb691feca5c5ddf6ed20f4adb778223210265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6ac").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"sendrawtransaction\","
                        + "\"params\":[\"80000001d405ab03e736a01ca277d94b1377113c7e961bb4550511fe1d408f30c77a82650000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b0000000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc5001a711802000000295f83f83fc439f56e6e1fb062d89c6f538263d70141403711e366fc99e77a110b6c96b5f8828ef956a6d5cfa5cb63273419149011b0f30dc5458faa59e4867d0ac7537e324c98124bb691feca5c5ddf6ed20f4adb778223210265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6ac\"],\"id\":1}");
    }

    @Test
    public void testSendToAddress() throws Exception {
        neow3j.sendToAddress(
                "c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b",
                "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ",
                "10.0"
        ).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"sendtoaddress\","
                        + "\"params\":[\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\","
                        + "\"AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ\",\"10.0\"],\"id\":1}");
    }

    @Test
    public void testSendToAddress_Fee() throws Exception {
        neow3j.sendToAddress(
                "c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b",
                "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ",
                "10.0",
                "0.01"
        ).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"sendtoaddress\","
                        + "\"params\":[\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\","
                        + "\"AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ\","
                        + "\"10.0\","
                        + "\"0.01\"],\"id\":1}");
    }

    @Test
    public void testSendToAddress_Fee_And_ChangeAddress() throws Exception {
        neow3j.sendToAddress(
                "c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b",
                "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ",
                "10.0",
                "0.01",
                "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"
        ).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"sendtoaddress\","
                        + "\"params\":[\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\","
                        + "\"AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ\","
                        + "\"10.0\","
                        + "\"0.01\","
                        + "\"AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y\"],\"id\":1}");
    }

    @Test
    public void testGetTransaction() throws Exception {
        neow3j.getTransaction("0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getrawtransaction\","
                        + "\"params\":[\"0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6\",1],\"id\":1}");
    }

    @Test
    public void testGetRawTransaction() throws Exception {
        neow3j.getRawTransaction("0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getrawtransaction\","
                        + "\"params\":[\"0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6\",0],\"id\":1}");
    }

    @Test
    public void testGetBalance() throws Exception {
        neow3j.getBalance("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getbalance\","
                        + "\"params\":[\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\"],\"id\":1}");
    }

    @Test
    public void testGetBalance_with_Prefix() throws Exception {
        neow3j.getBalance("0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getbalance\","
                        + "\"params\":[\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\"],\"id\":1}");
    }

    @Test
    public void testGetAssetState() throws Exception {
        neow3j.getAssetState("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getassetstate\","
                        + "\"params\":[\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\"],\"id\":1}");
    }

    @Test
    public void testGetAssetState_with_Prefix() throws Exception {
        neow3j.getAssetState("0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getassetstate\","
                        + "\"params\":[\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\"],\"id\":1}");
    }

    @Test
    public void testSendMany() throws Exception {
        neow3j.sendMany(
                Arrays.asList(
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "100", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "10", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2")
                )
        ).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"sendmany\","
                        + "\"params\":["
                        + "["
                        + "{\"asset\":\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\"value\":\"100\",\"address\":\"AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2\"},"
                        + "{\"asset\":\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\"value\":\"10\",\"address\":\"AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2\"}"
                        + "]"
                        + "],\"id\":1}");
    }

    @Test
    public void testSendMany_Empty_Transaction() throws Exception {
        neow3j.sendMany(Arrays.asList()).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"sendmany\","
                        + "\"params\":[[]],\"id\":1}");
    }

    @Test
    public void testSendMany_Fee() throws Exception {
        neow3j.sendMany(
                Arrays.asList(
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "100", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "10", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2")
                ),
                "50"
        ).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"sendmany\","
                        + "\"params\":["
                        + "["
                        + "{\"asset\":\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\"value\":\"100\",\"address\":\"AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2\"},"
                        + "{\"asset\":\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\"value\":\"10\",\"address\":\"AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2\"}"
                        + "],"
                        + "\"50\""
                        + "],\"id\":1}");
    }

    @Test
    public void testSendMany_Fee_And_ChangeAddress() throws Exception {
        neow3j.sendMany(
                Arrays.asList(
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "100", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "10", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2")
                ),
                "50",
                "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"
        ).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"sendmany\","
                        + "\"params\":["
                        + "["
                        + "{\"asset\":\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\"value\":\"100\",\"address\":\"AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2\"},"
                        + "{\"asset\":\"c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b\",\"value\":\"10\",\"address\":\"AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2\"}"
                        + "],"
                        + "\"50\","
                        + "\"AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y\""
                        + "],\"id\":1}");
    }

    @Test
    public void testDumpPrivKey() throws Exception {
        neow3j.dumpPrivKey("ARye5QEj8YX2vpJ297Lrcmz9m6F8hadgxg").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"dumpprivkey\","
                        + "\"params\":[\"ARye5QEj8YX2vpJ297Lrcmz9m6F8hadgxg\"],\"id\":1}");
    }

}
