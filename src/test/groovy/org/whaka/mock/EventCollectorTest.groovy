package org.whaka.mock

import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.whaka.mock.EventCollector
import spock.lang.Specification

import java.util.function.BiConsumer
import java.util.function.Predicate

/**
 * @author gdzabaev
 */
class EventCollectorTest extends Specification {

    private BiConsumer<Listener, Integer> method = {l,e -> l.event(e)}
    private Predicate<Integer> filter = Mock()
    private EventCollector.EventHandler<Integer> filterEH = Mock()
    private EventCollector<Listener, Integer> collector = new EventCollector<>(Listener.class, method, filter, filterEH)
    private Listener l = collector.getTarget()

    def "all filters are called" () {
        when:
            l.event(event)
            l.event(event * 2)
        then:
            1 * filter.test(event) >> true
            1 * filterEH.test(event) >> true
        and:
            1 * filter.test(event * 2) >> false
            1 * filterEH.test(event * 2) >> false
        where:
            event << 100
    }

    def "accepted is called" () {
        when:
            l.event(event)
            l.event(event * 2)
        then:
            1 * filter.test(event) >> true
            1 * filterEH.test(event) >> true
        and:
            1 * filterEH.accepted(event)

        and:
            1 * filter.test(event * 2) >> false
            1 * filterEH.test(event * 2) >> true
        and:
            0 * filterEH.accepted(_)

        where:
            event << 100
    }

    def "events are collected" (){
        when:
            l.event(event)
            l.event(event * 2)
            l.event(event * 3)
        then:
            1 * filter.test(event) >> true
            1 * filterEH.test(event) >> true
        and:
            collector.events.contains(event)

        and:
            1 * filter.test(event * 2) >> false
            1 * filterEH.test(event * 2) >> true
        and:
            !collector.events.contains(event * 2)

        and:
            1 * filter.test(event * 3) >> true
            1 * filterEH.test(event * 3) >> true
        and:
            collector.events.size() == 2
            collector.events.contains(event * 3)

        where:
            event << 100
    }

    private interface Listener {
        void event(Integer i);
    }
}
