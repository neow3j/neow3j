package io.neow3j.contract;

import static io.neow3j.contract.ContractParameter.any;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.transaction.Signer.calledByEntry;

import io.neow3j.protocol.Neow3j;
import io.neow3j.utils.AddressUtils;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Strings;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
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
    private Hash160 toAddress;
    private Hash160 asset;
    private BigDecimal amount;

    private static final String NEO_SCHEME = "neo";
    private static final String TRANSFER_FUNCTION = "transfer";
    private static final int MIN_NEP9_URI_LENGTH = 38;

    private static final String NEO_ASSET = "neo";
    private static final String GAS_ASSET = "gas";

    public NeoURI() {
    }

    public NeoURI(Neow3j neow3j) {
        this.neow3j = neow3j;
    }

    /**
     * Creates a NeoURI from a NEP-9 URI String.
     *
     * @param uriString a NEP-9 URI String.
     * @return a NeoURI object
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
        neoURI.toAddress(beginTx[1]);

        // Add the optional parts of the uri - asset and amount.
        if (baseAndQuery.length == 2) {
            String[] query = baseAndQuery[1].split("&");
            for (String singleQuery : query) {
                String[] singleQueryParts = singleQuery
                        .split("=", 2);
                if (singleQueryParts.length != 2) {
                    throw new IllegalArgumentException("This uri contains invalid queries.");
                }
                if (singleQueryParts[0].equals("asset") &&
                        neoURI.asset == null) {
                    String assetID = singleQueryParts[1];
                    if (assetID.equals(NEO_ASSET)) {
                        neoURI.asset = NeoToken.SCRIPT_HASH;
                    } else if (assetID.equals(GAS_ASSET)) {
                        neoURI.asset = GasToken.SCRIPT_HASH;
                    } else {
                        neoURI.asset = new Hash160(assetID);
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
     * Creates a transaction script to transfer and initializes a
     * {@link TransactionBuilder} based on this script which is ready to be
     * signed and sent.
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
        if (toAddress == null) {
            throw new IllegalStateException("Recipient address is not set.");
        }
        if (wallet == null) {
            throw new IllegalStateException("Wallet is not set.");
        }
        if (amount == null) {
            throw new IllegalStateException("Amount is not set.");
        }

        BigInteger fractions;
        BigDecimal factor;
        if (isNeoToken(asset)) {
            factor = BigDecimal.TEN.pow(NeoToken.DECIMALS);
            fractions = amount.multiply(factor).toBigInteger();
        } else if (isGasToken(asset)) {
            factor = BigDecimal.TEN.pow(GasToken.DECIMALS);
            fractions = amount.multiply(factor).toBigInteger();
        } else {
            fractions = computeFractions(neow3j, asset);
        }

        Hash160 sender = wallet.getDefaultAccount().getScriptHash();
        return new SmartContract(asset, neow3j)
                .invokeFunction(TRANSFER_FUNCTION,
                        hash160(sender),
                        hash160(toAddress),
                        integer(fractions),
                        any(null))
                .wallet(wallet)
                .signers(calledByEntry(sender));
    }

    private boolean isNeoToken(Hash160 asset) {
        return asset.equals(NeoToken.SCRIPT_HASH);
    }

    private boolean isGasToken(Hash160 asset) {
        return asset.equals(GasToken.SCRIPT_HASH);
    }

    private BigInteger computeFractions(Neow3j neow3j, Hash160 asset)
            throws IOException {

        int decimals = new FungibleToken(asset, neow3j).getDecimals();
        BigDecimal factor = BigDecimal.TEN.pow(decimals);
        return amount.multiply(factor).toBigInteger();
    }

    /**
     * Sets the recipient address.
     *
     * @param address the recipient address.
     * @return this NeoURI object.
     */
    public NeoURI toAddress(String address) {
        if (!AddressUtils.isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid address used.");
        }

        toAddress = Hash160.fromAddress(address);
        return this;
    }

    /**
     * Sets the recipient address.
     *
     * @param address the recipient address.
     * @return this NeoURI object.
     */
    public NeoURI toAddress(Hash160 address) {
        toAddress = address;
        return this;
    }

    /**
     * Sets the asset.
     *
     * @param asset the asset script hash as big endian byte array.
     * @return this NeoURI object.
     */
    public NeoURI asset(byte[] asset) {
        this.asset = new Hash160(asset);
        return this;
    }

    /**
     * Sets the asset.
     *
     * @param asset the asset.
     * @return this NeoURI object.
     */
    public NeoURI asset(Hash160 asset) {
        this.asset = asset;
        return this;
    }

    /**
     * Sets the asset.
     *
     * @param asset the asset.
     * @return this NeoURI object.
     */
    public NeoURI asset(String asset) {
        if (asset.equals("neo")) {
            this.asset = NeoToken.SCRIPT_HASH;
        } else if (asset.equals("gas")) {
            this.asset = GasToken.SCRIPT_HASH;
        } else {
            this.asset = new Hash160(asset);
        }
        return this;
    }

    /**
     * Sets the amount.
     *
     * @param amount the amount.
     * @return this NeoURI object.
     */
    public NeoURI amount(String amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }

    /**
     * Sets the amount.
     *
     * @param amount the amount.
     * @return this NeoURI object.
     */
    public NeoURI amount(Integer amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }

    /**
     * Sets the amount.
     *
     * @param amount the amount.
     * @return this NeoURI object.
     */
    public NeoURI amount(BigInteger amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }

    /**
     * Sets the amount.
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
        if (asset != null) {
            if (asset.equals(NeoToken.SCRIPT_HASH)) {
                query.add("asset=neo");
            } else if (asset.equals(GasToken.SCRIPT_HASH)) {
                query.add("asset=gas");
            } else {
                query.add("asset=" + asset.toString());
            }
        }
        if (amount != null) {
            query.add("amount=" + amount.toString());
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
        if (toAddress == null) {
            throw new IllegalStateException(
                    "Could not create a NEP-9 URI without a recipient address.");
        }
        String basePart = NEO_SCHEME + ":" + toAddress.toAddress();
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
     * Gets the recipient address.
     *
     * @return the recipient address.
     */
    public String getToAddress() {
        return toAddress.toAddress();
    }

    /**
     * Gets the recipient address as script hash.
     *
     * @return the script hash of the recipient address.
     */
    public Hash160 getAddressAsScriptHash() {
        return toAddress;
    }

    /**
     * Gets the asset.
     *
     * @return the asset.
     */
    public Hash160 getAsset() {
        return asset;
    }

    /**
     * Gets the asset.
     *
     * @return the asset as string.
     */
    public String getAssetAsString() {
        if (asset.equals(NeoToken.SCRIPT_HASH)) {
            return NEO_ASSET;
        } else if (asset.equals(GasToken.SCRIPT_HASH)) {
            return GAS_ASSET;
        }
        return asset.toString();
    }

    /**
     * Gets the asset.
     *
     * @return the asset as address.
     */
    public String getAssetAsAddress() {
        return asset.toAddress();
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
