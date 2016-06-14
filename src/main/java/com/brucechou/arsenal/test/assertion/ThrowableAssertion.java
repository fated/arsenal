package com.brucechou.arsenal.test.assertion;

import static com.brucechou.arsenal.test.assertion.ThrowableCauseMatcher.hasCause;
import static com.brucechou.arsenal.test.assertion.ThrowableCauseTypeMatcher.isClass;
import static com.brucechou.arsenal.test.assertion.ThrowableMessageMatcher.hasMessage;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import java.util.Objects;

/**
 * The {@code ThrowableAssertion} allows you to verify that your code throws a specific exception.
 *
 * <h3>Usage</h3>
 *
 * <pre>
 * public class SimpleThrowableAssertionTest {
 *
 *     &#064;Test (expected = MissingExceptionAssertionError.class) // making test pass
 *     public void throwsNothing() {
 *         // no exception expected, will throw MissingExceptionAssertionError.
 *         assertThrown(() -&gt; System.out.println());
 *     }
 *
 *     &#064;Test
 *     public void throwsExceptionWithSpecificType() {
 *         assertThrown(() -&gt; {
 *             throw new NullPointerException();
 *         }).expect(NullPointerException.class);
 *     }
 *
 *     &#064;Test
 *     public void throwsExceptionWithMethodReference() {
 *         assertThrown(new DummyService()::someMethod).expect(NullPointerException.class);
 *     }
 *
 *     &#064;Test
 *     public void throwsExceptionWithLambdaExpression() {
 *         assertThrown(() -&gt; new DummyService().someOtherMethod()).expect(NullPointerException.class);
 *     }
 *
 *     &#064;Test
 *     public void throwsExceptionWithConstructorReference() {
 *         assertThrown(SomeOtherDummyService::new).expect(NullPointerException.class);
 *     }
 *
 *     &#064;Test
 *     public void throwsExceptionWithLambdaConstructor() {
 *         assertThrown(() -&gt; new SomeOtherDummyService()).expect(NullPointerException.class);
 *     }
 *
 * }
 * </pre>
 *
 * <p>You can use {@code assertThrown} in your test just like other assertions in JUnit. You can add your testing
 * code as parameter. {@code ExceptionThrower} is a {@code @FunctionalInterface} which instances can be created with
 * lambda expressions, method references, or constructor references. {@code assertThrown} accepting
 * {@code ExceptionThrower} will expect and be ready to handle an exception.
 *
 * <p>After specifying the type of the expected exception your test is successful when such an exception
 * is thrown and it fails if a different or no exception is thrown.
 *
 * <p>Instead of specifying the exception's type you can characterize the
 * expected exception based on other criteria, too:
 *
 * <ul>
 *   <li>The exception's message contains a specific text: {@link #expectMessage(String)}</li>
 *   <li>The exception's message complies with a Hamcrest matcher: {@link #expectMessage(Matcher)}</li>
 *   <li>The exception's cause complies with a Hamcrest matcher: {@link #expectCause(Matcher)}</li>
 *   <li>The exception has no cause: {@link #expectNoCause()}</li>
 *   <li>The exception itself is an instance of specific type: {@link #expect(Class)}</li>
 *   <li>The exception itself complies with a Hamcrest matcher: {@link #expect(Matcher)}</li>
 *   <li>The exception itself is an instance of specific type: {@link #expect(Class)}</li>
 * </ul>
 *
 * <p>You can chain any of the presented expect-methods. The assertion is successful if all specifications are met.
 *
 * <pre>
 * &#064;Test
 * public void throwsException() {
 *     assertThrown(() -&gt; {
 *         throw new NullPointerException(&quot;What happened?&quot;);
 *     }).expect(NullPointerException.class).expectMessage(&quot;happened&quot;);
 * }
 * </pre>
 *
 * <h3>AssertionErrors</h3>
 *
 * <p>ThrowableAssertion uses {@link AssertionError}s for indicating that an assertion is failing.
 * E.g. the following test fails because of the {@code expect()} statement.
 *
 * <pre>
 * &#064;Test
 * public void throwsUnhandled() {
 *     assertThrown(() -&gt; {
 *         throw new IllegalArgumentException();
 *     }).expect(NullPointerException.class) // throws AssertionError
 * }
 * </pre>
 *
 * <h3>Missing Exceptions</h3>
 *
 * <p>By default missing exceptions are reported with an error message like "Expected exception was not thrown.".
 * You can configure a different message by means of {@link #assertThrown(String, ExceptionThrower)}.
 * (see {@code throwsNothing()}).
 */
