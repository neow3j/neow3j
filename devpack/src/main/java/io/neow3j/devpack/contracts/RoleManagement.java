package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.NativeContract;
import io.neow3j.devpack.constants.Role;

import static io.neow3j.devpack.Helper.reverse;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.devpack.constants.NativeContract.RoleManagementScriptHash;

/**
 * Represents an interface to the native RoleManagement contract that deals with the assignment of nodes to certain
 * roles. A node can have the roles defined in {@link Role}, e.g., be an oracle node or a validator node. This
 * contract provides the functionality to check the nodes assigned a particular role.
 */
@NativeContract
public class RoleManagement extends ContractInterface {

    public RoleManagement() {
        super(new Hash160(reverse(hexToBytes(RoleManagementScriptHash).toByteArray())));
    }

    /**
     * Gets the nodes with the given {@link Role}.
     *
     * @param role  the {@link Role} to get the nodes for.
     * @param index the block index at which to get the designated nodes for.
     * @return the public keys of the nodes with the given role.
     */
    public native ECPoint[] getDesignatedByRole(byte role, int index);

    /**
     * Designates the nodes with the given public keys to the {@link Role};
     *
     * @param role       the role of the designated nodes.
     * @param publicKeys the node's public keys.
     */
    public native void designateAsRole(byte role, ECPoint[] publicKeys);

}
