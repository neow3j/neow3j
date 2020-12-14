package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.DesignationRole;
import io.neow3j.protocol.core.methods.response.StackItem;
import org.bouncycastle.math.ec.ECPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents an Oracle contract and provides methods to invoke it.
 */
public class DesignationContract extends SmartContract {

    private static final String NAME = "Designation";

    private static final byte[] SCRIPT = new ScriptBuilder()
            .pushData(NAME)
            .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CALLNATIVE)
            .toArray();

    public static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder()
                    .opCode(OpCode.ABORT)
                    .pushData(ScriptHash.ZERO.toArray())
                    .pushData(SCRIPT)
                    .toArray());

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
        StackItem stackItem = callInvokeFunction(GET_DESIGNATED_BY_ROLE, Arrays.asList(
                ContractParameter.string(role.jsonValue()), ContractParameter.integer(index)))
                .getInvocationResult().getStack().get(0);
        // TODO: 14.12.20 Michael: check return type when preview4 node is available.
        return null;
    }

    /**
     * Creates a transaction script to designate accounts as roles and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param role The role.
     * @param publicKeys The accounts to designate the role.
     */
    public TransactionBuilder designateAsRole(DesignationRole role, List<ECPublicKey> publicKeys) {
        List<ContractParameter> params = new ArrayList<>();
        params.add(ContractParameter.string(role.jsonValue()));
        publicKeys.forEach(key -> params.add(ContractParameter.publicKey(key.toArray())));
        return invokeFunction(DESIGNATE_AS_ROLE, params.toArray(new ContractParameter[0]));
    }
}
