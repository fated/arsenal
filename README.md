# A lightweight throwable assertion lib - ArsenalAssertion

[![Build Status](https://travis-ci.org/fated/arsenal.svg?branch=master)](https://travis-ci.org/fated/arsenal)
[![Coverage Status](https://coveralls.io/repos/github/fated/arsenal/badge.svg?branch=master)](https://coveralls.io/github/fated/arsenal?branch=master)

ArsenalAssertion allows you to explicitly verify that your code throws a specific exception or does
not throw any exception.

The allowed exception thrower is an interface type declaration which is intended to be a *functional
interface* as defined by the Java Language Specification. This means the instances of functional
interfaces can be created with lambda expressions, method references, or constructor references.

Expectations on the exception could be easily added via chained method, which provides more flexibility
and more readability. Currently, it is capable to assert the exception itself, the message of the
exception and the cause of the exception. The expectations are using Hamcrest matchers, which provide
extendability on building complex matchers.

### Usage

The following is a simple example that uses this package to assert throwable.

```java
public class SimpleThrowableAssertionTest {

    @Test (expected = MissingExceptionAssertionError.class) // making test pass
    public void throwsNothing() {
        // no exception throws, will throw MissingExceptionAssertionError.
        assertThrown("Error detected", () -> System.out.println());
    }

    @Test
    public void throwsExceptionWithSpecificType() {
        assertThrown(() -> {
            throw new NullPointerException();
        }).expect(NullPointerException.class);
    }

    @Test
    public void throwsExceptionWithMethodReference() {
        assertThrown(new DummyService()::someMethod).expect(NullPointerException.class);
    }

    @Test
    public void throwsExceptionWithLambdaExpression() {
        assertThrown(() -> new DummyService().someOtherMethod()).expect(NullPointerException.class);
    }

    @Test
    public void throwsExceptionWithConstructorReference() {
        assertThrown(SomeOtherDummyService::new).expect(NullPointerException.class);
    }

    @Test
    public void throwsExceptionWithLambdaConstructor() {
        assertThrown(() -> new SomeOtherDummyService()).expect(NullPointerException.class);
    }

    @Test
    public void throwsNothing() {
        assertNotThrow(() -> System.out.println());
    }

    @Test (expected = UnexpectedThrowableAssertionError.class) // making test pass
    public void throwsUnexpected() {
        // unexpected exception throws, will throw UnexpectedThrowableAssertionError.
        assertNotThrow("Error detected", () -> {
            throw new NullPointerException();
        });
    }

}
```

#### Use `assertThrown`

You can use `assertThrown` in your test just like other assertions in JUnit. You can add your testing
code as parameter. `ExceptionThrower` is a `@FunctionalInterface` which instances can be created with
lambda expressions, method references, or constructor references. `assertThrown` accepting
`ExceptionThrower` will expect and be ready to handle an exception.

After specifying the type of the expected exception your test is successful when such an exception
is thrown and it fails if a different or no exception is thrown.

Instead of specifying the exception's type you can characterize the
expected exception based on other criteria, too:

* The exception's message contains a specific text: `expectMessage(String)`
* The exception's message complies with a Hamcrest matcher: `expectMessage(Matcher)`
* The exception's cause complies with a Hamcrest matcher: `expectCause(Matcher)`
* The exception has no cause: `expectNoCause()`
* The exception itself is an instance of specific type: `expect(Class)`
* The exception itself complies with a Hamcrest matcher: `expect(Matcher)`
* The exception itself is an instance of specific type: `expect(Class)`

You can chain any of the presented expect-methods. The assertion is successful if all specifications are met.

```java
@Test
public void throwsException() {
    assertThrown(() -> {
        throw new NullPointerException("What happened?");
    }).expect(NullPointerException.class).expectMessage("happened");
}
```

#### Use `assertNotThrow`

You can also use `assertNotThrow` in your test to explicitly assert that some code snippets do not throw
any exception. It's just like `assertThrown` but cannot be used together with the above expectation methods.

### Assertion Errors

ThrowableAssertion uses `AssertionError` for indicating that an assertion is failing.
E.g. the following test fails because of the `expect()` statement.

```java
@Test
public void throwsUnhandled() {
    assertThrown(() -> {
        throw new IllegalArgumentException();
    }).expect(NullPointerException.class) // throws AssertionError
}
```

### Missing Exceptions

By default missing exceptions are reported with an error message like "Expected exception was not thrown.".
You can configure a different message by means of `assertThrown(String, ExceptionThrower)`.
(see `throwsNothing()`).

### Unexpected Exceptions

By default unexpected exceptions are reported with an error message like "Unexpected exception was thrown:".
You can configure a different message by means of `assertNotThrow(String, ExceptionThrower)`.
(see `throwsUnexpected()`).
