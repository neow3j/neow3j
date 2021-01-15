package io.neow3j.contract;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;

import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.DesignationRole;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a Designation contract and provides methods to invoke it.
 */
public class DesignationContract extends SmartContract {

    private static final String NAME = "DesignationContract";
    public static final ScriptHash SCRIPT_HASH = SmartContract.getScriptHashOfNativeContract(NAME);
    private static final String GET_DESIGNATED_BY_ROLE = "getDesignatedByRole";
    private static final String DESIGNATE_AS_ROLE = "designateAsRole";

    /**
     * Constructs a new <tt>DesignateContract</tt> that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow The {@link Neow3j} instance to use for invocations.
     */
    public DesignationContract(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Gets the designated nodes by their role and the block index.
     *
     * @param role The designation role.
     * @param blockIndex The block index for which the nodes are designated.
     * @return the {@code ECPublicKeys} of the designated nodes.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<ECPublicKey> getDesignatedByRole(DesignationRole role, int blockIndex) throws IOException {
        checkBlockIndexValidity(blockIndex);
        NeoInvokeFunction invocation = callInvokeFunction(GET_DESIGNATED_BY_ROLE,
                Arrays.asList(
                        integer(role.byteValue()),
                        integer(blockIndex)));

        List<StackItem> arrayOfDesignates = invocation.getInvocationResult().getStack().get(0).asArray().getValue();

        return arrayOfDesignates.stream()
                .map(item -> new ECPublicKey(item.asByteString().getValue()))
                .collect(Collectors.toList());
    }

    private void checkBlockIndexValidity(int blockIndex) throws IOException {
        if (blockIndex < 0) {
            throw new IllegalArgumentException("The block index has to be positive.");
        }

        int currentBlockIndex = neow.getBlockCount().send().getBlockIndex().intValue();
        if (blockIndex > currentBlockIndex) {
            throw new IllegalArgumentException("The provided block index (" + blockIndex + ") is too high." +
                    " The current block count is " + currentBlockIndex + ".");
        }
    }

    /**
     * Creates a transaction script to designate nodes as a {@link DesignationRole} and
     * initializes a {@link TransactionBuilder} based on this script.
     *
     * @param role The designation role.
     * @param pubKeys The public keys of the nodes that are designated.
     * @return the transaction builder.
     */
    public TransactionBuilder designateAsRole(DesignationRole role, List<ECPublicKey> pubKeys) {
        if (role == null) {
            throw new IllegalArgumentException("The designation role cannot be null.");
        }
        if (pubKeys == null || pubKeys.isEmpty()) {
            throw new IllegalArgumentException("At least one public key is required for designation.");
        }
        ContractParameter roleParam = integer(role.byteValue());
        List<ContractParameter> pubKeysParams = pubKeys.stream()
                .map(k -> publicKey(k.getEncoded(true)))
                .collect(Collectors.toList());

        return invokeFunction(DESIGNATE_AS_ROLE, roleParam, array(pubKeysParams));
    }
}
