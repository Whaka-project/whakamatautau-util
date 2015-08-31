package org.whaka.util.function;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Equal to {@link BiConsumer}, but with 3 arguments.
 * 
 * @see #toConsumer(Consumer3)
 * @see #toBiConsumer(Consumer3)
 */
@FunctionalInterface
public interface Consumer3<A,B,C> {

	void accept(A a, B b, C c);

	/**
	 * Converts specified consumer into a {@link Consumer} were all arguments are represented
	 * as a single {@link Tuple3} instance.
	 */
	static <A,B,C> Consumer<Tuple3<A, B, C>> toConsumer(Consumer3<A, B, C> delegate) {
		return e -> delegate.accept(e._1, e._2, e._3);
	}
	
	/**
	 * Converts specified consumer into a {@link BiConsumer} were all arguments except the first one are represented
	 * as a single {@link Tuple2} instance.
	 */
	static <A,B,C> BiConsumer<A, Tuple2<B, C>> toBiConsumer(Consumer3<A, B, C> delegate) {
		return (a, e) -> delegate.accept(a, e._1, e._2);
	}
	
	default Consumer3<A, B, C> andThen(Consumer3<? super A, ? super B, ? super C> after) {
		Objects.requireNonNull(after, "Chained consumer cannot be null!");
		return (a, b, c) -> {
			accept(a, b, c);
			after.accept(a, b, c);
		};
	}
}
