package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.NeoInvoke;
import io.neow3j.protocol.core.methods.response.Script;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Nep5 {

    private Neow3j neow3j;
    private ScriptHash fromContractScriptHash;

    private Nep5() {
    }

    private Nep5(Builder builder) {
        this.neow3j = builder.neow3j;
        this.fromContractScriptHash = builder.fromContractScriptHash;
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
        return this.testInvoke("name", null).getStack().get(0).asByteArray().getAsString();
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
        return this.testInvoke("totalSupply", null).getStack().get(0).asByteArray().getAsNumber();
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
        return this.testInvoke("symbol", null).getStack().get(0).asByteArray().getAsString();
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
        return this.testInvoke("decimals", null).getStack().get(0).asInteger().getValue();
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
        return this.testInvoke("balanceOf", ContractParameter.byteArrayFromAddress(account.toAddress())).getStack().get(0).asByteArray().getAsNumber();
    }

    /**
     * Transfers an amount of tokens from one account to another.
     *
     * @param from the account, that sends the tokens.
     * @param to the account, that receives the tokens.
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
        ContractParameter fromParam = ContractParameter.byteArrayFromAddress(from.getAddress());
        ContractParameter toParam = ContractParameter.byteArrayFromAddress(to.toAddress());
        ContractParameter amountParam = ContractParameter.integer(amount);
        List<ContractParameter> params = new ArrayList<>();
        params.add(fromParam);
        params.add(toParam);
        params.add(amountParam);

        ContractInvocation invoc = new ContractInvocation.Builder(neow3j)
                .contractScriptHash(fromContractScriptHash)
                .function("transfer")
                .parameters(params)
                .account(from)
                .build()
                .sign()
                .invoke();

        // TODO: 11.03.20 Michael
        //  read invoc to identify successful invocation

        return false;
    }

    /**
     * Tests the contract invocation. Doing this does not affect the blockchain's state.
     * This method is useful for requests, that only want to retrieve the current state of the blockchain.
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
                .contractScriptHash(fromContractScriptHash)
                .function(method);

        if (param != null) {
            return builder.parameter(param).build().testInvoke();
        } else {
            return builder.build().testInvoke();
        }
    }

    public static class Builder {
        private Neow3j neow3j;
        private ScriptHash fromContractScriptHash;
        public Builder(Neow3j neow3j) {
            this.neow3j = neow3j;
        }
        public Nep5.Builder fromContract(ScriptHash contractScriptHash) {
            this.fromContractScriptHash = contractScriptHash;
            return this;
        }

        public Nep5 build() {
            Optional.ofNullable(neow3j).orElseThrow(() -> new IllegalStateException("Neow3j not set."));
            Optional.ofNullable(fromContractScriptHash).orElseThrow(() -> new IllegalStateException("Contract not set."));
            return new Nep5(this);
        }
    }

}
