package org.whaka.util.function;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * <p>Equal to {@link BiPredicate} but with 4 arguments.
 * 
 * @see #toPredicate()
 * @see #toBiPredicate()
 */
public interface Predicate4<A,B,C,D> {

	boolean test(A a, B b, C c, D d);
	
	/**
	 * Converts this predicate into a {@link Predicate} were all arguments are represented
	 * as a single {@link Tuple4} instance.
	 * 
	 * @see #toBiPredicate()
	 */
	default Predicate<Tuple4<A, B, C, D>> toPredicate() {
		return e -> test(e._1, e._2, e._3, e._4);
	}
	
	/**
	 * Converts this predicate into a {@link BiPredicate} were all arguments except the first one are represented
	 * as a single {@link Tuple3} instance.
	 * 
	 * @see #toConsumer()
	 */
	default BiPredicate<A, Tuple3<B, C, D>> toBiPredicate() {
		return (a,e) -> test(a, e._1, e._2, e._3);
	}
	
	default Predicate4<A, B, C, D> and(Predicate4<? super A, ? super B, ? super C, ? super D> other) {
		Objects.requireNonNull(other, "Chained predicate cannot be null!");
		return (a,b,c,d) -> test(a, b, c, d) && other.test(a, b, c, d);
	}
	
	default Predicate4<A, B, C, D> or(Predicate4<? super A, ? super B, ? super C, ? super D> other) {
		Objects.requireNonNull(other, "Chained predicate cannot be null!");
		return (a,b,c,d) -> test(a, b, c, d) || other.test(a, b, c, d);
	}
	
	/**
	 * Negate specified predicate
	 */
	static <A,B,C,D> Predicate4<A, B, C, D> not(Predicate4<A, B, C, D> delegate) {
		Objects.requireNonNull(delegate, "Negated predicate cannot be null!");
		return (a,b,c,d) -> !delegate.test(a, b, c, d);
	}
}
