package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractParameter.any;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NeoURITest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private static final String BEGIN_TX = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj";
    private static final String BEGIN_TX_ASSET_AMOUNT = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=neo&amount=1";
    private static final String BEGIN_TX_ASSET_NON_NATIVE = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c";
    private static final String BEGIN_TX_ASSET_AMOUNT_MULTIPLE_ASSETS_AND_AMOUNTS =
            "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=neo&amount=1&asset=gas&amount=80";

    private static Neow3j neow3j;
    private static Wallet wallet;

    private static final ScriptHash SENDER = ScriptHash.fromAddress("NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmy");
    private static final String RECIPIENT = "NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj";
    private static final ScriptHash RECIPIENT_SCRIPT_HASH = ScriptHash.fromAddress(RECIPIENT);
    private static final BigDecimal AMOUNT = new BigDecimal(1);

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockRule.port();
        WireMock.configureFor(port);
        neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        wallet = Wallet.withAccounts(Account.fromAddress(SENDER.toAddress()));
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
        NeoURI.fromURI("nao:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj");
    }

    @Test
    public void fromURI_invalidSeparator() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("does not conform to the NEP-9 standard");
        NeoURI.fromURI("neo-NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj");
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

        assertThat("getAddress()", neoURI.getToAddress(), is(RECIPIENT));
        assertThat("getAddressAsScriptHash()", neoURI.getAddressAsScriptHash(), is(RECIPIENT_SCRIPT_HASH));
        assertThat("getAsset()", neoURI.getAssetAsString(), isOneOf(NeoToken.SCRIPT_HASH.toString(), "neo"));
        assertThat("getAssetAsScriptHash()", neoURI.getAsset(), is(NeoToken.SCRIPT_HASH));
        assertThat("getAssetAsAddress()", neoURI.getAssetAsAddress(), is(NeoToken.SCRIPT_HASH.toAddress()));
        assertThat("getAmount()", neoURI.getAmount(), is(AMOUNT));
        assertThat("getAmountAsString()", neoURI.getAmountAsString(), is(AMOUNT.toString()));
    }

    @Test
    public void fromURI_Getter_GAS() {
        String BEGIN_TX_ASSET_GAS = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=gas";
        NeoURI neoURI = NeoURI.fromURI(BEGIN_TX_ASSET_GAS).buildURI();

        assertThat("getAsset()", neoURI.getAssetAsString(), isOneOf(GasToken.SCRIPT_HASH.toString(), "gas"));
        assertThat("getAssetAsScriptHash()", neoURI.getAsset(), is(GasToken.SCRIPT_HASH));
        assertThat("getAssetAsAddress()", neoURI.getAssetAsAddress(), is(GasToken.SCRIPT_HASH.toAddress()));
    }

    @Test
    public void buildURI() {
        NeoURI neoURI = new NeoURI()
                .toAddress(RECIPIENT)
                .buildURI();

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX));
    }

    @Test
    public void buildURI_address_ScriptHash() {
        NeoURI neoURI = new NeoURI()
                .toAddress(RECIPIENT_SCRIPT_HASH)
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
                .toAddress(RECIPIENT)
                .asset("neo")
                .buildURI();

        String BEGIN_TX_ASSET = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=neo";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET));
    }

    @Test
    public void buildURI_asset_fromByteArray() {
        NeoURI neoURI = new NeoURI()
                .toAddress(RECIPIENT)
                .asset(
                        Numeric.hexStringToByteArray("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c"))
                .buildURI();

        String BEGIN_TX_ASSET = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET));
    }

    @Test
    public void buildURI_amount() {
        NeoURI neoURI = new NeoURI()
                .toAddress(RECIPIENT)
                .amount(AMOUNT)
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?amount=1";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void buildURI_amount_String() {
        NeoURI neoURI = new NeoURI()
                .toAddress(RECIPIENT)
                .amount("1.0")
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?amount=1.0";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void buildURI_amount_Integer() {
        NeoURI neoURI = new NeoURI()
                .toAddress(RECIPIENT)
                .amount(15)
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?amount=15";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void buildURI_amount_BigInteger() {
        NeoURI neoURI = new NeoURI()
                .toAddress(RECIPIENT)
                .amount(new BigInteger("12"))
                .buildURI();

        String BEGIN_TX_AMOUNT = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?amount=12";
        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_AMOUNT));
    }

    @Test
    public void buildURI_asset_amount() {
        NeoURI neoURI = new NeoURI()
                .toAddress(RECIPIENT)
                .asset("neo")
                .amount(AMOUNT)
                .buildURI();

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET_AMOUNT));
    }

    @Test
    public void buildURI_asset_amount_addMultipleTimes() {
        NeoURI neoURI = new NeoURI()
                .toAddress(RECIPIENT)
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
        byte[] expectedScript = new ScriptBuilder().contractCall(NeoToken.SCRIPT_HASH,
                "transfer", Arrays.asList(
                        hash160(SENDER),
                        hash160(RECIPIENT_SCRIPT_HASH),
                        integer(1),
                        any(null))).toArray();

        TransactionBuilder b = new NeoURI()
                .neow3j(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(wallet)
                .toAddress(RECIPIENT)
                .amount(AMOUNT)
                .buildTransfer();

        assertThat(b.getScript(), is(expectedScript));
        assertThat(b.getSigners().get(0).getScriptHash(), is(SENDER));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
    }

    @Test
    public void buildTransfer_Gas() throws IOException {
        byte[] expectedScript = new ScriptBuilder().contractCall(GasToken.SCRIPT_HASH,
                "transfer", Arrays.asList(
                        hash160(SENDER),
                        hash160(RECIPIENT_SCRIPT_HASH),
                        integer(200000000),
                        any(null))).toArray();

        TransactionBuilder b = new NeoURI()
                .neow3j(neow3j)
                .asset(GasToken.SCRIPT_HASH)
                .wallet(wallet)
                .toAddress(RECIPIENT)
                .amount(2)
                .buildTransfer();

        assertThat(b.getScript(), is(expectedScript));
        assertThat(b.getSigners().get(0).getScriptHash(), is(SENDER));
    }

    @Test
    public void buildTransfer_noNeow3j() throws IOException {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Neow3j instance is not set.");
        new NeoURI()
                .toAddress(RECIPIENT)
                .buildTransfer();
    }

    @Test
    public void buildTransfer_noAddress() throws IOException {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Recipient address is not set.");
        new NeoURI(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(wallet)
                .amount(AMOUNT)
                .buildTransfer();
    }

    @Test
    public void buildTransfer_noWallet() throws IOException {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Wallet is not set.");
        new NeoURI(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .toAddress(RECIPIENT)
                .amount(AMOUNT)
                .buildTransfer();
    }

    @Test
    public void buildTransfer_noAmount() throws IOException {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Amount is not set.");
        new NeoURI(neow3j)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(wallet)
                .toAddress(RECIPIENT)
                .buildTransfer();
    }

    @Test
    public void buildTransfer_nonNativeAsset() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_decimals_nep17.json");
        assertThat(new NeoURI(neow3j)
                .asset("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                .wallet(wallet)
                .toAddress(RECIPIENT)
                .amount(AMOUNT)
                .buildTransfer(), is(instanceOf(TransactionBuilder.class)));
    }

    @Test
    public void buildTransfer_nonNativeAsset_badDecimalReturn() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_decimals_nep17_badFormat.json");
        exceptionRule.expect(UnexpectedReturnTypeException.class);
        exceptionRule.expectMessage("Got stack item of type Boolean but expected Integer.");
        new NeoURI(neow3j)
                .asset("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                .wallet(wallet)
                .toAddress(RECIPIENT)
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
                        .asset("d6c712eb53b1a130f59fd4e5864bdac27458a509")
                        .getAssetAsAddress(),
                is("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke"));
    }
}
