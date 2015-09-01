package org.whaka.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.whaka.mock.EventCollector.EventHandler;
import org.whaka.util.UberPredicates;

/**
 * Class provides a usability factory front for various implementations of {@link Predicate} and {@link EventHandlers}
 * interfaces used to configure event collection with {@link EventCollector}.
 */
public final class EventHandlers {

	private EventHandlers() {
	}

	/**
	 * Create and instance of the {@link LatchEventHandler} with a {@link CountDownLatch}
	 * initiated with the specified count.
	 */
	public static LatchEventHandler latch(int count) {
		return new LatchEventHandler(new CountDownLatch(count));
	}
	
	/**
	 * Create a predicate that will return {@code N} number of <code>false</code> results for any arguments.
	 * Then it will start to return <code>true</code> for any argument.
	 */
	public static <E> Predicate<E> skip(long n) {
		return UberPredicates.counter(n, false);
	}
	
	/**
	 * Create a predicate that will delegate call to the specified {@link AtomicBoolean} for any argument.
	 * Result predicate will return the same value as received from the {@link AtomicBoolean#get()} method.
	 */
	public static <E> Predicate<E> manual(AtomicBoolean flag) {
		return UberPredicates.fromSupplier(flag::get);
	}
	
	/**
	 * Equal to the {@link #chain(Collection)} but with vararg for filter predicates.
	 */
	@SafeVarargs
	public static <E> EventHandler<E> chain(Predicate<? super E> ... filters) {
		return chain(Arrays.asList(filters));
	}
	
	/**
	 * <p>Create an instance of the {@link EventHandler} that chain specified delegates so that
	 * next filter is called only if previous one had returned <code>true</code>.
	 * 
	 * <p>Handler like this allows you to build complex logic for event filtering;
	 * e.g. something like: "await 5 seconds" - <b>then</b> "skip 5 events". Since {@link EventCollector}
	 * calls <b>ALL</b> the registered filters on each event it isn't possible to implement something like this
	 * with basic functionality.
	 * 
	 * <p><b>Note:</b> since {@link EventHandler} is created, and not plain {@link Predicate} - you also can
	 * specify other handlers as delegates and their {@link EventHandler#eventCollected(Object)} method will be called
	 * when the same method of the created handler is called.
	 * 
	 * @throws NullPointerException if specified collection contains nulls
	 * @throws IllegalArgumentException if less than 2 filters are specified
	 */
	public static <E> EventHandler<E> chain(Collection<Predicate<? super E>> filters) {
		return new ChainEventHandler<>(filters);
	}
	
	/**
	 * <p>Method allows you to create a "listener" kind of handler that always returns <code>true</code> on test
	 * and calls specified consumer on {@link EventHandler#eventCollected(Object)}
	 * 
	 * <p>Equal to {@link #functional(Predicate, Consumer)} with a {@code (p -> true)} predicate.
	 */
	public static <E> EventHandler<E> collectCallback(Consumer<E> consumer) {
		return functional(p -> true, consumer);
	}
	
	/**
	 * <p>Create an instance of the {@link EventHandler} that will delegate it's functionality to the specified
	 * functionals. Each time {@link EventHandler#test(Object)} is called - it will call specified predicate;
	 * each time {@link EventHandler#eventCollected(Object)} is called it will call specified consumer.
	 * 
	 * <p>Might be useful to implement simple handlers where functionality is based upon some local values.
	 * 
	 * @see #collectCallback(Consumer)
	 */
	public static <E> EventHandler<E> functional(Predicate<? super E> predicate, Consumer<? super E> consumer) {
		return new EventHandler<E>() {
			@Override
			public boolean test(E t) { return predicate.test(t); }
			@Override
			public void eventCollected(E event) { consumer.accept(event); }
		};
	}
}
