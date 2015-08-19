package org.whaka.asserts

import static org.whaka.TestData.*

import org.hamcrest.Matcher

import spock.lang.Specification

import org.whaka.asserts.AssertError
import org.whaka.asserts.AssertResult

class AssertBuilderTest extends Specification {

	def "results building"() {
		given:
			AssertBuilder builder = new AssertBuilder()
			List<AssertResult> results = createAssertResultsList(actual, expected, message, cause)

		when:
			for (AssertResult result : results)
				builder.accept(result)
		then:
			builder.getAssertResults().equals(results)

		when:
			AssertError error1 = builder.build()
			AssertError error2 = builder.build()
		then:
			error1.getResults().equals(results)
			error2.getResults().equals(results)
			error1.equals(error2)

		where:
			actual << variousObjects()
			expected << variousObjects().reverse()
			message << variousMessages()
			cause << variousCauses()
	}

	def "perform-assert"() {
		given:
			AssertBuilder builder = new AssertBuilder()
			AssertResult result1 = new AssertResult("some1")
			AssertResult result2 = new AssertResult(1, 12, "some2", new RuntimeException())

		when:
			builder.performAssert()
		then:
			notThrown(AssertError)

		when:
			builder.accept(result1)
			builder.performAssert()
		then:
			AssertError e1 = thrown()
			e1.getResults().equals([result1])

		when:
			builder.accept(result2)
			builder.performAssert()
		then:
			AssertError e2 = thrown()
			e2.getResults().equals([result1, result2])
	}

	def "get-assert-results"() {
		given:
			AssertBuilder builder = new AssertBuilder()
			AssertResult result1 = new AssertResult("some1")
			AssertResult result2 = new AssertResult(1, 12, "some2", new RuntimeException())

		when: "assert result is 'accepted' by the builder"
			builder.accept(result1)
		then: "it is available in its result list"
			builder.getAssertResults().equals([result1])

		when: "assert result is added directly to the result list"
			builder.getAssertResults().add(result2)
		then: "it is available in built errors"
			AssertError e = builder.build()
			e.getResults().equals([result1, result2])

		when: "assert result is removed directly from the result list"
			builder.getAssertResults().remove(result1)
		then: "it is no longer available in built errors"
			AssertError e2 = builder.build()
			e2.getResults().equals([result2])

		when: "result list is directly cleared"
			builder.getAssertResults().clear()
		then: "no error can be built, for no results are available"
			builder.build() == null
	}

	def "add-message"() {
		given:
			AssertBuilder builder = new AssertBuilder()
		when:
			AssertBuilder builder2 = builder.addMessage(message)
		then:
			builder2.is(builder)
		and:
			builder.getAssertResults().size() == 1
			AssertResult result = builder.getAssertResults()[0]
			result.getActual() == null
			result.getExpected() == null
			result.getMessage() == message
			result.getCause() == null
		where:
			message << variousMessages()
	}

	def "add-message with format"() {
		given:
			AssertBuilder builder = new AssertBuilder()
		when:
			AssertBuilder builder2 = builder.addMessage(message, (Object[]) arguments)
		then:
			builder2.is(builder)
		and:
			builder.getAssertResults().size() == 1
			AssertResult result = builder.getAssertResults()[0]
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
		given:
			AssertBuilder builder = new AssertBuilder()

		when:
			ObjectAssertPerformer performer = builder.checkObject(actual)
		then:
			performer.getActual() == actual

		when:
			builder.checkObject(actual).isEqual(actual).withMessage(message)
		then:
			builder.getAssertResults().size() == 0

		when:
			builder.checkObject(actual).isEqual(expected).withMessage(message).withCause(cause)
		then:
			builder.getAssertResults().size() == 1
			AssertResult result = builder.getAssertResults().get(0)
			result.getActual() == actual
			result.getExpected() == expected
			result.getMessage() == message
			result.getCause() == cause

		where:
			actual << variousObjects()
			expected << variousObjects().reverse()
			message << variousMessages()
			cause << variousCauses()
	}

