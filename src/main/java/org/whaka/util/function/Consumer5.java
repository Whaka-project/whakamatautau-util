package org.whaka.util.function;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Equal to {@link BiConsumer}, but with 5 arguments.
 * 
 * @see #toConsumer(Consumer5)
 * @see #toBiConsumer(Consumer5)
 */
@FunctionalInterface
public interface Consumer5<A,B,C,D,E> {

	void accept(A a, B b, C c, D d, E e);
	
	/**
	 * Converts specified consumer into a {@link Consumer} were all arguments are represented
	 * as a single {@link Tuple5} instance.
	 */
	static <A,B,C,D,E> Consumer<Tuple5<A, B, C, D, E>> toConsumer(Consumer5<A, B, C, D, E> delegate) {
		return e -> delegate.accept(e._1, e._2, e._3, e._4, e._5);
	}
	
	/**
	 * Converts specified consumer into a {@link BiConsumer} were all arguments except the first one are represented
	 * as a single {@link Tuple4} instance.
	 */
	static <A,B,C,D,E> BiConsumer<A, Tuple4<B, C, D, E>> toBiConsumer(Consumer5<A, B, C, D, E> delegate) {
		return (a, e) -> delegate.accept(a, e._1, e._2, e._3, e._4);
	}
	
	default Consumer5<A, B, C, D, E> andThen(Consumer5<? super A, ? super B, ? super C, ? super D, ? super E> after) {
		Objects.requireNonNull(after, "Chained consumer cannot be null!");
		return (a, b, c, d, e) -> {
			accept(a, b, c, d, e);
			after.accept(a, b, c, d, e);
		};
	}
}
