package io.neow3j.devpack;

/**
 * A notification consists of a script hash (the sender of the notification) as a {@link Hash160}
 * and content itself called 'state'. The state can be of any type (e.g. number, string, array).
 * Thus, it is up to the developer to perform the correct casts on the state object.
 */
public class Notification implements ApiInterface {

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

}
