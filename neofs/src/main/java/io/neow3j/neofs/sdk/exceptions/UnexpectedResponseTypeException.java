package io.neow3j.neofs.sdk.exceptions;

import io.neow3j.neofs.lib.responses.PointerResponse;
import io.neow3j.neofs.lib.responses.ResponseType;
import io.neow3j.neofs.lib.responses.StringResponse;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getResponseBytes;
import static java.lang.String.format;

public class UnexpectedResponseTypeException extends Exception {

    public UnexpectedResponseTypeException(String message) {
        super(message);
    }

    public UnexpectedResponseTypeException(String expectedType, String actualType) {
        this(format("Unexpected response type. Expected '%s' but was '%s'.", expectedType, actualType));
    }

    public UnexpectedResponseTypeException(ResponseType expectedType, ResponseType actualType) {
        this(format("Unexpected response type. Expected '%s' but was '%s'.", expectedType, actualType));
    }

    public UnexpectedResponseTypeException(StringResponse response, String expectedType) {
        this(response.value, expectedType);
    }

    public UnexpectedResponseTypeException(PointerResponse response, String expectedType) {
        this(new String(getResponseBytes(response)), expectedType);
    }

}
