package org.whaka.mock;

import static org.hamcrest.Matchers.*;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.whaka.asserts.Assert;
import org.whaka.util.function.Tuple2;
import org.whaka.util.reflection.UberClasses;


@RunWith(JUnit4.class)
public class EventCombinerJavaTest {

	/**
	 * <p>{@link EventCombiner} only encapsulates a call to an abstract instance,
	 * with automatically created argument captors, as parameters. But it doesn't perform any additional mock
	 * configuration, and - in fact - doesn't know anything about any instance at all; so mock configuration got
	 * to be done manually.
	 * 
	 * <p>Method {@link EventCombiner#getValue()} calls {@link ArgumentCaptor#getValue()} on all generated
	 * captors and then apply combinator function to received results. So returned value is the result
	 * of the combinator specified at the construction of the combiner.
	 */
	@Test
	public void test_getValue() {

		// given:
		
		Function<Object[], String> combinator = Mockito.mock(UberClasses.cast(Function.class));
		Mockito.when(combinator.apply(Matchers.any())).then(invoke -> {
			Object[] arr = invoke.getArgumentAt(0, Object[].class);
			Assert.assertThat(arr, arrayWithSize(2));
			Assert.assertThat(arr[0], equalTo("qwe"));
			Assert.assertThat(arr[1], equalTo(42));
			return "combined result";
		});
		
		BiConsumer<Listener, ArgumentCaptor<?>[]> methodCall = (target, captors) ->
			target.event2((String)captors[0].capture(), (Integer)captors[1].capture());
			
		EventCombiner<Listener, String> combiner = EventCombiner.forCaptors(2, methodCall, combinator);

		Listener target = Mockito.mock(Listener.class);
		combiner.accept(Mockito.doNothing().when(target));
		
		// when:

		target.event2("qwe", 42);
		String res = combiner.getValue();
		
		// then:
		
		Mockito.verify(combinator, Mockito.times(1)).apply(Matchers.any());
		Assert.assertThat(res, equalTo("combined result"));
	}
	
	@Test
	public void test_basic() {
		
		// given:
		
		EventCombiner<Listener, Tuple2<String, Integer>> combiner = EventCombiner.create(Listener::event2);
		
		Listener target = Mockito.mock(Listener.class);
		combiner.accept(Mockito.doNothing().when(target));
		
		// when:
		
		target.event2("qwe", 42);
		Tuple2<String, Integer> res = combiner.getValue();
		
		// then:
		
		Assert.assertThat(res._1, equalTo("qwe"));
		Assert.assertThat(res._2, equalTo(42));
	}
	
	public static interface Listener {
		void event2(String s, Integer i);
	}
}
