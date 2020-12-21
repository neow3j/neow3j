package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class NeoTokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private Account account1;
    private static final ScriptHash NEO_TOKEN_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final String VOTE = NeoToken.VOTE;
    private static final String REGISTER_CANDIDATE = NeoToken.REGISTER_CANDIDATE;
    private static final String UNREGISTER_CANDIDATE = NeoToken.UNREGISTER_CANDIDATE;
    private Neow3j neow;

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);
        neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));

        account1 = new Account(ECKeyPair.create(Numeric.hexStringToByteArray(
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
        String responseBody = ContractTestHelper.loadFile(
                "/responses/invokefunction_unclaimedgas.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"0a46e2e37c9987f570b4af253fb77e7eef0f72b6\"" // neo contract
                        + ".*\"unclaimedGas\"" // function
                        + ".*\"f68f181731a47036a99f04dad90043a744edec0f\"" // script hash
                        + ".*100.*" // block height
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        BigInteger result = new NeoToken(neow)
                .unclaimedGas(ScriptHash.fromAddress("NMNB9beANndYi5bd8Cd3U35EMvzmWMDSy9"), 100);
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
        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH,
                REGISTER_CANDIDATE, Arrays.asList(ContractParameter.publicKey(pubKeyBytes)))
                .toArray();

        Wallet w = Wallet.withAccounts(account1);
        TransactionBuilder b = new NeoToken(neow).registerCandidate(account1.getECKeyPair().getPublicKey())
                .wallet(w)
                .signers(Signer.global(account1.getScriptHash()));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.GLOBAL));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void unregisterCandidate() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_unregistercandidate.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] pubKeyBytes = account1.getECKeyPair().getPublicKey().getEncoded(true);
        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH,
                UNREGISTER_CANDIDATE, Arrays.asList(ContractParameter.publicKey(pubKeyBytes)))
                .toArray();

        Wallet w = Wallet.withAccounts(account1);
        TransactionBuilder b = new NeoToken(neow).unregisterCandidate(account1.getECKeyPair().getPublicKey())
                .wallet(w)
                .signers(Signer.global(account1.getScriptHash()));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.GLOBAL));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void getCandidates() throws IOException {
        String responseBody = ContractTestHelper.loadFile(
                "/responses/invokefunction_getcandidates.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"0a46e2e37c9987f570b4af253fb77e7eef0f72b6\"" // neo contract
                        + ".*\"getCandidates\".*" // function
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        Map<ECPublicKey, Integer> result = new NeoToken(neow).getCandidates();
        assertThat(result.keySet(), contains(
                new ECPublicKey(Numeric.hexStringToByteArray(
                        "02200284598c6c1117f163dd938a4c8014cf2cf1164c4b7197f347109db50eae7c")),
                new ECPublicKey(Numeric.hexStringToByteArray(
                        "02c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238"))
        ));
        assertThat(result.values(), contains(100, 49999900));
    }

    @Test
    public void getValidators() throws IOException {
        String responseBody = ContractTestHelper.loadFile(
                "/responses/invokefunction_getvalidators.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"0a46e2e37c9987f570b4af253fb77e7eef0f72b6\"" // neo contract
                        + ".*\"getValidators\".*" // function
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        List<ECPublicKey> result = new NeoToken(neow).getValidators();
        String expKeyHex = "02c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238";
        ECPublicKey expKey = new ECPublicKey(Numeric.hexStringToByteArray(expKeyHex));
        assertThat(result, contains(expKey));
    }

    @Test
    public void getCommittee() throws IOException {
        String responseBody = ContractTestHelper.loadFile(
                "/responses/invokefunction_getcommittee.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"0a46e2e37c9987f570b4af253fb77e7eef0f72b6\"" // neo contract
                        + ".*\"getCommittee\".*" // function
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        List<ECPublicKey> result = new NeoToken(neow).getCommittee();
        String expKeyHex = "026aa8fe6b4360a67a530e23c08c6a72525afde34719c5436f9d3ced759f939a3d";
        ECPublicKey expKey = new ECPublicKey(Numeric.hexStringToByteArray(expKeyHex));
        assertThat(result, contains(expKey));
    }

    @Test
    public void getNextBlockValidators() throws IOException {
        String responseBody = ContractTestHelper.loadFile("/responses"
                + "/invokefunction_getnextblockvalidators.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"0a46e2e37c9987f570b4af253fb77e7eef0f72b6\"" // neo contract
                        + ".*\"getNextBlockValidators\".*" // function
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        List<ECPublicKey> result = new NeoToken(neow).getNextBlockValidators();
        String expKeyHex = "02c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238";
        ECPublicKey expKey = new ECPublicKey(Numeric.hexStringToByteArray(expKeyHex));
        assertThat(result, contains(expKey));
    }

    @Test
    public void voteProducesCorrectScriptAnsSigner() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_vote.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] pubKeyBytes1 = Numeric.hexStringToByteArray(
                "02200284598c6c1117f163dd938a4c8014cf2cf1164c4b7197f347109db50eae7c");
        byte[] pubKeyBytes2 = Numeric.hexStringToByteArray(
                "02c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238");
        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH, VOTE,
                Arrays.asList(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.publicKey(pubKeyBytes1),
                        ContractParameter.publicKey((pubKeyBytes2))))
                .toArray();

        Wallet w = Wallet.withAccounts(account1);
        TransactionBuilder b = new NeoToken(neow).vote(account1.getScriptHash(),
                new ECPublicKey(pubKeyBytes1), new ECPublicKey(pubKeyBytes2))
                .signers(Signer.global(account1.getScriptHash()));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.GLOBAL));
        assertThat(b.getScript(), is(expectedScript));

        b = new NeoToken(neow).vote(account1, new ECPublicKey(pubKeyBytes1), new ECPublicKey(pubKeyBytes2))
                .signers(Signer.global(account1.getScriptHash()));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.GLOBAL));
        assertThat(b.getScript(), is(expectedScript));
    }
}
