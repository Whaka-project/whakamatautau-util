package org.whaka.mock;

import static java.util.stream.Collectors.toList;
import static org.whaka.util.UberStreams.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.mockito.ArgumentCaptor;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.invocation.DescribedInvocation;
import org.whaka.util.reflection.UberClasses;

import com.google.common.collect.ImmutableList;

/**
 * Collect arguments from the called method if ALL filters are passed successfully.
 * May be used with listeners to collect events.
 *
 * @param <Target>  the target class
 * @param <Event>  the event type
 */
public class EventCollector<Target, Event> {

	private final Object lock = new Object();
	
	private final Target target;
	private final List<Event> events = new ArrayList<>();

	private EventCollector(Target target) {
		this.target = target;
	}

	@SafeVarargs
	public static <T, E> EventCollector<T, E> create(Class<T> targetClass,
			BiConsumer<T, E> method, Predicate<E>... filters){
		return create(targetClass, method, Arrays.asList(filters));
	}

	public static <T, E> EventCollector<T, E> create(Class<T> targetClass,
			BiConsumer<T, E> method, Collection<Predicate<E>> filters){
		return EventCollector.<T, E>createPartial(targetClass, UberClasses.cast(Object.class),
				(l, c) -> method.accept(l, c.capture()), filters);
	}
	
	@SafeVarargs
	public static <T, E> EventCollector<T, E> createPartial(Class<T> targetClass, Class<E> eventClass,
			BiConsumer<T, ArgumentCaptor<E>> method, Predicate<E> ... filters){
		return createPartial(targetClass, eventClass, method, Arrays.asList(filters));
	}
	
	public static <T, E> EventCollector<T, E> createPartial(Class<T> targetClass, Class<E> eventClass,
			BiConsumer<T, ArgumentCaptor<E>> method, Collection<Predicate<E>> filters){

		List<EventHandler<E>> eventHandlers = selectEventHandlers(filters);
		
		List<DescribedInvocation> invokes = new ArrayList<>();
		MockSettings settings = Mockito.withSettings()
				.invocationListeners(report -> invokes.add(report.getInvocation()));
		
		T target = Mockito.mock(targetClass, settings);
		EventCollector<T, E> collector = new EventCollector<>(target);

		ArgumentCaptor<E> captor = ArgumentCaptor.forClass(eventClass);
		method.accept(Mockito.doAnswer(invoke -> {
			E event = captor.getValue();
			synchronized (collector.lock) {
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
	
	private static <E> List<EventHandler<E>> selectEventHandlers(Collection<Predicate<E>> filters) {
		return filters.stream()
				.filter(EventHandler.class::isInstance)
				.map(EventHandler.class::cast)
				.collect(toList());
	}
	
	public Target getTarget() {
		return target;
	}

	public List<Event> getEvents() {
		synchronized (lock) {
			return ImmutableList.copyOf(events);
		}
	}

	public Event getLastEvent() {
		synchronized (lock) {
			return events.isEmpty() ? null : events.get(events.size() - 1);
		}
	}

	public interface EventHandler<Event> extends Predicate<Event> {

		/**
		 * Is called when ALL filters from the {@link EventCollector} are passed successfully
		 *
		 * @param event the processed event
		 */
		void eventCollected(Event event);
	}
}
