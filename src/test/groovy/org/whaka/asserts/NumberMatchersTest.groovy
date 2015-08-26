package org.whaka.asserts

import java.util.function.BiFunction
import java.util.function.Function
import java.util.function.Supplier

import org.hamcrest.Matcher

import spock.lang.Specification

class NumberMatchersTest extends Specification {

	private static final Double PINF = Double.POSITIVE_INFINITY
	private static final Double NINF = Double.NEGATIVE_INFINITY
	private static final Double MAX = Double.MAX_VALUE
	private static final Double MIN = Double.MIN_VALUE
	private static final Double NAN = Double.NaN

	def "invariant instances are memoized"() {
		given:
			Supplier<Matcher> getter = _getter
			com.google.common.base.Supplier<Matcher> supplier = _supplier
		expect:
			getter.get().is(getter.get())
			getter.get().is(supplier.get())
		where:
			_getter						|	_supplier
			NumberMatchers.&number		|	NumberMatchers.IS_NUMBER
			NumberMatchers.&finite		|	NumberMatchers.IS_FINITE
			NumberMatchers.&zero		|	NumberMatchers.IS_ZERO
			NumberMatchers.&positive	|	NumberMatchers.IS_POSITIVE
			NumberMatchers.&negative	|	NumberMatchers.IS_NEGATIVE
	}

	def "comparison NPE"() {
		given:
			Function<Number, Matcher> factory = _factory
		when:
			factory.apply(null as Number)
		then:
			thrown(NullPointerException)
		where:
			_factory << [
				NumberMatchers.&greaterThan,
				NumberMatchers.&lowerThan,
				NumberMatchers.&greaterThanOrEqual,
				NumberMatchers.&lowerThanOrEqual,
			]
	}

	def "bicomparison NPE"() {
		given:
			BiFunction<Number, Number, Matcher> factory = _factory

		when:
			factory.apply(null as Number, 42)
		then:
			thrown(NullPointerException)

		when:
			factory.apply(42, null as Number)
		then:
			thrown(NullPointerException)

		when:
			factory.apply(null as Number, null as Number)
		then:
			thrown(NullPointerException)

		where:
			_factory << [
				NumberMatchers.&between,
				NumberMatchers.&betweenOrEqual,
			]
	}

	def "bicomparison: min <= max assert"() {
		given:
			BiFunction<Number, Number, Matcher> factory = _factory

		when:
			factory.apply(41, 42)
		then:
			notThrown()

		when:
			factory.apply(42, 42)
		then:
			notThrown()

		when:
			factory.apply(43, 42)
		then:
			thrown(IllegalArgumentException)

		where:
			_factory << [
				NumberMatchers.&between,
				NumberMatchers.&betweenOrEqual,
			]
	}

	def "number"() {
		given:
			Matcher m = NumberMatchers.number()
		expect:
			m.matches(item as Double) == result
		where:
			item		|	result
			0			|	true
			-10000000	|	true
			+10000000	|	true
			MAX			|	true
			MIN			|	true
			PINF		|	true
			NINF		|	true
			NAN			|	false
			null		|	false
	}

	def "finite"() {
		given:
			Matcher m = NumberMatchers.finite()
		expect:
			m.matches(item as Double) == result
		where:
			item		|	result
			0			|	true
			-10000000	|	true
			+10000000	|	true
			MAX			|	true
			MIN			|	true
			PINF		|	false
			NINF		|	false
			NAN			|	false
			null		|	false
	}

	def "positive"() {
		given:
			Matcher m = NumberMatchers.positive()
		expect:
			m.matches(item as Number) == result
		where:
			item		|	result
			0			|	false
			-10000000	|	false
			+10000000	|	true
			MAX			|	true
			MIN			|	false
			-MAX		|	false
			-MIN		|	false
			PINF		|	true
			NINF		|	false
			NAN			|	false
			null		|	false
	}

	def "negative"() {
		given:
			Matcher m = NumberMatchers.negative()
		expect:
			m.matches(item as Number) == result
		where:
			item		|	result
			0			|	false
			-10000000	|	true
			+10000000	|	false
			MAX			|	false
			MIN			|	false
			-MAX		|	true
			-MIN		|	false
			PINF		|	false
			NINF		|	true
			NAN			|	false
			null		|	false
	}

	def "zero"() {
		given:
			Matcher m = NumberMatchers.zero()
		expect:
			m.matches(item as Number) == result
		where:
			item		|	result
			0			|	true
			-10000000	|	false
			+10000000	|	false
			MAX			|	false
			MIN			|	true
			-MAX		|	false
			-MIN		|	true
			PINF		|	false
			NINF		|	false
			NAN			|	false
			null		|	false
	}

