package io.neow3j.devpack.constants;

/**
 * Defines the possible roles of designates.
 */
public class Role {

    /**
     * The validators of the state. Used to generate and sign the state root.
     */
    public static final byte StateValidator = 1 << 2;

    /**
     * The nodes used to process Oracle requests.
     */
    public static final byte Oracle = 1 << 3;

    /**
     * The NeoFS Alphabet nodes.
     */
    public static final byte NeoFSAlphabetNode = 1 << 4;

}
