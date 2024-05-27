package io.neow3j.types;

import com.fasterxml.jackson.annotation.JsonValue;

import static java.lang.String.format;

// This enum can also be found in the neow3j devpack module (io.neow3j.devpack.constants.StackItemType). Make sure to
// update it there too when things change.
public enum StackItemType {

    ANY(StackItemType.ANY_VALUE, 0x00),
    POINTER(StackItemType.POINTER_VALUE, 0x10),
    BOOLEAN(StackItemType.BOOLEAN_VALUE, 0x20),
    INTEGER(StackItemType.INTEGER_VALUE, 0x21),
    BYTE_STRING(StackItemType.BYTE_STRING_VALUE, 0x28),
    BUFFER(StackItemType.BUFFER_VALUE, 0x30),
    ARRAY(StackItemType.ARRAY_VALUE, 0x40),
    STRUCT(StackItemType.STRUCT_VALUE, 0x41),
    MAP(StackItemType.MAP_VALUE, 0x48),
    INTEROP_INTERFACE(StackItemType.INTEROP_INTERFACE_VALUE, 0x60);

    public static final String ANY_VALUE = "Any";
    public static final String POINTER_VALUE = "Pointer";
    public static final String BOOLEAN_VALUE = "Boolean";
    public static final String INTEGER_VALUE = "Integer";
    public static final String BYTE_STRING_VALUE = "ByteString";
    public static final String BUFFER_VALUE = "Buffer";
    public static final String ARRAY_VALUE = "Array";
    public static final String STRUCT_VALUE = "Struct";
    public static final String MAP_VALUE = "Map";
    public static final String INTEROP_INTERFACE_VALUE = "InteropInterface";

    private final String jsonValue;
    private final byte byteValue;

    StackItemType(String jsonValue, int v) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) v;
    }

    public String getValue() {
        return this.jsonValue;
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
        throw new IllegalArgumentException(format("There exists no stack item with the provided byte value. The " +
                "provided byte value was %s.", byteValue));
    }

    public static StackItemType fromJsonValue(String jsonValue) {
        for (StackItemType e : StackItemType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(format("There exists no stack item with the provided json value. The " +
                "provided json value was %s.", jsonValue));
    }

}
