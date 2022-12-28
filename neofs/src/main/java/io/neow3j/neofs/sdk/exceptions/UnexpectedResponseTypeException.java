package io.neow3j.neofs.sdk.exceptions;

import io.neow3j.neofs.lib.responses.ResponseType;

import static java.lang.String.format;

public class UnexpectedResponseTypeException extends RuntimeException {

    public UnexpectedResponseTypeException(String message) {
        super(message);
    }

    public UnexpectedResponseTypeException(ResponseType expectedType, String actualType) {
        this(format("Unexpected response type. Expected '%s' but was '%s'.", expectedType, actualType));
    }

    public UnexpectedResponseTypeException(ResponseType expectedType, ResponseType actualType) {
        this(expectedType, actualType.toString());
    }

    public UnexpectedResponseTypeException(ResponseType expectedType, String actualType, String responseStringValue) {
        this(format("Unexpected response type. Expected '%s' but was '%s' with value '%s'.", expectedType, actualType,
                responseStringValue));
    }

    public UnexpectedResponseTypeException(ResponseType expectedType, ResponseType actualType, String responseStringValue) {
        this(expectedType, actualType.toString(), responseStringValue);
    }

}
