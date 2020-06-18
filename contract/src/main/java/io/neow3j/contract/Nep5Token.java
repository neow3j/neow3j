package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.exceptions.InsufficientFundsException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Represents a NEP-5 token contract and provides methods to invoke it.
 */
public class Nep5Token extends SmartContract {

    private static final String NEP5_NAME = "name";
    private static final String NEP5_TOTAL_SUPPLY = "totalSupply";
    private static final String NEP5_SYMBOL = "symbol";
    private static final String NEP5_DECIMALS = "decimals";
    private static final String NEP5_BALANCE_OF = "balanceOf";
    private static final String NEP5_TRANSFER = "transfer";

    private String name;
    // It is expected that Nep5 contracts return the total supply in fractions of their token.
    // Therefore an integer is used here instead of a decimal number.
    private BigInteger totalSupply;
    private Integer decimals;
    private String symbol;

    /**
     * Constructs a new <tt>Nep5Token</tt> representing the token contract with the given script
     * hash. Uses the given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash The token contract's script hash
     * @param neow       The {@link Neow3j} instance to use for invocations.
     */
    public Nep5Token(ScriptHash scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    /**
     * Gets the name of this token.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the name.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a string.
     */
    public String getName() throws IOException, UnexpectedReturnTypeException {
        if (this.name == null) {
            this.name = callFuncReturningString(NEP5_NAME);
        }
        return this.name;
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
    public String getSymbol() throws IOException, UnexpectedReturnTypeException {
        if (this.symbol == null) {
            this.symbol = callFuncReturningString(NEP5_SYMBOL);
        }
        return this.symbol;
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
        if (this.totalSupply == null) {
            this.totalSupply = callFuncReturningInt(NEP5_TOTAL_SUPPLY);
        }
        return this.totalSupply;
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
        if (this.decimals == null) {
            this.decimals = callFuncReturningInt(NEP5_DECIMALS).intValue();
        }
        return this.decimals;
    }

    /**
     * Gets the token balance for the given account script hash.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are send to the
     * neo-node.
     *
     * @param scriptHash The script hash of the account to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getBalanceOf(ScriptHash scriptHash) throws IOException,
            UnexpectedReturnTypeException {

        ContractParameter ofParam = ContractParameter.hash160(scriptHash);
        return callFuncReturningInt(NEP5_BALANCE_OF, ofParam);
    }

    /**
     * Gets the token balance for the given wallet, i.e., all accounts in the wallet.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are send to the
     * neo-node.
     *
     * @param wallet The wallet to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getBalanceOf(Wallet wallet) throws IOException,
            UnexpectedReturnTypeException {

        BigInteger sum = BigInteger.ZERO;
        for (Account a : wallet.getAccounts()) {
            sum = sum.add(getBalanceOf(a.getScriptHash()));
        }
        return sum;
    }

    /**
     * Creates and sends a transfer transaction.
     * <p>
     * Currently only the wallet's default account is used to cover the token amount.
     *
     * @param wallet The wallet from which to send the tokens from.
     * @param to     The script hash of the receiver.
     * @param amount The amount to transfer as a decimal number (not token fractions).
     * @return The transaction id.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NeoSendRawTransaction transfer(Wallet wallet, ScriptHash to, BigDecimal amount)
            throws IOException {

        return buildTransferInvocation(wallet, to, amount).send();
    }

    // Method extracted for testability.
    Invocation buildTransferInvocation(Wallet wallet, ScriptHash to, BigDecimal amount)
            throws IOException {

        Account acc = wallet.getDefaultAccount();
        BigDecimal factor = BigDecimal.TEN.pow(getDecimals());
        BigInteger fractions = amount.multiply(factor).toBigInteger();
        BigInteger accBalance = getBalanceOf(acc.getScriptHash());
        if (accBalance.compareTo(fractions) < 0) {
            throw new InsufficientFundsException("The wallet's default account does not hold enough"
                    + " tokens. Transfer amount is " + fractions.toString() + " but account"
                    + " only holds " + accBalance.toString() + " (in token fractions).");
        }
        return invoke(NEP5_TRANSFER)
                .withWallet(wallet)
                .withParameters(
                        ContractParameter.hash160(acc.getScriptHash()),
                        ContractParameter.hash160(to),
                        ContractParameter.integer(fractions)
                )
                .failOnFalse()
                .build()
                .sign();
    }

    public String transferFromURI(Wallet wallet, NeoURI neoURI) throws IOException, ErrorResponseException {
        ScriptHash to = neoURI.getAddress();
        BigDecimal amount = neoURI.getAmount();
        ScriptHash asset = neoURI.getAsset();

        if (asset == null || amount == null) {
            return "";
        }

        if (this.scriptHash.equals(asset)) {
            return transfer(wallet, to, amount);
        }

        return "";
    }
}
