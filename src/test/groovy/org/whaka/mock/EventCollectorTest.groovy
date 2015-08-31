package org.whaka.mock

import java.util.function.BiConsumer
import java.util.function.Predicate

import org.mockito.ArgumentCaptor
import org.mockito.Matchers
import org.whaka.mock.EventCollector.EventHandler

import spock.lang.Specification

/**
 * @author gdzabaev
 */
class EventCollectorTest extends Specification {

    def "all filters are called" () {
        given:
            BiConsumer<Listener, Integer> method = {l,e -> l.event(e)}
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            def collector = EventCollector.create(Listener.class, method, filter, filterEH)
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

	def "partial: all filters are called" () {
		given:
			BiConsumer<Listener, ArgumentCaptor<String>> method = {l,c -> l.event2(Matchers.any(), c.capture())}
			Predicate<String> filter = Mock()
			EventHandler<String> filterEH = Mock()
			def collector = EventCollector.createPartial(Listener.class, String.class, method, filter, filterEH)
			Listener l = collector.getTarget()
		when:
			l.event2(100, "qwe")
			l.event2(200, "rty")
		then:
			1 * filter.test("qwe") >> true
			1 * filterEH.test("qwe") >> true
		and:
			1 * filter.test("rty") >> false
			1 * filterEH.test("rty") >> false
		and:
			0 * filter.test(_)
			0 * filterEH.test(_)
	}

    def "eventCollected is called" () {
        given:
            BiConsumer<Listener, Integer> method = {l,e -> l.event(e)}
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            def collector = EventCollector.create(Listener.class, method, filter, filterEH)
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

	def "partial: eventCollected is called" () {
		given:
			BiConsumer<Listener, ArgumentCaptor<String>> method = {l,c -> l.event2(Matchers.any(), c.capture())}
			Predicate<String> filter = Mock()
			EventHandler<String> filterEH = Mock()
			def collector = EventCollector.createPartial(Listener.class, String.class, method, filter, filterEH)
			Listener l = collector.getTarget()
		when:
			l.event2(100, "qwe")
			l.event2(200, "rty")
		then:
			2 * filter.test(_) >> true
			2 * filterEH.test(_) >> true
		and:
			1 * filterEH.eventCollected("qwe")
		and:
			1 * filterEH.eventCollected("rty")
		and:
			0 * filterEH.eventCollected(_)
	}

    def "eventCollected is not called" () {
        given:
            BiConsumer<Listener, Integer> method = {l,e -> l.event(e)}
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            def collector = EventCollector.create(Listener.class, method, filter, filterEH)
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

	def "partial: eventCollected is not called" () {
		given:
			BiConsumer<Listener, ArgumentCaptor<String>> method = {l,c -> l.event2(Matchers.any(), c.capture())}
			Predicate<String> filter = Mock()
			EventHandler<String> filterEH = Mock()
			def collector = EventCollector.createPartial(Listener.class, String.class, method, filter, filterEH)
			Listener l = collector.getTarget()
		when:
			l.event2(100, "qwe")
			l.event2(200, "rty")
		then:
			2 * filter.test(_) >> false
			2 * filterEH.test(_) >> true
		and:
			0 * filterEH.eventCollected(_)
	}

    def "events are collected" (){
        given:
            BiConsumer<Listener, Integer> method = {l,e -> l.event(e)}
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            def collector = EventCollector.create(Listener.class, method, filter, filterEH)
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
            collector.events.size() == 2
            collector.events.containsAll([100, 300])
    }

	def "partial: events are collected" (){
		given:
			BiConsumer<Listener, ArgumentCaptor<String>> method = {l,c -> l.event2(Matchers.any(), c.capture())}
			Predicate<String> filter = Mock()
			EventHandler<String> filterEH = Mock()
			def collector = EventCollector.createPartial(Listener.class, String.class, method, filter, filterEH)
			Listener l = collector.getTarget()
		when:
			l.event2(100, "qwe")
			l.event2(200, "rty")
			l.event2(300, "qaz")
		then:
			1 * filter.test("qwe") >> false
			2 * filter.test(_) >> true
			3 * filterEH.test(_) >> true
		and:
			collector.events.size() == 2
			collector.events.containsAll(["rty", "qaz"])
	}

    private interface Listener {
        void event(Integer i)
        void event2(Integer i, String s)
    }
}
