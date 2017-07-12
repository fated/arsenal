package com.brucechou.arsenal.conditional;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConditionalReturnTest extends Assert {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    private final String trueValue = "Test";
    private final String falseValue = "Other Test";
    private final String nullValue = null;

    private final Set<String> resultCollector = new HashSet<>();

    private final Predicate<String> predicate = (v) -> v.length() == 4;
    private final Consumer<String> resultConsumer = resultCollector::add;
    private final Function<String, String> resultConverter = String::toUpperCase;

    private final Supplier<RuntimeException> exceptionSupplier = RuntimeException::new;
    private final Function<String, RuntimeException> exceptionBuilder = RuntimeException::new;

    @After
    public void tearDown() throws Exception {
        resultCollector.clear();
    }

    @Test
    public void testNoPredicate() throws Exception {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Predicate is not given!"));

        Conditional.of(trueValue)
                   .then(resultConsumer)
                   .orElse(resultConsumer);
    }

    @Test
    public void testOfNullableWithTrueValue() throws Exception {
        Conditional.ofNullable(trueValue)
                   .then(resultConsumer)
                   .orElse(resultConsumer);

        assertTrue(resultCollector.contains(trueValue));
    }

    @Test
    public void testOfNullableWithNullValue() throws Exception {
        Conditional.ofNullable(nullValue)
                   .then(resultConsumer)
                   .orElse(resultConsumer);

        assertTrue(resultCollector.contains(nullValue));
    }

    @Test
    public void testOnNullCheckWithTrueValue() throws Exception {
        Conditional.of(trueValue)
                   .onNullCheck()
                   .then(resultConsumer)
                   .orElse(resultConsumer);

        assertTrue(resultCollector.contains(trueValue));
    }

    @Test
    public void testOnNullCheckWithNullValue() throws Exception {
        Conditional.of(nullValue)
                   .onNullCheck()
                   .then(resultConsumer)
                   .orElse(resultConsumer);

        assertTrue(resultCollector.contains(nullValue));
    }

    @Test
    public void testOnNullCheckTwoTimes() throws Exception {
        expected.expect(IllegalStateException.class);
        expected.expectMessage(Matchers.is("Predicate is already set!"));

        Conditional.of(nullValue)
                   .onNullCheck()
                   .onNullCheck()
                   .then(resultConsumer)
                   .orElse(resultConsumer);
    }

    @Test
    public void testOnNullPredicate() throws Exception {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Predicate cannot be null!"));

        Conditional.of(nullValue)
                   .on(null)
                   .then(resultConsumer)
                   .orElse(resultConsumer);
    }

    @Test
    public void testOnPredicateTwoTimes() throws Exception {
        expected.expect(IllegalStateException.class);
        expected.expectMessage(Matchers.is("Predicate is already set!"));

        Conditional.of(nullValue)
                   .on(predicate)
                   .on(predicate)
                   .then(resultConsumer)
                   .orElse(resultConsumer);
    }

    @Test
    public void testGetWithTrueValue() throws Exception {
        boolean result = Conditional.of(trueValue)
                                    .on(predicate)
                                    .get();

        assertTrue(result);
    }

    @Test
    public void testGetWithFalseValue() throws Exception {
        boolean result = Conditional.of(falseValue)
                                    .on(predicate)
                                    .get();

        assertFalse(result);
    }

    @Test
    public void testGetWithNullPredicate() throws Exception {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Predicate is not given!"));

        Conditional.of(nullValue).get();
    }

    @Test
    public void testOrElseWithTrueValue() throws Exception {
        Conditional.of(trueValue)
                   .on(predicate)
                   .then(resultConsumer)
                   .orElse(resultConsumer);

        assertTrue(resultCollector.contains(trueValue));
    }

    @Test
    public void testThenConsumerTwoTimes() throws Exception {
        expected.expect(IllegalStateException.class);
        expected.expectMessage(Matchers.is("Then consumer is already set!"));

        Conditional.of(trueValue)
                   .on(predicate)
                   .then(resultConsumer)
                   .then(resultConsumer)
                   .orElse(resultConsumer);
    }

    @Test
    public void testOrElseWithTrueValueAndNullConsumer() throws Exception {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Then consumer cannot be null!"));

        Conditional.of(trueValue)
                   .on(predicate)
                   .then(null)
                   .orElse(resultConsumer);
    }

    @Test
    public void testOrElseWithFalseValue() throws Exception {
        Conditional.of(falseValue)
                   .on(predicate)
                   .then(resultConsumer)
                   .orElse(resultConsumer);

        assertTrue(resultCollector.contains(falseValue));
    }

    @Test
    public void testOrElseWithNullThenConsumer() throws Exception {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Then consumer is not given!"));

        Conditional.of(trueValue)
                   .on(predicate)
                   .orElse(resultConsumer);
    }

    @Test
    public void testOrElseWithNullElseConsumer() throws Exception {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Else consumer cannot be null!"));

        Conditional.of(falseValue)
                   .on(predicate)
                   .then(resultConsumer)
                   .orElse(null);
    }

    @Test
    public void testOrElseDoNothingWithTrueValue() throws Exception {
        Conditional.of(trueValue)
                   .on(predicate)
                   .then(resultConsumer);

        assertTrue(resultCollector.contains(trueValue));
    }

    @Test
    public void testOrElseDoNothingWithFalseValue() throws Exception {
        Conditional.of(falseValue)
                   .on(predicate)
                   .then(resultConsumer);

        assertFalse(resultCollector.contains(falseValue));
        assertTrue(resultCollector.isEmpty());
    }

    @Test
    public void testOrElseThrowSupplierWithTrueValue() throws Exception {
        Conditional.of(trueValue)
                   .on(predicate)
                   .then(resultConsumer)
                   .orElseThrow(exceptionSupplier);

        assertTrue(resultCollector.contains(trueValue));
    }

    @Test
    public void testOrElseThrowSupplierWithFalseValue() throws Exception {
        expected.expect(RuntimeException.class);
        expected.expectMessage(Matchers.isEmptyOrNullString());

        Conditional.of(falseValue)
                   .on(predicate)
                   .then(resultConsumer)
                   .orElseThrow(exceptionSupplier);
    }

    @Test
    public void testOrElseThrowSupplierWithNullSupplier() throws Throwable {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Exception supplier cannot be null!"));

        Conditional.of(falseValue)
                   .on(predicate)
                   .then(resultConsumer)
                   .orElseThrow((Supplier<Throwable>) null);
    }

    @Test
    public void testOrElseThrowFunctionWithTrueValue() throws Exception {
        Conditional.of(trueValue)
                   .on(predicate)
                   .then(resultConsumer)
                   .orElseThrow(exceptionBuilder);

        assertTrue(resultCollector.contains(trueValue));
    }

    @Test
    public void testOrElseThrowFunctionWithFalseValue() throws Exception {
        expected.expect(RuntimeException.class);
        expected.expectMessage(Matchers.is(falseValue));

        Conditional.of(falseValue)
                   .on(predicate)
                   .then(resultConsumer)
                   .orElseThrow(exceptionBuilder);
    }

    @Test
    public void testOrElseThrowFunctionWithNullFunction() throws Throwable {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Exception builder cannot be null!"));

        Conditional.of(falseValue)
                   .on(predicate)
                   .then(resultConsumer)
                   .orElseThrow((Function<String, Throwable>) null);
    }

    @Test
    public void testOrElseIfConsumeAtLastElseStatement() throws Exception {
        Conditional.of(5)
                   .on(v -> v > 10)
                   .then(v -> resultCollector.add(v + " > 10"))
                   .orElseIf(v -> v < 1)
                   .then(v -> resultCollector.add(v + " < 1"))
                   .orElse(v -> resultCollector.add(v + " < 10 && " + v + " > 1"));

        assertEquals(1, resultCollector.size());
        assertTrue(resultCollector.contains("5 < 10 && 5 > 1"));
    }

    @Test
    public void testOrElseIfConsumeAtMiddleElseStatement() throws Exception {
        Conditional.of(0)
                   .on(v -> v > 10)
                   .then(v -> resultCollector.add(v + " > 10"))
                   .orElseIf(v -> v < 1)
                   .then(v -> resultCollector.add(v + " < 1"))
                   .orElse(v -> resultCollector.add(v + " < 10 && " + v + " > 1"));

        assertEquals(1, resultCollector.size());
        assertTrue(resultCollector.contains("0 < 1"));
    }

    @Test
    public void testOrElseIfConsumeAtFirstElseStatement() throws Exception {
        Conditional.of(11)
                   .on(v -> v > 10)
                   .then(v -> resultCollector.add(v + " > 10"))
                   .orElseIf(v -> v < 1)
                   .then(v -> resultCollector.add(v + " < 1"))
                   .orElse(v -> resultCollector.add(v + " < 10 && " + v + " > 1"));

        assertEquals(1, resultCollector.size());
        assertTrue(resultCollector.contains("11 > 10"));
    }

    @Test
    public void testOrElseIfNoLastElseStatement() throws Exception {
        Conditional.of(5)
                   .on(v -> v > 10)
                   .then(v -> resultCollector.add(v + " > 10"))
                   .orElseIf(v -> v < 1)
                   .then(v -> resultCollector.add(v + " < 1"));

        assertEquals(0, resultCollector.size());
    }

    @Test
    public void testOrElseIfLastElseStatementThrows() throws Exception {
        expected.expect(RuntimeException.class);
        expected.expectMessage(Matchers.isEmptyOrNullString());

        Conditional.of(5)
                   .on(v -> v > 10)
                   .then(v -> resultCollector.add(v + " > 10"))
                   .orElseIf(v -> v < 1)
                   .then(v -> resultCollector.add(v + " < 1"))
                   .orElseThrow(() -> new RuntimeException());
    }

    @Test
    public void testOrElseIfMiddleElseStatementSupplier() throws Exception {
        Conditional.of(0)
                   .on(v -> v > 10)
                   .then(v -> resultCollector.add(v + " > 10"))
                   .orElseIf(v -> v < 5)
                   .then(v -> resultCollector.add(v + " < 5"))
                   .orElseIf(v -> v < 1)
                   .then(v -> resultCollector.add(v + " < 1"))
                   .orElseThrow(() -> new RuntimeException());

        assertEquals(1, resultCollector.size());
        assertTrue(resultCollector.contains("0 < 5"));
    }

    @Test
    public void testOrElseIfFirstElseStatementSupplier() throws Exception {
        Conditional.of(11)
                   .on(v -> v > 10)
                   .then(v -> resultCollector.add(v + " > 10"))
                   .orElseIf(v -> v < 1)
                   .then(v -> resultCollector.add(v + " < 1"))
                   .orElseThrow((v) -> new RuntimeException(v + " < 10 && " + v + " > 1"));

        assertEquals(1, resultCollector.size());
        assertTrue(resultCollector.contains("11 > 10"));
    }

    @Test
    public void testOrElseIfChainedElseStatementFunction() throws Exception {
        Conditional.of(11)
                   .on(v -> v > 10)
                   .then(v -> resultCollector.add(v + " > 10"))
                   .orElseIf(v -> v > 9)
                   .then(v -> resultCollector.add(v + " < 9"))
                   .orElseIf(v -> v < 8)
                   .then(v -> resultCollector.add(v + " < 8"))
                   .orElseIf(v -> v < 7)
                   .then(v -> resultCollector.add(v + " < 7"))
                   .orElseThrow((v) -> new RuntimeException(v + " < 10 && " + v + " > 1"));

        assertEquals(1, resultCollector.size());
        assertTrue(resultCollector.contains("11 > 10"));
    }

    @Test
    public void testOrElseIfLastElseStatementGet() throws Exception {
        boolean result = Conditional.of(5)
                                    .on(v -> v > 10)
                                    .then(v -> resultCollector.add(v + " > 10"))
                                    .orElseIf(v -> v < 1)
                                    .then(v -> resultCollector.add(v + " < 1"))
                                    .get();

        assertFalse(result);
    }

    @Test
    public void testOrElseIfMiddleElseStatementGet() throws Exception {
        boolean result = Conditional.of(0)
                                    .on(v -> v > 10)
                                    .then(v -> resultCollector.add(v + " > 10"))
                                    .orElseIf(v -> v < 1)
                                    .then(v -> resultCollector.add(v + " < 1"))
                                    .get();

        assertTrue(result);
        assertEquals(1, resultCollector.size());
        assertTrue(resultCollector.contains("0 < 1"));
    }

    @Test
    public void testOrElseIfFirstElseStatementGet() throws Exception {
        boolean result = Conditional.of(11)
                                    .on(v -> v > 10)
                                    .then(v -> resultCollector.add(v + " > 10"))
                                    .orElseIf(v -> v < 1)
                                    .then(v -> resultCollector.add(v + " < 1"))
                                    .get();

        assertFalse(result);
        assertEquals(1, resultCollector.size());
        assertTrue(resultCollector.contains("11 > 10"));
    }

    @Test
    public void testOrElseReturnValueWithTrueValue() throws Exception {
        String result = Conditional.of(trueValue)
                                   .on(predicate)
                                   .thenReturn(Function.identity())
                                   .orElse(falseValue);

        assertEquals(trueValue, result);
    }

    @Test
    public void testOrElseReturnValueWithFalseValue() throws Exception {
        String result = Conditional.of(falseValue)
                                   .on(predicate)
                                   .thenReturn(Function.identity())
                                   .orElse(trueValue);

        assertEquals(trueValue, result);
    }

    @Test
    public void testThenReturnValueWithNullConverter() throws Exception {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Then converter cannot be null!"));

        Conditional.of(nullValue)
                   .on(predicate)
                   .thenReturn(null)
                   .orElse(trueValue);
    }

    @Test
    public void testOrElseReturnWithTrueValue() throws Exception {
        String result = Conditional.of(trueValue)
                                   .on(predicate)
                                   .thenReturn(Function.identity())
                                   .orElseReturn(resultConverter);

        assertEquals(trueValue, result);
    }

    @Test
    public void testOrElseReturnWithFalseValue() throws Exception {
        String result = Conditional.of(falseValue)
                                   .on(predicate)
                                   .thenReturn(Function.identity())
                                   .orElseReturn(resultConverter);

        assertEquals(falseValue.toUpperCase(), result);
    }

    @Test
    public void testOrElseReturnWithNullConverter() throws Throwable {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Else converter cannot be null!"));

        Conditional.of(falseValue)
                   .on(predicate)
                   .thenReturn(Function.identity())
                   .orElseReturn(null);
    }

    @Test
    public void testOrElseReturnThrowSupplierWithTrueValue() throws Exception {
        String result = Conditional.of(trueValue)
                                   .on(predicate)
                                   .thenReturn(Function.identity())
                                   .orElseThrow(exceptionSupplier);

        assertEquals(trueValue, result);
    }

    @Test
    public void testOrElseReturnThrowSupplierWithFalseValue() throws Exception {
        expected.expect(RuntimeException.class);
        expected.expectMessage(Matchers.isEmptyOrNullString());

        Conditional.of(falseValue)
                   .on(predicate)
                   .thenReturn(Function.identity())
                   .orElseThrow(exceptionSupplier);
    }

    @Test
    public void testOrElseReturnThrowSupplierWithNullFunction() throws Throwable {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Exception supplier cannot be null!"));

        Conditional.of(falseValue)
                   .on(predicate)
                   .thenReturn(Function.identity())
                   .orElseThrow((Supplier<Throwable>) null);
    }

    @Test
    public void testOrElseReturnThrowFunctionWithTrueValue() throws Exception {
        String result = Conditional.of(trueValue)
                                   .on(predicate)
                                   .thenReturn(Function.identity())
                                   .orElseThrow(exceptionBuilder);

        assertEquals(trueValue, result);
    }

    @Test
    public void testOrElseReturnThrowFunctionWithFalseValue() throws Exception {
        expected.expect(RuntimeException.class);
        expected.expectMessage(Matchers.is(falseValue));

        Conditional.of(falseValue)
                   .on(predicate)
                   .thenReturn(Function.identity())
                   .orElseThrow(exceptionBuilder);
    }

    @Test
    public void testOrElseReturnThrowFunctionWithNullFunction() throws Throwable {
        expected.expect(NullPointerException.class);
        expected.expectMessage(Matchers.is("Exception builder cannot be null!"));

        Conditional.of(falseValue)
                   .on(predicate)
                   .thenReturn(Function.identity())
                   .orElseThrow((Function<String, Throwable>) null);
    }

    @Test
    public void testEquals() throws Exception {
        Conditional<String> conditional1 = Conditional.of(trueValue).on(predicate).then(resultConsumer);

        assertTrue(conditional1.equals(conditional1));

        assertFalse(conditional1.equals(trueValue));

        assertFalse(conditional1.equals(Conditional.of(falseValue)));

        assertFalse(conditional1.equals(Conditional.of(trueValue).onNullCheck()));

        assertFalse(conditional1.equals(Conditional.of(trueValue).on((v) -> v.length() == 4)));

        // Currently function objects are only equals when they are the same reference, so it's false here
        assertFalse(conditional1.equals(Conditional.of(trueValue).on((v) -> v.length() == 4).then(resultConsumer)));

        assertFalse(conditional1.equals(Conditional.of(trueValue).on(predicate).then(v -> {})));

        Conditional<String> conditional2 = Conditional.of(trueValue).on(predicate).then(resultConsumer);

        assertTrue(conditional1.equals(conditional2));
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals(Arrays.hashCode(new Object[] {trueValue, null, null}), Conditional.of(trueValue)
                                                                                       .hashCode());

        assertEquals(Arrays.hashCode(new Object[] {trueValue, predicate, null}), Conditional.of(trueValue)
                                                                                            .on(predicate)
                                                                                            .hashCode());

        assertEquals(Arrays.hashCode(new Object[] {trueValue, predicate, resultConsumer}),
                     Conditional.of(trueValue).on(predicate).then(resultConsumer).hashCode());
    }

    @Test
    public void testToString() throws Exception {
        Conditional<String> conditional1 = Conditional.of(trueValue).on(predicate);
        assertNotNull(conditional1.toString());
        assertTrue(conditional1.toString().contains(trueValue));

        Conditional<String> conditional2 = Conditional.of(trueValue).on(predicate);
        assertNotNull(conditional2.toString());
        assertTrue(conditional2.toString().contains(trueValue));
        assertTrue(conditional2.toString().contains(predicate.toString()));

        Conditional<String> conditional3 = Conditional.of(trueValue).on(predicate).then(resultConsumer);
        assertNotNull(conditional3.toString());
        assertTrue(conditional3.toString().contains(trueValue));
        assertTrue(conditional3.toString().contains(predicate.toString()));
        assertTrue(conditional3.toString().contains(resultConsumer.toString()));
    }

}