public class ThrowableAssertion {

    private final Throwable caught;

    /**
     * Instantiate a throwable assertion object, require a non-null throwable.
     *
     * @param caught a throwable needs to be asserted
     * @throws NullPointerException if a null value is passed in
     */
    private ThrowableAssertion(Throwable caught) throws NullPointerException {
        this.caught = Objects.requireNonNull(caught, "Throwable caught should not be null!");
    }

    /**
     * Assert that an {@link ExceptionThrower} satisfies all following expectations. If it doesn't,
     * it throws an {@link MissingExceptionAssertionError} with message.
     *
     * @param message the identifying message for the {@link MissingExceptionAssertionError}
     *     (<code>null</code> okay)
     * @param exceptionThrower exceptionThrower to be checked
     * @return a throwable assertion object contains the caught throwable
     */
    public static ThrowableAssertion assertThrown(String message, ExceptionThrower exceptionThrower) {
        try {
            exceptionThrower.throwThrowable();
        } catch (Throwable caught) {
            return new ThrowableAssertion(caught);
        }

        if (message != null && !message.trim().isEmpty()) {
            throw new MissingExceptionAssertionError(message);
        } else {
            throw new MissingExceptionAssertionError();
        }
    }

    /**
     * Assert that an {@link ExceptionThrower} satisfies all following expectations. If it doesn't,
     * it throws an {@link MissingExceptionAssertionError} with default message.
     *
     * @param exceptionThrower exceptionThrower to be checked
     * @return a throwable assertion object contains the caught throwable
     */
    public static ThrowableAssertion assertThrown(ExceptionThrower exceptionThrower) {
        return assertThrown(null, exceptionThrower);
    }

    /**
     * Assert explicitly that an {@link ExceptionThrower} does not throw any throwable. If it does,
     * it throws an {@link UnexpectedThrowableAssertionError} with message.
     *
     * @param message the identifying message for the {@link UnexpectedThrowableAssertionError}
     *     (<code>null</code> okay)
     * @param exceptionThrower exceptionThrower to be checked
     */
    public static void assertNotThrow(String message, ExceptionThrower exceptionThrower) {
        try {
            exceptionThrower.throwThrowable();
        } catch (Throwable caught) {
            if (message != null && !message.trim().isEmpty()) {
                throw new UnexpectedThrowableAssertionError(message, caught);
            } else {
                throw new UnexpectedThrowableAssertionError(caught);
            }
        }
    }

    /**
     * Assert explicitly that an {@link ExceptionThrower} does not throw any throwable. If it does,
     * it throws an {@link UnexpectedThrowableAssertionError} with message.
     *
     * @param exceptionThrower exceptionThrower to be checked
     */
    public static void assertNotThrow(ExceptionThrower exceptionThrower) {
        assertNotThrow(null, exceptionThrower);
    }

    /**
     * Assert that an {@link Throwable} satisfies all following expectations.
     *
     * @param caught a throwable needs to be asserted
     * @return a throwable assertion object contains the caught throwable
     * @throws NullPointerException if caught is null
     */
    public static ThrowableAssertion assertThrowable(Throwable caught) throws NullPointerException {
        return new ThrowableAssertion(caught);
    }

    /**
     * Extract cause of current caught throwable as a new throwable assertion if the type equals to specific type.
     * This is useful when asserting reflection method invoking, since it is usually wrapped with exception.
     *
     * @param type the type of throwable that needs to be extracted
     * @return a new throwable assertion of the cause if the type is matched, otherwise return the original one
     * @throws NullPointerException if current caught throwable does not have cause
     */
    public ThrowableAssertion extractFrom(Class<? extends Throwable> type) throws NullPointerException {
        if (Objects.equals(caught.getClass(), type)) {
            return new ThrowableAssertion(caught.getCause());
        }
        return this;
    }

