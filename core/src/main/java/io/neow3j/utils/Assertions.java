package io.neow3j.utils;

/**
 * Assertion utility functions.
 */
public class Assertions {

    /**
     * Verify that the provided precondition holds true.
     *
     * @param assertionResult the assertion value.
     * @param errorMessage    the error message if the precondition fails.
     */
    public static void verifyPrecondition(boolean assertionResult, String errorMessage) {
        if (!assertionResult) {
            throw new RuntimeException(errorMessage);
        }
    }

}
