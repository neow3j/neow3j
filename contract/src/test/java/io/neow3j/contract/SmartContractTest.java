package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForGetBlockCount;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.hamcrest.core.StringContains;
import org.hamcrest.Matchers;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SmartContractTest {

    private static final ScriptHash NEO_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final ScriptHash SOME_SCRIPT_HASH =
            new ScriptHash("969a77db482f74ce27105f760efa139223431394");
    private static final String NEP5_TRANSFER = "transfer";
    private static final String NEP5_BALANCEOF = "balanceOf";
    private static final String NEP5_NAME = "name";
    private static final String NEP5_TOTALSUPPLY = "totalSupply";

    private static final String TEST_CONTRACT_1_NEF = "contracts/test_contract_1.nef";
    private static final String TEST_CONTRACT_1_MANIFEST =
            "contracts/test_contract_1.manifest.json";
    private static final String TEST_CONTRACT_1_SCRIPT_HASH =
            "0xc570e5cd068dd9f8dcdee0e4b201d70aaff61ff9";
    private static final String TEST_CONTRACT_1_DEPLOY_SCRIPT =
            "0d5f017b2267726f757073223a5b5d2c226665617475726573223a7b2273746f72616765223a747275652c2270617961626c65223a66616c73657d2c22737570706f727465647374616e6461726473223a5b5d2c22616269223a7b2268617368223a22307863353730653563643036386464396638646364656530653462323031643730616166663631666639222c226d6574686f6473223a5b7b226e616d65223a22656e747279222c22706172616d6574657273223a5b7b226e616d65223a2273222c2274797065223a22537472696e67227d5d2c226f6666736574223a302c2272657475726e74797065223a22427974654172726179227d5d2c226576656e7473223a5b5d7d2c227065726d697373696f6e73223a5b7b22636f6e7472616374223a222a222c226d6574686f6473223a222a227d5d2c22747275737473223a5b5d2c22736166656d6574686f6473223a5b5d2c226578747261223a6e756c6c7d0c2a5700010c0568656c6c6f0c05776f726c642150419bf667ce41e63f18847821419bf667ce41925de8314041ce352c85";
    private static final String SCRIPT_NEO_INVOKEFUNCTION_NAME = Numeric.toHexStringNoPrefix(
            new ScriptBuilder().contractCall(NEO_SCRIPT_HASH, "name", new ArrayList<>())
                    .toArray());

    private File nefFile;
    private File manifestFile;
    private Account account1;
    private ScriptHash recipient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private Neow3j neow;

    @Before
    public void setUp() throws URISyntaxException {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);
        neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));

        nefFile = new File(this.getClass().getClassLoader()
                .getResource(TEST_CONTRACT_1_NEF).toURI());
        manifestFile = new File(this.getClass().getClassLoader()
                .getResource(TEST_CONTRACT_1_MANIFEST).toURI());

        account1 = Account.fromWIF("L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR");
        recipient = new ScriptHash("969a77db482f74ce27105f760efa139223431394");
    }

    @Test
    public void constructSmartContractWithoutScriptHash() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("script hash"));
        new SmartContract(null, this.neow);
    }

    @Test
    public void constructSmartContractWithoutNeow3j() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("Neow3j"));
        new SmartContract(NEO_SCRIPT_HASH, null);
    }

    @Test
    public void constructSmartContract() {
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, this.neow);
        assertThat(sc.getScriptHash(), is(NEO_SCRIPT_HASH));
    }

    @Test
    public void constructSmartContractForDeploymentWithoutNeow3j() throws IOException,
            DeserializationException {

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("Neow3j"));
        new SmartContract(nefFile, manifestFile, null);
    }

    @Test
    public void constructSmartContractForDeployment() throws IOException,
            DeserializationException {

        SmartContract c = new SmartContract(nefFile, manifestFile, this.neow);
        assertThat(c.getScriptHash().toString(),
                is(Numeric.cleanHexPrefix(TEST_CONTRACT_1_SCRIPT_HASH)));
        assertThat(c.getManifest().getAbi().getHash(), is(TEST_CONTRACT_1_SCRIPT_HASH));
        assertThat(c.getNefFile().getScriptHash().toString(),
                is(Numeric.cleanHexPrefix(TEST_CONTRACT_1_SCRIPT_HASH)));
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
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, this.neow);
        expectedException.expect(IllegalStateException.class);
        sc.deploy();
    }

    @Test
    public void invokeWithNullString() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("null"));
        new SmartContract(NEO_SCRIPT_HASH, this.neow).invokeFunction(null);
    }

    @Test
    public void invokeWithEmptyString() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("empty"));
        new SmartContract(NEO_SCRIPT_HASH, this.neow).invokeFunction("");
    }

    @Test
    public void invokeShouldProduceCorrectScript() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForGetBlockCount(1000);
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NEO_SCRIPT_HASH, NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(5))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, this.neow);
        Transaction tx = sc.invokeFunction(NEP5_TRANSFER,
                ContractParameter.hash160(account1.getScriptHash()),
                ContractParameter.hash160(recipient),
                ContractParameter.integer(5))
                .wallet(w)
                .signers(Signer.feeOnly(w.getDefaultAccount().getScriptHash()))
                .sign();

        assertThat(tx.getScript(), is(expectedScript));
    }

    @Test
    public void callFunctionReturningString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_name.json",
                SOME_SCRIPT_HASH.toString(), "name");
        SmartContract sc = new SmartContract(SOME_SCRIPT_HASH, this.neow);
        String name = sc.callFuncReturningString("name");
        assertThat(name, is("ANT"));
    }

    @Test
    public void callFunctionReturningNonString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                NEO_SCRIPT_HASH.toString(), NEP5_NAME);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, this.neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(new StringContains(StackItemType.INTEGER.jsonValue()));
        sc.callFuncReturningString(NEP5_NAME);
    }

    @Test
    public void callFunctionReturningInt() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                NEO_SCRIPT_HASH.toString(), NEP5_TOTALSUPPLY);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, this.neow);
        BigInteger supply = sc.callFuncReturningInt(NEP5_TOTALSUPPLY);
        assertThat(supply, is(BigInteger.valueOf(3000000000000000L)));
    }

    @Test
    public void callFunctionReturningNonInt() throws IOException {
        setUpWireMockForCall("invokefunction", "invokescript_registercandidate.json",
                NEO_SCRIPT_HASH.toString(), NEP5_TOTALSUPPLY);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, this.neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(new StringContains(StackItemType.BOOLEAN.jsonValue()));
        sc.callFuncReturningInt(NEP5_TOTALSUPPLY);
    }

    @Test
    public void invokingFunctionPerformsCorrectCall() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_3.json",
                NEO_SCRIPT_HASH.toString(), NEP5_BALANCEOF, account1.getScriptHash().toString());

        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, this.neow);
        NeoInvokeFunction response = sc.callInvokeFunction(NEP5_BALANCEOF,
                Arrays.asList(ContractParameter.hash160(account1.getScriptHash())));
        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(BigInteger.valueOf(3)));
    }
    @Test
    public void invokingFunctionPerformsCorrectCall_WithoutParameters() throws IOException {
        setUpWireMockForCall("invokefunction",
                "invokefunction_name.json",
                SOME_SCRIPT_HASH.toString(),
                "name",
                "[\"721e1376b75fe93889023d47832c160fcc5d4a06\"]"
        );

        NeoInvokeFunction i = new SmartContract(SOME_SCRIPT_HASH, neow)
                .callInvokeFunction("name");

        assertThat(i.getResult().getStack().get(0).asByteString().getAsString(), Matchers.is("ANT"));
        assertThat(i.getResult().getScript(), Matchers.is(SCRIPT_NEO_INVOKEFUNCTION_NAME));
    }

    @Test
    public void callingInvokeScriptOnContractDeployProducesCorrectCall() throws IOException,
            DeserializationException, URISyntaxException {

        setUpWireMockForCall("invokescript", "invokescript_deploy.json",
                TEST_CONTRACT_1_DEPLOY_SCRIPT);
        File nef = new File(this.getClass().getClassLoader()
                .getResource(TEST_CONTRACT_1_NEF).toURI());
        File manifest = new File(this.getClass().getClassLoader()
                .getResource(TEST_CONTRACT_1_MANIFEST).toURI());
        Wallet w = Wallet.withAccounts(account1);
        NeoInvokeScript response = new SmartContract(nef, manifest, neow).deploy()
                .signers(Signer.feeOnly(account1.getScriptHash()))
                .wallet(w)
                .callInvokeScript();

        assertThat(response.getInvocationResult().getScript(), is(TEST_CONTRACT_1_DEPLOY_SCRIPT));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.INTEROP_INTERFACE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void callInvokeFunction_missingFunction() throws IOException {
        new SmartContract(NEO_SCRIPT_HASH, neow).callInvokeFunction("",
                Arrays.asList(ContractParameter.hash160(account1.getScriptHash())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void callInvokeFunctionWithoutParameters_missingFunction() throws IOException {
        new SmartContract(NEO_SCRIPT_HASH, neow).callInvokeFunction("");
    }

    @Test
    public void deployProducesCorrectScript() throws Throwable {
        setUpWireMockForCall("sendrawtransaction", "sendrawtransaction.json");
        setUpWireMockForCall("invokescript", "invokescript_deploy.json",
                TEST_CONTRACT_1_DEPLOY_SCRIPT);

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new SmartContract(nefFile, manifestFile, neow).deploy()
                .wallet(w)
                .signers(Signer.calledByEntry(account1.getScriptHash()))
                .validUntilBlock(1000)
                .sign();

        assertThat(tx.getScript(), is(Numeric.hexStringToByteArray(TEST_CONTRACT_1_DEPLOY_SCRIPT)));
    }
}
