package io.neow3j.devpack.framework;

/**
 * A notification consists of a script hash (the sender of the notification) and content itself
 * called 'state'. The state can be of any type (e.g. number, string, array). Thus, it is up to the
 * developer to perform the correct casts on the state object.
 */
public class Notification {

    private Notification(byte[] scriptHash, Object state) {
        this.scriptHash = scriptHash;
        this.state = state;
    }

    /**
     * Sender script hash.
     */
    public final byte[] scriptHash;

    /**
     * Notification's state
     */
    public final Object state;

}


