package io.neow3j.serialization.exceptions;

import io.neow3j.serialization.NeoSerializableInterface;

/**
 * Is thrown when an error occurs in the attempt to deserialize an object implementing the
 * {@link NeoSerializableInterface}.
 */
public class DeserializationException extends Exception {
    public DeserializationException() {
    }

    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeserializationException(Throwable cause) {
        super(cause);
    }

    public DeserializationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
