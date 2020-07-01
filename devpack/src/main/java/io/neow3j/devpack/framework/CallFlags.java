package io.neow3j.devpack.framework;

/**
 * Defines flags for invoking smart contracts.
 */
public class CallFlags {

    public static final byte NONE = 0x00;

    public static final byte ALLOW_MODIFY_STATES = 0b00000001;

    public static final byte ALLOW_CALL = 0b00000010;

    public static final byte ALLOW_NOTIFY = 0b00000100;

    public static final byte READ_ONLY = ALLOW_CALL | ALLOW_NOTIFY;

    public static final byte ALL = ALLOW_MODIFY_STATES | ALLOW_CALL | ALLOW_NOTIFY;

}
