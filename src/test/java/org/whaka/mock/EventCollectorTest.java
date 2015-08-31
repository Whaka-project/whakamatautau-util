package org.whaka.mock;

import static org.hamcrest.Matchers.*;
import static org.whaka.util.function.Tuple2.*;

import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.whaka.asserts.Assert;
import org.whaka.mock.EventCollector.EventHandler;
import org.whaka.util.function.Tuple2;
import org.whaka.util.reflection.UberClasses;

@RunWith(JUnit4.class)
public class EventCollectorTest {

	@Test
	public void all_filters_are_called() {

		// given:
		
		Predicate<Integer> filter = Mockito.mock(UberClasses.cast(Predicate.class));
		EventHandler<Integer> filterEH = Mockito.mock(UberClasses.cast(EventHandler.class));
		
		Mockito.when(filter.test(100)).thenReturn(true);
		Mockito.when(filterEH.test(100)).thenReturn(true);
		
		Mockito.when(filter.test(200)).thenReturn(false);
		Mockito.when(filterEH.test(200)).thenReturn(false);
		
		EventCollector<Listener, Integer> collector = createCollector(filter, filterEH);
		Listener target = collector.getTarget();
		
		// when:
		
		target.event(100);
		target.event(200);
		
		// then:
		
		Mockito.verify(filter, Mockito.times(2)).test(Matchers.any());
		Mockito.verify(filterEH, Mockito.times(2)).test(Matchers.any());
	}
	
	@Test
	public void eventCollected_is_called() {
		
		// given:
		
		Predicate<Integer> filter = Mockito.mock(UberClasses.cast(Predicate.class));
		EventHandler<Integer> filterEH = Mockito.mock(UberClasses.cast(EventHandler.class));
		
		Mockito.when(filter.test(Matchers.any())).thenReturn(true);
		Mockito.when(filterEH.test(Matchers.any())).thenReturn(true);
		
		EventCollector<Listener, Integer> collector = createCollector(filter, filterEH);
		Listener target = collector.getTarget();
		
		// when:
		
		target.event(100);
		target.event(200);
		
		// then:
		
		Mockito.verify(filter, Mockito.times(2)).test(Matchers.any());
		Mockito.verify(filterEH, Mockito.times(2)).test(Matchers.any());
		
		// and:
		
		InOrder order = Mockito.inOrder(filterEH);
		order.verify(filterEH, Mockito.times(1)).eventCollected(100);
		order.verify(filterEH, Mockito.times(1)).eventCollected(200);
	}
	
	@Test
	public void eventCollected_is_not_called() {
		
		// given:
		
		Predicate<Integer> filter = Mockito.mock(UberClasses.cast(Predicate.class));
		EventHandler<Integer> filterEH = Mockito.mock(UberClasses.cast(EventHandler.class));
		
		Mockito.when(filter.test(Matchers.any())).thenReturn(false);
		Mockito.when(filterEH.test(Matchers.any())).thenReturn(true);
		
		EventCollector<Listener, Integer> collector = createCollector(filter, filterEH);
		Listener target = collector.getTarget();
		
		// when:
		
		target.event(100);
		target.event(200);
		
		// then:
		
		Mockito.verify(filter, Mockito.times(2)).test(Matchers.any());
		Mockito.verify(filterEH, Mockito.times(2)).test(Matchers.any());
		
		// and:

		Mockito.verifyNoMoreInteractions(filterEH);
	}
	
	@Test
	public void events_are_collected() {
		
		// given:
		
		Predicate<Integer> filter = Mockito.mock(UberClasses.cast(Predicate.class));
		EventHandler<Integer> filterEH = Mockito.mock(UberClasses.cast(EventHandler.class));
		
		Mockito.when(filter.test(Matchers.any())).thenReturn(true);
		Mockito.when(filterEH.test(Matchers.any())).thenReturn(true);
		
		Mockito.when(filter.test(200)).thenReturn(false);
		
		EventCollector<Listener, Integer> collector = createCollector(filter, filterEH);
		Listener target = collector.getTarget();
		
		// when:
		
		target.event(100);
		target.event(200);
		target.event(300);
		
		// then:
		
		Mockito.verify(filter, Mockito.times(3)).test(Matchers.any());
		Mockito.verify(filterEH, Mockito.times(3)).test(Matchers.any());
		
		// and:
		
		List<Integer> events = collector.getEvents();
		Assert.assertThat(events, contains(100, 300));
		Assert.assertThat(collector.getLastEvent(), equalTo(300));
	}
	
	@Test(expected = IllegalStateException.class)
	public void no_last_event_exception() {
		
		// given:
		
		EventCollector<Listener, Integer> collector = createCollector();
		
		// when:
		
		collector.getLastEvent();
	}
	
