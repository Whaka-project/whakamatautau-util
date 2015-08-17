package org.whaka.util.function;

public interface DangerousConsumer<T, E extends Exception> {

	public void accept(T t) throws E;
}
