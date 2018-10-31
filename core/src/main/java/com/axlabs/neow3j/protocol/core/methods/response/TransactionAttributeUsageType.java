package com.axlabs.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionAttributeUsageType {

    CONTRACT_HASH("ContractHash", 0x00),
    ECDH02("ECDH02", 0x02),
    ECDH03("ECDH03", 0x03),
    SCRIPT("Script", 0x20),
    VOTE("Vote", 0x30),
    DESCRIPTION_URL("DescriptionUrl", 0x81),
    DESCRIPTION("Description", 0x90),
    HASH1("Hash1", 0xa1),
    HASH2("Hash2", 0xa2),
    HASH3("Hash3", 0xa3),
    HASH4("Hash4", 0xa4),
    HASH5("Hash5", 0xa5),
    HASH6("Hash6", 0xa6),
    HASH7("Hash7", 0xa7),
    HASH8("Hash8", 0xa8),
    HASH9("Hash9", 0xa9),
    HASH10("Hash10", 0xaa),
    HASH11("Hash11", 0xab),
    HASH12("Hash12", 0xac),
    HASH13("Hash13", 0xad),
    HASH14("Hash14", 0xae),
    HASH15("Hash15", 0xaf),

    REMARK("Remark", 0xf0),
    REMARK1("Remark1", 0xf1),
    REMARK2("Remark2", 0xf2),
    REMARK3("Remark3", 0xf3),
    REMARK4("Remark4", 0xf4),
    REMARK5("Remark5", 0xf5),
    REMARK6("Remark6", 0xf6),
    REMARK7("Remark7", 0xf7),
    REMARK8("Remark8", 0xf8),
    REMARK9("Remark9", 0xf9),
    REMARK10("Remark10", 0xfa),
    REMARK11("Remark11", 0xfb),
    REMARK12("Remark12", 0xfc),
    REMARK13("Remark13", 0xfd),
    REMARK14("Remark14", 0xfe),
    REMARK15("Remark15", 0xff);

    private String jsonValue;
    private byte byteValue;

    TransactionAttributeUsageType(String jsonValue, int byteValue) {
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

    public static TransactionAttributeUsageType valueOf(byte byteValue) {
        for (TransactionAttributeUsageType e : TransactionAttributeUsageType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

    public static TransactionAttributeUsageType fromJsonValue(String jsonValue) {
        for (TransactionAttributeUsageType e : TransactionAttributeUsageType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

}
