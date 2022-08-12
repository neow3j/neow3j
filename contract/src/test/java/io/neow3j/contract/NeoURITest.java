package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
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
import java.math.BigDecimal;
import java.net.URI;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
import static io.neow3j.types.ContractParameter.any;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NeoURITest {

    private static final String BEGIN_TX = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj";
    private static final String BEGIN_TX_ASSET_AMOUNT =
            "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=neo&amount=1";
    private static final String BEGIN_TX_ASSET_NON_NATIVE =
            "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c";
    private static final String BEGIN_TX_ASSET_AMOUNT_MULTIPLE_ASSETS_AND_AMOUNTS =
            "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=neo&amount=1&asset=gas&amount=80";

    private static Neow3j neow3j;

    private static final Account SENDER_ACCOUNT =
            Account.fromWIF("L2jLP9VXA23Hbzo7PmvLfjwkbUaaz887w3aGaeAz5xWyzjizpu9C");
    private static final Hash160 SENDER = SENDER_ACCOUNT.getScriptHash();
    private static final String RECIPIENT_ADDRESS = "NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj";
    private static final Hash160 RECIPIENT = Hash160.fromAddress(RECIPIENT_ADDRESS);
    private static final BigDecimal AMOUNT = BigDecimal.ONE;

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeAll
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
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
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> NeoURI.fromURI(null));
        assertThat(thrown.getMessage(), is("The provided string is null."));
    }

    @Test
    public void fromURI_emptyString() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> NeoURI.fromURI(""));
        assertThat(thrown.getMessage(), is("The provided string does not conform to the NEP-9 standard."));
    }

    @Test
    public void fromURI_invalidScheme() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> NeoURI.fromURI("nao:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj"));
        assertThat(thrown.getMessage(), is("The provided string does not conform to the NEP-9 standard."));
    }

    @Test
    public void fromURI_invalidQuery() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> NeoURI.fromURI("neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset==neo"));
        assertThat(thrown.getMessage(), is("This URI contains invalid queries."));
    }

    @Test
    public void fromURI_invalidSeparator() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> NeoURI.fromURI("neo-NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj"));
        assertThat(thrown.getMessage(), is("The provided string does not conform to the NEP-9 standard."));
    }

    @Test
    public void fromURI_invalidURI_short() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> NeoURI.fromURI("neo:AK2nJJpJr6o664"));
        assertThat(thrown.getMessage(), is("The provided string does not conform to the NEP-9 standard."));
    }

    @Test
    public void fromURI_invalidScale_neo() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> NeoURI.fromURI("neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=neo&amount=1.1")
                        .neow3j(neow3j)
                        .buildTransferFrom(SENDER_ACCOUNT));
        assertThat(thrown.getMessage(), is("The NEO token does not support any decimal places."));
    }

    @Test
    public void fromURI_invalidScale_gas() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> NeoURI.fromURI("neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=gas&amount=0.000000001")
                        .neow3j(neow3j)
                        .buildTransferFrom(SENDER_ACCOUNT));
        assertThat(thrown.getMessage(), is("The GAS token does not support more than 8 decimal places."));
    }

    @Test
    public void fromURI_multipleAssetsAndAmounts() {
        URI uri = NeoURI.fromURI(BEGIN_TX_ASSET_AMOUNT_MULTIPLE_ASSETS_AND_AMOUNTS)
                .buildURI()
                .getURI();

        assertThat(uri, is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
    }

    @Test
    public void testBuildUriNonNativeAsset() {
        Hash160 tokenHash = new Hash160("c0338c7be47126b92eae8a67a2ebaedbbdce6ceb");
        Hash160 recipient = Hash160.fromAddress("NV4fSVvFNHAHtmyCVpQnQ85qXdttUaZkbS");
        NeoURI neoURI = new NeoURI()
                .token(tokenHash)
                .to(recipient)
                .amount(BigDecimal.valueOf(13))
                .buildURI();

        assertThat(neoURI.getURIAsString(), is("neo:NV4fSVvFNHAHtmyCVpQnQ85qXdttUaZkbS?" +
                "asset=c0338c7be47126b92eae8a67a2ebaedbbdce6ceb&amount=13"));
    }

    @Test
    public void fromURI_nonNativeToken() {
        NeoURI neoURI = NeoURI.fromURI(BEGIN_TX_ASSET_NON_NATIVE);

        assertThat(neoURI.getToken(), is(new Hash160("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")));
    }

    @Test
    public void fromURI_Getter() {
        NeoURI neoURI = NeoURI.fromURI(BEGIN_TX_ASSET_AMOUNT).buildURI();

        assertThat("getAddress()", neoURI.getRecipientAddress(), is(RECIPIENT_ADDRESS));
        assertThat("getAddressAsScriptHash()", neoURI.getRecipient(),
                is(RECIPIENT));
        assertThat("getAsset()", neoURI.getTokenAsString(),
                isOneOf(NeoToken.SCRIPT_HASH.toString(), "neo"));
        assertThat("getAssetAsScriptHash()", neoURI.getToken(), is(NeoToken.SCRIPT_HASH));
        assertThat("getAssetAsAddress()", neoURI.getTokenAsAddress(),
                is(NeoToken.SCRIPT_HASH.toAddress()));
        assertThat("getAmount()", neoURI.getAmount(), is(AMOUNT));
        assertThat("getAmountAsString()", neoURI.getAmountAsString(), is(AMOUNT.toString()));
    }

    @Test
    public void fromURI_Getter_GAS() {
        String BEGIN_TX_ASSET_GAS = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=gas";
        NeoURI neoURI = NeoURI.fromURI(BEGIN_TX_ASSET_GAS).buildURI();

        assertThat("getAsset()", neoURI.getTokenAsString(),
                isOneOf(GasToken.SCRIPT_HASH.toString(), "gas"));
        assertThat("getAssetAsScriptHash()", neoURI.getToken(), is(GasToken.SCRIPT_HASH));
        assertThat("getAssetAsAddress()", neoURI.getTokenAsAddress(),
                is(GasToken.SCRIPT_HASH.toAddress()));
    }

    @Test
    public void buildURI() {
        NeoURI neoURI = new NeoURI()
                .to(RECIPIENT)
                .buildURI();

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX));
    }

    @Test
    public void buildURI_address_ScriptHash() {
        NeoURI neoURI = new NeoURI()
                .to(RECIPIENT)
                .buildURI();

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX));
    }

    @Test
    public void buildURI_noAddress() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> new NeoURI().buildURI());
        assertThat(thrown.getMessage(), is("Could not create a NEP-9 URI without a recipient address."));
    }

    @Test
    public void buildURI_asset() {
        NeoURI neoURI = new NeoURI()
                .to(RECIPIENT)
                .token("neo")
                .buildURI();

        String BEGIN_TX_ASSET = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?asset=neo";

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET));
    }

    @Test
    public void buildURI_amount() {
        NeoURI neoURI = new NeoURI()
                .to(RECIPIENT)
                .amount(AMOUNT)
                .buildURI();

        String beginTxAmount = "neo:NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj?amount=1";

        assertThat("getURI()", neoURI.getURI(), is(URI.create(beginTxAmount)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(beginTxAmount));
    }

    @Test
    public void buildURI_asset_amount() {
        NeoURI neoURI = new NeoURI()
                .to(RECIPIENT)
                .token("neo")
                .amount(AMOUNT)
                .buildURI();

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET_AMOUNT));
    }

    @Test
    public void buildURI_asset_amount_addMultipleTimes() {
        NeoURI neoURI = new NeoURI()
                .to(RECIPIENT)
                .token("gas")
                .token("neo")
                .amount(new BigDecimal("90"))
                .amount(AMOUNT)
                .buildURI();

        assertThat("getURI()", neoURI.getURI(), is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
        assertThat("getURIAsString()", neoURI.getURIAsString(), is(BEGIN_TX_ASSET_AMOUNT));
    }

    @Test
    public void buildTransfer() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_decimals_nep17.json", "decimals");
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_1000000.json",
                "balanceOf");
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NeoToken.SCRIPT_HASH, "transfer",
                        asList(hash160(SENDER),
                                hash160(RECIPIENT),
                                integer(100),
                                any(null)))
                .toArray();

        TransactionBuilder b = new NeoURI()
                .neow3j(neow3j)
                .token(NeoToken.SCRIPT_HASH)
                .to(RECIPIENT)
                .amount(AMOUNT)
                .buildTransferFrom(SENDER_ACCOUNT);

        assertThat(b.getScript(), is(expectedScript));
        assertThat(b.getSigners().get(0).getScriptHash(), is(SENDER));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
    }

    @Test
    public void buildTransfer_Gas() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_decimals_nep17.json", "decimals");
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_1000000.json",
                "balanceOf");
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(GasToken.SCRIPT_HASH, "transfer",
                        asList(hash160(SENDER),
                                hash160(RECIPIENT),
                                integer(200),
                                any(null)))
                .toArray();

        TransactionBuilder b = new NeoURI()
                .neow3j(neow3j)
                .token(GasToken.SCRIPT_HASH)
                .to(RECIPIENT)
                .amount(new BigDecimal("2"))
                .buildTransferFrom(SENDER_ACCOUNT);

        assertThat(b.getScript(), is(expectedScript));
        assertThat(b.getSigners().get(0).getScriptHash(), is(SENDER));
    }

    @Test
    public void buildTransfer_noNeow3j() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> new NeoURI()
                        .to(RECIPIENT)
                        .buildTransferFrom(SENDER_ACCOUNT));
        assertThat(thrown.getMessage(), is("Neow3j instance is not set."));
    }

    @Test
    public void buildTransfer_noAddress() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> new NeoURI(neow3j)
                        .token(NeoToken.SCRIPT_HASH)
                        .amount(AMOUNT)
                        .buildTransferFrom(SENDER_ACCOUNT));
        assertThat(thrown.getMessage(), is("Recipient is not set."));
    }

    @Test
    public void buildTransfer_noAmount() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> new NeoURI(neow3j)
                        .token(NeoToken.SCRIPT_HASH)
                        .to(RECIPIENT)
                        .buildTransferFrom(SENDER_ACCOUNT));
        assertThat(thrown.getMessage(), is("Amount is not set."));
    }

    @Test
    public void buildTransfer_nonNativeAsset() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_decimals_nep17.json", "decimals");
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_1000000.json",
                "balanceOf");

        TransactionBuilder b = new NeoURI(neow3j)
                .token("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                .to(RECIPIENT)
                .amount(AMOUNT)
                .buildTransferFrom(SENDER_ACCOUNT);

        assertThat(b, instanceOf(TransactionBuilder.class));
    }

    @Test
    public void buildTransfer_nonNativeAsset_invalidAmountDecimals() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_decimals_nep17.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new NeoURI(neow3j)
                        .token("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                        .to(RECIPIENT)
                        .amount(new BigDecimal("0.001"))
                        .buildTransferFrom(SENDER_ACCOUNT));
        assertThat(thrown.getMessage(), containsString("does not support more than 2 decimal places."));
    }

    @Test
    public void buildTransfer_nonNativeAsset_badDecimalReturn() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_decimals_nep17_badFormat.json");

        UnexpectedReturnTypeException thrown = assertThrows(UnexpectedReturnTypeException.class,
                () -> new NeoURI(neow3j)
                        .token("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                        .to(RECIPIENT)
                        .amount(AMOUNT)
                        .buildTransferFrom(SENDER_ACCOUNT));
        assertThat(thrown.getMessage(), is("Got stack item of type Boolean but expected Integer."));
    }

    @Test
    public void getAsset() {
        assertThat(new NeoURI(neow3j)
                        .token("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                        .getToken(),
                is(new Hash160("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")));
    }

    @Test
    public void getAssetAsString() {
        assertThat(new NeoURI(neow3j)
                        .token("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c")
                        .getTokenAsString(),
                is("b1e8f1ce80c81dc125e7d0e75e5ce3f7f4d4d36c"));
    }

    @Test
    public void getAssetAsAddress() {
        assertThat(new NeoURI(neow3j)
                        .token("d6c712eb53b1a130f59fd4e5864bdac27458a509")
                        .getTokenAsAddress(),
                is("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke"));
    }

}