	def "assert-boolean"() {
		given:
			AssertBuilder builder = new AssertBuilder()
			RuntimeException cause = new RuntimeException("some cause")

		when:
			BooleanAssertPerformer performer = builder.checkBoolean(true)
		then:
			performer.getActual() == true

		when:
			builder.checkBoolean(true).isTrue().withMessage("never gonna happen")
		then:
			builder.getAssertResults().size() == 0

		when:
			builder.checkBoolean(true).isFalse().withMessage("Error[expected=%s]!", false)
		then:
			builder.getAssertResults().size() == 1
			AssertResult result = builder.getAssertResults().get(0)
			result.getActual() == true
			result.getExpected() == false
			result.getMessage() == "Error[expected=false]!"
			result.getCause() == null

		when:
			builder.checkBoolean(false).isTrue().withCause(cause)
		then:
			builder.getAssertResults().size() == 2
			AssertResult result2 = builder.getAssertResults().get(1)
			result2.getActual() == false
			result2.getExpected() == true
			result2.getMessage() == null
			result2.getCause() == cause
	}

	def "assert-throwable"() {
		given:
			AssertBuilder builder = new AssertBuilder()

		when:
			ThrowableAssertPerformer performer = builder.checkThrowable(throwable)
		then:
			performer.getActual().is(throwable)

		when:
			builder.checkThrowable(throwable).isInstanceOf(throwable.getClass())
		then:
			builder.getAssertResults().isEmpty()

		when: "not-expected is called against null value"
			builder.checkThrowable(null).notExpected()
		then: "assert passes successfully"
			builder.getAssertResults().isEmpty()

		when: "throwable is catched - it can be reported with not-expected method"
			try {
				throw throwable
			} catch (Throwable e) {
				builder.checkThrowable(e).notExpected()
			}
		then: "assert result will be added"
			builder.getAssertResults().size() == 1
			AssertResult result = builder.getAssertResults()[0]
		and: "result will contain specific message"
			result.getMessage() == ThrowableAssertPerformer.MESSAGE_UNEXPECTED_THROWABLE_HAPPENED
		and: "result will contain catched CLASS as actual value, and null as expected"
			result.getActual() == throwable.getClass()
			result.getExpected() == null
		and: "result will contain catched throwable as cause"
			result.getCause() == throwable

		when: "some exception was expected - it can be reported with is-instance-of"
			try {
				// no exception thrown
				builder.checkThrowable(null).isInstanceOf(NullPointerException.class)
			} catch (Exception e) {
			}
		then: "assert result will be added"
			builder.getAssertResults().size() == 2
			AssertResult result2 = builder.getAssertResults()[1]
		and: "result will contain specific message"
			result2.getMessage() == ThrowableAssertPerformer.MESSAGE_EXPECTED_THROWABLE_NOT_HAPPENED
		and: "result will contain null as actual value, and expected class as expected value"
			result2.getActual() == null
			result2.getExpected() == NullPointerException.class

		where:
			throwable << variousCauses() - null
	}

	def "assert-collection"() {
		given:
			AssertBuilder builder = new AssertBuilder()

		when:
			Collection<?> collection = ["qwe"]
			CollectionAssertPerformer<?> performer = builder.checkCollection(collection)
		then:
			performer.getActual().is(collection)

		when:
			builder.checkCollection([]).isEmpty()
			builder.checkCollection([12, 22, 34]).contains(22)
			builder.checkCollection([12, (int[])[1,2], 34]).contains((int[])[1,2])
			builder.checkCollection(null).isEmptyOrNull()
			builder.checkCollection([1,2,3,4]).containsAny([9,8,7,3])
			builder.checkCollection([1,2,3,4]).containsAll([4,2])
		then:
			builder.getAssertResults().size == 0

		when:
			builder.checkCollection([1,2,3]).isEmpty()
		then:
			builder.getAssertResults().size == 1
			AssertResult result = builder.getAssertResults()[0]
			result.getActual() == [1,2,3]
			result.getExpected() == []
			result.getMessage() == CollectionAssertPerformer.MESSAGE_EMPTY_COLLECTION_EXPECTED
			result.getCause() == null
	}

	def "checkThat"() {
		given:
			Matcher matcher = Mock()
			AssertBuilder builder = new AssertBuilder()

		when:
			builder.checkThat(42, matcher)
		then:
			1 * matcher.matches(42) >> true
		and:
			0 * matcher._
			builder.getAssertResults().isEmpty()

		when:
			builder.checkThat(42, matcher).withMessage("msg")
		then:
			1 * matcher.matches(42) >> false
		and:
			1 * matcher.describeTo(_) >> {it[0].appendText("test test")}
		and:
			builder.getAssertResults().size() == 1
			def res = builder.getAssertResults()[0]
		and:
			res.getActual() == 42
			res.getExpected() == "test test"
			res.getMessage() == "msg"
			res.getCause() == null
	}
}
