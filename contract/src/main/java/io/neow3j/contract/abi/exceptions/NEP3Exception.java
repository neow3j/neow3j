package io.neow3j.contract.abi.exceptions;

/**
 * Base class for NEP3 exceptions.
 */
public class NEP3Exception extends Exception {

    public NEP3Exception(String message) {
        super(message);
    }

    public NEP3Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
