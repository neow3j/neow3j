package io.neow3j.devpack.neo;

import io.neow3j.devpack.annotations.Contract;

/**
 * Represents the native contract that deals with the designation of nodes to certain roles. A node
 * can have the roles defined in {@link Role}, e.g. be an oracle node or a validator node. This
 * contract provides the functionality to check the nodes designated under a particular role.
 */
@Contract(scriptHash = "0xc0073f4c7069bf38995780c9da065f9b3949ea7a")
public class DesignationContract {

    /**
     * Gets the script hash of the Designation contract.
     *
     * @return the script hash.
     */
    public static native byte[] hash();

    /**
     * Gets the designates with the given {@link Role}.
     *
     * @param role The {@link Role}.
     * @return the public keys of the designates with the given role.
     */
    public static native byte[][] getDesignatedByRole(byte role);
}
