package org.whaka.asserts

import static org.whaka.TestData.*
import spock.lang.Specification

class AssertResultTest extends Specification {

	def "maximum constructor sets all fields"() {
		when:
			AssertResult result = new AssertResult(actual, expected, message, cause)
		then:
			result.getActual().is(actual)
			result.getExpected().is(expected)
			result.getMessage().is(message)
			result.getCause().is(cause)
		where:
			actual << variousObjects()
			expected << variousObjects().reverse()
			message << variousMessages()
			cause << variousCauses()
	}

	def "no cause constructor"() {
		when:
			AssertResult result = new AssertResult(actual, expected, message)
		then:
			result.getActual().is(actual)
			result.getExpected().is(expected)
			result.getMessage().is(message)
			result.getCause() == null
		where:
			actual << variousObjects().reverse()
			expected << variousObjects()
			message << variousMessages()
	}

	def "no objects constructor"() {
		when:
			AssertResult result = new AssertResult(message, cause)
		then:
			result.getActual() == null
			result.getExpected() == null
			result.getMessage().is(message)
			result.getCause().is(cause)
		where:
			message << variousMessages()
			cause << variousCauses()
	}

	def "only message constructor"() {
		when:
			AssertResult result = new AssertResult(message)
		then:
			result.getActual() == null
			result.getExpected() == null
			result.getMessage().is(message)
			result.getCause() == null
		where:
			message << variousMessages()
	}

	def "empty constructor"() {
		when:
			AssertResult result = new AssertResult()
		then:
			result.getActual() == null
			result.getExpected() == null
			result.getMessage() == null
			result.getCause() == null
	}

	def "getters/setters"() {
		given:
			AssertResult result = new AssertResult()

		when:
			result.setActual(actual)
		then:
			result.getActual().is(actual)
			result.getExpected() == null
			result.getMessage() == null
			result.getCause() == null

		when:
			result.setExpected(expected)
		then:
			result.getActual().is(actual)
			result.getExpected().is(expected)
			result.getMessage() == null
			result.getCause() == null

		when:
			result.setMessage(message)
		then:
			result.getActual().is(actual)
			result.getExpected().is(expected)
			result.getMessage().is(message)
			result.getCause() == null

		when:
			result.setCause(cause)
		then:
			result.getActual().is(actual)
			result.getExpected().is(expected)
			result.getMessage().is(message)
			result.getCause().is(cause)

		where:
			actual << variousObjects()
			expected << variousObjects().reverse()
			message << variousMessages()
			cause << variousCauses()
	}

	def "equal/hash"() {
		given:
			AssertResult result1 = new AssertResult(actual, expected, message, cause)
			AssertResult result2 = new AssertResult(actual, expected, message)
			AssertResult result3 = new AssertResult(message, cause)
			AssertResult result4 = new AssertResult(message)
			AssertResult result5 = new AssertResult()
		expect:
			result1.equals(result1)
			result1.equals(result2) == (cause == null)
			result2.equals(result1) == (cause == null)
			result3.equals(result1) == (actual == null && expected == null)
			result1.equals(result3) == (actual == null && expected == null)
			result2.equals(result3) == (actual == null && expected == null && cause == null)
			result3.equals(result2) == (actual == null && expected == null && cause == null)
			result3.equals(result4) == (cause == null)
			result4.equals(result3) == (cause == null)
			result4.equals(result5) == (message == null)
			result5.equals(result4) == (message == null)
			compareHashCodeIfEquals(result1, result1)
			compareHashCodeIfEquals(result1, result2)
			compareHashCodeIfEquals(result2, result3)
			compareHashCodeIfEquals(result3, result4)
			compareHashCodeIfEquals(result4, result5)
			compareHashCodeIfEquals(result5, result1)
		where:
			actual << variousObjects()
			expected << variousObjects().reverse()
			message << variousMessages()
			cause << variousCauses()
	}

	private boolean compareHashCodeIfEquals(Object a, Object b) {
		return a.equals(b) ? a.hashCode() == b.hashCode() : true
	}
}
