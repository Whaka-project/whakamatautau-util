package com.whaka.util.function;

public interface DangerousSupplier<T, E extends Exception> {

	public T get() throws E;
}
