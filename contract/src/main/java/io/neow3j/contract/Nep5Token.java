package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.exceptions.InsufficientFundsException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Represents a NEP-5 token contract.
 * <p>
 * The first time that the getters are used, RPC calls need to be made to fetch the information from
 * a Neo node.
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

    public Nep5Token(ScriptHash scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    /**
     * Gets the name of this token.
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
     * Gets the token balance for the given script hash in fractions.
     * <p>
     * The balance is not saved locally. Calling this method multiple times for the same script hash
     * causes a new RPC call in every invocation.
     *
     * @param scriptHash The script hash to fetch the balance for.
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
     * Creates and sends a transfer transaction.
     * <p>
     * Currently only the wallet's default account is used to cover the token amount.
     *
     * @param wallet The wallet from which to send the tokens from.
     * @param to     The script hash of the receiver.
     * @param amount The amount to transfer as a decimal number, i.e. not in fractions but token
     *               units.
     * @return The transaction hash.
     * @throws ErrorResponseException
     * @throws IOException            if there was a problem fetching information from the Neo
     *                                node.
     */
    public String transfer(Wallet wallet, ScriptHash to, BigDecimal amount)
            throws ErrorResponseException, IOException {

        Account acc = wallet.getDefaultAccount();
        BigDecimal factor = BigDecimal.TEN.pow(getDecimals());
        BigInteger fractions = amount.multiply(factor).toBigInteger();
        // TODO: Extend balance checking to other accounts in the wallet.
        // TODO: Move balance checking to the Wallet and Accounts.
        BigInteger defaultAccBalance = getBalanceOf(acc.getScriptHash());
        if (defaultAccBalance.compareTo(fractions) < 0) {
            throw new InsufficientFundsException("Default account does not hold enough tokens. "
                    + "Transfer amount is " + fractions.toString() + " but account only holds "
                    + defaultAccBalance.toString() + " (in token fractions).");
        }
        return invoke(NEP5_TRANSFER)
                .withWallet(wallet)
                .withParameters(
                        ContractParameter.byteArrayFromAddress(acc.getAddress()),
                        ContractParameter.byteArrayFromAddress(to.toAddress()),
                        ContractParameter.integer(fractions)
                )
                .failOnFalse()
                .build()
                .sign()
                .send();
    }

    private String callFuncReturningString(String function)
            throws UnexpectedReturnTypeException, IOException {

        StackItem item = invoke(function).run().getInvocationResult().getStack().get(0);
        if (item.getType().equals(StackItemType.BYTE_ARRAY)) {
            return item.asByteArray().getAsString();
        }
        throw new UnexpectedReturnTypeException(item.getType(), StackItemType.BYTE_ARRAY);
    }

    protected BigInteger callFuncReturningInt(String function, ContractParameter... params)
            throws IOException, UnexpectedReturnTypeException {

        StackItem item;
        if (params.length > 0) {
            item = invoke(function).run().getInvocationResult().getStack().get(0);
        } else {
            item = invoke(function).withParameters(params).run()
                    .getInvocationResult().getStack().get(0);
        }
        if (item.getType().equals(StackItemType.INTEGER)) {
            return item.asInteger().getValue();
        }
        if (item.getType().equals(StackItemType.BYTE_ARRAY)) {
            return item.asByteArray().getAsNumber();
        }
        throw new UnexpectedReturnTypeException(item.getType(), StackItemType.INTEGER,
                StackItemType.BYTE_ARRAY);
    }

    private BigInteger callFuncReturningInt(String function)
            throws UnexpectedReturnTypeException, IOException {

        return callFuncReturningInt(function, new ContractParameter[]{});
    }
}
