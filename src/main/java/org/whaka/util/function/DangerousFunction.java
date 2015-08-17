package org.whaka.util.function;

public interface DangerousFunction<T, R, E extends Exception> {

	public R apply(T t) throws E;
}
