package com.brucechou.arsenal.test.assertion;

import org.assertj.core.api.ThrowableAssert;

import java.util.Objects;

/**
 * The {@link ThrowableAssertionJ} allows you to verify that your code
 * throws a specific exception or not in AssertJ's style.
 * <h3>Usage</h3>
 *
 * <pre>
 * public class SimpleThrowableAssertionJTest {
 *
 *     &#064;Test (expected = MissingExceptionAssertionError.class) // making test pass
 *     public void throwsNothing() {
 *         // no exception expected, will throw MissingExceptionAssertionError.
 *         assertThat(() -&gt; System.out.println()).doThrow();
 *     }
 *
 *     &#064;Test
 *     public void throwsExceptionWithSpecificType() {
 *         assertThat(() -&gt; {
 *             throw new NullPointerException();
 *         }).doThrow().isExactlyInstanceOf(NullPointerException.class);
 *     }
 *
 *     &#064;Test
 *     public void throwsExceptionWithMethodReference() {
 *         assertThat(new DummyService()::someMethod).doThrow().isExactlyInstanceOf(NullPointerException.class);
 *     }
 *
 *     &#064;Test
 *     public void throwsExceptionWithLambdaExpression() {
 *         assertThat(() -&gt; new DummyService().someOtherMethod())
 *             .doThrow().isExactlyInstanceOf(NullPointerException.class);
 *     }
 *
 *     &#064;Test
 *     public void throwsExceptionWithConstructorReference() {
 *         assertThat(SomeOtherDummyService::new).doThrow().isExactlyInstanceOf(NullPointerException.class);
 *     }
 *
 *     &#064;Test
 *     public void throwsExceptionWithLambdaConstructor() {
 *         assertThat(() -&gt; new SomeOtherDummyService()).doThrow().isExactlyInstanceOf(NullPointerException.class);
 *     }
 *
 * }
 * </pre>
 *
 * <p>You can use {@code assertThat} in your test just like other assertions in AssertJ. You can add your testing
 * code as parameter. {@code Thrower} is a {@code @FunctionalInterface} which instances can be created with
 * lambda expressions, method references, or constructor references. {@code assertThat} accepting
 * {@code Thrower} will expect and be ready to handle an exception.
 *
 * <p>After specifying the type of the expected exception your test is successful when such an exception
 * is thrown and it fails if a different or no exception is thrown.
 *
 * <p>Instead of specifying the exception's type you can characterize the
 * expected exception based on other AssertJ criteria, too:
 *
 * <p>You can chain any of the presented assertion-methods just like AssertJ.
 * The assertion is successful if all specifications are met.
 *
 * <pre>
 * &#064;Test
 * public void throwsException() {
 *     assertThat(() -&gt; {
 *         throw new NullPointerException(&quot;What happened?&quot;);
 *     }).doThrow().isInstanceOf(NullPointerException.class).hasMessage(&quot;happened&quot;).hasNoCause();
 * }
 * </pre>
 *
 * <h3>AssertionErrors</h3>
 *
 * <p>ThrowableAssertionJ uses {@link AssertionError}s for indicating that an assertion is failing.
 * E.g. the following test fails because of the {@code isInstanceOf()} statement.
 *
 * <pre>
 * &#064;Test
 * public void throwsUnhandled() {
 *     assertThat(() -&gt; {
 *         throw new IllegalArgumentException();
 *     }).doThrow().isInstanceOf(NullPointerException.class) // throws AssertionError
 * }
 * </pre>
 *
 * <h3>Missing Exceptions</h3>
 *
 * <p>By default missing exceptions are reported with an error message like "Expected exception was not thrown.".
 */
public class ThrowableAssertionJ<R> {

    private final ThrowerWithResult<R> throwerWithResult;

    /**
     * Instantiate an AssertJ's {@link ThrowableAssert} wrapper.
     *
     * @param throwerWithResult a throwable needs to be asserted
     */
    private ThrowableAssertionJ(ThrowerWithResult<R> throwerWithResult) {
        this.throwerWithResult = throwerWithResult;
    }

    /**
     * An AssertJ style method to trigger a throwable assertion, require a non-null thrower.
     *
     * @param thrower non-null thrower with returned value
     * @param <V> type of return value
     * @return an AssertJ's {@link ThrowableAssert} wrapper
     * @throws NullPointerException if input is null
     */
    public static <V> ThrowableAssertionJ assertThat(ThrowerWithResult<V> thrower) throws NullPointerException {
        Objects.requireNonNull(thrower, "Thrower should not be null!");
        return new ThrowableAssertionJ<>(thrower);
    }

    /**
     * An AssertJ style method to trigger a throwable assertion, require a non-null thrower.
     *
     * @param thrower non-null thrower with no returned value
     * @return an AssertJ's {@link ThrowableAssert} wrapper
     * @throws NullPointerException if input is null
     */
    public static ThrowableAssertionJ assertThat(Thrower thrower) throws AssertionError {
        Objects.requireNonNull(thrower, "Thrower should not be null!");
        return new ThrowableAssertionJ<Void>(() -> {
            thrower.throwThrowable();
            return null;
        });
    }

    /**
     * An AssertJ style method to trigger a throwable assertion, require a nullable throwable.
     *
     * @param throwable nullable throwable
     * @return an object of AssertJ's {@link ThrowableAssert}
     */
    public static ThrowableAssert assertThat(Throwable throwable) {
        return new ThrowableAssert(throwable);
    }

    /**
     * Check if the callable passed in does throw anything.
     *
     * @return an object of AssertJ's {@link ThrowableAssert} with caught throwable inside
     * @throws MissingExceptionAssertionError if no exception throws
     */
    public ThrowableAssert doThrow() throws MissingExceptionAssertionError {
        try {
            throwerWithResult.throwThrowable();
        } catch (Throwable caught) {
            return new ThrowableAssert(caught);
        }

        throw new MissingExceptionAssertionError();
    }

    /**
     * Check if the callable passed in does throw nothing.
     *
     * @return the returned value of the callable
     * @throws UnexpectedThrowableAssertionError if it exception
     */
    public R doNotThrow() throws UnexpectedThrowableAssertionError {
        try {
            return throwerWithResult.throwThrowable();
        } catch (Throwable caught) {
            throw new UnexpectedThrowableAssertionError(caught);
        }
    }

}
