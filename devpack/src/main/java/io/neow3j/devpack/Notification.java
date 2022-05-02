package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;

/**
 * A notification consists of a script hash (the sender of the notification) as a {@link Hash160} and content itself
 * called 'state'. The state can be of any type (e.g. number, string, array). Thus, it is up to the developer to
 * perform the correct casts on the state object.
 */
public class Notification implements InteropInterface {

    /**
     * Sender script hash.
     */
    public final Hash160 scriptHash;

    /**
     * Notification's state
     */
    public final Object state;

    private Notification(Hash160 scriptHash, Object state) {
        this.scriptHash = scriptHash;
        this.state = state;
    }

    /**
     * Compares this notification to the given object. The comparison happens by reference only. Thus, if you
     * retrieve the same notification twice, e.g., with {@link io.neow3j.devpack.Runtime#getNotifications(Hash160)},
     * then comparing the two will return false.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same notification. False, otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

}
