package org.whaka.asserts

import spock.lang.Specification

class BooleanAssertTest extends Specification {

	def "construction"() {
		when:
			BooleanAssert _assert = new BooleanAssert(actual)
		then:
			_assert.getActual() == actual
		where:
			actual << [true, false, null]
	}

	def "is-true"() {
		when:
			new BooleanAssert(true).isTrue()
		then:
			notThrown(AssertError)

		when:
			new BooleanAssert(false).isTrue()
		then:
			AssertError e1 = thrown()
			checkError(e1, false, true, null)

		when:
			new BooleanAssert(null).isTrue("Custom msg!")
		then:
			AssertError e2 = thrown()
			checkError(e2, null, true, "Custom msg!")
	}

	def "is-false"() {
		when:
			new BooleanAssert(false).isFalse()
		then:
			notThrown(AssertError)

		when:
			new BooleanAssert(true).isFalse("I WANT FAAAAAALSE")
		then:
			AssertError e1 = thrown()
			checkError(e1, true, false, "I WANT FAAAAAALSE")

		when:
			new BooleanAssert(null).isFalse()
		then:
			AssertError e2 = thrown()
			checkError(e2, null, false, null)
	}

	private void checkError(AssertError e, Object actual, Object expected, String message) {
		assert e.getResults().size() == 1
		AssertResult result = e.getResults()[0]
		assert result.getActual() == actual
		assert result.getExpected() == expected
		assert result.getMessage() == message
	}
}
