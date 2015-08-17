package org.whaka.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class UberPredicates {

	private UberPredicates() {
	}
	
	public static <T> Predicate<T> not(Predicate<T> predicate) {
		return t -> !predicate.test(t);
	}
	
	public static <T> Predicate<T> anyOf(Collection<Predicate<T>> predicates) {
		List<Predicate<T>> copy = new ArrayList<>(predicates);
		return t -> copy.stream().anyMatch(p -> p.test(t));
	}
	
	public static <T> Predicate<T> allOf(Collection<Predicate<T>> predicates) {
		List<Predicate<T>> copy = new ArrayList<>(predicates);
		return t -> copy.stream().allMatch(p -> p.test(t));
	}
	
	public static <T> Predicate<T> noneOf(Collection<Predicate<T>> predicates) {
		return not(anyOf(predicates));
	}
	
	public static <T> Predicate<T> distinct(Predicate<T> delegate) {
		return new DistinctPredicateProxy<T>(delegate);
	}
	
	public static class DistinctPredicateProxy<T> implements Predicate<T> {

		private final AtomicBoolean everCalled = new AtomicBoolean();
		private final BiPredicate<T, T> distinctionPredicate;
		private final Predicate<T> delegate;
		private T prevArgument;
		private boolean lastResult;

		public DistinctPredicateProxy(Predicate<T> delegate) {
			this(delegate, (a, b) -> a != b);
		}
		
		public DistinctPredicateProxy(Predicate<T> delegate, BiPredicate<T, T> distinctionPredicate) {
			this.distinctionPredicate = distinctionPredicate;
			this.delegate = delegate;
		}
		
		@Override
		public boolean test(T t) {
			if (!everCalled.getAndSet(true) || distinctionPredicate.test(prevArgument, t)) {
				prevArgument = t;
				lastResult = delegate.test(t);
			}
			return lastResult;
		}
	}
}
