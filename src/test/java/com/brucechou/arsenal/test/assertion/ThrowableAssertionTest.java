package com.brucechou.arsenal.test.assertion;

import static com.brucechou.arsenal.test.assertion.ThrowableAssertion.assertNotThrow;
import static com.brucechou.arsenal.test.assertion.ThrowableAssertion.assertThrown;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ThrowableAssertionTest extends Assert {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final String defaultMissingMessage = "Expected exception was not thrown.";
    private final String defaultUnexpectedMessage = "Unexpected exception was thrown:";
    private final String errorMessage = "Test Error Message";
    private final String wrongMsgPart = "ABC";

    private final IllegalArgumentException cause = new IllegalArgumentException(errorMessage);

    @Test
    public void testMissingExceptionWithDefaultMessage() throws Exception {
        exception.expect(MissingExceptionAssertionError.class);
        exception.expectMessage(defaultMissingMessage);
        assertThrown(this::throwNothing);
    }

    @Test
    public void testMissingExceptionWithCustomizedMessage() throws Exception {
        final String message = "Wanted exception was not thrown.";

        exception.expect(MissingExceptionAssertionError.class);
        exception.expectMessage(message);
        assertThrown(message, this::throwNothing);
    }

    @Test
    public void testMissingExceptionWithEmptyMessage() throws Exception {
        final String message = "    ";

        exception.expect(MissingExceptionAssertionError.class);
        exception.expectMessage(defaultMissingMessage);
        assertThrown(message, this::throwNothing);
    }

    @Test
    public void testUnexpectedExceptionWithDefaultMessage() throws Exception {
        exception.expect(UnexpectedThrowableAssertionError.class);
        exception.expectMessage(defaultUnexpectedMessage);
        assertNotThrow(this::throwSomethingWithoutCause);
    }

    @Test
    public void testUnexpectedExceptionWithCustomizedMessage() throws Exception {
        final String message = "Unwanted exception was thrown:";

        exception.expect(UnexpectedThrowableAssertionError.class);
        exception.expectMessage(message);
        assertNotThrow(message, this::throwSomethingWithoutCause);
    }

    @Test
    public void testUnexpectedExceptionWithEmptyMessage() throws Exception {
        final String message = "    ";

        exception.expect(UnexpectedThrowableAssertionError.class);
        exception.expectMessage(defaultUnexpectedMessage);
        assertNotThrow(message, this::throwSomethingWithoutCause);
    }

    @Test
    public void testAssertionSuccessForExceptionWithoutCause() throws Exception {
        assertThrown(this::throwSomethingWithoutCause)
                .expect(RuntimeException.class)
                .expect(isA(Throwable.class))
                .expectMessage(errorMessage)
                .expectNoCause();
    }

    @Test
    public void testAssertionSuccessForExceptionWithCause() throws Exception {
        assertThrown(() -> throwSomething(cause))
                .expect(RuntimeException.class)
                .expectMessage(errorMessage)
                .expectCause(IllegalArgumentException.class)
                .expectCause(isA(RuntimeException.class))
                .expectCause(sameInstance(cause));
    }

    @Test
    public void testAssertionSuccessForNotThrow() throws Exception {
        assertNotThrow(this::throwNothing);
    }

    @Test
    public void testExtractFromOuterException() throws Exception {
        assertThrown(() -> throwSomething(cause))
                .extractFrom(RuntimeException.class)
                .expect(IllegalArgumentException.class)
                .expectMessage(errorMessage)
                .expectNoCause();
    }

    @Test
    public void testExtractFromMismatchException() throws Exception {
        exception.expect(AssertionError.class);
        assertThrown(() -> throwSomething(cause))
                .extractFrom(IllegalArgumentException.class)
                .expect(IllegalArgumentException.class)
                .expectMessage(errorMessage)
                .expectNoCause();
    }

    @Test
    public void testExtractFromWithNoCause() throws Exception {
        exception.expect(RuntimeException.class);
        assertThrown(this::throwSomethingWithoutCause)
                .extractFrom(RuntimeException.class);
    }

    @Test
    public void testExpectTypeAssertionError() throws Exception {
        exception.expect(AssertionError.class);
        assertThrown(this::throwSomethingWithoutCause)
                .expect(IllegalArgumentException.class);
    }

    @Test
    public void testExpectTypeAssertionErrorWithSuperClass() throws Exception {
        exception.expect(AssertionError.class);
        assertThrown(this::throwSomethingWithoutCause)
                .expect(Exception.class);
    }

    @Test
    public void testExpectMatcherAssertionError() throws Exception {
        exception.expect(AssertionError.class);
        assertThrown(this::throwSomethingWithoutCause)
                .expect(nullValue(Throwable.class));
    }

    @Test
    public void testExpectMessageAssertionError() throws Exception {
        exception.expect(AssertionError.class);
        assertThrown(this::throwSomethingWithoutCause)
                .expectMessage(wrongMsgPart);
    }

    @Test
    public void testExpectMessageMatcherAssertionError() throws Exception {
        exception.expect(AssertionError.class);
        assertThrown(this::throwSomethingWithoutCause)
                .expectMessage(startsWith(wrongMsgPart));
    }

    @Test
    public void testExpectCauseAssertionError() throws Exception {
        exception.expect(AssertionError.class);
        assertThrown(() -> throwSomething(cause))
                .expectCause(IllegalAccessException.class);
    }

    @Test
    public void testExpectCauseAssertionErrorWithSuperClass() throws Exception {
        exception.expect(AssertionError.class);
        assertThrown(() -> throwSomething(cause))
                .expectCause(RuntimeException.class);
    }

    @Test
    public void testExpectCauseMatcherAssertionError() throws Exception {
        exception.expect(AssertionError.class);
        assertThrown(() -> throwSomething(new IllegalArgumentException()))
                .expectCause(sameInstance(cause));
    }

    private void throwNothing() {
        // this method throws nothing
    }

    private void throwSomethingWithoutCause() throws Throwable {
        throwSomething(null);
    }

    private void throwSomething(Throwable cause) throws Throwable {
        throw new RuntimeException(errorMessage, cause);
    }

}
