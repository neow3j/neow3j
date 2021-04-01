package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NeoVMStateType {

    NONE("NONE"),

    HALT("HALT"),
    FAULT("FAULT"),
    BREAK("BREAK");

    private final String jsonValue;

    NeoVMStateType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

    public static NeoVMStateType fromJsonValue(String jsonValue) {
        for (NeoVMStateType e : NeoVMStateType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

}
