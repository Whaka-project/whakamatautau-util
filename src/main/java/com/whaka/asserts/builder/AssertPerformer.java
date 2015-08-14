package com.whaka.asserts.builder;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import com.whaka.asserts.AssertResult;

class AssertPerformer<T> {

	private final T actual;
	private final Consumer<AssertResult> consumer;
	
	public AssertPerformer(T actual, Consumer<AssertResult> consumer) {
		this.actual = actual;
		this.consumer = consumer;
	}
	
	public T getActual() {
		return actual;
	}
	
	protected Consumer<AssertResult> getConsumer() {
		return consumer;
	}
	
	protected <E> AssertResultConstructor performCheck(BiPredicate<T, E> predicate, E expected) {
		AssertResult result = null;
		if (!predicate.test(getActual(), expected))
			result = performResult(getActual(), expected);
		return AssertResultConstructor.create(result);
	}
	
	protected AssertResult performResult(Object actual, Object expected) {
		return performResult(new AssertResult(actual, expected, null));
	}
	
	protected AssertResult performResult(AssertResult result) {
		consumer.accept(result);
		return result;
	}
}
