package org.whaka.asserts.builder

import spock.lang.Specification

import org.whaka.TestData
import org.whaka.asserts.AssertResult

class AssertResultConstructorTest extends Specification {

	def "construction"() {
		given:
			AssertResult result = Mock(AssertResult)
		when:
			AssertResultConstructor constructor = new AssertResultConstructor(result)
		then:
			0 * result._
		and:
			constructor.getAssertResult().is(result)
	}

	def "create (factory method)"() {
		given:
			AssertResult result = Mock(AssertResult)

		when:
			AssertResultConstructor constructor = AssertResultConstructor.create(result)
		then:
			0 * result._
		and:
			constructor.getAssertResult().is(result)

		when:
			AssertResultConstructor constructor2 = AssertResultConstructor.create(null)
		then:
			constructor2.is(AssertResultConstructor.EMPTY)
	}

	def "with-message"() {
		given:
			String capturedMessage = null
			AssertResult mockResult = Mock(AssertResult)
			AssertResultConstructor constructor = new AssertResultConstructor(mockResult)
		when: "withMessage is called with a single argument"
			constructor.withMessage(msg)
		then: "setMessage is called on the result with the same message"
			1 * mockResult.setMessage(_) >> {args -> capturedMessage = args[0]}
			capturedMessage == msg
		where:
			msg << [null, "", "qwe", "qwe%s", "%s%d%f%b%%%q__"]
	}

	def "with-message with parameters"() {
		given:
			String capturedMessage = null
			AssertResult mockResult = Mock(AssertResult)
			AssertResultConstructor constructor = new AssertResultConstructor(mockResult)

		when: "withMessage is called with multiple parameters"
			constructor.withMessage("some_%s", "qwe")
		then: "setMessage is called on the result with the message formatted by the String.format"
			1 * mockResult.setMessage(_) >> {args -> capturedMessage = args[0]}
			capturedMessage == "some_qwe"

		when:
			constructor.withMessage("some_%s_%d_%%", false, 42)
		then:
			1 * mockResult.setMessage(_) >> {args -> capturedMessage = args[0]}
			capturedMessage == "some_false_42_%"

		when: "with-message is called with parameters"
			constructor.withMessage("some_%s_%d", false)
		then: "default String.format rules apply for it"
			thrown(MissingFormatArgumentException)

		when: "specified string is null"
			constructor.withMessage(null, false, 42)
		then: "no format is performed and setMessage is called with null as argument"
			1 * mockResult.setMessage(_) >> {args -> capturedMessage = args[0]}
			capturedMessage == null
	}

	def "with-cause"() {
		given:
			Throwable capturedCause = null
			AssertResult mockResult = Mock(AssertResult)
			AssertResultConstructor constructor = new AssertResultConstructor(mockResult)
		when: "with-cause is called"
			constructor.withCause(cause)
		then: "setCause is called on the result with the same cause"
			1 * mockResult.setCause(_) >> {args -> capturedCause = args[0]}
			capturedCause == cause
		where:
			cause << TestData.variousCauses()
	}
}
