package com.whaka.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class UberFunctions {

	private UberFunctions() {
	}
	
	public static <T,R> Function<T, R> distinct(Function<T, R> delegate) {
		return new DistinctFunctionProxy<T,R>(delegate);
	}
	
	public static class DistinctFunctionProxy<T, R> implements Function<T, R> {

		private final AtomicBoolean everCalled = new AtomicBoolean();
		private final BiPredicate<T, T> distinctionPredicate;
		private final Function<T, R> delegate;
		private T prevArgument;
		private R lastResult;

		public DistinctFunctionProxy(Function<T, R> delegate) {
			this(delegate, (a, b) -> a != b);
		}
		
		public DistinctFunctionProxy(Function<T, R> delegate, BiPredicate<T, T> distinctionPredicate) {
			this.distinctionPredicate = distinctionPredicate;
			this.delegate = delegate;
		}
		
		@Override
		public R apply(T t) {
			if (!everCalled.getAndSet(true) || distinctionPredicate.test(prevArgument, t)) {
				prevArgument = t;
				lastResult = delegate.apply(t);
			}
			return lastResult;
		}
	}
}
