package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Represents a token wrapper class that contains shared methods for the fungible Nep17 and
 * non-fungible Nep11 token standards.
 */
public class Token extends SmartContract {

    private static final String TOTAL_SUPPLY = "totalSupply";
    private static final String SYMBOL = "symbol";
    private static final String DECIMALS = "decimals";

    // It is expected that token contracts return the total supply in fractions
    // of their token. Therefore, an integer is used here instead of a decimal
    // number.
    private BigInteger totalSupply;
    private Integer decimals;
    private String symbol;

    public Token(Hash160 scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    /**
     * Gets the symbol of this token.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the symbol.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a string.
     */
    public String getSymbol() throws IOException,
            UnexpectedReturnTypeException {
        if (symbol == null) {
            symbol = callFuncReturningString(SYMBOL);
        }
        return symbol;
    }

    /**
     * Gets the total supply of this token in fractions.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the total supply.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getTotalSupply() throws IOException, UnexpectedReturnTypeException {
        if (totalSupply == null) {
            totalSupply = callFuncReturningInt(TOTAL_SUPPLY);
        }
        return totalSupply;
    }

    /**
     * Gets the number of fractions that one unit of this token can be divided into.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the the number of fractions.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public int getDecimals() throws IOException, UnexpectedReturnTypeException {
        if (decimals == null) {
            decimals = callFuncReturningInt(DECIMALS).intValue();
        }
        return decimals;
    }

    /**
     * Gets the {@code amount} in fractions of the token.
     *
     * @param amount the amount.
     * @return the amount in fractions.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    protected BigInteger getAmountAsBigInteger(BigDecimal amount)
            throws IOException {
        BigDecimal factor = BigDecimal.TEN.pow(getDecimals());
        return amount.multiply(factor).toBigInteger();
    }

    /**
     * Gets the {@code amount} of the token as a decimal number (not token fractions).
     *
     * @param amount the amount.
     * @return the amount as a decimal number (not token fractions).
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    protected BigDecimal getAmountAsBigDecimal(BigInteger amount)
            throws IOException {
        BigDecimal a = new BigDecimal(amount);
        BigDecimal divisor = BigDecimal.TEN.pow(getDecimals());
        return a.divide(divisor);
    }

}
