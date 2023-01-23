package io.neow3j.neofs.lib.exceptions;

/**
 * This exception is thrown if the native NeoFS library returns a type that is not an expected response type.
 */
public class NeoFSLibException extends RuntimeException {

    public NeoFSLibException(String message) {
        super(message);
    }

}
