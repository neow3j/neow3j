package io.neow3j.contract;

import static io.neow3j.contract.ContractParameter.byteArray;
import static java.lang.String.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Represents a Management contract and provides methods to invoke it.
 */
public class ContractManagement extends SmartContract {

    private static final String NAME = "ContractManagement";
    public final static long NEF_CHECKSUM = 3516775561L;
    public static final ScriptHash SCRIPT_HASH = getScriptHashOfNativeContract(NEF_CHECKSUM, NAME);

    private static final String GET_MINIMUM_DEPLOYMENT_FEE = "getMinimumDeploymentFee";
    private static final String SET_MINIMUM_DEPLOYMENT_FEE = "setMinimumDeploymentFee";
    private static final String GET_CONTRACT = "getContract";
    private static final String DEPLOY = "deploy";

    /**
     * Constructs a new <tt>ManagementContract</tt> that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow The {@link Neow3j} instance to use for invocations.
     */
    public ContractManagement(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Gets the minimum fee required for deployment.
     *
     * @return The minimum required fee for contract deployment.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getMinimumDeploymentFee() throws IOException {
        return callFuncReturningInt(GET_MINIMUM_DEPLOYMENT_FEE);
    }

    /**
     * Creates a transaction script to set the minimum deployment fee and initializes a {@link
     * TransactionBuilder} based on this script.
     *
     * @param minimumFee the minimum deployment fee.
     * @return A transaction builder.
     */
    public TransactionBuilder setMinimumDeploymentFee(BigInteger minimumFee) {
        return invokeFunction(SET_MINIMUM_DEPLOYMENT_FEE, ContractParameter.integer(minimumFee));
    }

    /**
     * Returns the state of a smart contract.
     *
     * @param scriptHash The script hash of the smart contract.
     * @return The state of the smart contract.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public ContractState getContract(ScriptHash scriptHash) throws IOException {
        StackItem stackItem = callInvokeFunction(GET_CONTRACT,
                Arrays.asList(ContractParameter.hash160(scriptHash)))
                .getInvocationResult().getStack().get(0);
        if (!stackItem.getType().equals(StackItemType.ARRAY)) {
            throw new UnexpectedReturnTypeException(stackItem.getType(), StackItemType.ARRAY);
        }
        int id = stackItem.asArray().get(0).asInteger().getValue().intValue();
        int updateCounter = stackItem.asArray().get(1).asInteger().getValue().intValue();
        String hash = Numeric.reverseHexString(stackItem.asArray().get(2).asByteString()
                .getAsHexString());
        String script = Numeric.toHexStringNoPrefix(stackItem.asArray().get(3).asByteString()
                .getValue());
        ContractManifest manifest = stackItem.asArray().get(4).asByteString().getAsJson(
                ContractManifest.class);
        return new ContractState(id, updateCounter, hash, script, manifest);
    }

    public TransactionBuilder deploy(NefFile nef, ContractManifest manifest)
            throws JsonProcessingException {

        if (nef == null) {
            throw new IllegalArgumentException("The NEF file cannot be null.");
        }
        if (manifest == null) {
            throw new IllegalArgumentException("The manifest cannot be null.");
        }
        byte[] manifestBytes = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(manifest);
        if (manifestBytes.length > NeoConstants.MAX_MANIFEST_SIZE) {
            throw new IllegalArgumentException(format("The given contract manifest is too long. "
                            + "Manifest was %d bytes big, but a max of %d bytes is allowed.",
                    manifestBytes.length, NeoConstants.MAX_MANIFEST_SIZE));
        }
        return invokeFunction(DEPLOY, byteArray(nef.toArray()), byteArray(manifestBytes));
    }

}