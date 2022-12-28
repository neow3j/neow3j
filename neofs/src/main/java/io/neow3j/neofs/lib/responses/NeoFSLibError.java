package io.neow3j.neofs.lib.responses;

/**
 * This class is used when the native NeoFS library returns an error (i.e., {@link ResponseType#ERROR}).
 */
public class NeoFSLibError {

    private final String message;

    public NeoFSLibError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
