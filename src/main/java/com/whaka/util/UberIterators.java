package com.whaka.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

public class UberIterators {
	
	private UberIterators() {
	}

	public static <T> Iterable<T> iterate(T seed, Function<T, T> next, Predicate<T> validator) {
		return () -> new FunctionIterator<>(seed, next, validator);
	}
	
	public static class FunctionIterator<T> implements Iterator<T> {
		
		private final AtomicReference<T> ref;
		private final Function<T, T> next;
		private final Predicate<T> validator;
		
		public FunctionIterator(T seed, Function<T, T> next, Predicate<T> validator) {
			this.ref = new AtomicReference<>(seed);
			this.next = next;
			this.validator = UberPredicates.distinct(validator);
		}
		
		@Override
		public boolean hasNext() {
			return validator.test(ref.get());
		}
		
		@Override
		public T next() {
			if (!hasNext())
				throw new NoSuchElementException();
			return ref.getAndUpdate(next::apply);
		}
	}
}
