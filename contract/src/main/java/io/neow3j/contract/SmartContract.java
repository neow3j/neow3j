package io.neow3j.contract;

import io.neow3j.constants.OpCode;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.exceptions.StackItemCastException;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Strings;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.neow3j.model.types.StackItemType.BOOLEAN;
import static io.neow3j.model.types.StackItemType.BYTE_STRING;
import static io.neow3j.model.types.StackItemType.INTEGER;
import static io.neow3j.utils.Numeric.reverseHexString;
import static java.util.Arrays.asList;

/**
 * Represents a smart contract on the Neo blockchain and provides methods to invoke and deploy it.
 */
public class SmartContract {

    protected Hash160 scriptHash;
    protected Neow3j neow;

    /**
     * Constructs a {@code SmartContract} representing the smart contract with the given script
     * hash. Uses the given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash the smart contract's script hash.
     * @param neow       the {@link Neow3j} instance to use for invocations.
     */
    public SmartContract(Hash160 scriptHash, Neow3j neow) {
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
     * Initializes a {@link TransactionBuilder} for an invocation of this contract with the
     * provided function and parameters. The order of the parameters is relevant.
     *
     * @param function           the function to invoke.
     * @param contractParameters the parameters to pass with the invocation.
     * @return a {@link TransactionBuilder} allowing to set further details of the invocation.
     */
    public TransactionBuilder invokeFunction(String function,
            ContractParameter... contractParameters) {

        if (Strings.isEmpty(function)) {
            throw new IllegalArgumentException(
                    "The invocation function must not be null or empty.");
        }
        ScriptBuilder b = new ScriptBuilder().contractCall(scriptHash, function,
                asList(contractParameters));
        return new TransactionBuilder(neow).script(b.toArray());
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting a String
     * as return type.
     *
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @return the string returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as a
     *                                       String.
     */
    public String callFuncReturningString(String function, ContractParameter... params)
            throws UnexpectedReturnTypeException, IOException {

        StackItem item = callInvokeFunction(function, asList(params))
                .getInvocationResult().getStack().get(0);
        if (item.getType().equals(BYTE_STRING)) {
            return item.getString();
        }
        throw new UnexpectedReturnTypeException(item.getType(), BYTE_STRING);
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting an
     * Integer as return type.
     *
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @return the integer returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as an
     *                                       integer.
     */
    public BigInteger callFuncReturningInt(String function, ContractParameter... params)
            throws IOException, UnexpectedReturnTypeException {

        StackItem item;
        if (params.length == 0) {
            item = callInvokeFunction(function).getInvocationResult().getStack().get(0);
        } else {
            item = callInvokeFunction(function, asList(params))
                    .getInvocationResult().getStack().get(0);
        }
        if (item.getType().equals(INTEGER)) {
            return item.getInteger();
        }
        throw new UnexpectedReturnTypeException(item.getType(), INTEGER);
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting a
     * Boolean as return type.
     *
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @return the boolean returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as an
     *                                       boolean.
     */
    public boolean callFuncReturningBool(String function, ContractParameter... params)
            throws IOException, UnexpectedReturnTypeException {

        StackItem item;
        if (params.length == 0) {
            item = callInvokeFunction(function).getInvocationResult().getStack().get(0);
        } else {
            item = callInvokeFunction(function, asList(params))
                    .getInvocationResult().getStack().get(0);
        }
        if (item.getType().equals(BOOLEAN)) {
            return item.getBoolean();
        }
        throw new UnexpectedReturnTypeException(item.getType(), BOOLEAN);
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function expecting a
     * script hash as the return type.
     *
     * @param function the function to call.
     * @param params   the contract parameters to include in the call.
     * @return the script hash returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as
     *                                       script hash.
     */
    public Hash160 callFunctionReturningScriptHash(String function, List<ContractParameter> params)
            throws IOException {

        StackItem stackItem = callInvokeFunction(function, params)
                .getInvocationResult().getStack().get(0);
        return extractScriptHash(stackItem);
    }

    private Hash160 extractScriptHash(StackItem item) {
        if (!item.getType().equals(BYTE_STRING)) {
            throw new UnexpectedReturnTypeException(item.getType(), BYTE_STRING);
        }
        try {
            return new Hash160(reverseHexString(item.getHexString()));
        } catch (StackItemCastException | IllegalArgumentException e) {
            throw new UnexpectedReturnTypeException("Return type did not contain script hash in " +
                    "expected format.", e);
        }
    }

    /**
     * Sends an {@code invokefunction} RPC call to the given contract function.
     *
     * @param function the function to call.
     * @param signers  the list of signers for this contract call.
     * @return the call's response.
     * @throws IOException if something goes wrong when communicating with the Neo node.
     */
    public NeoInvokeFunction callInvokeFunction(String function, Signer... signers)
            throws IOException {
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
    public NeoInvokeFunction callInvokeFunction(String function, List<ContractParameter> params,
            Signer... signers) throws IOException {
        // Remark: The list of signers may be required for `invokefunction`
        // calls that will hit a CheckWitness check in the smart contract.
        if (Strings.isEmpty(function)) {
            throw new IllegalArgumentException(
                    "The invocation function must not be null or empty.");
        }
        return neow.invokeFunction(scriptHash.toString(), function, params, signers).send();
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
        ContractState contractState = neow.getContractState(scriptHash).send()
                .getContractState();
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

    protected static Hash160 getScriptHashOfNativeContract(String contractName) {
        return getContractHash(Hash160.ZERO, 0, contractName);
    }

    /**
     * Calculates the hash of the contract deployed by {@code sender}.
     * <p>
     * A contract's hash doesn't change after deployment. Even if the contract's script is
     * updated the hash stays the same. It depends on the initial NEF checksum, contract name,
     * and the account that sent the deployment transaction.
     *
     * @param sender       the account that deployed the contract.
     * @param nefCheckSum  the checksum of the contract's NEF file.
     * @param contractName the contract's name.
     * @return the hash of the contract.
     */
    public static Hash160 getContractHash(Hash160 sender, long nefCheckSum, String contractName) {
        return Hash160.fromScript(
                new ScriptBuilder()
                        .opCode(OpCode.ABORT)
                        .pushData(sender.toArray())
                        .pushInteger(nefCheckSum)
                        .pushData(contractName).toArray());
    }

}
