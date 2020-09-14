package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class NeoURITest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private static final String BEGIN_TX = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
    private static final String BEGIN_TX_ASSET_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo&amount=1";
    private static final String BEGIN_TX_ASSET_AMOUNT_MULTIPLE_ASSETS_AND_AMOUNTS =
            "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo&amount=1&asset=gas&amount=80";

    private static Neow3j neow3j;
    private static final Wallet WALLET = Wallet.createWallet();

    private static final String ADDRESS = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
    private static final ScriptHash ADDRESS_SCRIPT_HASH = ScriptHash.fromAddress(ADDRESS);
    private static final BigDecimal AMOUNT = new BigDecimal(1);
    private static final ScriptHash NEO_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final String NEO = NeoToken.SCRIPT_HASH.toString();
    private static final String NEO_ADDRESS = NeoToken.SCRIPT_HASH.toAddress();
    private static final String GAS = GasToken.SCRIPT_HASH.toString();
    private static final ScriptHash GAS_SCRIPT_HASH = GasToken.SCRIPT_HASH;
    private static final String GAS_ADDRESS = GasToken.SCRIPT_HASH.toAddress();

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);
        neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
    }

    @Test
    public void testGenerateFromURI() {
        URI uri = NeoURI.fromURI(BEGIN_TX_ASSET_AMOUNT).buildURI().getURI();

        assertThat(uri, is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateFromURI_null() {
        NeoURI.fromURI("").buildURI();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateFromURI_emptyString() {
        NeoURI.fromURI("").buildURI();
    }

    @Test
    public void testGenerateFromURI_MULTIPLE_ASSETS_AND_AMOUNTS() {
        URI uri = NeoURI.fromURI(BEGIN_TX_ASSET_AMOUNT_MULTIPLE_ASSETS_AND_AMOUNTS).buildURI().getURI();

        assertThat(uri, is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
    }

    @Test
    public void testGenerateFromURI_Getter() {
        NeoURI neoURI = NeoURI.fromURI(BEGIN_TX_ASSET_AMOUNT).buildURI();

        assertThat("getAddress()", neoURI.getAddress(), is(ADDRESS));
        assertThat("getAddressAsScriptHash()", neoURI.getAddressAsScriptHash(), is(ADDRESS_SCRIPT_HASH));
        assertThat("getAsset()", neoURI.getAsset(), isOneOf(NEO, "neo"));
        assertThat("getAssetAsScriptHash()", neoURI.getAssetAsScriptHash(), is(NEO_SCRIPT_HASH));
        assertThat("getAssetAsAddress()", neoURI.getAssetAsAddress(), is(NEO_ADDRESS));
        assertThat("getAmount()", neoURI.getAmount(), is(AMOUNT));
        assertThat("getAmountAsString()", neoURI.getAmountAsString(), is(AMOUNT.toString()));
    }

    @Test
    public void testGenerateFromURI_Getter_GAS() {
        String BEGIN_TX_ASSET_GAS = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=gas";
        NeoURI neoURI = NeoURI.fromURI(BEGIN_TX_ASSET_GAS).buildURI();

        assertThat("getAsset()", neoURI.getAsset(), isOneOf(GAS, "gas"));
        assertThat("getAssetAsScriptHash()", neoURI.getAssetAsScriptHash(), is(GAS_SCRIPT_HASH));
        assertThat("getAssetAsAddress()", neoURI.getAssetAsAddress(), is(GAS_ADDRESS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateFromURI_InvalidURI() {
        NeoURI.fromURI("neo").buildURI();
    }

    @Test
    public void testGenerateURI() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .buildURI();

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX));
    }

    @Test
    public void testGenerateURI_Address_ScriptHash() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS_SCRIPT_HASH)
                .buildURI();
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateURI_InvalidAddress() {
        String invalidAddress = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp";
        new NeoURI().toAddress(invalidAddress);
    }

    @Test(expected = IllegalStateException.class)
    public void testGenerateURI_NoAddress() {
        new NeoURI().buildURI();
    }

    @Test
    public void testGenerateURI_Asset() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .asset("neo")
                .buildURI();

        String BEGIN_TX_ASSET = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET));
    }

    @Test
    public void testGenerateURI_Asset_FromByteArray() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .assetFromByteArray("de5f57d430d3dece511cf975a8d37848cb9e0525")
                .buildURI();

        String BEGIN_TX_ASSET = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=de5f57d430d3dece511cf975a8d37848cb9e0525";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET));
    }

    @Test
    public void testGenerateURI_Amount() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?amount=1";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void testGenerateURI_Amount_String() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .amount("1.0")
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?amount=1.0";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void testGenerateURI_Amount_Integer() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .amount(15)
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?amount=15";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void testGenerateURI_Amount_BigInteger() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .amount(new BigInteger("12"))
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?amount=12";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void testGenerateURI_Asset_Amount() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .asset("neo")
                .amount(AMOUNT)
                .buildURI();

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET_AMOUNT));
    }

    @Test
    public void testGenerateURI_Asset_Amount_AddMultipleTimes() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .asset("gas")
                .asset("neo")
                .amount("90")
                .amount(AMOUNT)
                .buildURI();

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET_AMOUNT));
    }

    @Test
    public void testURI_TransactionBuilder() throws IOException {
        assertThat(new NeoURI()
                .neow3j(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .transferBuilder(),
                is(instanceOf(TransactionBuilder.class)));
    }

    @Test
    public void testURI_TransactionBuilder_Gas() throws IOException {
        assertThat(new NeoURI(neow3j)
                .asset(GasToken.SCRIPT_HASH)
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .transferBuilder(),
                is(instanceOf(TransactionBuilder.class)));
    }

    @Test(expected = IllegalStateException.class)
    public void testURI_TransactionBuilder_NoNeow3j() throws IOException {
        new NeoURI()
                .toAddress(ADDRESS)
                .transferBuilder();
    }

    @Test(expected = IllegalStateException.class)
    public void testURI_InvocationBuilder_NoAddress() throws IOException {
        new NeoURI(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(WALLET)
                .amount(AMOUNT)
                .transferBuilder();
    }

    @Test(expected = IllegalStateException.class)
    public void testURI_InvocationBuilder_NoWallet() throws IOException {
        new NeoURI(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .transferBuilder();
    }

    @Test(expected = IllegalStateException.class)
    public void testURI_InvocationBuilder_NoAmount() throws IOException {
        new NeoURI(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .transferBuilder();
    }

    @Test
    public void testURI_NorNeoNorGas() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_decimals_nep5.json");
        assertThat(new NeoURI(neow3j)
                .asset("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .transferBuilder(), is(instanceOf(TransactionBuilder.class)));
    }

    @Test(expected = UnexpectedReturnTypeException.class)
    public void testURI_NorNeoNorGas_BadDecimalReturn() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_decimals_nep5_badFormat.json");
        new NeoURI(neow3j)
                .asset("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .transferBuilder();
    }

    @Test
    public void testURI_getAssetAsScriptHash() {
        assertThat(new NeoURI(neow3j)
                        .asset("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                        .getAssetAsScriptHash(),
                is(new ScriptHash("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")));
    }

    @Test
    public void testURI_getAssetAsAddress() {
        assertThat(new NeoURI(neow3j)
                        .asset("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                        .getAssetAsAddress(),
                is("ARhJPYxmizqheBQA2dSQAHWfQQsbTSba2S"));
    }
}
