package io.neow3j.neofs.lib.exceptions;

import io.neow3j.neofs.lib.responses.ResponseType;

/**
 * This exception is thrown if the native NeoFS library returns a type that is not a specified in
 * {@link ResponseType}.
 */
public class NeoFSLibException extends RuntimeException {

    public NeoFSLibException(String message) {
        super(message);
    }

}
