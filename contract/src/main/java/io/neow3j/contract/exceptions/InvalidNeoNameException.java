package io.neow3j.contract.exceptions;

import static java.lang.String.format;

/**
 * Is thrown when instantiating a {@link io.neow3j.contract.types.NNSName} with an invalid domain name.
 */
public class InvalidNeoNameException extends RuntimeException {

    public InvalidNeoNameException(String invalidName) {
        super(format("'%s' is not a valid NNS name.", invalidName));
    }

}
