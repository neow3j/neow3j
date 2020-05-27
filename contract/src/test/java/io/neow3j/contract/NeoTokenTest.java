package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.model.NeoConfig;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Cosigner;
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
                        + ".*\"9bde8f209c88dd0e7ca3bf0af0f476cdd8207789\"" // neo contract
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
        ContractTestHelper.setUpWireMockForCall("invokefunction",
                "invokefunction_registerCandidate.json",
                "9bde8f209c88dd0e7ca3bf0af0f476cdd8207789", "registerCandidate");
        ContractTestHelper.setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        NeoConfig.setMagicNumber(new byte[]{0x01, 0x03, 0x00, 0x0}); // Magic number 769

        byte[] privateKey = Numeric.hexStringToByteArray(
                "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9");
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        Account a = Account.fromECKeyPair(keyPair).isDefault().build();
        Wallet w = new Wallet.Builder().accounts(a).build();
        Invocation inv = new NeoToken(neow).buildRegisterInvocation(
                a.getScriptHash(), w, keyPair.getPublicKey());
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().toAddress(), is("Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ"));
        assertThat(tx.getSystemFee(), is(6007570L));
        assertThat(tx.getNetworkFee(), is(1262390L));
        assertThat(tx.getCosigners(), contains(Cosigner.global(a.getScriptHash())));
        byte[] script = Numeric.hexStringToByteArray(
                "0c2102200284598c6c1117f163dd938a4c8014cf2cf1164c4b7197f347109db50eae7c11c00c11726567697374657243616e6469646174650c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52");
        assertThat(tx.getScript(), is(script));
        byte[] verifScript = Numeric.hexStringToByteArray(
                "0c2102200284598c6c1117f163dd938a4c8014cf2cf1164c4b7197f347109db50eae7c0b418a6b1e75");
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(verifScript));
    }

    @Test
    public void getValidators() throws IOException {
        String responseBody = ContractTestHelper.loadFile(
                "/responses/invokefunction_getValidators.json");
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":"
                        + ".*\"9bde8f209c88dd0e7ca3bf0af0f476cdd8207789\"" // neo contract
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
                        + ".*\"9bde8f209c88dd0e7ca3bf0af0f476cdd8207789\"" // neo contract
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
                        + ".*\"9bde8f209c88dd0e7ca3bf0af0f476cdd8207789\"" // neo contract
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
        ContractTestHelper.setUpWireMockForCall("invokefunction", "invokefunction_vote.json",
                "9bde8f209c88dd0e7ca3bf0af0f476cdd8207789", "vote");
        ContractTestHelper.setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        NeoConfig.setMagicNumber(new byte[]{0x01, 0x03, 0x00, 0x0}); // Magic number 769

        byte[] privateKey = Numeric.hexStringToByteArray(
                "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9");
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        Account a = Account.fromECKeyPair(keyPair).isDefault().build();
        Wallet w = new Wallet.Builder().accounts(a).build();
        ECPublicKey validator1 = a.getECKeyPair().getPublicKey();
        ECPublicKey validator2 = new ECPublicKey(Numeric.hexStringToByteArray(
                "02c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238"));

        Invocation inv = new NeoToken(neow).buildVoteInvocation(
                a.getScriptHash(), w, validator1, validator2);
        Transaction tx = inv.getTransaction();
        assertThat(tx.getSender().toAddress(), is("Aa1rZbE1k8fXTwzaxxsPRtJYPwhDQjWRFZ"));
        assertThat(tx.getSystemFee(), is(501007930L));
        assertThat(tx.getNetworkFee(), is(1306390L));
        assertThat(tx.getCosigners(), contains(Cosigner.global(a.getScriptHash())));
        byte[] script = Numeric.hexStringToByteArray(
                "0c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380c2102200284598c6c1117f163dd938a4c8014cf2cf1164c4b7197f347109db50eae7c0c14c8172ea3b405bf8bfc57c33a8410116b843e13df13c00c04766f74650c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52");
        assertThat(tx.getScript(), is(script));
        byte[] verifScript = Numeric.hexStringToByteArray(
                "0c2102200284598c6c1117f163dd938a4c8014cf2cf1164c4b7197f347109db50eae7c0b418a6b1e75");
        assertThat(tx.getWitnesses().get(0).getVerificationScript().getScript(), is(verifScript));
    }
}