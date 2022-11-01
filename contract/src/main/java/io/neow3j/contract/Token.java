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

    // It is expected that token contracts return the total supply in fractions of their token. Therefore, an integer
    // is used here instead of a decimal number.
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
     * Gets the total supply of this token in fractions.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the total supply.
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
     * Gets the number of fractions that one unit of this token can be divided into.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the number of fractions.
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
     * Converts the token amount from a decimal point number to the amount in token fractions according to this
     * token's number of decimals.
     * <p>
     * Use this method to convert e.g. 1.5 GAS to its fraction value 150_000_000.
     *
     * @param amount the token amount in decimals.
     * @return the token amount in fractions.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger toFractions(BigDecimal amount) throws IOException {
        return toFractions(amount, getDecimals());
    }


    /**
     * Converts the token amount from a decimal point number to its amount in token fractions according to the
     * specified number of decimals.
     * <p>
     * Use this method to convert e.g. a token amount of 25.5 for a token with 4 decimals to 255_000.
     *
     * @param amount   the token amount.
     * @param decimals the token decimal points.
     * @return the token amount in fractions.
     */
    public static BigInteger toFractions(BigDecimal amount, int decimals) {
        if (amount.stripTrailingZeros().scale() > decimals) {
            throw new IllegalArgumentException("The provided amount has too many decimal points. Make sure the " +
                    "decimals of the provided amount do not exceed the supported token decimals.");
        }
        BigInteger factor = BigInteger.TEN.pow(decimals);
        BigDecimal fractions = amount.multiply(new BigDecimal(factor));
        return fractions.toBigInteger();
    }

    /**
     * Converts the token amount from token fractions to its decimal point value according to this token's number of
     * decimals.
     * <p>
     * Use this method to convert e.g. 600_000 GAS to its decimal value 0.006.
     *
     * @param amount the token amount in fractions.
     * @return the token amount in decimals.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigDecimal toDecimals(BigInteger amount) throws IOException {
        return toDecimals(amount, getDecimals());
    }

    /**
     * Converts the token amount from token fractions to its decimal point value according to the specified number of
     * decimals.
     * <p>
     * Use this method to convert e.g. 600_000 GAS to its decimal value 0.006.
     *
     * @param amount   the token amount in fractions.
     * @param decimals the token decimal points.
     * @return the token amount in decimals.
     */
    public static BigDecimal toDecimals(BigInteger amount, int decimals) {
        return new BigDecimal(amount, decimals);
    }

    // Resolves the text record of a NNS domain name and returns its script hash.
    protected Hash160 resolveNNSTextRecord(NNSName name) throws UnresolvableDomainNameException, IOException {
        String resolvedAddress = new NeoNameService(neow3j).resolve(name, RecordType.TXT);
        return Hash160.fromAddress(resolvedAddress);
    }

}
