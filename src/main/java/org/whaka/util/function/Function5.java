package org.whaka.util.function;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>Equal to {@link BiFunction} but with 5 arguments.
 */
public interface Function5<A,B,C,D,E,R> {

	R apply(A a, B b, C c, D d, E e);
	
	/**
	 * Convert this function to the {@link Function} were all arguments are represented
	 * as a single {@link Tuple5} instance.
	 */
	default Function<Tuple5<A, B, C, D, E>, R> toFunction() {
		return e -> apply(e._1, e._2, e._3, e._4, e._5);
	}
	
	/**
	 * Convert this function to the {@link BiFunction} were all arguments except the thirst one are represented
	 * as a single {@link Tuple4} instance.
	 */
	default BiFunction<A, Tuple4<B, C, D, E>, R> toBiFunction() {
		return (a,e) -> apply(a, e._1, e._2, e._3, e._4);
	}
	
	default <V> Function5<A, B, C, D, E, V> andThen(Function<? super R, ? extends V> then) {
		Objects.requireNonNull(then, "Chained function cannot be null!");
		return (a,b,c,d,e) -> then.apply(apply(a, b, c, d, e));
	}
}
