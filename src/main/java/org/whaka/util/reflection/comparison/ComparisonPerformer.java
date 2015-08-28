package org.whaka.util.reflection.comparison;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * <p>Comparison performer represents both a {@link BiFunction} that converts two instances of the same type
 * into a {@link ComparisonResult}, <b>and</b> a {@link BiPredicate} that converts two instances of the same type
 * into a boolean answer.
 * 
 * <p>Main function of a performer is to perform some complex comparison and to provide a proper extended result.
 * But sometimes there's no need for a result like this, and a simple "yes/no" answer will suffice. For case like this
 * it is also a predicate.
 * 
 * <p><b>Note:</b> there's a default implementation of the {@link #test(Object, Object)} method that simply
 * calls {@link #apply(Object, Object)} and then checks received result for success. But more complex performers
 * might also override it and provide it's own implementation, for example to limit some costly operations, required
 * for a more extended compare.
 */
public interface ComparisonPerformer<T> extends BiFunction<T, T, ComparisonResult>, BiPredicate<T, T> {

	@Override
	ComparisonResult apply(T actual, T expected);
	
	/**
	 * May be used in case simple yes/no answer is enough and there's no need for extended {@link ComparisonResult}.
	 * <b>Note:</b> by default this method just calls {@link #apply(Object, Object)} and checks result for success.
	 * 
	 * @see #apply(Object, Object)
	 */
	@Override
	default boolean test(T a, T b) {
		return apply(a, b).isSuccess();
	}
	
	/**
	 * Used to identify performer in a result.
	 * Recommended to return human readable ID of the performer
	 */
	default String getName() {
		return "@" + System.identityHashCode(this);
	}
}
