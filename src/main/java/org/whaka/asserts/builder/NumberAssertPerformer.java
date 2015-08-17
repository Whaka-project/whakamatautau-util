package org.whaka.asserts.builder;

import static org.whaka.util.DoubleMath.*;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.whaka.asserts.AssertResult;
import org.whaka.util.DoubleMath;

/**
 * <p>Class uses {@link DoubleMath} util to compare any numbers as doubles.
 *
 * <p><b>Note:</b> next methods use {@link DoubleMath#compare(double, double)}:
 * <ul>
 * 	<li>{@link #isGreaterThan(Number)}
 * 	<li>{@link #isLowerThan(Number)}
 * 	<li>{@link #isBetween(Number, Number)}
 * 	<li>{@link #isGreaterThanOrEqual(Number)}
 * 	<li>{@link #isLowerThanOrEqual(Number)}
 * 	<li>{@link #isBetweenOrEqual(Number, Number)}
 * </ul>
 * It means they treat <code>NaN</code> as the greatest number there is,
 * AND they create 'comparison fail' if any of arguments is <code>null</code>.
 *
 * <p>To perform comparison safe for <code>null</code> or <code>NaN</code> use these methods:
 * <ul>
 * 	<li>{@link #isPositive()}
 * 	<li>{@link #isNegative()}
 * </ul>
 * Or use combined asserts.
 */
public class NumberAssertPerformer extends AssertPerformer<Number> {

	public static final String MESSAGE_EQUAL_NUMBERS = "Numbers expected to be equal!";
	public static final String MESSAGE_NOT_EQUAL_NUMBERS = "Numbers expected to be NOT equal!";
	public static final String MESSAGE_INCOMPARABLE_NUMBERS = "Numbers cannot be compared!";
	public static final String MESSAGE_NUMBERS_COMPARE_FAIL = "Numbers comparison failed!";
	
	public NumberAssertPerformer(Number actual, Consumer<AssertResult> consumer) {
		super(actual, consumer);
	}
	
	public AssertResultConstructor isZero() {
		return isEqual(0.0);
	}
	
	public AssertResultConstructor isEqual(Number expected) {
		return performCheck((a,e) -> DoubleMath.equals(asDouble(a), asDouble(e)),
			expected).withMessage(MESSAGE_EQUAL_NUMBERS);
	}
	
	public AssertResultConstructor isNotEqual(Number unexpected) {
		String formattedExpected = "Not: " + unexpected;
		return performCheck((a,e) -> !DoubleMath.equals(asDouble(a), asDouble(unexpected)),
			formattedExpected).withMessage(MESSAGE_NOT_EQUAL_NUMBERS);
	}
	
	public AssertResultConstructor isNumber() {
		return performActualValueCheck(DoubleMath::isNumber, "Any number").withMessage(MESSAGE_EQUAL_NUMBERS);
	}
	
	public AssertResultConstructor isFinite() {
		return performActualValueCheck(DoubleMath::isFinite, "Any finite number").withMessage(MESSAGE_EQUAL_NUMBERS);
	}
	
	public AssertResultConstructor isPositive() {
		return performActualValueCheck(DoubleMath::isPositive, "Any positive number").withMessage(MESSAGE_EQUAL_NUMBERS);
	}
	
	public AssertResultConstructor isNegative() {
		return performActualValueCheck(DoubleMath::isNegative, "Any negative number").withMessage(MESSAGE_EQUAL_NUMBERS);
	}
	
	private AssertResultConstructor performActualValueCheck(Predicate<Double> predicate, Object expected) {
		return performCheck((a,e) -> predicate.test(asDouble(a)), expected);
	}
	
	public AssertResultConstructor isGreaterThan(Number other) {
		return performCompareResultCheck(other, i -> i > 0, ">" + other);
	}
	
	public AssertResultConstructor isLowerThan(Number other) {
		return performCompareResultCheck(other, i -> i < 0, "<" + other);
	}
	
	public AssertResultConstructor isGreaterThanOrEqual(Number other) {
		return performCompareResultCheck(other, i -> i >= 0, ">=" + other);
	}
	
	public AssertResultConstructor isLowerThanOrEqual(Number other) {
		return performCompareResultCheck(other, i -> i <= 0, "<=" + other);
	}
	
	private AssertResultConstructor performCompareResultCheck(Number other,
			Predicate<Integer> resultPredicate, Object formattedExpected) {
		if (getActual() == null || other == null)
			return createAndPerformResult(formattedExpected, MESSAGE_INCOMPARABLE_NUMBERS);
		int comparisonResult = DoubleMath.compare(asDouble(getActual()), asDouble(other));
		if (!resultPredicate.test(comparisonResult))
			return createAndPerformResult(formattedExpected, MESSAGE_NUMBERS_COMPARE_FAIL);
		return AssertResultConstructor.EMPTY;
	}

	public AssertResultConstructor isBetween(Number min, Number max) {
		String formattedMessage = String.format("%s<>%s", min, max);
		return performCompareResultRangeCheck(min, max, (a,b) -> a > 0 && b < 0, formattedMessage);
	}
	
	public AssertResultConstructor isBetweenOrEqual(Number min, Number max) {
		String formattedMessage = String.format("%s<=>%s", min, max);
		return performCompareResultRangeCheck(min, max, (a,b) -> a >= 0 && b <= 0, formattedMessage);
	}
	
	private AssertResultConstructor performCompareResultRangeCheck(Number min, Number max,
			BiPredicate<Integer, Integer> rangePredicate, String formattedMessage) {
		if (getActual() == null || min == null || max == null)
			return createAndPerformResult(formattedMessage, MESSAGE_INCOMPARABLE_NUMBERS);
		int compareMin = DoubleMath.compare(asDouble(getActual()), asDouble(min));
		int compareMax = DoubleMath.compare(asDouble(getActual()), asDouble(max));
		if (!rangePredicate.test(compareMin, compareMax))
			return createAndPerformResult(formattedMessage, MESSAGE_NUMBERS_COMPARE_FAIL);
		return AssertResultConstructor.EMPTY;
	}
	
	private AssertResultConstructor createAndPerformResult(Object expected, String message) {
		AssertResult result = performResult(getActual(), expected);
		return AssertResultConstructor.create(result).withMessage(message);
	}
}
