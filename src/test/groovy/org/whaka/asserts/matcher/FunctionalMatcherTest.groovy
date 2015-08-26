package org.whaka.asserts.matcher

import java.util.function.Consumer
import java.util.function.Predicate

import org.hamcrest.Description

import spock.lang.Specification

class FunctionalMatcherTest extends Specification {

	def "construction"() {
		given:
			Predicate predicate = Mock()
			Consumer describer = Mock()
		when:
			def m = new FunctionalMatcher(type, predicate, describer)
		then:
			0 * predicate.test(_)
			0 * describer.accept(_)
		and:
			m.getType().is(type)
			m.getPredicate().is(predicate)
			m.getDescriber().is(describer)
		where:
			type << [String, Number, Boolean, FunctionalMatcherTest, Object, Void]
	}

	def "construction with string"() {
		given:
			Predicate predicate = Mock()
			Description description = Mock()

		when:
			def m = new FunctionalMatcher(String, predicate, "test description")
		then:
			0 * predicate.test(_)
		and:
			m.getType().is(String)
			m.getPredicate().is(predicate)

		when:
			m.describeTo(description)
		then:
			1 * description.appendText("test description")
	}

	def "matches: wrong instance"() {
		given:
			Predicate predicate = Mock()
			Consumer describer = Mock()
			def m = new FunctionalMatcher(type, predicate, describer)
		when:
			def res = m.matches(wrongInstance)
		then:
			0 * predicate.test(_)
			0 * describer.accept(_)
		and:
			res == false
		where:
			type			|	wrongInstance
			String			|	12
			String			|	false
			Integer			|	15.0
			Integer			|	"qwe"
			Boolean			|	42
			Boolean			|	"rty"
			Number			|	true
			Number			|	"qaz"
	}

	def "matches: right instance"() {
		given:
			Predicate predicate = Mock()
			Consumer describer = Mock()
			def m = new FunctionalMatcher(String, predicate, describer)
		when:
			def res = m.matches("item")
		then:
			1 * predicate.test("item") >> result
			0 * describer.accept(_)
		and:
			res == result
		where:
			result << [true, false]
	}

	def "matches: null is not tested for type"() {
		given:
			Predicate predicate = Mock()
			Consumer describer = Mock()
			def m = new FunctionalMatcher(type, predicate, describer)
		when:
			def res = m.matches(null as Object)
		then:
			1 * predicate.test(null) >> true
			0 * describer.accept(_)
		and:
			res == true
		where:
			type << [String, Number, Boolean, FunctionalMatcherTest, Object, Void]
	}

	def "describeTo"() {
		given:
			Predicate predicate = Mock()
			Consumer describer = Mock()
			Description description = Mock()
			def m = new FunctionalMatcher(String, predicate, describer)
		when:
			m.describeTo(description)
		then:
			0 * predicate.test(_)
			1 * describer.accept(description) >> {}
	}
}
