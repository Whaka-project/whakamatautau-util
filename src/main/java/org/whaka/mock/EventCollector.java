package org.whaka.mock;

import static java.util.stream.Collectors.*;
import static org.whaka.util.UberStreams.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.mockito.ArgumentCaptor;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.invocation.DescribedInvocation;
import org.whaka.util.reflection.UberClasses;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * <p>Class allows you easily create a mock of the specified type that stubs a single specified method.
 * When stubbed method is called it's argument got collected into a list. User then can access all collected
 * values (see {@link #getEvents()}) or the last collected value (see {@link #getLastEvent()}).
 * 
 * <p>Mock instance is created from the specified class using {@link Mockito} API, so all restrictions are the same.
 * Method to be stubbed is specified using a {@link BiConsumer} which accepts an instance of mocked class and
 * matcher of the captured event type (note, this matcher will always be <code>null</code>). This consumer
 * should perform a single call upon the accepted mock, using matcher as an argument  - called method will be
 * stubbed. The most convenient way is to specify a "method link", for example: {@code ActionListener::actionPerformed}
 * 
 * <p>Created mock might be accessed by the {@link #getTarget()} method, and used as a regular instance. Also note
 * that only specified method got stubbed by the collector, but {@link Mockito} API might be used to perform
 * additional manual configuration upon created target instance.
 * 
 * <p>Additionally collector allows you to register a set of filters, used to decide which event will be collected.
 * Filters are specified as {@link Predicate} of the event type. When stubbed method is called - <b>ALL</b> specified
 * filters are called to test the captured event. After <b>ALL</b> filters got called - results are checked; if at least
 * one of the filters returned <code>false</code> - event is ignored, otherwise - it gets collected and processed
 * by handlers (see {@link EventHandler}).
 * 
 * @param <Target>  the target class to be mocked
 * @param <Event>  the event type to be collected
 * 
 * @see #create(Class, BiConsumer, Collection)
 * @see #createPartial(Class, Class, BiConsumer, Collection)
 * @see EventHandler
 */
public class EventCollector<Target, Event> {

	private final Target target;
	private final List<Event> events = new ArrayList<>();

	private EventCollector(Target target) {
		this.target = target;
	}

	/**
	 * 
	 */
	@SafeVarargs
	public static <T, E> EventCollector<T, E> create(Class<T> targetClass,
			BiConsumer<T, E> method, Predicate<? super E>... filters){
		return create(targetClass, method, Arrays.asList(filters));
	}

	/**
	 *
	 * @see #create(Class, BiConsumer, Predicate...)
	 * @see #createPartial(Class, Class, BiConsumer, Collection)
	 */
	public static <T, E> EventCollector<T, E> create(Class<T> targetClass,
			BiConsumer<T, E> method, Collection<Predicate<? super E>> filters){
		return EventCollector.<T, E>createPartial(targetClass, UberClasses.cast(Object.class),
				(l, c) -> method.accept(l, c.capture()), filters);
	}
	
	/**
	 * 
	 */
	@SafeVarargs
	public static <T, E> EventCollector<T, E> createPartial(Class<T> targetClass, Class<E> eventClass,
			BiConsumer<T, ArgumentCaptor<E>> method, Predicate<? super E> ... filters){
		return createPartial(targetClass, eventClass, method, Arrays.asList(filters));
	}
	
	/**
	 *
	 * @see #create(Class, BiConsumer, Collection)
	 * @see #createPartial(Class, Class, BiConsumer, Predicate...)
	 */
	public static <T, E> EventCollector<T, E> createPartial(Class<T> targetClass, Class<E> eventClass,
			BiConsumer<T, ArgumentCaptor<E>> method, Collection<Predicate<? super E>> filters){

		List<EventHandler<? super E>> eventHandlers = selectEventHandlers(filters);
		
		List<DescribedInvocation> invokes = new ArrayList<>();
		MockSettings settings = Mockito.withSettings()
				.invocationListeners(report -> invokes.add(report.getInvocation()));
		
		T target = Mockito.mock(targetClass, settings);
		EventCollector<T, E> collector = new EventCollector<>(target);

		ArgumentCaptor<E> captor = ArgumentCaptor.forClass(eventClass);
		method.accept(Mockito.doAnswer(invoke -> {
			E event = captor.getValue();
			synchronized (collector.events) {
				boolean filterFail = stream(filters).map(p -> p.test(event)).toSet().contains(false);
				if (!filterFail) {
					collector.events.add(event);
					eventHandlers.forEach(c -> c.eventCollected(event));
				}
			}
			return null;
		}).when(target), captor);
		
		if (invokes.size() != 1)
			throw new IllegalStateException("Single listener interaction was expected! But actual: " + invokes);
		
		return collector;
	}
	
	private static <E> List<EventHandler<? super E>> selectEventHandlers(Collection<Predicate<? super E>> filters) {
		return filters.stream()
				.filter(EventHandler.class::isInstance)
				.map(EventHandler.class::cast)
				.collect(toList());
	}
	
	/**
	 * Created and configured mock instance of the specified target type.
	 */
	public Target getTarget() {
		return target;
	}

	/**
	 * Number of events collected so far.
	 */
	public int size() {
		synchronized (events) {
			return events.size();
		}
	}
	
	/**
	 * <b>Immutable</b> collection of all the events collected so far.
	 */
	public List<Event> getEvents() {
		synchronized (events) {
			return ImmutableList.copyOf(events);
		}
	}

	/**
	 * This method either returns the last captured event, or throws an exception if there're no events.
	 * 
	 * @throws IllegalStateException if there're no events captured
	 */
	public Event getLastEvent() {
		synchronized (events) {
			Preconditions.checkState(!events.isEmpty(), "No events were captured!");
			return events.get(events.size() - 1);
		}
	}

	/**
	 * <p>This interface represents a kind of an event filter that also gets notified when event is collected.
	 * Instances of this handler should be specified to an {@link EventCollector} among other predicate filters.
	 * 
	 * @see #eventCollected(Object)
	 * @see #createCallback(Consumer)
	 */
	public interface EventHandler<Event> extends Predicate<Event> {

		@Override
		default boolean test(Event t) {
			return true;
		}
		
		/**
		 * <p>This method is called by an {@link EventCollector} when new captured event has passed <b>ALL</b> filters
		 * and already got collected. So if this instance for an event <code>X</code> returns <code>false</code>
		 * from the {@link #test(Object)} method - then {@link #eventCollected(Object)} definitely will NOT be called
		 * for the event <code>X</code>. But if this instance for an event <code>X</code> returns <code>true</code>
		 * from the {@link #test(Object)} method - then {@link #eventCollected(Object)} <b>might</b> be called; but
		 * only if all the other filters registered in the same collector will also return <code>true</code>.
		 */
		void eventCollected(Event event);
		
		/**
		 * Method allows you to create a "listener" kind of handler that always returns <code>true</code> on test
		 * and calls specified consumer on {@link #eventCollected(Object)}
		 */
		public static <E> EventHandler<E> createCallback(Consumer<E> consumer) {
			return consumer::accept;
		}
	}
}
