package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionAttributeUsageType {

    CONTRACT_HASH("ContractHash", 0x00, 32, 32),
    ECDH02("ECDH02", 0x02, 32, 32),
    ECDH03("ECDH03", 0x03, 32, 32),
    SCRIPT("Script", 0x20, 20, 20),
    VOTE("Vote", 0x30, null, null),
    DESCRIPTION_URL("DescriptionUrl", 0x81, 255, null),
    DESCRIPTION("Description", 0x90, 65535, null),
    HASH1("Hash1", 0xa1, 32, 32),
    HASH2("Hash2", 0xa2, 32, 32),
    HASH3("Hash3", 0xa3, 32, 32),
    HASH4("Hash4", 0xa4, 32, 32),
    HASH5("Hash5", 0xa5, 32, 32),
    HASH6("Hash6", 0xa6, 32, 32),
    HASH7("Hash7", 0xa7, 32, 32),
    HASH8("Hash8", 0xa8, 32, 32),
    HASH9("Hash9", 0xa9, 32, 32),
    HASH10("Hash10", 0xaa, 32, 32),
    HASH11("Hash11", 0xab, 32, 32),
    HASH12("Hash12", 0xac, 32, 32),
    HASH13("Hash13", 0xad, 32, 32),
    HASH14("Hash14", 0xae, 32, 32),
    HASH15("Hash15", 0xaf, 32, 32),

    REMARK("Remark", 0xf0, 65535, null),
    REMARK1("Remark1", 0xf1, 65535, null),
    REMARK2("Remark2", 0xf2, 65535, null),
    REMARK3("Remark3", 0xf3, 65535, null),
    REMARK4("Remark4", 0xf4, 65535, null),
    REMARK5("Remark5", 0xf5, 65535, null),
    REMARK6("Remark6", 0xf6, 65535, null),
    REMARK7("Remark7", 0xf7, 65535, null),
    REMARK8("Remark8", 0xf8, 65535, null),
    REMARK9("Remark9", 0xf9, 65535, null),
    REMARK10("Remark10", 0xfa, 65535, null),
    REMARK11("Remark11", 0xfb, 65535, null),
    REMARK12("Remark12", 0xfc, 65535, null),
    REMARK13("Remark13", 0xfd, 65535, null),
    REMARK14("Remark14", 0xfe, 65535, null),
    REMARK15("Remark15", 0xff, 65535, null);

    private String jsonValue;
    private byte byteValue;
    private Integer maxDataLength;
    private Integer fixedDataLength;

    TransactionAttributeUsageType(String jsonValue, int byteValue, Integer maxDataLength,
                                  Integer fixedDataLength) {

        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
        this.maxDataLength = maxDataLength;
        this.fixedDataLength = fixedDataLength;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    /**
     * @return The length in bytes that this attribute usage type should have maximally. For some
     * types this is also the exact length they must have.
     */
    public Integer maxDataLength() {
        return this.maxDataLength;
    }

    public Integer fixedDataLength() {
        return this.fixedDataLength;
    }

    @JsonCreator
    public static TransactionAttributeUsageType fromJson(Object value) {
        if (value instanceof String) {
            return fromJsonValue((String) value);
        }
        if (value instanceof Integer) {
            return valueOf(((Integer) value).byteValue());
        }
        throw new IllegalArgumentException(String.format("%s value type not found.", TransactionAttributeUsageType.class.getName()));
    }

    public static TransactionAttributeUsageType valueOf(byte byteValue) {
        for (TransactionAttributeUsageType e : TransactionAttributeUsageType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("%s value type not found.", TransactionAttributeUsageType.class.getName()));
    }

    public static TransactionAttributeUsageType fromJsonValue(String jsonValue) {
        for (TransactionAttributeUsageType e : TransactionAttributeUsageType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("%s value type not found.", TransactionAttributeUsageType.class.getName()));
    }

}
