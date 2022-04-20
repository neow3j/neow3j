package io.neow3j.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Indicates the status of the VM.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum NeoVMStateType {

    /**
     * Indicates that the execution is in progress or has not yet begun.
     */
    NONE("NONE", 0),

    /**
     * Indicates that the execution has been completed successfully.
     */
    HALT("HALT", 1),

    /**
     * Indicates that the execution has ended, and an exception that cannot be caught is thrown.
     */
    FAULT("FAULT", 1 << 1),

    /**
     * Indicates that a breakpoint is currently being hit.
     */
    BREAK("BREAK", 1 << 2);

    private final String jsonValue;
    private final Integer intValue;

    NeoVMStateType(String jsonValue, int intValue) {
        this.jsonValue = jsonValue;
        this.intValue = intValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonValue
    public Integer intValue() {
        return intValue;
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

    public static NeoVMStateType fromIntValue(Integer intValue) {
        if (intValue == null) {
            return NONE;
        }
        for (NeoVMStateType e : NeoVMStateType.values()) {
            if (e.intValue.equals(intValue)) {
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
