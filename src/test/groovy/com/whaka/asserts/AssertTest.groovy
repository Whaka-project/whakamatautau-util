package com.whaka.asserts

import spock.lang.Specification

import com.whaka.TestData

class AssertTest extends Specification {

	def "fail"() {
		when:
			Assert.fail(message)
		then:
			AssertError e = thrown()
			e.getResults().size() == 1
		and:
			AssertResult result = e.getResults()[0]
			result.getActual() == null
			result.getExpected() == null
			result.getMessage() == message
			result.getCause() == null
		where:
			message << TestData.variousMessages()
	}

	def "fail with format"() {
		when:
			Assert.fail(message, (Object[]) arguments)
		then:
			AssertError e = thrown()
			e.getResults().size() == 1
		and:
			AssertResult result = e.getResults()[0]
			result.getActual() == null
			result.getExpected() == null
			result.getMessage() == resultMessage
			result.getCause() == null
		where:
			message		|	arguments	||	resultMessage
			null		|	[]			||	null
			""			|	[]			||	""
			""			|	[12]		||	""
			"%s"		|	[12]		||	"12"
			"_%s_%d_%%"	|	[false, 42]	||	"_false_42_%"
	}

	def "assert-object"() {
		when:
			ObjectAssert _assert = Assert.assertObject(actual)
		then:
			_assert.getActual() == actual
		where:
			actual << TestData.variousObjects()
	}

	def "assert-boolean"() {
		when:
			BooleanAssert _assert = Assert.assertBoolean(actual)
		then:
			_assert.getActual() == actual
		where:
			actual << [true, false, null]
	}

	def "assert-number"() {
		when:
			NumberAssert _assert = Assert.assertNumber(actual)
		then:
			_assert.getActual() == actual
		where:
			actual << [1, 12, Double.NaN, Double.MAX_VALUE, Double.NEGATIVE_INFINITY, null, BigDecimal.TEN, 999L]
	}

	def "assert-throwable"() {
		when:
			ThrowableAssert _assert = Assert.assertThrowable(throwable)
		then:
			_assert.getActual() == throwable
		where:
			throwable << TestData.variousCauses()
	}

	def "assert-collection"() {
		when:
			CollectionAssert<?> _assert = Assert.assertCollection(collection)
		then:
			_assert.getActual() == collection
		where:
			collection << [null, [], Collections.emptyList(), Arrays.asList(1,2,3), ["qwe", 0.5, false]]
	}

	def "builder"() {
		expect:
			Assert.builder().is(Assert.builder()) == false
			Assert.builder() != null
	}
}
