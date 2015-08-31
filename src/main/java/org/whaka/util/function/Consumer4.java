package org.whaka.util.function;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Equal to {@link BiConsumer}, but with 4 arguments.
 * 
 * @see #toConsumer(Consumer4)
 * @see #toBiConsumer(Consumer4)
 */
@FunctionalInterface
public interface Consumer4<A,B,C,D> {

	void accept(A a, B b, C c, D d);
	
	/**
	 * Converts specified consumer into a {@link Consumer} were all arguments are represented
	 * as a single {@link Tuple4} instance.
	 */
	static <A,B,C,D> Consumer<Tuple4<A, B, C, D>> toConsumer(Consumer4<A, B, C, D> delegate) {
		return e -> delegate.accept(e._1, e._2, e._3, e._4);
	}
	
	/**
	 * Converts specified consumer into a {@link BiConsumer} were all arguments except the first one are represented
	 * as a single {@link Tuple3} instance.
	 */
	static <A,B,C,D> BiConsumer<A, Tuple3<B, C, D>> toBiConsumer(Consumer4<A, B, C, D> delegate) {
		return (a, e) -> delegate.accept(a, e._1, e._2, e._3);
	}
	
	default Consumer4<A, B, C, D> andThen(Consumer4<? super A, ? super B, ? super C, ? super D> after) {
		Objects.requireNonNull(after, "Chained consumer cannot be null!");
		return (a, b, c, d) -> {
			accept(a, b, c, d);
			after.accept(a, b, c, d);
		};
	}
}
