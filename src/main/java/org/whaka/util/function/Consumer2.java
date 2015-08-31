package org.whaka.util.function;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Equal to {@link BiConsumer}.
 * 
 * @see #toConsumer(Consumer2)
 */
@FunctionalInterface
public interface Consumer2<A,B> extends BiConsumer<A, B> {

	@Override
	void accept(A a, B b);

	/**
	 * Converts specified consumer into a {@link Consumer} were all arguments are represented
	 * as a single {@link Tuple2} instance.
	 */
	static <A,B> Consumer<Tuple2<A, B>> toConsumer(Consumer2<A, B> delegate) {
		return e -> delegate.accept(e._1, e._2);
	}
}
