package com.brucechou.arsenal.test.assertion;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher that applies a delegate matcher to a Throwable to compare the exact type of the throwable,
 * returning the result of that match.
 *
 * @param <T> the type of the throwable being matched
 */
public class ThrowableCauseTypeMatcher<T> extends TypeSafeMatcher<T> {

    private final Matcher<?> equalMatcher;

    public ThrowableCauseTypeMatcher(Class<?> type) {
        this.equalMatcher = Matchers.equalTo(type);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("of exact type ");
        description.appendDescriptionOf(equalMatcher);
    }

    @Override
    protected boolean matchesSafely(T item) {
        return equalMatcher.matches(item.getClass());
    }

    @Override
    protected void describeMismatchSafely(T item, Description description) {
        description.appendText("type ");
        equalMatcher.describeMismatch(item.getClass(), description);
    }

    /**
     * Returns a matcher that verifies that the exception for which the supplied matcher
     * evaluates to true.
     *
     * @param type of the exception
     * @param <T> type of the exception
     * @return a isClass matcher
     */
    @Factory
    @SuppressWarnings("unchecked")
    public static <T> Matcher<T> isClass(final Class<?> type) {
        return (Matcher<T>) new ThrowableCauseTypeMatcher<>(type);
    }

}
