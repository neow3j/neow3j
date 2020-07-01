package io.neow3j.contract;

import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.crypto.ECKeyPair;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SmartContractTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow;

    @Before
    public void setUp() {
        WireMock.configure();
        neow = Neow3j.build(new HttpService("http://localhost:8080"));
    }

    @Test
    public void constructSmartContractWithoutScriptHash() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("script hash"));
        new SmartContract(null, this.neow);
    }

    @Test
    public void constructSmartContractWithoutNeow3j() {
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("Neow3j"));
        new SmartContract(neo, null);
    }

    @Test
    public void constructSmartContract() {
        SmartContract sc = new SmartContract(
                new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789"), this.neow);
        assertThat(sc.getScriptHash(),
                is(new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789")));
    }

    @Test
    public void constructSmartContractForDeploymentWithoutNewo3j() throws IOException,
            DeserializationException, URISyntaxException {

        File nef = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world.nef").toURI());
        File manifest = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world.manifest.json").toURI());
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("Neow3j"));
        new SmartContract(nef, manifest, null);
    }

    @Test
    public void constructSmartContractForDeployment() throws IOException,
            DeserializationException, URISyntaxException {

        File nef = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world.nef").toURI());
        File manifest = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world.manifest.json").toURI());
        SmartContract c = new SmartContract(nef, manifest, this.neow);
        assertThat(c.getScriptHash().toString(), is("b1872e12d6151da6312d0ff6617df37a98a48591"));
        assertThat(Numeric.cleanHexPrefix(c.getManifest().getAbi().getHash()),
                is("b1872e12d6151da6312d0ff6617df37a98a48591"));
        assertThat(c.getNefFile().getScriptHash().toString(),
                is("b1872e12d6151da6312d0ff6617df37a98a48591"));
    }

    @Test
    public void constructSmartContractForDeploymentWithUnequalScriptHashInNefAndManifest()
            throws IOException, DeserializationException, URISyntaxException {
        File nef = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world.nef").toURI());
        File manifest = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world_different_scripthash.manifest.json").toURI());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                new StringContainsInOrder(Arrays.asList("NEF", "script hash", "manifest")));
        new SmartContract(nef, manifest, this.neow);
    }

    @Test
    public void constructSmartContractForDeploymentWithTooLongManifest()
            throws IOException, DeserializationException, URISyntaxException {
        File nef = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world.nef").toURI());
        File manifest = new File(this.getClass().getClassLoader()
                .getResource("contracts/too_large.manifest.json").toURI());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("manifest is too long");
        new SmartContract(nef, manifest, this.neow);
    }

    @Test
    public void tryDeployAfterUsingWrongConstructor() throws IOException {
        SmartContract sc = new SmartContract(
                new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789"), this.neow);
        expectedException.expect(IllegalStateException.class);
        sc.deploy();
    }

    @Test
    public void invokeWithNullString() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("null"));
        new SmartContract(new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789"), this.neow)
                .invoke(null);
    }

    @Test
    public void invokeWithEmptyString() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("empty"));
        new SmartContract(new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789"), this.neow)
                .invoke("");
    }

    @Test
    public void invoke() throws IOException {
        String script =
                "150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238";
        setUpWireMockForCall("invokescript", "invokescript_transfer_5_neo.json", script,
                "969a77db482f74ce27105f760efa139223431394"); // witness script hash

        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
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
        assertThat(i.getTransaction().getNetworkFee(), is(1264390L));
        assertThat(i.getTransaction().getSystemFee(), is(9007810L));
        assertThat(i.getTransaction().getScript(), is(Numeric.hexStringToByteArray(script)));
        byte[] expectedVerificationScript = Numeric.hexStringToByteArray(
                "0c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380b418a6b1e75");
        assertThat(i.getTransaction().getWitnesses().get(0).getVerificationScript().getScript(),
                is(expectedVerificationScript));
    }

    @Test
    public void callFunctionReturningString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_name.json",
                "9bde8f209c88dd0e7ca3bf0af0f476cdd8207789", "name");
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        SmartContract sc = new SmartContract(neo, this.neow);
        String name = sc.callFuncReturningString("name");
        assertThat(name, is("NEO"));
    }

    @Test
    public void callFunctionReturningNonString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                "9bde8f209c88dd0e7ca3bf0af0f476cdd8207789", "name");
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        SmartContract sc = new SmartContract(neo, this.neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(new StringContains(StackItemType.INTEGER.jsonValue()));
        sc.callFuncReturningString("name");
    }

    @Test
    public void callFunctionReturningInt() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                "8c23f196d8a1bfd103a9dcb1f9ccf0c611377d3b", "totalSupply");
        ScriptHash neo = new ScriptHash("8c23f196d8a1bfd103a9dcb1f9ccf0c611377d3b");
        SmartContract sc = new SmartContract(neo, this.neow);
        BigInteger supply = sc.callFuncReturningInt("totalSupply");
        assertThat(supply, is(BigInteger.valueOf(3000000000000000L)));
    }

    @Test
    public void callFunctionReturningNonInt() throws IOException {
        setUpWireMockForCall("invokefunction",
                "invokescript_registercandidate.json",
                "9bde8f209c88dd0e7ca3bf0af0f476cdd8207789", "totalSupply");
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        SmartContract sc = new SmartContract(neo, this.neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(new StringContains(StackItemType.BOOLEAN.jsonValue()));
        sc.callFuncReturningInt("totalSupply");
    }

    @Test
    public void callFunctionWithParams() throws IOException {
        setUpWireMockForCall("invokefunction",
                "invokefunction_balanceOf.json",
                "9bde8f209c88dd0e7ca3bf0af0f476cdd8207789",
                "balanceOf",
                "df133e846b1110843ac357fc8bbf05b4a32e17c8");
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        SmartContract sc = new SmartContract(neo, this.neow);

        ScriptHash acc = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");
        NeoInvokeFunction response = sc.invokeFunction("balanceOf", ContractParameter.hash160(acc));
        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(BigInteger.valueOf(3000000000000000L)));
    }

    @Test
    public void invokeScriptOnDeployment()
            throws IOException, DeserializationException, URISyntaxException {
        File nef = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world.nef").toURI());
        File manifest = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world.manifest.json").toURI());

        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(
                "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9"));
        Account a = new Account(pair);
        Wallet w = Wallet.withAccounts(a);
        String script =
                "0db8017b2267726f757073223a5b5d2c226665617475726573223a7b2273746f72616765223a747275652c2270617961626c65223a66616c73657d2c22616269223a7b2268617368223a22307862313837326531326436313531646136333132643066663636313764663337613938613438353931222c226d6574686f6473223a5b7b226e616d65223a226d61696e222c22706172616d6574657273223a5b7b226e616d65223a226f7065726174696f6e222c2274797065223a22537472696e67227d2c7b226e616d65223a2261726773222c2274797065223a224172726179227d5d2c226f6666736574223a302c2272657475726e54797065223a22426f6f6c65616e227d5d2c226576656e7473223a5b5d7d2c227065726d697373696f6e73223a5b7b22636f6e7472616374223a222a222c226d6574686f6473223a222a227d5d2c22747275737473223a5b5d2c22736166654d6574686f6473223a5b5d2c226578747261223a7b22417574686f72223a224e656f222c22456d61696c223a22646576406e656f2e6f7267222c224465736372697074696f6e223a2254686973206973206120636f6e7472616374206578616d706c65227d7d0c1f5700020c0548656c6c6f0c05576f726c642150419bf667ce41e63f1884114041ce352c85";
        setUpWireMockForCall("invokescript", "invokescript_deploy.json");

        NeoInvokeScript response = new SmartContract(nef, manifest, neow).deploy()
                .withSender(a.getScriptHash())
                .withWallet(w)
                .invokeScript();

        assertThat(response.getInvocationResult().getScript(), is(script));
        assertThat(response.getInvocationResult().getStack().get(0).getType(),
                is(StackItemType.INTEROP_INTERFACE));
    }

    @Test
    public void deploy() throws IOException, DeserializationException, URISyntaxException {
        NeoConfig.setMagicNumber(new byte[]{0x01, 0x03, 0x00, 0x0}); // Magic number 769
        File nef = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world.nef").toURI());
        File manifest = new File(this.getClass().getClassLoader()
                .getResource("contracts/hello_world.manifest.json").toURI());

        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(
                "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9"));
        Account a = new Account(pair);
        Wallet w = Wallet.withAccounts(a);
        String script =
                "0db8017b2267726f757073223a5b5d2c226665617475726573223a7b2273746f72616765223a747275652c2270617961626c65223a66616c73657d2c22616269223a7b2268617368223a22307862313837326531326436313531646136333132643066663636313764663337613938613438353931222c226d6574686f6473223a5b7b226e616d65223a226d61696e222c22706172616d6574657273223a5b7b226e616d65223a226f7065726174696f6e222c2274797065223a22537472696e67227d2c7b226e616d65223a2261726773222c2274797065223a224172726179227d5d2c226f6666736574223a302c2272657475726e54797065223a22426f6f6c65616e227d5d2c226576656e7473223a5b5d7d2c227065726d697373696f6e73223a5b7b22636f6e7472616374223a222a222c226d6574686f6473223a222a227d5d2c22747275737473223a5b5d2c22736166654d6574686f6473223a5b5d2c226578747261223a7b22417574686f72223a224e656f222c22456d61696c223a22646576406e656f2e6f7267222c224465736372697074696f6e223a2254686973206973206120636f6e7472616374206578616d706c65227d7d0c1f5700020c0548656c6c6f0c05576f726c642150419bf667ce41e63f1884114041ce352c85";
        setUpWireMockForCall("invokescript", "invokescript_deploy.json", script,
                "df133e846b1110843ac357fc8bbf05b4a32e17c8");
        String verificationScript =
                "0c2102200284598c6c1117f163dd938a4c8014cf2cf1164c4b7197f347109db50eae7c0b418a6b1e75";
        setUpWireMockForCall("sendrawtransaction", "sendrawtransaction.json", script,
                verificationScript); // verification script, part of the transaction hex.

        Invocation i = new SmartContract(nef, manifest, neow).deploy()
                .withSender(a.getScriptHash())
                .withWallet(w)
                .withValidUntilBlock(1000)
                .build()
                .sign();

        assertThat(i.getTransaction().getSender(),
                is(new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8")));
        assertThat(i.getTransaction().getSystemFee(), is(47113180L));
        assertThat(i.getTransaction().getNetworkFee(), is(1662390L));
        assertThat(i.getTransaction().getScript(), is(Numeric.hexStringToByteArray(script)));
        assertThat(i.getTransaction().getWitnesses().get(0).getVerificationScript().getScript(),
                is(Numeric.hexStringToByteArray(verificationScript)));
    }
}