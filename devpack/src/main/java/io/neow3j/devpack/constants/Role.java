package io.neow3j.devpack.constants;

/**
 * Defines the possible roles of designates.
 */
public class Role {

    /**
     * The validators of the state. Used to generate and sign the state root.
     */
    public static final byte STATE_VALIDATOR = 1 << 2;

    /**
     * The nodes used to process Oracle requests.
     */
    public static final byte ORACLE = 1 << 3;

    /**
     * The NeoFS Alphabet nodes.
     */
    public static final byte NEO_FS_ALPHABET_NODE = 1 << 4;

}
