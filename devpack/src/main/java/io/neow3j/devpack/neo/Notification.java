package io.neow3j.devpack.neo;

import io.neow3j.devpack.ApiInterface;
import io.neow3j.devpack.Hash160;

/**
 * A notification consists of a script hash (the sender of the notification) and content itself
 * called 'state'. The state can be of any type (e.g. number, string, array). Thus, it is up to the
 * developer to perform the correct casts on the state object.
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


