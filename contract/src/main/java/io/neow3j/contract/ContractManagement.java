package io.neow3j.contract;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.neow3j.constants.NeoConstants;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.ContractState;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Represents a Management contract and provides methods to invoke it.
 */
public class ContractManagement extends SmartContract {

    private static final String NAME = "ContractManagement";
    public static final Hash160 SCRIPT_HASH = calcNativeContractHash(NAME);

    private static final String GET_MINIMUM_DEPLOYMENT_FEE = "getMinimumDeploymentFee";
    private static final String SET_MINIMUM_DEPLOYMENT_FEE = "setMinimumDeploymentFee";
    private static final String GET_CONTRACT_BY_ID = "getContractById";
    private static final String GET_CONTRACT_HASHES = "getContractHashes";
    private static final String HAS_METHOD = "hasMethod";
    private static final String DEPLOY = "deploy";

    /**
     * Constructs a new {@link ContractManagement} that uses the given {@link Neow3j} instance for invocations.
     *
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public ContractManagement(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Gets the minimum fee required for deployment.
     *
     * @return the minimum required fee for contract deployment.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getMinimumDeploymentFee() throws IOException {
        return callFunctionReturningInt(GET_MINIMUM_DEPLOYMENT_FEE);
    }

    /**
     * Creates a transaction script to set the minimum deployment fee and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
     *
     * @param minimumFee the minimum deployment fee.
     * @return a transaction builder.
     */
    public TransactionBuilder setMinimumDeploymentFee(BigInteger minimumFee) {
        return invokeFunction(SET_MINIMUM_DEPLOYMENT_FEE, integer(minimumFee));
    }

    /**
     * Gets the contract state of the contract with {@code contractHash}.
     * <p>
     * Makes use of the RPC {@link io.neow3j.protocol.core.JsonRpc2_0Neow3j#getContractState(Hash160)}.
     *
     * @param contractHash the contract hash.
     * @return the contract state.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public ContractState getContract(Hash160 contractHash) throws IOException {
        return neow3j.getContractState(contractHash).send().getContractState();
    }

    /**
     * Gets the contract state of the contract with {@code id}.
     * <p>
     * Makes use of the RPC {@link io.neow3j.protocol.core.JsonRpc2_0Neow3j#getContractState(Hash160)}.
     *
     * @param id the contract id.
     * @return the contract state.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public ContractState getContractById(int id) throws IOException {
        return neow3j.getContractState(getContractHashById(id)).send().getContractState();
    }

    private Hash160 getContractHashById(int id) throws IOException {
        InvocationResult response = callInvokeFunction(GET_CONTRACT_BY_ID, asList(integer(id))).getInvocationResult();
        try {
            return new Hash160(reverseArray(response.getStack().get(0).getList().get(2).getByteArray()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not get the contract hash for the provided id.");
        }
    }

    /**
     * Get all non native contract hashes and ids.
     *
     * @return all non native contract hashes and ids.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public  Iterator<ContractState.ContractIdentifiers> getContractHashes() throws IOException {
        return callFunctionReturningIterator(ContractState.ContractIdentifiers::fromStackItem, GET_CONTRACT_HASHES);
    }

    /**
     * Get all non native contract hashes and ids.
     * <p>
     * Use this method if sessions are disabled on the Neo node.
     * <p>
     * This method returns at most {@link NeoConstants#MAX_ITERATOR_ITEMS_DEFAULT} values. If there are more values,
     * connect to a Neo node that supports sessions and use {@link ContractManagement#getContractHashes()}.
     *
     * @return all non native contract hashes and ids.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<ContractState.ContractIdentifiers> getContractHashesUnwrapped() throws IOException {
        List<StackItem> list = callFunctionAndUnwrapIterator(GET_CONTRACT_HASHES, asList(), DEFAULT_ITERATOR_COUNT);
        return list.stream().map(ContractState.ContractIdentifiers::fromStackItem).collect(Collectors.toList());
    }

    /**
     * Checks if a method exists in a contract.
     *
     * @param contractHash the contract hash.
     * @param method       the method.
     * @param paramCount   the number of parameters.
     * @return true if the method exists. False otherwise.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public boolean hasMethod(Hash160 contractHash, String method, int paramCount) throws IOException {
        return callFunctionReturningBool(HAS_METHOD, hash160(contractHash), string(method), integer(paramCount));
    }

    /**
     * Creates a script and a containing transaction builder for a transaction that deploys the contract with the
     * given NEF and manifest.
     *
     * @param nef      the NEF file.
     * @param manifest the manifest.
     * @return a transaction builder containing the deployment script.
     * @throws JsonProcessingException if there is a problem serializing the manifest.
     */
    public TransactionBuilder deploy(NefFile nef, ContractManifest manifest) throws JsonProcessingException {
        return deploy(nef, manifest, null);
    }

    /**
     * Creates a script and a containing transaction builder for a transaction that deploys the contract with the
     * given NEF and manifest.
     *
     * @param nef      the NEF file.
     * @param manifest the manifest.
     * @param data     data to pass to the deployed contract's {@code _deploy} method.
     * @return a transaction builder containing the deployment script.
     * @throws JsonProcessingException if there is a problem serializing the manifest.
     */
    public TransactionBuilder deploy(NefFile nef, ContractManifest manifest, ContractParameter data)
            throws JsonProcessingException {

        if (nef == null) {
            throw new IllegalArgumentException("The NEF file cannot be null.");
        }
        if (manifest == null) {
            throw new IllegalArgumentException("The manifest cannot be null.");
        }
        byte[] manifestBytes = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(manifest);
        if (manifestBytes.length > NeoConstants.MAX_MANIFEST_SIZE) {
            throw new IllegalArgumentException(format("The given contract manifest is too long. Manifest was %d bytes" +
                    " big, but a max of %d bytes is allowed.", manifestBytes.length, NeoConstants.MAX_MANIFEST_SIZE));
        }
        if (data == null) {
            return invokeFunction(DEPLOY, byteArray(nef.toArray()), byteArray(manifestBytes));
        } else {
            return invokeFunction(DEPLOY, byteArray(nef.toArray()), byteArray(manifestBytes), data);
        }
    }

}
