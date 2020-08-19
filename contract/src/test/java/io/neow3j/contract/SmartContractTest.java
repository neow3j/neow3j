package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.WIF;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.NeoConfig;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.hamcrest.core.StringContains;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SmartContractTest {

    private static final String CONSENSUS_NODE_WIF =
            "L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR";
    private static final String CONSENSUS_NODE_SCRIPTHASH =
            "cc45cc8987b0e35371f5685431e3c8eeea306722";
    private static final String TEST_CONTRACT_1_NEF = "contracts/test_contract_1.nef";
    private static final String TEST_CONTRACT_1_MANIFEST =
            "contracts/test_contract_1.manifest.json";
    private static final String TEST_CONTRACT_1_DEPLOY_SCRIPT =
            "0d5f017b2267726f757073223a5b5d2c226665617475726573223a7b2273746f72616765223a747275652c2270617961626c65223a66616c73657d2c22737570706f727465647374616e6461726473223a5b5d2c22616269223a7b2268617368223a22307863353730653563643036386464396638646364656530653462323031643730616166663631666639222c226d6574686f6473223a5b7b226e616d65223a22656e747279222c22706172616d6574657273223a5b7b226e616d65223a2273222c2274797065223a22537472696e67227d5d2c226f6666736574223a302c2272657475726e74797065223a22427974654172726179227d5d2c226576656e7473223a5b5d7d2c227065726d697373696f6e73223a5b7b22636f6e7472616374223a222a222c226d6574686f6473223a222a227d5d2c22747275737473223a5b5d2c22736166656d6574686f6473223a5b5d2c226578747261223a6e756c6c7d0c2a5700010c0568656c6c6f0c05776f726c642150419bf667ce41e63f18847821419bf667ce41925de8314041ce352c85";
    private File nefFile;
    private File manifestFile;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow;

    @Before
    public void setUp() throws URISyntaxException {
        WireMock.configure();
        neow = Neow3j.build(new HttpService("http://localhost:8080"));
        nefFile = new File(this.getClass().getClassLoader()
                .getResource(TEST_CONTRACT_1_NEF).toURI());
        manifestFile = new File(this.getClass().getClassLoader()
                .getResource(TEST_CONTRACT_1_MANIFEST).toURI());
    }

    @Test
    public void constructSmartContractWithoutScriptHash() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("script hash"));
        new SmartContract(null, this.neow);
    }

    @Test
    public void constructSmartContractWithoutNeow3j() {
        ScriptHash neo = new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("Neow3j"));
        new SmartContract(neo, null);
    }

    @Test
    public void constructSmartContract() {
        SmartContract sc = new SmartContract(
                new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525"), this.neow);
        assertThat(sc.getScriptHash(),
                is(new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525")));
    }

    @Test
    public void constructSmartContractForDeploymentWithoutNeow3j() throws IOException,
            DeserializationException, URISyntaxException {

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("Neow3j"));
        new SmartContract(nefFile, manifestFile, null);
    }

    @Test
    public void constructSmartContractForDeployment() throws IOException,
            DeserializationException {

        SmartContract c = new SmartContract(nefFile, manifestFile, this.neow);
        assertThat(c.getScriptHash().toString(), is("c570e5cd068dd9f8dcdee0e4b201d70aaff61ff9"));
        assertThat(c.getManifest().getAbi().getHash(),
                is("0xc570e5cd068dd9f8dcdee0e4b201d70aaff61ff9"));
        assertThat(c.getNefFile().getScriptHash().toString(),
                is("c570e5cd068dd9f8dcdee0e4b201d70aaff61ff9"));
    }

    @Test
    public void constructSmartContractForDeploymentWithUnequalScriptHashInNefAndManifest()
            throws IOException, DeserializationException, URISyntaxException {

        File manifest = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world_different_scripthash.manifest.json").toURI());
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                new StringContainsInOrder(Arrays.asList("NEF", "script hash", "manifest")));
        new SmartContract(nefFile, manifest, this.neow);
    }

    @Test
    public void constructSmartContractForDeploymentWithTooLongManifest()
            throws IOException, DeserializationException, URISyntaxException {

        File manifest = new File(this.getClass().getClassLoader()
                .getResource("contracts/too_large.manifest.json").toURI());
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("manifest is too long");
        new SmartContract(nefFile, manifest, this.neow);
    }

    @Test
    public void tryDeployAfterUsingWrongConstructor() throws IOException {
        SmartContract sc = new SmartContract(
                new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525"), this.neow);
        expectedException.expect(IllegalStateException.class);
        sc.deploy();
    }

    @Test
    public void invokeWithNullString() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("null"));
        new SmartContract(new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525"), this.neow)
                .invoke(null);
    }

    @Test
    public void invokeWithEmptyString() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("empty"));
        new SmartContract(new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525"), this.neow)
                .invoke("");
    }

    @Test
    public void invoke() throws IOException {
        byte[] expectedScript = new ScriptBuilder()
                .pushInteger(5)
                .pushData(Numeric.hexStringToByteArray("c8172ea3b405bf8bfc57c33a8410116b843e13df"))
                .pushData(Numeric.hexStringToByteArray("064a5dcc0f162c83473d028938e95fb776131e72"))
                .pushInteger(3)
                .pack()
                .pushData(Numeric.hexStringToByteArray("7472616e73666572"))
                .pushData(Numeric.hexStringToByteArray("25059ecb4878d3a875f91c51ceded330d4575fde"))
                .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CALL)
                .opCode(OpCode.ASSERT)
                .toArray();
        setUpWireMockForCall("invokescript", "invokescript_transfer_5_neo.json",
                Numeric.toHexStringNoPrefix(expectedScript));

        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);
        ScriptHash neo = new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525");
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");
        SmartContract sc = new SmartContract(neo, this.neow);
        Invocation i = sc.invoke("transfer")
                .withWallet(w)
                .withParameters(ContractParameter.hash160(sender.getScriptHash()),
                        ContractParameter.hash160(receiver),
                        ContractParameter.integer(5))
                .withNonce(1800992192)
                .withValidUntilBlock(2107199)
                .failOnFalse()
                .build()
                .sign();

        assertThat(i.getTransaction().getNonce(), is(1800992192L));
        assertThat(i.getTransaction().getValidUntilBlock(), is(2107199L));
        assertThat(i.getTransaction().getNetworkFee(), is(133000L));
        assertThat(i.getTransaction().getSystemFee(), is(9007810L));
        assertThat(i.getTransaction().getScript(), is(expectedScript));
        byte[] expectedVerificationScript = ScriptBuilder.buildVerificationScript(sender.getECKeyPair().getPublicKey().getEncoded(true));
        assertThat(i.getTransaction().getWitnesses().get(0).getVerificationScript().getScript(),
                is(expectedVerificationScript));
    }

    @Test
    public void callFunctionReturningString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_name.json",
                "de5f57d430d3dece511cf975a8d37848cb9e0525", "name");
        ScriptHash neo = new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525");
        SmartContract sc = new SmartContract(neo, this.neow);
        String name = sc.callFuncReturningString("name");
        assertThat(name, is("NEO"));
    }

    @Test
    public void callFunctionReturningNonString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                "de5f57d430d3dece511cf975a8d37848cb9e0525", "name");
        ScriptHash neo = new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525");
        SmartContract sc = new SmartContract(neo, this.neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(new StringContains(StackItemType.INTEGER.jsonValue()));
        sc.callFuncReturningString("name");
    }

    @Test
    public void callFunctionReturningInt() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                "668e0c1f9d7b70a99dd9e06eadd4c784d641afbc", "totalSupply");
        ScriptHash neo = new ScriptHash("668e0c1f9d7b70a99dd9e06eadd4c784d641afbc");
        SmartContract sc = new SmartContract(neo, this.neow);
        BigInteger supply = sc.callFuncReturningInt("totalSupply");
        assertThat(supply, is(BigInteger.valueOf(3000000000000000L)));
    }

    @Test
    public void callFunctionReturningNonInt() throws IOException {
        setUpWireMockForCall("invokefunction",
                "invokescript_registercandidate.json",
                "de5f57d430d3dece511cf975a8d37848cb9e0525", "totalSupply");
        ScriptHash neo = new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525");
        SmartContract sc = new SmartContract(neo, this.neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(new StringContains(StackItemType.BOOLEAN.jsonValue()));
        sc.callFuncReturningInt("totalSupply");
    }

    @Test
    public void callFunctionWithParams() throws IOException {
        setUpWireMockForCall("invokefunction",
                "invokefunction_balanceOf.json",
                "de5f57d430d3dece511cf975a8d37848cb9e0525",
                "balanceOf",
                "df133e846b1110843ac357fc8bbf05b4a32e17c8");
        ScriptHash neo = new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525");
        SmartContract sc = new SmartContract(neo, this.neow);

        ScriptHash acc = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");
        NeoInvokeFunction response = sc.invokeFunction("balanceOf", ContractParameter.hash160(acc));
        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(BigInteger.valueOf(3000000000000000L)));
    }

    @Ignore("The test fails because the mocked `incokescript` RPC call expects the script hash of "
            + "the sender in as a parameter in the list of signers. But because of changed "
            + "InteropServiceCalls, neow3j currently produces another script hash from the sender"
            + " account as expected in this test. As soon as these changes to the "
            + "InteropServiceCode are introduced to neow3j this test can be normally run.")
    @Test
    public void invokeScriptOnDeployment()
            throws IOException, DeserializationException, URISyntaxException {
        File nef = new File(this.getClass().getClassLoader()
                .getResource(TEST_CONTRACT_1_NEF).toURI());
        File manifest = new File(this.getClass().getClassLoader()
                .getResource(TEST_CONTRACT_1_MANIFEST).toURI());

        ECKeyPair pair = ECKeyPair.create(WIF.getPrivateKeyFromWIF(CONSENSUS_NODE_WIF));
        Account a = new Account(pair);
        Wallet w = Wallet.withAccounts(a);

        String script =
                "0d5f017b2267726f757073223a5b5d2c226665617475726573223a7b2273746f72616765223a747275652c2270617961626c65223a66616c73657d2c22737570706f727465647374616e6461726473223a5b5d2c22616269223a7b2268617368223a22307863353730653563643036386464396638646364656530653462323031643730616166663631666639222c226d6574686f6473223a5b7b226e616d65223a22656e747279222c22706172616d6574657273223a5b7b226e616d65223a2273222c2274797065223a22537472696e67227d5d2c226f6666736574223a302c2272657475726e74797065223a22427974654172726179227d5d2c226576656e7473223a5b5d7d2c227065726d697373696f6e73223a5b7b22636f6e7472616374223a222a222c226d6574686f6473223a222a227d5d2c22747275737473223a5b5d2c22736166656d6574686f6473223a5b5d2c226578747261223a6e756c6c7d0c2a5700010c0568656c6c6f0c05776f726c642150419bf667ce41e63f18847821419bf667ce41925de8314041ce352c85";
        setUpWireMockForCall("invokescript", "invokescript_deploy.json",
                script, CONSENSUS_NODE_SCRIPTHASH);

        NeoInvokeScript response = new SmartContract(nef, manifest, neow).deploy()
                .withSender(a.getScriptHash())
                .withWallet(w)
                .invokeScript();

        assertThat(response.getInvocationResult().getScript(), is(script));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.INTEROP_INTERFACE));
    }

    @Test
    public void deployProducesCorrectScript() throws IOException, DeserializationException,
            URISyntaxException {

        NeoConfig.setMagicNumber(new byte[]{0x01, 0x03, 0x00, 0x0}); // Magic number 769

        ECKeyPair pair = ECKeyPair.create(WIF.getPrivateKeyFromWIF(CONSENSUS_NODE_WIF));
        Account a = new Account(pair);
        Wallet w = Wallet.withAccounts(a);

        setUpWireMockForCall("sendrawtransaction", "sendrawtransaction.json");
        setUpWireMockForCall("invokescript", "invokescript_deploy.json",
                TEST_CONTRACT_1_DEPLOY_SCRIPT);

        Invocation i = new SmartContract(nefFile, manifestFile, neow).deploy()
                .withSender(a.getScriptHash())
                .withWallet(w)
                .withValidUntilBlock(1000)
                .build()
                .sign();

        assertThat(i.getTransaction().getScript(),
                is(Numeric.hexStringToByteArray(TEST_CONTRACT_1_DEPLOY_SCRIPT)));
    }
}