package io.neow3j.model.types;

import static java.lang.String.format;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CallFlagsType {

    NONE(CallFlagsType.NONE_VALUE, 0),
    READ_STATES(CallFlagsType.READ_STATES_VALUE, 0b00000001),
    WRITE_STATES(CallFlagsType.WRITE_STATES_VALUE, 0b00000010),
    ALLOW_CALL(CallFlagsType.ALLOW_CALL_VALUE, 0b00000100),
    ALLOW_NOTIFY(CallFlagsType.ALLOW_NOTIFY_VALUE, 0b00001000),

    STATES(CallFlagsType.STATES_VALUE, READ_STATES.byteValue | WRITE_STATES.byteValue),
    READ_ONLY(CallFlagsType.READ_ONLY_VALUE, READ_STATES.byteValue | ALLOW_CALL.byteValue),
    ALL(CallFlagsType.ALL_VALUE, STATES.byteValue | ALLOW_CALL.byteValue | ALLOW_NOTIFY.byteValue);

    public static final String NONE_VALUE = "None";
    public static final String READ_STATES_VALUE = "ReadStates";
    public static final String WRITE_STATES_VALUE = "WriteStates";
    public static final String ALLOW_CALL_VALUE = "AllowCall";
    public static final String ALLOW_NOTIFY_VALUE = "AllowNotify";

    public static final String STATES_VALUE = "States";
    public static final String READ_ONLY_VALUE = "ReadOnly";
    public static final String ALL_VALUE = "All";

    private final String jsonValue;
    private final byte byteValue;

    CallFlagsType(String jsonValue, int byteValue) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
    }

    public static CallFlagsType valueOf(byte byteValue) {
        for (CallFlagsType e : CallFlagsType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(format("There exists no call flag with the provided "
                + "byte value (%d)", byteValue));
    }

    public static CallFlagsType fromJsonValue(String jsonValue) {
        for (CallFlagsType e : CallFlagsType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                "There exists no call flag with the provided json value." +
                        " The provided json value was " + jsonValue + ".");
    }

    public String getValue() {
        return this.jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

    public byte byteValue() {
        return byteValue;
    }
}