	@Test
	public void eventCombiner_capture_selective() {
		
		// given:
		
		EventCombiner<Listener, String> combiner =
				EventCombiner.forCaptor((l,c) -> l.event2(Matchers.any(), c.capture()));
		
		Predicate<String> filter = Mockito.mock(UberClasses.cast(Predicate.class));
		
		Mockito.when(filter.test("qwe")).thenReturn(true);
		Mockito.when(filter.test("rty")).thenReturn(false);
				
		EventCollector<Listener, String> collector = EventCollector.create(Listener.class, combiner, filter);
		Listener target = collector.getTarget();
		
		// when
		
		target.event2(42, "qwe");
		target.event2(12, "rty");
		target.event(100);
		
		// then
		
		InOrder order = Mockito.inOrder(filter);
		order.verify(filter, Mockito.times(1)).test("qwe");
		order.verify(filter, Mockito.times(1)).test("rty");
		order.verifyNoMoreInteractions();
		
		// and:
		
		List<String> events = collector.getEvents();
		Assert.assertThat(events, contains("qwe"));
		Assert.assertThat(collector.getLastEvent(), equalTo("qwe"));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void eventCombiner_capture_two_arguments() {
		
		// given:
		
		EventCombiner<Listener, Tuple2<Integer, String>> combiner = EventCombiner.create(Listener::event2);
		
		Predicate<Tuple2<Integer, String>> filter = Mockito.mock(UberClasses.cast(Predicate.class));
		
		Mockito.when(filter.test(tuple2(42, "qwe"))).thenReturn(true);
		Mockito.when(filter.test(tuple2(12, "rty"))).thenReturn(false);
		
		EventCollector<Listener, Tuple2<Integer, String>> collector = EventCollector.create(Listener.class, combiner, filter);
		Listener target = collector.getTarget();
		
		// when
		
		target.event2(42, "qwe");
		target.event2(12, "rty");
		target.event(100);
		
		// then
		
		InOrder order = Mockito.inOrder(filter);
		order.verify(filter, Mockito.times(1)).test(tuple2(42, "qwe"));
		order.verify(filter, Mockito.times(1)).test(tuple2(12, "rty"));
		order.verifyNoMoreInteractions();
		
		// and:
		
		List<Tuple2<Integer, String>> events = collector.getEvents();
		Assert.assertThat(events, contains(tuple2(42, "qwe")));
		Assert.assertThat(collector.getLastEvent(), equalTo(tuple2(42, "qwe")));
	}
	
	@Test
	public void ChainHandler_eventCollected_is_called() {
		
		// given:
		
		Predicate<Integer> filter = Mockito.mock(UberClasses.cast(Predicate.class));
		EventHandler<Integer> handler = Mockito.mock(UberClasses.cast(EventHandler.class));
		
		Mockito.when(filter.test(Matchers.any())).thenReturn(true);
		Mockito.when(handler.test(Matchers.any())).thenReturn(true);
		
		EventCollector<Listener, Integer> collector =
				EventCollector.create(Listener.class, Listener::event, EventHandler.chain(filter, handler));
		
		Listener target = collector.getTarget();
		
		// when
		
		target.event(100);
		
		// then
		
		InOrder order = Mockito.inOrder(filter, handler);
		order.verify(filter, Mockito.times(1)).test(100);
		order.verify(handler, Mockito.times(1)).test(100);
		order.verify(handler, Mockito.times(1)).eventCollected(100);
		order.verifyNoMoreInteractions();
	}
	
	@Test
	public void ChainHandler_eventCollected_is_NOT_called() {
		
		// given:
		
		Predicate<Integer> filter = Mockito.mock(UberClasses.cast(Predicate.class));
		EventHandler<Integer> handler = Mockito.mock(UberClasses.cast(EventHandler.class));
		
		Mockito.when(filter.test(Matchers.any())).thenReturn(true);
		Mockito.when(handler.test(Matchers.any())).thenReturn(false);
		
		EventCollector<Listener, Integer> collector =
				EventCollector.create(Listener.class, Listener::event, EventHandler.chain(filter, handler));
		
		Listener target = collector.getTarget();
		
		// when
		
		target.event(100);
		
		// then
		
		InOrder order = Mockito.inOrder(filter, handler);
		order.verify(filter, Mockito.times(1)).test(100);
		order.verify(handler, Mockito.times(1)).test(100);
		order.verifyNoMoreInteractions();
	}
	
	@Test
	public void ChainHandler_is_NOT_called() {
		
		// given:
		
		Predicate<Integer> filter = Mockito.mock(UberClasses.cast(Predicate.class));
		EventHandler<Integer> handler = Mockito.mock(UberClasses.cast(EventHandler.class));
		
		Mockito.when(filter.test(Matchers.any())).thenReturn(false);
		Mockito.when(handler.test(Matchers.any())).thenReturn(true);
		
		EventCollector<Listener, Integer> collector =
				EventCollector.create(Listener.class, Listener::event, EventHandler.chain(filter, handler));
		
		Listener target = collector.getTarget();
		
		// when
		
		target.event(100);
		
		// then
		
		InOrder order = Mockito.inOrder(filter, handler);
		order.verify(filter, Mockito.times(1)).test(100);
		order.verifyNoMoreInteractions();
	}
	
	@SafeVarargs
	private static EventCollector<Listener, Integer> createCollector(Predicate<Integer> ... filters) {
		return EventCollector.create(Listener.class, Listener::event, filters);
	}

	public static interface Listener {
		void event(Integer i);
		void event2(Integer i, String s);
	}
}
