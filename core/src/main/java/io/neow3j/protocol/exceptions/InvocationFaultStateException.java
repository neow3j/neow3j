package io.neow3j.protocol.exceptions;

public class InvocationFaultStateException extends RuntimeException {

    private static final String invocationFaultMessage =
            "The invocation resulted in a FAULT VM state. The VM exited due to the following exception: ";

    public InvocationFaultStateException(String exception) {
        super(invocationFaultMessage + exception);
    }

    public InvocationFaultStateException(String customMessage, String exception) {
        super(customMessage + " " + exception);
    }

}
