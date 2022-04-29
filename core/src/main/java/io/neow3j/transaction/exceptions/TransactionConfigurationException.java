package io.neow3j.transaction.exceptions;

/**
 * Is thrown in case of invalid configurations when building a {@link
 * io.neow3j.transaction.Transaction}.
 */
public class TransactionConfigurationException extends RuntimeException {

    public TransactionConfigurationException() {
        super();
    }

    public TransactionConfigurationException(String message) {
        super(message);
    }

    public TransactionConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
