package org.whaka.util.function;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * <p>Equal to {@link BiPredicate} but with 5 arguments.
 * 
 * @see #toPredicate(Predicate5)
 * @see #toBiPredicate(Predicate5)
 */
@FunctionalInterface
public interface Predicate5<A,B,C,D,E> {

	boolean test(A a, B b, C c, D d, E e);
	
	/**
	 * Converts specified predicate into a {@link Predicate} were all arguments are represented
	 * as a single {@link Tuple5} instance.
	 */
	static <A,B,C,D,E> Predicate<Tuple5<A, B, C, D, E>> toPredicate(Predicate5<A, B, C, D, E> delegate) {
		return e -> delegate.test(e._1, e._2, e._3, e._4, e._5);
	}
	
	/**
	 * Converts specified predicate into a {@link BiPredicate} were all arguments except the first one are represented
	 * as a single {@link Tuple3} instance.
	 */
	static <A,B,C,D,E> BiPredicate<A, Tuple4<B, C, D, E>> toBiPredicate(Predicate5<A, B, C, D, E> delegate) {
		return (a,e) -> delegate.test(a, e._1, e._2, e._3, e._4);
	}
	
	default Predicate5<A, B, C, D, E> and(Predicate5<? super A, ? super B, ? super C, ? super D, ? super E> other) {
		Objects.requireNonNull(other, "Chained predicate cannot be null!");
		return (a,b,c,d,e) -> test(a, b, c, d, e) && other.test(a, b, c, d, e);
	}
	
	default Predicate5<A, B, C, D, E> or(Predicate5<? super A, ? super B, ? super C, ? super D, ? super E> other) {
		Objects.requireNonNull(other, "Chained predicate cannot be null!");
		return (a,b,c,d,e) -> test(a, b, c, d, e) || other.test(a, b, c, d, e);
	}
	
	/**
	 * Negate specified predicate
	 */
	static <A,B,C,D,E> Predicate5<A, B, C, D, E> not(Predicate5<A, B, C, D, E> delegate) {
		Objects.requireNonNull(delegate, "Negated predicate cannot be null!");
		return (a,b,c,d,e) -> !delegate.test(a, b, c, d, e);
	}
}
