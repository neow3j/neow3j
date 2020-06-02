package io.neow3j.contract;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.math.BigInteger;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SmartContractTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    Neow3j neow;

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
        // Required for fetching of system fee of the invocation.
        ContractTestHelper.setUpWireMockForCall("invokefunction",
                "invokefunction_transfer_neo.json",
                "9bde8f209c88dd0e7ca3bf0af0f476cdd8207789", // NEO script hash
                "transfer", // method
                "969a77db482f74ce27105f760efa139223431394", // sender script hash
                "df133e846b1110843ac357fc8bbf05b4a32e17c8", // receiver script hash
                "5", // amount
                "969a77db482f74ce27105f760efa139223431394" // witness script hash
        );

        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = Account.fromECKeyPair(senderPair).isDefault().build();
        Wallet w = new Wallet.Builder().accounts(sender).build();
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");
        SmartContract sc = new SmartContract(neo, this.neow);
        Invocation i = sc.invoke("transfer")
                .withWallet(w)
                .withParameters(ContractParameter.hash160(sender.getScriptHash()),
                        ContractParameter.hash160(receiver),
                        ContractParameter.integer(5))
                .withNonce(1800992192)
                .validUntilBlock(2107199)
                .failOnFalse()
                .build()
                .sign();

        assertThat(i.getTransaction().getNonce(), Matchers.is(1800992192L));
        assertThat(i.getTransaction().getValidUntilBlock(), Matchers.is(2107199L));
        assertThat(i.getTransaction().getNetworkFee(), Matchers.is(1264390L));
        assertThat(i.getTransaction().getSystemFee(), Matchers.is(9007810L));
        byte[] expectedScript = Numeric.hexStringToByteArray(
                "150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238");
        assertThat(i.getTransaction().getScript(), Matchers.is(expectedScript));
        byte[] expectedVerificationScript = Numeric.hexStringToByteArray(
                "0c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380b418a6b1e75");
        assertThat(i.getTransaction().getWitnesses().get(0).getVerificationScript().getScript(),
                Matchers.is(expectedVerificationScript));
    }

    @Test
    public void callFunctionReturningString() throws IOException {
        ContractTestHelper.setUpWireMockForCall("invokefunction", "invokefunction_name.json",
                "9bde8f209c88dd0e7ca3bf0af0f476cdd8207789", "name");
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        SmartContract sc = new SmartContract(neo, this.neow);
        String name = sc.callFuncReturningString("name");
        assertThat(name, is("NEO"));
    }

    @Test
    public void callFunctionReturningNonString() throws IOException {
        ContractTestHelper.setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                "9bde8f209c88dd0e7ca3bf0af0f476cdd8207789", "name");
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        SmartContract sc = new SmartContract(neo, this.neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(new StringContains(StackItemType.INTEGER.jsonValue()));
        sc.callFuncReturningString("name");
    }

    @Test
    public void callFunctionReturningInt() throws IOException {
        ContractTestHelper.setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                "8c23f196d8a1bfd103a9dcb1f9ccf0c611377d3b", "totalSupply");
        ScriptHash neo = new ScriptHash("8c23f196d8a1bfd103a9dcb1f9ccf0c611377d3b");
        SmartContract sc = new SmartContract(neo, this.neow);
        BigInteger supply = sc.callFuncReturningInt("totalSupply");
        assertThat(supply, is(BigInteger.valueOf(3000000000000000L)));
    }

    @Test
    public void callFunctionReturningNonInt() throws IOException {
        ContractTestHelper.setUpWireMockForCall("invokefunction",
                "invokefunction_registercandidate.json",
                "9bde8f209c88dd0e7ca3bf0af0f476cdd8207789", "totalSupply");
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        SmartContract sc = new SmartContract(neo, this.neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(new StringContains(StackItemType.BOOLEAN.jsonValue()));
        sc.callFuncReturningInt("totalSupply");
    }

    @Test
    public void callFunctionWithParams() throws IOException {
        ContractTestHelper.setUpWireMockForCall("invokefunction",
                "invokefunction_balanceOf.json",
                "9bde8f209c88dd0e7ca3bf0af0f476cdd8207789",
                "balanceOf",
                "df133e846b1110843ac357fc8bbf05b4a32e17c8");
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        SmartContract sc = new SmartContract(neo, this.neow);

        ScriptHash acc = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");
        NeoInvokeFunction response = sc.callFunction("balanceOf", ContractParameter.hash160(acc));
        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(BigInteger.valueOf(3000000000000000L)));
    }
}