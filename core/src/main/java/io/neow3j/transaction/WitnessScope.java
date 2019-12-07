package io.neow3j.transaction;

import java.util.ArrayList;
import java.util.Arrays;
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
     * This scope allows the specification of additional contracts in which the witness can be
     * used.
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

    /**
     * Extracts the scopes encoded in the given byte.
     *
     * @param combinedScopes The byte representation of the scopes.
     * @return the list of scopes encoded by the given byte.
     */
    public static List<WitnessScope> extractCombinedScopes(byte combinedScopes) {
        if (combinedScopes == GLOBAL.byteValue()) {
            return Arrays.asList(GLOBAL);
        }
        List<WitnessScope> scopes = new ArrayList<>();
        if ((combinedScopes & CALLED_BY_ENTRY.byteValue()) != 0) {
            scopes.add(CALLED_BY_ENTRY);
        }
        if ((combinedScopes & CUSTOM_CONSTRACTS.byteValue()) != 0) {
            scopes.add(CUSTOM_CONSTRACTS);
        }
        if ((combinedScopes & CUSTOM_GROUPS.byteValue()) != 0) {
            scopes.add(CUSTOM_GROUPS);
        }
        return scopes;
    }

    /**
     * Encodes the given scopes in one byte.
     *
     * @param scopes The scopes to encode.
     * @return the scope encoding byte.
     */
    public static byte combineScopes(List<WitnessScope> scopes) {
        byte combined = 0;
        for (WitnessScope s : scopes) {
            combined |= s.byteValue();
        }
        return combined;
    }

}
