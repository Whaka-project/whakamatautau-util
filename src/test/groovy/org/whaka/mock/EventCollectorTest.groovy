package org.whaka.mock

import java.util.function.BiConsumer
import java.util.function.Predicate

import org.whaka.mock.EventCollector.EventHandler

import spock.lang.Specification

/**
 * @author gdzabaev
 */
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
		BiConsumer<Listener, Integer> method = {l,e -> l.event(e)}
		return EventCollector.create(Listener.class, method, filters)
	}

    private interface Listener {
        void event(Integer i)
        void event2(Integer i, String s)
    }
}
