package io.neow3j.devpack.constants;

/**
 * Defines flags for invoking smart contracts.
 */
public class CallFlags {

    public static final byte NONE = 0;
    public static final byte READ_STATES = 1;
    public static final byte WRITE_STATES = 1 << 1;
    public static final byte ALLOW_CALL = 1 << 2;
    public static final byte ALLOW_NOTIFY = 1 << 3;

    public static final byte STATES = READ_STATES | WRITE_STATES;
    public static final byte READ_ONLY = READ_STATES | ALLOW_CALL;
    public static final byte ALL = STATES | ALLOW_CALL | ALLOW_NOTIFY;

}
