package io.neow3j.transaction.exceptions;

public class CosignerConfigurationException extends RuntimeException {
    public CosignerConfigurationException(String message) {
       super(message);
    }

    public CosignerConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
