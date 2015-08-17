package org.whaka.util

import spock.lang.Specification

class UberMapsTest extends Specification {

	def "entry with key and value"() {
		when:
			def e = UberMaps.entry(key, val)
		then:
			e instanceof UberMaps.Entry
			e.key.is(key)
			e.val.is(val)
		where:
			key				|	val
			12				|	42
			"qwe"			|	"rty"
			null			|	null
	}

	def "cloning entry"() {
		given: "some default implementation of an entry"
			def entry = [1:10].entrySet()[0]
		when: "cloning factory method is called with some entry"
			def uberEntry = UberMaps.entry(entry)
		then: "new uber entry is returned with the same fields"
			uberEntry instanceof UberMaps.Entry
			uberEntry.key.is(entry.getKey())
			uberEntry.val.is(entry.getValue())
	}

	def "cloning entry returns the same instance"() {
		given: "an uber entry"
			def entry = new UberMaps.Entry(10, 20)
		when: "cloning factory method is called with an uber entry"
			def uberEntry = UberMaps.entry(entry)
		then: "the same instance is returned, for entry is immutable"
			uberEntry.is(entry)
	}

	def "Entry"() {
		given:
			def e = new UberMaps.Entry(key, val)

		expect:
			e.key.is(key)
			e.val.is(val)
		and:
			e.key().is(key)
			e.val().is(val)
		and:
			e.getKey().is(key)
			e.getValue().is(val)

		when:
			e.setValue(42)
		then:
			thrown(UnsupportedOperationException)

		where:
			key				|	val
			null			|	null
			null			|	12
			null			|	"qwe"
			12				|	null
			"qwe"			|	null
			12				|	42
			"qwe"			|	"rty"
			12				|	"qwe"
			"qwe"			|	12
	}
}
