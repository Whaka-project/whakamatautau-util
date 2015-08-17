package org.whaka.asserts.builder

import java.util.function.Consumer

import spock.lang.Specification

import org.whaka.TestData
import org.whaka.asserts.AssertResult
import org.whaka.asserts.ComparisonAssertResult
import org.whaka.util.reflection.comparison.ComparisonFail
import org.whaka.util.reflection.comparison.ComparisonPerformer
import org.whaka.util.reflection.comparison.ComparisonResult

class ObjectAssertPerformerTest extends Specification {

	def "construction"() {
		given:
			Consumer<AssertResult> consumer = Mock()
		when:
			ObjectAssertPerformer performer = new ObjectAssertPerformer(actual, consumer)
		then:
			0 * consumer.accept(_)
			performer.actual == actual
		where:
			actual << TestData.variousObjects()
	}

	def "is-equal success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer performer = new ObjectAssertPerformer(actual, consumer)
		when: "isEqual is called successfully"
			AssertResultConstructor messageConstructor = performer.isEqual(actual)
		then: "consumer doesn't get called"
			0 * consumer.accept(_)
		and: "performer returns message constructor with no result"
			messageConstructor.getAssertResult() == null
		where:
			actual << TestData.variousObjects()
	}

	def "is-equal fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer performer = new ObjectAssertPerformer(actual, consumer)
		when: "isEqual call 'fails' (passed argument is not equal to stored value)"
			AssertResultConstructor messageConstructor = performer.isEqual(expected)
		then: "consumer receives new constructed assert result with the same fields"
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == actual
			capturedResult.getExpected() == expected
			capturedResult.getMessage() == ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS
			capturedResult.getCause() == null
		and: "performer returns new message constructor with the same result"
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual << TestData.variousObjects()
			expected << TestData.variousObjects().reverse()
	}

	def "is-NOT-equal success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNotEqual(expected)
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			actual << TestData.variousObjects()
			expected << TestData.variousObjects().reverse()
	}

	def "is-NOT-equal fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNotEqual(actual)
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == actual
			capturedResult.getExpected() == "Not '${actual}'"
			capturedResult.getMessage() == ObjectAssertPerformer.MESSAGE_NOT_EQUAL_OBJECTS
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual << TestData.variousObjects()
	}

	def "is-equal with comparison performer - success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(actual, consumer)
		and:
			ComparisonPerformer<?> comparisonPerformer = Mock()
			ComparisonResult comparisonResult = new ComparisonResult(actual, expected, comparisonPerformer, true)

		when: "#isEqual is called with additional parameter - comparison performer"
			AssertResultConstructor messageConstructor = performer.isEqual(expected, comparisonPerformer)
		then: "specified comparison performer is executed once with actual and expected objects"
			1 * comparisonPerformer.compare(actual, expected) >> comparisonResult
		and: "consumer is not called, for performer returned successful result"
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			actual << TestData.variousObjects()
			expected << TestData.variousObjects().reverse()
	}

	def "is-equal with comparison performer - fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(actual, consumer)
		and:
			ComparisonPerformer<?> comparisonPerformer = Mock()
			ComparisonResult comparisonResult = new ComparisonResult(actual, expected, comparisonPerformer, false)

		when: "#isEqual is called with comparison performer"
			AssertResultConstructor messageConstructor = performer.isEqual(expected, comparisonPerformer)
		then: "performer is executed once"
			1 * comparisonPerformer.compare(actual, expected) >> comparisonResult
		and: "if performer returns failed result - consumer receives assert result"
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and: "result contains actual and expected objects, default message, and no cause"
			capturedResult.getActual().is(actual)
			capturedResult.getExpected().is(expected)
			capturedResult.getMessage() == ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS
			capturedResult.getCause() == null
		and: "result contains ComparisonResult returned by the comparison performer"
			capturedResult instanceof ComparisonAssertResult
			capturedResult.getComparisonResult().is(comparisonResult)
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual << TestData.variousObjects()
			expected << TestData.variousObjects().reverse()
	}

	def "is-equal with comparison performer - fail with cause"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(actual, consumer)
		and:
			ComparisonPerformer<?> comparisonPerformer = Mock()
			RuntimeException cause = new RuntimeException()
			ComparisonResult comparisonResult = new ComparisonFail(actual, expected, comparisonPerformer, cause)

		when:
			AssertResultConstructor messageConstructor = performer.isEqual(expected, comparisonPerformer)
		then: "comparison performer returns instance of the ComparisonFail"
			1 * comparisonPerformer.compare(actual, expected) >> comparisonResult
		and:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and:
			capturedResult.getActual().is(actual)
			capturedResult.getExpected().is(expected)
			capturedResult.getMessage() == ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS
		and: "assert result contains cause"
			capturedResult.getCause().is(cause)
		and:
			capturedResult instanceof ComparisonAssertResult
			capturedResult.getComparisonResult().is(comparisonResult)
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual << TestData.variousObjects()
			expected << TestData.variousObjects().reverse()
	}

	def "is-equal with comparison performer - perform fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(actual, consumer)
		and:
			ComparisonPerformer<?> comparisonPerformer = Mock()
			RuntimeException error = new RuntimeException()

		when:
			performer.isEqual(expected, comparisonPerformer)
		then: "comparison performer throws an exception while executing"
			1 * comparisonPerformer.compare(actual, expected) >> {throw error}
		and:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and:
			capturedResult.getActual().is(actual)
			capturedResult.getExpected().is(expected)
			capturedResult.getMessage() == ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS
		and: "assert result contains cause"
			capturedResult.getCause().is(error)
		and: "assert result contains comparison result"
			capturedResult instanceof ComparisonAssertResult
			def comparisonResult = capturedResult.getComparisonResult()
		and: "comparison result is a fail with thrown error"
			comparisonResult instanceof ComparisonFail
			comparisonResult.getCause().is(error)
		where:
			actual << TestData.variousObjects()
			expected << TestData.variousObjects().reverse()
	}

	def "is-null success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(null, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNull()
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
	}

	def "is-null fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNull()
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == actual
			capturedResult.getExpected() == null
			capturedResult.getMessage() == ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual << TestData.variousObjects() - null
	}

	def "is-NOT-null success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNotNull()
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			actual << TestData.variousObjects() - null
	}

	def "is-NOT-null fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(null, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNotNull()
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == null
			capturedResult.getExpected() == ObjectAssertPerformer.EXPECTED_NON_NULL_VALUE
			capturedResult.getMessage() == ObjectAssertPerformer.MESSAGE_NOT_NULL_VALUE
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
	}

	def "null-consistent success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNullConsistent(expected)
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			actual	|	expected
			null	|	null
			""		|	12
	}

	def "null-consistent fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer<Object> performer = new ObjectAssertPerformer<Object>(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNullConsistent(expected)
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == actual
			capturedResult.getExpected() == expected
			capturedResult.getMessage() == ObjectAssertPerformer.MESSAGE_NULL_CONSISTENT
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual	|	expected
			""		|	null
			null	|	12
	}

	def "each assert place it's message as default"() {
		given:
			Consumer<AssertResult> consumer = Mock()

		expect:
			new ObjectAssertPerformer(42, consumer).isNull()
				.getAssertResult().getMessage() == ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS

			new ObjectAssertPerformer(null, consumer).isNotNull()
				.getAssertResult().getMessage() == ObjectAssertPerformer.MESSAGE_NOT_NULL_VALUE

			new ObjectAssertPerformer(null, consumer).isNullConsistent(42)
				.getAssertResult().getMessage() == ObjectAssertPerformer.MESSAGE_NULL_CONSISTENT

			new ObjectAssertPerformer(null, consumer).isEqual(42)
				.getAssertResult().getMessage() == ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS

			new ObjectAssertPerformer(null, consumer).isNotEqual(null)
				.getAssertResult().getMessage() == ObjectAssertPerformer.MESSAGE_NOT_EQUAL_OBJECTS
	}

	def "is-in success"() {
		given:
			Consumer<AssertResult> consumer = Mock(Consumer)
			ObjectAssertPerformer performer = new ObjectAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isIn(collection)
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			actual			|		collection
			2				|		[1,2,3,4]
			12				|		[12,13,14]
			null			|		["", null, "qwe"]
			arr(3,4)		|		[arr(3,4),arr(5,6)]		// < default deep predicate is used
	}

	def "is-in fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			ObjectAssertPerformer performer = new ObjectAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isIn(collection)
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
			messageConstructor.getAssertResult().is(capturedResult)
		and:
			capturedResult.getActual()[0].is(actual)
			capturedResult.getExpected() == "Any of ${collection}"
			capturedResult.getMessage() == CollectionAssertPerformer.MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES
			capturedResult.getCause() == null
		where:
			actual			|		collection
			null			|		[12]
			1				|		[12]
			1				|		[null]
			1				|		[9,8,7]
			1				|		[0,null,"qwe"]
			arr(1,2)		|		[arr(7,8),arr(5,6)]
	}

	def "is-in empty collection"() {
		when:
			Consumer<AssertResult> consumer = Mock()
			new ObjectAssertPerformer(42, consumer).isIn(collection)
		then:
			0 * consumer.accept(_)
			thrown(IllegalArgumentException)
		where:
			collection << [null, []]
	}

	private int[] arr(int[] ints) {
		return ints
	}
}
