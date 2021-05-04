package io.neow3j.contract;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static java.util.Arrays.asList;

import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Role;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the RoleManagement contract that is used to assign roles to and check roles of
 * designated nodes.
 */
public class RoleManagement extends SmartContract {

    private static final String NAME = "RoleManagement";
    public static final Hash160 SCRIPT_HASH = getScriptHashOfNativeContract(NAME);

    private static final String GET_DESIGNATED_BY_ROLE = "getDesignatedByRole";
    private static final String DESIGNATE_AS_ROLE = "designateAsRole";

    /**
     * Constructs a new {@code RoleManagement} that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public RoleManagement(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Gets the designated nodes by their role and the block index.
     *
     * @param role       the role.
     * @param blockIndex the block index for which the nodes are designated.
     * @return the {@code ECPublicKeys} of the designated nodes.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<ECPublicKey> getDesignatedByRole(Role role, BigInteger blockIndex) throws IOException {
        checkBlockIndexValidity(blockIndex);
        NeoInvokeFunction invocation = callInvokeFunction(GET_DESIGNATED_BY_ROLE,
                asList(
                        integer(role.byteValue()),
                        integer(blockIndex)));

        List<StackItem> arrayOfDesignates = invocation.getInvocationResult().getStack().get(0)
                .getList();

        return arrayOfDesignates.stream()
                .map(item -> new ECPublicKey(item.getByteArray()))
                .collect(Collectors.toList());
    }

    private void checkBlockIndexValidity(BigInteger blockIndex) throws IOException {
        if (blockIndex.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("The block index has to be positive.");
        }

        BigInteger currentBlockIndex = neow3j.getBlockCount().send().getBlockIndex();
        if (blockIndex.compareTo(currentBlockIndex) > 0) {
            throw new IllegalArgumentException("The provided block index (" + blockIndex + ") is " +
                    "too high. The current block count is " + currentBlockIndex + ".");
        }
    }

    /**
     * Creates a transaction script to designate nodes as a {@link Role} and
     * initializes a {@link TransactionBuilder} based on this script.
     *
     * @param role    the designation role.
     * @param pubKeys the public keys of the nodes that are designated.
     * @return the transaction builder.
     */
    public TransactionBuilder designateAsRole(Role role, List<ECPublicKey> pubKeys) {
        if (role == null) {
            throw new IllegalArgumentException("The designation role cannot be null.");
        }
        if (pubKeys == null || pubKeys.isEmpty()) {
            throw new IllegalArgumentException("At least one public key is required for " +
                    "designation.");
        }
        ContractParameter roleParam = integer(role.byteValue());
        List<ContractParameter> pubKeysParams = pubKeys.stream()
                .map(k -> publicKey(k.getEncoded(true)))
                .collect(Collectors.toList());

        return invokeFunction(DESIGNATE_AS_ROLE, roleParam, array(pubKeysParams));
    }

}
