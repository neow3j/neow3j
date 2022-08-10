package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoAccountState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.test.WireMockTestHelper.loadFile;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.AccountSigner.global;
import static io.neow3j.types.ContractParameter.any;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NeoTokenTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private Account account1;
    private static final String NEOTOKEN_SCRIPTHASH = "ef4073a0f2b305a38ec4050e4d3d28bc40ea63f5";
    private static final String VOTE = "vote";
    private static final String REGISTER_CANDIDATE = "registerCandidate";
    private static final String UNREGISTER_CANDIDATE = "unregisterCandidate";
    private static final String GET_GAS_PER_BLOCK = "getGasPerBlock";
    private static final String SET_GAS_PER_BLOCK = "setGasPerBlock";
    private static final String GET_REGISTER_PRICE = "getRegisterPrice";
    private static final String SET_REGISTER_PRICE = "setRegisterPrice";
    private static final String GET_ACCOUNT_STATE = "getAccountState";

    private Neow3j neow;

    @BeforeAll
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);
        neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        account1 = new Account(ECKeyPair.create(hexStringToByteArray(
                "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3")));
    }

    @Test
    public void getName() {
        assertThat(new NeoToken(neow).getName(), is("NeoToken"));
    }

    @Test
    public void getSymbol() {
        assertThat(new NeoToken(neow).getSymbol(), is("NEO"));
    }

    @Test
    public void getTotalSupply() {
        assertThat(new NeoToken(neow).getTotalSupply(), is(new BigInteger("100000000")));
    }

    @Test
    public void getDecimals() {
        assertThat(new NeoToken(neow).getDecimals(), is(0));
    }

    @Test
    public void getUnclaimedGas() throws IOException {
        String responseBody = loadFile(
                "/responses/invokefunction_unclaimedgas.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"" + NEOTOKEN_SCRIPTHASH + "\""
                        + ".*\"unclaimedGas\"" // function
                        + ".*\"f68f181731a47036a99f04dad90043a744edec0f\""
                        // script hash
                        + ".*100.*" // block height
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        BigInteger result = new NeoToken(neow)
                .unclaimedGas(Hash160.fromAddress("NMNB9beANndYi5bd8Cd3U35EMvzmWMDSy9"), 100);
        assertThat(result, is(new BigInteger("60000000000")));

        result = new NeoToken(neow)
                .unclaimedGas(Account.fromAddress("NMNB9beANndYi5bd8Cd3U35EMvzmWMDSy9"), 100);
        assertThat(result, is(new BigInteger("60000000000")));
    }

    @Test
    public void registerCandidate() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_registercandidate.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] pubKeyBytes = account1.getECKeyPair().getPublicKey().getEncoded(true);
        byte[] expectedScript = new ScriptBuilder().contractCall(NeoToken.SCRIPT_HASH,
                        REGISTER_CANDIDATE, singletonList(publicKey(pubKeyBytes)))
                .toArray();

        TransactionBuilder b = new NeoToken(neow)
                .registerCandidate(account1.getECKeyPair().getPublicKey())
                .signers(global(account1));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.GLOBAL));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void unregisterCandidate() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_unregistercandidate.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] pubKeyBytes = account1.getECKeyPair().getPublicKey().getEncoded(true);
        byte[] expectedScript = new ScriptBuilder().contractCall(NeoToken.SCRIPT_HASH,
                        UNREGISTER_CANDIDATE, asList(publicKey(pubKeyBytes)))
                .toArray();

        TransactionBuilder b = new NeoToken(neow)
                .unregisterCandidate(account1.getECKeyPair().getPublicKey())
                .signers(global(account1));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.GLOBAL));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void getCandidates() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_getcandidates.json",
                NEOTOKEN_SCRIPTHASH, "getCandidates");

        List<NeoToken.Candidate> result = new NeoToken(neow).getCandidates();
        assertThat(result.size(), is(2));
        result.forEach(c -> {
            assertThat(c.getPublicKey(), notNullValue());
            assertThat(c.getVotes(), is(BigInteger.ZERO));
        });
    }

    @Test
    public void getCommittee() throws IOException {
        String responseBody = loadFile(
                "/responses/invokefunction_getcommittee.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"" + NEOTOKEN_SCRIPTHASH + "\""
                        + ".*\"getCommittee\".*" // function
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        List<ECPublicKey> result = new NeoToken(neow).getCommittee();
        String expKeyHex = "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
        ECPublicKey expKey = new ECPublicKey(hexStringToByteArray(expKeyHex));

        assertThat(result, contains(expKey));
    }

    @Test
    public void getNextBlockValidators() throws IOException {
        String responseBody = loadFile("/responses/invokefunction_getnextblockvalidators.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"" + NEOTOKEN_SCRIPTHASH + "\""
                        + ".*\"getNextBlockValidators\".*" // function
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        List<ECPublicKey> result = new NeoToken(neow).getNextBlockValidators();
        String expKeyHex = "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
        ECPublicKey expKey = new ECPublicKey(hexStringToByteArray(expKeyHex));

        assertThat(result, contains(expKey));
    }

    @Test
    public void voteWithAccountProducesCorrectScript() throws IOException {
        setUpWireMockForInvokeFunction("getCandidates", "invokefunction_getcandidates.json");
        setUpWireMockForCall("invokescript", "invokescript_vote.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] pubKey = account1.getECKeyPair().getPublicKey().getEncoded(true);
        byte[] expectedScript = new ScriptBuilder().contractCall(NeoToken.SCRIPT_HASH, VOTE,
                        asList(
                                hash160(account1.getScriptHash()),
                                publicKey(pubKey)))
                .toArray();

        TransactionBuilder b = new NeoToken(neow)
                .vote(account1, new ECPublicKey(pubKey))
                .signers(global(account1));

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void voteWitScriptHashProducesCorrectScript() throws IOException {
        setUpWireMockForInvokeFunction("getCandidates", "invokefunction_getcandidates.json");
        setUpWireMockForCall("invokescript", "invokescript_vote.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] pubKey = account1.getECKeyPair().getPublicKey().getEncoded(true);
        byte[] expectedScript = new ScriptBuilder().contractCall(NeoToken.SCRIPT_HASH, VOTE,
                        asList(
                                hash160(account1.getScriptHash()),
                                publicKey(pubKey)))
                .toArray();

        TransactionBuilder b = new NeoToken(neow)
                .vote(account1.getScriptHash(), new ECPublicKey(pubKey))
                .signers(global(account1));

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void cancelVoteWithAccountProducesCorrectScript() throws IOException {
        setUpWireMockForInvokeFunction("getCandidates", "invokefunction_getcandidates.json");
        setUpWireMockForCall("invokescript", "invokescript_vote.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NeoToken.SCRIPT_HASH, VOTE,
                asList(hash160(account1.getScriptHash()), any(null))).toArray();

        TransactionBuilder b = new NeoToken(neow)
                .cancelVote(account1)
                .signers(global(account1));

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void buildVoteScript() {
        ECPublicKey pubKey = account1.getECKeyPair().getPublicKey();
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NeoToken.SCRIPT_HASH, VOTE,
                        asList(hash160(account1.getScriptHash()),
                                publicKey(pubKey.getEncoded(true))))
                .toArray();
        byte[] script = new NeoToken(neow).buildVoteScript(account1.getScriptHash(), pubKey);

        assertThat(script, is(expectedScript));
    }

    @Test
    public void buildCancelVoteScript() {
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NeoToken.SCRIPT_HASH, VOTE,
                        asList(hash160(account1.getScriptHash()), any(null)))
                .toArray();
        byte[] script = new NeoToken(neow).buildVoteScript(account1.getScriptHash(), null);

        assertThat(script, is(expectedScript));
    }

    @Test
    public void getGasPerBlockInvokesCorrectFunctionAndHandlesReturnValueCorrectly()
            throws IOException {

        setUpWireMockForInvokeFunction(GET_GAS_PER_BLOCK, "invokefunction_getGasPerBlock.json");
        int res = new NeoToken(neow).getGasPerBlock().intValue();

        assertThat(res, is(500_000));
    }

    @Test
    public void setGasPerBlockProducesCorrectScript() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_vote.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        BigInteger gasPerBlock = new BigInteger("10000");
        TransactionBuilder txBuilder = new NeoToken(neow)
                .setGasPerBlock(gasPerBlock)
                .signers(calledByEntry(account1));

        byte[] expectedScript = new ScriptBuilder().contractCall(NeoToken.SCRIPT_HASH,
                        SET_GAS_PER_BLOCK, asList(integer(gasPerBlock)))
                .toArray();

        assertThat(txBuilder.getScript(), is(expectedScript));
    }

    @Test
    public void getRegisterPriceInvokesCorrectFunctionAndHandlesReturnValueCorrectly()
            throws IOException {

        setUpWireMockForInvokeFunction(GET_REGISTER_PRICE, "invokefunction_getRegisterPrice.json");
        BigInteger res = new NeoToken(neow).getRegisterPrice();

        assertThat(res, is(new BigInteger("100000000000")));
    }

    @Test
    public void setRegisterPriceProducesCorrectScript()
            throws IOException {

        setUpWireMockForCall("invokescript", "invokescript_vote.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        BigInteger registerPrice = new BigInteger("50000000000");
        TransactionBuilder txBuilder = new NeoToken(neow)
                .setRegisterPrice(registerPrice)
                .signers(calledByEntry(account1));

        byte[] expectedScript = new ScriptBuilder().contractCall(NeoToken.SCRIPT_HASH,
                        SET_REGISTER_PRICE, singletonList(integer(registerPrice)))
                .toArray();

        assertThat(txBuilder.getScript(), is(expectedScript));
    }

    @Test
    public void scriptHash() {
        assertThat(new NeoToken(neow).getScriptHash().toString(), is(NEOTOKEN_SCRIPTHASH));
    }

    @Test
    public void testGetAccountState() throws IOException {
        setUpWireMockForInvokeFunction(GET_ACCOUNT_STATE, "neoToken_getAccountState.json");
        NeoAccountState neoAccountState =
                new NeoToken(neow).getAccountState(account1.getScriptHash());

        assertThat(neoAccountState.getBalance(), is(BigInteger.valueOf(20000)));
        assertThat(neoAccountState.getBalanceHeight(), is(BigInteger.valueOf(259)));

        ECPublicKey publicKey =
                new ECPublicKey(
                        "037279f3a507817251534181116cb38ef30468b25074827db34cbbc6adc8873932");

        assertThat(neoAccountState.getPublicKey(), is(publicKey));
    }

    @Test
    public void testGetAccountState_noVote() throws IOException {
        setUpWireMockForInvokeFunction(GET_ACCOUNT_STATE, "neoToken_getAccountState_noVote.json");
        NeoAccountState neoAccountState =
                new NeoToken(neow).getAccountState(account1.getScriptHash());

        assertThat(neoAccountState.getBalance(), is(BigInteger.valueOf(12000)));
        assertThat(neoAccountState.getBalanceHeight(), is(BigInteger.valueOf(820)));
        assertNull(neoAccountState.getPublicKey());
    }

    @Test
    public void testGetAccountState_noBalance() throws IOException {
        setUpWireMockForInvokeFunction(GET_ACCOUNT_STATE,
                "neoToken_getAccountState_noBalance.json");
        NeoAccountState neoAccountState =
                new NeoToken(neow).getAccountState(account1.getScriptHash());

        assertThat(neoAccountState.getBalance(), is(BigInteger.ZERO));
        assertNull(neoAccountState.getBalanceHeight());
        assertNull(neoAccountState.getPublicKey());
    }

    @Test
    public void isCandidate() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_getcandidates.json",
                NEOTOKEN_SCRIPTHASH, "getCandidates");

        ECPublicKey pubKey = new ECPublicKey(
                "02c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238");

        assertTrue(new NeoToken(neow).isCandidate(pubKey));
    }

    @Test
    public void getAllCandidatesIterator() throws IOException {
        setUpWireMockForInvokeFunction("getAllCandidates", "invokefunction_iterator_session.json");
        setUpWireMockForCall("traverseiterator", "neo_getAllCandidates_traverseiterator.json");
        setUpWireMockForCall("terminatesession", "terminatesession.json");

        Iterator<NeoToken.Candidate> it = new NeoToken(neow).getAllCandidatesIterator();
        assertThat(it.getMapper(), is(NeoToken.candidateMapper()));

        List<NeoToken.Candidate> candidates = it.traverse(2);
        ECPublicKey pubKey1 = new ECPublicKey("02607a38b8010a8f401c25dd01df1b74af1827dd16b821fc07451f2ef7f02da60f");
        BigInteger votes1 = BigInteger.valueOf(340356);
        ECPublicKey pubKey2 = new ECPublicKey("037279f3a507817251534181116cb38ef30468b25074827db34cbbc6adc8873932");
        BigInteger votes2 = BigInteger.valueOf(10000000);
        assertThat(candidates.get(0).getPublicKey(), is(pubKey1));
        assertThat(candidates.get(0).getVotes(), is(votes1));
        assertThat(candidates.get(1).getPublicKey(), is(pubKey2));
        assertThat(candidates.get(1).getVotes(), is(votes2));

        it.terminateSession();
    }

    @Test
    public void getCandidateVotes() throws IOException {
        setUpWireMockForInvokeFunction("getCandidateVote", "invokefunction_getCandidateVote.json");
        BigInteger votes = new NeoToken(neow).getCandidateVotes(Account.create().getECKeyPair().getPublicKey());
        assertThat(votes, is(BigInteger.valueOf(721978)));
    }

}
