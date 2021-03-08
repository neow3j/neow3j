package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static io.neow3j.contract.ContractTestHelper.loadFile;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NeoTokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Account account1;
    private static final String NEOTOKEN_SCRIPTHASH = "f61eebf573ea36593fd43aa150c055ad7906ab83";
    private static final String VOTE = NeoToken.VOTE;
    private static final String REGISTER_CANDIDATE = NeoToken.REGISTER_CANDIDATE;
    private static final String UNREGISTER_CANDIDATE = NeoToken.UNREGISTER_CANDIDATE;
    private static final String GET_GAS_PER_BLOCK = NeoToken.GET_GAS_PER_BLOCK;
    private static final String SET_GAS_PER_BLOCK = NeoToken.SET_GAS_PER_BLOCK;

    private Neow3j neow;

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockRule.port();
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

        Wallet w = Wallet.withAccounts(account1);
        TransactionBuilder b = new NeoToken(neow).registerCandidate(
                account1.getECKeyPair().getPublicKey())
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
        byte[] expectedScript = new ScriptBuilder().contractCall(NeoToken.SCRIPT_HASH,
                UNREGISTER_CANDIDATE, singletonList(publicKey(pubKeyBytes)))
                .toArray();

        Wallet w = Wallet.withAccounts(account1);
        TransactionBuilder b = new NeoToken(neow).unregisterCandidate(
                account1.getECKeyPair().getPublicKey())
                .wallet(w)
                .signers(Signer.global(account1.getScriptHash()));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.GLOBAL));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void getCandidates() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_getcandidates.json",
                NEOTOKEN_SCRIPTHASH, "getCandidates");

        Map<ECPublicKey, Integer> result = new NeoToken(neow).getCandidates();
        assertThat(result.size(), is(2));
        result.forEach((key, value) -> {
            assertThat(key, notNullValue());
            assertThat(value, is(0));
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
        ECPublicKey expKey = new ECPublicKey(Numeric.hexStringToByteArray(expKeyHex));
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
        ECPublicKey expKey = new ECPublicKey(Numeric.hexStringToByteArray(expKeyHex));
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
                .wallet(Wallet.withAccounts(account1))
                .signers(Signer.global(account1.getScriptHash()));

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

        Wallet w = Wallet.withAccounts(account1);
        TransactionBuilder b = new NeoToken(neow)
                .vote(account1.getScriptHash(), new ECPublicKey(pubKey))
                .wallet(w)
                .signers(Signer.global(account1.getScriptHash()));

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void voteForNonCandidateThrows() throws IOException {
        setUpWireMockForInvokeFunction("getCandidates", "invokefunction_getcandidates.json");
        NeoToken neoToken = new NeoToken(neow);
        Account nonCandidateAccount = Account.fromWIF(
                "KyHFg26DHTUWZtmUVTRqDHg8uVvZi9dr5zV3tQ22JZUjvWVCFvtw");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("provided public key is not a candidate");
        neoToken.vote(nonCandidateAccount.getScriptHash(),
                nonCandidateAccount.getECKeyPair().getPublicKey());
    }

    @Test
    public void getGasPerBlockInvokesCorrectFunctionAndHandlesReturnValueCorrectly()
            throws IOException {

        setUpWireMockForInvokeFunction(GET_GAS_PER_BLOCK, "invokefunction_getGasPerBlock.json");
        int res = new NeoToken(neow).getGasPerBlock().intValue();
        assertThat(res, is(500_000));
    }

    @Test
    public void setGasPerBlockProducesCorrectScript()
            throws IOException {

        setUpWireMockForCall("invokescript", "invokescript_vote.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        int gasPerBlock = 10000;
        Wallet w = Wallet.withAccounts(account1);
        TransactionBuilder txBuilder = new NeoToken(neow).setGasPerBlock(gasPerBlock)
                .wallet(w)
                .signers(Signer.calledByEntry(account1.getScriptHash()));

        byte[] expectedScript = new ScriptBuilder().contractCall(NeoToken.SCRIPT_HASH,
                SET_GAS_PER_BLOCK, singletonList(integer(gasPerBlock)))
                .toArray();

        assertThat(txBuilder.getScript(), is(expectedScript));
    }

    @Test
    public void scriptHash() {
        assertThat(new NeoToken(neow).getScriptHash().toString(), is(NEOTOKEN_SCRIPTHASH));
    }

}
