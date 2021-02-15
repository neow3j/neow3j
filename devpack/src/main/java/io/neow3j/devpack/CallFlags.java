package io.neow3j.devpack;

/**
 * Defines flags for invoking smart contracts.
 */
public class CallFlags {

    public static final byte NONE = 0x00;
    public static final byte READ_STATES = 0b00000001;
    public static final byte WRITE_STATES = 0b00000010;
    public static final byte ALLOW_CALL = 0b00000100;
    public static final byte ALLOW_NOTIFY = 0b00001000;

    public static final byte STATES = READ_STATES | WRITE_STATES;
    public static final byte READ_ONLY = READ_STATES | ALLOW_CALL;
    public static final byte ALL = STATES | ALLOW_CALL | ALLOW_NOTIFY;

}
