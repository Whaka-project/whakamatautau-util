package org.whaka.asserts.builder

import static org.whaka.TestData.*

import java.util.function.Consumer

import spock.lang.Specification

import org.whaka.asserts.AssertResult

class AssertPerformerTest extends Specification {

	def "construction"() {
		given:
			Object actual = new Object()
			Consumer<AssertResult> consumer = Mock(Consumer)
		when:
			AssertPerformer performer = new AssertPerformer(actual, consumer)
		then:
			0 * consumer.accept(_)
		and:
			performer.getActual().is(actual)

	}

	def "consume instance"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock(Consumer)
			AssertPerformer performer = new AssertPerformer(actual, consumer)

		when: "assert performer 'performs' assert"
			AssertResult result = performer.performResult(new AssertResult(actual, expected, message, cause))
		then: "consumer receives 'performed' assert result"
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == actual
			capturedResult.getExpected() == expected
			capturedResult.getMessage() == message
			capturedResult.getCause() == cause
		and: "performer returns 'performed' result"
			result.is(capturedResult)

		where:
			actual << variousObjects()
			expected << variousObjects().reverse()
			message << variousMessages()
			cause << variousCauses()
	}

	def "consume parameters"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock(Consumer)
			AssertPerformer performer = new AssertPerformer(actual, consumer)
		when: "perform with parameters called (instead of instance)"
			AssertResult performedResult = performer.performResult(actual, expected)
		then: "consumer receives result the same way"
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.is(performedResult)
		and: "performed result is equal to other result built with the same parameters"
			performedResult.equals(new AssertResult(actual, expected, null))
		where:
			actual << variousObjects()
			expected << variousObjects().reverse()
	}
}
