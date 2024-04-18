package io.neow3j.devpack.constants;

// These values can also be found in the neow3j core module (io.neow3j.transaction.WitnessScope). Make sure to update it
// here and in the core, when things change.
public class WitnessScope {

    /**
     * A witness with this scope is only used for transactions and is disabled in contracts.
     */
    public static final byte NONE = 0x00;

    /**
     * This scope limits the use of a witness to the level of the contract called in the transaction. I.e. it only
     * allows the invoked contract to use the witness.
     */
    public static final byte CALLED_BY_ENTRY = 0x01;

    /**
     * This scope allows the specification of additional contracts in which the witness can be used.
     */
    public static final byte CUSTOM_CONTRACTS = 0x10;

    /**
     * This scope allows the specification of contract groups in which the witness can be used.
     */
    public static final byte CUSTOM_GROUPS = 0x20;

    /**
     * Indicates that the current context must satisfy the specified rules.
     */
    public static final byte WITNESS_RULES = 0x40;

    /**
     * The global scope allows to use a witness in all contexts. It cannot be combined with other scopes.
     */
    public static final int GLOBAL = 0x80;
}
