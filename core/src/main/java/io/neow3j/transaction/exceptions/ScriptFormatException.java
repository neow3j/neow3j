package io.neow3j.transaction.exceptions;

public class ScriptFormatException extends RuntimeException {

    public ScriptFormatException() {
        super();
    }

    public ScriptFormatException(String s) {
    }

    public ScriptFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptFormatException(Throwable cause) {
        super(cause);
    }

}
