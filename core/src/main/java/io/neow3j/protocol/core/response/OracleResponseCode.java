package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import static java.lang.String.format;

public enum OracleResponseCode {

    /**
     * Indicates that the request has been successfully completed.
     */
    SUCCESS("Success", 0x00),

    /**
     * Indicates that the protocol of the request is not supported.
     */
    PROTOCOL_NOT_SUPPORTED("ProtocolNotSupported", 0x10),

    /**
     * Indicates that the oracle nodes cannot reach a consensus on the result of the request.
     */
    CONSENSUS_UNREACHABLE("ConsensusUnreachable", 0x12),

    /**
     * Indicates that the requested Uri does not exist.
     */
    NOT_FOUND("NotFound", 0x14),

    /**
     * Indicates that the request was not completed within the specified time.
     */
    TIMEOUT("Timeout", 0x16),

    /**
     * Indicates that there is no permission to request the resource.
     */
    FORBIDDEN("Forbidden", 0x18),

    /**
     * Indicates that the data for the response is too large.
     */
    RESPONSE_TOO_LARGE("ResponseTooLarge", 0x1a),

    /**
     * Indicates that the request failed due to insufficient balance.
     */
    INSUFFICIENT_FUNDS("InsufficientFunds", 0x1c),

    /**
     * Indicates that the content-type of the request is not supported.
     */
    CONTENT_TYPE_NOT_SUPPORTED("ContentTypeNotSupported", 0x1f),

    /**
     * Indicates that the request failed due to other errors.
     */
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
                format("%s value type not found.", OracleResponseCode.class.getName()));
    }

    public static OracleResponseCode valueOf(byte byteValue) {
        for (OracleResponseCode e : OracleResponseCode.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(format("%s value type not found.", OracleResponseCode.class.getName()));
    }

    public static OracleResponseCode fromJsonValue(String jsonValue) {
        for (OracleResponseCode e : OracleResponseCode.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                format("%s value type not found.", OracleResponseCode.class.getName()));
    }

    public byte byteValue() {
        return this.byteValue;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

}
