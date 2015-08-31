package org.whaka.mock

import java.util.function.BiConsumer
import java.util.function.Predicate

import org.mockito.ArgumentCaptor
import org.mockito.Matchers
import org.whaka.mock.EventCombinerJavaTest.Listener
import org.whaka.util.function.Consumer3
import org.whaka.util.function.Tuple2

import spock.lang.Specification

class EventCollectorTest extends Specification {

    def "all filters are called" () {
        given:
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            def collector = createCollector(filter, filterEH)
            Listener l = collector.getTarget()
        when:
            l.event(100)
            l.event(200)
        then:
            1 * filter.test(100) >> true
            1 * filterEH.test(100) >> true
        and:
            1 * filter.test(200) >> false
            1 * filterEH.test(200) >> false
		and:
			0 * filter.test(_)
			0 * filterEH.test(_)
    }

    def "eventCollected is called" () {
        given:
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            def collector = createCollector(filter, filterEH)
            Listener l = collector.getTarget()
        when:
            l.event(100)
            l.event(200)
        then:
            2 * filter.test(_) >> true
            2 * filterEH.test(_) >> true
        and:
            1 * filterEH.eventCollected(100)
            1 * filterEH.eventCollected(200)
    }

    def "eventCollected is not called" () {
        given:
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            def collector = createCollector(filter, filterEH)
            Listener l = collector.getTarget()
        when:
            l.event(100)
            l.event(200)
        then:
            2 * filter.test(_) >> false
            2 * filterEH.test(_) >> true
        and:
            0 * filterEH.eventCollected(_)
    }

    def "events are collected" (){
        given:
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            def collector = createCollector(filter, filterEH)
            Listener l = collector.getTarget()
        when:
            l.event(100)
            l.event(200)
            l.event(300)
        then:
            1 * filter.test(200) >> false
            2 * filter.test(_) >> true
            3 * filterEH.test(_) >> true
        and:
            collector.size() == 2
            collector.getEvents().containsAll([100, 300])
			collector.getLastEvent() == 300
    }

	def "last event: exception"() {
		given:
			EventCollector<Listener, ?> collector = createCollector()
		when:
			collector.getLastEvent()
		then:
			thrown(IllegalStateException)
	}

	@SuppressWarnings("rawtypes")
	private static EventCollector<Listener, Integer> createCollector(Predicate<Integer>[] filters) {
		BiConsumer<Listener, Integer> method = {Listener l, e -> l.event(e)}
		return EventCollector.create(Listener.class, method, filters)
	}

	def "EventCombiner: capture selective"() {
		given:
			EventCombiner<Listener, String> combiner =
				EventCombiner.forCaptor({Listener l, ArgumentCaptor<String> c -> l.event2(Matchers.any(), c.capture())})
		and:
			Predicate<String> filter = Mock()
			EventCollector<Listener, String> collector = EventCollector.create(Listener.class, combiner, filter)

		when:
			collector.getTarget().event2(42, "qwe")
		then:
			1 * filter.test("qwe") >> true

		when:
			collector.getTarget().event2(12, "rty")
		then:
			1 * filter.test("rty") >> false

		when:
			collector.getTarget().event(100)
		then:
			0 * filter.test(_)

		expect:
			collector.getEvents() == ["qwe"]
	}

	def "EventCombiner: capture two arguments"() {
		given:
			Consumer3<Listener, Integer, String> methodCall = {Listener l, i, s -> l.event2(i,s)}
			EventCombiner<Listener, Tuple2<Integer, String>> combiner = EventCombiner.create(methodCall)
		and:
			Predicate<Tuple2<Integer, String>> filter = Mock()
			EventCollector<Listener, Tuple2<Integer, String>> collector = EventCollector.create(Listener.class, combiner, filter)

		when:
			collector.getTarget().event2(42, "qwe")
		then:
			1 * filter.test(Tuple2.tuple2(42, "qwe")) >> true

		when:
			collector.getTarget().event2(12, "rty")
		then:
			1 * filter.test(Tuple2.tuple2(12, "rty")) >> false

		when:
			collector.getTarget().event(100)
		then:
			0 * filter.test(_)

		expect:
			collector.getEvents() == [Tuple2.tuple2(42, "qwe")]
	}
}
