package io.neow3j.contract;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.publicKey;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.DesignationRole;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.wallet.Account;
import java.util.stream.Collectors;
import org.bouncycastle.math.ec.ECPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents an Oracle contract and provides methods to invoke it.
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
     * Gets the designate by its role and index.
     *
     * @param role the designate role.
     * @param index the index.
     * @return the designates by the given role and index.
     */
    public ECPoint[] getDesignatedByRole(DesignationRole role, int index) throws IOException {
//    params:
//    - role: Integer
//    - index: Integer
//    returnType: ECPoint[]
        NeoInvokeFunction invoc = callInvokeFunction(GET_DESIGNATED_BY_ROLE,
                Arrays.asList(
                        ContractParameter.integer(role.byteValue()),
                        ContractParameter.integer(index)));
        StackItem stackItem = invoc.getInvocationResult().getStack().get(0);
        // TODO: 14.12.20 Michael: check return type and implement it.
        return null;
    }

    /**
     * Creates a transaction script to designate nodes as a {@link DesignationRole} and
     * initializes a {@link TransactionBuilder} based on this script.
     *
     * @param role  The role.
     * @param nodes The nodes to be designated.
     */
    public TransactionBuilder designateAsRole(DesignationRole role, List<ECPublicKey> pubKeys) {
//    params:
//    - role: Integer
//    - nodes: Array of ECPoints
//    returnType: Void
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }
        if (pubKeys == null || pubKeys.isEmpty()) {
            throw new IllegalArgumentException("At least one node is required for designation.");
        }
        ContractParameter roleParam = ContractParameter.integer(role.byteValue());
        List<ContractParameter> pubKeysParams = pubKeys.stream()
                .map(k -> publicKey(k.getEncoded(true)))
                .collect(Collectors.toList());

        return invokeFunction(DESIGNATE_AS_ROLE, roleParam, array(pubKeysParams));
    }
}
