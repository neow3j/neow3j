package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash160;
import io.neow3j.utils.Strings;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

/**
 * Wrapper class to generate NEP-9 compatible URI schemes for NEP-17 Token transfers.
 */
public class NeoURI {

    private URI uri;

    private Neow3j neow3j;
    private Wallet wallet;
    private Hash160 recipient;
    private Hash160 tokenHash;
    private BigDecimal amount;

    private static final String NEO_SCHEME = "neo";
    private static final int MIN_NEP9_URI_LENGTH = 38;

    private static final String NEO_TOKEN_STRING = "neo";
    private static final String GAS_TOKEN_STRING = "gas";

    public NeoURI() {
    }

    public NeoURI(Neow3j neow3j) {
        this.neow3j = neow3j;
    }

    /**
     * Creates a NeoURI from a NEP-9 URI String.
     *
     * @param uriString a NEP-9 URI String.
     * @return a NeoURI object.
     * @throws IllegalFormatException if the provided URI has an invalid format.
     */
    public static NeoURI fromURI(String uriString)
            throws IllegalFormatException {

        if (uriString == null) {
            throw new IllegalArgumentException("The provided String is null.");
        }

        String[] baseAndQuery = uriString.split("\\?");
        String[] beginTx = baseAndQuery[0].split(":");

        if (!beginTx[0].equals(NEO_SCHEME) || beginTx.length != 2 ||
                uriString.length() < MIN_NEP9_URI_LENGTH) {
            throw new IllegalArgumentException("The provided string does not conform to the NEP-9" +
                    " standard.");
        }
        NeoURI neoURI = new NeoURI();

        // Add the address
        neoURI.to(Hash160.fromAddress(beginTx[1]));

        // Add the optional parts of the uri - asset and amount.
        if (baseAndQuery.length == 2) {
            String[] query = baseAndQuery[1].split("&");
            for (String singleQuery : query) {
                String[] singleQueryParts = singleQuery
                        .split("=");
                if (singleQueryParts.length != 2) {
                    throw new IllegalArgumentException("This uri contains invalid queries.");
                }
                if (singleQueryParts[0].equals("asset") &&
                        neoURI.tokenHash == null) {
                    String assetID = singleQueryParts[1];
                    if (assetID.equals(NEO_TOKEN_STRING)) {
                        neoURI.tokenHash = NeoToken.SCRIPT_HASH;
                    } else if (assetID.equals(GAS_TOKEN_STRING)) {
                        neoURI.tokenHash = GasToken.SCRIPT_HASH;
                    } else {
                        neoURI.tokenHash = new Hash160(assetID);
                    }
                } else if (singleQueryParts[0].equals("amount") &&
                        neoURI.amount == null) {
                    neoURI.amount = new BigDecimal(singleQueryParts[1]);
                }
            }
        }
        return neoURI;
    }

    /**
     * Creates a transaction script to transfer and initializes a {@link TransactionBuilder}
     * based on this script which is ready to be signed and sent.
     * <p>
     * Uses only the wallet's default account.
     *
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder buildTransfer() throws IOException {
        if (neow3j == null) {
            throw new IllegalStateException("Neow3j instance is not set.");
        }
        if (recipient == null) {
            throw new IllegalStateException("Recipient is not set.");
        }
        if (wallet == null) {
            throw new IllegalStateException("Wallet is not set.");
        }
        if (amount == null) {
            throw new IllegalStateException("Amount is not set.");
        }

        FungibleToken token = new FungibleToken(tokenHash, neow3j);

        int amountScale = amount.stripTrailingZeros().scale();
        if (isNeoToken(tokenHash) && amountScale > NeoToken.DECIMALS) {
            throw new IllegalArgumentException("The Neo token does not support any decimal " +
                    "places.");
        } else if (isGasToken(tokenHash) && amountScale > GasToken.DECIMALS) {
            throw new IllegalArgumentException("The Gas token does not support more than " +
                    GasToken.DECIMALS + " decimal places.");
        } else {
            int decimals = token.getDecimals();
            if (amountScale > decimals) {
                throw new IllegalArgumentException("The token '" + tokenHash + "' does not " +
                        "support more than " + decimals + " decimal places.");
            }
        }

        return token.transfer(wallet, recipient, token.toFractions(amount));
    }

    private boolean isNeoToken(Hash160 asset) {
        return asset.equals(NeoToken.SCRIPT_HASH);
    }

    private boolean isGasToken(Hash160 asset) {
        return asset.equals(GasToken.SCRIPT_HASH);
    }

    /**
     * Sets the recipient's script hash.
     *
     * @param recipient the recipient's script hash.
     * @return this NeoURI object.
     */
    public NeoURI to(Hash160 recipient) {
        this.recipient = recipient;
        return this;
    }

