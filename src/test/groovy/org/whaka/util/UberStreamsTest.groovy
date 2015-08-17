package org.whaka.util

import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream

import spock.lang.Specification

class UberStreamsTest extends Specification {

	def "iterate: seed, supplier, validator"() {

		given:
			Object seed = new Object()
			Function<Object, Object> supplier = Mock()
			Predicate<Object> validator = Mock()
			Stream<Object> stream = UberStreams.iterate(seed, supplier, validator)
			def next = null

		when: "stream is checked for elements the first time"
			next = UberStreams.iterate(seed, supplier, validator).findFirst().orElse("qwe")
		then: "validator is called with the specified seed"
			1 * validator.test(seed) >> false
		and: "if validator returns false - then stream is closed"
			next == "qwe"

		when: "validator returns true for an element"
			next = UberStreams.iterate(seed, supplier, validator).findFirst().orElse("qwe")
		then: "supplier is called to produce next element of the stream"
			1 * validator.test(seed) >> true
			1 * supplier.apply(seed)
		and: "'approved' element become part of the stream"
			next.is(seed)

		when:
			next = UberStreams.iterate(seed, supplier, validator).collect(Collectors.toList())
		then:
			1 * validator.test(seed) >> true
			1 * supplier.apply(seed) >> 42
		and:
			1 * validator.test(42) >> true
			1 * supplier.apply(42) >> "sup!"
		and:
			1 * validator.test("sup!") >> true
			1 * supplier.apply("sup!") >> null
		and:
			1 * validator.test(null) >> false
			0 * supplier.apply(_)
		and:
			next == [seed, 42, "sup!"]
	}
}
