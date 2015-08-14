package com.whaka.util;

import com.google.common.base.Preconditions;

/**
 * <p>
 * Class provides static utility methods to perform simple numerical operations
 * on double value. For example, direct double comparison is not safe, so double
 * values need to be compared with a delta. This class provides methods for
 * common check and compare operations.
 * 
 * <p>
 * <b>Note:</b>
 * <ul>
 * <li>{@link #roundTo(double, int)}
 * <li>{@link #compare(double, double, double)}
 * <li>{@link #equals(Double, Double, double)}
 * </ul>
 * These methods take argument to configure "accuracy" of an operation. But!
 * <ul>
 * <li>{@link #round(double)}
 * <li>{@link #compare(double, double)}
 * <li>{@link #equals(Double, Double)}
 * </ul>
 * These methods select appropriate "accuracy" automatically. <b>Note again:</b>
 * there's no default constant epsilon (delta) to compare two double values.
 * Instead, methods select "last affected" decimal place, based on the exponent
 * of the specified value. See method: {@link #getLastAffectedDecimal(double)}
 */
public final class DoubleMath {

	/**
	 * Maximum value that can be automatically selected for
	 * {@link #round(double)} method. Also maximum decimal position to be used
	 * for {@link #compare(double, double)} method.
	 */
	public static final int MAXIMUM_DEFAULT_DECIMALS = 14;

	/**
	 * Maximum absolute value that can be used in the
	 * {@link #roundTo(double, int)} method.
	 */
	public static final int MAXIMUM_POSSIBLE_DECIMALS = 42;

	private DoubleMath() {
	}
	
	/**
	 * <p>If specified number is <code>null</code> - returns null.
	 * <p>If specified number is instance of Double - the same instance is returned.
	 * <p>In any other case - x.doubleValue() is returned.
	 */
	public static Double asDouble(Number x) {
		return x == null ? null : x instanceof Double ? (Double) x : (Double) x.doubleValue();
	}

	public static boolean isZero(Double d) {
		return equals(d, 0.0);
	}

	/**
	 * @return <code>true</code> if specified number is not <code>null</code>,
	 *         not {@link Double#NaN}, and greater than zero.
	 */
	public static boolean isPositive(Double d) {
		return isNumber(d) && compare(d, 0.0) > 0;
	}

	/**
	 * @return <code>true</code> if specified number is not <code>null</code>,
	 *         not {@link Double#NaN}, and lower than zero.
	 */
	public static boolean isNegative(Double d) {
		return isNumber(d) && compare(d, 0.0) < 0;
	}

	/**
	 * @return <code>true</code> if specified number is not <code>null</code>,
	 *         not {@link Double#NaN}, not {@link Double#POSITIVE_INFINITY}, and
	 *         not {@link Double#NEGATIVE_INFINITY}
	 */
	public static boolean isFinite(Double d) {
		return d != null && Double.isFinite(d);
	}

	/**
	 * @return <code>true</code> if specified number is not <code>null</code>
	 *         and <b>not</b> {@link Double#NaN}
	 */
	public static boolean isNumber(Double d) {
		return d != null && !Double.isNaN(d);
	}

	public static boolean equals(Double a, Double b) {
		if (a == null || b == null)
			return a == b;
		return compare(a, b) == 0;
	}

	public static boolean equals(Double a, Double b, double accuracy) {
		if (a == null || b == null)
			return a == b;
		return compare(a, b, accuracy) == 0;
	}

	public static int compare(double a, double b) {
		return performCompareWithOptionalAccuracy(a, b, null);
	}

	public static int compare(double a, double b, double accuracy) {
		Preconditions.checkArgument(Double.isFinite(accuracy)
				&& accuracy >= 0.0,
				"Accuracy should be a finite positive number!");
		return performCompareWithOptionalAccuracy(a, b, accuracy);
	}

	private static int performCompareWithOptionalAccuracy(double a, double b,
			Double accuracy) {
		if (!Double.isFinite(a) || !Double.isFinite(b))
			return Double.compare(a, b);
		int lastDecimal = getLastAffectedDecimal(a);
		if (lastDecimal != getLastAffectedDecimal(b))
			return Double.compare(a, b);
		if (accuracy == null)
			accuracy = Math.pow(10, -lastDecimal);
		double delta = roundTo(a - b, lastDecimal + 1);
		if (Math.abs(delta) >= accuracy)
			return (int) Math.signum(delta);
		return 0;
	}

	public static double round(double a) {
		return roundTo(a, getLastAffectedDecimal(a));
	}

	/**
	 * <p>Method returns dynamically calculated "index" (deximal position)
	 * of the digit that is considered as last for rounding or comparison.
	 * Used to dinamically calculate "accuracy" for the equality, comparison, or rounding operations.
	 * 
	 * <p><b>Note again:</b> there's no default constant epsilon (delta) to compare two double values.
	 * Instead, methods select "last affected" decimal place, based on the exponent of the specified value.
	 * Basic formula is:
	 * 
	 * <pre>
	 * ({@value #MAXIMUM_DEFAULT_DECIMALS} + 1) - number of digits before decimal point
	 * </pre>
	 * 
	 * <b>Note</b> that zero before decimal point is treated as one digit. For
	 * example:
	 * <ul>
	 * 	<li>For the number <code>0.01</code> selected decimal place will be: {@value #MAXIMUM_DEFAULT_DECIMALS}
	 * 	<li>For the number <code>0.1</code> selected decimal place will be: {@value #MAXIMUM_DEFAULT_DECIMALS}
	 * 	<li>For the number <code>1.0</code> selected decimal place will be: {@value #MAXIMUM_DEFAULT_DECIMALS}
	 * 	<li>For the number <code>10.0</code> selected decimal place will be: {@value #MAXIMUM_DEFAULT_DECIMALS} - 1
	 * 	<li>For the number <code>10 000.0</code> selected decimal place will be: {@value #MAXIMUM_DEFAULT_DECIMALS} - 4
	 * </ul>
	 * 
	 * <p> Therefore the bigger the integer part of a number - the lower its "accuracy". Just the way doubles are.<br>
	 * But it doesn't work the other way around - selected decimal place never will be greater
	 * than {@link #MAXIMUM_DEFAULT_DECIMALS}.<br>
	 * Basically for any <code>x</code> where <code>|x| < 10.0</code> - maximum possible accuracy will be used.<br>
	 * For any double with more than {@value #MAXIMUM_DEFAULT_DECIMALS} digits before decimal point - 0 is returned.
	 */
	public static int getLastAffectedDecimal(double d) {
		if (!Double.isFinite(d))
			return 0;
		int decimalPosition = (int) Math.log10(d);
		if (decimalPosition < 0)
			return MAXIMUM_DEFAULT_DECIMALS;
		if (decimalPosition > MAXIMUM_DEFAULT_DECIMALS)
			return 0;
		return MAXIMUM_DEFAULT_DECIMALS - decimalPosition;
	}

	/**
	 * @see #MAXIMUM_POSSIBLE_DECIMALS
	 */
	public static double roundTo(double a, int decimals) {
		Preconditions.checkArgument(
				Math.abs(decimals) <= MAXIMUM_POSSIBLE_DECIMALS,
				"Illegal decimals! Max possible value: "
						+ MAXIMUM_POSSIBLE_DECIMALS);
		if (!Double.isFinite(a))
			return a;
		if (decimals == 0)
			return Math.round(a);
		double pow = Math.pow(10, decimals);
		return Math.round(a * pow) / pow;
	}
}
