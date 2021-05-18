package io.neow3j.contract;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.integer;
import static java.lang.String.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.neow3j.constants.NeoConstants;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.methods.response.ContractManifest;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Represents a Management contract and provides methods to invoke it.
 */
public class ContractManagement extends SmartContract {

    private static final String NAME = "ContractManagement";
    public static final Hash160 SCRIPT_HASH = getScriptHashOfNativeContract(NAME);

    private static final String GET_MINIMUM_DEPLOYMENT_FEE = "getMinimumDeploymentFee";
    private static final String SET_MINIMUM_DEPLOYMENT_FEE = "setMinimumDeploymentFee";
    private static final String DEPLOY = "deploy";

    /**
     * Constructs a new {@link ContractManagement} that uses the given {@link Neow3j} instance for
     * invocations.
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
        return callFuncReturningInt(GET_MINIMUM_DEPLOYMENT_FEE);
    }

    /**
     * Creates a transaction script to set the minimum deployment fee and initializes a {@link
     * TransactionBuilder} based on this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has
     * to be signed by the committee members.
     *
     * @param minimumFee the minimum deployment fee.
     * @return a transaction builder.
     */
    public TransactionBuilder setMinimumDeploymentFee(BigInteger minimumFee) {
        return invokeFunction(SET_MINIMUM_DEPLOYMENT_FEE, integer(minimumFee));
    }

    /**
     * Creates a script and a containing transaction builder for a transaction that deploys the
     * contract with the given NEF and manifest.
     *
     * @param nef      The NEF file.
     * @param manifest The manifest.
     * @return a transaction builder containing the deployment script.
     * @throws JsonProcessingException If there is a problem serializing the manifest.
     */
    public TransactionBuilder deploy(NefFile nef, ContractManifest manifest)
            throws JsonProcessingException {

        return deploy(nef, manifest, null);
    }

    /**
     * Creates a script and a containing transaction builder for a transaction that deploys the
     * contract with the given NEF and manifest.
     *
     * @param nef      The NEF file.
     * @param manifest The manifest.
     * @param data     Data to pass to the deployed contract's {@code _deploy} method.
     * @return a transaction builder containing the deployment script.
     * @throws JsonProcessingException If there is a problem serializing the manifest.
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
            throw new IllegalArgumentException(format("The given contract manifest is too long. " +
                            "Manifest was %d bytes big, but a max of %d bytes is allowed.",
                    manifestBytes.length, NeoConstants.MAX_MANIFEST_SIZE));
        }
        if (data == null) {
            return invokeFunction(DEPLOY, byteArray(nef.toArray()), byteArray(manifestBytes));
        } else {
            return invokeFunction(DEPLOY, byteArray(nef.toArray()), byteArray(manifestBytes), data);
        }
    }

}
