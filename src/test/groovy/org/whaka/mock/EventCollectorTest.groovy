package org.whaka.mock

import spock.lang.Specification

import java.util.function.BiConsumer
import java.util.function.Predicate

/**
 * @author gdzabaev
 */
class EventCollectorTest extends Specification {

    def "all filters are called" () {
        given:
            BiConsumer<Listener, Integer> method = {l,e -> l.event(e)}
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            EventCollector<Listener, Integer> collector = EventCollector.create(Listener.class, method, filter, filterEH)
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
    }

    def "eventCollected is called" () {
        given:
            BiConsumer<Listener, Integer> method = {l,e -> l.event(e)}
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            EventCollector<Listener, Integer> collector = EventCollector.create(Listener.class, method, filter, filterEH)
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
            BiConsumer<Listener, Integer> method = {l,e -> l.event(e)}
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            EventCollector<Listener, Integer> collector = EventCollector.create(Listener.class, method, filter, filterEH)
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
            BiConsumer<Listener, Integer> method = {l,e -> l.event(e)}
            Predicate<Integer> filter = Mock()
            EventCollector.EventHandler<Integer> filterEH = Mock()
            EventCollector<Listener, Integer> collector = EventCollector.create(Listener.class, method, filter, filterEH)
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

    private interface Listener {
        void event(Integer i);
    }
}
