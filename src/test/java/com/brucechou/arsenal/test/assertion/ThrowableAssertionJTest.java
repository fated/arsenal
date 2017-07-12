package com.brucechou.arsenal.test.assertion;

import static com.brucechou.arsenal.test.assertion.ThrowableAssertionJ.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ThrowableAssertionJTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final String defaultMissingMessage = "Expected exception was not thrown.";
    private final String defaultUnexpectedMessage = "Unexpected exception was thrown:";
    private final String defaultNonNullMessage = "Thrower should not be null!";
    private final String errorMessage = "Test Error Message";

    private final IllegalArgumentException cause = new IllegalArgumentException(errorMessage);

    @Test
    public void testMissingExceptionWithDefaultMessage() throws Exception {
        exception.expect(MissingExceptionAssertionError.class);
        exception.expectMessage(defaultMissingMessage);
        assertThat(this::throwNothing).doThrow();
    }

    @Test
    public void testUnexpectedExceptionWithDefaultMessage() throws Exception {
        exception.expect(UnexpectedThrowableAssertionError.class);
        exception.expectMessage(defaultUnexpectedMessage);
        assertThat(this::throwSomethingWithoutCause).doNotThrow();
    }

    @Test
    public void testAssertionSuccessForExceptionWithoutCause() throws Exception {
        assertThat(this::throwSomethingWithoutCause)
                .doThrow()
                .isExactlyInstanceOf(RuntimeException.class)
                .isInstanceOf(Throwable.class)
                .hasMessage(errorMessage)
                .hasNoCause();
    }

    @Test
    public void testAssertionSuccessForExceptionWithCause() throws Exception {
        assertThat(() -> throwSomething(cause))
                .doThrow()
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage(errorMessage)
                .hasCauseExactlyInstanceOf(IllegalArgumentException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasCause(cause);
    }

    @Test
    public void testAssertionSuccessForNotThrow() throws Exception {
        assertThat(this::throwNothing).doNotThrow();
    }

    @Test
    public void testAssertionSuccessForNotThrowWithReturn() throws Exception {
        assertEquals(errorMessage, assertThat(this::throwNothingWithReturn).doNotThrow());
    }

    @Test
    public void testAssertThrowWithNullValue() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage(defaultNonNullMessage);
        assertThat((ThrowerWithResult<String>) null);
    }

    @Test
    public void testAssertNotThrowWithNullValue() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage(defaultNonNullMessage);
        assertThat((Thrower) null);
    }

    @Test
    public void testAssertThrowableWithNullValue() throws Exception {
        assertThat((Throwable) null).isNull();
    }

    @Test
    public void testAssertThrowableSuccess() throws Exception {
        assertThat(cause)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage(errorMessage)
                .hasNoCause();
    }

    private void throwNothing() {
        // this method throws nothing
    }

    private String throwNothingWithReturn() {
        // this method throws nothing
        return errorMessage;
    }

    private void throwSomethingWithoutCause() throws Throwable {
        throwSomething(null);
    }

    private void throwSomething(Exception cause) throws Exception {
        throw new RuntimeException(errorMessage, cause);
    }

}
