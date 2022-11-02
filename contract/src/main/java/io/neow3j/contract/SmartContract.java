package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.ContractState;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.InteropInterfaceStackItem;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.exceptions.InvocationFaultStateException;
import io.neow3j.protocol.exceptions.StackItemCastException;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.Strings;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static io.neow3j.types.StackItemType.BOOLEAN;
import static io.neow3j.types.StackItemType.BUFFER;
import static io.neow3j.types.StackItemType.BYTE_STRING;
import static io.neow3j.types.StackItemType.INTEGER;
import static io.neow3j.types.StackItemType.INTEROP_INTERFACE;
import static io.neow3j.utils.Numeric.reverseHexString;
import static io.neow3j.utils.Numeric.toHexString;
import static java.util.Arrays.asList;

/**
 * Represents a smart contract on the Neo blockchain and provides methods to invoke and deploy it.
 */
@SuppressWarnings("unchecked")
public class SmartContract {

    protected Hash160 scriptHash;
    protected Neow3j neow3j;

    protected static final int DEFAULT_ITERATOR_COUNT = 100;

    /**
     * Constructs a {@code SmartContract} representing the smart contract with the given script hash. Uses the given
     * {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash the smart contract's script hash.
     * @param neow3j     the {@link Neow3j} instance to use for invocations.
     */
    public SmartContract(Hash160 scriptHash, Neow3j neow3j) {
        if (scriptHash == null) {
            throw new IllegalArgumentException("The contract script hash must not be null.");
        }
        if (neow3j == null) {
            throw new IllegalArgumentException("The Neow3j object must not be null.");
        }
        this.scriptHash = scriptHash;
        this.neow3j = neow3j;
    }

    /**
     * Initializes a {@link TransactionBuilder} for an invocation of this contract with the provided function and
     * parameters. The order of the parameters is relevant.
     *
     * @param function the function to invoke.
     * @param params   the parameters to pass with the invocation.
     * @return a {@link TransactionBuilder} allowing to set further details of the invocation.
     */
    public TransactionBuilder invokeFunction(String function, ContractParameter... params) {
        byte[] script = buildInvokeFunctionScript(function, params);
        return new TransactionBuilder(neow3j).script(script);
    }

