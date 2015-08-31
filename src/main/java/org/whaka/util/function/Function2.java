package org.whaka.util.function;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>Equal to {@link BiFunction}.
 * 
 * @see #toFunction(Function2)
 */
@FunctionalInterface
public interface Function2<A,B,R> extends BiFunction<A, B, R> {

	@Override
	R apply(A a, B b);
	
	/**
	 * Convert specified function to the {@link Function} were all arguments are represented
	 * as a single {@link Tuple2} instance.
	 */
	static <A,B,R> Function<Tuple2<A, B>, R> toFunction(Function2<A, B, R> delegate) {
		return e -> delegate.apply(e._1, e._2);
	}
}
