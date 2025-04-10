package io.neow3j.protocol.exceptions;

/**
 * This exception is thrown when a method that requires a connection to a Neo node is called on an
 * {@link io.neow3j.protocol.OfflineService} instance.
 * <p>
 * If you want to connect to a Neo node, make sure to use a {@link io.neow3j.protocol.Neow3jService} implementation
 * that can connect to a node.
 */
public class OfflineServiceException extends RuntimeException {

    private static final String EXCEPTION_MESSAGE = "Invalid neow3j service for this function. You cannot interact " +
            "with a Neo node using an OfflineService instance. If you are using a Neow3j instance, make sure to " +
            "provide a service (e.g., HttpService) that holds a valid endpoint to a Neo node when building Neow3j.";

    /**
     * Creates a new {@link OfflineServiceException} with the default message.
     */
    public OfflineServiceException() {
        super(EXCEPTION_MESSAGE);
    }

}
