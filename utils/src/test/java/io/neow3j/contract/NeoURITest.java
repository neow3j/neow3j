package io.neow3j.contract;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class NeoURITest {

    private final String BEGIN_TX = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";

    private final String BEGIN_TX_ASSET = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo";

    private final String BEGIN_TX_ASSET_AMOUNT = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo&amount=1";

    private final String BEGIN_TX_DESCRIPTION = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo&amount=1.0&description=Hello";

    private final String BEGIN_TX_ECDH02 = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=gas&amount=0.1&ecdh02=02ed53ad58c838435d4dd7a4b25c1eba01384c814ca53a539405434807afbb04b4";

    private final String VALID_ADDRESS = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";

    @Test
    public void fromUri() {
        NeoURI neoURI = NeoURI.fromURI(BEGIN_TX);
        String uriString = neoURI.getUriAsString();

        assertEquals(BEGIN_TX, uriString);
    }

    @Test
    public void beginTx() {
        NeoURI neoURI = new NeoURI.Builder()
                .toAddress(VALID_ADDRESS)
                .build();

        String uri = neoURI.getUriAsString();

        assertEquals(BEGIN_TX, uri);
    }

    @Test
    public void beginTxAsset() {
        NeoURI neoURI = new NeoURI.Builder()
                .toAddress(VALID_ADDRESS)
                .asset("neo")
                .build();

        String uri = neoURI.getUriAsString();

        assertEquals(BEGIN_TX_ASSET, uri);
    }

    @Test
    public void beginTxAssetAmount() {
        int amount = 1;

        NeoURI neoURI = new NeoURI.Builder()
                .toAddress(VALID_ADDRESS)
                .asset("neo")
                .amount(amount)
                .build();

        String uri = neoURI.getUriAsString();

        assertEquals(BEGIN_TX_ASSET_AMOUNT, uri);
    }

    @Test
    public void beginTxDescription() {
        NeoURI neoURI = new NeoURI.Builder()
                .toAddress(VALID_ADDRESS)
                .asset("neo")
                .amount("1.0")
                .description("Hello")
                .build();

        String uri = neoURI.getUriAsString();

        assertEquals(BEGIN_TX_DESCRIPTION, uri);
    }

    @Test
    public void beginTxEcdh02() {
        NeoURI neoURI = new NeoURI.Builder()
                .toAddress(VALID_ADDRESS)
                .asset("gas")
                .amount("0.1")
                .ecdh02("02ed53ad58c838435d4dd7a4b25c1eba01384c814ca53a539405434807afbb04b4")
                .build();
        String uri = neoURI.getUriAsString();

        assertEquals(BEGIN_TX_ECDH02, uri);
    }

    @Test(expected = IllegalStateException.class)
    public void useInvalidAddress() {
        String invalidAddress = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp";

        new NeoURI.Builder()
                .toAddress(invalidAddress)
                .build();
    }

}
