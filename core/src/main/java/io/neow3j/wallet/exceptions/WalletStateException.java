package io.neow3j.wallet.exceptions;

public class WalletStateException extends RuntimeException {

    public WalletStateException() {
        super();
    }

    public WalletStateException(String message) {
        super(message);
    }

    public WalletStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public WalletStateException(Throwable cause) {
        super(cause);
    }

}
