package io.neow3j.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

public enum WitnessScope {

    /**
     * A witness with this scope is only used for transactions and is disabled in contracts.
     */
    NONE("None", 0x00),

    /**
     * This scope limits the use of a witness to the level of the contract called in the transaction. I.e. it only
     * allows the invoked contract to use the witness.
     */
    CALLED_BY_ENTRY("CalledByEntry", 0x01),

    /**
     * This scope allows the specification of additional contracts in which the witness can be used.
     */
    CUSTOM_CONTRACTS("CustomContracts", 0x10),

    /**
     * This scope allows the specification of contract groups in which the witness can be used.
     */
    CUSTOM_GROUPS("CustomGroups", 0x20),

    /**
     * Indicates that the current context must satisfy the specified rules.
     */
    WITNESS_RULES("WitnessRules", 0x40),

    /**
     * The global scope allows to use a witness in all contexts. It cannot be combined with other scopes.
     */
    GLOBAL("Global", 0x80);

    private String jsonValue;

    private byte byteValue;

    WitnessScope(String jsonValue, int byteValue) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
    }

    /**
     * Extracts the scopes encoded in the given byte.
     *
     * @param combinedScopes the byte representation of the scopes.
     * @return the list of scopes encoded by the given byte.
     */
    public static List<WitnessScope> extractCombinedScopes(byte combinedScopes) {
        if (combinedScopes == NONE.byteValue()) {
            return Arrays.asList(NONE);
        }
        List<WitnessScope> scopes = new ArrayList<>();
        if ((combinedScopes & GLOBAL.byteValue()) != 0) {
            scopes.add(GLOBAL);
        }
        if ((combinedScopes & CALLED_BY_ENTRY.byteValue()) != 0) {
            scopes.add(CALLED_BY_ENTRY);
        }
        if ((combinedScopes & CUSTOM_CONTRACTS.byteValue()) != 0) {
            scopes.add(CUSTOM_CONTRACTS);
        }
        if ((combinedScopes & CUSTOM_GROUPS.byteValue()) != 0) {
            scopes.add(CUSTOM_GROUPS);
        }
        if ((combinedScopes & WITNESS_RULES.byteValue()) != 0) {
            scopes.add(WITNESS_RULES);
        }
        return scopes;
    }

    /**
     * Encodes the given scopes in one byte.
     *
     * @param scopes the scopes to encode.
     * @return the scope encoding byte.
     */
    public static byte combineScopes(List<WitnessScope> scopes) {
        byte combined = 0;
        for (WitnessScope s : scopes) {
            combined |= s.byteValue();
        }
        return combined;
    }

    @JsonCreator
    public static WitnessScope fromJson(Object value) {
        if (value instanceof String) {
            return fromJsonValue((String) value);
        }
        if (value instanceof Integer) {
            return valueOf(((Integer) value).byteValue());
        }
        throw new IllegalArgumentException(format("%s value type not found.", WitnessScope.class.getName()));
    }

    public static WitnessScope valueOf(byte byteValue) {
        for (WitnessScope e : WitnessScope.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(format("%s value type not found.", WitnessScope.class.getName()));
    }

    public static WitnessScope fromJsonValue(String jsonValue) {
        for (WitnessScope e : WitnessScope.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(format("%s value type not found.", WitnessScope.class.getName()));
    }

    public byte byteValue() {
        return this.byteValue;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

}
