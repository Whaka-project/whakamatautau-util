package org.whaka.asserts;

import static org.whaka.util.DoubleMath.*;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.whaka.asserts.matcher.FunctionalMatcher;
import org.whaka.util.DoubleMath;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Class provides static factory methods for custom Hamcrest matchers related to numbers.
 * 
 * @see DoubleMath
 * @see UberMatchers
 */
public final class NumberMatchers {

	// Memoizers are used to ensure "invariant" matchers are instantiated on demand, BUT only once
	
	private static final Supplier<Matcher<Double>> IS_NUMBER = Suppliers.memoize(
			() -> new FunctionalMatcher<>(Double.class, DoubleMath::isNumber, "number"));
	
	private static final Supplier<Matcher<Double>> IS_FINITE = Suppliers.memoize(
			() -> new FunctionalMatcher<>(Double.class, DoubleMath::isFinite, "finite number"));
	
	private static final Supplier<Matcher<Number>> IS_ZERO = Suppliers.memoize(
			() -> new FunctionalMatcher<>(Number.class, convertPredicate(DoubleMath::isZero), d -> d.appendValue(0)));
	
	private static final Supplier<Matcher<Number>> IS_POSITIVE = Suppliers.memoize(
			() -> new FunctionalMatcher<>(Number.class, convertPredicate(DoubleMath::isPositive), "positive number"));
	
	private static final Supplier<Matcher<Number>> IS_NEGATIVE = Suppliers.memoize(
			() -> new FunctionalMatcher<>(Number.class, convertPredicate(DoubleMath::isNegative), "negative number"));
	
	private NumberMatchers() {
	}
	
	/**
	 * Matcher equivalent of the {@link DoubleMath#isNumber(Double)}.
	 * Matches if item is <b>not null</b> and <b>not {@link Double#NaN}</b>
	 */
	public static Matcher<Double> number() {
		return IS_NUMBER.get();
	}
	
	/**
	 * Matcher equivalent of the {@link DoubleMath#isFinite(Double)}.
	 * Matches if item is <b>not null</b>, <b>not {@link Double#NaN}</b>, and <b>is finite</b>.
	 */
	public static Matcher<Double> finite() {
		return IS_FINITE.get();
	}
	
	/**
	 * Matcher equivalent of the {@link DoubleMath#isZero(Double)}.
	 * Matches if item is <b>not null</b> and <b>equal to 0.0</b>.
	 */
	public static Matcher<Number> zero() {
		return IS_ZERO.get();
	}
	
	/**
	 * <p>Matcher equivalent of the {@link DoubleMath#isPositive(Double)}.
	 * Matches if item is <b>number</b> and <b>greater than 0.0</b>.
	 */
	public static Matcher<Number> positive() {
		return IS_POSITIVE.get();
	}
	
	/**
	 * Matcher equivalent of the {@link DoubleMath#isNegative(Double)}.
	 * Matches if item is <b>number</b> and <b>lower than 0.0</b>.
	 */
	public static Matcher<Number> negative() {
		return IS_NEGATIVE.get();
	}
	
	private static Predicate<Number> convertPredicate(Predicate<Double> delegate) {
		return a -> delegate.test(asDouble(a));
	}
	
	/**
	 * Matcher equivalent of the {@link DoubleMath#equals(Double, Double)}.
	 * Matches if item is <b>double-equal</b> to the specified number value.
	 */
	public static Matcher<Number> equalTo(Number value) {
		return new FunctionalMatcher<>(Number.class,
				convertBiPredicate(DoubleMath::equals, value),
				createSingleValueDescriber("equal to", value));
	}
	
	private static Predicate<Number> convertBiPredicate(BiPredicate<Double, Double> delegate, Number value) {
		return a -> delegate.test(asDouble(a), asDouble(value));
	}
	
	/**
	 * Matcher equivalent of the {@link DoubleMath#compare(double, double)}.
	 * Matches if item is <b>not-null</b>, and <b>double-greater</b> than specified number value.
	 * 
	 * @throws NullPointerException if specified value is <code>null</code>
	 */
	public static Matcher<Number> greaterThan(Number value) {
		return createCompareMatcher(value, i -> i > 0, "greater than");
	}
	
