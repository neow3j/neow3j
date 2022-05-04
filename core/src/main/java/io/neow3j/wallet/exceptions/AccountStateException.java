package io.neow3j.wallet.exceptions;

/**
 * Is thrown if an account is in an invalid state for the situation it is used in. E.g. when trying to sign a
 * transaction with an account that does not hold an decrypted private key.
 */
public class AccountStateException extends RuntimeException {

    public AccountStateException() {
        super();
    }

    public AccountStateException(String message) {
        super(message);
    }

    public AccountStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountStateException(Throwable cause) {
        super(cause);
    }

}