    /**
     * Verify that your code throws an exception that is exactly an instance of specific {@code type}.
     * <pre>
     * &#064;Test
     * public void throwsExceptionWithSpecificType() {
     *     assertThrown(() -&gt; {
     *         throw new NullPointerException();
     *     }).expect(NullPointerException.class);
     * }
     * </pre>
     *
     * @param type expected type of the exception
     * @return the assertion itself
     */
    public ThrowableAssertion expect(Class<? extends Throwable> type) {
        return expect(isClass(type));
    }

    /**
     * Verify that your code throws an exception that is matched by a Hamcrest matcher.
     * <pre>
     * &#064;Test
     * public void throwsExceptionThatCompliesWithMatcher() {
     *     assertThrown(() -&gt; {
     *         throw new NullPointerException();
     *     }).expect(isA(NullPointerException.class));
     * }
     * </pre>
     *
     * @param matcher a Hamcrest matcher expected to be matched with the exception
     * @return the assertion itself
     */
    @SuppressWarnings("unchecked")
    public ThrowableAssertion expect(Matcher<?> matcher) {
        MatcherAssert.assertThat(caught, (Matcher<Throwable>) matcher);
        return this;
    }

    /**
     * Verify that your code throws an exception whose message contains a specific text.
     * <pre>
     * &#064;Test
     * public void throwsExceptionWhoseMessageContainsSpecificText() {
     *     assertThrown(() -&gt; {
     *         throw new NullPointerException(&quot;What happened?&quot;);
     *     }).expectMessage(&quot;happened&quot;);
     * }
     * </pre>
     *
     * @param subString expected text contained in the message of the exception
     * @return the assertion itself
     */
    public ThrowableAssertion expectMessage(String subString) {
        return expectMessage(containsString(subString));
    }

    /**
     * Verify that your code throws an exception whose message is matched by a Hamcrest matcher.
     * <pre>
     * &#064;Test
     * public void throwsExceptionWhoseMessageCompliesWithMatcher() {
     *     assertThrown(() -&gt; {
     *         throw new NullPointerException(&quot;What happened?&quot;);
     *     }).expectMessage(startsWith(&quot;What&quot;));
     * }
     * </pre>
     *
     * @param matcher a Hamcrest matcher expected to be matched with the message of exception
     * @return the assertion itself
     */
    public ThrowableAssertion expectMessage(Matcher<String> matcher) {
        return expect(hasMessage(matcher));
    }

    /**
     * Verify that your code throws an exception with no cause.
     * <pre>
     * &#064;Test
     * public void throwsExceptionWithNoCause() {
     *     assertThrown(() -&gt; {
     *         throw new IllegalArgumentException(&quot;What happened?&quot;);
     *     }).expectNoCause();
     * }
     * </pre>
     *
     * @return the assertion itself
     */
    public ThrowableAssertion expectNoCause() {
        return expect(hasCause(nullValue(Throwable.class)));
    }

    /**
     * Verify that your code throws an exception whose cause is exactly an instance of specific {@code type}.
     * <pre>
     * &#064;Test
     * public void throwsExceptionWhoseCauseIsSpecificType() {
     *     NullPointerException cause = new NullPointerException();
     *     assertThrown(() -&gt; {
     *         throw new IllegalArgumentException(&quot;What happened?&quot;, cause);
     *     }).expectMessage(NullPointerException.class);
     * }
     * </pre>
     *
     * @param type expected type of the cause of the exception
     * @return the assertion itself
     */
    public ThrowableAssertion expectCause(Class<? extends Throwable> type) {
        return expectCause(isClass(type));
    }

    /**
     * Verify that your code throws an exception whose cause is matched by a Hamcrest matcher.
     * <pre>
     * &#064;Test
     * public void throwsExceptionWhoseCauseCompliesWithMatcher() {
     *     NullPointerException cause = new NullPointerException();
     *     assertThrown(() -&gt; {
     *         throw new IllegalArgumentException(&quot;What happened?&quot;, cause);
     *     }).expectMessage(is(cause));
     * }
     * </pre>
     *
     * @param matcher a Hamcrest matcher expected to be matched with the cause of exception
     * @return the assertion itself
     */
    public ThrowableAssertion expectCause(Matcher<? extends Throwable> matcher) {
        return expect(allOf(notNullValue(), hasCause(matcher)));
    }

}
