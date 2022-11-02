package io.neow3j.contract.exceptions;

import static java.lang.String.format;

/**
 * Is thrown when instantiating a {@link io.neow3j.contract.types.NNSName.NNSRoot} with an invalid root.
 */
public class InvalidNeoNameServiceRootException extends RuntimeException {

    public InvalidNeoNameServiceRootException(String invalidRoot) {
        super(format("'%s' is not a valid NNS root.", invalidRoot));
    }

}
