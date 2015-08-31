package org.whaka.mock;

import static org.whaka.util.UberStreams.*;
import static org.whaka.util.function.Tuple2.*;
import static org.whaka.util.function.Tuple3.*;
import static org.whaka.util.function.Tuple4.*;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.whaka.util.function.Consumer3;
import org.whaka.util.function.Consumer4;
import org.whaka.util.function.Consumer5;
import org.whaka.util.function.Tuple2;
import org.whaka.util.function.Tuple3;
import org.whaka.util.function.Tuple4;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * @see #forValues(int, BiConsumer, Function)
 * @see #forCaptors(int, BiConsumer, Function)
 * @see #forCaptor(BiConsumer)
 * @see #create(BiConsumer)
 * @see #create(Consumer3)
 * @see #create(Consumer4)
 * @see #create(Consumer5)
 */
public class EventCombiner<Target, Event> implements Consumer<Target> {

	private final ArgumentCaptor<?>[] captors;
	private final BiConsumer<Target, ArgumentCaptor<?>[]> methodCall;
	private final Function<Object[], Event> combiner;
	
	private EventCombiner(int numberOfEvents,
			BiConsumer<Target, ArgumentCaptor<?>[]> methodCall,
			Function<Object[], Event> combiner) {
		Preconditions.checkArgument(numberOfEvents > 0, "Expected positive number of events!");
		this.methodCall = Objects.requireNonNull(methodCall, "Method call cannot be null!");
		this.combiner = Objects.requireNonNull(combiner, "Arguments combiner cannot be null!");
		this.captors = IntStream.range(0, numberOfEvents)
				.mapToObj(i -> ArgumentCaptor.forClass(Object.class))
				.toArray(ArgumentCaptor[]::new);
	}
	
	/**
	 * 
	 */
	public static <T,E> EventCombiner<T,E> forValues(int numberOfEvents,
			BiConsumer<T, Object[]> methodCall,
			Function<Object[], E> combiner) {
		Objects.requireNonNull(methodCall, "Method call cannot be null!");
		return forCaptors(numberOfEvents,
				(t, captors) -> methodCall.accept(t, stream(captors).map(c -> c.capture()).toArray()),
				combiner);
	}

	/**
	 * 
	 */
	public static <T,E> EventCombiner<T,E> forCaptors(int numberOfEvents,
			BiConsumer<T, ArgumentCaptor<?>[]> methodCall,
			Function<Object[], E> combiner) {
		return new EventCombiner<>(numberOfEvents, methodCall, combiner);
	}

	/**
	 * <p>Pure form of the event collector allows to stub only methods with one argument. But using a {@link BiPredicate}
	 * to indicate stubbed method allows you to call <i>any</i> method where matcher (passed into a predicate) might
	 * be passed as one of the multiple arguments. Example:
	 * <pre>
	 * 	interface Listener {
	 * 		void event(Integer i, String s);
	 * 	}
	 * 
	 * 	BiPredicate&lt;Listener, String&gt; methodCall =
	 * 		(l,s) -> l.event(Matchers.any(), s);
	 * 
	 * 	EventCollector&lt;Listener, String&gt; collector =
	 * 		EventCollector.create(Listener.class, methodCall);
	 * </pre>
	 * 
	 * <p>The problem with this example is that it won't work as expected. Specifics of the {@link Mockito} functionality
	 * require matchers to be created <b>in the same order</b> as matched arguments. And because the matcher specified
	 * in the predicate by an event collector was created <i>before</i> the one created manually - it still will
	 * try to match <b>the first</b> argument of the called method.
	 * 
	 * <p>But we definitely can put such a loophole to use and allow a "partial collect", but the API gets a bit less
	 * convenient. Since matchers are required to be created in the same order - user will have to initiate collector
	 * matcher manually. So {@link BiPredicate} accepted by this method takes an instance of the {@link ArgumentCaptor}
	 * as a second argument. User will have to call {@link ArgumentCaptor#capture()} on it to specify the argument
	 * to be collected. Example:
	 * <pre>
	 * 	interface Listener {
	 * 		void event(Integer i, String s);
	 * 	}
	 * 
	 * 	BiPredicate&lt;Listener, ArgumentCaptor&lt;String&gt;&gt; methodCall =
	 * 		(l,c) -> l.event(Matchers.any(), c.capture());
	 * 
	 * 	EventCollector&lt;Listener, String&gt; collector =
	 * 		EventCollector.createPartial(Listener.class, String.class, methodCall);
	 * </pre>
	 * 
	 * <p>In this example collector's matcher is initiated after the matcher created by the {@link Matchers#any()} call.
	 * Additional inconvenience is that class of the captured event is also have to be specified,
	 * since {@link BiPredicate} itself cannot properly guess it from call to a method with multiple arguments.
	 * But as a result it provides you an interesting functionality (relatively easy to implement) that allows you
	 * to capture one of the arguments, completely ignoring others (<code>Mockito's</code> {@link Matchers} got
	 * to be used manually to create matchers for other arguments).
	 */
	@SuppressWarnings("unchecked")
	public static <T,E> EventCombiner<T,E> forCaptor(BiConsumer<T, ArgumentCaptor<E>> methodCall) {
		return forCaptors(1,
				(t, captors) -> methodCall.accept(t, (ArgumentCaptor<E>) captors[0]),
				arr -> (E)arr[0]);
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> EventCombiner<T, E> create(BiConsumer<T, E> methodCall) {
		Objects.requireNonNull(methodCall, "Method call cannot be null!");
		return forValues(1,
				(t, arr) -> methodCall.accept(t, (E)arr[0]),
				arr -> (E)arr[0]);
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static <T, A, B> EventCombiner<T, Tuple2<A, B>> create(Consumer3<T, A, B> methodCall) {
		Objects.requireNonNull(methodCall, "Method call cannot be null!");
		return forValues(2,
				(t, arr) -> methodCall.accept(t, (A)arr[0], (B)arr[1]),
				arr -> tuple2((A)arr[0], (B)arr[1]));
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static <T, A, B, C> EventCombiner<T, Tuple3<A, B, C>> create(Consumer4<T, A, B, C> methodCall) {
		Objects.requireNonNull(methodCall, "Method call cannot be null!");
		return forValues(3,
				(t, arr) -> methodCall.accept(t, (A)arr[0], (B)arr[1], (C)arr[2]),
				arr -> tuple3((A)arr[0], (B)arr[1], (C)arr[2]));
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static <T, A, B, C, D> EventCombiner<T, Tuple4<A, B, C, D>> create(Consumer5<T, A, B, C, D> methodCall) {
		Objects.requireNonNull(methodCall, "Method call cannot be null!");
		return forValues(4,
				(t, arr) -> methodCall.accept(t, (A)arr[0], (B)arr[1], (C)arr[2], (D)arr[3]),
				arr -> tuple4((A)arr[0], (B)arr[1], (C)arr[2], (D)arr[3]));
	}
	
	public ArgumentCaptor<?>[] getCaptors() {
		return captors.clone();
	}
	
	public BiConsumer<Target, ArgumentCaptor<?>[]> getMethodCall() {
		return methodCall;
	}
	
	public Function<Object[], Event> getCombiner() {
		return combiner;
	}
	
	/**
	 * 
	 */
	@Override
	public void accept(Target t) {
		getMethodCall().accept(t, getCaptors());
	}

	/**
	 * 
	 */
	public Event getValue() {
		Object[] values = stream(getCaptors()).map(c -> c.getValue()).toArray();
		return getCombiner().apply(values);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("captors", getCaptors().length)
				.addValue(System.identityHashCode(this))
				.toString();
	}
}