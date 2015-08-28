package org.whaka.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.mockito.Matchers;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.invocation.DescribedInvocation;

/**
 * Collect arguments from the called method (may be used with listeners).
 *
 * @param <Target>  the target class
 * @param <Event>  the event type
 */
public class EventCollector<Target, Event> {

	private final List<Event> events = Collections.synchronizedList(new ArrayList<>());
	private final Target target;

	@SafeVarargs
	@SuppressWarnings("unchecked")
	public EventCollector(Class<Target> targetClass, BiConsumer<Target, Event> targetMethod, Predicate<Event>... filters) {
		this(targetClass, targetMethod, new HashSet<>(Arrays.asList(filters)));
	}

	@SuppressWarnings("unchecked")
	public EventCollector(Class<Target> targetClass, BiConsumer<Target, Event> targetMethod, Collection<Predicate<Event>> filters) {

		List<DescribedInvocation> invokes = new ArrayList<>();
		MockSettings settings = Mockito.withSettings()
				.invocationListeners(report -> invokes.add(report.getInvocation()));

		this.target = Mockito.mock(targetClass, settings);

		Set<EventHandler<Event>> eventHandlers = filters.stream()
				.filter(p -> p instanceof EventHandler)
				.map(p -> (EventHandler<Event>) p)
				.collect(Collectors.toSet());

		targetMethod.accept(Mockito.doAnswer(invoke -> {
			Event event = (Event) invoke.getArguments()[0];
			boolean filterFail = filters.stream().map(p -> p.test(event)).collect(Collectors.toSet()).contains(false);
			if(!filterFail) {
				events.add(event);
				eventHandlers.forEach(c -> c.accepted(event));
			}

			return null;
		}).when(this.target), Matchers.any());

		if (invokes.size() != 1)
			throw new IllegalStateException("Single listener interaction was expected! But actual: " + invokes);
	}

	public Target getTarget() {
		return target;
	}

	public List<Event> getEvents() {
		return events;
	}

	public Event getLastEvent() {
		return events.isEmpty() ? null : events.get(events.size() - 1);
	}


	public interface EventHandler<Event> extends Predicate<Event> {

		/**
		 * Is called when ALL filters from the {@link EventCollector} are passed successfully
		 *
		 * @param event the processed event
		 */
		void accepted(Event event);
	}
}
