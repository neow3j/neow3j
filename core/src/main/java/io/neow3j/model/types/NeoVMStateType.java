package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum NeoVMStateType {

    /**
     * Indicates that the execution is in progress or has not yet begun.
     */
    NONE("NONE"),

    /**
     * Indicates that the execution has been completed successfully.
     */
    HALT("HALT"),

    /**
     * Indicates that the execution has ended, and an exception that cannot be caught is thrown.
     */
    FAULT("FAULT"),

    /**
     * Indicates that a breakpoint is currently being hit.
     */
    BREAK("BREAK");

    private final String jsonValue;

    NeoVMStateType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static NeoVMStateType fromJsonValue(String jsonValue) {
        if (jsonValue == null || jsonValue.isEmpty()) {
            return NONE;
        }
        for (NeoVMStateType e : NeoVMStateType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return jsonValue;
    }

}
