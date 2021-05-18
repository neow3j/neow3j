package io.neow3j.types;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ContractParameterType {

    ANY("Any", 0x00),

    BOOLEAN("Boolean", 0x10),
    INTEGER("Integer", 0x11),
    BYTE_ARRAY("ByteArray", 0x12),
    STRING("String", 0x13),
    // 160-bit hash value
    HASH160("Hash160", 0x14),
    // 256-bit hash value
    HASH256("Hash256", 0x15),
    // Byte Array
    PUBLIC_KEY("PublicKey", 0x16),
    SIGNATURE("Signature", 0x17),

    // Object Array
    ARRAY("Array", 0x20),
    MAP("Map", 0x22),

    INTEROP_INTERFACE("InteropInterface", 0x30),

    VOID("Void", 0xff);

    private final String jsonValue;
    private final byte byteValue;

    ContractParameterType(String jsonValue, int v) {
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

    public static ContractParameterType valueOf(byte byteValue) {
        for (ContractParameterType e : ContractParameterType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

    public static ContractParameterType fromJsonValue(String jsonValue) {
        for (ContractParameterType e : ContractParameterType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

}
