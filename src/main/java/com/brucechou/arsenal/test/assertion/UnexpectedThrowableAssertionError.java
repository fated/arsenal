package com.brucechou.arsenal.test.assertion;

/**
 * Assertion error which is thrown when unexpected exception is thrown. The unexpected exception
 * will be stored in the cause of this exception.
 */
public class UnexpectedThrowableAssertionError extends AssertionError {

    private static final String DEFAULT_MESSAGE = "Unexpected exception was thrown: ";

    /**
     * create a new exception with default message.
     *
     * @param cause the unexpected exception
     */
    public UnexpectedThrowableAssertionError(Throwable cause) {
        this(DEFAULT_MESSAGE, cause);
    }

    /**
     * create a new exception with a customized message.
     *
     * @param message customized message
     * @param cause the unexpected exception
     */
    public UnexpectedThrowableAssertionError(String message, Throwable cause) {
        super(message + cause.toString(), cause);
    }

}
