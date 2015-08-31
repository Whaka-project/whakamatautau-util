package org.whaka.util.function;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * <p>Equal to {@link BiPredicate}.
 * 
 * @see #toPredicate(Predicate2)
 */
@FunctionalInterface
public interface Predicate2<A,B> extends BiPredicate<A, B> {

	@Override
	boolean test(A a, B b);
	
	/**
	 * Converts this predicate into a {@link Predicate} were all arguments are represented
	 * as a single {@link Tuple2} instance.
	 */
	static <A,B> Predicate<Tuple2<A, B>> toPredicate(Predicate2<A, B> delegate) {
		return e -> delegate.test(e._1, e._2);
	}
	
	/**
	 * Negate specified predicate
	 */
	static <A,B> Predicate2<A, B> not(Predicate2<A, B> delegate) {
		Objects.requireNonNull(delegate, "Negated predicate cannot be null!");
		return (a,b) -> !delegate.test(a, b);
	}
}
