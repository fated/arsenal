package com.brucechou.arsenal.test.assertion;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher that applies a delegate matcher to the message of the current Throwable,
 * returning the result of that match.
 *
 * @param <T> the type of the throwable being matched
 */
public class ThrowableMessageMatcher<T extends Throwable> extends TypeSafeMatcher<T> {

    private final Matcher<String> matcher;

    public ThrowableMessageMatcher(Matcher<String> matcher) {
        this.matcher = matcher;
    }

    public void describeTo(Description description) {
        description.appendText("exception with message ");
        description.appendDescriptionOf(matcher);
    }

    @Override
    protected boolean matchesSafely(T item) {
        return matcher.matches(item.getMessage());
    }

    @Override
    protected void describeMismatchSafely(T item, Description description) {
        description.appendText("message ");
        matcher.describeMismatch(item.getMessage(), description);
    }

    /**
     * Returns a matcher that verifies that the exception has a message for which the supplied matcher
     * evaluates to true.
     *
     * @param matcher to apply to the message of the exception
     * @param <T> type of the exception
     * @return a hasMessage matcher
     */
    @Factory
    public static <T extends Throwable> Matcher<T> hasMessage(final Matcher<String> matcher) {
        return new ThrowableMessageMatcher<>(matcher);
    }

}
