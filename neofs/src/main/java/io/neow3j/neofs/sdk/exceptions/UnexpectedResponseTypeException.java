package io.neow3j.neofs.sdk.exceptions;

import io.neow3j.neofs.lib.responses.ExpectedResponseType;

import static java.lang.String.format;

/**
 * Thrown when the shared-lib returns a response type that was unexpected.
 */
public class UnexpectedResponseTypeException extends RuntimeException {

    public UnexpectedResponseTypeException(String message) {
        super(message);
    }

    public UnexpectedResponseTypeException(ExpectedResponseType expectedType, String actualType) {
        this(format("Unexpected response type. Expected '%s' but was '%s'.", expectedType, actualType));
    }

    public UnexpectedResponseTypeException(ExpectedResponseType expectedType, ExpectedResponseType actualType) {
        this(expectedType, actualType.toString());
    }

    public UnexpectedResponseTypeException(ExpectedResponseType expectedType, String actualType, String responseStringValue) {
        this(format("Unexpected response type. Expected '%s' but was '%s' with value '%s'.", expectedType, actualType,
                responseStringValue));
    }

    public UnexpectedResponseTypeException(ExpectedResponseType expectedType, ExpectedResponseType actualType, String responseStringValue) {
        this(expectedType, actualType.toString(), responseStringValue);
    }

}
