package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StackItemType {

    BYTE_ARRAY("ByteArray", 0x00),
    BOOLEAN("Boolean", 0x01),
    INTEGER("Integer", 0x02),
    INTEROP_INTERFACE("InteropInterface", 0x40),
    ARRAY("Array", 0x80),
    STRUCT("Struct", 0x81),
    MAP("Map", 0x82);

    private String jsonValue;
    private byte byteValue;

    StackItemType(String jsonValue, int v) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) v;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public static StackItemType valueOf(byte byteValue) {
        for (StackItemType e : StackItemType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

    public static StackItemType fromJsonValue(String jsonValue) {
        for (StackItemType e : StackItemType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

}
