package org.whaka.asserts

import org.hamcrest.Matcher
import org.whaka.TestData

import spock.lang.Specification

class AssertTest extends Specification {

	def "fail"() {
		when:
			Assert.fail(message)
		then:
			AssertError e = thrown()
			e.getResults().size() == 1
		and:
			AssertResult result = e.getResults()[0]
			result.getActual() == null
			result.getExpected() == null
			result.getMessage() == message
			result.getCause() == null
		where:
			message << TestData.variousMessages() - null
	}

	def "fail with format"() {
		when:
			Assert.fail(message, (Object[]) arguments)
		then:
			AssertError e = thrown()
			e.getResults().size() == 1
		and:
			AssertResult result = e.getResults()[0]
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

	def "fail NPE"() {
		when:
			Assert.fail(null)
		then:
			thrown(NullPointerException)

		when:
			Assert.fail(null, 1, 2, 3)
		then:
			thrown(NullPointerException)

		when:
			Assert.fail("%s", [null])
		then:
			thrown(AssertError)
	}

	def "builder"() {
		expect:
			Assert.builder().is(Assert.builder()) == false
			Assert.builder() != null
	}

	def "assertThat with message and cause"() {
		given:
			Matcher matcher = Mock()
			def cause = new RuntimeException()

		when:
			Assert.assertThat(42, matcher, "msg", cause)
		then:
			1 * matcher.matches(42) >> true
		and:
			notThrown(AssertError)

		when:
			Assert.assertThat(42, matcher, "msg", cause)
		then:
			1 * matcher.matches(42) >> false
		and:
			1 * matcher.describeTo(_) >> {it[0].appendText("test test")}
		and:
			AssertError error = thrown()
		and:
			error.getResults().size() == 1
			def res = error.getResults()[0]
		and:
			res.getActual() == 42
			res.getExpected() == "test test"
			res.getMessage() == "msg"
			res.getCause().is(cause)
	}

	def "assertThat with message"() {
		given:
			Matcher matcher = Mock()

		when:
			Assert.assertThat(42, matcher, "msg")
		then:
			1 * matcher.matches(42) >> true
		and:
			notThrown(AssertError)

		when:
			Assert.assertThat(42, matcher, "msg")
		then:
			1 * matcher.matches(42) >> false
		and:
			1 * matcher.describeTo(_) >> {it[0].appendText("test test")}
		and:
			AssertError error = thrown()
		and:
			error.getResults().size() == 1
			def res = error.getResults()[0]
		and:
			res.getActual() == 42
			res.getExpected() == "test test"
			res.getMessage() == "msg"
			res.getCause() == null
	}

	def "assertThat"() {
		given:
			Matcher matcher = Mock()

		when:
			Assert.assertThat(42, matcher)
		then:
			1 * matcher.matches(42) >> true
		and:
			notThrown(AssertError)

		when:
			Assert.assertThat(42, matcher)
		then:
			1 * matcher.matches(42) >> false
		and:
			1 * matcher.describeTo(_) >> {it[0].appendText("test test")}
		and:
			AssertError error = thrown()
		and:
			error.getResults().size() == 1
			def res = error.getResults()[0]
		and:
			res.getActual() == 42
			res.getExpected() == "test test"
			res.getMessage() == null
			res.getCause() == null
	}
}
