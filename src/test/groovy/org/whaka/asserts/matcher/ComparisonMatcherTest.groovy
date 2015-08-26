package org.whaka.asserts.matcher

import org.whaka.TestData
import org.whaka.asserts.ComparisonAssertResult
import org.whaka.util.reflection.comparison.ComparisonFail
import org.whaka.util.reflection.comparison.ComparisonPerformer
import org.whaka.util.reflection.comparison.ComparisonResult

import spock.lang.Specification

class ComparisonMatcherTest extends Specification {

	def "construction"() {
		given:
			ComparisonPerformer performer = Mock()
		when:
			def m = new ComparisonMatcher(value, performer)
		then:
			0 * performer.compare(_, _)
		and:
			m.getValue().is(value)
			m.getComparisonPerformer().is(performer)
		where:
			value << TestData.variousObjects()
	}

	def "matches: success"() {
		given:
			ComparisonPerformer performer = Mock()
			ComparisonResult comparisonResult = new ComparisonResult(null, null, null, true)
			def m = new ComparisonMatcher("value", performer)
		when:
			ComparisonAssertResult res = m.matches("item", "msg " + cause, cause)
		then:
			1 * performer.compare("item", "value") >> comparisonResult
		and:
			res == null
		where:
			cause << TestData.variousCauses()
	}

	def "matches: fail"() {
		given:
			ComparisonPerformer performer = Mock()
			ComparisonResult comparisonResult = new ComparisonResult("item", "value", performer, false)
			def m = new ComparisonMatcher("value", performer)
		when:
			ComparisonAssertResult res = m.matches("item", "msg " + cause, cause)
		then:
			1 * performer.compare("item", "value") >> comparisonResult
		and:
			res.getComparisonResult().is(comparisonResult)
			res.getActual() == "item"
			res.getExpected() == "value"
			res.getMessage() == "msg $cause"
			res.getCause() == cause
		where:
			cause << TestData.variousCauses()
	}

	def "matches: if comparison fail happened - specified cause is ignored"() {
		given:
			ComparisonPerformer performer = Mock()
			def comparisonCause = new IOException("stahp!")
			ComparisonResult comparisonResult = new ComparisonFail(null, null, null, comparisonCause)
			def m = new ComparisonMatcher("value", performer)
		when:
			ComparisonAssertResult res = m.matches("item", "msg " + cause, cause)
		then:
			1 * performer.compare("item", "value") >> comparisonResult
		and:
			res.getComparisonResult().is(comparisonResult)
			res.getMessage() == "msg $cause"
			res.getCause() == comparisonCause
		where:
			cause << TestData.variousCauses()
	}
}
