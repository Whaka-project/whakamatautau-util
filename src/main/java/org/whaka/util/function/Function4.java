package org.whaka.util.function;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>Equal to {@link BiFunction} but with 4 arguments.
 */
public interface Function4<A,B,C,D,R> {

	R apply(A a, B b, C c, D d);
	
	/**
	 * Convert this function to the {@link Function} were all arguments are represented
	 * as a single {@link Tuple4} instance.
	 */
	default Function<Tuple4<A, B, C, D>, R> toFunction() {
		return e -> apply(e._1, e._2, e._3, e._4);
	}
	
	/**
	 * Convert this function to the {@link BiFunction} were all arguments except the thirst one are represented
	 * as a single {@link Tuple3} instance.
	 */
	default BiFunction<A, Tuple3<B, C, D>, R> toBiFunction() {
		return (a,e) -> apply(a, e._1, e._2, e._3);
	}
	
	default <V> Function4<A, B, C, D, V> andThen(Function<? super R, ? extends V> then) {
		Objects.requireNonNull(then, "Chained function cannot be null!");
		return (a,b,c,d) -> then.apply(apply(a, b, c, d));
	}
}
