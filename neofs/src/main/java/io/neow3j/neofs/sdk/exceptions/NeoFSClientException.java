package io.neow3j.neofs.sdk.exceptions;

/**
 * Thrown when something went wrong with interacting with the shared-lib or if the shared-lib returns an error.
 */
public class NeoFSClientException extends RuntimeException {

    public NeoFSClientException(String message) {
        super(message);
    }

}
