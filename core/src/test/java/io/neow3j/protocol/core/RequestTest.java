package io.neow3j.protocol.core;

import io.neow3j.crypto.Base64;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.RequestTester;
import io.neow3j.protocol.core.response.OracleResponse;
import io.neow3j.protocol.core.response.OracleResponseCode;
import io.neow3j.protocol.core.response.TransactionSendToken;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.witnessrule.AndCondition;
import io.neow3j.transaction.witnessrule.BooleanCondition;
import io.neow3j.transaction.witnessrule.CalledByContractCondition;
import io.neow3j.transaction.witnessrule.CalledByEntryCondition;
import io.neow3j.transaction.witnessrule.CalledByGroupCondition;
import io.neow3j.transaction.witnessrule.GroupCondition;
import io.neow3j.transaction.witnessrule.NotCondition;
import io.neow3j.transaction.witnessrule.OrCondition;
import io.neow3j.transaction.witnessrule.ScriptHashCondition;
import io.neow3j.transaction.witnessrule.WitnessRule;
import io.neow3j.transaction.witnessrule.WitnessAction;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Date;

import static io.neow3j.test.TestProperties.committeeAccountScriptHash;
import static io.neow3j.test.TestProperties.defaultAccountPublicKey;
import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.string;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class RequestTest extends RequestTester {

    private Neow3j neow3j;
    private Neow3jExpress neow3jExpress;

    @Override
    protected void initWeb3Client(HttpService httpService) {
        neow3j = Neow3j.build(httpService);
        neow3jExpress = Neow3jExpress.build(httpService);
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
    public void testGetRawBlock_Index() throws Exception {
        neow3j.getRawBlock(new BigInteger("12345")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblock\"," +
                "\"params\":[12345,0]," +
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
    public void testGetBlockHeader_Index() throws Exception {
        neow3j.getBlockHeader(new BigInteger("12345")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getblockheader\"," +
                "\"params\":[12345,1]," +
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
        neow3j.getNativeContractState("NeoToken").send();

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
    public void testGetRawTransaction() throws Exception {
        Hash256 hash = new Hash256("0x1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6");
        neow3j.getRawTransaction(hash).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getrawtransaction\"," +
                "\"params\":[\"1f31821787b0a53df0ff7d6e0e7ecba3ac19dd517d6d2ea5aaf00432c20831d6\",0]," +
                "\"id\":1}");
    }

    @Test
    public void testGetStorage() throws Exception {
        String key = "616e797468696e67";
        neow3j.getStorage(new Hash160("03febccf81ac85e3d795bc5cbd4e84e907812aa3"), key).send();

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
        ECKeyPair.ECPublicKey pubKey = new ECKeyPair.ECPublicKey(defaultAccountPublicKey());

        neow3j.invokeFunction(
                new Hash160("af7c7328eee5a275a3bcaee2bf0cf662b5e739be"),
                "balanceOf",
                asList(
                        hash160(new Hash160(
                                "91b83e96f2a7c4fdf0c1688441ec61986c7cae26"))
                ),
                AccountSigner.calledByEntry(new Hash160("0xcadb3dc2faa3ef14a13b619c9a43124755aa2569"))
                        .setAllowedContracts(new Hash160(neoTokenHash()))
                        .setAllowedGroups(pubKey)
                        .setRules(new WitnessRule(WitnessAction.ALLOW,
                                new CalledByContractCondition(new Hash160(neoTokenHash()))))
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokefunction\"," +
                "\"params\":[\"af7c7328eee5a275a3bcaee2bf0cf662b5e739be\",\"balanceOf\"," +
                "[{\"type\":\"Hash160\",\"value\":\"91b83e96f2a7c4fdf0c1688441ec61986c7cae26\"}]," +
                "[{\"account\":\"cadb3dc2faa3ef14a13b619c9a43124755aa2569\"," +
                "\"scopes\":\"CalledByEntry,CustomContracts,CustomGroups,WitnessRules\"," +
                "\"allowedcontracts\":[\"ef4073a0f2b305a38ec4050e4d3d28bc40ea63f5\"]," +
                "\"allowedgroups" +
                "\":[\"033a4d051b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7857787f1b\"]," +
                "\"rules\":[" +
                "   {" +
                "       \"action\":\"Allow\"," +
                "       \"condition\": {" +
                "           \"type\":\"CalledByContract\"," +
                "           \"hash\":\"" + neoTokenHash() + "\"" +
                "        }" +
                "   }" +
                "]}]" +
                "]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeFunction_witnessRules() throws Exception {
        ECKeyPair.ECPublicKey pubKey = new ECKeyPair.ECPublicKey(defaultAccountPublicKey());

        neow3j.invokeFunction(
                new Hash160("af7c7328eee5a275a3bcaee2bf0cf662b5e739be"),
                "balanceOf",
                asList(hash160(new Hash160("91b83e96f2a7c4fdf0c1688441ec61986c7cae26"))),
                AccountSigner.calledByEntry(new Hash160("0xcadb3dc2faa3ef14a13b619c9a43124755aa2569"))
                        .setAllowedContracts(new Hash160(neoTokenHash()))
                        .setAllowedGroups(pubKey)
                        .setRules(
                                new WitnessRule(WitnessAction.DENY,
                                        new AndCondition(
                                                new BooleanCondition(true),
                                                new CalledByContractCondition(new Hash160(neoTokenHash())),
                                                new CalledByGroupCondition(pubKey),
                                                new GroupCondition(pubKey)
                                        )
                                ),
                                new WitnessRule(WitnessAction.DENY,
                                        new OrCondition(
                                                new CalledByGroupCondition(pubKey),
                                                new ScriptHashCondition(new Hash160(committeeAccountScriptHash()))
                                        )
                                ),
                                new WitnessRule(WitnessAction.ALLOW, new NotCondition(new CalledByEntryCondition()))
                        )
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokefunction\"," +
                "\"params\":[\"af7c7328eee5a275a3bcaee2bf0cf662b5e739be\",\"balanceOf\"," +
                "[{\"type\":\"Hash160\",\"value\":\"91b83e96f2a7c4fdf0c1688441ec61986c7cae26\"}]," +
                "[{\"account\":\"cadb3dc2faa3ef14a13b619c9a43124755aa2569\"," +
                "\"scopes\":\"CalledByEntry,CustomContracts,CustomGroups,WitnessRules\"," +
                "\"allowedcontracts\":[\"ef4073a0f2b305a38ec4050e4d3d28bc40ea63f5\"]," +
                "\"allowedgroups" +
                "\":[\"033a4d051b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7857787f1b\"]," +
                "\"rules\":[" +
                "   {" +
                "       \"action\":\"Deny\"," +
                "       \"condition\": {" +
                "           \"type\":\"And\"," +
                "           \"expressions\":[" +
                "               {" +
                "                   \"type\":\"Boolean\"," +
                "                   \"expression\":true" +
                "               },{" +
                "                   \"type\":\"CalledByContract\"," +
                "                   \"hash\":\"" + neoTokenHash() + "\"" +
                "               },{" +
                "                   \"type\":\"CalledByGroup\"," +
                "                   \"group\":\"" + defaultAccountPublicKey() + "\"" +
                "               },{" +
                "                   \"type\":\"Group\"," +
                "                   \"group\":\"" + defaultAccountPublicKey() + "\"" +
                "               }" +
                "           ]" +
                "       }" +
                "   }," +
                "   {" +
                "       \"action\":\"Deny\"," +
                "       \"condition\": {" +
                "           \"type\":\"Or\"," +
                "           \"expressions\":[" +
                "               {" +
                "                   \"type\":\"CalledByGroup\"," +
                "                   \"group\":\"" + defaultAccountPublicKey() + "\"" +
                "               },{" +
                "                   \"type\":\"ScriptHash\"," +
                "                   \"hash\":\"" + committeeAccountScriptHash() + "\"" +
                "               }" +
                "           ]" +
                "       }" +
                "   }," +
                "   {" +
                "       \"action\":\"Allow\"," +
                "       \"condition\": {" +
                "           \"type\":\"Not\"," +
                "           \"expression\":{" +
                "               \"type\": \"CalledByEntry\"" +
                "           }" +
                "       }" +
                "   }" +
                "]}]" +
                "]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeFunctionDiagnostics() throws Exception {
        neow3j.invokeFunctionDiagnostics(
                new Hash160("af7c7328eee5a275a3bcaee2bf0cf662b5e739be"),
                "balanceOf",
                asList(hash160(new Hash160("91b83e96f2a7c4fdf0c1688441ec61986c7cae26")))
        ).send();

        verifyResult(
                "{" +
                "    \"jsonrpc\":\"2.0\"," +
                "    \"method\":\"invokefunction\"," +
                "    \"params\": [" +
                "        \"af7c7328eee5a275a3bcaee2bf0cf662b5e739be\"," +
                "        \"balanceOf\"," +
                "        [" +
                "            {" +
                "                \"type\": \"Hash160\"," +
                "                \"value\": \"91b83e96f2a7c4fdf0c1688441ec61986c7cae26\"" +
                "            }" +
                "        ]," +
                "        []," +
                "        true" +
                "    ]," +
                "    \"id\": 1" +
                "}"
        );
    }

    @Test
    public void testInvokeFunctionDiagnostics_noParams() throws Exception {
        neow3j.invokeFunctionDiagnostics(
                new Hash160("af7c7328eee5a275a3bcaee2bf0cf662b5e739be"),
                "symbol"
        ).send();

        verifyResult(
                "{" +
                        "    \"jsonrpc\":\"2.0\"," +
                        "    \"method\":\"invokefunction\"," +
                        "    \"params\": [" +
                        "        \"af7c7328eee5a275a3bcaee2bf0cf662b5e739be\"," +
                        "        \"symbol\"," +
                        "        []," +
                        "        []," +
                        "        true" +
                        "    ]," +
                        "    \"id\": 1" +
                        "}"
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
                "\"params\":[\"af7c7328eee5a275a3bcaee2bf0cf662b5e739be\",\"decimals\",[],[]]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeScript() throws Exception {
        neow3j.invokeScript("10c00c08646563696d616c730c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b52")
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokescript\"," +
                "\"params\":[\"EMAMCGRlY2ltYWxzDBQlBZ7LSHjTqHX5HFHO3tMw1Fdf3kFifVtS\",[]]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeScriptDiagnostics() throws Exception {
        neow3j.invokeScriptDiagnostics("10c00c08646563696d616c730c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b52")
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokescript\"," +
                "\"params\":[\"EMAMCGRlY2ltYWxzDBQlBZ7LSHjTqHX5HFHO3tMw1Fdf3kFifVtS\",[], true]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeScriptWithSigner() throws Exception {
        neow3j.invokeScript(
                "10c00c08646563696d616c730c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b52",
                calledByEntry(new Hash160("0xcc45cc8987b0e35371f5685431e3c8eeea306722"))
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokescript\"," +
                "\"params\":[\"EMAMCGRlY2ltYWxzDBQlBZ7LSHjTqHX5HFHO3tMw1Fdf3kFifVtS\"," +
                "[{\"account\":\"cc45cc8987b0e35371f5685431e3c8eeea306722\"," +
                "\"scopes\":\"CalledByEntry\",\"allowedcontracts\":[],\"allowedgroups\":[], " +
                "\"rules\":[]}]]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeScriptDiagnosticsWithSigner() throws Exception {
        neow3j.invokeScriptDiagnostics(
                "10c00c08646563696d616c730c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b52",
                calledByEntry(new Hash160("0xcc45cc8987b0e35371f5685431e3c8eeea306722"))
        ).send();

        verifyResult(
                "{" +
                "    \"jsonrpc\": \"2.0\"," +
                "    \"method\": \"invokescript\"," +
                "    \"params\": [" +
                "        \"EMAMCGRlY2ltYWxzDBQlBZ7LSHjTqHX5HFHO3tMw1Fdf3kFifVtS\"," +
                "        [" +
                "            {" +
                "                \"account\": \"cc45cc8987b0e35371f5685431e3c8eeea306722\"," +
                "                \"scopes\": \"CalledByEntry\"," +
                "                \"allowedcontracts\": []," +
                "                \"allowedgroups\": []," +
                "                \"rules\": []" +
                "            }" +
                "        ]," +
                "        true" +
                "    ]," +
                "    \"id\":1" +
                "}"
        );
    }

    @Test
    public void testTraverseIterator() throws Exception {
        neow3j.traverseIterator(
                "127d3320-db35-48d5-b6d3-ca22dca4a370",
                "cb7ef774-1ade-4a83-914b-94373ca92010",
                100
        ).send();

        verifyResult(
                "{\n" +
                " \"jsonrpc\": \"2.0\",\n" +
                " \"method\": \"traverseiterator\",\n" +
                " \"params\":[\n" +
                "    \"127d3320-db35-48d5-b6d3-ca22dca4a370\",\n" +
                "    \"cb7ef774-1ade-4a83-914b-94373ca92010\",\n" +
                "    100\n" +
                " ],\n" +
                " \"id\": 1\n" +
                "}"
        );
    }

    @Test
    public void testTerminateSession() throws Exception {
        neow3j.terminateSession("127d3320-db35-48d5-b6d3-ca22dca4a370").send();

        verifyResult(
                "{\n" +
                        " \"jsonrpc\": \"2.0\",\n" +
                        " \"method\": \"terminatesession\",\n" +
                        " \"params\":[\n" +
                        "    \"127d3320-db35-48d5-b6d3-ca22dca4a370\"\n" +
                        " ],\n" +
                        " \"id\": 1\n" +
                        "}"
        );
    }

    @Test
    public void testInvokeContractVerify() throws Exception {
        neow3j.invokeContractVerify(
                new Hash160("af7c7328eee5a275a3bcaee2bf0cf662b5e739be"),
                asList(string("a string"), string("another string")),
                calledByEntry(new Hash160("cadb3dc2faa3ef14a13b619c9a43124755aa2569"))
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"invokecontractverify\"," +
                "\"params\":[\"af7c7328eee5a275a3bcaee2bf0cf662b5e739be\"," +
                "[{\"type\":\"String\",\"value\":\"a string\"}," +
                "{\"type\":\"String\",\"value\":\"another string\"}]," +
                "[{\"account\":\"cadb3dc2faa3ef14a13b619c9a43124755aa2569\"," +
                "\"scopes\":\"CalledByEntry\",\"allowedcontracts\":[],\"allowedgroups\":[]," +
                "\"rules\":[]}]" +
                "]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testInvokeContractVerifyNoParamsNoSigners() throws Exception {
        neow3j.invokeContractVerify(new Hash160("af7c7328eee5a275a3bcaee2bf0cf662b5e739be"), null)
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
        neow3j.validateAddress("NTzVAPBpnUUCvrA6tFPxBHGge8Kyw8igxX").send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"validateaddress\"," +
                "\"params\":[\"NTzVAPBpnUUCvrA6tFPxBHGge8Kyw8igxX\"]," +
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
        neow3j.dumpPrivKey(new Hash160("c11d816956b6682c3406bb99b7ec8a3e93f005c1")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"dumpprivkey\"," +
                "\"params\":[\"NdWaiUoBWbPxGsm5wXPjXYJxCyuY1Zw8uW\"]," +
                "\"id\":1}");
    }

    @Test
    public void testGetWalletBalance() throws Exception {
        neow3j.getWalletBalance(new Hash160("de5f57d430d3dece511cf975a8d37848cb9e0525")).send();

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
        neow3j.getUnclaimedGas(new Hash160("ffa6adbb5f82ad2a1aafa22ce6aaf05dad5de39e")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getunclaimedgas\"," +
                "\"params\":[\"NaQ6Kj6qYinh1frv1wrn53wbPFe5BH5T7g\"]," +
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
                new Hash160("0xde5f57d430d3dece511cf975a8d37848cb9e0525"),
                new Hash160("8cdb257b8873049918fe5a1e7f6289f75d720ba5"),
                new Hash160("db1acbae4dbae55f8325724cf080ed782925c7a7"),
                BigInteger.TEN
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendfrom\"," +
                "\"params\":[\"de5f57d430d3dece511cf975a8d37848cb9e0525\"," +
                "\"NaxePjypvtsQ5GVi6S1jBsSjXribTSUKRu\"," +
                "\"NbD6be5uYezFZRSBDt6aBfYR9bYsAk8Yui\"," +
                "10" +
                "]," +
                "\"id\":1}");
    }

    @Test
    public void testSendFrom_TransactionSendAsset() throws Exception {
        neow3j.sendFrom(
                new Hash160("44b159ceed1bfbd753748227309428f54f52e4dd"),
                new TransactionSendToken(
                        new Hash160("0xde5f57d430d3dece511cf975a8d37848cb9e0525"),
                        BigInteger.TEN,
                        "NUokBS9rfH8qncwFdfByBTT9yJjxQv8h2h"
                )
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendfrom\"," +
                "\"params\":[\"de5f57d430d3dece511cf975a8d37848cb9e0525\"," +
                "\"Ng9E3D4DpM6JrgSxizhanJ6zm6BjvZ2XkM\"," +
                "\"NUokBS9rfH8qncwFdfByBTT9yJjxQv8h2h\"," +
                "10" +
                "]," +
                "\"id\":1}");
    }

    @Test
    public void testSendMany() throws Exception {
        neow3j.sendMany(
                asList(
                        new TransactionSendToken(
                                new Hash160("0xde5f57d430d3dece511cf975a8d37848cb9e0525"),
                                new BigInteger("100"),
                                "NRkkHsxkzFxGz77mJtJgYZ3FnBm8baU5Um"),
                        new TransactionSendToken(
                                new Hash160("0xde5f57d430d3dece511cf975a8d37848cb9e0525"),
                                BigInteger.TEN,
                                "NNFGNNK1HXSSnA7yKLzRpr8YXwcdgTrsCu")
                )
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendmany\"," +
                "\"params\":[" +
                "[{\"asset\":\"de5f57d430d3dece511cf975a8d37848cb9e0525\",\"value\":100,\"address\":\"NRkkHsxkzFxGz77mJtJgYZ3FnBm8baU5Um\"}," +
                "{\"asset\":\"de5f57d430d3dece511cf975a8d37848cb9e0525\",\"value\":10,\"address\":\"NNFGNNK1HXSSnA7yKLzRpr8YXwcdgTrsCu\"}]" +
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
                Hash160.fromAddress("NiVNRW6cBXwkvrZnetZToaHPGSSGgV1HmA"),
                asList(
                        new TransactionSendToken(
                                new Hash160("0xde5f57d430d3dece511cf975a8d37848cb9e0525"),
                                new BigInteger("100"),
                                "Nhsi2q3hkByxcH2uBQw7cjc2qEpzXSEKTC"),
                        new TransactionSendToken(
                                new Hash160("0xde5f57d430d3dece511cf975a8d37848cb9e0525"),
                                BigInteger.TEN,
                                "NcwVWxJZh9fxncJ9Sq8msVLotJDsAD3ZD8")
                )
        ).send();


        verifyResult("{\"jsonrpc\": \"2.0\"," +
                     "\"method\": \"sendmany\"," +
                     "\"params\": [\"NiVNRW6cBXwkvrZnetZToaHPGSSGgV1HmA\"," +
                     "[" +
                     "   {" +
                     "       \"asset\": \"de5f57d430d3dece511cf975a8d37848cb9e0525\", " +
                     "       \"value\": 100," +
                     "       \"address\": \"Nhsi2q3hkByxcH2uBQw7cjc2qEpzXSEKTC\"" +
                     "   }," +
                     "   {" +
                     "       \"asset\": \"de5f57d430d3dece511cf975a8d37848cb9e0525\", " +
                     "       \"value\": 10," +
                     "       \"address\": \"NcwVWxJZh9fxncJ9Sq8msVLotJDsAD3ZD8\"}" +
                     "   ]" +
                     "]," +
                     "\"id\": 1" +
                     "}");
    }

    @Test
    public void testSendToAddress() throws Exception {
        neow3j.sendToAddress(
                new Hash160("0xde5f57d430d3dece511cf975a8d37848cb9e0525"),
                new Hash160("674231bd321880fc5c4a73994c87870e52c5fe39"),
                BigInteger.TEN
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendtoaddress\"," +
                "\"params\":[\"de5f57d430d3dece511cf975a8d37848cb9e0525\"," +
                "\"NRCcuUUxKCa3sp45o7bjXetyxUeq58T4ED\"," +
                "10" +
                "]," +
                "\"id\":1}");
    }

    @Test
    public void testSendToAddress_TransactionSendAsset() throws Exception {
        neow3j.sendToAddress(
                new TransactionSendToken(
                        new Hash160("0xde5f57d430d3dece511cf975a8d37848cb9e0525"),
                        BigInteger.TEN,
                        "NaCsFrmoJepqCJSxnTyb41CXVSjr3dMjuL")
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"sendtoaddress\"," +
                "\"params\":[\"de5f57d430d3dece511cf975a8d37848cb9e0525\"," +
                "\"NaCsFrmoJepqCJSxnTyb41CXVSjr3dMjuL\"," +
                "10" +
                "]," +
                "\"id\":1}");
    }

    // TokenTracker: Nep17

    @Test
    public void testGetNep17Transfers() throws Exception {
        neow3j.getNep17Transfers(new Hash160("04457ce4219e462146ac00b09793f81bc5bca2ce")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep17transfers\"," +
                "\"params\":[\"NekZLTu93WgrdFHxzBEJUYgLTQMAT85GLi\"]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetNep17Transfers_Date() throws Exception {
        neow3j.getNep17Transfers(new Hash160("8bed27d0e88266807a6339270f0593510967cb45"),
                new Date(1553105830L)).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep17transfers\"," +
                "\"params\":[\"NSH1UeM96PKhjuzVBKcyWeNNuQkT3sHGmA\",1553105830]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetNep17Transfers_DateFromTo() throws Exception {
        neow3j.getNep17Transfers(new Hash160("2eeda865e7824c71b3fe14bed35d04d0f2f0e9d6"),
                new Date(1553105830),
                new Date(1557305830)
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep17transfers\"," +
                "\"params\":[\"NfWL3Kx7qtZzXrajmggAD4b6r2kGzajbaJ\",1553105830,1557305830]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetNep17Balances() throws Exception {
        neow3j.getNep17Balances(new Hash160("5d75775015b024970bfeacf7c6ab1b0ade974886")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep17balances\"," +
                "\"params\":[\"NY9zhKwcmht5cQJ3oRqjJGo3QuVLwXwTzL\"]," +
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
                "\"params\":[\"420d1eb458c707d698c6d2ba0f91327918ddb3b7bae2944df070f3f4e579078b\"]," +
                "\"id\":1}"
        );
    }

    // StateService

    @Test
    public void testGetStateRoot() throws Exception {
        neow3j.getStateRoot(52L).send();

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

    @Test
    public void testGetState() throws Exception {
        neow3j.getState(
                new Hash256("0x7bf925dbd33af0e00d392b92313da59369ed86c82494d0e02040b24faac0a3ca"),
                new Hash160("7c5832ba81fd0af40ec11e96b1c26613466dae02"),
                "4101210780d1c5615f7912"
        ).send();

        verifyResult("{\"jsonrpc\": \"2.0\"," +
                "\"method\": \"getstate\"," +
                "\"params\": [" +
                "\"7bf925dbd33af0e00d392b92313da59369ed86c82494d0e02040b24faac0a3ca\"," +
                "\"7c5832ba81fd0af40ec11e96b1c26613466dae02\"," +
                "\"QQEhB4DRxWFfeRI=\"" +
                "]," +
                "\"id\": 1}");
    }

    @Test
    public void testFindStates() throws Exception {
        neow3j.findStates(
                new Hash256("0x76d6bddf6d9b5979d532877f0617bf31abd03d663c73357dfb2e2417a287b09f"),
                new Hash160("0xd2a4cff31913016155e38e474a2c06d08be276cf"),
                "0bfe",
                "0b",
                2).send();

        verifyResult("{\"jsonrpc\": \"2.0\"," +
                "\"method\": \"findstates\"," +
                "\"params\": [" +
                "\"76d6bddf6d9b5979d532877f0617bf31abd03d663c73357dfb2e2417a287b09f\"," +
                "\"d2a4cff31913016155e38e474a2c06d08be276cf\"," +
                "\"C/4=\"," +
                "\"Cw==\"," +
                "2" +
                "]," +
                "\"id\": 1}");
    }

    @Test
    public void testFindStates_noCount() throws Exception {
        neow3j.findStates(
                new Hash256("0x76d6bddf6d9b5979d532877f0617bf31abd03d663c73357dfb2e2417a287b09f"),
                new Hash160("0xd2a4cff31913016155e38e474a2c06d08be276cf"),
                "0bfe",
                "0b").send();

        verifyResult("{\"jsonrpc\": \"2.0\"," +
                "\"method\": \"findstates\"," +
                "\"params\": [" +
                "\"76d6bddf6d9b5979d532877f0617bf31abd03d663c73357dfb2e2417a287b09f\"," +
                "\"d2a4cff31913016155e38e474a2c06d08be276cf\"," +
                "\"C/4=\"," +
                "\"Cw==\"" +
                "]," +
                "\"id\": 1}");
    }

    @Test
    public void testFindStates_noStartKey_withCount() throws Exception {
        neow3j.findStates(
                new Hash256("0x76d6bddf6d9b5979d532877f0617bf31abd03d663c73357dfb2e2417a287b09f"),
                new Hash160("0xd2a4cff31913016155e38e474a2c06d08be276cf"),
                "0bfe",
                53).send();

        verifyResult("{\"jsonrpc\": \"2.0\"," +
                "\"method\": \"findstates\"," +
                "\"params\": [" +
                "\"76d6bddf6d9b5979d532877f0617bf31abd03d663c73357dfb2e2417a287b09f\"," +
                "\"d2a4cff31913016155e38e474a2c06d08be276cf\"," +
                "\"C/4=\"," +
                "\"\"," +
                "53" +
                "]," +
                "\"id\": 1}");
    }

    @Test
    public void testFindStates_noStartKey() throws Exception {
        neow3j.findStates(
                new Hash256("0x76d6bddf6d9b5979d532877f0617bf31abd03d663c73357dfb2e2417a287b09f"),
                new Hash160("0xd2a4cff31913016155e38e474a2c06d08be276cf"),
                "0bfe").send();

        verifyResult("{\"jsonrpc\": \"2.0\"," +
                "\"method\": \"findstates\"," +
                "\"params\": [" +
                "\"76d6bddf6d9b5979d532877f0617bf31abd03d663c73357dfb2e2417a287b09f\"," +
                "\"d2a4cff31913016155e38e474a2c06d08be276cf\"," +
                "\"C/4=\"" +
                "]," +
                "\"id\": 1}");
    }

    // Neo-express related tests

    @Test
    public void testExpressGetPopulatedBlocks() throws Exception {
        neow3jExpress.expressGetPopulatedBlocks().send();

        verifyResult("{\n" +
                " \"jsonrpc\": \"2.0\",\n" +
                " \"method\": \"expressgetpopulatedblocks\",\n" +
                " \"params\":[],\n" +
                " \"id\": 1\n" +
                "}");
    }

    @Test
    public void testExpressGetNep17Contracts() throws Exception {
        neow3jExpress.expressGetNep17Contracts().send();

        verifyResult("{\n" +
                " \"jsonrpc\": \"2.0\",\n" +
                " \"method\": \"expressgetnep17contracts\",\n" +
                " \"params\":[],\n" +
                " \"id\": 1\n" +
                "}");
    }

    @Test
    public void testExpressGetContractStorage() throws Exception {
        neow3jExpress.expressGetContractStorage(
                new Hash160("0xd2a4cff31913016155e38e474a2c06d08be276cf")).send();

        verifyResult("{\n" +
                " \"jsonrpc\": \"2.0\",\n" +
                " \"method\": \"expressgetcontractstorage\",\n" +
                " \"params\":[\"d2a4cff31913016155e38e474a2c06d08be276cf\"],\n" +
                " \"id\": 1\n" +
                "}");
    }

    @Test
    public void testExpressListContracts() throws Exception {
        neow3jExpress.expressListContracts().send();

        verifyResult("{\n" +
                " \"jsonrpc\": \"2.0\",\n" +
                " \"method\": \"expresslistcontracts\",\n" +
                " \"params\":[],\n" +
                " \"id\": 1\n" +
                "}");
    }

    @Test
    public void testExpressCreateCheckpoint() throws Exception {
        neow3jExpress.expressCreateCheckpoint("checkpoint-1.neoxp-checkpoint").send();

        verifyResult("{\n" +
                " \"jsonrpc\": \"2.0\",\n" +
                " \"method\": \"expresscreatecheckpoint\",\n" +
                " \"params\":[\"checkpoint-1.neoxp-checkpoint\"],\n" +
                " \"id\": 1\n" +
                "}");
    }

    @Test
    public void testExpressListOracleRequests() throws Exception {
        neow3jExpress.expressListOracleRequests().send();

        verifyResult("{\n" +
                " \"jsonrpc\": \"2.0\",\n" +
                " \"method\": \"expresslistoraclerequests\",\n" +
                " \"params\":[],\n" +
                " \"id\": 1\n" +
                "}");
    }

    @Test
    public void testExpressCreateOracleResponseTx() throws Exception {
        neow3jExpress.expressCreateOracleResponseTx(
                new OracleResponse(3, OracleResponseCode.SUCCESS, "bmVvdzNq")).send();

        verifyResult("{\n" +
                " \"jsonrpc\": \"2.0\",\n" +
                " \"method\": \"expresscreateoracleresponsetx\",\n" +
                " \"params\":[\n" +
                "    {\n" +
                "        \"id\": 3,\n" +
                "        \"code\": \"Success\",\n" +
                "        \"result\": \"bmVvdzNq\"\n" +
                "    }\n" +
                " ],\n" +
                " \"id\": 1\n" +
                "}");
    }

    @Test
    public void testExpressShutdown() throws Exception {
        neow3jExpress.expressShutdown().send();

        verifyResult("{\n" +
                " \"jsonrpc\": \"2.0\",\n" +
                " \"method\": \"expressshutdown\",\n" +
                " \"params\":[],\n" +
                " \"id\": 1\n" +
                "}");
    }

    // TokenTracker: Nep11

    @Test
    public void testGetNep11Balances() throws Exception {
        neow3j.getNep11Balances(new Hash160("5d75775015b024970bfeacf7c6ab1b0ade974886")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep11balances\"," +
                "\"params\":[\"NY9zhKwcmht5cQJ3oRqjJGo3QuVLwXwTzL\"]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetNep11Transfers() throws Exception {
        neow3j.getNep11Transfers(new Hash160("04457ce4219e462146ac00b09793f81bc5bca2ce")).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep11transfers\"," +
                "\"params\":[\"NekZLTu93WgrdFHxzBEJUYgLTQMAT85GLi\"]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetNep11Transfers_Date() throws Exception {
        neow3j.getNep11Transfers(new Hash160("8bed27d0e88266807a6339270f0593510967cb45"),
                new Date(1553105830L)).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep11transfers\"," +
                "\"params\":[\"NSH1UeM96PKhjuzVBKcyWeNNuQkT3sHGmA\",1553105830]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetNep11Transfers_DateFromTo() throws Exception {
        neow3j.getNep11Transfers(new Hash160("2eeda865e7824c71b3fe14bed35d04d0f2f0e9d6"),
                new Date(1553105830),
                new Date(1557305830)
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep11transfers\"," +
                "\"params\":[\"NfWL3Kx7qtZzXrajmggAD4b6r2kGzajbaJ\",1553105830,1557305830]," +
                "\"id\":1}"
        );
    }

    @Test
    public void testGetNep11Properties() throws Exception {
        neow3j.getNep11Properties(new Hash160("2eeda865e7824c71b3fe14bed35d04d0f2f0e9d6"),
                "12345"
        ).send();

        verifyResult("{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"getnep11properties\"," +
                "\"params\":[\"NfWL3Kx7qtZzXrajmggAD4b6r2kGzajbaJ\",\"12345\"]," +
                "\"id\":1}"
        );
    }

}
