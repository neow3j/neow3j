package io.neow3j.transaction.exceptions;

import io.neow3j.transaction.Signer;

/**
 * Is thrown in case of invalid configurations when building a {@link Signer}.
 */
public class SignerConfigurationException extends RuntimeException {

    public SignerConfigurationException() {
        super();
    }

    public SignerConfigurationException(String message) {
        super(message);
    }

    public SignerConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
