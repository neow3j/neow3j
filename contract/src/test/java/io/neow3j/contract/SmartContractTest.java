package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.ContractTestHelper.setUpWireMockForGetBlockCount;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.types.Hash160;
import io.neow3j.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;

import org.hamcrest.core.StringContains;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SmartContractTest {

    private static final Hash160 NEO_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final Hash160 SOME_SCRIPT_HASH =
            new Hash160("969a77db482f74ce27105f760efa139223431394");
    private static final String NEP17_TRANSFER = "transfer";
    private static final String NEP17_BALANCEOF = "balanceOf";
    private static final String NEP17_NAME = "name";
    private static final String NEP17_TOTALSUPPLY = "totalSupply";

    private Account account1;
    private Hash160 recipient;

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
        neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port),
                new Neow3jConfig().setNetworkMagic(769));
        account1 = Account.fromWIF("L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR");
        recipient = new Hash160("969a77db482f74ce27105f760efa139223431394");
    }

    @Test
    public void constructSmartContractWithoutScriptHash() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("script hash"));
        new SmartContract(null, neow);
    }

    @Test
    public void constructSmartContractWithoutNeow3j() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("Neow3j"));
        new SmartContract(NEO_SCRIPT_HASH, null);
    }

    @Test
    public void constructSmartContract() {
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        assertThat(sc.getScriptHash(), is(NEO_SCRIPT_HASH));
    }

    @Test
    public void testGetManifest() throws IOException {
        setUpWireMockForCall("getcontractstate", "contractstate.json");
        SmartContract c = new SmartContract(SOME_SCRIPT_HASH, neow);
        ContractManifest manifest = c.getManifest();
        assertThat(manifest.getName(), is("neow3j"));
    }

    @Test
    public void testGetName() throws IOException {
        setUpWireMockForCall("getcontractstate", "contractstate.json");
        SmartContract c = new SmartContract(SOME_SCRIPT_HASH, neow);
        String name = c.getName();
        assertThat(name, is("neow3j"));
    }

    @Test
    public void invokeWithNullString() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("null"));
        new SmartContract(NEO_SCRIPT_HASH, neow).invokeFunction(null);
    }

    @Test
    public void invokeWithEmptyString() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("empty"));
        new SmartContract(NEO_SCRIPT_HASH, neow).invokeFunction("");
    }

    @Test
    public void invokeShouldProduceCorrectScript() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForGetBlockCount(1000);
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NEO_SCRIPT_HASH, NEP17_TRANSFER, asList(
                        hash160(account1.getScriptHash()),
                        hash160(recipient),
                        integer(5))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        Transaction tx = sc.invokeFunction(NEP17_TRANSFER,
                hash160(account1.getScriptHash()),
                hash160(recipient),
                integer(5))
                .wallet(w)
                .signers(Signer.feeOnly(w.getDefaultAccount().getScriptHash()))
                .sign();

        assertThat(tx.getScript(), is(expectedScript));
    }

    @Test
    public void callFunctionReturningString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_symbol.json",
                SOME_SCRIPT_HASH.toString(), "symbol");
        SmartContract sc = new SmartContract(SOME_SCRIPT_HASH, neow);
        String name = sc.callFuncReturningString("symbol");
        assertThat(name, is("ant"));
    }

    @Test
    public void callFunctionReturningNonString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                NEO_SCRIPT_HASH.toString(), NEP17_NAME);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(new StringContains(StackItemType.INTEGER.jsonValue()));
        sc.callFuncReturningString(NEP17_NAME);
    }

    @Test
    public void callFunctionReturningInt() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                NEO_SCRIPT_HASH.toString(), NEP17_TOTALSUPPLY);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        BigInteger supply = sc.callFuncReturningInt(NEP17_TOTALSUPPLY);
        assertThat(supply, is(BigInteger.valueOf(3000000000000000L)));
    }

    @Test
    public void callFunctionReturningInt_withParameter() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_3.json",
                NEO_SCRIPT_HASH.toString(), NEP17_BALANCEOF);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        BigInteger balance = sc.callFuncReturningInt(NEP17_BALANCEOF,
                hash160(new Hash160("ec2b32ed87e3747e826a0abd7229cb553220fd7a")));
        assertThat(balance, is(BigInteger.valueOf(3)));
    }

    @Test
    public void callFunctionReturningNonInt() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_returnTrue.json",
                NEO_SCRIPT_HASH.toString(), NEP17_TRANSFER);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(
                new StringContains("but expected " + StackItemType.INTEGER.jsonValue()));
        sc.callFuncReturningInt(NEP17_TRANSFER);
    }

    @Test
    public void callFunctionReturningBool() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_returnFalse.json",
                NEO_SCRIPT_HASH.toString(), NEP17_TRANSFER);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        boolean transferSuccessful = sc.callFuncReturningBool(NEP17_TRANSFER);
        assertFalse(transferSuccessful);
    }

    @Test
    public void callFunctionReturningBool_withParameter() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_returnTrue.json",
                NEO_SCRIPT_HASH.toString(), NEP17_TRANSFER);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        boolean transferSuccessful = sc.callFuncReturningBool(NEP17_TRANSFER,
                hash160(new Hash160("ec2b32ed87e3747e826a0abd7229cb553220fd7a")));
        assertTrue(transferSuccessful);
    }

    @Test
    public void callFunctionReturningBool_asInteger_zero() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_returnIntZero.json",
                NEO_SCRIPT_HASH.toString(), "getZero");
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        boolean b = sc.callFuncReturningBool("getZero");
        assertFalse(b);
    }

    @Test
    public void callFunctionReturningBool_asInteger_one() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_returnIntOne.json",
                NEO_SCRIPT_HASH.toString(), "getOne");
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        boolean b = sc.callFuncReturningBool("getOne");
        assertTrue(b);
    }

    @Test
    public void callFunctionReturningNonBool() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_getcandidates.json",
                NEO_SCRIPT_HASH.toString(), "getCandidates");
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(
                new StringContains("but expected " + StackItemType.BOOLEAN.jsonValue()));
        sc.callFuncReturningBool("getCandidates");
    }

    @Test
    public void invokingFunctionPerformsCorrectCall() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_3.json",
                NEO_SCRIPT_HASH.toString(), NEP17_BALANCEOF, account1.getScriptHash().toString());

        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        NeoInvokeFunction response = sc.callInvokeFunction(NEP17_BALANCEOF,
                singletonList(hash160(account1.getScriptHash())));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger(),
                is(BigInteger.valueOf(3)));
    }

    @Test
    public void invokingFunctionPerformsCorrectCall_WithoutParameters() throws IOException {
        setUpWireMockForCall("invokefunction",
                "invokefunction_symbol_neo.json",
                NEO_SCRIPT_HASH.toString(),
                "symbol");
        NeoInvokeFunction i = new SmartContract(NEO_SCRIPT_HASH, neow).callInvokeFunction("symbol");
        assertThat(i.getResult().getStack().get(0).getString(), Matchers.is("NEO"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void callInvokeFunction_missingFunction() throws IOException {
        new SmartContract(NEO_SCRIPT_HASH, neow).callInvokeFunction("",
                singletonList(hash160(account1.getScriptHash())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void callInvokeFunctionWithoutParameters_missingFunction() throws IOException {
        new SmartContract(NEO_SCRIPT_HASH, neow).callInvokeFunction("");
    }

}
