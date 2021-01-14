package io.neow3j.protocol.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DesignationRole {

    STATE_VALIDATOR("StateValidator", 0x04),
    ORACLE("Oracle", 0x08);

    private String jsonValue;

    private byte byteValue;

    DesignationRole(String jsonValue, int byteValue) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
    }

    @JsonCreator
    public static DesignationRole fromJson(Object value) {
        if (value instanceof String) {
            return fromJsonValue((String) value);
        }
        if (value instanceof Integer) {
            return valueOf(((Integer) value).byteValue());
        }
        throw new IllegalArgumentException(
                String.format("%s value type not found.", DesignationRole.class.getName()));
    }

    public static DesignationRole valueOf(byte byteValue) {
        for (DesignationRole e : DesignationRole.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                String.format("%s value type not found.", DesignationRole.class.getName()));
    }

    public static DesignationRole fromJsonValue(String jsonValue) {
        for (DesignationRole e : DesignationRole.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                String.format("%s value type not found.", DesignationRole.class.getName()));
    }

    public byte byteValue() {
        return this.byteValue;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

}
