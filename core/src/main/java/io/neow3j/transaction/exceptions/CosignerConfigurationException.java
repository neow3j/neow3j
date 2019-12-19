package io.neow3j.transaction.exceptions;

/**
 * Is thrown in case of invalid configurations when building a {@link
 * io.neow3j.transaction.Cosigner}.
 */
public class CosignerConfigurationException extends RuntimeException {

    public CosignerConfigurationException() {
        super();
    }

    public CosignerConfigurationException(String message) {
        super(message);
    }

    public CosignerConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