    /**
     * Sets the token.
     *
     * @param token the token hash.
     * @return this NeoURI object.
     */
    public NeoURI token(Hash160 token) {
        this.tokenHash = token;
        return this;
    }

    /**
     * Sets the token.
     *
     * @param token the token hash, 'neo' or 'gas'.
     * @return this NeoURI object.
     */
    public NeoURI token(String token) {
        if (token.equals("neo")) {
            this.tokenHash = NeoToken.SCRIPT_HASH;
        } else if (token.equals("gas")) {
            this.tokenHash = GasToken.SCRIPT_HASH;
        } else {
            this.tokenHash = new Hash160(token);
        }
        return this;
    }

    /**
     * Sets the amount.
     * <p>
     * Make sure to use decimals and not token fractions. E.g. for GAS use 1.5 instead of
     * 150_000_000.
     *
     * @param amount the amount.
     * @return this NeoURI object.
     */
    public NeoURI amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Sets the neow3j instance.
     *
     * @param neow3j the neow3j instance.
     * @return this NeoURI object.
     */
    public NeoURI neow3j(Neow3j neow3j) {
        this.neow3j = neow3j;
        return this;
    }

    /**
     * Sets the wallet.
     *
     * @param wallet the wallet.
     * @return this NeoURI object.
     */
    public NeoURI wallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    private String buildQueryPart() {
        List<String> query = new ArrayList<>();
        if (tokenHash != null) {
            if (tokenHash.equals(NeoToken.SCRIPT_HASH)) {
                query.add("asset=neo");
            } else if (tokenHash.equals(GasToken.SCRIPT_HASH)) {
                query.add("asset=gas");
            } else {
                query.add("asset=" + tokenHash);
            }
        }
        if (amount != null) {
            query.add("amount=" + amount);
        }
        return Strings.join(query, "&");
    }

    /**
     * Builds a NEP-9 URI from the set variables and stores its value to its variable {@code uri}
     * as a {@link URI}.
     *
     * @return this NeoURI object.
     */
    public NeoURI buildURI() {
        if (recipient == null) {
            throw new IllegalStateException(
                    "Could not create a NEP-9 URI without a recipient address.");
        }
        String basePart = NEO_SCHEME + ":" + recipient.toAddress();
        String queryPart = buildQueryPart();

        String uri;
        if (queryPart.equals("")) {
            uri = basePart;
        } else {
            uri = basePart + "?" + queryPart;
        }

        this.uri = URI.create(uri);
        return this;
    }

    /**
     * Gets the NEP-9 URI of this NeoURI.
     *
     * @return the {@link URI} of this NeoURI.
     */
    public URI getURI() {
        return uri;
    }

    /**
     * Gets the NEP-9 URI of this  NeoURI.
     *
     * @return the URI of this instance as string.
     */
    public String getURIAsString() {
        return uri.toString();
    }

    /**
     * Gets the recipient address as script hash.
     *
     * @return the script hash of the recipient address.
     */
    public Hash160 getRecipient() {
        return recipient;
    }

    /**
     * Gets the recipient address.
     *
     * @return the recipient address.
     */
    public String getRecipientAddress() {
        return recipient.toAddress();
    }

    /**
     * Gets the asset.
     *
     * @return the asset.
     */
    public Hash160 getToken() {
        return tokenHash;
    }

    /**
     * Gets the asset.
     *
     * @return the asset as string.
     */
    public String getTokenAsString() {
        if (tokenHash.equals(NeoToken.SCRIPT_HASH)) {
            return NEO_TOKEN_STRING;
        } else if (tokenHash.equals(GasToken.SCRIPT_HASH)) {
            return GAS_TOKEN_STRING;
        }
        return tokenHash.toString();
    }

    /**
     * Gets the asset.
     *
     * @return the asset as address.
     */
    public String getTokenAsAddress() {
        return tokenHash.toAddress();
    }

    /**
     * Gets the amount.
     *
     * @return the amount.
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Gets the amount.
     *
     * @return the amount as string.
     */
    public String getAmountAsString() {
        return amount.toString();
    }

}
