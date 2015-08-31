package org.whaka.util.function;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * <p>Equal to {@link BiPredicate} but with 3 arguments.
 * 
 * @see #toPredicate()
 * @see #toBiPredicate()
 */
public interface Predicate3<A,B,C> {

	boolean test(A a, B b, C c);
	
	/**
	 * Converts this predicate into a {@link Predicate} were all arguments are represented
	 * as a single {@link Tuple3} instance.
	 * 
	 * @see #toBiPredicate()
	 */
	default Predicate<Tuple3<A, B, C>> toPredicate() {
		return e -> test(e._1, e._2, e._3);
	}
	
	/**
	 * Converts this predicate into a {@link BiPredicate} were all arguments except the first one are represented
	 * as a single {@link Tuple2} instance.
	 * 
	 * @see #toConsumer()
	 */
	default BiPredicate<A, Tuple2<B, C>> toBiPredicate() {
		return (a,e) -> test(a, e._1, e._2);
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
