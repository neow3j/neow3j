package io.neow3j.protocol.exceptions;

import io.neow3j.protocol.core.Response.Error;

import static java.lang.String.format;

public class RpcResponseErrorException extends RuntimeException {

    public RpcResponseErrorException(Error error) {
        super(format("The Neo node responded with an error: %s", error));
    }

}
