package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash160;
import io.neow3j.types.StackItemType;
import io.neow3j.wallet.Account;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SmartContractTest {

    private static final Hash160 NEO_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final Hash160 SOME_SCRIPT_HASH = new Hash160("969a77db482f74ce27105f760efa139223431394");

    private SmartContract someContract;
    private SmartContract neoContract;

    private static final String NEP17_TRANSFER = "transfer";
    private static final String NEP17_BALANCEOF = "balanceOf";
    private static final String NEP17_NAME = "name";
    private static final String NEP17_TOTALSUPPLY = "totalSupply";

    private Account account1;
    private Hash160 recipient;

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private Neow3j neow;

    @BeforeAll
    public void setUp() throws URISyntaxException {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);
        neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port), new Neow3jConfig().setNetworkMagic(769));
        account1 = Account.fromWIF("L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR");
        recipient = new Hash160("969a77db482f74ce27105f760efa139223431394");
        someContract = new SmartContract(SOME_SCRIPT_HASH, neow);
        neoContract = new SmartContract(NEO_SCRIPT_HASH, neow);
    }

    @Test
    public void constructSmartContractWithoutScriptHash() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> new SmartContract(null, neow));
        assertThat(thrown.getMessage(), is("The contract script hash must not be null."));
    }

    @Test
    public void constructSmartContractWithoutNeow3j() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> new SmartContract(NEO_SCRIPT_HASH, null));
        assertThat(thrown.getMessage(), is("The Neow3j object must not be null."));
    }

    @Test
    public void constructSmartContract() {
        SmartContract sc = neoContract;
        assertThat(sc.getScriptHash(), is(NEO_SCRIPT_HASH));
    }

    @Test
    public void testGetManifest() throws IOException {
        setUpWireMockForCall("getcontractstate", "contractstate.json");
        SmartContract c = someContract;
        ContractManifest manifest = c.getManifest();

        assertThat(manifest.getName(), is("neow3j"));
    }

    @Test
    public void testGetName() throws IOException {
        setUpWireMockForCall("getcontractstate", "contractstate.json");
        SmartContract c = someContract;
        String name = c.getName();

        assertThat(name, is("neow3j"));
    }

    @Test
    public void invokeWithNullString() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> neoContract.invokeFunction(null));
        assertThat(thrown.getMessage(), is("The invocation function must not be null or empty."));
    }

    @Test
    public void invokeWithEmptyString() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> neoContract.invokeFunction(""));
        assertThat(thrown.getMessage(), is("The invocation function must not be null or empty."));
    }

    @Test
    public void testBuildInvokeFunctionScript() {
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NEO_SCRIPT_HASH, NEP17_TRANSFER, asList(
                        hash160(account1.getScriptHash()),
                        hash160(recipient),
                        integer(42)))
                .toArray();

        byte[] script = neoContract
                .buildInvokeFunctionScript(NEP17_TRANSFER, hash160(account1), hash160(recipient),
                        integer(42));

        assertThat(script, is(expectedScript));
    }

    @Test
    public void invokeShouldProduceCorrectScript() {
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NEO_SCRIPT_HASH, NEP17_TRANSFER, asList(
                        hash160(account1.getScriptHash()),
                        hash160(recipient),
                        integer(5)))
                .toArray();

        SmartContract sc = neoContract;
        TransactionBuilder b = sc.invokeFunction(NEP17_TRANSFER,
                hash160(account1.getScriptHash()),
                hash160(recipient),
                integer(5));

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void callFunctionReturningString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_symbol.json",
                SOME_SCRIPT_HASH.toString(), "symbol");
        SmartContract sc = someContract;
        String name = sc.callFunctionReturningString("symbol");

        assertThat(name, is("ant"));
    }

    @Test
    public void callFunctionReturningNonString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                NEO_SCRIPT_HASH.toString(), NEP17_NAME);
        SmartContract sc = neoContract;

        assertThrows(UnexpectedReturnTypeException.class, () -> sc.callFunctionReturningString(NEP17_NAME));
    }

    @Test
    public void callFunctionReturningInt() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                NEO_SCRIPT_HASH.toString(), NEP17_TOTALSUPPLY);
        SmartContract sc = neoContract;
        BigInteger supply = sc.callFunctionReturningInt(NEP17_TOTALSUPPLY);

        assertThat(supply, is(BigInteger.valueOf(3000000000000000L)));
    }

    @Test
    public void callFunctionReturningInt_withParameter() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_3.json",
                NEO_SCRIPT_HASH.toString(), NEP17_BALANCEOF);
        SmartContract sc = neoContract;
        BigInteger balance = sc.callFunctionReturningInt(NEP17_BALANCEOF,
                hash160(new Hash160("ec2b32ed87e3747e826a0abd7229cb553220fd7a")));

        assertThat(balance, is(BigInteger.valueOf(3)));
    }

    @Test
    public void callFunctionReturningNonInt() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_returnTrue.json",
                NEO_SCRIPT_HASH.toString(), NEP17_TRANSFER);
        SmartContract sc = neoContract;

        UnexpectedReturnTypeException thrown = assertThrows(UnexpectedReturnTypeException.class,
                () -> sc.callFunctionReturningInt(NEP17_TRANSFER));
        assertThat(thrown.getMessage(), containsString(format("but expected %s.", StackItemType.INTEGER.jsonValue())));
    }

    @Test
    public void callFunctionReturningBool() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_returnFalse.json", NEO_SCRIPT_HASH.toString(),
                NEP17_TRANSFER);
        SmartContract sc = neoContract;
        boolean transferSuccessful = sc.callFunctionReturningBool(NEP17_TRANSFER);

        assertFalse(transferSuccessful);
    }

    @Test
    public void callFunctionReturningBool_withParameter() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_returnTrue.json",
                NEO_SCRIPT_HASH.toString(), NEP17_TRANSFER);
        SmartContract sc = neoContract;
        boolean transferSuccessful = sc.callFunctionReturningBool(NEP17_TRANSFER,
                hash160(new Hash160("ec2b32ed87e3747e826a0abd7229cb553220fd7a")));

        assertTrue(transferSuccessful);
    }

    @Test
    public void callFunctionReturningBool_asInteger_zero() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_returnIntZero.json",
                NEO_SCRIPT_HASH.toString(), "getZero");
        SmartContract sc = neoContract;
        boolean b = sc.callFunctionReturningBool("getZero");

        assertFalse(b);
    }

    @Test
    public void callFunctionReturningBool_asInteger_one() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_returnIntOne.json",
                NEO_SCRIPT_HASH.toString(), "getOne");
        SmartContract sc = neoContract;
        boolean b = sc.callFunctionReturningBool("getOne");

        assertTrue(b);
    }

    @Test
    public void callFunctionReturningNonBool() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_getcandidates.json",
                NEO_SCRIPT_HASH.toString(), "getCandidates");
        SmartContract sc = neoContract;

        UnexpectedReturnTypeException thrown = assertThrows(UnexpectedReturnTypeException.class,
                () -> sc.callFunctionReturningBool("getCandidates"));
        assertThat(thrown.getMessage(), containsString(format("but expected %s.", StackItemType.BOOLEAN.jsonValue())));
    }

    @Test
    public void testCallFunctionReturningScriptHash() throws IOException {
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");
        Hash160 scriptHash = someContract.callFunctionReturningScriptHash("ownerOf");

        assertThat(scriptHash, is(new Hash160("69ecca587293047be4c59159bf8bc399985c160d")));
    }

    @Test
    public void testCallFunctionReturningIterator_traverseWithFunction() throws IOException {
        setUpWireMockForInvokeFunction("tokensOf", "invokefunction_iterator_session.json");
        setUpWireMockForCall("traverseiterator", "nft_tokensof_traverseiterator.json");

        Function<StackItem, String> function = StackItem::getString;
        Iterator iterator = someContract.callFunctionReturningIterator(function, "tokensOf");
        assertThat(iterator.getIteratorId(), is("190d19ca-e935-4ad0-95c9-93b8cf6d115c"));
        assertThat(iterator.getSessionId(), is("a7b35b13-bdfc-4ab3-a398-88a9db9da4fe"));
        assertThat(iterator.getMapper(), is(function));

        List<String> traverse = iterator.traverse(100);
        assertThat(traverse.get(0), is("tokenof1"));
        assertThat(traverse.get(1), is("tokenof2"));
    }

    @Test
    public void testCallFunctionAndTraverseIterator() throws IOException {
        setUpWireMockForInvokeFunction("iterateTokens", "invokefunction_iterator_session.json");
        setUpWireMockForCall("traverseiterator", "traverseiterator.json");
        setUpWireMockForCall("terminatesession", "terminatesession.json");

        List<StackItem> tokens = someContract.callFunctionAndTraverseIterator("iterateTokens");
        assertThat(tokens, hasSize(2));
        List<StackItem> token1 = tokens.get(0).getList();
        assertThat(token1.get(0).getString(), is("neow#1"));
        assertThat(token1.get(1).getString(), is("besttoken"));
        List<StackItem> token2 = tokens.get(1).getList();
        assertThat(token2.get(0).getString(), is("neow#2"));
        assertThat(token2.get(1).getString(), is("almostbesttoken"));
    }

    @Test
    public void testCallFunctionReturningAndTraverseIterator() throws IOException {
        setUpWireMockForInvokeFunction("tokens", "invokefunction_iterator_session.json");
        setUpWireMockForCall("traverseiterator", "traverseiterator.json");
        setUpWireMockForCall("terminatesession", "terminatesession.json");

        List<StackItem> tokens = someContract.callFunctionAndTraverseIterator("tokens");
        List<StackItem> token1 = tokens.get(0).getList();
        assertThat(token1.get(0).getString(), is("neow#1"));
        assertThat(token1.get(1).getString(), is("besttoken"));
        List<StackItem> token2 = tokens.get(1).getList();
        assertThat(token2.get(0).getString(), is("neow#2"));
        assertThat(token2.get(1).getString(), is("almostbesttoken"));
    }

    @Test
    public void testCallFunctionReturningAndTraversingIterator_withFunction() throws IOException {
        setUpWireMockForInvokeFunction("tokens", "invokefunction_iterator_session.json");
        setUpWireMockForCall("traverseiterator", "traverseiterator.json");
        setUpWireMockForCall("terminatesession", "terminatesession.json");

        // Get only the token name for each item
        Function<StackItem, String> function = i -> i.getList().get(1).getString();

        List<String> traverse = someContract.callFunctionAndTraverseIterator(function, "tokens");
        assertThat(traverse.get(0), is("besttoken"));
        assertThat(traverse.get(1), is("almostbesttoken"));
    }

    @Test
    public void testCallFunctionReturningIterator() throws IOException {
        setUpWireMockForInvokeFunction("tokensOf", "invokefunction_iterator_session.json");
        Iterator iterator = someContract.callFunctionReturningIterator("tokensOf");

        assertThat(iterator.getIteratorId(), is("190d19ca-e935-4ad0-95c9-93b8cf6d115c"));
        assertThat(iterator.getSessionId(), is("a7b35b13-bdfc-4ab3-a398-88a9db9da4fe"));
    }

    @Test
    public void testCallFunctionReturningIteratorOtherReturnType() throws IOException {
        setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");

        UnexpectedReturnTypeException thrown = assertThrows(UnexpectedReturnTypeException.class,
                () -> neoContract.callFunctionReturningIterator("symbol"));
        assertThat(thrown.getMessage(),
                containsString(format("but expected %s.", StackItemType.INTEROP_INTERFACE.jsonValue())));
    }

    @Test
    public void testCallFunctionReturningIterator_sessionsDisabled() throws IOException {
        setUpWireMockForInvokeFunction("tokensOf", "invokefunction_iterator_sessionDisabled.json");

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> someContract.callFunctionReturningIterator("tokensOf"));
        assertThat(thrown.getMessage(),
                is("No session id was found. The connected Neo node might not support sessions."));
    }

    @Test
    public void invokingFunctionPerformsCorrectCall() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_3.json", NEO_SCRIPT_HASH.toString(),
                NEP17_BALANCEOF, account1.getScriptHash().toString());

        SmartContract sc = neoContract;
        NeoInvokeFunction response = sc.callInvokeFunction(NEP17_BALANCEOF,
                asList(hash160(account1.getScriptHash())));

        assertThat(response.getInvocationResult().getStack().get(0).getInteger(),
                is(BigInteger.valueOf(3)));
    }

    @Test
    public void invokingFunctionPerformsCorrectCall_WithoutParameters() throws IOException {
        setUpWireMockForCall("invokefunction",
                "invokefunction_symbol_neo.json",
                NEO_SCRIPT_HASH.toString(),
                "symbol");
        NeoInvokeFunction i = neoContract.callInvokeFunction("symbol");

        assertThat(i.getResult().getStack().get(0).getString(), Matchers.is("NEO"));
    }

    @Test
    public void testCallFunctionAndUnwrapIterator() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_ownerOf_array.json");
        List<StackItem> iteratorArray = someContract.callFunctionAndUnwrapIterator("ownerOf", asList(), 20);

        assertThat(iteratorArray, hasSize(2));
        assertThat(iteratorArray.get(0).getAddress(), Matchers.is("NSdNMyrz7Bp8MXab41nTuz1mRCnsFr5Rsv"));
        assertThat(iteratorArray.get(1).getAddress(), Matchers.is("NhxK1PEmijLVD6D4WSuPoUYJVk855L21ru"));
    }

    @Test
    public void callInvokeFunction_missingFunction() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> neoContract.callInvokeFunction("",
                        asList(hash160(account1.getScriptHash()))));
        assertThat(thrown.getMessage(), is("The invocation function must not be null or empty."));
    }

    @Test
    public void callInvokeFunctionWithoutParameters_missingFunction() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> neoContract.callInvokeFunction(""));
        assertThat(thrown.getMessage(), is("The invocation function must not be null or empty."));
    }

}
