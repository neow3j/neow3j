package io.neow3j.devpack.neo;

import io.neow3j.devpack.ContractInterface;
import io.neow3j.devpack.annotations.ContractHash;

/**
 * Represents the native contract that deals with the designation of nodes to certain roles. A node
 * can have the roles defined in {@link Role}, e.g. be an oracle node or a validator node. This
 * contract provides the functionality to check the nodes designated under a particular role.
 */
@ContractHash("0xc0073f4c7069bf38995780c9da065f9b3949ea7a")
public class DesignationContract extends ContractInterface {

    /**
     * Gets the designates with the given {@link Role}.
     *
     * @param role The {@link Role} to get the designates for.
     * @param index The block index at which to get the designates for.
     * @return the public keys of the designates with the given role.
     */
    public static native byte[][] getDesignatedByRole(byte role, int index);

    /**
     * Designates the nodes with the given public keys to the {@link Role};
     *
     * @param role       The role of the designated nodes.
     * @param publicKeys The node's public keys.
     */
    public static native void designateAsRole(byte role, byte[][] publicKeys);

}
