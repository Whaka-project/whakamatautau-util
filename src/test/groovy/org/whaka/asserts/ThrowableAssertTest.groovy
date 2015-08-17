package org.whaka.asserts

import spock.lang.Specification

import org.whaka.TestData
import org.whaka.asserts.builder.ThrowableAssertPerformer

class ThrowableAssertTest extends Specification {

	def "construction"() {
		when:
			ThrowableAssert _assert = new ThrowableAssert(throwable)
		then:
			_assert.getActual() == throwable
		where:
			throwable << TestData.variousCauses()
	}

	def "not-expected"() {

		when:
			new ThrowableAssert(null).notExpected()
		then:
			notThrown(AssertError)

		when:
			new ThrowableAssert(throwable).notExpected()
		then:
			AssertError e = thrown()
			checkError(e, throwable.getClass(), null,
				ThrowableAssertPerformer.MESSAGE_UNEXPECTED_THROWABLE_HAPPENED, throwable)

		where:
			throwable << TestData.variousCauses() - null
	}

	def "is-instance-of"() {

		given:
			RuntimeException runtimeException = new RuntimeException()

		when:
			new ThrowableAssert(runtimeException).isInstanceOf(RuntimeException.class)
		then:
			notThrown(AssertError)

		when:
			new ThrowableAssert(runtimeException).isInstanceOf(NullPointerException.class)
		then:
			AssertError errorIllegal = thrown()
			checkError(errorIllegal, RuntimeException.class, NullPointerException.class,
				ThrowableAssertPerformer.MESSAGE_ILLEGAL_THROWABLE_HAPPENED, runtimeException)

		when:
			new ThrowableAssert(null).isInstanceOf(NullPointerException.class)
		then:
			AssertError errorAbsent = thrown()
			checkError(errorAbsent, null, NullPointerException.class,
				ThrowableAssertPerformer.MESSAGE_EXPECTED_THROWABLE_NOT_HAPPENED, null)
	}

	private void checkError(AssertError e, Object actual, Object expected, String message, Throwable cause) {
		assert e.getResults().size() == 1
		AssertResult result = e.getResults()[0]
		assert result.getActual() == actual
		assert result.getExpected() == expected
		assert result.getMessage() == message
		assert result.getCause() == cause
	}
}
