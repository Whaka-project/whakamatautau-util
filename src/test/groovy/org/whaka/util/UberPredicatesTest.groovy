package org.whaka.util

import java.util.function.BiPredicate
import java.util.function.BooleanSupplier
import java.util.function.Consumer
import java.util.function.Predicate

import org.whaka.util.UberPredicates.DistinctPredicateProxy

import spock.lang.Specification

class UberPredicatesTest extends Specification {

	def "counter"() {
		given:
			Predicate pred = UberPredicates.counter(3, value)
		expect:
			pred.test("qwe") == value
			pred.test("rty") == value
			pred.test("qaz") == value
			pred.test("pop") == !value
			pred.test("zaz") == !value
		where:
			value << [true, false]
	}

	def "peek"() {
		given:
			Consumer consumer = Mock()
		and:
			Predicate predicate = UberPredicates.peek(consumer, result)
		when:
			def res = predicate.test("qwe")
		then:
			1 * consumer.accept("qwe")
		and:
			res == result
		where:
			result << [true, false]
	}

	def "from supplier"() {
		given:
			BooleanSupplier sup = Mock()
		and:
			Predicate pred = UberPredicates.fromSupplier(sup)
		when:
			def res = pred.test("qwe")
		then:
			1 * sup.getAsBoolean() >> result
		and:
			res == result
		where:
			result << [true, false]
	}

	def "not"() {
		given:
			Predicate<Object> delegate = Mock()
			Predicate<Object> not = UberPredicates.not(delegate)
			def result = null

		when:
			result = not.test("qwe")
		then:
			1 * delegate.test("qwe") >> true
			result == false

		when:
			result = not.test(12)
		then:
			1 * delegate.test(12) >> false
			result == true
	}

	def "any-of"() {
		given:
			Predicate<Object> mock1 = Mock()
			Predicate<Object> mock2 = Mock()
			Predicate<Object> mock3 = Mock()
			Predicate<Object> anyOf = UberPredicates.anyOf([mock1, mock2, mock3])
			def result = null

		when: result = anyOf.test("qwe")
		then: 1 * mock1.test("qwe") >> false
		and:  1 * mock2.test("qwe") >> false
		and:  1 * mock3.test("qwe") >> false
		and:  result == false

		when: result = anyOf.test(12)
		then: 1 * mock1.test(12) >> false
		and:  1 * mock2.test(12) >> true
		and:  0 * _._
		and:  result == true

		when: result = anyOf.test(false)
		then: 1 * mock1.test(false) >> true
		and:  0 * _._
		and:  result == true

		when: "any-of built upon empty collection"
			Predicate<Object> emptyAnyOf = UberPredicates.anyOf([])
		then: "predicate alway returns 'false', cuz NOT A SINGLE DELEGATE RETURNES 'true'"
			emptyAnyOf.test("qwe") == false
	}

	def "all-of"() {
		given:
			Predicate<Object> mock1 = Mock()
			Predicate<Object> mock2 = Mock()
			Predicate<Object> mock3 = Mock()
			Predicate<Object> allOf = UberPredicates.allOf([mock1, mock2, mock3])
			def result = null

		when: result = allOf.test("qwe")
		then: 1 * mock1.test("qwe") >> true
		and:  1 * mock2.test("qwe") >> true
		and:  1 * mock3.test("qwe") >> true
		and:  result == true

		when: result = allOf.test(12)
		then: 1 * mock1.test(12) >> true
		and:  1 * mock2.test(12) >> true
		and:  1 * mock3.test(12) >> false
		and:  result == false

		when: result = allOf.test(false)
		then: 1 * mock1.test(false) >> true
		and:  1 * mock2.test(false) >> false
		and:  0 * _._
		and:  result == false

		when: result = allOf.test(null)
		then: 1 * mock1.test(null) >> false
		and:  0 * _._
		and:  result == false

		when: "all-of built upon empty collection"
			Predicate<Object> emptyAllOf = UberPredicates.allOf([])
		then: "Predicate always returns 'true', CUZ NOT A SINGLE DELEGATE RETURNS 'false' "
			emptyAllOf.test("qwe") == true
	}

