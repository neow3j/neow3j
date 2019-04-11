package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ContractParameterType {

    SIGNATURE("Signature", 0x00),
    BOOLEAN("Boolean", 0x01),
    INTEGER("Integer", 0x02),
    // 160-bit hash value
    HASH160("Hash160", 0x03),
    // 256-bit hash value
    HASH256("Hash256", 0x04),
    // Byte Array
    BYTE_ARRAY("ByteArray", 0x05),
    PUBLIC_KEY("PublicKey", 0x06),
    STRING("String", 0x07),

    // Object Array
    ARRAY("Array", 0x10),
    INTEROP_INTERFACE("InteropInterface", 0xf0),
    VOID("Void", 0xff);

    private String jsonValue;
    private byte byteValue;

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
