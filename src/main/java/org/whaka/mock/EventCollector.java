package org.whaka.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.mockito.Matchers;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.invocation.DescribedInvocation;

import com.google.common.collect.ImmutableList;

import static org.whaka.util.UberStreams.stream;

/**
 * Collect arguments from the called method if ALL filters are passed successfully.
 * May be used with listeners to collect events.
 *
 * @param <Target>  the target class
 * @param <Event>  the event type
 */
public class EventCollector<Target, Event> {

	private final List<Event> events = new ArrayList<>();
	private final Target target;
	private final Object lock = new Object();

	@SuppressWarnings("unchecked")
	private EventCollector(Class<Target> targetClass, BiConsumer<Target, Event> targetMethod, Collection<Predicate<Event>> filters) {

		List<DescribedInvocation> invokes = new ArrayList<>();
		MockSettings settings = Mockito.withSettings()
				.invocationListeners(report -> invokes.add(report.getInvocation()));

		this.target = Mockito.mock(targetClass, settings);

		List<EventHandler<Event>> eventHandlers = filters.stream()
				.filter(EventHandler.class::isInstance)
				.map(p -> (EventHandler<Event>) p)
				.collect(Collectors.toList());

		targetMethod.accept(Mockito.doAnswer(invoke -> {
			synchronized (lock) {
				Event event = (Event) invoke.getArguments()[0];
				boolean filterFail = stream(filters).map(p -> p.test(event)).toSet().contains(false);
				if (!filterFail) {
					events.add(event);
					eventHandlers.forEach(c -> c.eventCollected(event));
				}

				return null;
			}
		}).when(this.target), Matchers.any());

		if (invokes.size() != 1)
			throw new IllegalStateException("Single listener interaction was expected! But actual: " + invokes);
	}

	@SafeVarargs
	public static <T, E> EventCollector<T, E> create(Class<T> target, BiConsumer<T, E> method, Predicate<E>... filters){
		return new EventCollector<>(target, method, Arrays.asList(filters));
	}

	public static <T, E> EventCollector<T, E> create(Class<T> target, BiConsumer<T, E> method, Collection<Predicate<E>> filters){
		return new EventCollector<>(target, method, filters);
	}

	public Target getTarget() {
		return target;
	}

	public synchronized List<Event> getEvents() {
		return ImmutableList.copyOf(events);
	}

	public synchronized Event getLastEvent() {
		return events.isEmpty() ? null : events.get(events.size() - 1);
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
