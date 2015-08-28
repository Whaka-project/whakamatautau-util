package org.whaka.asserts

import java.util.function.BiPredicate

import org.hamcrest.Matcher

import spock.lang.Specification

class UberMatchersTest extends Specification {

	def "hasItem with predicate: basics"() {
		given:
			BiPredicate predicate = Mock()
			Matcher m = UberMatchers.hasItem("item", predicate)
			List<String> values = ["qwe", "rty", "qaz", "pop"]
		when:
			def res = m.matches(values)
		then:
			1 * predicate.test("qwe", "item") >> false
		and:
			1 * predicate.test("rty", "item") >> false
		and:
			1 * predicate.test("qaz", "item") >> true
		and:
			0 * predicate.test(_, _)
		and:
			res == true
	}

	def "hasItem with predicate: examples"() {
		given:
			Matcher m = UberMatchers.hasItem(item, predicate)
		expect:
			m.matches(values as List) == result
		where:
			values				|	item		|	predicate				||	result
			[]					|	3			|	Objects.&equals			||	false
			[]					|	3			|	{a,b -> true}			||	false
			[1,2]				|	3			|	Objects.&equals			||	false
			[1,2]				|	3			|	{a,b -> true}			||	true
			[1,2,3]				|	3			|	Objects.&equals			||	true
			[1,2,3]				|	3			|	{a,b -> false}			||	false
			[]					|	null		|	Objects.&equals			||	false
			[null]				|	null		|	Objects.&equals			||	true
			[null, null]		|	null		|	Objects.&equals			||	true
			[arr(1,2),arr(3,4)]	|	arr(1,2)	|	Objects.&equals			||	false
			[arr(1,2),arr(3,4)]	|	arr(1,2)	|	Objects.&deepEquals		||	true
	}

	def "hasItem with predicate: NPE"() {

		when: "item is null, but predicate is valid"
			UberMatchers.hasItem(null, Mock(BiPredicate))
		then: "nothing is thrown - null value is searched"
			notThrown()

		when: "predicate is null"
			UberMatchers.hasItem(42, null)
		then:
			thrown(NullPointerException)

		when: "properly created matcher is called upon null value as a collection"
			UberMatchers.hasItem(42, {a,b->true}).matches(null as Collection)
		then:
			thrown(NullPointerException)
	}

	def "isIn with predicate: basics"() {
		given:
			BiPredicate predicate = Mock()
			Matcher m = UberMatchers.isIn(["qwe", "rty", "qaz", "pop"], predicate)
		when:
			def res = m.matches("item")
		then:
			1 * predicate.test("qwe", "item") >> false
		and:
			1 * predicate.test("rty", "item") >> false
		and:
			1 * predicate.test("qaz", "item") >> true
		and:
			0 * predicate.test(_, _)
		and:
			res == true
	}

	def "isIn with predicate: examples"() {
		given:
			Matcher m = UberMatchers.isIn(values as List, predicate)
		expect:
			m.matches(item) == result
		where:
			values				|	item		|	predicate				||	result
			[]					|	3			|	Objects.&equals			||	false
			[]					|	3			|	{a,b -> true}			||	false
			[1,2]				|	3			|	Objects.&equals			||	false
			[1,2]				|	3			|	{a,b -> true}			||	true
			[1,2,3]				|	3			|	Objects.&equals			||	true
			[1,2,3]				|	3			|	{a,b -> false}			||	false
			[]					|	null		|	Objects.&equals			||	false
			[null]				|	null		|	Objects.&equals			||	true
			[null, null]		|	null		|	Objects.&equals			||	true
			[arr(1,2),arr(3,4)]	|	arr(1,2)	|	Objects.&equals			||	false
			[arr(1,2),arr(3,4)]	|	arr(1,2)	|	Objects.&deepEquals		||	true
	}

	def "isIn with predicate: NPE"() {

		when: "collection is null"
			UberMatchers.isIn(null, Mock(BiPredicate))
		then:
			thrown(NullPointerException)

		when: "predicate is null"
			UberMatchers.isIn([], null)
		then:
			thrown(NullPointerException)
	}

	private static int[] arr(int[] arr) {
		return arr
	}
}
