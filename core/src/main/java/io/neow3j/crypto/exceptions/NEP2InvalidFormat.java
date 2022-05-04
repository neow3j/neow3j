package io.neow3j.crypto.exceptions;

/**
 * NEP2 format exception.
 */
public class NEP2InvalidFormat extends Exception {

    public NEP2InvalidFormat() {
    }

    public NEP2InvalidFormat(String message) {
        super(message);
    }

    public NEP2InvalidFormat(String message, Throwable cause) {
        super(message, cause);
    }

}
