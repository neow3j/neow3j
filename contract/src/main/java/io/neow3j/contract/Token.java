package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.contract.exceptions.UnresolvableDomainNameException;
import io.neow3j.contract.types.NNSName;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.types.Hash160;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Represents a token wrapper class that contains shared methods for the fungible NEP-17 and non-fungible NEP-11 token
 * standards.
 */
public class Token extends SmartContract {

    private static final String TOTAL_SUPPLY = "totalSupply";

    private static final String SYMBOL = "symbol";

    private static final String DECIMALS = "decimals";

    // Token contracts return the total supply as an integer count of base units.
    private BigInteger totalSupply;

    private Integer decimals;

    private String symbol;

    public Token(Hash160 scriptHash, Neow3j neow3j) {
        super(scriptHash, neow3j);
    }

    /**
     * Gets the symbol of this token.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the symbol.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something interpretable as a
     *                                       string.
     */
    public String getSymbol() throws IOException, UnexpectedReturnTypeException {
        if (symbol == null) {
            symbol = callFunctionReturningString(SYMBOL);
        }
        return symbol;
    }

    /**
     * Gets the total supply of this token in base units.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the total supply in base units.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something interpretable as a
     *                                       number.
     */
    public BigInteger getTotalSupply() throws IOException, UnexpectedReturnTypeException {
        if (totalSupply == null) {
            totalSupply = callFunctionReturningInt(TOTAL_SUPPLY);
        }
        return totalSupply;
    }

    /**
     * Gets the number of decimal places supported by this token. For example, a token with 2 decimal places has
     * 100 base units per whole token.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the number of decimal places.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something interpretable as a
     *                                       number.
     */
    public int getDecimals() throws IOException, UnexpectedReturnTypeException {
        if (decimals == null) {
            decimals = callFunctionReturningInt(DECIMALS).intValue();
        }
        return decimals;
    }

    /**
     * Converts a decimal token amount to base units using this token's number of decimal places.
     * <p>
     * For example, 1.5 GAS is 150_000_000 base units.
     *
     * @param amount the decimal token amount.
     * @return the token amount in base units.
     * @throws IllegalArgumentException if the amount requires more decimal places than the token supports.
     * @throws IOException              if there was a problem fetching information from the Neo node.
     * @since 3.25.0
     */
    public BigInteger toBaseUnits(BigDecimal amount) throws IOException {
        return toBaseUnits(amount, getDecimals());
    }

    /**
     * Converts a decimal token amount to base units using the specified number of decimal places.
     * <p>
     * For example, 25.5 tokens with 4 decimal places is 255_000 base units.
     *
     * @param amount   the decimal token amount.
     * @param decimals the number of decimal places supported by the token.
     * @return the token amount in base units.
     * @throws IllegalArgumentException if the amount requires more decimal places than the token supports.
     * @since 3.25.0
     */
    public static BigInteger toBaseUnits(BigDecimal amount, int decimals) {
        if (amount.stripTrailingZeros().scale() > decimals) {
            throw new IllegalArgumentException("The provided amount has too many decimal points. Make sure the " +
                    "decimals of the provided amount do not exceed the supported token decimals.");
        }
        BigInteger factor = BigInteger.TEN.pow(decimals);
        return amount.multiply(new BigDecimal(factor)).toBigInteger();
    }

    /**
     * Converts a token amount in base units to a decimal amount using this token's number of decimal places.
     * <p>
     * For example, 600_000 base units of GAS is 0.006 GAS.
     *
     * @param amount the token amount in base units.
     * @return the decimal token amount.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @since 3.25.0
     */
    public BigDecimal fromBaseUnits(BigInteger amount) throws IOException {
        return fromBaseUnits(amount, getDecimals());
    }

    /**
     * Converts a token amount in base units to a decimal amount using the specified number of decimal places.
     * <p>
     * For example, 255_000 base units with 4 decimal places is 25.5000 tokens.
     *
     * @param amount   the token amount in base units.
     * @param decimals the number of decimal places supported by the token.
     * @return the decimal token amount.
     * @since 3.25.0
     */
    public static BigDecimal fromBaseUnits(BigInteger amount, int decimals) {
        return new BigDecimal(amount, decimals);
    }

    /**
     * Converts a decimal token amount to base units using this token's number of decimal places.
     *
     * @param amount the decimal token amount.
     * @return the token amount in base units.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @deprecated Since 3.25.0, use {@link #toBaseUnits(BigDecimal)}. Scheduled for removal in 3.26.0.
     */
    @Deprecated
    public BigInteger toFractions(BigDecimal amount) throws IOException {
        return Token.toBaseUnits(amount, getDecimals());
    }

    /**
     * Converts a decimal token amount to base units using the specified number of decimal places.
     *
     * @param amount   the decimal token amount.
     * @param decimals the number of decimal places supported by the token.
     * @return the token amount in base units.
     * @deprecated Since 3.25.0, use {@link #toBaseUnits(BigDecimal, int)}. Scheduled for removal in 3.26.0.
     */
    @Deprecated
    public static BigInteger toFractions(BigDecimal amount, int decimals) {
        return toBaseUnits(amount, decimals);
    }

    /**
     * Converts a token amount in base units to a decimal amount using this token's number of decimal places.
     *
     * @param amount the token amount in base units.
     * @return the decimal token amount.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @deprecated Since 3.25.0, use {@link #fromBaseUnits(BigInteger)}. Scheduled for removal in 3.26.0.
     */
    @Deprecated
    public BigDecimal toDecimals(BigInteger amount) throws IOException {
        return Token.fromBaseUnits(amount, getDecimals());
    }

    /**
     * Converts a token amount in base units to a decimal amount using the specified number of decimal places.
     *
     * @param amount   the token amount in base units.
     * @param decimals the number of decimal places supported by the token.
     * @return the decimal token amount.
     * @deprecated Since 3.25.0, use {@link #fromBaseUnits(BigInteger, int)}. Scheduled for removal in 3.26.0.
     */
    @Deprecated
    public static BigDecimal toDecimals(BigInteger amount, int decimals) {
        return fromBaseUnits(amount, decimals);
    }

    // Resolves the text record of a NNS domain name and returns its script hash.
    protected Hash160 resolveNNSTextRecord(NNSName name) throws UnresolvableDomainNameException, IOException {
        String resolvedAddress = new NeoNameService(neow3j).resolve(name, RecordType.TXT);
        return Hash160.fromAddress(resolvedAddress);
    }

}
