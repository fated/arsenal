package com.brucechou.arsenal.test.assertion;

/**
 * A functional interface that will probably throw a throwable.
 */
@FunctionalInterface
public interface Thrower {

    void throwThrowable() throws Throwable;

}
