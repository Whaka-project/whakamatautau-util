package org.whaka.mock;

import static org.whaka.util.UberStreams.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.invocation.InvocationOnMock;
import org.whaka.util.function.Consumer3;
import org.whaka.util.function.Consumer4;
import org.whaka.util.function.Consumer5;

import com.google.common.base.MoreObjects;
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
 * @see #create(Class, EventCombiner, Collection)
 * @see EventHandler
 * @see EventHandlers
 * @see EventCombiner
 */
public class EventCollector<Target, Event> {

	private final Target target;
	private final DescribedInvocation invocation;
	private final EventCombiner<Target, Event> combiner;
	private final List<Predicate<? super Event>> eventFilters;
	private final List<EventHandler<? super Event>> eventHandlers;
	private final List<Event> events = new ArrayList<>();

	private EventCollector(Class<Target> targetClass, EventCombiner<Target, Event> combiner,
			Collection<Predicate<? super Event>> filters) {
		
		this.combiner = Objects.requireNonNull(combiner, "Event combiner cannot be null!");
		this.eventFilters = ImmutableList.copyOf(filters);
		this.eventHandlers = selectEventHandlers(filters);
		
		List<DescribedInvocation> invocations = new ArrayList<>();
		MockSettings settings = Mockito.withSettings()
			.invocationListeners(report -> invocations.add(report.getInvocation()));
		
		this.target = Mockito.mock(targetClass, settings);
		combiner.accept(Mockito.doAnswer(this::answer).when(target));
		
		if (invocations.size() != 1)
			throw new IllegalStateException("Single listener interaction was expected! But actual: " + invocations);
		
		this.invocation = invocations.get(0);
	}
	
	private static <E> List<EventHandler<? super E>> selectEventHandlers(Collection<Predicate<? super E>> filters) {
		return filters.stream()
				.filter(EventHandler.class::isInstance)
				.map(p -> (EventHandler<? super E>) p)
				.collect(Collectors.toList());
	}
	
	/**
	 * Equal to the {@link #create(Class, BiConsumer, Collection)} but with vararg for predicate filters.
	 */
	@SafeVarargs
	public static <T, E> EventCollector<T, E> create(Class<T> targetClass,
			BiConsumer<T, E> method, Predicate<? super E>... filters){
		return create(targetClass, method, Arrays.asList(filters));
	}

	/**
	 * <p>Create an event collector that contains mock instance of the specified target class with a one method stubbed.
	 * Method to be stubbed is indicated by the specified predicate. All the predicates in the specified collection
	 * will be used to filter out events.
	 * 
	 * <p>Usability method. Equal to {@link #create(Class, EventCombiner, Collection)} with EventCombiner
	 * created using the {@link EventCombiner#create(BiConsumer)} method.
	 *
	 * @see #create(Class, BiConsumer, Predicate...)
	 * @see #create(Class, EventCombiner, Collection)
	 * @see EventHandler
	 */
	public static <T, E> EventCollector<T, E> create(Class<T> targetClass,
			BiConsumer<T, E> method, Collection<Predicate<? super E>> filters){
		return EventCollector.<T, E>create(targetClass, EventCombiner.create(method), filters);
	}
	
	/**
	 * Equal to the {@link #create(Class, EventCombiner, Collection)} but with vararg for predicate filters.
	 */
	@SafeVarargs
	public static <T, E> EventCollector<T, E> create(Class<T> targetClass,
			EventCombiner<T, E> captor, Predicate<? super E> ... filters){
		return create(targetClass, captor, Arrays.asList(filters));
	}
	
	/**
	 * <p>Create collector that will contain a mock of the specified target type,
	 * if will also stub the method represented by the specified {@link EventCombiner},
	 * and all the events will be filtered by specified predicates.
	 * 
	 * @see EventCombiner#create(Consumer3)
	 * @see EventCombiner#create(Consumer4)
	 * @see EventCombiner#create(Consumer5)
	 * @see EventCombiner#forCaptor(BiConsumer)
	 */
	public static <T, E> EventCollector<T, E> create(Class<T> targetClass,
			EventCombiner<T, E> captor, Collection<Predicate<? super E>> filters){
		return new EventCollector<>(targetClass, captor, filters);
	}
	
	/**
	 * Method is called when stubbed method is called upon target.
	 */
	private Object answer(InvocationOnMock invoke) {
		Event event = combiner.getValue();
		synchronized (events) {
			if (testEvent(event)) {
				events.add(event);
				eventHandlers.forEach(c -> c.eventCollected(event));
			}
		}
		return null;
	}
	
	/**
	 * Returns <code>true</code> if <b>ALL</b> registered event filters returned <code>true</code>
	 * for the specified event.
	 */
	private boolean testEvent(Event e) {
		return !stream(eventFilters).map(p -> p.test(e)).toSet().contains(false);
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
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("target", getTarget())
				.add("stub", invocation)
				.add("combiner", combiner)
				.add("filters", eventFilters.size())
				.add("handlers", eventHandlers.size())
				.toString();
	}

	/**
	 * <p>This interface represents a kind of an event filter that also gets notified when event is collected.
	 * Instances of this handler should be specified to an {@link EventCollector} among other predicate filters.
	 * 
	 * @see #eventCollected(Object)
	 */
	public interface EventHandler<Event> extends Predicate<Event> {

		/**
		 * <p>This method is called by an {@link EventCollector} when new captured event has passed <b>ALL</b> filters
		 * and already got collected. So if this instance for an event <code>X</code> returns <code>false</code>
		 * from the {@link #test(Object)} method - then {@link #eventCollected(Object)} definitely will NOT be called
		 * for the event <code>X</code>. But if this instance for an event <code>X</code> returns <code>true</code>
		 * from the {@link #test(Object)} method - then {@link #eventCollected(Object)} <b>might</b> be called; but
		 * only if all the other filters registered in the same collector will also return <code>true</code>.
		 */
		void eventCollected(Event event);
	}
}
