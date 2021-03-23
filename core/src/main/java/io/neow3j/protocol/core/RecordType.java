package io.neow3j.protocol.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RecordType {

    /* region [RFC 1035](https://tools.ietf.org/html/rfc1035) */

    /**
     * An address record.
     */
    A("A", 0x01),

    /**
     * A canonical name record.
     */
    CNAME("CNAME", 0x05),

    /**
     * A text record.
     */
    TXT("TXT", 0x10),

    /* endregion */

    /* region [RFC 3596](https://tools.ietf.org/html/rfc3596) */

    /**
     * An IPv6 address record.
     */
    AAAA("AAAA", 0x1C);

    /* endregion */

    private final String jsonValue;

    private final byte byteValue;

    RecordType(String jsonValue, int byteValue) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
    }

    @JsonCreator
    public static RecordType fromJson(Object value) {
        if (value instanceof String) {
            return fromJsonValue((String) value);
        }
        if (value instanceof Integer) {
            return valueOf(((Integer) value).byteValue());
        }
        throw new IllegalArgumentException(
                String.format("%s value type not found.", RecordType.class.getName()));
    }

    public static RecordType valueOf(byte byteValue) {
        for (RecordType e : RecordType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                String.format("%s value type not found.", RecordType.class.getName()));
    }

    public static RecordType fromJsonValue(String jsonValue) {
        for (RecordType e : RecordType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                String.format("%s value type not found.", RecordType.class.getName()));
    }

    public byte byteValue() {
        return this.byteValue;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }
}