	def "equalTo"() {
		given:
			Matcher m = NumberMatchers.equalTo(value as Number)
		expect:
			m.matches(item as Number) == result
		where:
			item				|	value			||	result
			0					|	0				||	true
			MIN					|	0				||	true
			-MIN				|	0				||	true
			NINF				|	PINF			||	false
			PINF				|	NINF			||	false
			NAN					|	0				||	false
			MAX					|	PINF			||	false
			-MAX				|	NINF			||	false
			42.0 as BigDecimal	|	42 as Long		||	true
	}

	def "greaterThan"() {
		given:
			Matcher m = NumberMatchers.greaterThan(value as Number)
		expect:
			m.matches(item as Number) == result
		where:
			item				|	value			||	result
			0					|	0				||	false
			MIN					|	0				||	false
			-MIN				|	0				||	false
			NINF				|	PINF			||	false
			PINF				|	NINF			||	true
			NAN					|	0				||	true
			MAX					|	PINF			||	false
			PINF				|	MAX				||	true
			-MAX				|	NINF			||	true
			MAX					|	MIN				||	true
			42.0 as BigDecimal	|	41 as Long		||	true
			41.0 as Float		|	42 as Short		||	false
			42.0 as Double		|	42 as Byte		||	false
	}

	def "lowerThan"() {
		given:
			Matcher m = NumberMatchers.lowerThan(value as Number)
		expect:
			m.matches(item as Number) == result
		where:
			item				|	value			||	result
			0					|	0				||	false
			MIN					|	0				||	false
			-MIN				|	0				||	false
			NINF				|	PINF			||	true
			PINF				|	NINF			||	false
			NAN					|	0				||	false
			MAX					|	PINF			||	true
			PINF				|	MAX				||	false
			-MAX				|	NINF			||	false
			MAX					|	MIN				||	false
			42.0 as BigDecimal	|	41 as Long		||	false
			41.0 as Float		|	42 as Short		||	true
			42.0 as Double		|	42 as Byte		||	false
	}

	def "greaterThanOrEqual"() {
		given:
			Matcher m = NumberMatchers.greaterThanOrEqual(value as Number)
		expect:
			m.matches(item as Number) == result
		where:
			item				|	value			||	result
			0					|	0				||	true
			MIN					|	0				||	true
			-MIN				|	0				||	true
			NINF				|	PINF			||	false
			PINF				|	NINF			||	true
			NAN					|	0				||	true
			MAX					|	PINF			||	false
			PINF				|	MAX				||	true
			-MAX				|	NINF			||	true
			MAX					|	MIN				||	true
			42.0 as BigDecimal	|	41 as Long		||	true
			41.0 as Float		|	42 as Short		||	false
			42.0 as Double		|	42 as Byte		||	true
	}

	def "lowerThanOrEqual"() {
		given:
			Matcher m = NumberMatchers.lowerThanOrEqual(value as Number)
		expect:
			m.matches(item as Number) == result
		where:
			item				|	value			||	result
			0					|	0				||	true
			MIN					|	0				||	true
			-MIN				|	0				||	true
			NINF				|	PINF			||	true
			PINF				|	NINF			||	false
			NAN					|	0				||	false
			MAX					|	PINF			||	true
			PINF				|	MAX				||	false
			-MAX				|	NINF			||	false
			MAX					|	MIN				||	false
			42.0 as BigDecimal	|	41 as Long		||	false
			41.0 as Float		|	42 as Short		||	true
			42.0 as Double		|	42 as Byte		||	true
	}

	def "between"() {
		given:
			Matcher m = NumberMatchers.between(a as Number, b as Number)
		expect:
			m.matches(item as Number) == result
		where:
			item				|	a			|	b			||	result
			0					|	0			|	0			||	false
			0					|	0			|	1			||	false
			0					|	-1			|	0			||	false
			0					|	-1			|	1			||	true
			0					|	1			|	10			||	false
			0					|	MIN			|	MAX			||	false
			0					|	-MAX		|	MIN			||	false
			0					|	-MAX		|	MAX			||	true
			0					|	NINF		|	PINF		||	true
			0					|	-1			|	NAN			||	true
			PINF				|	MAX			|	NAN			||	true
			-MAX				|	NINF		|	0			||	true
	}

	def "betweenOrEqual"() {
		given:
			Matcher m = NumberMatchers.betweenOrEqual(a as Number, b as Number)
		expect:
			m.matches(item as Number) == result
		where:
			item				|	a			|	b			||	result
			0					|	0			|	0			||	true
			0					|	0			|	1			||	true
			0					|	-1			|	0			||	true
			0					|	-1			|	1			||	true
			0					|	1			|	10			||	false
			0					|	MIN			|	MAX			||	true
			0					|	-MAX		|	MIN			||	true
			0					|	-MAX		|	MAX			||	true
			0					|	NINF		|	PINF		||	true
			0					|	-1			|	NAN			||	true
			PINF				|	MAX			|	NAN			||	true
			-MAX				|	NINF		|	0			||	true
	}
}
