package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Wrapper to interact with contracts that are compatible with the NEP-5 standard.
 */
public class Nep5 {

    private Neow3j neow3j;
    private ScriptHash scriptHash;

    private Nep5() {
    }

    private Nep5(Builder builder) {
        this.neow3j = builder.neow3j;
        this.scriptHash = builder.scriptHash;
    }

    /**
     * Gets the name of the token.
     *
     * @return the name of the token.
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the call to the node lead to an error. Not due to the
     *                                  contract invocation itself but due to the call in general.
     */
    public String name() throws IOException, ErrorResponseException {
        InvocationResult invocResultName = this.testInvoke("name", null);
        return streamResponse(invocResultName)
                .asByteArray()
                .getAsString();
    }

    /**
     * Gets the total supply of the token.
     *
     * @return the total token supply deployed in the system.
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the call to the node lead to an error. Not due to the
     *                                  contract invocation itself but due to the call in general.
     */
    public BigInteger totalSupply() throws IOException, ErrorResponseException {
        InvocationResult invocResultTotalSupply = this.testInvoke("totalSupply", null);
        return streamResponse(invocResultTotalSupply)
                .asByteArray()
                .getAsNumber();
    }

    /**
     * Gets the symbol of the token.
     *
     * @return the symbol of the token.
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the call to the node lead to an error. Not due to the
     *                                  contract invocation itself but due to the call in general.
     */
    public String symbol() throws IOException, ErrorResponseException {
        InvocationResult invocResultSymbol = this.testInvoke("symbol", null);
        return streamResponse(invocResultSymbol)
                 .asByteArray()
                 .getAsString();
    }

    /**
     * Gets the number of decimals used by the token.
     *
     * @return the number of decimals used by the token.
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the call to the node lead to an error. Not due to the
     *                                  contract invocation itself but due to the call in general.
     */
    public BigInteger decimals() throws IOException, ErrorResponseException {
        InvocationResult invocResultDecimals = this.testInvoke("decimals", null);
        return streamResponse(invocResultDecimals)
                .asInteger()
                .getValue();
    }

    /**
     * Gets the token balance of an account.
     *
     * @param account the account of which the balance is checked.
     * @return the token balance of the account.
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the call to the node lead to an error. Not due to the
     *                                  contract invocation itself but due to the call in general.
     */
    public BigInteger balanceOf(ScriptHash account) throws IOException, ErrorResponseException {
        InvocationResult invocResultBalanceOf = this.testInvoke("balanceOf",
                ContractParameter.byteArrayFromAddress(account.toAddress()));
        return streamResponse(invocResultBalanceOf)
                .asByteArray()
                .getAsNumber();
    }

    /**
     * Gets the stack from the response and streams it to get the first Entry of the stack.
     *
     * @param invocResult the response {@link InvocationResult} from an RPC call.
     * @return the first item of the stack.
     */
    private StackItem streamResponse(InvocationResult invocResult) {
        return Optional.ofNullable(invocResult.getStack())
                .orElseGet(Collections::emptyList)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Stack is null or empty in the response."));
    }

    /**
     * Transfers an amount of tokens from one account to another.
     *
     * @param from the account, that sends the tokens.
     * @param to the address, represented as a {@link ScriptHash}, that receives the tokens.
     * @param amount the amount of tokens, that is transferred.
     * @return a boolean, if the transfer was successfully processed.
     * @throws IOException            if a connection problem with the RPC node arises.
     * @throws ErrorResponseException if the execution of the invocation lead to an error on the RPC
     *                                node.
     */
    public Boolean transfer(Account from, ScriptHash to, BigInteger amount) throws IOException, ErrorResponseException {
        if (amount.intValue() < 0) {
            throw new IllegalArgumentException("Transfer amount has to be greater than zero.");
        }

        return invokeTransfer(from, to, amount).getResponse().getResult();
    }

    /**
     * Transfers an amount of tokens from one account to another.
     * This method takes the parameter amount as a String, which may be more convenient for developers.
     *
     * @param from the account, that sends the tokens.
     * @param to the address, represented as a {@link ScriptHash}, that receives the tokens.
     * @param amountAsString the amount of tokens, that is transferred.
     * @return a boolean, if the transfer was successfully processed.
     * @throws IOException            if a connection problem with the RPC node arises.
     * @throws ErrorResponseException if the execution of the invocation lead to an error on the RPC
     *                                node.
     */
    public Boolean transfer(Account from, ScriptHash to, String amountAsString) throws IOException, ErrorResponseException {
        BigInteger amount = new BigInteger(amountAsString);
        if (amount.signum() == -1) {
            throw new IllegalArgumentException("Transfer amount has to be greater than zero.");
        }

        return invokeTransfer(from, to, amount).getResponse().getResult();
    }

    /**
     * Invokes the transfer method in the smart contract.
     *
     * @param from the account, that sends the tokens.
     * @param to the address, represented as a {@link ScriptHash}, that receives the tokens.
     * @param amount the amount of tokens, that is transferred.
     * @return an invocation object {@link ContractInvocation}.
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the execution of the invocation lead to an error on the RPC
     *                                  node.
     */
    private ContractInvocation invokeTransfer(Account from, ScriptHash to, BigInteger amount) throws IOException, ErrorResponseException {
        List<ContractParameter> params = new ArrayList<>();
        params.add(ContractParameter.byteArrayFromAddress(from.getAddress()));
        params.add(ContractParameter.byteArrayFromAddress(to.toAddress()));
        params.add(ContractParameter.integer(amount));

        return new ContractInvocation.Builder(neow3j)
                .contractScriptHash(scriptHash)
                .function("transfer")
                .parameters(params)
                .account(from)
                .build()
                .sign()
                .invoke();
    }

    /**
     * Tests the contract invocation. Doing this does not affect the blockchain's state.
     * This method is useful for requests that only want to retrieve the current state of the blockchain.
     *
     * @param method the NEP5 method.
     * @param param the additional NEP5 parameters used for the invocation.
     * @return the result of the invocation.
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the call to the node lead to an error. Not due to the
     *                                  contract invocation itself but due to the call in general.
     */
    private InvocationResult testInvoke(String method, ContractParameter param) throws IOException, ErrorResponseException {
        ContractInvocation.Builder builder = new ContractInvocation.Builder(neow3j)
                .contractScriptHash(scriptHash)
                .function(method);

        if (param != null) {
            return builder.parameter(param).build().testInvoke();
        } else {
            return builder.build().testInvoke();
        }
    }

    public static class Builder {
        private Neow3j neow3j;
        private ScriptHash scriptHash;

        /**
         * Constructor of the Builder class of this NEP5 object.
         *
         * @param neow3j The neow3j object used in this NEP5 object.
         */
        public Builder(Neow3j neow3j) {
            this.neow3j = neow3j;
        }

        /**
         * Sets the script hash of this NEP5 object. It specifies the NEP5 contract.
         *
         * @param contractScriptHash The contract script hash.
         * @return this Builder object.
         */
        public Builder fromContract(ScriptHash contractScriptHash) {
            this.scriptHash = contractScriptHash;
            return this;
        }

        /**
         * Builds the NEP5 contract object. It collects the necessary inputs (the neow3j object and the contract's
         * script hash).
         *
         * @return The constructed NEP5 object.
         */
        public Nep5 build() {
            if (neow3j == null) throw new IllegalStateException("Neow3j not set");
            if (scriptHash == null) throw new IllegalStateException("Contract script hash not set");
            return new Nep5(this);
        }
    }

}
