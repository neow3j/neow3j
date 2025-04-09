package io.neow3j.protocol.exceptions;

public class OfflineServiceException extends RuntimeException {

    private static final String EXCEPTION_MESSAGE = "Invalid neow3j service for this function. You cannot interact " +
            "with a Neo node using an OfflineService instance. If you are using a Neow3j instance, make sure to " +
            "provide a service (e.g., HttpService) that holds a valid endpoint to a Neo node when building Neow3j.";

    public OfflineServiceException() {
        super(EXCEPTION_MESSAGE);
    }

}
