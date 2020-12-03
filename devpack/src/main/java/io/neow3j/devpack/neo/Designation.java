package io.neow3j.devpack.neo;

import io.neow3j.devpack.annotations.Contract;

@Contract(scriptHash = "0x763afecf3ebba0a67568a2c8be06e8f068c62666")
public class Designation {

    /**
     * Gets the name of the Designation contract.
     *
     * @return the name.
     */
    public static native String name();

    /**
     * Gets the script hash of the Designation contract.
     *
     * @return the script hash.
     */
    public static native byte[] hash();

    /**
     * Gets the designates with the given {@link DesignateRole}.
     *
     * @param role The designate role.
     * @return an array of elliptic curve points (public keys) of the designates.
     */
    public static native byte[][] getDesignatedByRole(byte role);
}
