package com.brucechou.arsenal.test.assertion;

/**
 * Assertion error which is thrown when expected exception is missing.
 */
public class MissingExceptionAssertionError extends AssertionError {

    private static String defaultMessage = "Expected exception was not thrown.";

    /**
     * create a new exception with default message.
     */
    public MissingExceptionAssertionError() {
        super(defaultMessage);
    }

    /**
     * create a new exception with a customized message.
     *
     * @param message customized message
     */
    public MissingExceptionAssertionError(String message) {
        super(message);
    }

}
