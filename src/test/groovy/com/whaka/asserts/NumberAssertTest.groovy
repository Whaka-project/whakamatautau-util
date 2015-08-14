package com.whaka.asserts

import static java.lang.Double.*
import spock.lang.Specification

import com.whaka.asserts.builder.NumberAssertPerformer

class NumberAssertTest extends Specification {

	private static final Double PINF = Double.POSITIVE_INFINITY
	private static final Double NINF = Double.NEGATIVE_INFINITY
	private static final Double MAX = Double.MAX_VALUE
	private static final Double MIN = Double.MIN_VALUE

	def "construction"() {
		when:
			NumberAssert _assert = new NumberAssert(actual)
		then:
			_assert.getActual() == actual
		where:
			actual << [0,1,-1,PINF,NINF,MAX,MIN,NaN,null,BigDecimal.TEN]
	}

	def "is-zero"() {
		when:
			new NumberAssert(0.0).isZero()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-15).isZero()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1).isZero()
		then:
			AssertError e1 = thrown()
			checkError(e1, 1, 0.0, NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS)

		when:
			new NumberAssert(null).isZero("Custom msg!")
		then:
			AssertError e2 = thrown()
			checkError(e2, null, 0.0, "Custom msg!")
	}

	def "is-equal"() {
		when:
			new NumberAssert(0.0).isEqual(-0.0)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-15).isEqual(1e-16)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(null).isEqual(null)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1).isEqual(NaN)
		then:
			AssertError e1 = thrown()
			checkError(e1, 1, NaN, NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS)

		when:
			new NumberAssert(null).isEqual(NaN, "Nananan!")
		then:
			AssertError e2 = thrown()
			checkError(e2, null, NaN, "Nananan!")
	}

	def "is-not-equal"() {
		when:
			new NumberAssert(0.0).isNotEqual(1)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-13).isNotEqual(1e-14)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(null).isNotEqual(NaN)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1).isNotEqual(1)
		then:
			AssertError e1 = thrown()
			checkError(e1, 1, "Not: 1", NumberAssertPerformer.MESSAGE_NOT_EQUAL_NUMBERS)

		when:
			new NumberAssert(null).isNotEqual(null, "qwe")
		then:
			AssertError e2 = thrown()
			checkError(e2, null, "Not: null", "qwe")
	}

	def "is-number"() {
		when:
			new NumberAssert(0.0).isNumber()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(PINF).isNumber()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(MIN).isNumber()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NaN).isNumber()
		then:
			AssertError e1 = thrown()
			checkError(e1, NaN, "Any number", NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS)

		when:
			new NumberAssert(null).isNumber("rty")
		then:
			AssertError e2 = thrown()
			checkError(e2, null, "Any number", "rty")
	}

	def "is-finite"() {
		when:
			new NumberAssert(0.0).isFinite()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(MAX).isFinite()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-15).isFinite()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NaN).isFinite()
		then:
			AssertError e1 = thrown()
			checkError(e1, NaN, "Any finite number", NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS)

		when:
			new NumberAssert(PINF).isFinite()
		then:
			AssertError e2 = thrown()
			checkError(e2, PINF, "Any finite number", NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS)

		when:
			new NumberAssert(null).isFinite("qaz")
		then:
			AssertError e3 = thrown()
			checkError(e3, null, "Any finite number", "qaz")
	}

	def "is-positive"() {
		when:
			new NumberAssert(1).isPositive()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-14).isPositive()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(PINF).isPositive()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NaN).isPositive()
		then:
			AssertError e1 = thrown()
			checkError(e1, NaN, "Any positive number", NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS)

		when:
			new NumberAssert(1e-15).isPositive()
		then:
			AssertError e2 = thrown()
			checkError(e2, 1e-15, "Any positive number", NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS)

		when:
			new NumberAssert(null).isPositive("zzz")
		then:
			AssertError e3 = thrown()
			checkError(e3, null, "Any positive number", "zzz")
	}

	def "is-negative"() {
		when:
			new NumberAssert(-1).isNegative()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(-1e-14).isNegative()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NINF).isNegative()
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NaN).isNegative()
		then:
			AssertError e1 = thrown()
			checkError(e1, NaN, "Any negative number", NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS)

		when:
			new NumberAssert(1e-15).isNegative()
		then:
			AssertError e2 = thrown()
			checkError(e2, 1e-15, "Any negative number", NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS)

		when:
			new NumberAssert(null).isNegative("So negative!")
		then:
			AssertError e3 = thrown()
			checkError(e3, null, "Any negative number", "So negative!")
	}

	def "is-greater-than"() {
		when:
			new NumberAssert(1).isGreaterThan(0.0)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-14).isGreaterThan(0.0)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(PINF).isGreaterThan(MAX)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NaN).isGreaterThan(PINF)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NINF).isGreaterThan(PINF)
		then:
			AssertError e1 = thrown()
			checkError(e1, NINF, ">Infinity", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(NaN).isGreaterThan(NaN)
		then:
			AssertError e2 = thrown()
			checkError(e2, NaN, ">NaN", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(1e-15).isGreaterThan(0.0)
		then:
			AssertError e3 = thrown()
			checkError(e3, 1e-15, ">0.0", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(null).isGreaterThan(1)
		then:
			AssertError e4 = thrown()
			checkError(e4, null, ">1", NumberAssertPerformer.MESSAGE_INCOMPARABLE_NUMBERS)

		when:
			new NumberAssert(null).isGreaterThan(null, "hehehe")
		then:
			AssertError e5 = thrown()
			checkError(e5, null, ">null", "hehehe")
	}

	def "is-lower-than"() {
		when:
			new NumberAssert(-1).isLowerThan(0.0)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-14).isLowerThan(1e-13)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(PINF).isLowerThan(NaN)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NINF).isLowerThan(MIN)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(PINF).isLowerThan(NINF)
		then:
			AssertError e1 = thrown()
			checkError(e1, PINF, "<-Infinity", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(NaN).isLowerThan(NaN)
		then:
			AssertError e2 = thrown()
			checkError(e2, NaN, "<NaN", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(-1e-15).isLowerThan(0.0)
		then:
			AssertError e3 = thrown()
			checkError(e3, -1e-15, "<0.0", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(null).isLowerThan(1)
		then:
			AssertError e4 = thrown()
			checkError(e4, null, "<1", NumberAssertPerformer.MESSAGE_INCOMPARABLE_NUMBERS)

		when:
			new NumberAssert(null).isLowerThan(null, "hahaha")
		then:
			AssertError e5 = thrown()
			checkError(e5, null, "<null", "hahaha")
	}

	def "is-greater-than-or-equal"() {
		when:
			new NumberAssert(1).isGreaterThanOrEqual(0.0)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(-1e-15).isGreaterThanOrEqual(0.0)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(PINF).isGreaterThanOrEqual(MAX)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NaN).isGreaterThanOrEqual(NaN)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NINF).isGreaterThanOrEqual(PINF)
		then:
			AssertError e1 = thrown()
			checkError(e1, NINF, ">=Infinity", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(0.0).isGreaterThanOrEqual(1)
		then:
			AssertError e2 = thrown()
			checkError(e2, 0.0, ">=1", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(-1e-14).isGreaterThanOrEqual(0.0)
		then:
			AssertError e3 = thrown()
			checkError(e3, -1e-14, ">=0.0", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(null).isGreaterThanOrEqual(1)
		then:
			AssertError e4 = thrown()
			checkError(e4, null, ">=1", NumberAssertPerformer.MESSAGE_INCOMPARABLE_NUMBERS)

		when:
			new NumberAssert(null).isGreaterThanOrEqual(null, "huhuhu")
		then:
			AssertError e5 = thrown()
			checkError(e5, null, ">=null", "huhuhu")
	}

	def "is-lower-than-or-equal"() {
		when:
			new NumberAssert(-1).isLowerThanOrEqual(0.0)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-14).isLowerThanOrEqual(1e-13)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-15).isLowerThanOrEqual(0.0)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NaN).isLowerThanOrEqual(NaN)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NINF).isLowerThanOrEqual(MIN)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(PINF).isLowerThanOrEqual(NINF)
		then:
			AssertError e1 = thrown()
			checkError(e1, PINF, "<=-Infinity", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(NaN).isLowerThanOrEqual(PINF)
		then:
			AssertError e2 = thrown()
			checkError(e2, NaN, "<=Infinity", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(1e-14).isLowerThanOrEqual(0.0)
		then:
			AssertError e3 = thrown()
			checkError(e3, 1e-14, "<=0.0", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(null).isLowerThanOrEqual(1)
		then:
			AssertError e4 = thrown()
			checkError(e4, null, "<=1", NumberAssertPerformer.MESSAGE_INCOMPARABLE_NUMBERS)

		when:
			new NumberAssert(null).isLowerThanOrEqual(null, "hihihi")
		then:
			AssertError e5 = thrown()
			checkError(e5, null, "<=null", "hihihi")
	}

	def "is-between"() {
		when:
			new NumberAssert(0).isBetween(-1, 1)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-14).isBetween(0, 1)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(PINF).isBetween(MAX, NaN)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(-MAX).isBetween(NINF, 0)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(10).isBetween(0, 1)
		then:
			AssertError e1 = thrown()
			checkError(e1, 10, "0<>1", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(NaN).isBetween(NINF, PINF)
		then:
			AssertError e2 = thrown()
			checkError(e2, NaN, "-Infinity<>Infinity", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(1e-15).isBetween(0, 1)
		then:
			AssertError e3 = thrown()
			checkError(e3, 1e-15, "0<>1", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(null).isBetween(1, 0)
		then:
			AssertError e4 = thrown()
			checkError(e4, null, "1<>0", NumberAssertPerformer.MESSAGE_INCOMPARABLE_NUMBERS)

		when:
			new NumberAssert(null).isBetween(null, null, "hohoho")
		then:
			AssertError e5 = thrown()
			checkError(e5, null, "null<>null", "hohoho")
	}

	def "is-between-or-equal"() {
		when:
			new NumberAssert(0).isBetweenOrEqual(0, 1)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-14).isBetweenOrEqual(1e-14, 1e-13)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(1e-15).isBetweenOrEqual(0, 1e-16)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(NaN).isBetweenOrEqual(NaN, NaN)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(-MAX).isBetweenOrEqual(NINF, -MAX)
		then:
			notThrown(AssertError)

		when:
			new NumberAssert(10).isBetweenOrEqual(0, 1)
		then:
			AssertError e1 = thrown()
			checkError(e1, 10, "0<=>1", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(NaN).isBetweenOrEqual(NINF, PINF)
		then:
			AssertError e2 = thrown()
			checkError(e2, NaN, "-Infinity<=>Infinity", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(-1e-14).isBetweenOrEqual(0, 1)
		then:
			AssertError e3 = thrown()
			checkError(e3, -1e-14, "0<=>1", NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL)

		when:
			new NumberAssert(null).isBetweenOrEqual(1, 0)
		then:
			AssertError e4 = thrown()
			checkError(e4, null, "1<=>0", NumberAssertPerformer.MESSAGE_INCOMPARABLE_NUMBERS)

		when:
			new NumberAssert(null).isBetweenOrEqual(null, null, "eheheh")
		then:
			AssertError e5 = thrown()
			checkError(e5, null, "null<=>null", "eheheh")
	}

	private void checkError(AssertError e, Object actual, Object expected, String message) {
		assert e.getResults().size() == 1
		AssertResult result = e.getResults()[0]
		assert result.getActual() == actual
		assert result.getExpected() == expected
		assert result.getMessage() == message
	}
}
