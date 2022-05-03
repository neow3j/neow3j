package io.neow3j.types;

import static java.lang.String.format;

public enum CallFlags {

    NONE(0),
    READ_STATES(0b00000001),
    WRITE_STATES(0b00000010),
    ALLOW_CALL(0b00000100),
    ALLOW_NOTIFY(0b00001000),

    STATES(READ_STATES.value | WRITE_STATES.value),
    READ_ONLY(READ_STATES.value | ALLOW_CALL.value),
    ALL(STATES.value | ALLOW_CALL.value | ALLOW_NOTIFY.value);

    private final byte value;

    CallFlags(int value) {
        this.value = (byte) value;
    }

    public byte getValue() {
        return value;
    }

    public static CallFlags valueOf(byte value) {
        for (CallFlags e : CallFlags.values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                format("There exists no call flag with the provided byte value (%d)", value));
    }

}
