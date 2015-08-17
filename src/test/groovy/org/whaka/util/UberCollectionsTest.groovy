package org.whaka.util

import java.util.function.BiPredicate

import org.spockframework.runtime.ConditionNotSatisfiedError

import spock.lang.Specification

class UberCollectionsTest extends Specification {

	def "contains-any"() {
		expect:
			UberCollections.containsAny(collection, anyOf) == result
		where:
			collection						|	anyOf				|	result
			[]								|	[]					|	true
			[null]							|	[]					|	true
			[]								|	[null]				|	false
			[null, null]					|	[null]				|	true
			[null]							|	[null, null]		|	true
			[1,2,3]							|	[null, null]		|	false
			[1,2,3]							|	[3,4,5]				|	true
			[1,2,3]							|	[4,5,6]				|	false
			[1]								|	[5,4,3,2,1,0]		|	true
			[arr(1,2),arr(2,3),arr(3,4)]	|	[arr(2,3)]			|	true	// < default deep predicate
			[arr(1,2),arr(2,3),arr(3,4)]	|	[arr(3,2)]			|	false
	}

	def "contains-any with predicate"() {
		given:
			BiPredicate<?, Object> falsePredicate = Mock(BiPredicate)
			BiPredicate<?, Object> truePredicate = Mock(BiPredicate)
			BiPredicate<?, Object> shallowPredicate = Objects.&equals
			falsePredicate.test(_, _) >> false
			truePredicate.test(_, _) >> true
		expect:
			UberCollections.containsAny(collection, anyOf, falsePredicate) == (anyOf.size == 0)
			UberCollections.containsAny(collection, anyOf, truePredicate) == (collection.size > 0 || anyOf.size == 0)
			UberCollections.containsAny(collection, anyOf, shallowPredicate) == result
		where:
			collection						|	anyOf				|	result
			[]								|	[]					|	true
			[null]							|	[]					|	true
			[]								|	[null]				|	false
			[null, null]					|	[null]				|	true
			[null]							|	[null, null]		|	true
			[1,2,3]							|	[null, null]		|	false
			[1,2,3]							|	[3,4,5]				|	true
			[1,2,3]							|	[4,5,6]				|	false
			[1]								|	[5,4,3,2,1,0]		|	true
			[arr(1,2),arr(2,3),arr(3,4)]	|	[arr(2,3)]			|	false	// < shallow predicate is used
			[arr(1,2),arr(2,3),arr(3,4)]	|	[arr(3,2)]			|	false
	}

	def "contains-all"() {
		expect:
			UberCollections.containsAll(collection, allOf) == result
		where:
			collection						|	allOf				|	result
			[]								|	[]					|	true
			[null]							|	[]					|	true
			[]								|	[null]				|	false
			[null, null]					|	[null]				|	true
			[null, null]					|	[null, null]		|	true
			[null, null, null]				|	[null, null]		|	true
			[null]							|	[null, null]		|	false
			[1,2,3]							|	[null, null]		|	false
			[1,2,3]							|	[2,4]				|	false
			[1,2,3]							|	[2,3]				|	true
			[1,2,3]							|	[1,2,3]				|	true
			[1,2,3,4,5]						|	[1,5]				|	true
			[arr(1,2),arr(2,3)]				|	[arr(2,3)]			|	true	// < default deep predicate
			[arr(1,2),arr(2,3)]				|	[arr(3,2)]			|	false
			[1,2,3]							|	[1,2,2]				|	false
			[1,2,2,3]						|	[1,2,2]				|	true
	}

	def "contains-all with predicate"() {
		given:
			BiPredicate<?, Object> falsePredicate = Mock(BiPredicate)
			BiPredicate<?, Object> truePredicate = Mock(BiPredicate)
			BiPredicate<?, Object> shallowPredicate = Objects.&equals
			falsePredicate.test(_, _) >> false
			truePredicate.test(_, _) >> true
		expect:
			UberCollections.containsAll(collection, allOf, falsePredicate) == (allOf.size == 0)
			UberCollections.containsAll(collection, allOf, truePredicate) == (allOf.size <= collection.size)
			UberCollections.containsAll(collection, allOf, shallowPredicate) == result
		where:
			collection						|	allOf				|	result
			[]								|	[]					|	true
			[null]							|	[]					|	true
			[]								|	[null]				|	false
			[null, null]					|	[null]				|	true
			[null, null]					|	[null, null]		|	true
			[null, null, null]				|	[null, null]		|	true
			[null]							|	[null, null]		|	false
			[1,2,3]							|	[null, null]		|	false
			[1,2,3]							|	[2,4]				|	false
			[1,2,3]							|	[2,3]				|	true
			[1,2,3]							|	[1,2,3]				|	true
			[1,2,3,4,5]						|	[1,5]				|	true
			[arr(1,2),arr(2,3)]				|	[arr(2,3)]			|	false	// < shallow predicate
			[arr(1,2),arr(2,3),12]			|	[12]				|	true
			[1,2,3]							|	[1,2,2]				|	false
			[1,2,2,3]						|	[1,2,2]				|	true
	}