    /**
     * Builds a script to invoke a function on this smart contract.
     *
     * @param function the function to invoke.
     * @param params   the parameters to pass to the function.
     * @return the script.
     */
    public byte[] buildInvokeFunctionScript(String function, ContractParameter... params) {
        if (Strings.isEmpty(function)) {
            throw new IllegalArgumentException("The invocation function must not be null or empty.");
        }
        return new ScriptBuilder().contractCall(scriptHash, function, asList(params)).toArray();
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting a String as return type.
     *
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @return the string returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as a String.
     * @throws InvocationFaultStateException if the invocation results in a fault VM state.
     */
    public String callFunctionReturningString(String function, ContractParameter... params)
            throws UnexpectedReturnTypeException, IOException {

        InvocationResult invocationResult = callInvokeFunction(function, asList(params)).getInvocationResult();
        throwIfFaultState(invocationResult);
        StackItem item = invocationResult.getStack().get(0);
        StackItemType type = item.getType();
        if (type.equals(BYTE_STRING)) {
            return item.getString();
        }
        throw new UnexpectedReturnTypeException(type, BYTE_STRING);
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting an Integer as return type.
     *
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @return the integer returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as an integer.
     * @throws InvocationFaultStateException if the invocation results in a fault VM state.
     */
    public BigInteger callFunctionReturningInt(String function, ContractParameter... params) throws IOException,
            UnexpectedReturnTypeException {

        InvocationResult invocationResult;
        if (params.length == 0) {
            invocationResult = callInvokeFunction(function).getInvocationResult();
        } else {
            invocationResult = callInvokeFunction(function, asList(params)).getInvocationResult();
        }
        throwIfFaultState(invocationResult);
        StackItem item = invocationResult.getStack().get(0);
        if (item.getType().equals(INTEGER)) {
            return item.getInteger();
        }
        throw new UnexpectedReturnTypeException(item.getType(), INTEGER);
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting a Boolean as return type.
     *
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @return the boolean returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as a boolean.
     * @throws InvocationFaultStateException if the invocation results in a fault VM state.
     */
    public boolean callFunctionReturningBool(String function, ContractParameter... params) throws IOException,
            UnexpectedReturnTypeException {

        InvocationResult invocationResult;
        if (params.length == 0) {
            invocationResult = callInvokeFunction(function).getInvocationResult();
        } else {
            invocationResult = callInvokeFunction(function, asList(params)).getInvocationResult();
        }
        throwIfFaultState(invocationResult);
        StackItem item = invocationResult.getStack().get(0);
        StackItemType type = item.getType();
        if (type.equals(BOOLEAN) ||
                type.equals(INTEGER) ||
                type.equals(BYTE_STRING) ||
                type.equals(BUFFER)) {
            return item.getBoolean();
        }
        throw new UnexpectedReturnTypeException(type, BOOLEAN);
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting a script hash as the return
     * type.
     *
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @return the script hash returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as script hash.
     * @throws InvocationFaultStateException if the invocation results in a fault VM state.
     */
    public Hash160 callFunctionReturningScriptHash(String function, ContractParameter... params) throws IOException {
        InvocationResult result = callInvokeFunction(function, asList(params)).getInvocationResult();
        throwIfFaultState(result);
        StackItem stackItem = result.getStack().get(0);
        return extractScriptHash(stackItem);
    }

    private Hash160 extractScriptHash(StackItem item) {
        if (!item.getType().equals(BYTE_STRING)) {
            throw new UnexpectedReturnTypeException(item.getType(), BYTE_STRING);
        }
        try {
            return new Hash160(reverseHexString(item.getHexString()));
        } catch (StackItemCastException | IllegalArgumentException e) {
            throw new UnexpectedReturnTypeException("Return type did not contain script hash in expected format.", e);
        }
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting an
     * {@link InteropInterfaceStackItem} as a return type.
     * <p>
     * Traverse the returned iterator with {@link Iterator#traverse(int)} to retrieve the iterator items.
     * <p>
     * In order to traverse the returned iterator, sessions need to be enabled on the Neo node. If sessions are
     * disabled on the Neo node, use {@link SmartContract#callFunctionAndUnwrapIterator(String, List, int, Signer...)}.
     *
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @return the iterator.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @throws InvocationFaultStateException if the invocation results in a fault VM state.
     */
    public Iterator<StackItem> callFunctionReturningIterator(String function, ContractParameter... params)
            throws IOException {
        return callFunctionReturningIterator(i -> i, function, params);
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting an
     * {@link InteropInterfaceStackItem} as a return type.
     * <p>
     * Traverse the returned iterator with {@link Iterator#traverse(int)} to retrieve the iterator items with
     * provided mapper applied to each item.
     * <p>
     * In order to traverse the returned iterator, sessions need to be enabled on the Neo node. If sessions are
     * disabled on the Neo node, use {@link SmartContract#callFunctionAndUnwrapIterator(String, List, int, Signer...)}.
     *
     * @param mapper   the function to apply on the stack items in the iterator.
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @param <T>      the type the stack items are mapped to by the mapper function when traversing the iterator.
     * @return the iterator.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @throws InvocationFaultStateException if the invocation results in a fault VM state.
     */
    public <T> Iterator<T> callFunctionReturningIterator(Function<StackItem, T> mapper, String function,
            ContractParameter... params) throws IOException {

        InvocationResult invocationResult = callInvokeFunction(function, asList(params)).getInvocationResult();
        throwIfFaultState(invocationResult);

        StackItem stackItem = invocationResult.getStack().get(0);
        if (!stackItem.getType().equals(INTEROP_INTERFACE)) {
            throw new UnexpectedReturnTypeException(stackItem.getType(), INTEROP_INTERFACE);
        }

        String sessionId = invocationResult.getSessionId();
        String iteratorId = stackItem.getIteratorId();
        return new Iterator<>(neow3j, sessionId, iteratorId, mapper);
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting an
     * {@link InteropInterfaceStackItem} as a return type. Then, traverses the iterator to retrieve the first
     * {@link SmartContract#DEFAULT_ITERATOR_COUNT} stack items mapped with the provided mapper function.
     * <p>
     * Consider that the returned list might be limited in size and not reveal all entries that exist in the iterator.
     * <p>
     * This method requires sessions to be enabled on the Neo node. If sessions are disabled on the Neo node, use
     * {@link SmartContract#callFunctionAndUnwrapIterator(String, List, int, Signer...)}.
     *
     * @param mapper   the function to apply on the stack items in the iterator.
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @param <T>      the type the stack items are mapped to by the mapper function.
     * @return the mapped iterator items.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @throws InvocationFaultStateException if the invocation results in a fault VM state.
     */
    public <T> List<T> callFunctionAndTraverseIterator(Function<StackItem, T> mapper, String function,
            ContractParameter... params) throws IOException {
        return callFunctionAndTraverseIterator(mapper, function, DEFAULT_ITERATOR_COUNT, params);
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting an
     * {@link InteropInterfaceStackItem} as a return type. Then, traverses the iterator to retrieve the first
     * {@code maxIteratorResultItems} stack items mapped with the provided mapper function.
     * <p>
     * Consider that the returned list might be limited in size and not reveal all entries that exist in the iterator.
     * <p>
     * This method requires sessions to be enabled on the Neo node. If sessions are disabled on the Neo node, use
     * {@link SmartContract#callFunctionAndUnwrapIterator(String, List, int, Signer...)}.
     *
     * @param mapper                 the function to apply on the stack items in the iterator.
     * @param function               the function to call.
     * @param maxIteratorResultItems the maximal number of items to return.
     * @param params                 the contract parameters to include in the call.
     * @param <T>                    the type the stack items are mapped to by the mapper function.
     * @return the mapped iterator items.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @throws InvocationFaultStateException if the invocation results in a fault VM state.
     */
    public <T> List<T> callFunctionAndTraverseIterator(Function<StackItem, T> mapper, String function,
            int maxIteratorResultItems, ContractParameter... params) throws IOException {

        Iterator<T> iterator = callFunctionReturningIterator(mapper, function, params);
        List<T> iteratorItems = iterator.traverse(maxIteratorResultItems);
        iterator.terminateSession();
        return iteratorItems;
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting an
     * {@link InteropInterfaceStackItem} as a return type. Then, traverses the iterator to retrieve the first
     * {@link SmartContract#DEFAULT_ITERATOR_COUNT} stack items.
     * <p>
     * Consider that the returned list might be limited in size and not reveal all entries that exist in the iterator.
     * <p>
     * This method requires sessions to be enabled on the Neo node. If sessions are disabled on the Neo node, use
     * {@link SmartContract#callFunctionAndUnwrapIterator(String, List, int, Signer...)}.
     *
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @return the iterator.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @throws InvocationFaultStateException if the invocation results in a fault VM state.
     */
    public List<StackItem> callFunctionAndTraverseIterator(String function, ContractParameter... params)
            throws IOException {

        Iterator<StackItem> iterator = callFunctionReturningIterator(function, params);
        List<StackItem> iteratorItems = iterator.traverse(DEFAULT_ITERATOR_COUNT);
        iterator.terminateSession();
        return iteratorItems;
    }

    /**
     * Calls {@code function} of this contract and expects an iterator as the return value. That iterator is then
     * traversed and its entries are put in an array which is returned. Note, that this all happens on the NeoVM.
     * Thus, this method is useful for Neo nodes that don't have iterator sessions enabled.
     *
     * @param function               the function to call.
     * @param params                 the contract parameters to include in the call.
     * @param maxIteratorResultItems the maximal number of iterator result items to include in the array. This value
     *                               must not exceed NeoVM limits.
     * @param signers                the list of signers for this request.
     * @return a list of stack items of the returned iterator.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as a String.
     * @throws InvocationFaultStateException if the invocation results in a fault VM state.
     */
    public List<StackItem> callFunctionAndUnwrapIterator(String function, List<ContractParameter> params,
            int maxIteratorResultItems, Signer... signers) throws UnexpectedReturnTypeException, IOException {

        byte[] script = ScriptBuilder.buildContractCallAndUnwrapIterator(scriptHash, function, params,
                maxIteratorResultItems);

        InvocationResult invocationResult = neow3j.invokeScript(toHexString(script), signers).send()
                .getInvocationResult();
        throwIfFaultState(invocationResult);
        return invocationResult.getStack().get(0).getList();
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function.
     *
     * @param function the function to call.
     * @param signers  the list of signers for this contract call.
     * @return the call's response.
     * @throws IOException if something goes wrong when communicating with the Neo node.
     */
    public NeoInvokeFunction callInvokeFunction(String function, Signer... signers) throws IOException {
        return callInvokeFunction(function, new ArrayList<>(), signers);
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function.
     *
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @param signers  the list of signers for this contract call.
     * @return the call's response.
     * @throws IOException if something goes wrong when communicating with the Neo node.
     */
    public NeoInvokeFunction callInvokeFunction(String function, List<ContractParameter> params, Signer... signers)
            throws IOException {
        // Remark: The list of signers may be required for `invokefunction` calls that will hit a CheckWitness check
        // in the smart contract.
        if (Strings.isEmpty(function)) {
            throw new IllegalArgumentException("The invocation function must not be null or empty.");
        }
        return neow3j.invokeFunction(scriptHash, function, params, signers).send();
    }

    void throwIfFaultState(InvocationResult invocationResult) {
        if (invocationResult.hasStateFault()) {
            throw new InvocationFaultStateException(invocationResult.getException());
        }
    }

    /**
     * Gets the script hash of this smart contract.
     *
     * @return the script hash of this smart contract.
     */
    public Hash160 getScriptHash() {
        return scriptHash;
    }

    /**
     * Gets the manifest of this smart contract.
     *
     * @return the manifest of this smart contract.
     * @throws IOException if something goes wrong when communicating with the Neo node.
     */
    public ContractManifest getManifest() throws IOException {
        ContractState contractState = neow3j.getContractState(scriptHash).send().getContractState();
        return contractState.getManifest();
    }

    /**
     * Gets the name of this smart contract.
     *
     * @return the name of this smart contract.
     * @throws IOException if something goes wrong when communicating with the Neo node.
     */
    public String getName() throws IOException {
        return getManifest().getName();
    }

    protected static Hash160 calcNativeContractHash(String contractName) {
        return calcContractHash(Hash160.ZERO, 0, contractName);
    }

    /**
     * Calculates the hash of the contract deployed by {@code sender}.
     * <p>
     * A contract's hash doesn't change after deployment. Even if the contract's script is updated the hash stays the
     * same. It depends on the initial NEF checksum, contract name, and the sender of the deployment transaction.
     *
     * @param sender       the sender of the contract deployment transaction.
     * @param nefCheckSum  the checksum of the contract's NEF file.
     * @param contractName the contract's name.
     * @return the hash of the contract.
     */
    public static Hash160 calcContractHash(Hash160 sender, long nefCheckSum, String contractName) {
        return Hash160.fromScript(ScriptBuilder.buildContractHashScript(sender, nefCheckSum, contractName));
    }

}
