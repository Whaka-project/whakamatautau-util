package org.whaka.util.function;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>Equal to {@link BiFunction} but with 3 arguments.
 */
public interface Function3<A,B,C,R> {

	R apply(A a, B b, C c);
	
	/**
	 * Convert this function to the {@link Function} were all arguments are represented
	 * as a single {@link Tuple3} instance.
	 */
	default Function<Tuple3<A, B, C>, R> toFunction() {
		return e -> apply(e._1, e._2, e._3);
	}
	
	/**
	 * Convert this function to the {@link BiFunction} were all arguments except the thirst one are represented
	 * as a single {@link Tuple2} instance.
	 */
	default BiFunction<A, Tuple2<B, C>, R> toBiFunction() {
		return (a,e) -> apply(a, e._1, e._2);
	}
	
	default <V> Function3<A, B, C, V> andThen(Function<? super R, ? extends V> then) {
		Objects.requireNonNull(then, "Chained function cannot be null!");
		return (a,b,c) -> then.apply(apply(a, b, c));
	}
}
