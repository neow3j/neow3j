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
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NeoURITest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private static final String BEGIN_TX = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
    private static final String BEGIN_TX_ASSET_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo&amount=1";
    private static final String BEGIN_TX_ASSET_NON_NATIVE = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c";
    private static final String BEGIN_TX_ASSET_AMOUNT_MULTIPLE_ASSETS_AND_AMOUNTS =
            "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo&amount=1&asset=gas&amount=80";

    private static Neow3j neow3j;
    private static final Wallet WALLET = Wallet.create();

    private static final String ADDRESS = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
    private static final ScriptHash ADDRESS_SCRIPT_HASH = ScriptHash.fromAddress(ADDRESS);
    private static final BigDecimal AMOUNT = new BigDecimal(1);
    private static final ScriptHash NEO_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final String NEO = NeoToken.SCRIPT_HASH.toString();
    private static final String NEO_ADDRESS = NeoToken.SCRIPT_HASH.toAddress();
    private static final String GAS = GasToken.SCRIPT_HASH.toString();
    private static final ScriptHash GAS_SCRIPT_HASH = GasToken.SCRIPT_HASH;
    private static final String GAS_ADDRESS = GasToken.SCRIPT_HASH.toAddress();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);
        neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
    }

    @Test
    public void fromURI() {
        URI uri = NeoURI.fromURI(BEGIN_TX_ASSET_AMOUNT).buildURI().getURI();

        assertThat(uri, is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
    }

    @Test
    public void fromURI_null() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("The provided String is null.");
        NeoURI.fromURI(null);
    }

    @Test
    public void fromURI_emptyString() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("does not conform to the NEP-9 standard");
        NeoURI.fromURI("");
    }

    @Test
    public void fromURI_invalidScheme() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("does not conform to the NEP-9 standard");
        NeoURI.fromURI("nao:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y");
    }

    @Test
    public void fromURI_invalidSeparator() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("does not conform to the NEP-9 standard");
        NeoURI.fromURI("neo-AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y");
    }

    @Test
    public void fromURI_invalidURI_short() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("does not conform to the NEP-9 standard.");
        NeoURI.fromURI("neo:AK2nJJpJr6o664");
    }

    @Test
    public void fromURI_multipleAssetsAndAmounts() {
        URI uri = NeoURI.fromURI(BEGIN_TX_ASSET_AMOUNT_MULTIPLE_ASSETS_AND_AMOUNTS).buildURI().getURI();
        assertThat(uri, is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
    }

    @Test
    public void fromURI_nonNativeToken() {
        NeoURI neoURI = NeoURI.fromURI(BEGIN_TX_ASSET_NON_NATIVE);
        assertThat(neoURI.getAsset(), is(new ScriptHash("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")));
    }

    @Test
    public void fromURI_Getter() {
        NeoURI neoURI = NeoURI.fromURI(BEGIN_TX_ASSET_AMOUNT).buildURI();

        assertThat("getAddress()", neoURI.getToAddress(), is(ADDRESS));
        assertThat("getAddressAsScriptHash()", neoURI.getAddressAsScriptHash(), is(ADDRESS_SCRIPT_HASH));
        assertThat("getAsset()", neoURI.getAssetAsString(), isOneOf(NEO, "neo"));
        assertThat("getAssetAsScriptHash()", neoURI.getAsset(), is(NEO_SCRIPT_HASH));
        assertThat("getAssetAsAddress()", neoURI.getAssetAsAddress(), is(NEO_ADDRESS));
        assertThat("getAmount()", neoURI.getAmount(), is(AMOUNT));
        assertThat("getAmountAsString()", neoURI.getAmountAsString(), is(AMOUNT.toString()));
    }

    @Test
    public void fromURI_Getter_GAS() {
        String BEGIN_TX_ASSET_GAS = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=gas";
        NeoURI neoURI = NeoURI.fromURI(BEGIN_TX_ASSET_GAS).buildURI();

        assertThat("getAsset()", neoURI.getAssetAsString(), isOneOf(GAS, "gas"));
        assertThat("getAssetAsScriptHash()", neoURI.getAsset(), is(GAS_SCRIPT_HASH));
        assertThat("getAssetAsAddress()", neoURI.getAssetAsAddress(), is(GAS_ADDRESS));
    }

    @Test
    public void buildURI() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .buildURI();

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX));
    }

    @Test
    public void buildURI_address_ScriptHash() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS_SCRIPT_HASH)
                .buildURI();
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX));
    }

    @Test
    public void buildURI_invalidAddress() {
        String invalidAddress = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp";
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Invalid address");
        new NeoURI().toAddress(invalidAddress);
    }

    @Test
    public void buildURI_noAddress() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Could not create a NEP-9 URI without a recipient address.");
        new NeoURI().buildURI();
    }

    @Test
    public void buildURI_asset() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .asset("neo")
                .buildURI();

        String BEGIN_TX_ASSET = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET));
    }

    @Test
    public void buildURI_asset_fromByteArray() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .asset(
                        Numeric.hexStringToByteArray("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c"))
                .buildURI();

        String BEGIN_TX_ASSET = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET));
    }

    @Test
    public void buildURI_amount() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?amount=1";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void buildURI_amount_String() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .amount("1.0")
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?amount=1.0";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void buildURI_amount_Integer() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .amount(15)
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?amount=15";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void buildURI_amount_BigInteger() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .amount(new BigInteger("12"))
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?amount=12";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void buildURI_asset_amount() {
        NeoURI neoURI = new NeoURI()
                .toAddress(ADDRESS)
                .asset("neo")
                .amount(AMOUNT)
                .buildURI();

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET_AMOUNT));
    }

    @Test
    public void buildURI_asset_amount_addMultipleTimes() {
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
    public void buildTransfer() throws IOException {
        assertThat(new NeoURI()
                .neow3j(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .buildTransfer(),
                is(instanceOf(TransactionBuilder.class)));
    }

    @Test
    public void buildTransfer_Gas() throws IOException {
        assertThat(new NeoURI(neow3j)
                .asset(GasToken.SCRIPT_HASH)
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .buildTransfer(),
                is(instanceOf(TransactionBuilder.class)));
    }

    @Test
    public void buildTransfer_noNeow3j() throws IOException {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Neow3j instance is not set.");
        new NeoURI()
                .toAddress(ADDRESS)
                .buildTransfer();
    }

    @Test
    public void buildTransfer_noAddress() throws IOException {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Recipient address is not set.");
        new NeoURI(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(WALLET)
                .amount(AMOUNT)
                .buildTransfer();
    }

    @Test
    public void buildTransfer_noWallet() throws IOException {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Wallet is not set.");
        new NeoURI(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .buildTransfer();
    }

    @Test
    public void buildTransfer_noAmount() throws IOException {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Amount is not set.");
        new NeoURI(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .buildTransfer();
    }

    @Test
    public void buildTransfer_nonNativeAsset() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_decimals_nep5.json");
        assertThat(new NeoURI(neow3j)
                .asset("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .buildTransfer(), is(instanceOf(TransactionBuilder.class)));
    }

    @Test
    public void buildTransfer_nonNativeAsset_badDecimalReturn() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_decimals_nep5_badFormat.json");
        exceptionRule.expect(UnexpectedReturnTypeException.class);
        exceptionRule.expectMessage("Got stack item of type Boolean but expected Integer.");
        new NeoURI(neow3j)
                .asset("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .buildTransfer();
    }

    @Test
    public void getAsset() {
        assertThat(new NeoURI(neow3j)
                .asset("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                .getAsset(),
                is(new ScriptHash("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")));
    }

    @Test
    public void getAssetAsString() {
        assertThat(new NeoURI(neow3j)
                        .asset("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                        .getAssetAsString(),
                is("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c"));
    }

    @Test
    public void getAssetAsAddress() {
        assertThat(new NeoURI(neow3j)
                        .asset("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                        .getAssetAsAddress(),
                is("ARhJPYxmizqheBQA2dSQAHWfQQsbTSba2S"));
    }
}
