package org.whaka.asserts.matcher

import org.hamcrest.Matcher
import org.whaka.TestData

import spock.lang.Specification

class ConsistencyMatcherTest extends Specification {

	def "construction"() {
		given:
			Matcher matcher = Mock()

		when:
			def mPositive = new ConsistencyMatcher(value, matcher)
		then:
			1 * matcher.matches(value) >> true
		and:
			mPositive.getValue().is(value)
			mPositive.getMatcher().is(matcher)

		when:
			def mNegative = new ConsistencyMatcher(value, matcher)
		then:
			1 * matcher.matches(value) >> false
		and:
			mNegative.getValue().is(value)
			mNegative.getMatcher().is(matcher)

		where:
			value << TestData.variousObjects()
	}

	def "matches: itself"() {
		given:
			Matcher matcherPositive = Mock()
			matcherPositive.matches(value) >> true
		and:
			Matcher matcherNegative = Mock()
			matcherNegative.matches(value) >> false
		and:
			def mPositive = new ConsistencyMatcher(value, matcherPositive)
			def mNegative = new ConsistencyMatcher(value, matcherNegative)

		when:
			def res1 = mPositive.matches(value)
		then:
			1 * matcherPositive.matches(value) >> true
		and:
			res1 == true

		when:
			def res2 = mNegative.matches(value)
		then:
			1 * matcherNegative.matches(value) >> false
		and:
			res2 == true

		where:
			value << TestData.variousObjects()
	}
}
