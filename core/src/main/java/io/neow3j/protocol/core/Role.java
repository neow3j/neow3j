package io.neow3j.protocol.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import static java.lang.String.format;

/**
 * Represents the roles in the NEO system.
 */
public enum Role {

    /**
     * The validators of state. Used to generate and sign the state root.
     */
    STATE_VALIDATOR("StateValidator", 0x04),

    /**
     * The nodes used to process Oracle requests.
     */
    ORACLE("Oracle", 0x08),

    /**
     * NeoFS Alphabet nodes.
     */
    NEO_FS_ALPHABET_NODE("NeoFSAlphabetNode", 0x10),

    /**
     * P2P Notary nodes used to process P2P notary requests.
     */
    P2P_NOTARY("P2PNotary", 0x20);

    private String jsonValue;

    private byte byteValue;

    Role(String jsonValue, int byteValue) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
    }

    @JsonCreator
    public static Role fromJson(Object value) {
        if (value instanceof String) {
            return fromJsonValue((String) value);
        }
        if (value instanceof Integer) {
            return valueOf(((Integer) value).byteValue());
        }
        throw new IllegalArgumentException(format("%s value type not found.", Role.class.getName()));
    }

    public static Role valueOf(byte byteValue) {
        for (Role e : Role.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(format("%s value type not found.", Role.class.getName()));
    }

    public static Role fromJsonValue(String jsonValue) {
        for (Role e : Role.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(format("%s value type not found.", Role.class.getName()));
    }

    public byte byteValue() {
        return this.byteValue;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

}
