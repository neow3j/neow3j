package io.neow3j.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import static java.lang.String.format;

public enum TransactionAttributeType {

    /**
     * This attribute allows committee members to prioritize a transaction.
     */
    HIGH_PRIORITY(TransactionAttributeType.HIGH_PRIORITY_VALUE, 0x01, HighPriorityAttribute.class),

    /**
     * This attribute is used by oracle nodes to append oracle responses to a transaction.
     */
    ORACLE_RESPONSE(TransactionAttributeType.ORACLE_RESPONSE_VALUE, 0x11, OracleResponseAttribute.class),

    /**
     * This attribute is used to specify the earliest time a transaction can be included in a block.
     */
    NOT_VALID_BEFORE(TransactionAttributeType.NOT_VALID_BEFORE_VALUE, 0x20, NotValidBeforeAttribute.class),

    /**
     * This attribute is used to specify if it has a conflict with another transaction that might have already been
     * verified in the mempool.
     */
    CONFLICTS(TransactionAttributeType.CONFLICTS_VALUE, 0x21, ConflictsAttribute.class);

    public static final String HIGH_PRIORITY_VALUE = "HighPriority";
    public static final String ORACLE_RESPONSE_VALUE = "OracleResponse";
    public static final String NOT_VALID_BEFORE_VALUE = "NotValidBefore";
    public static final String CONFLICTS_VALUE = "Conflicts";

    private String jsonValue;
    private byte byteValue;
    private Class<? extends TransactionAttribute> clazz;

    TransactionAttributeType(String jsonValue, int byteValue,
            Class<? extends TransactionAttribute> clazz) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
        this.clazz = clazz;
    }

    public static TransactionAttributeType valueOf(byte byteValue) {
        for (TransactionAttributeType e : TransactionAttributeType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                format("%s value type not found.", TransactionAttributeType.class.getName()));
    }

    @JsonCreator
    public static TransactionAttributeType fromJson(Object value) {
        if (value instanceof String) {
            return fromJsonValue((String) value);
        }
        throw new IllegalArgumentException(
                format("%s value type not found.", TransactionAttributeType.class.getName()));
    }

    public static TransactionAttributeType fromJsonValue(String jsonValue) {
        for (TransactionAttributeType e : TransactionAttributeType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                format("%s value type not found.", TransactionAttributeType.class.getName()));
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public Class<? extends TransactionAttribute> clazz() {
        return this.clazz;
    }

}
