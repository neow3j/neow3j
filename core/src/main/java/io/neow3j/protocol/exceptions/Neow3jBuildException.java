package io.neow3j.protocol.exceptions;

import io.neow3j.protocol.Neow3j;

import static java.lang.String.format;

/**
 * This exception is thrown when a {@link Neow3j} instance cannot be built.
 */
public class Neow3jBuildException extends RuntimeException {

    /**
     * Creates a new {@link Neow3jBuildException} with the given message.
     *
     * @param message the additional message explaining the reason for the exception.
     */
    public Neow3jBuildException(String message) {
        super(format("Could not build Neow3j instance: %s", message));
    }

}
