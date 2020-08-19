package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import io.neow3j.constants.InteropServiceCode;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import io.neow3j.transaction.WitnessScope;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Transaction;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class NeoTokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow;

    @Before
    public void setUp() {
        // Configure WireMock to use default host and port "localhost:8080".
        WireMock.configure();
        neow = Neow3j.build(new HttpService("http://localhost:8080"));
    }

    @Test
    public void getName() {
        assertThat(new NeoToken(neow).getName(), is("NEO"));
    }

    @Test
    public void getSymbol() {
        assertThat(new NeoToken(neow).getSymbol(), is("neo"));
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
                        + ".*\"de5f57d430d3dece511cf975a8d37848cb9e0525\"" // neo contract
                        + ".*\"unclaimedGas\"" // function
                        + ".*\"f68f181731a47036a99f04dad90043a744edec0f\"" // script hash
                        + ".*100.*" // block height
                ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        BigInteger result = new NeoToken(neow)
                .getUnclaimedGas(ScriptHash.fromAddress("AHE5cLhX5NjGB5R2PcdUvGudUoGUBDeHX4"), 100);
        assertThat(result, is(new BigInteger("60000000000")));
    }

    @Test
    public void registerCandidate() throws IOException {
        byte[] expectedScript = new ScriptBuilder()
                .pushData(Numeric.hexStringToByteArray("02200284598c6c1117f163dd938a4c8014cf2cf1164c4b7197f347109db50eae7c"))
                .pushInteger(1)
                .pack()
                .pushData(Numeric.hexStringToByteArray("726567697374657243616e646964617465"))
                .pushData(Numeric.hexStringToByteArray("25059ecb4878d3a875f91c51ceded330d4575fde"))
                .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CALL)
                .toArray();
        setUpWireMockForCall("invokescript", "invokescript_registercandidate.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] privateKey = Numeric.hexStringToByteArray(
                "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9");
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        Account a = new Account(keyPair);
        Wallet w = Wallet.withAccounts(a);
        Invocation inv = new NeoToken(neow).buildRegisterInvocation(
                a.getScriptHash(), w, keyPair.getPublicKey());
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().getScriptHash().toAddress(), is("ANzk4JsM7PY1QTZrVSTfzeDU3E9pWqajEb"));
        assertThat(tx.getSystemFee(), is(6007570L));
        assertThat(tx.getNetworkFee(), is(1240390L));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(a.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.GLOBAL));
        assertThat(tx.getScript(), is(expectedScript));

        byte[] verifScript = ScriptBuilder.buildVerificationScript(a.getECKeyPair().getPublicKey().getEncoded(true));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(verifScript));
    }

    @Test
    public void getValidators() throws IOException {
        String responseBody = ContractTestHelper.loadFile(
                "/responses/invokefunction_getvalidators.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"de5f57d430d3dece511cf975a8d37848cb9e0525\"" // neo contract
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
    public void getCandidates() throws IOException {
        String responseBody = ContractTestHelper.loadFile(
                "/responses/invokefunction_getcandidates.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"de5f57d430d3dece511cf975a8d37848cb9e0525\"" // neo contract
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
    public void getNextBlockValidators() throws IOException {
        String responseBody = ContractTestHelper.loadFile("/responses"
                + "/invokefunction_getnextblockvalidators.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"de5f57d430d3dece511cf975a8d37848cb9e0525\"" // neo contract
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
    public void vote() throws IOException {
        byte[] expectedScript = new ScriptBuilder()
                .pushData(Numeric.hexStringToByteArray("02c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238"))
                .pushData(Numeric.hexStringToByteArray("02200284598c6c1117f163dd938a4c8014cf2cf1164c4b7197f347109db50eae7c"))
                .pushData(Numeric.hexStringToByteArray("4f37f3deae488c13b671ea6489d07b15a4396310"))
                .pushInteger(3)
                .pack()
                .pushData(Numeric.hexStringToByteArray("766f7465"))
                .pushData(Numeric.hexStringToByteArray("25059ecb4878d3a875f91c51ceded330d4575fde"))
                .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CALL)
                .toArray();
        setUpWireMockForCall("invokescript", "invokescript_vote.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] privateKey = Numeric.hexStringToByteArray(
                "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9");
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        Account a = new Account(keyPair);
        Wallet w = Wallet.withAccounts(a);
        ECPublicKey validator1 = a.getECKeyPair().getPublicKey();
        ECPublicKey validator2 = new ECPublicKey(Numeric.hexStringToByteArray(
                "02c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238"));

        Invocation inv = new NeoToken(neow).buildVoteInvocation(
                a.getScriptHash(), w, validator1, validator2);
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().getScriptHash().toAddress(), is("ANzk4JsM7PY1QTZrVSTfzeDU3E9pWqajEb"));
        assertThat(tx.getSystemFee(), is(501007930L));
        assertThat(tx.getNetworkFee(), is(1284390L));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(a.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.GLOBAL));
        System.out.println(Numeric.toHexStringNoPrefix(tx.getScript()));
        assertThat(tx.getScript(), is(expectedScript));
        byte[] verifScript = ScriptBuilder.buildVerificationScript(a.getECKeyPair().getPublicKey().getEncoded(true));
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(verifScript));
    }
}