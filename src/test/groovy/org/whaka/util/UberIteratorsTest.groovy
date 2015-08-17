package org.whaka.util

import java.util.function.Function
import java.util.function.Predicate

import spock.lang.Specification

class UberIteratorsTest extends Specification {

	def "iterate: seed, supplier, validator"() {
		given:
			Object seed = new Object()
			Function<Object, Object> supplier = Mock()
			Predicate<Object> validator = Mock()
			Iterable<Object> iterable = UberIterators.iterate(seed, supplier, validator)

			Iterator<Object> iterator = iterable.iterator()
			def next = null

		when: "#hasNext is called on a newly created iterator"
			next = iterator.hasNext()
		then: "specified validator function is called with the seed"
			1 * validator.test(seed) >> true
		and: "result of #hasNext is the result of teh validator"
			next == true

		when: "#next is called on a newly created iterator"
			next = iterator.next()
		then: "#hasNext calls validator only once for each value"
			0 * validator.test(seed)
		and: "specified supplier function is called with the seed"
			1 * supplier.apply(seed) >> 42
		and: "returned object is the seed itself"
			next.is(seed)

		when: "#next is called again"
			next = iterator.next()
		then: "validator and supplier are called with the value previously produced by the supplier"
			1 * validator.test(42) >> true
			1 * supplier.apply(42) >> null
		and: "new validated value is returned"
			next == 42

		when: "#next is called while #hasNext returns false"
			iterator.next()
			validator.test(null) >> false
		then:
			thrown(NoSuchElementException)

		when: "another iterator is created from the same iterable"
			Iterator<Object> iterator2 = iterable.iterator()
			iterator2.next()
		then: "iteration will start from the seed again"
			1 * validator.test(seed) >> true
		and:
			1 * supplier.apply(seed) >> "qwe"

		when: "hasNext is called"
			def hasNext = iterator2.hasNext()
		then: "and supplier returns any 'invalid' value"
			1 * validator.test("qwe") >> false
		and: "iteration will be finished"
			hasNext == false
	}
}
