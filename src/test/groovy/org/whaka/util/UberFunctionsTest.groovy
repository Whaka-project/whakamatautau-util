package org.whaka.util

import java.util.function.BiPredicate
import java.util.function.Function

import spock.lang.Specification

import org.whaka.util.UberFunctions.DistinctFunctionProxy

class UberFunctionsTest extends Specification {

	def "distinct proxy"() {

		given:
			Function<Object, Object> delegate = Mock()
			DistinctFunctionProxy<Object> proxy = new DistinctFunctionProxy<>(delegate)
			def result = null

		when: "proxy is called for the first time"
			result = proxy.apply(null)
		then: "delegate is called, whatever the argument was passed"
			1 * delegate.apply(null) >> "true"
		and: "result is the result of the delegate"
			result == "true"

		when: "proxy is called with the same argument again"
			result = proxy.apply(null)
		then: "delegate is not called"
			0 * delegate.apply(_)
		and: "previous result is returned"
			result == "true"

		when:
			proxy.apply(42)
			proxy.apply(42)
			proxy.apply(42)
			proxy.apply(42)
			result = proxy.apply(42)
		then:
			1 * delegate.apply(42) >> 12
		and:
			result == 12

		when:
			result = proxy.apply(null)
		then:
			1 * delegate.apply(null) >> false
		and:
			result == false

		when: "equal but 'distinct' object are passed"
			proxy.apply(new String("qwe"))
			proxy.apply(new String("qwe"))
		then: "delegate is called twice, cuz by default reference equality is used"
			2 * delegate.apply("qwe")
	}

	def "distinct proxy with predicate"() {

		given:
			Function<Object, Object> delegate = Mock()
			BiPredicate<Object, Object> distinctionPredicate = Mock()
			DistinctFunctionProxy<Object, Object> proxy = new DistinctFunctionProxy<>(delegate, distinctionPredicate)
			def result = null

		when: "proxy is called first time"
			result = proxy.apply("qwe")
		then: "predicate is not called, for there's no previous value"
			0 * distinctionPredicate.test(_, _)
		and: "delegate is called with the same parameter"
			1 * delegate.apply("qwe") >> 12
		and:
			result == 12

		when: "proxy is called again"
			result = proxy.apply(42)
		then: "distinction predicate is called, and should return TRUE IF object are DISTINCT"
			1 * distinctionPredicate.test("qwe", 42) >> true
		and: "delegate is called if predicate returns true"
			1 * delegate.apply(42) >> "str"
		and:
			result == "str"

		when: "proxy called again with the same value"
			result = proxy.apply(42)
		then: "predicate returns false, indicating that object ARE NOT distinct"
			1 * distinctionPredicate.test(42, 42) >> false
		and: "delegate is not called"
			0 * delegate.apply(_)
		and:
			result == "str"

		when: "proxy called with the same value again"
			result = proxy.apply(42)
		then: "predicate can return true"
			1 * distinctionPredicate.test(42, 42) >> true
		and: "delegate will be called"
			1 * delegate.apply(42) >> false
		and:
			result == false
	}
}
