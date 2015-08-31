package org.whaka.mock;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.StreamHandler;
import java.util.stream.Collectors;

import org.whaka.mock.EventCollector.EventHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * <p>Implementation of the {@link StreamHandler} that chains registered delegates so that next filter is called
 * only if previous one returned <code>true</code>.
 * 
 * <p>Since this class implements {@link EventHandler} - registered handlers will receive call
 * to the {@link #eventCollected(Object)} method when this handler gets the call.
 */
public class ChainEventHandler<Event> implements EventHandler<Event> {

	private final List<Predicate<? super Event>> eventFilters;
	private final List<EventHandler<? super Event>> eventHandlers;
	
	public ChainEventHandler(Collection<Predicate<? super Event>> filters) {
		Preconditions.checkArgument(!filters.contains(null), "Event predicate cannot be null!");
		this.eventFilters = ImmutableList.copyOf(filters);
		this.eventHandlers = filters.stream()
				.filter(EventHandler.class::isInstance)
				.map(p -> (EventHandler<? super Event>) p)
				.collect(Collectors.toList());
	}
	
	@Override
	public boolean test(Event t) {
		return eventFilters.stream().allMatch(p -> p.test(t));
	}

	@Override
	public void eventCollected(Event event) {
		eventHandlers.forEach(h -> h.eventCollected(event));
	}
}
