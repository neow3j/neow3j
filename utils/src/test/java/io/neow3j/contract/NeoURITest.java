package io.neow3j.contract;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NeoURITest {

    private final String BEGIN_TX = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";

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

        String beginTxAsset = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo";
        assertEquals(beginTxAsset, uri);
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

        String beginTxAssetAmount = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo&amount=1";
        assertEquals(beginTxAssetAmount, uri);
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

        String beginTxDescription = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=neo&amount=1.0&description=Hello";
        assertEquals(beginTxDescription, uri);
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

        String beginTxEcdh02 = "neo:AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y?asset=gas&amount=0.1&ecdh02=02ed53ad58c838435d4dd7a4b25c1eba01384c814ca53a539405434807afbb04b4";
        assertEquals(beginTxEcdh02, uri);
    }

    @Test(expected = IllegalStateException.class)
    public void useInvalidAddress() {
        String invalidAddress = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp";

        new NeoURI.Builder()
                .toAddress(invalidAddress)
                .build();
    }

}
