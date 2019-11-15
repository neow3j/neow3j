package io.neow3j.transaction.exceptions;

public class MissingSenderAccountException extends RuntimeException {
    public MissingSenderAccountException(String message) {
        super(message);
    }

    public MissingSenderAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
