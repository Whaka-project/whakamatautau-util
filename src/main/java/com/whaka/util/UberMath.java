package com.whaka.util;

import java.util.function.IntToDoubleFunction;

import com.google.common.math.DoubleMath;

public class UberMath {

	/**
	 * <p>Calculate binomial coefficient for the specified <b>n</b> and <b>k</b>.
	 * 
	 * <p>In other words, for a set of specified size <b>n</b> method calculates
	 * number of possible distinct combinations of the specified size <b>k</b>.
	 * 
	 * <p>Example, in the set [a, b, c] of size 3 - there're 3 possible combinations of size 2:
	 * <ul>
	 * 	<li>a, b
	 * 	<li>a, c
	 * 	<li>b, c
	 * </ul>
	 * Or one possible combination, of size 3:
	 * <ul>
	 * 	<li>a, b, c
	 * </ul>
	 * 
	 * <p><b>Note:</b>
	 * <pre>
	 * 	<li>If <b>k</b> = 0		method returns 1 for any n
	 * 	<li>If <b>k</b> = 1		method returns n for any n > 0
	 * 	<li>If <b>k</b> = (n-1)		method returns n for any n > 0
	 * 	<li>If <b>k</b> = n		method returns 1 for any n
	 * </pre>
	 * 
	 * @param n - size of a set. Required positive.
	 * @param k - size of a combination. Required to be >= 0 and <= n.
	 */
	public static int nChooseK(int n, int k) {
		IntToDoubleFunction f = DoubleMath::factorial;
		double nf = f.applyAsDouble(n);
		double kf = f.applyAsDouble(k);
		double nkf = f.applyAsDouble(n - k);
		return (int) Math.round(nf / (kf * nkf));
	}
}
