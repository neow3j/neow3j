package io.neow3j.abi.exceptions;

/**
 * NEP3 parsing exception or invalid format.
 */
public class NEP3ParsingException extends NEP3Exception {

    public NEP3ParsingException(String message) {
        super(message);
    }

    public NEP3ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
