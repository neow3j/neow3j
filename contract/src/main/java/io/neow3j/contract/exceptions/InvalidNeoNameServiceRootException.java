package io.neow3j.contract.exceptions;

import static java.lang.String.format;

public class InvalidNeoNameServiceRootException extends Exception {

    public InvalidNeoNameServiceRootException(String invalidRoot) {
        super(format("'%s' is not a valid NNS root.", invalidRoot));
    }

}
