package org.whaka.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.whaka.mock.EventCollector.EventHandler;

public final class EventHandlers {

	private EventHandlers() {
	}
	
	public static LatchEventHandler latch(int count) {
		return new LatchEventHandler(new CountDownLatch(count));
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
	 */
	public static <E> EventHandler<E> chain(Collection<Predicate<? super E>> filters) {
		return new ChainEventHandler<>(filters);
	}
	
	/**
	 * Method allows you to create a "listener" kind of handler that always returns <code>true</code> on test
	 * and calls specified consumer on {@link EventHandler#eventCollected(Object)}
	 */
	public static <E> EventHandler<E> callback(Consumer<E> consumer) {
		return consumer::accept;
	}
}
