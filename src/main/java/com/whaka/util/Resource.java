package com.whaka.util;

import java.util.Objects;

import com.whaka.util.function.DangerousConsumer;

/**
 * Class provides a wrapper that can be used in <b>try-with-resource</b> or in {@link Try} util in case original
 * resource doesn't implements {@link AutoCloseable}. This class provides functionality to wrap existing resource
 * and store operation for it's proper closing and then it will be delegated from {@link AutoCloseable#close()}.
 * 
 * @see #create(Object, DangerousConsumer)
 * @see #getValue()
 */
public class Resource<T, E extends Exception> implements AutoCloseable {

	private final T value;
	private final DangerousConsumer<T, E> close;

	private Resource(T value, DangerousConsumer<T, E> close) {
		this.value = Objects.requireNonNull(value, "Resource cannot be null!");
		this.close = Objects.requireNonNull(close, "Close operation cannot be null!");
	}
	
	/**
	 * Create new resource wrapper for specified actual resource and specified close operation for the resource.
	 */
	public static <T, E extends Exception> Resource<T, E> create(T value, DangerousConsumer<T, E> close) {
		return new Resource<T, E>(value, close);
	}
	
	/**
	 * Returns actual resource.
	 */
	public T getValue() {
		return value;
	}
	
	public DangerousConsumer<T, E> getClose() {
		return close;
	}
	
	@Override
	public void close() throws E {
		getClose().accept(getValue());
	}
}
