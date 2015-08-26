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

	def "matches"() {

		given: "a delegate matcher"
			Matcher matcher = Mock()

		when: "consistency matcher is create with a 'value' and a delegate"
			def m = new ConsistencyMatcher("value", matcher)
		then: "delegate is immediately asked to match the value 'value'"
			1 * matcher.matches("value") >> initialMatch

		when: "consistency matcher is asked to match an 'item'"
			def res1 = m.matches("item1")
		then: "delegate is also asked to match the 'item'"
			1 * matcher.matches("item1") >> initialMatch
		and: "if 'item' is matched the same way as 'value' - result is true"
			res1 == true

		when:
			def res2 = m.matches("item2")
		then:
			1 * matcher.matches("item2") >> !initialMatch
		and: "if 'item' is matched NOT the same way as 'value' - result is negative"
			res2 == false

		where:
			initialMatch << [true, false]
	}
}