	def "none-of"() {
		given:
			Predicate<Object> mock1 = Mock()
			Predicate<Object> mock2 = Mock()
			Predicate<Object> mock3 = Mock()
			Predicate<Object> noneOf = UberPredicates.noneOf([mock1, mock2, mock3])
			def result = null

		when: result = noneOf.test("qwe")
		then: 1 * mock1.test("qwe") >> false
		and:  1 * mock2.test("qwe") >> false
		and:  1 * mock3.test("qwe") >> false
		and:  result == true

		when: result = noneOf.test(12)
		then: 1 * mock1.test(12) >> false
		and:  1 * mock2.test(12) >> false
		and:  1 * mock3.test(12) >> true
		and:  result == false

		when: result = noneOf.test(false)
		then: 1 * mock1.test(false) >> false
		and:  1 * mock2.test(false) >> true
		and:  0 * _._
		and:  result == false

		when: result = noneOf.test(null)
		then: 1 * mock1.test(null) >> true
		and:  0 * _._
		and:  result == false

		when: "none-of built upon empty collection"
			Predicate<Object> emptyNoneOf = UberPredicates.noneOf([])
		then: "Predicate always returns 'true', CUZ NOT A SINGLE DELEGATE RETURNS 'false' "
			emptyNoneOf.test("qwe") == true
	}

	def "distinct proxy"() {

		given:
			Predicate<Object> delegate = Mock()
			DistinctPredicateProxy<Object> proxy = new DistinctPredicateProxy<>(delegate)
			def result = null

		when: "proxy is called for the first time"
			result = proxy.test(null)
		then: "delegate is called, whatever the argument was passed"
			1 * delegate.test(null) >> true
		and: "result is the result of the delegate"
			result == true

		when: "proxy is called with the same argument again"
			result = proxy.test(null)
		then: "delegate is not called"
			0 * delegate.test(_)
		and: "previous result is returned"
			result == true

		when:
			proxy.test(42)
			proxy.test(42)
			proxy.test(42)
			proxy.test(42)
			result = proxy.test(42)
		then:
			1 * delegate.test(42) >> false
		and:
			result == false

		when:
			result = proxy.test(null)
		then:
			1 * delegate.test(null) >> false
		and:
			result == false

		when: "equal but 'distinct' object are passed"
			proxy.test(new String("qwe"))
			proxy.test(new String("qwe"))
		then: "delegate is called twice, cuz by default reference equality is used"
			2 * delegate.test("qwe")
	}

	def "distinct proxy with predicate"() {

		given:
			Predicate<Object> delegate = Mock()
			BiPredicate<Object, Object> distinctionPredicate = Mock()
			DistinctPredicateProxy<Object> proxy = new DistinctPredicateProxy<>(delegate, distinctionPredicate)
			def result = null

		when: "proxy is called first time"
			result = proxy.test("qwe")
		then: "predicate is not called, for there's no previous value"
			0 * distinctionPredicate.test(_, _)
		and: "delegate is called with the same parameter"
			1 * delegate.test("qwe") >> false
		and:
			result == false

		when: "proxy is called again"
			result = proxy.test(42)
		then: "distinction predicate is called, and should return TRUE IF object are DISTINCT"
			1 * distinctionPredicate.test("qwe", 42) >> true
		and: "delegate is called if predicate returns true"
			1 * delegate.test(42) >> true
		and:
			result == true

		when: "proxy called again with the same value"
			result = proxy.test(42)
		then: "predicate returns false, indicating that object ARE NOT distinct"
			1 * distinctionPredicate.test(42, 42) >> false
		and: "delegate is not called"
			0 * delegate.test(_)
		and:
			result == true

		when: "proxy called with the same value again"
			result = proxy.test(42)
		then: "predicate can return true"
			1 * distinctionPredicate.test(42, 42) >> true
		and: "delegate will be called"
			1 * delegate.test(42) >> false
		and:
			result == false
	}
}
