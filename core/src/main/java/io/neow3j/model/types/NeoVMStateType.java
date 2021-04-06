package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
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

}
