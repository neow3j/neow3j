package io.neow3j.protocol.exceptions;

public class StackItemCastException extends RuntimeException {

    public StackItemCastException(String message) {
        super(message);
    }

    public StackItemCastException(Exception e) {
        super(e);
    }

}
