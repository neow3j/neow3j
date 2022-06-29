package io.neow3j.contract;

import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Role;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Represents the RoleManagement contract that is used to assign roles to and check roles of designated nodes.
 */
@SuppressWarnings("unchecked")
public class RoleManagement extends SmartContract {

    private static final String NAME = "RoleManagement";
    public static final Hash160 SCRIPT_HASH = calcNativeContractHash(NAME);

    private static final String GET_DESIGNATED_BY_ROLE = "getDesignatedByRole";
    private static final String DESIGNATE_AS_ROLE = "designateAsRole";

    /**
     * Constructs a new {@code RoleManagement} that uses the given {@link Neow3j} instance for invocations.
     *
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public RoleManagement(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Gets the nodes that where assigned to the given role at the given block index.
     *
     * @param role       the role.
     * @param blockIndex the block
     * @return the {@code ECPublicKeys} of the designated nodes.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<ECPublicKey> getDesignatedByRole(Role role, BigInteger blockIndex) throws IOException {
        checkBlockIndexValidity(blockIndex);
        NeoInvokeFunction invocation = callInvokeFunction(GET_DESIGNATED_BY_ROLE,
                asList(integer(role.byteValue()), integer(blockIndex)));

        List<StackItem> arrayOfDesignates = invocation.getInvocationResult().getStack().get(0).getList();

        return arrayOfDesignates.stream()
                .map(item -> new ECPublicKey(item.getByteArray()))
                .collect(Collectors.toList());
    }

    private void checkBlockIndexValidity(BigInteger blockIndex) throws IOException {
        if (blockIndex.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("The block index has to be positive.");
        }

        BigInteger currentBlockCount = neow3j.getBlockCount().send().getBlockCount();
        if (blockIndex.compareTo(currentBlockCount) > 0) {
            throw new IllegalArgumentException(format("The provided block index (%s) is too high. The current block " +
                    "count is %s.", blockIndex, currentBlockCount));
        }
    }

    /**
     * Creates a transaction script to designate nodes as a {@link Role} and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * This method can only be successfully invoked by the committee, i.e., the transaction has to be signed by the
     * committee members.
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
            throw new IllegalArgumentException("At least one public key is required for designation.");
        }
        ContractParameter roleParam = integer(role.byteValue());
        List<ContractParameter> pubKeysParams = pubKeys.stream()
                .map(k -> publicKey(k.getEncoded(true)))
                .collect(Collectors.toList());

        return invokeFunction(DESIGNATE_AS_ROLE, roleParam, array(pubKeysParams));
    }

}
