package io.neow3j.neofs.lib.exceptions;

/**
 * This exception is thrown if the native NeoFS library returns a type that is not a specified in
 * {@link io.neow3j.neofs.lib.responses.ResponseType}.
 */
public class NeoFSLibException extends RuntimeException {

    public NeoFSLibException(String message) {
        super(message);
    }

}
