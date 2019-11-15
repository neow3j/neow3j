package io.neow3j.transaction.exceptions;

public class MissingValidUntilBlockException extends RuntimeException {
    public MissingValidUntilBlockException(String message) {
        super(message);
    }

    public MissingValidUntilBlockException(String message, Throwable cause) {
        super(message, cause);
    }
}
