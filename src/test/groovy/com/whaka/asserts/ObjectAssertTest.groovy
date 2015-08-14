package com.whaka.asserts

import spock.lang.Specification

import com.whaka.TestData
import com.whaka.asserts.builder.ObjectAssertPerformer
import com.whaka.util.reflection.comparison.ComparisonPerformers
import com.whaka.util.reflection.comparison.ComparisonResult

class ObjectAssertTest extends Specification {

	def "construction"() {
		when:
			ObjectAssert _assert = new ObjectAssert(actual)
		then:
			_assert.getActual() == actual
		where:
			actual << TestData.variousObjects()
	}

	def "is-null-consistent"() {
		given:
			ObjectAssert _assert = new ObjectAssert(actual)

		when:
			_assert.isNullConsistent(consistent)
		then:
			notThrown(AssertError)

		when:
			_assert.isNullConsistent(inconsistent)
		then:
			AssertError e = thrown()
			checkError(e, actual, inconsistent, ObjectAssertPerformer.MESSAGE_NULL_CONSISTENT)

		where:
			actual	|	consistent	|	inconsistent
			null	|	null		|	"str"
			"str"	|	42			|	null
	}

	def "is-equal"() {
		given:
			ObjectAssert _assert = new ObjectAssert(actual)

		when:
			_assert.isEqual(actual)
		then:
			notThrown(AssertError)

		when:
			_assert.isEqual(unequal)
		then:
			AssertError e = thrown()
			checkError(e, actual, unequal, ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS)

		where:
			actual << TestData.variousObjects()
			unequal << TestData.variousObjects().reverse()
	}

	def "is-equal with comparison performer - success"() {
		given:
			ObjectAssert _assert = new ObjectAssert(actual)
		when:
			_assert.isEqual(expected, comparisonPerformer)
		then:
			notThrown(AssertError)
		where:
			actual								|	expected							|	comparisonPerformer
			null								|	null								|	ComparisonPerformers.DEEP_EQUALS
			12									|	12									|	ComparisonPerformers.DEEP_EQUALS
			12									|	12									|	ComparisonPerformers.REFLECTIVE_EQUALS
			12									|	12									|	ComparisonPerformers.DOUBLE_MATH_EQUALS
			new BigDecimal(BigInteger.TEN, 1)	|	new BigDecimal(BigInteger.ONE, 0)	|	ComparisonPerformers.DOUBLE_MATH_EQUALS
			""									|	""									|	ComparisonPerformers.DEEP_EQUALS
			""									|	""									|	{a, b -> new ComparisonResult("", "", null, true)}
	}

	def "is-equal with comparison performer - fail"() {
		given:
			ObjectAssert _assert = new ObjectAssert(actual)
		when:
			_assert.isEqual(expected, comparisonPerformer)
		then:
			AssertError error = thrown()
			checkError(error, actual, expected, ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS)
		and:
			AssertResult result = error.getResults()[0]
			result instanceof ComparisonAssertResult
		where:
			actual								|	expected							|	comparisonPerformer
			null								|	12									|	ComparisonPerformers.DEEP_EQUALS
			12									|	""									|	ComparisonPerformers.DEEP_EQUALS
			12									|	13									|	ComparisonPerformers.REFLECTIVE_EQUALS
			12									|	13									|	ComparisonPerformers.DOUBLE_MATH_EQUALS
			new BigDecimal(BigInteger.TEN, 1)	|	new BigDecimal(BigInteger.ONE, 0)	|	ComparisonPerformers.REFLECTIVE_EQUALS
			"1"									|	""									|	ComparisonPerformers.DEEP_EQUALS
			""									|	""									|	{a, b -> new ComparisonResult("", "", null, false)}
	}

	def "is-not-equal"() {
		given:
			ObjectAssert _assert = new ObjectAssert(actual)

		when:
			_assert.isNotEqual(unequal)
		then:
			notThrown(AssertError)

		when:
			_assert.isNotEqual(actual)
		then:
			AssertError e = thrown()
			checkError(e, actual, "Not '${actual}'", ObjectAssertPerformer.MESSAGE_NOT_EQUAL_OBJECTS)

		where:
			actual << TestData.variousObjects()
			unequal << TestData.variousObjects().reverse()
	}

	def "is-null"() {
		given:
			ObjectAssert assertNull = new ObjectAssert(null)
			ObjectAssert assertNotNull = new ObjectAssert(42)

		when:
			assertNull.isNull()
		then:
			notThrown(AssertError)

		when:
			assertNotNull.isNull()
		then:
			AssertError e = thrown()
			checkError(e, 42, null, ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS)
	}

	def "is-NOT-null"() {
		given:
			ObjectAssert assertNull = new ObjectAssert(null)
			ObjectAssert assertNotNull = new ObjectAssert(42)

		when:
			assertNotNull.isNotNull()
		then:
			notThrown(AssertError)

		when:
			assertNull.isNotNull()
		then:
			AssertError e = thrown()
			checkError(e, null, ObjectAssertPerformer.EXPECTED_NON_NULL_VALUE, ObjectAssertPerformer.MESSAGE_NOT_NULL_VALUE)
	}

	def "multiple continuous throws"() {
		given:
			ObjectAssert _assert = new ObjectAssert(null)

		when:
			_assert.isEqual(42)
		then:
			AssertError e = thrown()
			checkError(e, null, 42, ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS)

		when:
			_assert.isEqual("str")
		then:
			AssertError e2 = thrown()
			checkError(e2, null, "str", ObjectAssertPerformer.MESSAGE_EQUAL_OBJECTS)

		when:
			_assert.isNotEqual(null)
		then:
			AssertError e3 = thrown()
			checkError(e3, null, "Not 'null'", ObjectAssertPerformer.MESSAGE_NOT_EQUAL_OBJECTS)

		when:
			_assert.isNotNull()
		then:
			AssertError e4 = thrown()
			checkError(e4, null, ObjectAssertPerformer.EXPECTED_NON_NULL_VALUE, ObjectAssertPerformer.MESSAGE_NOT_NULL_VALUE)
	}

	private void checkError(AssertError e, Object actual, Object expected, String message) {
		assert e.getResults().size() == 1
		AssertResult result = e.getResults().get(0)
		assert result.getActual() == actual
		assert result.getExpected() == expected
		assert result.getMessage() == message
	}
}
