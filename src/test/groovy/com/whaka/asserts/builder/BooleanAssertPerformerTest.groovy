package com.whaka.asserts.builder

import java.util.function.Consumer

import spock.lang.Specification

import com.whaka.asserts.AssertResult

class BooleanAssertPerformerTest extends Specification {

	def "construction"() {
		given:
			Consumer<AssertResult> consumer = Mock(Consumer)
		when:
			BooleanAssertPerformer performer = new BooleanAssertPerformer(actual, consumer)
		then:
			0 * consumer.accept(_)
			performer.actual == actual
		where:
			actual << [true, false, null]
	}

	def "expected-true success"() {
		given:
			Consumer<AssertResult> consumer = Mock(Consumer)
			BooleanAssertPerformer performer = new BooleanAssertPerformer(true, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isTrue()
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
	}

	def "expected-true fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock(Consumer)
			BooleanAssertPerformer performer = new BooleanAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isTrue()
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == actual
			capturedResult.getExpected() == true
			capturedResult.getMessage() == null
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual << [false, null]
	}

	def "expected-false success"() {
		given:
			Consumer<AssertResult> consumer = Mock(Consumer)
			BooleanAssertPerformer performer = new BooleanAssertPerformer(false, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isFalse()
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
	}

	def "expected-false fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock(Consumer)
			BooleanAssertPerformer performer = new BooleanAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isFalse()
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == actual
			capturedResult.getExpected() == false
			capturedResult.getMessage() == null
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual << [true, null]
	}
}
