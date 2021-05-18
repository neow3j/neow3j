package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OracleResponseCode {

    SUCCESS("Success", 0x00),
    PROTOCOL_NOT_SUPPORTED("ProtocolNotSupported", 0x10),
    CONSENSUS_UNREACHABLE("ConsensusUnreachable", 0x12),
    NOT_FOUND("NotFound", 0x14),
    TIMEOUT("Timeout", 0x16),
    FORBIDDEN("Forbidden", 0x18),
    RESPONSE_TOO_LARGE("ResponseTooLarge", 0x1a),
    INSUFFICIENT_FUNDS("InsufficientFunds", 0x1c),
    ERROR("Error", 0xff);

    private String jsonValue;

    private byte byteValue;

    OracleResponseCode(String jsonValue, int byteValue) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
    }

    @JsonCreator
    public static OracleResponseCode fromJson(Object value) {
        if (value instanceof String) {
            return fromJsonValue((String) value);
        }
        if (value instanceof Integer) {
            return valueOf(((Integer) value).byteValue());
        }
        throw new IllegalArgumentException(
                String.format("%s value type not found.", OracleResponseCode.class.getName()));
    }

    public static OracleResponseCode valueOf(byte byteValue) {
        for (OracleResponseCode e : OracleResponseCode.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                String.format("%s value type not found.", OracleResponseCode.class.getName()));
    }

    public static OracleResponseCode fromJsonValue(String jsonValue) {
        for (OracleResponseCode e : OracleResponseCode.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                String.format("%s value type not found.", OracleResponseCode.class.getName()));
    }

    public byte byteValue() {
        return this.byteValue;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

}
