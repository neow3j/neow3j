package io.neow3j.devpack.constants;

public enum WitnessScope {

    /**
     * A witness with this scope is only used for transactions and is disabled in contracts.
     */
    NONE,

    /**
     * This scope limits the use of a witness to the level of the contract called in the transaction. I.e. it only
     * allows the invoked contract to use the witness.
     */
    CALLED_BY_ENTRY,

    /**
     * This scope allows the specification of additional contracts in which the witness can be used.
     */
    CUSTOM_CONTRACTS,

    /**
     * This scope allows the specification of contract groups in which the witness can be used.
     */
    CUSTOM_GROUPS,

    /**
     * Indicates that the current context must satisfy the specified rules.
     */
    WITNESS_RULES,

    /**
     * The global scope allows to use a witness in all contexts. It cannot be combined with other scopes.
     */
    GLOBAL;
}
