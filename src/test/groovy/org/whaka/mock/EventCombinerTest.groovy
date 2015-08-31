package org.whaka.mock

import java.util.function.BiConsumer
import java.util.function.Function

import org.whaka.util.function.Consumer3
import org.whaka.util.function.Consumer4

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class EventCombinerTest extends Specification {

	def "for captors"() {
		given:
			BiConsumer methodCall = Mock()
			Function combinator = Mock()

		when: "#forCaptors is called with some method call, and a combinator"
			def combiner = EventCombiner.forCaptors(captors, methodCall, combinator)
		then: "method call and combinator function are stored"
			combiner.getMethodCall().is(methodCall)
			combiner.getCombiner().is(combinator)
		and: "specified number of argument captors are created"
			combiner.getCaptors().length == captors

		when: "accept is called with some target"
			combiner.accept("target")
		then: "specified method call is called with the target and created captors"
			1 * methodCall.accept("target", combiner.getCaptors())

		where:
			captors	<< [1, 3, 5]
	}

	def "for values"() {
		given:
			BiConsumer methodCall = Mock()
			Function combinator = Mock()

		when: "#forValues is called with some method call, and a combinator"
			def combiner = EventCombiner.forValues(captors, methodCall, combinator)
		then: "combinator function is stored"
			combiner.getCombiner().is(combinator)
		and: "method call is wrapped, so it's not the same"
			combiner.getMethodCall().is(methodCall) == false
		and: "specified number of argument captors is created"
			combiner.getCaptors().length == captors

		when: "accept is called with some target"
			combiner.accept("target")
		then: "method call is called with the target and initiated matchers"
			1 * methodCall.accept("target", _) >> {
				Object[] matchers = it[1]
				// the same amount of matchers is passed, as created captors
				assert matchers.size() == captors
				matchers.each {
					// each matcher is null
					assert it == null
				}
			}

		where:
			captors	<< [1, 3, 5]
	}

	def "for captor"() {
		given:
			BiConsumer methodCall = Mock()

		when: "#forCaptor is called with some method call"
			def combiner = EventCombiner.forCaptor(methodCall)
		then: "method call is wrapped, so it's not the same"
			combiner.getMethodCall().is(methodCall) == false
		and: "single argument captor is created"
			combiner.getCaptors().length == 1

		when: "accept is called with some target"
			combiner.accept("target")
		then: "specified method call is called with the target and single created captor"
			1 * methodCall.accept("target", combiner.getCaptors()[0])
	}

	def "create: BiConsumer"() {
		given:
			BiConsumer methodCall = Mock()

		when:
			def combiner = EventCombiner.create(methodCall)
		then:
			combiner.getCaptors().length == 1

		when:
			combiner.accept("target")
		then:
			1 * methodCall.accept("target", null)
	}

	def "create: Consumer3"() {
		given:
			Consumer3 methodCall = Mock()

		when:
			def combiner = EventCombiner.create(methodCall)
		then:
			combiner.getCaptors().length == 2

		when:
			combiner.accept("target")
		then:
			1 * methodCall.accept("target", null, null)
	}

	def "create: Consumer4"() {
		given:
			Consumer4 methodCall = Mock()

		when:
			def combiner = EventCombiner.create(methodCall)
		then:
			combiner.getCaptors().length == 3

		when:
			combiner.accept("target")
		then:
			1 * methodCall.accept("target", null, null, null)
	}
}
