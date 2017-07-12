package com.brucechou.arsenal.conditional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A container object contains a nullable value which may pass or may not pass a non-null predicate.
 * Use {@link #get()} to get the predicate result on the value.
 *
 * <p>Additional methods that depend on the predicate result of the contained value are provided,
 * such as {@link #then(Consumer)} (execute a block of code if value passes the predicate) and
 * {@link #orElse(Consumer)} (execute a block of code if the value is present).
 *
 * @since 1.0
 */
public final class Conditional<T> {

    /**
     * Perform a null check of the value, default predicate.
     */
    private static final Predicate<?> NULL_CHECK = Objects::nonNull;

    /**
     * A predicate that always return false, used as initial value for parent Conditional object.
     */
    private static final Predicate<?> ALWAYS_FALSE = v -> false;

    /**
     * A Conditional object that with null value and a always false predicate
     */
    private static final Conditional<?> ALWAYS_FALSE_CONDITIONAL = new Conditional<>(null, ALWAYS_FALSE);

    /**
     * The value to be tested, if null, means that a null value is to be tested.
     */
    private final T value;

    /**
     * Predicate to be performed.
     */
    private Predicate<T> predicate = null;

    /**
     * If predicate tests the value to be true, this consumer will be performed.
     */
    private Consumer<T> thenConsumer = null;

    /**
     * Save the test result to prevent perform twice on the same value and predicate.
     */
    private Boolean result = null;

    /**
     * Save the parent Conditional, used in chained Conditional object, initialled to always false conditional.
     */
    @SuppressWarnings("unchecked")
    private Conditional<T> parent = (Conditional<T>) ALWAYS_FALSE_CONDITIONAL;

    /**
     * Constructs an instance of the value.
     *
     * @param value the nullable value to be predicated
     */
    private Conditional(T value) {
        this.value = value;
    }

    /**
     * Constructs an instance of the value and on the predicate.
     *
     * @param value the nullable value to be predicated
     * @param predicate the non-null predicate to perform
     * @throws NullPointerException if predicate is null
     */
    private Conditional(T value, Predicate<T> predicate) {
        requireNonNull(predicate, "Predicate cannot be null!");
        this.value = value;
        this.predicate = predicate;
    }

    /**
     * Constructs an instance of the value and on the non-null predicate with a non-null parent.
     *
     * @param value the nullable value to be predicated
     * @param predicate the non-null predicate to perform
     * @param parent the parent Conditional objects which is used in {@code orElseIf()}
     * @throws NullPointerException if predicate is null or parent is null
     */
    private Conditional(T value, Predicate<T> predicate, Conditional<T> parent) {
        requireNonNull(predicate, "Predicate cannot be null!");
        requireNonNull(parent, "Parent Conditional cannot be null!");
        this.value = value;
        this.predicate = predicate;
        this.parent = parent;
    }

    /**
     * Returns an {@code Conditional} of the specified nullable value to be predicated.
     *
     * @param <T> the class of the value
     * @param value the value to be predicated, which is nullable
     * @return an {@code Conditional} of the value
     */
    public static <T> Conditional<T> of(T value) {
        return new Conditional<>(value);
    }

    /**
     * Returns an {@code Conditional} of the specified nullable value to be predicated and on the default null-check.
     *
     * @param <T> the class of the value
     * @param value the value to be predicated, which is nullable
     * @return an {@code Conditional} of the value on the default null-check
     */
    @SuppressWarnings("unchecked")
    public static <T> Conditional<T> ofNullable(T value) {
        return new Conditional<>(value, (Predicate<T>) NULL_CHECK);
    }

    /**
     * Returns an {@code Conditional} which performs a null check on the specific value.
     *
     * @return an {@code Conditional} of the specific value on the default null-check
     */
    @SuppressWarnings("unchecked")
    public Conditional<T> onNullCheck() {
        setPredicate((Predicate<T>) NULL_CHECK);
        return this;
    }

    /**
     * Returns an {@code Conditional} which performs a specific non-null predicate on the specific value.
     *
     * @param predicate the specific predicate to test
     * @return an {@code Conditional} of the specific value on a specific predicate
     */
    @SuppressWarnings("HiddenField")
    public Conditional<T> on(Predicate<T> predicate) {
        requireNonNull(predicate, "Predicate cannot be null!");
        setPredicate(predicate);

        return this;
    }

    /**
     * Get result of testing the value on the predicate, if used in chained Conditional,
     * only return the result of last predicate statement.
     *
     * @return true if the value passes the predicate, otherwise false
     * @throws NullPointerException if {@code predicate} is null
     */
    public boolean get() {
        checkIfPredicateExist();

        return actualPerformPredicate();
    }

    /**
     * Set the specified consumer with the value, which will be invoked when the value passes the predicate.
     *
     * @param consumer block to be executed if a value passes the predicate
     * @return an {@code Conditional} of the specific value on a specific predicate and then consume a specific consumer
     * @throws NullPointerException if {@code consumer} is null
     */
    public Conditional<T> then(Consumer<T> consumer) {
        requireNonNull(consumer, "Then consumer cannot be null!");
        setThenConsumer(consumer);

        if (needSkip()) {
            return this;
        }

        // perform predicate and accept consumer here to align the following usage
        // Conditional.of(someValue).on(somePredicate).then(someConsumer);
        if (performPredicate()) {
            thenConsumer.accept(value);
        }

        return this;
    }

    /**
     * Set the specified converter with the value, which will be invoked and returned
     * when the value passes the predicate. Return a inner holder that returns a converted value.
     *
     * @param <R> the class of converted value
     * @param converter block to be executed and returned if a value passes the predicate
     * @return an instance of {@code ConditionalReturn} that returns a converted value
     * @throws NullPointerException if {@code predicate} is null or {@code consumer} is null
     */
    public <R> ConditionalReturn<R> thenReturn(Function<T, R> converter) {
        checkIfPredicateExist();
        requireNonNull(converter, "Then converter cannot be null!");

        return new ConditionalReturn<>(value, predicate, converter);
    }

    /**
     * Perform the predicate on the value, then apply the {@code thenConsumer} on the value
     * if the value passes the predicate, otherwise, apply the {@code elseConsumer}.
     *
     * @param elseConsumer the non-null consumer to be performed if the value does not pass the predicate
     * @throws NullPointerException if {@code elseConsumer} is null or {@code predicate} is null
     */
    public void orElse(Consumer<T> elseConsumer) {
        requireNonNull(elseConsumer, "Else consumer cannot be null!");

        if (needSkip()) {
            return;
        }

        if (performPredicate()) {
            thenConsumer.accept(value);
        } else {
            elseConsumer.accept(value);
        }
    }

    /**
     * Perform the predicate on the value, then apply the {@code thenConsumer} on the value
     * if the value passes the predicate, otherwise, throws what {@code exceptionSupplier} generates.
     *
     * @apiNote A method reference to the exception constructor with an empty
     *          argument list can be used as the supplier. For example, {@code IllegalStateException::new}
     *
     * @param <X> Type of the exception to be thrown
     * @param exceptionSupplier the non-null supplier which will return the exception to be thrown
     * @throws X if the value does not pass the predicate
     * @throws NullPointerException if {@code exceptionSupplier} is null or {@code predicate} is null
     */
    public <X extends Throwable> void orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        requireNonNull(exceptionSupplier, "Exception supplier cannot be null!");

        if (needSkip()) {
            return;
        }

        if (performPredicate()) {
            thenConsumer.accept(value);
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Perform the predicate on the value, then apply the {@code thenConsumer} on the value
     * if the value passes the predicate, otherwise, throws what {@code exceptionBuilder} generates on the value.
     *
     * @apiNote A method reference to the exception constructor with a single
     *          argument list can be used as the function if the value matches the argument type.
     *          For example, {@code IllegalStateException::new} when value is of {@code String} type
     *
     * @param <X> Type of the exception to be thrown
     * @param exceptionBuilder the non-null builder which will generate the exception to be thrown on the value
     * @throws X if the value does not pass the predicate
     * @throws NullPointerException if {@code exceptionBuilder} is null or {@code predicate} is null
     */
    public <X extends Throwable> void orElseThrow(Function<T, ? extends X> exceptionBuilder) throws X {
        requireNonNull(exceptionBuilder, "Exception builder cannot be null!");

        if (needSkip()) {
            return;
        }

        if (performPredicate()) {
            thenConsumer.accept(value);
        } else {
            throw exceptionBuilder.apply(value);
        }
    }

    /**
     * Create a chained Conditional object that will perform similar to if-elseif statement.
     *
     * @param predicate new predicate
     * @return a new Conditional with specific value and a new predicate
     */
    @SuppressWarnings("HiddenField")
    public Conditional<T> orElseIf(Predicate<T> predicate) {
        requireNonNull(predicate, "Predicate cannot be null!");

        return new Conditional<>(this.value, predicate, this);
    }

    /**
     * Check if predicate exist or not.
     *
     * @throws NullPointerException if {@code predicate} is null
     */
    private void checkIfPredicateExist() {
        if (isNull(predicate)) {
            throw new NullPointerException("Predicate is not given!");
        }
    }

    /**
     * Check if thenConsumer exist or not.
     *
     * @throws NullPointerException if {@code thenConsumer} is null
     */
    private void checkIfThenConsumerExist() {
        if (isNull(thenConsumer)) {
            throw new NullPointerException("Then consumer is not given!");
        }
    }

    /**
     * Check if need to skip execution or not, recursively check all predicates in the chained Conditional,
     * any true result in the parent will make this child performing to skip.
     */
    private boolean needSkip() {
        return parent != null && (parent.actualPerformPredicate() || parent.needSkip());
    }

    /**
     * Check if predicate exist or not, if not exist, set to passed-in predicate,
     * if already set, throws {@code IllegalStateException}
     *
     * @throws IllegalStateException if {@code predicate} is non-null
     */
    private void setPredicate(Predicate<T> predicate) {
        if (nonNull(this.predicate)) {
            throw new IllegalStateException("Predicate is already set!");
        }

        this.predicate = predicate;
    }

    /**
     * Check if then consumer exist or not, if not exist, set to passed-in then consumer,
     * if already set, throws {@code IllegalStateException}
     *
     * @throws IllegalStateException if {@code thenConsumer} is non-null
     */
    private void setThenConsumer(Consumer<T> thenConsumer) {
        if (nonNull(this.thenConsumer)) {
            throw new IllegalStateException("Then consumer is already set!");
        }

        this.thenConsumer = thenConsumer;
    }

    /**
     * Perform the predicate on the specific value.
     *
     * @return true if the value passes the predicate, otherwise false
     * @throws NullPointerException if {@code predicate} is null or {@code thenConsumer} is null
     */
    private boolean performPredicate() {
        checkIfPredicateExist();
        checkIfThenConsumerExist();

        return actualPerformPredicate();
    }

    /**
     * Check if result already exists or not before actually perform predicate,
     * will not perform same value on same predicate twice.
     *
     * @return the result of the predicate on the value
     */
    private boolean actualPerformPredicate() {
        if (result == null) {
            result = predicate.test(value);
        }

        return result;
    }

    /**
     * The inner class of {@code Conditional} that return a converted value instead of consuming it.
     *
     * @param <R> the class of converted value
     */
    public final class ConditionalReturn<R> {

        private final T value;
        private final Predicate<T> predicate;
        private final Function<T, ? extends R> thenConverter;

        private ConditionalReturn(T value, Predicate<T> predicate, Function<T, ? extends R> thenConverter) {
            this.value = value;
            this.predicate = predicate;
            this.thenConverter = thenConverter;
        }

        /**
         * Perform the predicate on the value, then apply the {@code thenConverter} on the value to get a return value
         * if the value passes the predicate, otherwise, return the {@code elseValue}.
         *
         * @param elseValue the nullable value to be returned if the value does not pass the predicate
         * @return converted value if value passes the predicate, otherwise {@code elseValue}
         */
        public R orElse(R elseValue) {
            if (predicate.test(value)) {
                return thenConverter.apply(value);
            } else {
                return elseValue;
            }
        }

        /**
         * Perform the predicate on the value, then apply the {@code thenConverter} on the value to get a return value
         * if the value passes the predicate, otherwise, return the value after applying {@code elseConverter}.
         *
         * @param elseConverter block to be executed and returned if a value does not pass the predicate
         * @return then converted value if value passes the predicate, otherwise {@code elseConverter} converted value
         * @throws NullPointerException if {@code elseConverter} is null
         */
        public R orElseReturn(Function<T, ? extends R> elseConverter) {
            requireNonNull(elseConverter, "Else converter cannot be null!");

            if (predicate.test(value)) {
                return thenConverter.apply(value);
            } else {
                return elseConverter.apply(value);
            }
        }

        /**
         * Perform the predicate on the value, then apply the {@code thenConverter} on the value to get a return value
         * if the value passes the predicate, otherwise, throws what {@code exceptionSupplier} generates.
         *
         * @apiNote A method reference to the exception constructor with an empty
         *          argument list can be used as the supplier. For example, {@code IllegalStateException::new}
         *
         * @param <X> Type of the exception to be thrown
         * @param exceptionSupplier the non-null supplier which will return the exception to be thrown
         * @return then converted value if value passes the predicate, otherwise throws an exception
         * @throws X if the value does not pass the predicate
         * @throws NullPointerException if {@code exceptionSupplier} is null
         */
        public <X extends Throwable> R orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            requireNonNull(exceptionSupplier, "Exception supplier cannot be null!");

            if (predicate.test(value)) {
                return thenConverter.apply(value);
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Perform the predicate on the value, then apply the {@code thenConverter} on the value to get a return value
         * if the value passes the predicate, otherwise, throws what {@code exceptionBuilder} generates on the value.
         *
         * @apiNote A method reference to the exception constructor with a single
         *          argument list can be used as the function if the value matches the argument type.
         *          For example, {@code IllegalStateException::new} when value is of {@code String} type
         *
         * @param <X> Type of the exception to be thrown
         * @param exceptionBuilder the non-null builder which will generate the exception to be thrown on the value
         * @return then converted value if value passes the predicate, otherwise throws an exception
         * @throws X if the value does not pass the predicate
         * @throws NullPointerException if {@code exceptionBuilder} is null
         */
        public <X extends Throwable> R orElseThrow(Function<T, ? extends X> exceptionBuilder) throws X {
            requireNonNull(exceptionBuilder, "Exception builder cannot be null!");

            if (predicate.test(value)) {
                return thenConverter.apply(value);
            } else {
                throw exceptionBuilder.apply(value);
            }
        }

    }

    /**
     * Indicates whether some other object is "equal to" this Conditional. It's not so useful currently
     * since functional objects are equal to each other only when they are the same reference.
     * The other object is considered equal if:
     *
     * <ul>
     * <li>it is also an {@code Conditional} and;
     * <li>the values are "equal to" each other via {@code equals()} and;
     * <li>the predicates are "equal to" each other via {@code equals()} and;
     * <li>the thenConsumers are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {code true} if the other object is "equal to" this object otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Conditional)) {
            return false;
        }

        Conditional<?> other = (Conditional<?>) obj;

        return internalEqualityCheck(value, other.value)
                       && internalEqualityCheck(predicate, other.predicate)
                       && internalEqualityCheck(thenConsumer, other.thenConsumer);
    }

    private static boolean internalEqualityCheck(Object a, Object b) {
        return Objects.equals(a, b);
    }

    /**
     * Returns the hash code value of the combination of value, predicate and thenConsumer, if any,
     * or 0 (zero) if no field is present.
     *
     * @return hash code value of the Conditional or 0 if no field is present
     */
    @Override
    public int hashCode() {
        return internalHashCodeCompute(value, predicate, thenConsumer);
    }

    private static int internalHashCodeCompute(Object... objects) {
        return Arrays.hashCode(objects);
    }

    /**
     * Returns a non-empty string representation of this Conditional suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return String.format("Conditional of value [%s] on condition [%s] to consume [%s]",
                             value, predicate, thenConsumer);
    }

}
