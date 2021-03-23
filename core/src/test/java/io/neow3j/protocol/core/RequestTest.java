package io.neow3j.protocol.core;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.string;
import static io.neow3j.transaction.Signer.calledByEntry;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import io.neow3j.contract.Hash160;
import io.neow3j.contract.Hash256;
import io.neow3j.crypto.Base64;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.RequestTester;
import io.neow3j.protocol.core.methods.response.TransactionSendAsset;
import io.neow3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.Date;

import io.neow3j.transaction.Signer;
import io.neow3j.transaction.WitnessScope;
import org.junit.Test;

public class RequestTest extends RequestTester {

    private Neow3j neow3j;

    @Override
    protected void initWeb3Client(HttpService httpService) {
        neow3j = Neow3j.build(httpService);
    }

    // Blockchain Methods

    @Test
    public void testGetBestBlockHash() throws Exception {
        neow3j.getBestBlockHash().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getbestblockhash\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testGetBlockHash() throws Exception {
        neow3j.getBlockHash(new BigInteger("16293")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblockhash\"," +
                "\"params\":[16293]," +
                "\"id\":1}");
    }

    @Test
    public void testGetBlock_Index() throws Exception {
        neow3j.getBlock(new BigInteger("12345"), true).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblock\"," +
                "\"params\":[12345,1]," +
                "\"id\":1}");
    }

