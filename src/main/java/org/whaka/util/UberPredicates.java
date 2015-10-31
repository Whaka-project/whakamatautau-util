package org.whaka.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;

public class UberPredicates {

	private UberPredicates() {
	}
	
	/**
	 * Create a predicate that will return specified {@code N} numbers of specified {@code value}.
	 * Then predicate will return {@code !value}
	 */
	public static <T> Predicate<T> counter(long n, boolean value) {
		Preconditions.checkArgument(n > 0, "Positive number is expected!");
		AtomicLong counter = new AtomicLong(n);
		return p -> counter.getAndUpdate(l -> l > 0 ? l - 1 : 0) > 0 ? value : !value;
	}
	
	/**
	 * Create a predicate that calls specified consumer and then returns specified fixed result.
	 * 
	 * @see #peekTrue(Consumer)
	 * @see #peekFalse(Consumer)
	 */
	public static <T> Predicate<T> peek(Consumer<? super T> consumer, boolean result) {
		return t -> { consumer.accept(t); return result; };
	}
	
	/**
	 * Equal to the {@link #peek(Consumer, boolean)} with fixed <code>true</code> result
	 * 
	 * @see #peekFalse(Consumer)
	 */
	public static <T> Predicate<T> peekTrue(Consumer<? super T> consumer) {
		return peek(consumer, true);
	}
	
	/**
	 * Equal to the {@link #peek(Consumer, boolean)} with fixed <code>false</code> result
	 * 
	 * @see #peekTrue(Consumer)
	 */
	public static <T> Predicate<T> peekFalse(Consumer<? super T> consumer) {
		return peek(consumer, false);
	}
	
	/**
	 * Create a predicate that calls specified boolean supplier on any argument
	 */
	public static <T> Predicate<T> fromSupplier(BooleanSupplier sup) {
		return t -> sup.getAsBoolean();
	}
	
	public static <T> Predicate<T> not(Predicate<T> predicate) {
		return t -> !predicate.test(t);
	}
	
	@SafeVarargs
	public static <T> Predicate<T> anyOf(Predicate<T>... predicates) {
		return anyOf(Arrays.asList(predicates));
	}
	
	public static <T> Predicate<T> anyOf(Collection<Predicate<T>> predicates) {
		List<Predicate<T>> copy = new ArrayList<>(predicates);
		return t -> copy.stream().anyMatch(p -> p.test(t));
	}
	
	@SafeVarargs
	public static <T> Predicate<T> allOf(Predicate<T>... predicates) {
		return allOf(Arrays.asList(predicates));
	}
	
	public static <T> Predicate<T> allOf(Collection<Predicate<T>> predicates) {
		List<Predicate<T>> copy = new ArrayList<>(predicates);
		return t -> copy.stream().allMatch(p -> p.test(t));
	}
	
	@SafeVarargs
	public static <T> Predicate<T> noneOf(Predicate<T>... predicates) {
		return noneOf(Arrays.asList(predicates));
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
