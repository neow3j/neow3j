package io.neow3j.contract.exceptions;

public class InvocationConfigurationException extends RuntimeException {

    public InvocationConfigurationException() {
        super();
    }

    public InvocationConfigurationException(String message) {
        super(message);
    }

    public InvocationConfigurationException(Throwable cause) {
        super(cause);
    }

    public InvocationConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