	def "equal"() {
		expect:
			UberCollections.containsEqualElements(collection1, collection2) == result
			UberCollections.containsEqualElements(collection2, collection1) == result
		where:
			collection1				|	collection2				|	result
			[]						|	[]						|	true
			[null]					|	[null]					|	true
			[null, null]			|	[null, null]			|	true
			[]						|	[null]					|	false
			[1,2,3]					|	[1,2,3]					|	true
			[1,2,3]					|	[3,2,1]					|	true
			[1,2,3]					|	[1,3,2]					|	true
			[1,null]				|	[null,1]				|	true
			[1,null,1]				|	[null,1]				|	false
			[arr(1,2),arr(2,3)]		|	[arr(2,3),arr(1,2)]		|	true	// < default deep predicate
			[arr(1,2),arr(2,3)]		|	[arr(3,2),arr(1,2)]		|	false
	}

	def "equal with predicate"() {
		given:
			BiPredicate<?, Object> falsePredicate = Mock(BiPredicate)
			BiPredicate<?, Object> truePredicate = Mock(BiPredicate)
			BiPredicate<?, Object> shallowPredicate = Objects.&equals
			falsePredicate.test(_, _) >> false
			truePredicate.test(_, _) >> true
		expect:
			UberCollections.containsEqualElements(collection1, collection2, falsePredicate) == (collection1.size == 0 && collection2.size == 0)
			UberCollections.containsEqualElements(collection2, collection1, falsePredicate) == (collection1.size == 0 && collection2.size == 0)
			UberCollections.containsEqualElements(collection1, collection2, truePredicate) == (collection1.size == collection2.size)
			UberCollections.containsEqualElements(collection2, collection1, truePredicate) == (collection1.size == collection2.size)
			UberCollections.containsEqualElements(collection1, collection2, shallowPredicate) == result
			UberCollections.containsEqualElements(collection2, collection1, shallowPredicate) == result
		where:
			collection1						|	collection2			|	result
			[]								|	[]					|	true
			[null]							|	[null]				|	true
			[null, null]					|	[null, null]		|	true
			[]								|	[null]				|	false
			[1,2,3]							|	[1,2,3]				|	true
			[1,2,3]							|	[3,2,1]				|	true
			[1,2,3]							|	[1,3,2]				|	true
			[1,null]						|	[null,1]			|	true
			[1,null,1]						|	[null,1]			|	false
			[arr(1,2),arr(2,3)]				|	[arr(2,3),arr(1,2)]	|	false	// < shallow predicate
			[arr(1,2),arr(2,3)]				|	[arr(3,2),arr(1,2)]	|	false
	}

	def "to-string"() {
		expect:
			UberCollections.toString(collection) == "${collection}"
		where:
			collection << [
				null,
				[],
				[42],
				[null, null],
				Arrays.asList(12,22,34),
				["empty", [15, null]],
				[[],[[],[]]],
				[[arr(1,2)]]
			]
	}

	def "to-array-recursive"() {
		given:
			List<Object> list = [
				arr(1,2),
				[
					arr(3,4),
					arr(5,6),
					[
						arr(7,8),
						arr(9,0)
					]
				]
			]

		when:
			checkNoCollections(list.toArray())
		then:
			thrown(ConditionNotSatisfiedError)

		when:
			Object[] arr = UberCollections.toArrayRecursive(list)
			checkNoCollections(arr)
		then:
			notThrown(ConditionNotSatisfiedError)
	}

	private void checkNoCollections(Object[] arr) {
		for (Object o : arr) {
			assert !(o instanceof Collection)
			if (o instanceof Object[])
				checkNoCollections(o as Object[])
		}
	}

