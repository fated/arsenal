# Conditional - A value-based class to replace if-else statement and ternary operator

`Conditional` is a value-based class that holds a value and its predicate, if the predicate on the value is true, the
passed-in thenConsumer will be executed on the value, otherwise, the elseConsumer will be executed. You could use this
to replace almost all your if-else statement and `?-:` ternary operator to make your code more fluent and clear.

## Usage

### Create an instance of `Conditional`

Since `Conditional` is a value-based class, it does not have public access to its constructors and setters, please use
the following two static methods to create a `Conditional` object:

#### `of(T value)`

This could create an object of `Conditional`. Please notice that `value` does not need to be non-null, which is different
from `Optional`, since null value is also a valid input here.

#### `ofNullable(T value)`

This could also create an object of `Conditional`. Same as above, the `value` is also nullable. Furthermore, it will also
set the predicate to null check, which means you cannot set the predicate to others later. It's the same as below:
```java
    Conditonal.of(value)
              .onNullCheck()
```

### Set a predicate on the value

After create an object of some value, you then need to set a predicate on the value to assert.

#### `on(Predicate<T> predicate)`

By calling `on(...)` you could pass in a predicate to perform on the value, since the API accepts a `FunctionalInterface`
you could pass in a lambda statement, a method reference or anything it fits.

```java
    Conditonal.of(value)
              .on(v -> CONST.equals(v))
```

#### `onNullCheck()`

This is a shortcut to set the predicate to a default null check.

### Set `then` behavior

If the predicate is true on the value, the `then` behavior will be executed, there are several pre-defined behaviors as
below:

#### `then(Consumer<T> consumer)`

This is the most common behavior, only if the predicate passed, the consumer will be executed, and you don't need to
set `else` behavior if there is no else behavior, see example:

```java
    Conditonal.of(value)
              .onNullCheck()
              .then(System.out::println);
```

#### `get()`

This behavior simply return the assertion result of the predicate on the value.

```java
    boolean result = Conditonal.of(value)
                               .onNullCheck()
                               .get();

```

#### `thenReturn(Function<T, R> converter)`

This will return a result that applies the converter on the value if the predicate is true. For this behavior, an `else`
behavior must be provide, see example:

```java
    String result = Conditional.of(Arrays.asList("abc", "def", "xyz"))
                               .on(list -> !list.isEmpty())
                               .thenReturn(list -> String.join(", ", list))
                               .orElse("");
```

The above example checks a string list is empty or not, if not empty, it returns the joined string on every elements in
 the list, otherwise it returns a default empty string. More `else` behavior please check below.

### Set `else` behavior and execute the conditional check

Usually `else` behavior is the last step you'll need, it will only be triggered when the predicate is false, there are
several pre-defined behaviors as below:

#### `orElse(Consumer<T> elseConsumer)`

This is the most common behavior, only if the predicate is not passed, the elseConsumer will be executed, and it is the
last termination step of `Conditional`, see example:

```java
    Conditonal.of(value)
              .onNullCheck()
              .then(System.out::println)
              .orElse(System.err::println)
```

#### `orElseThrow(Supplier<? extends X> exceptionSupplier)`

This behavior will throw an exception provided by the exceptionSupplier if the predicate is false, see example:

```java
    Conditonal.of(value)
              .onNullCheck()
              .then(System.out::println)
              .orElseThrow(() -> new RuntimeException());
```

#### `orElseThrow(Function<T, ? extends X> exceptionBuilder)`

This behavior will also throw an exception if the predicate is false, the difference is this behavior will let you build
some exception based on the value, so you can add them in the error message to make it more meaningful, see example:

```java
    Conditonal.of(value)
              .onNullCheck()
              .then(System.out::println)
              .orElseThrow((v) -> new RuntimeException(v + " is invalid"));
```

#### `orElse(R elseValue)`

If your previous then behavior is `thenReturn`, then your have two more choices besides these two exception throwers.
One of them is `orElse(R elseValue)`, it provides a default value if the predicate is false, see example:

```java
    String result = Conditional.of(Arrays.asList("abc", "def", "xyz"))
                               .on(list -> !list.isEmpty())
                               .thenReturn(list -> String.join(", ", list))
                               .orElse("");
```

#### `orElseReturn(Function<T, ? extends R> elseConverter)`

The other behavior is this one, it let you pass in a converter which will be applied to the value if the predicate is
false, and the converted value will be return, see example:

```java
    String result = Conditional.of(Arrays.asList("abc", "def", "xyz"))
                               .on(list -> list.size() > 5)
                               .thenReturn(list -> String.join(", ", list))
                               .orElseReturn(list -> String.join("; ", list));
```

#### `orElseIf(Predicate<T> predicate)`

When using `if-else` statement, you could use `else if` to start a new if statement in else branch, you can also do that
in `Conditional`. Use `orElseIf(...)` as the else behavior and pass in a new predicate, it will return a new
`Conditional` object with original value and the new predicate, the previous `Conditional` will be stored as new one's
parent, since we still need them when we run the whole check. You can build several `orElseIf` together to a chained
`Conditional` object as below:

```java
    Conditional.of(value)
               .on(v -> v > 10)
               .then(v -> System.out.println(v + " > 10"))
               .orElseIf(v -> v < 1)
               .then(v -> System.out.println(v + " < 1"))
               .orElse(v -> System.out.println(v + " <= 10 && " + v + " >= 1"));
```

Currently, this behavior can only be used after `then()`, not possible to be used with `thenReturn`.

## Examples

The followings are some useful examples that leverage `Conditional`.

### 1. Replace simple if-else statement

Write the following:
```java
    Conditional.of(someString)
               .on(s -> s.length() > 5)
               .then(s -> System.out.println(s + " is longer than 5 chars!"))
               .orElse(s -> System.out.println(s + " is shorter than or equal to 5 chars!"));
```

To replace:
```java
    if (someString.length() > 5) {
        System.out.println(s + " is longer than 5 chars!");
    } else {
        System.out.println(s + " is shorter than or equal to 5 chars!");
    }
```

### 2. Replace chained `if-else-if` statement

Write the following:
```java
    Conditional.of(value)
               .on(v -> v > 10)
               .then(v -> System.out.println(v + " > 10"))
               .orElseIf(v -> v < 1)
               .then(v -> System.out.println(v + " < 1"))
               .orElse(v -> System.out.println(v + " <= 10 && " + v + " >= 1"));
```

To replace
```java
    if (value > 10) {
        System.out.println(value + " > 10")
    } else if (value < 1) {
        System.out.println(value + " < 1")
    } else {
        System.out.println(value + " <= 10 && " + value + " >= 1")
    }
```

### 3. Replace statement using ternary operation

Write the following
```java
    String result = Conditional.of(value)
                               .on(v -> isValid(v))
                               .thenReturn(v -> v.toUpperCase())
                               .orElse("");
```

To replace
```java
    String result = isValid(value) ? value.toUpperCase() : "";
```
