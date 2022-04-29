package io.neow3j.crypto.exceptions;

/**
 * NEP2 specific account not found in the wallet.
 */
public class NEP2AccountNotFound extends Exception {

    public NEP2AccountNotFound() {
    }

    public NEP2AccountNotFound(String message) {
        super(message);
    }

    public NEP2AccountNotFound(String message, Throwable cause) {
        super(message, cause);
    }

}
