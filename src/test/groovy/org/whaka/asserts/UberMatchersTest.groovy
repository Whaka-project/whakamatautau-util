package org.whaka.asserts

import java.util.function.BiPredicate
import java.util.function.Function

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

	def "hasAnyItem with predicate: basics"() {
		given:
			BiPredicate predicate = Mock()
			Matcher m = UberMatchers.hasAnyItem(["it1", "it2"], predicate)
			List<String> values = ["qwe", "rty", "qaz"]
		when:
			def res = m.matches(values)
		then: 1 * predicate.test("qwe", "it1") >> false
		and:  1 * predicate.test("rty", "it1") >> false
		and:  1 * predicate.test("qaz", "it1") >> false
		and:  1 * predicate.test("qwe", "it2") >> false
		and:  1 * predicate.test("rty", "it2") >> true
		and:
			0 * predicate.test(_, _)
		and:
			res == true
	}

	def "hasAnyItem with predicate: examples"() {
		given:
			Matcher m = UberMatchers.hasAnyItem(items, predicate)
		expect:
			m.matches(values as List) == result
		where:
			values				|	items				|	predicate				||	result
			[]					|	[3,4]				|	Objects.&equals			||	false
			[]					|	[3,4]				|	{a,b -> true}			||	false
			[1,2]				|	[3,4]				|	Objects.&equals			||	false
			[1,2]				|	[3,4]				|	{a,b -> true}			||	true
			[1,2,3]				|	[3,4]				|	Objects.&equals			||	true
			[1,2,3]				|	[3,4]				|	{a,b -> false}			||	false
			[]					|	[null]				|	Objects.&equals			||	false
			[null]				|	[null]				|	Objects.&equals			||	true
			[null, null]		|	[1,null]			|	Objects.&equals			||	true
			[arr(1,2),arr(3,4)]	|	[arr(5,6),arr(1,2)]	|	Objects.&equals			||	false
			[arr(1,2),arr(3,4)]	|	[arr(5,6),arr(1,2)]	|	Objects.&deepEquals		||	true
	}

	def "hasAnyItem with predicate: errors"() {

		when: "items collection is null, but predicate is valid"
			UberMatchers.hasAnyItem(null, Mock(BiPredicate))
		then:
			thrown(NullPointerException)

		when: "items collection is empty"
			UberMatchers.hasAnyItem([], Mock(BiPredicate))
		then:
			thrown(IllegalArgumentException)

		when: "predicate is null"
			UberMatchers.hasItem([42], null)
		then:
			thrown(NullPointerException)

		when: "properly created matcher is called upon null value as a collection"
			UberMatchers.hasItem([42], {a,b->true}).matches(null as Collection)
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

	def "convert"() {
		given:
			Matcher<String> stringMatcher = Mock()
			Function<Integer, String> converter = Mock()
		and:
			Matcher<Integer> integerMatcher = UberMatchers.convert(stringMatcher, converter)
		when:
			def res = integerMatcher.matches(42)
		then:
			1 * converter.apply(42) >> "qwe"
		and:
			1 * stringMatcher.matches("qwe") >> result
		and:
			res == result
		where:
			result << [true, false]
	}

	def "converting: NPE"() {

		when: "delegate matcher is null"
			UberMatchers.convert(null, Mock(Function))
		then:
			thrown(NullPointerException)

		when: "function is null"
			UberMatchers.convert(Mock(Matcher), null)
		then:
			thrown(NullPointerException)
	}

	private static int[] arr(int[] arr) {
		return arr
	}
}
