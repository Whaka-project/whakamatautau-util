package org.whaka.asserts.builder

import java.util.function.Consumer

import spock.lang.Specification

import org.whaka.TestData
import org.whaka.asserts.AssertResult

class ThrowableAssertPerformerTest extends Specification {

	def "construction"() {
		given:
			Consumer<AssertResult> mockConsumer = Mock(Consumer)
		when:
			ThrowableAssertPerformer performer = new ThrowableAssertPerformer(throwable, mockConsumer)
		then:
			0 * mockConsumer.accept(_)
			performer.actual == throwable
		where:
			throwable << TestData.variousCauses()
	}

	def "is-instance-of success"() {
		given:
			Consumer<AssertResult> mockConsumer = Mock(Consumer)
			ThrowableAssertPerformer performer = new ThrowableAssertPerformer(throwable, mockConsumer)
		when:
			AssertResultConstructor messageConstructor1 = performer.isInstanceOf(expectedType)
			AssertResultConstructor messageConstructor2 = performer.isInstanceOf(Throwable.class)
		then:
			0 * mockConsumer.accept(_)
			messageConstructor1.getAssertResult() == null
			messageConstructor2.getAssertResult() == null
		where:
			throwable							|	expectedType
			new RuntimeException()				|	RuntimeException.class
			new IllegalArgumentException()		|	RuntimeException.class
			new IllegalArgumentException()		|	IllegalArgumentException.class
			new IllegalArgumentException()		|	Exception.class
			new IOException()					|	IOException.class
			new FileNotFoundException()			|	IOException.class
			new FileNotFoundException()			|	FileNotFoundException.class
	}

	def "is-instance-of fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> mockConsumer = Mock(Consumer)
			ThrowableAssertPerformer performer = new ThrowableAssertPerformer(throwable, mockConsumer)
		when:
			AssertResultConstructor messageConstructor = performer.isInstanceOf(expectedType)
		then:
			1 * mockConsumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == throwable.getClass()
			capturedResult.getExpected() == expectedType
			capturedResult.getCause() == throwable
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			throwable							|	expectedType
			new RuntimeException()				|	FileNotFoundException.class
			new RuntimeException()				|	IOException.class
			new IllegalArgumentException()		|	IndexOutOfBoundsException.class
			new IndexOutOfBoundsException()		|	ArrayIndexOutOfBoundsException.class
			new IOException()					|	RuntimeException.class
			new IOException()					|	FileNotFoundException.class
	}

	def "is-instance-of null = unexpected"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> mockConsumer = Mock(Consumer)
			ThrowableAssertPerformer performer = new ThrowableAssertPerformer(throwable, mockConsumer)
		when:
			AssertResultConstructor messageConstructor = performer.isInstanceOf(null)
		then:
			1 * mockConsumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == throwable.getClass()
			capturedResult.getExpected() == null
			capturedResult.getCause() == throwable
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			throwable << TestData.variousCauses() - null
	}

	def "is-instance-of fail on actual null"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> mockConsumer = Mock(Consumer)
			ThrowableAssertPerformer performer = new ThrowableAssertPerformer(null, mockConsumer)
		when:
			AssertResultConstructor messageConstructor = performer.isInstanceOf(expectedType)
		then:
			1 * mockConsumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == null
			capturedResult.getExpected() == expectedType
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			expectedType << [FileNotFoundException.class, IOException.class, IndexOutOfBoundsException.class,
				 ArrayIndexOutOfBoundsException.class, RuntimeException.class]
	}

	def "is-instance-of fail message"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> mockConsumer = Mock(Consumer)
			ThrowableAssertPerformer performer = new ThrowableAssertPerformer(throwable, mockConsumer)
		when:
			performer.isInstanceOf(expectedType)
		then:
			1 * mockConsumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getMessage() == expectedMessage
		where:
			throwable				|	expectedType		||	expectedMessage
			new RuntimeException()	|	IOException.class	||	ThrowableAssertPerformer.MESSAGE_ILLEGAL_THROWABLE_HAPPENED
			null					|	IOException.class	||	ThrowableAssertPerformer.MESSAGE_EXPECTED_THROWABLE_NOT_HAPPENED
			new RuntimeException()	|	null				||	ThrowableAssertPerformer.MESSAGE_UNEXPECTED_THROWABLE_HAPPENED
	}

	def "is-unexpected success"() {
		given:
			Consumer<AssertResult> mockConsumer = Mock(Consumer)
			ThrowableAssertPerformer performer = new ThrowableAssertPerformer(null, mockConsumer)
		when:
			AssertResultConstructor messageConstructor = performer.notExpected()
		then:
			0 * mockConsumer.accept(_)
			messageConstructor.getAssertResult() == null
	}

	def "is-unexpected fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> mockConsumer = Mock(Consumer)
			ThrowableAssertPerformer performer = new ThrowableAssertPerformer(throwable, mockConsumer)
		when:
			AssertResultConstructor messageConstructor = performer.notExpected()
		then:
			1 * mockConsumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getActual() == throwable.getClass()
			capturedResult.getExpected() == null
			capturedResult.getCause() == throwable
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			throwable << TestData.variousCauses() - null
	}

	def "is-unexpected fail message"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> mockConsumer = Mock(Consumer)
		when:
			ThrowableAssertPerformer performer = new ThrowableAssertPerformer(new RuntimeException(), mockConsumer)
			performer.notExpected()
		then:
			1 * mockConsumer.accept(_) >> {args -> capturedResult = args[0]}
			capturedResult.getMessage() == ThrowableAssertPerformer.MESSAGE_UNEXPECTED_THROWABLE_HAPPENED
	}

	def "each assert place it's message as default"() {
		given:
			Consumer<AssertResult> consumer = Mock(Consumer)

		expect:
			new ThrowableAssertPerformer(new RuntimeException(), consumer).notExpected()
				.getAssertResult().getMessage() == ThrowableAssertPerformer.MESSAGE_UNEXPECTED_THROWABLE_HAPPENED

			new ThrowableAssertPerformer(new RuntimeException(), consumer).isInstanceOf(IOException.class)
				.getAssertResult().getMessage() == ThrowableAssertPerformer.MESSAGE_ILLEGAL_THROWABLE_HAPPENED

			new ThrowableAssertPerformer(null, consumer).isInstanceOf(IOException.class)
				.getAssertResult().getMessage() == ThrowableAssertPerformer.MESSAGE_EXPECTED_THROWABLE_NOT_HAPPENED
	}
}
