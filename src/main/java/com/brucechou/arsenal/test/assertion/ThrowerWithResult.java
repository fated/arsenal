package com.brucechou.arsenal.test.assertion;

/**
 * A functional interface with return value that will probably throw a throwable.
 *
 * @param <R> type of return value
 */
@FunctionalInterface
public interface ThrowerWithResult<R> {

    R throwThrowable() throws Throwable;

}
