package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.constants.Role;

/**
 * Represents the native contract that deals with the assignment of nodes to certain roles. A node
 * can have the roles defined in {@link Role}, e.g. be an oracle node or a validator node. This
 * contract provides the functionality to check the nodes assigned a particular role.
 */
@ContractHash("0x49cf4e5378ffcd4dec034fd98a174c5491e395e2")
public class RoleManagement extends ContractInterface {

    /**
     * Gets the nodes with the given {@link Role}.
     *
     * @param role  The {@link Role} to get the nodes for.
     * @param index The block index at which to get the designated nodes for.
     * @return the public keys of the nodes with the given role.
     */
    public static native ECPoint[] getDesignatedByRole(byte role, int index);

    /**
     * Designates the nodes with the given public keys to the {@link Role};
     *
     * @param role       The role of the designated nodes.
     * @param publicKeys The node's public keys.
     */
    public static native void designateAsRole(byte role, ECPoint[] publicKeys);

}
