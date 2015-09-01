package org.whaka.mock

import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import java.util.function.Predicate

import org.whaka.mock.EventCollector.EventHandler

import spock.lang.Specification

class EventHandlersTest extends Specification {

	def "callback"() {
		given:
			Consumer consumer = Mock()
		and:
			EventHandler handler = EventHandlers.collectCallback(consumer)

		when:
			def res = handler.test("qwe")
		then:
			0 * consumer.accept(_)
		and:
			res == true

		when:
			handler.eventCollected("some")
		then:
			1 * consumer.accept("some")
	}

	def "functional"() {
		given:
			Predicate predicate = Mock()
			Consumer consumer = Mock()
		and:
			EventHandler handler = EventHandlers.functional(predicate, consumer)

		when:
			def res = handler.test("qwe")
		then:
			1 * predicate.test("qwe") >> result
			0 * consumer.accept(_)
		and:
			res == result

		when:
			handler.eventCollected("some")
		then:
			1 * consumer.accept("some")
			0 * predicate.test(_)

		where:
			result << [true, false]
	}

	def "skip"() {
		given:
			Predicate pred = EventHandlers.skip(3)
		expect:
			pred.test("qwe") == false
			pred.test("qwe") == false
			pred.test("qwe") == false
			pred.test("qwe") == true
			pred.test("qwe") == true
	}

	def "manual"() {
		given:
			AtomicBoolean flag = new AtomicBoolean(value)
		and:
			Predicate pred = EventHandlers.manual(flag)

		expect:
			pred.test("qwe") == value

		when:
			flag.set(!value)
		then:
		pred.test("qwe") == !value

		where:
			value << [true, false]
	}
}