    @Test
    public void testGetBlock_Index_onlyHeader() throws Exception {
        neow3j.getBlock(new BigInteger("12345"), false).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblockheader\"," +
                "\"params\":[12345,1]," +
                "\"id\":1}");
    }

    @Test
    public void testGetBlock_Hash() throws Exception {
        neow3j.getBlock(
                new Hash256("0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d"),
                true
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblock\"," +
                "\"params\":[\"2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d\",1]," +
                "\"id\":1}");
    }

    @Test
    public void testGetBlock_Hash_fromString() throws Exception {
        neow3j.getBlock(
                "0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d",
                true
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblock\"," +
                "\"params\":[\"2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d\",1]," +
                "\"id\":1}");
    }

    @Test
    public void testGetBlock_notFullTxObjects() throws Exception {
        neow3j.getBlock(
                new Hash256("0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d"),
                false
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblockheader\"," +
                "\"params\":[\"2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d\",1]," +
                "\"id\":1}");
    }

    @Test
    public void testGetBlock_notFullTxObjects_fromString() throws Exception {
        neow3j.getBlock(
                "0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d",
                false
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblockheader\"," +
                "\"params\":[\"2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d\",1]," +
                "\"id\":1}");
    }

    @Test
    public void testGetRawBlock_Index() throws Exception {
        neow3j.getRawBlock(new BigInteger("12345")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblock\"," +
                "\"params\":[12345,0]," +
                "\"id\":1}");
    }

    @Test
    public void testGetRawBlock_Hash() throws Exception {
        neow3j.getRawBlock("0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d")
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblock\"," +
                "\"params\":[\"2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d\",0]," +
                "\"id\":1}");
    }

    @Test
    public void testGetBlockHeaderCount() throws Exception {
        neow3j.getBlockHeaderCount().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblockheadercount\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testGetBlockCount() throws Exception {
        neow3j.getBlockCount().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblockcount\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testGetNativeContracts() throws Exception {
        neow3j.getNativeContracts().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                        "\"method\":\"getnativecontracts\"," +
                        "\"params\":[]," +
                        "\"id\":1}");
    }

    @Test
    public void testGetBlockHeader_Hash() throws Exception {
        neow3j.getBlockHeader(
                "0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblockheader\"," +
                "\"params\":[\"2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d\",1]," +
                "\"id\":1}");
    }

    @Test
    public void testGetBlockHeader_Index() throws Exception {
        neow3j.getBlockHeader(new BigInteger("12345")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblockheader\"," +
                "\"params\":[12345,1]," +
                "\"id\":1}");
    }

    @Test
    public void testGetRawBlockHeader_Hash() throws Exception {
        neow3j.getRawBlockHeader("0x2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d")
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblockheader\"," +
                "\"params\":[\"2240b34669038f82ac492150d391dfc3d7fe5e3c1d34e5b547d50e99c09b468d\",0]," +
                "\"id\":1}");
    }

    @Test
    public void testGetRawBlockHeader_Index() throws Exception {
        neow3j.getRawBlockHeader(new BigInteger("12345")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblockheader\"," +
                "\"params\":[12345,0]," +
                "\"id\":1}");
    }

    @Test
    public void testGetContractState() throws Exception {
        neow3j.getContractState(new Hash160("dc675afc61a7c0f7b3d2682bf6e1d8ed865a0e5f")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getcontractstate\"," +
                "\"params\":[\"dc675afc61a7c0f7b3d2682bf6e1d8ed865a0e5f\"]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetContractState_byName() throws Exception {
        neow3j.getContractState("NeoToken").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getcontractstate\"," +
                "\"params\":[\"NeoToken\"]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetMemPool() throws Exception {
        neow3j.getMemPool().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getrawmempool\"," +
                "\"params\":[1]," +
                "\"id\":1}");
    }

    @Test
    public void testGetRawMemPool() throws Exception {
        neow3j.getRawMemPool().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getrawmempool\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testGetTransaction() throws Exception {
        Hash256 hash = new Hash256("0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6");
        neow3j.getTransaction(hash).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                        "\"method\":\"getrawtransaction\"," +
                        "\"params\":[\"1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6\",1]," +
                        "\"id\":1}");
    }

    @Test
    public void testGetTransaction_string() throws Exception {
        neow3j.getTransaction("0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6")
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getrawtransaction\"," +
                "\"params\":[\"1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6\",1]," +
                "\"id\":1}");
    }

    @Test
    public void testGetRawTransaction() throws Exception {
        Hash256 hash = new Hash256("0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6");
        neow3j.getRawTransaction(hash).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getrawtransaction\"," +
                "\"params\":[\"1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6\",0]," +
                "\"id\":1}");
    }

    @Test
    public void testGetRawTransaction_string() throws Exception {
        neow3j.getRawTransaction("0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6")
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getrawtransaction\"," +
                "\"params\":[\"1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6\",0]," +
                "\"id\":1}");
    }

    @Test
    public void testGetStorage() throws Exception {
        String key = "616e797468696e67";
        neow3j.getStorage("03febccf81ac85e3d795bc5cbd4e84e907812aa3", key).send();

        String keyBase64 = Base64.encode("616e797468696e67");
        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getstorage\"," +
                "\"params\":[\"03febccf81ac85e3d795bc5cbd4e84e907812aa3\",\"" + keyBase64 + "\"]," +
                "\"id\":1}");
    }

    @Test
    public void testGetTransactionHeight() throws Exception {
        neow3j.getTransactionHeight(
                new Hash256("0x793f560ae7058a50c672890e69c9292391dd159ce963a33462059d03b9573d6a"))
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"gettransactionheight\"," +
                "\"params\":[\"793f560ae7058a50c672890e69c9292391dd159ce963a33462059d03b9573d6a\"]," +
                "\"id\":1}");
    }

    @Test
    public void testGetTransactionHeight_fromString() throws Exception {
        neow3j.getTransactionHeight("0x793f560ae7058a50c672890e69c9292391dd159ce963a33462059d03b9573d6a")
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"gettransactionheight\"," +
                "\"params\":[\"793f560ae7058a50c672890e69c9292391dd159ce963a33462059d03b9573d6a\"]," +
                "\"id\":1}");
    }

    @Test
    public void testGetNextBlockValidators() throws Exception {
        neow3j.getNextBlockValidators().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnextblockvalidators\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testGetCommittee() throws Exception {
        neow3j.getCommittee().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getcommittee\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    // Node Methods

    @Test
    public void testGetConnectionCount() throws Exception {
        neow3j.getConnectionCount().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getconnectioncount\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testGetPeers() throws Exception {
        neow3j.getPeers().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getpeers\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testGetVersion() throws Exception {
        neow3j.getVersion().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getversion\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testSendRawTransaction() throws Exception {
        neow3j.sendRawTransaction("80000001d405ab03e736a01ca277d94b1377113c7e961bb4550511fe1d408f30c77a82650000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b0000000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc5001a711802000000295f83f83fc439f56e6e1fb062d89c6f538263d70141403711e366fc99e77a110b6c96b5f8828ef956a6d5cfa5cb63273419149011b0f30dc5458faa59e4867d0ac7537e324c98124bb691feca5c5ddf6ed20f4adb778223210265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6ac")
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendrawtransaction\"," +
                "\"params\":[\"gAAAAdQFqwPnNqAconfZSxN3ETx+lhu0VQUR/h1AjzDHeoJlAAACm3z/2qZ0vq4Pkw6+YIWvkJPl/lazSlwiDM3Pbvwzb8UAypo7AAAAACO6JwPFMmPo1uUi3DIgMznc2O7pm3z/2qZ0vq4Pkw6+YIWvkJPl/lazSlwiDM3Pbvwzb8UAGnEYAgAAAClfg/g/xDn1bm4fsGLYnG9TgmPXAUFANxHjZvyZ53oRC2yWtfiCjvlWptXPpctjJzQZFJARsPMNxUWPqlnkhn0Kx1N+MkyYEku2kf7KXF3fbtIPStt3giMhAmW/kGvzhfvz93eDLlWoeZG8++GbCX+3xcouQCWk1eXWrA==\"]," +
                "\"id\":1}");
    }

    @Test
    public void testSubmitBlock() throws Exception {
        neow3j.submitBlock("00000000000000000000000000000000").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"submitblock\"," +
                "\"params\":[\"00000000000000000000000000000000\"]," +
                "\"id\":1}"
        );
    }

    // SmartContract Methods

    @Test
    public void testInvokeFunction() throws Exception {
        neow3j.invokeFunction(
                "af7c7328eee5a275a3bcaee2bf0cf662b5e739be",
                "balanceOf",
                singletonList(
                        hash160(new Hash160(
                                "91b83e96f2a7c4fdf0c1688441ec61986c7cae26"))
                ),
                new Signer.Builder()
                        .account(new Hash160("0xcadb3dc2faa3ef14a13b619c9a43124755aa2569"))
                        .scopes(WitnessScope.CALLED_BY_ENTRY)
                        .build()
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokefunction\"," +
                "\"params\":[\"af7c7328eee5a275a3bcaee2bf0cf662b5e739be\",\"balanceOf\"," +
                "[{\"type\":\"Hash160\",\"value\":\"91b83e96f2a7c4fdf0c1688441ec61986c7cae26\"}]," +
                "[{\"account\":\"cadb3dc2faa3ef14a13b619c9a43124755aa2569\"," +
                "\"scopes\":[\"CalledByEntry\"],\"allowedcontracts\":[],\"allowedgroups\":[]}]" +
                "]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeFunction_without_Params() throws Exception {
        neow3j.invokeFunction(
                new Hash160("af7c7328eee5a275a3bcaee2bf0cf662b5e739be"),
                "decimals"
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokefunction\"," +
                "\"params\":[\"af7c7328eee5a275a3bcaee2bf0cf662b5e739be\",\"decimals\",[]]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeFunction_without_Params_fromString() throws Exception {
        neow3j.invokeFunction(
                "af7c7328eee5a275a3bcaee2bf0cf662b5e739be",
                "decimals"
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokefunction\"," +
                "\"params\":[\"af7c7328eee5a275a3bcaee2bf0cf662b5e739be\",\"decimals\",[]]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeScript() throws Exception {
        neow3j.invokeScript("10c00c08646563696d616c730c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b52")
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokescript\"," +
                "\"params\":[\"10c00c08646563696d616c730c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b52\"]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeScriptWithWitness() throws Exception {
        neow3j.invokeScript(
                "10c00c08646563696d616c730c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b52",
                calledByEntry(new Hash160("0xcc45cc8987b0e35371f5685431e3c8eeea306722"))
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokescript\"," +
                "\"params\":[\"10c00c08646563696d616c730c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b52\"," +
                "[{\"account\":\"cc45cc8987b0e35371f5685431e3c8eeea306722\",\"scopes\":[\"CalledByEntry\"],\"allowedcontracts\":[],\"allowedgroups\":[]}]" +
                "]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeContractVerify() throws Exception {
        neow3j.invokeContractVerify(
                "af7c7328eee5a275a3bcaee2bf0cf662b5e739be",
                asList(string("a string"), string("another string")),
                calledByEntry(new Hash160("cadb3dc2faa3ef14a13b619c9a43124755aa2569"))
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokecontractverify\"," +
                "\"params\":[\"af7c7328eee5a275a3bcaee2bf0cf662b5e739be\"," +
                "[{\"type\":\"String\",\"value\":\"a string\"}," +
                "{\"type\":\"String\",\"value\":\"another string\"}]," +
                "[{\"account\":\"cadb3dc2faa3ef14a13b619c9a43124755aa2569\",\"scopes\":[\"CalledByEntry\"],\"allowedcontracts\":[],\"allowedgroups\":[]}]" +
                "]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeContractVerifyNoParamsNoSigners() throws Exception {
        neow3j.invokeContractVerify("af7c7328eee5a275a3bcaee2bf0cf662b5e739be", null)
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokecontractverify\"," +
                "\"params\":[\"af7c7328eee5a275a3bcaee2bf0cf662b5e739be\",[],[]]," +
                "\"id\":1}"
        );
    }

    // Utilities Methods

    @Test
    public void testListPlugins() throws Exception {
        neow3j.listPlugins().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"listplugins\"," +
                "\"params\":[]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testValidateAddress() throws Exception {
        neow3j.validateAddress("AQVh2pG732YvtNaxEGkQUei3YA4cvo7d2i").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"validateaddress\"," +
                "\"params\":[\"AQVh2pG732YvtNaxEGkQUei3YA4cvo7d2i\"]," +
                "\"id\":1}");
    }

    // Wallet Methods

    @Test
    public void testCloseWallet() throws Exception {
        neow3j.closeWallet().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"closewallet\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testOpenWallet() throws Exception {
        neow3j.openWallet("wallet.json", "one").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"openwallet\"," +
                "\"params\":[\"wallet.json\",\"one\"]," +
                "\"id\":1}");
    }

    @Test
    public void testDumpPrivKey() throws Exception {
        neow3j.dumpPrivKey("AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"dumpprivkey\"," +
                "\"params\":[\"AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4\"]," +
                "\"id\":1}");
    }

    @Test
    public void testGetWalletBalance() throws Exception {
        neow3j.getWalletBalance("de5f57d430d3dece511cf975a8d37848cb9e0525").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getwalletbalance\"," +
                "\"params\":[\"de5f57d430d3dece511cf975a8d37848cb9e0525\"]," +
                "\"id\":1}");
    }

    @Test
    public void testGetWalletBalance_with_Prefix() throws Exception {
        neow3j.getWalletBalance("0xde5f57d430d3dece511cf975a8d37848cb9e0525").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getwalletbalance\"," +
                "\"params\":[\"de5f57d430d3dece511cf975a8d37848cb9e0525\"]," +
                "\"id\":1}");
    }

    @Test
    public void testGetNewAddress() throws Exception {
        neow3j.getNewAddress().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnewaddress\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testGetWalletUnclaimedGas() throws Exception {
        neow3j.getWalletUnclaimedGas().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getwalletunclaimedgas\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testGetUnclaimedGas() throws Exception {
        neow3j.getUnclaimedGas("AGZLEiwUyCC4wiL5sRZA3LbxWPs9WrZeyN").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getunclaimedgas\"," +
                "\"params\":[\"AGZLEiwUyCC4wiL5sRZA3LbxWPs9WrZeyN\"]," +
                "\"id\":1}");
    }

    @Test
    public void testImportPrivKey() throws Exception {
        neow3j.importPrivKey("L5c6jz6Rh8arFJW3A5vg7Suaggo28ApXVF2EPzkAXbm94ThqaA6r").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"importprivkey\"," +
                "\"params\":[\"L5c6jz6Rh8arFJW3A5vg7Suaggo28ApXVF2EPzkAXbm94ThqaA6r\"]," +
                "\"id\":1}");
    }

    @Test
    public void testCalculateNetworkFee() throws Exception {
        neow3j.calculateNetworkFee("6e656f77336a").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"calculatenetworkfee\"," +
                "\"params\":[\"bmVvdzNq\"]," +
                "\"id\":1}");
    }

    @Test
    public void testListAddress() throws Exception {
        neow3j.listAddress().send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"listaddress\"," +
                "\"params\":[]," +
                "\"id\":1}");
    }

    @Test
    public void testSendFrom() throws Exception {
        neow3j.sendFrom(
                "AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4",
                "0xde5f57d430d3dece511cf975a8d37848cb9e0525",
                "AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb",
                "10.0"
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendfrom\"," +
                "\"params\":[\"0xde5f57d430d3dece511cf975a8d37848cb9e0525\"," +
                "\"AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4\"," +
                "\"AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb\"," +
                "\"10.0\"" +
                "]," +
                "\"id\":1}");
    }

    @Test
    public void testSendFrom_TransactionSendAsset() throws Exception {
        neow3j.sendFrom(
                "AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4",
                new TransactionSendAsset(
                        "0xde5f57d430d3dece511cf975a8d37848cb9e0525",
                        "10.0",
                        "AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb"
                )
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendfrom\"," +
                "\"params\":[\"0xde5f57d430d3dece511cf975a8d37848cb9e0525\"," +
                "\"AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4\"," +
                "\"AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb\"," +
                "\"10.0\"" +
                "]," +
                "\"id\":1}");
    }

    @Test
    public void testSendMany() throws Exception {
        neow3j.sendMany(
                asList(
                        new TransactionSendAsset(
                                "0xde5f57d430d3dece511cf975a8d37848cb9e0525",
                                "100",
                                "AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb"),
                        new TransactionSendAsset(
                                "0xde5f57d430d3dece511cf975a8d37848cb9e0525",
                                "10",
                                "AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4")
                )
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendmany\"," +
                "\"params\":[" +
                "[{\"asset\":\"0xde5f57d430d3dece511cf975a8d37848cb9e0525\",\"value\":\"100\",\"address\":\"AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb\"}," +
                "{\"asset\":\"0xde5f57d430d3dece511cf975a8d37848cb9e0525\",\"value\":\"10\",\"address\":\"AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4\"}]" +
                "]," +
                "\"id\":1}");
    }

    @Test
    public void testSendMany_Empty_Transaction() throws Exception {
        neow3j.sendMany(emptyList()).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendmany\"," +
                "\"params\":[[]]," +
                "\"id\":1}");
    }

    @Test
    public void testSendManyWithFrom() throws Exception {
        neow3j.sendMany(
                "AGZLEiwUyCC4wiL5sRZA3LbxWPs9WrZeyN",
                asList(
                        new TransactionSendAsset(
                                "0xde5f57d430d3dece511cf975a8d37848cb9e0525",
                                "100",
                                "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionSendAsset(
                                "0xde5f57d430d3dece511cf975a8d37848cb9e0525",
                                "10",
                                "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
                )
        ).send();


        verifyResult("{" +
                     "  \"jsonrpc\": \"2.0\"," +
                     "  \"method\": \"sendmany\"," +
                     "  \"params\": [\"AGZLEiwUyCC4wiL5sRZA3LbxWPs9WrZeyN\"," +
                     "  [" +
                     "     {" +
                     "         \"asset\": \"0xde5f57d430d3dece511cf975a8d37848cb9e0525\", " +
                     "         \"value\": \"100\"," +
                     "         \"address\": \"AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2\"" +
                     "     }," +
                     "     {" +
                     "         \"asset\": \"0xde5f57d430d3dece511cf975a8d37848cb9e0525\", " +
                     "         \"value\": \"10\"," +
                     "         \"address\": \"AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y\"}" +
                     "     ]" +
                     "  ]," +
                     "  \"id\": 1" +
                     "}");
    }

    @Test
    public void testSendToAddress() throws Exception {
        neow3j.sendToAddress(
                "0xde5f57d430d3dece511cf975a8d37848cb9e0525",
                "AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb",
                "10.0"
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendtoaddress\"," +
                "\"params\":[\"0xde5f57d430d3dece511cf975a8d37848cb9e0525\"," +
                "\"AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb\"," +
                "\"10.0\"" +
                "]," +
                "\"id\":1}");
    }

    @Test
    public void testSendToAddress_TransactionSendAsset() throws Exception {
        neow3j.sendToAddress(
                new TransactionSendAsset(
                        "0xde5f57d430d3dece511cf975a8d37848cb9e0525",
                        "10.0",
                        "AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb")
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendtoaddress\"," +
                "\"params\":[\"0xde5f57d430d3dece511cf975a8d37848cb9e0525\"," +
                "\"AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb\"," +
                "\"10.0\"" +
                "]," +
                "\"id\":1}");
    }

    // RpcNep17Tracker

    @Test
    public void testGetNep17Transfers() throws Exception {
        neow3j.getNep17Transfers("AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep17transfers\"," +
                "\"params\":[\"AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb\"]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetNep17Transfers_Date() throws Exception {
        neow3j.getNep17Transfers("AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb", new Date(1553105830L)).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep17transfers\"," +
                "\"params\":[\"AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb\",1553105830]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetNep17Transfers_DateFromTo() throws Exception {
        neow3j.getNep17Transfers("AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb",
                new Date(1553105830),
                new Date(1557305830)
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep17transfers\"," +
                "\"params\":[\"AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb\",1553105830,1557305830]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetNep17Balances() throws Exception {
        neow3j.getNep17Balances("AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep17balances\"," +
                "\"params\":[\"AcozGpiGDpp9Vt9RMyokWNyu7hh341T2bb\"]," +
                "\"id\":1}"
        );
    }

    // ApplicationLogs

    @Test
    public void testGetApplicationLog() throws Exception {
        neow3j.getApplicationLog(
                new Hash256("420d1eb458c707d698c6d2ba0f91327918ddb3b7bae2944df070f3f4e579078b"))
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                        "\"method\":\"getapplicationlog\"," +
                        "\"params\":[\"420d1eb458c707d698c6d2ba0f91327918ddb3b7bae2944df070f3f4e579078b\"]" +
                        ",\"id\":1}"
        );
    }

    @Test
    public void testGetApplicationLog_fromStringTxId() throws Exception {
        neow3j.getApplicationLog("420d1eb458c707d698c6d2ba0f91327918ddb3b7bae2944df070f3f4e579078b")
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getapplicationlog\"," +
                "\"params\":[\"420d1eb458c707d698c6d2ba0f91327918ddb3b7bae2944df070f3f4e579078b\"]" +
                ",\"id\":1}"
        );
    }

    // StateService

    @Test
    public void testGetStateRoot() throws Exception {
        neow3j.getStateRoot(new BigInteger("52")).send();

        verifyResult("{\"jsonrpc\": \"2.0\"," +
                "\"method\": \"getstateroot\"," +
                "\"params\": [52]," +
                "\"id\": 1}");
    }

    @Test
    public void testGetProof() throws Exception {
        neow3j.getProof(
                new Hash256("0x7bf925dbd33af0e00d392b92313da59369ed86c82494d0e02040b24faac0a3ca"),
                new Hash160("0x79bcd398505eb779df6e67e4be6c14cded08e2f2"),
                "616e797468696e67"
        ).send();

        verifyResult("{\"jsonrpc\": \"2.0\"," +
                "\"method\": \"getproof\"," +
                "\"params\": [" +
                "\"7bf925dbd33af0e00d392b92313da59369ed86c82494d0e02040b24faac0a3ca\"," +
                "\"79bcd398505eb779df6e67e4be6c14cded08e2f2\"," +
                "\"YW55dGhpbmc=\"" +
                "]," +
                "\"id\": 1}");
    }

    @Test
    public void testVerifyProof() throws Exception {
        neow3j.verifyProof(
                new Hash256("0x7bf925dbd33af0e00d392b92313da59369ed86c82494d0e02040b24faac0a3ca"),
                "05fbffffff17062401010f034736fa564770d2be67c4c069e99e67bdbc978aea4cbdf7f09b4bf919d07a8c7bf200040404040404040403b97ce0716d448e55d75b39f2f03ef02679180ac52e419460b3bdf112da31a6260393049f1d2ea67548e395d3a6850346fc2b94f0f9f6e5b2f5a22b752e13d03d9c03a470bff9771a6adcb51ccbd667be893505743828a1acfe7ac14715725d0c035f0366ddb2164ca79a31da0a1500e323be8c4d21658eaa326844fbba64ec3691d8fa03cc68bc42b5329e9c006f73fe37c50eb48433ad24ba8f7b30d69538ffdc0d9d700359dfe16748fa392c633d04260cc1ba42bcac4cf3f2313671bd99c767f2b55c90040342eefd9168b302445d5780ba52ae2d762f62302fd65c8f3e4b662681e671ca36042901060f0f0f0f0f0f0382015f7e7b15332a807d98e0fb882ef7b37f80aa4eb002bc436eece7ab628d1952000326dba0d8c4e75ffc88c482413fbbf0a844be9d0e7cefb357710a0ff4d1eb0cbe03c68a878ea900a40ce4df9623a70b16236d8ff45ef55439791c7ae0e6d56d05cd04040404040404040404040404040492000403c7d4f9d7fcff0d3a05782879e88300fbc48b03f0abcd8644dc0f1802a3b11cea03d0b35cc3e22812241e0cf634d112afbb950e4f6d31ffe9dd77f32515bed33c6d03fd0dfd6487b6ac5eb62366894a84b710d4da02950804cdb7097d89021d7bfa0d0404040394c3dccd97fec526b583eeb0afd266cea32ea72e51d6896bb21ac035da8851b804040404040404040406020402000200"
        ).send();

        verifyResult("{\"jsonrpc\": \"2.0\"," +
                "\"method\": \"verifyproof\"," +
                "\"params\": [" +
                "\"7bf925dbd33af0e00d392b92313da59369ed86c82494d0e02040b24faac0a3ca\"," +
                "\"Bfv///8XBiQBAQ8DRzb6Vkdw0r5nxMBp6Z5nvbyXiupMvffwm0v5GdB6jHvyAAQEBAQEBAQEA7l84HFtRI5V11s58vA+8CZ5GArFLkGUYLO98RLaMaYmA5MEnx0upnVI45XTpoUDRvwrlPD59uWy9aIrdS4T0D2cA6Rwv/l3GmrctRzL1me+iTUFdDgooaz+esFHFXJdDANfA2bdshZMp5ox2goVAOMjvoxNIWWOqjJoRPu6ZOw2kdj6A8xovEK1Mp6cAG9z/jfFDrSEM60kuo97MNaVOP/cDZ1wA1nf4WdI+jksYz0EJgzBukK8rEzz8jE2cb2Zx2fytVyQBANC7v2RaLMCRF1XgLpSri12L2IwL9Zcjz5LZiaB5nHKNgQpAQYPDw8PDw8DggFffnsVMyqAfZjg+4gu97N/gKpOsAK8Q27s56tijRlSAAMm26DYxOdf/IjEgkE/u/CoRL6dDnzvs1dxCg/00esMvgPGioeOqQCkDOTfliOnCxYjbY/0XvVUOXkceuDm1W0FzQQEBAQEBAQEBAQEBAQEBJIABAPH1PnX/P8NOgV4KHnogwD7xIsD8KvNhkTcDxgCo7Ec6gPQs1zD4igSJB4M9jTREq+7lQ5PbTH/6d138yUVvtM8bQP9Df1kh7asXrYjZolKhLcQ1NoClQgEzbcJfYkCHXv6DQQEBAOUw9zNl/7FJrWD7rCv0mbOoy6nLlHWiWuyGsA12ohRuAQEBAQEBAQEBAYCBAIAAgA=\"" +
                "]," +
                "\"id\": 1}");
    }

    @Test
    public void testGetStateHeight() throws Exception {
        neow3j.getStateHeight().send();

        verifyResult("{\"jsonrpc\": \"2.0\"," +
                "\"method\": \"getstateheight\"," +
                "\"params\": []," +
                "\"id\": 1}");
    }

}
