package org.whaka.util.reflection.comparison;

import java.util.function.BiPredicate;

/**
 * Comparison performer represents
 */
public interface ComparisonPerformer<T> extends BiPredicate<T, T> {

	ComparisonResult appl(T actual, T expected);

	/**
	 * Used to identify performer in a result.
	 * Recommended to return human readable ID of the performer
	 */
	default String getName() {
		return "@" + System.identityHashCode(this);
	}
	
	/**
	 * May be used in case simple yes/no answer is enough and there's no need for extended {@link ComparisonResult}.
	 * <b>Note:</b> by default this method just calls {@link #appl(Object, Object)} and checks result for success.
	 * 
	 * @see #appl(Object, Object)
	 */
	@Override
	default boolean test(T a, T b) {
		return appl(a, b).isSuccess();
	}
}