	def "collection-size and predicates rules"() {
		given:
			BiPredicate<?, Object> falsePredicate = Mock(BiPredicate)
			falsePredicate.test(_, _) >> false
		and:
			BiPredicate<?, Object> truePredicate = Mock(BiPredicate)
			truePredicate.test(_, _) >> true

		expect: "contains-any AND contains-all are always true IF target collection is empty"
			UberCollections.containsAny([], [], truePredicate)
			UberCollections.containsAny([], [], falsePredicate)
			UberCollections.containsAny([1], [], truePredicate)
			UberCollections.containsAny([1], [], falsePredicate)
			UberCollections.containsAll([], [], truePredicate)
			UberCollections.containsAll([], [], falsePredicate)
			UberCollections.containsAll([1], [], truePredicate)
			UberCollections.containsAll([1], [], falsePredicate)

		and: "equal-elements is always true, IF source and target collections are both empty"
			UberCollections.containsEqualElements([], [], truePredicate)
			UberCollections.containsEqualElements([], [], falsePredicate)

		and: "contains-any with true-predicate will always be true WHILE source is not empty"
			UberCollections.containsAny([], [42], truePredicate) == false
			UberCollections.containsAny([1], [42], truePredicate)
			UberCollections.containsAny([1,2], [42], truePredicate)
			UberCollections.containsAny([1], [42,43], truePredicate)

		and: "contains-all with true-predicate will always be true WHILE source has >= elements than target"
			UberCollections.containsAll([], [42], truePredicate) == false
			UberCollections.containsAll([1], [42], truePredicate)
			UberCollections.containsAll([1,2], [42], truePredicate)
			UberCollections.containsAll([1], [42,43], truePredicate) == false

		and: "equal-elements with true-predicate will always be true WHILE source has as many elements as target"
			UberCollections.containsEqualElements([], [42], truePredicate) == false
			UberCollections.containsEqualElements([1], [42], truePredicate)
			UberCollections.containsEqualElements([1,2], [42], truePredicate) == false
			UberCollections.containsEqualElements([1], [42,43], truePredicate) == false

		and: "all three methods with false-predicate will always be FALSE IF target collection is not empty"
			UberCollections.containsAny([], [42], falsePredicate) == false
			UberCollections.containsAny([1], [42], falsePredicate) == false
			UberCollections.containsAny([1,2], [42], falsePredicate) == false
			UberCollections.containsAny([1], [42,43], falsePredicate) == false
			UberCollections.containsAll([], [42], falsePredicate) == false
			UberCollections.containsAll([1], [42], falsePredicate) == false
			UberCollections.containsAll([1,2], [42], falsePredicate) == false
			UberCollections.containsAll([1], [42,43], falsePredicate) == false
			UberCollections.containsEqualElements([], [42], falsePredicate) == false
			UberCollections.containsEqualElements([1], [42], falsePredicate) == false
			UberCollections.containsEqualElements([1,2], [42], falsePredicate) == false
			UberCollections.containsEqualElements([1], [42,43], falsePredicate) == false
	}

	def "deep-equals-predicate"() {
		given:
			BiPredicate<?, Object> pred = UberCollections.deepEqualsPredicate()

		expect:
			pred.test(A, B) == result
			pred.test(B, A) == result
			pred.test(A, A) == true
			pred.test(B, B) == true
		and:
			pred.test(null, null)

		where:
			A							|	B							||	result
			null						|	42							||	false
			""							|	0.5							||	false
			false						|	0							||	false
			true						|	true						||	true
			[12]						|	[]							||	false
			[]							|	[]							||	true
			[false]						|	[false]						||	true
			(arr(1,2))					|	(arr(2,1))					||	false
			(arr(1,2))					|	(arr(1,2))					||	true
			[arr(1,2)]					|	[arr(1,2)]					||	true
			[[arr(1,2)]]				|	[[arr(1,2)]]				||	true
			[[arr(1,2)] as Object[]]	|	[[arr(1,2)] as Object[]]	||	true
			[[arr(5,6)] as List]		|	[[arr(5,6)] as List]		||	true
			[[arr(5,6),arr(1,2)]]		|	[[arr(1,2),arr(5,6)]]		||	true	// equal-elements is performed for collections
			[[arr(3,4)] as Object[]]	|	[[arr(3,4)] as List]		||	false	// arrays are not equal to collections
	}

	private int[] arr(int[] ints) {
		return ints
	}
}
