package io.neow3j.crypto.exceptions;

/**
 * NEP2 format exception.
 */
public class NEP2InvalidPassphrase extends Exception {

    public NEP2InvalidPassphrase() {
    }

    public NEP2InvalidPassphrase(String message) {
        super(message);
    }

    public NEP2InvalidPassphrase(String message, Throwable cause) {
        super(message, cause);
    }

}
