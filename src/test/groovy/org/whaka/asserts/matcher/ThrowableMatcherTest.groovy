package org.whaka.asserts.matcher

import org.whaka.TestData
import org.whaka.asserts.AssertResult

import spock.lang.Specification

class ThrowableMatcherTest extends Specification {

	def "throwableOfType"() {
		when:
			def m1 = ThrowableMatcher.throwableOfType(cause?.getClass())
		then:
			m1 instanceof ThrowableMatcher
			m1.getExpectedType() == cause?.getClass()
		where:
			cause << TestData.variousCauses()
	}

	def "notExpected"() {
		expect:
			ThrowableMatcher.notExpected().is(ThrowableMatcher.NOT_EXPECTED)
			ThrowableMatcher.notExpected().is(ThrowableMatcher.throwableOfType(null))
			ThrowableMatcher.notExpected().getExpectedType() == null
	}

	def "expected message"() {
		given:
			def m1 = ThrowableMatcher.throwableOfType(type)
		expect:
			m1.getExpectedMessage() == message
		where:
			type					|	message
			null					|	"no exception"
			RuntimeException		|	"instance of ${RuntimeException.class.getCanonicalName()}"
			FileNotFoundException	|	"instance of ${FileNotFoundException.class.getCanonicalName()}"
			AssertionError			|	"instance of ${AssertionError.class.getCanonicalName()}"
			Throwable				|	"instance of ${Throwable.class.getCanonicalName()}"
	}

	def "matches"() {
		given:
			def m1 = ThrowableMatcher.throwableOfType(cause?.getClass())
		expect:
			m1.matches(cause)
		where:
			cause << TestData.variousCauses()
	}

	def "matches empty"() {
		given:
			def m1 = ThrowableMatcher.notExpected()
		expect:
			m1.matches(cause) == (cause == null)
		where:
			cause << TestData.variousCauses()
	}

	def "matches with result"() {
		given:
			def m1 = ThrowableMatcher.throwableOfType(RuntimeException)
			def item = new IOException("qwe")
		when:
			AssertResult res = m1.matches(item, "msg " + cause, cause).get()
		then:
			res.getActual() == item?.getClass()
			res.getExpected() == "instance of ${RuntimeException.class.getCanonicalName()}"
			res.getMessage() == "msg " + cause
			res.getCause() == cause?:item
		where:
			cause << TestData.variousCauses()
	}
}
