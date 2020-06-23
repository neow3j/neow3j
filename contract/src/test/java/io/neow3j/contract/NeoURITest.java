package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.Wallet;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;

public class NeoURITest {

    private static final String BEGIN_TX = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
    private static final String BEGIN_TX_ASSET_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo&amount=1";
    private static final String BEGIN_TX_ASSET_AMOUNT_MULTIPLE_ASSETS_AND_AMOUNTS =
            "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo&amount=1&asset=gas&amount=80";

    private static final Neow3j NEOW3J = Neow3j.build(new HttpService("http://localhost:40332"));
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

    @Test
    public void testGenerateFromURI() {
        URI uri = NeoURI.fromURI(BEGIN_TX_ASSET_AMOUNT).buildURI().getURI();

        assertThat(uri, is(URI.create(BEGIN_TX_ASSET_AMOUNT)));
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

    @Test(expected = IllegalStateException.class)
    public void testGenerateFromURI_InvalidURI() {
        NeoURI.fromURI("neo").buildURI().getURI();
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

    @Test(expected = IllegalStateException.class)
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
                .assetFromByteArray("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789")
                .buildURI();

        String BEGIN_TX_ASSET = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=9bde8f209c88dd0e7ca3bf0af0f476cdd8207789";
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
    public void testURI_InvocationBuilder() throws IOException {
        new NeoURI(NEOW3J)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .invocationBuilder();
    }

    @Test
    public void testURI_InvocationBuilder_Neow3j() throws IOException {
        assertThat(new NeoURI()
                .neow3j(NEOW3J)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .invocationBuilder(),
                is(instanceOf(Invocation.Builder.class)));
    }

    @Test
    public void testURI_InvocationBuilder_Gas() throws IOException {
        assertThat(new NeoURI(NEOW3J)
                .asset(GasToken.SCRIPT_HASH)
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .invocationBuilder(),
                is(instanceOf(Invocation.Builder.class)));
    }

    @Test(expected = IllegalStateException.class)
    public void testURI_InvocationBuilder_NoNeow3j() throws IOException {
        new NeoURI()
                .toAddress(ADDRESS)
                .invocationBuilder();
    }

    @Test(expected = IllegalStateException.class)
    public void testURI_InvocationBuilder_NoAddress() throws IOException {
        new NeoURI(NEOW3J)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(WALLET)
                .amount(AMOUNT)
                .invocationBuilder();
    }

    @Test(expected = IllegalStateException.class)
    public void testURI_InvocationBuilder_NoWallet() throws IOException {
        new NeoURI(NEOW3J)
                .asset(NeoToken.SCRIPT_HASH)
                .toAddress(ADDRESS)
                .amount(AMOUNT)
                .invocationBuilder();
    }

    @Test(expected = IllegalStateException.class)
    public void testURI_InvocationBuilder_NoAmount() throws IOException {
        new NeoURI(NEOW3J)
                .asset(NeoToken.SCRIPT_HASH)
                .wallet(WALLET)
                .toAddress(ADDRESS)
                .invocationBuilder();
    }
}
