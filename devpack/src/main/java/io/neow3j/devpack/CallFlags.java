package io.neow3j.devpack;

import java.io.WriteAbortedException;

/**
 * Defines flags for invoking smart contracts.
 */
public class CallFlags {

    public static final byte NONE = io.neow3j.model.types.CallFlags.NONE.getValue();
    public static final byte READ_STATES = io.neow3j.model.types.CallFlags.READ_STATES.getValue();
    public static final byte WRITE_STATES = io.neow3j.model.types.CallFlags.WRITE_STATES.getValue();
    public static final byte ALLOW_CALL = io.neow3j.model.types.CallFlags.ALLOW_CALL.getValue();
    public static final byte ALLOW_NOTIFY = io.neow3j.model.types.CallFlags.ALLOW_NOTIFY.getValue();

    public static final byte STATES = io.neow3j.model.types.CallFlags.STATES.getValue();
    public static final byte READ_ONLY = io.neow3j.model.types.CallFlags.READ_ONLY.getValue();
    public static final byte ALL = io.neow3j.model.types.CallFlags.ALL.getValue();

}
