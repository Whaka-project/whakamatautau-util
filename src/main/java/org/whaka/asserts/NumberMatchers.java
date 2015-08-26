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

/**
 * Class provides static factory methods for custom Hamcrest matchers related to numbers.
 * 
 * @see UberMatchers
 */
public final class NumberMatchers {

	private NumberMatchers() {
	}
	
	public static Matcher<Double> number() {
		return new FunctionalMatcher<>(Double.class, DoubleMath::isNumber, createNameDescriber("number"));
	}
	
	public static Matcher<Double> finite() {
		return new FunctionalMatcher<>(Double.class, DoubleMath::isFinite, createNameDescriber("finite number"));
	}
	
	public static Matcher<Number> zero() {
		return new FunctionalMatcher<>(Number.class, convertPredicate(DoubleMath::isZero), d -> d.appendValue(0));
	}
	
	public static Matcher<Number> positive() {
		return new FunctionalMatcher<>(Number.class, convertPredicate(DoubleMath::isPositive), createNameDescriber("positive number"));
	}
	
	public static Matcher<Number> negative() {
		return new FunctionalMatcher<>(Number.class, convertPredicate(DoubleMath::isPositive), createNameDescriber("negative number"));
	}
	
	private static Predicate<Number> convertPredicate(Predicate<Double> delegate) {
		return a -> delegate.test(asDouble(a));
	}
	
	private static Consumer<Description> createNameDescriber(String operationName) {
		return d -> d.appendText(operationName);
	}
	
	public static Matcher<Number> equalTo(Number value) {
		return new FunctionalMatcher<>(Number.class,
				convertBiPredicate(DoubleMath::equals, value),
				createSingleValueDescriber("equal to", value));
	}
	
	private static Predicate<Number> convertBiPredicate(BiPredicate<Double, Double> delegate, Number value) {
		return a -> delegate.test(asDouble(a), asDouble(value));
	}
	
	public static Matcher<Number> greaterThan(Number value) {
		Objects.requireNonNull(value, "Cannot compare to null!");
		return new FunctionalMatcher<>(Number.class,
				convertCompareResultPredicate(i -> i > 0, value),
				createSingleValueDescriber("greater than", value));
	}
	
	public static Matcher<Number> lowerThan(Number value) {
		Objects.requireNonNull(value, "Cannot compare to null!");
		return new FunctionalMatcher<>(Number.class,
				convertCompareResultPredicate(i -> i < 0, value),
				createSingleValueDescriber("lower than", value));
	}
	
	public static Matcher<Number> greaterThanOrEqual(Number value) {
		Objects.requireNonNull(value, "Cannot compare to null!");
		return new FunctionalMatcher<>(Number.class,
				convertCompareResultPredicate(i -> i >= 0, value),
				createSingleValueDescriber("greater than or equal to", value));
	}
	
	public static Matcher<Number> lowerThanOrEqual(Number value) {
		Objects.requireNonNull(value, "Cannot compare to null!");
		return new FunctionalMatcher<>(Number.class,
				convertCompareResultPredicate(i -> i <= 0, value),
				createSingleValueDescriber("lower than or equal to", value));
	}
	
	private static Predicate<Number> convertCompareResultPredicate(IntPredicate predicate, Number value) {
		return convertBiPredicate((a, b) -> a != null && b != null && predicate.test(DoubleMath.compare(a, b)), value);
	}
	
	private static Consumer<Description> createSingleValueDescriber(String operationName, Number value) {
		return d -> d.appendText(String.format("number %s ", operationName)).appendValue(value);
	}
	
	public static Matcher<Number> between(Number min, Number max) {
		Objects.requireNonNull(min, "Cannot compare to null!");
		Objects.requireNonNull(max, "Cannot compare to null!");
		return new FunctionalMatcher<>(Number.class,
				convertBiCompareResultPredicate((a, b) -> a > 0 && b < 0, min, max),
				createBiCompareDescriber("<>", min, max));
	}
	
	public static Matcher<Number> betweenOrEqual(Number min, Number max) {
		Objects.requireNonNull(min, "Cannot compare to null!");
		Objects.requireNonNull(max, "Cannot compare to null!");
		return new FunctionalMatcher<>(Number.class,
				convertBiCompareResultPredicate((a, b) -> a >= 0 && b <= 0, min, max),
				createBiCompareDescriber("<=>", min, max));
	}
	
	private static Predicate<Number> convertBiCompareResultPredicate(BiPredicate<Integer, Integer> predicate, Number a, Number b) {
		return convertPredicate(x -> x != null && predicate.test(
				DoubleMath.compare(x, asDouble(a)),
				DoubleMath.compare(x, asDouble(b))));
	}
	
	private static Consumer<Description> createBiCompareDescriber(String operationName, Number min, Number max) {
		return d -> d.appendValue(min).appendText(String.format(" % ", operationName)).appendValue(max);
	}
}