	/**
	 * Matcher equivalent of the {@link DoubleMath#compare(double, double)}.
	 * Matches if item is <b>not-null</b>, and <b>double-lower</b> than specified number value.
	 * 
	 * @throws NullPointerException if specified value is <code>null</code>
	 */
	public static Matcher<Number> lowerThan(Number value) {
		return createCompareMatcher(value, i -> i < 0, "lower than");
	}
	
	/**
	 * Matcher equivalent of the {@link DoubleMath#compare(double, double)}.
	 * Matches if item is <b>not-null</b>, and <b>double-greater</b> than or <b>double-equal</b> to the specified number value.
	 * 
	 * @throws NullPointerException if specified value is <code>null</code>
	 */
	public static Matcher<Number> greaterThanOrEqual(Number value) {
		return createCompareMatcher(value, i -> i >= 0, "greater than or equal to");
	}
	
	/**
	 * Matcher equivalent of the {@link DoubleMath#compare(double, double)}.
	 * Matches if item is <b>not-null</b>, and <b>double-lower</b> than or <b>double-equal</b> to the specified number value.
	 * 
	 * @throws NullPointerException if specified value is <code>null</code>
	 */
	public static Matcher<Number> lowerThanOrEqual(Number value) {
		return createCompareMatcher(value, i -> i <= 0, "lower than or equal to");
	}
	
	private static Matcher<Number> createCompareMatcher(Number value, IntPredicate predicate, String op) {
		Objects.requireNonNull(value, "Cannot compare to null!");
		return new FunctionalMatcher<>(Number.class,
				convertCompareResultPredicate(predicate, value),
				createSingleValueDescriber(op, value));
	}
	
	private static Predicate<Number> convertCompareResultPredicate(IntPredicate predicate, Number value) {
		return convertBiPredicate((a, b) -> a != null && b != null && predicate.test(DoubleMath.compare(a, b)), value);
	}
	
	private static Consumer<Description> createSingleValueDescriber(String operationName, Number value) {
		return d -> d.appendText(String.format("number %s ", operationName)).appendValue(value);
	}
	
	/**
	 * Matcher equivalent of the {@link DoubleMath#compare(double, double)} for two bound arguments.
	 * Matches if item is <b>double-greater</b> than first specified number,
	 * or <b>double-greater</b> than second specified argument.
	 * 
	 * @throws NullPointerException if either specified value is <code>null</code>
	 * @throws IllegalArgumentException if specified min is greater than specified max
	 */
	public static Matcher<Number> between(Number min, Number max) {
		return createBiCompareMatcher(min, max, (a, b) -> a > 0 && b < 0, "number between");
	}
	
	/**
	 * Matcher equivalent of the {@link DoubleMath#compare(double, double)} for two bound arguments.
	 * Matches if item is <b>double-greater</b> than first specified number,
	 * or <b>double-greater</b> than second specified argument, or <b>double-equal</b> to any of them.
	 * 
	 * @throws NullPointerException if either specified value is <code>null</code>
	 * @throws IllegalArgumentException if specified min is greater than specified max
	 */
	public static Matcher<Number> betweenOrEqual(Number min, Number max) {
		return createBiCompareMatcher(min, max, (a, b) -> a >= 0 && b <= 0, "number between or equal");
	}
	
	private static Matcher<Number> createBiCompareMatcher(Number min, Number max, BiPredicate<Integer, Integer> predicate, String op) {
		Objects.requireNonNull(min, "Cannot compare to null!");
		Objects.requireNonNull(max, "Cannot compare to null!");
		Preconditions.checkArgument(DoubleMath.compare(asDouble(min), asDouble(max)) <= 0, "min is greater than max!");
		return new FunctionalMatcher<>(Number.class,
				convertBiCompareResultPredicate(predicate, min, max),
				createBiCompareDescriber(op, min, max));
	}
	
	private static Predicate<Number> convertBiCompareResultPredicate(BiPredicate<Integer, Integer> predicate, Number a, Number b) {
		return convertPredicate(x -> x != null && predicate.test(
				DoubleMath.compare(x, asDouble(a)),
				DoubleMath.compare(x, asDouble(b))));
	}
	
	private static Consumer<Description> createBiCompareDescriber(String operationName, Number min, Number max) {
		return d -> d.appendText(operationName + " ").appendValue(min).appendText(" and ").appendValue(max);
	}
}
