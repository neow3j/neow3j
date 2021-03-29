package io.neow3j.devpack.contracts;

/**
 * Defines the possible roles of designates.
 */
public class Role {

    public static final byte STATE_VALIDATOR =
            io.neow3j.protocol.core.Role.STATE_VALIDATOR.byteValue();

    public static final byte ORACLE = io.neow3j.protocol.core.Role.ORACLE.byteValue();

}
