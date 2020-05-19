package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionAttributeType {

    COSIGNER("Cosigner", 0x01);

    private String jsonValue;
    private byte byteValue;

    TransactionAttributeType(String jsonValue, int byteValue) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    @JsonCreator
    public static TransactionAttributeType fromJson(Object value) {
        if (value instanceof String) {
            return fromJsonValue((String) value);
        }
        if (value instanceof Integer) {
            return valueOf(((Integer) value).byteValue());
        }
        throw new IllegalArgumentException(String.format("%s value type not found.", TransactionAttributeType.class.getName()));
    }

    public static TransactionAttributeType valueOf(byte byteValue) {
        for (TransactionAttributeType e : TransactionAttributeType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("%s value type not found.", TransactionAttributeType.class.getName()));
    }

    public static TransactionAttributeType fromJsonValue(String jsonValue) {
        for (TransactionAttributeType e : TransactionAttributeType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("%s value type not found.", TransactionAttributeType.class.getName()));
    }

}
