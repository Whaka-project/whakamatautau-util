package org.whaka.util.reflection.comparison;

import java.util.function.BiPredicate;

public interface ComparisonPerformer<T> {

	ComparisonResult compare(T actual, T expected);

	/**
	 * Used to identify performer in a result.
	 * Recommended to return human readable ID of the performer
	 */
	default String getName() {
		return "@" + System.identityHashCode(this);
	}
	
	default BiPredicate<T, T> toPredicate() {
		return (a,b) -> compare(a, b).isSuccess();
	}
}
