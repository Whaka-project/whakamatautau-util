package org.whaka.util.function;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * <p>Equal to {@link BiPredicate} but with 3 arguments.
 * 
 * @see #toPredicate(Predicate3)
 * @see #toBiPredicate(Predicate3)
 */
@FunctionalInterface
public interface Predicate3<A,B,C> {

	boolean test(A a, B b, C c);
	
	/**
	 * Converts this predicate into a {@link Predicate} were all arguments are represented
	 * as a single {@link Tuple3} instance.
	 */
	static <A,B,C> Predicate<Tuple3<A, B, C>> toPredicate(Predicate3<A, B, C> delegate) {
		return e -> delegate.test(e._1, e._2, e._3);
	}
	
	/**
	 * Converts specified predicate into a {@link BiPredicate} were all arguments except the first one are represented
	 * as a single {@link Tuple2} instance.
	 */
	static <A,B,C> BiPredicate<A, Tuple2<B, C>> toBiPredicate(Predicate3<A, B, C> delegate) {
		return (a,e) -> delegate.test(a, e._1, e._2);
	}
	
	default Predicate3<A, B, C> and(Predicate3<? super A, ? super B, ? super C> other) {
		Objects.requireNonNull(other, "Chained predicate cannot be null!");
		return (a,b,c) -> test(a, b, c) && other.test(a, b, c);
	}
	
	default Predicate3<A, B, C> or(Predicate3<? super A, ? super B, ? super C> other) {
		Objects.requireNonNull(other, "Chained predicate cannot be null!");
		return (a,b,c) -> test(a, b, c) || other.test(a, b, c);
	}
	
	/**
	 * Negate specified predicate
	 */
	static <A,B,C> Predicate3<A, B, C> not(Predicate3<A, B, C> delegate) {
		Objects.requireNonNull(delegate, "Negated predicate cannot be null!");
		return (a,b,c) -> !delegate.test(a, b, c);
	}
}
