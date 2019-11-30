package io.neow3j.transaction;

import java.util.List;

public enum WitnessScope {

    /**
     * The global scope allows to use a witness in all contexts. It cannot be combined with other
     * scopes.
     */
    GLOBAL(0x00),

    /**
     * This scope limits the use of a witness to the level of the contract called in the
     * transaction. I.e. it only allows the invoked contract to use the witness.
     */
    CALLED_BY_ENTRY(0x01),

    /**
     * This scope allows the specification of additional contracts in which the witness can be used.
     */
    CUSTOM_CONSTRACTS(0x10),

    /**
     * This scope allows the specification of contract groups in which the witness can be used.
     */
    CUSTOM_GROUPS(0x20);

    private byte byteValue;

    WitnessScope(int byteValue) {
        this.byteValue = (byte) byteValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public static WitnessScope valueOf(byte byteValue) {
        for (WitnessScope e : WitnessScope.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

    public static byte getCombinedScope(List<WitnessScope> scopes) {
        byte combined = 0;
        for (WitnessScope s : scopes) {
            combined |= s.byteValue();
        }
        return combined;
    }

}
