package io.neow3j.contract;

import io.neow3j.contract.Invocation.InvocationBuilder;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.StackItem;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Represents a smart contract on the Neo blockchain and provides methods to invoke it.
 */
public class SmartContract {

    protected ScriptHash scriptHash;
    protected Neow3j neow;

    /**
     * Constructs a <tt>SmartContract</tt> representing the smart contract with the given script
     * hash. Uses the given {@link Neow3j} instance for all interactions with the smart contract.
     * @param scriptHash The smart contract's script hash.
     * @param neow The {@link Neow3j} instance to use for invocations.
     */
    public SmartContract(ScriptHash scriptHash, Neow3j neow) {
        if (scriptHash == null) {
            throw new IllegalArgumentException("The contract script hash must not be null.");
        }
        if (neow == null) {
            throw new IllegalArgumentException("The Neow3j object must not be null.");
        }
        this.scriptHash = scriptHash;
        this.neow = neow;
    }

    /**
     * Initializes an invocation of the given function on this contract.
     *
     * @param function The function to invoke.
     * @return An {@link InvocationBuilder} allowing to set further details of the invocation.
     */
    public InvocationBuilder invoke(String function) {
        if (function == null || function.isEmpty()) {
            throw new IllegalArgumentException(
                    "The invocation function must not be null or empty.");
        }
        return new InvocationBuilder(neow, scriptHash, function);
    }

    /**
     * Does an {@code invokefunction} call to the given contract function expecting a String as
     * return type.
     *
     * @param function The function to call.
     * @param params   The contract parameters to include in the call.
     * @return the string returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as a
     *                                       String.
     */
    public String callFuncReturningString(String function, ContractParameter... params)
            throws UnexpectedReturnTypeException, IOException {

        StackItem item = callFunction(function, params);
        if (item.getType().equals(StackItemType.BYTE_STRING)) {
            return item.asByteString().getAsString();
        }
        throw new UnexpectedReturnTypeException(item.getType(), StackItemType.BYTE_STRING);
    }

    /**
     * Does an {@code invokefunction} call to the given contract function expecting an integer as
     * return type.
     *
     * @param function The function to call.
     * @param params   The contract parameters to include in the call.
     * @return the integer returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as an
     *                                       integer.
     */
    public BigInteger callFuncReturningInt(String function, ContractParameter... params)
            throws IOException, UnexpectedReturnTypeException {

        StackItem item = callFunction(function, params);
        if (item.getType().equals(StackItemType.INTEGER)) {
            return item.asInteger().getValue();
        }
        if (item.getType().equals(StackItemType.BYTE_STRING)) {
            return item.asByteString().getAsNumber();
        }
        throw new UnexpectedReturnTypeException(item.getType(), StackItemType.INTEGER,
                StackItemType.BYTE_STRING);
    }


    protected StackItem callFunction(String function, ContractParameter... params)
            throws IOException {

        if (params.length > 0) {
            return invoke(function).withParameters(params).call()
                    .getInvocationResult().getStack().get(0);
        } else {
            return invoke(function).call().getInvocationResult().getStack().get(0);
        }
    }


}
