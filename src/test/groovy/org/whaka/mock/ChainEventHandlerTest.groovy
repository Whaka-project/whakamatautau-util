package org.whaka.mock

import java.util.function.Predicate

import org.whaka.mock.EventCollector.EventHandler

import spock.lang.Specification

class ChainEventHandlerTest extends Specification {

	def "construction"() {
		given:
			Predicate filter = Mock()
			EventHandler filterHandler = Mock()
		when:
			def handler = new ChainEventHandler([filter, filterHandler])
		then:
			handler.eventFilters == [filter, filterHandler]
			handler.eventHandlers == [filterHandler]
	}

	def "static construction"() {
		given:
			Predicate filter = Mock()
			EventHandler filterHandler = Mock()
		when:
			def handler = EventHandlers.chain(filter, filterHandler)
		then:
			handler.eventFilters == [filter, filterHandler]
			handler.eventHandlers == [filterHandler]
	}

	def "stream filters call"() {
		given:
			Predicate f1 = Mock()
			Predicate f2 = Mock()
			Predicate f3 = Mock()
		and:
			def handler = new ChainEventHandler<>([f1, f2, f3])

		when:
			def res = handler.test("qwe")
		then:
			1 * f1.test("qwe") >> true
		and:
			1 * f2.test("qwe") >> false
		and:
			0 * f3.test(_)
		and:
			res == false

		when:
			def res2 = handler.test("qwe")
		then:
			1 * f1.test("qwe") >> true
		and:
			1 * f2.test("qwe") >> true
		and:
			1 * f3.test("qwe") >> true
		and:
			res2 == true
	}

	def "eventCollected is delegated"() {
		given:
			Predicate f1 = Mock()
			Predicate f2 = Mock()
			EventHandler h = Mock()
		and:
			def handler = EventHandlers.chain(f1, f2, h)

		when:
			handler.eventCollected("qwe")
		then:
			1 * h.eventCollected("qwe")
	}

	def "null filter - NPE"() {
		when:
			new ChainEventHandler([null])
		then:
			thrown(NullPointerException)
	}

	def "less than 2 filters - IAE"() {
		when:
			new ChainEventHandler([])
		then:
			thrown(IllegalArgumentException)

		when:
			new ChainEventHandler([Mock(Predicate)])
		then:
			thrown(IllegalArgumentException)

		when:
			new ChainEventHandler([Mock(Predicate), Mock(Predicate)])
		then:
			notThrown(IllegalArgumentException)
	}

	public static interface Listener {
		void event(String s)
	}
}
