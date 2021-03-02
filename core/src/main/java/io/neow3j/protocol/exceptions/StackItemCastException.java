package io.neow3j.protocol.exceptions;

import io.neow3j.protocol.core.methods.response.IntegerStackItem;
import io.neow3j.protocol.core.methods.response.StackItem;

public class StackItemCastException extends RuntimeException {

    public StackItemCastException(String message) {
        super(message);
    }

}
