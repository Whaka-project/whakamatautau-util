package org.whaka.asserts

import static org.whaka.TestData.*

import org.hamcrest.Matcher

import spock.lang.Specification

class AssertBuilderTest extends Specification {

	def "results building"() {
		given:
			AssertBuilder builder = new AssertBuilder()
			List<AssertResult> results = createAssertResultsList(actual, expected, message, cause)

		when:
			for (AssertResult result : results)
				builder.addResult(result)
		then:
			builder.getAssertResults().equals(results)

		when:
			AssertError error1 = builder.build().get()
			AssertError error2 = builder.build().get()
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

	def "build on empty builder"() {
		given:
			AssertBuilder builder = new AssertBuilder()
		expect:
			builder.build().isPresent() == false
			builder.build().isPresent() == false
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
			builder.addResult(result1)
			builder.performAssert()
		then:
			AssertError e1 = thrown()
			e1.getResults().equals([result1])

		when:
			builder.addResult(result2)
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
			builder.addResult(result1)
		then: "it is available in its result list"
			builder.getAssertResults().equals([result1])

		when: "assert result is added directly to the result list"
			builder.getAssertResults().add(result2)
		then: "it is available in built errors"
			AssertError e = builder.build().get()
			e.getResults().equals([result1, result2])

		when: "assert result is removed directly from the result list"
			builder.getAssertResults().remove(result1)
		then: "it is no longer available in built errors"
			AssertError e2 = builder.build().get()
			e2.getResults().equals([result2])

		when: "result list is directly cleared"
			builder.getAssertResults().clear()
		then: "no error can be built, for no results are available"
			builder.build().isPresent() == false
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
			message << variousMessages() - null
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
			""			|	[]			||	""
			""			|	[12]		||	""
			"%s"		|	[12]		||	"12"
			"_%s_%d_%%"	|	[false, 42]	||	"_false_42_%"
	}

	def "add-message NPE"() {
		given:
			AssertBuilder builder = new AssertBuilder()

		when:
			builder.addMessage(null)
		then:
			thrown(NullPointerException)

		when:
			builder.addMessage(null, 1, 2, 3)
		then:
			thrown(NullPointerException)

			when:
			builder.addMessage("%s", [null])
		then:
			notThrown(NullPointerException)
	}

	def "checkThat with message and cause"() {
		given:
			Matcher matcher = Mock()
			AssertBuilder builder = new AssertBuilder()
			def cause = new RuntimeException()

		when:
			builder.checkThat(42, matcher, "msg", cause)
		then:
			1 * matcher.matches(42) >> true
		and:
			0 * matcher._
			builder.getAssertResults().isEmpty()

		when:
			builder.checkThat(42, matcher, "msg", cause)
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
			res.getCause().is(cause)
	}

	def "checkThat with message"() {
		given:
			Matcher matcher = Mock()
			AssertBuilder builder = new AssertBuilder()

		when:
			builder.checkThat(42, matcher, "msg")
		then:
			1 * matcher.matches(42) >> true
		and:
			0 * matcher._
			builder.getAssertResults().isEmpty()

		when:
			builder.checkThat(42, matcher, "msg")
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
			builder.checkThat(42, matcher)
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
			res.getMessage() == null
			res.getCause() == null
	}

	def "checkThat for throwable adds cause"() {
		given:
			Matcher matcher = Mock()
			AssertBuilder builder = new AssertBuilder()
			def actual = new IllegalArgumentException()

		when:
			builder.checkThat(actual, matcher)
		then:
			1 * matcher.matches(actual) >> false
		and:
			1 * matcher.describeTo(_) >> {it[0].appendText("qweqwe")}
		and:
			builder.getAssertResults().size() == 1
			def res = builder.getAssertResults()[0]
		and:
			res.getActual() == actual
			res.getExpected() == "qweqwe"
			res.getMessage() == null
			res.getCause().is(actual)
	}
}
