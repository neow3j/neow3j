package io.neow3j.transaction.exceptions;

public class TransactionConfigurationException extends RuntimeException {
    public TransactionConfigurationException(String message) {
        super(message);
    }

    public TransactionConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
