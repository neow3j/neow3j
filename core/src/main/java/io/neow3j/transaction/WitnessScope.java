package io.neow3j.transaction;

public enum WitnessScope {

    /**
     * The global scope allows to use a witness in all contexts. It cannot be combined with other
     * scopes.
     */
    GLOBAL(0x00),
    /**
     * This scope limits the use of a witness to the level of the script contained in the same
     * transaction. I.e. it is only valid in the invocations made by the script that is contained in
     * the same transaction with the witness. In any further internal invocations the witness
     * will expire.
     */
    CALLED_BY_ENTRY(0x01),
    /**
     * This scope allows the specification of contracts in which the witness can be used.
     */
    CUSTOM_CONSTRACTS(0x10),
    /**
     * This scope allows the specification of contract groups in which the witness can be used.
     */
    CUSTOM_GROUPS(0x20);

    private byte byteValue;

    private WitnessScope(int byteValue) {
        this.byteValue = (byte) byteValue;
    }

}
