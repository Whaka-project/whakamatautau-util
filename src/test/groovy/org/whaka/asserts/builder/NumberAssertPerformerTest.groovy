package org.whaka.asserts.builder

import static java.lang.Double.*

import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.Predicate

import spock.lang.Specification

import org.whaka.asserts.AssertResult
import org.whaka.util.DoubleMath
import org.whaka.util.DoubleMathTest

class NumberAssertPerformerTest extends Specification {

	private static final Double PINF = Double.POSITIVE_INFINITY
	private static final Double NINF = Double.NEGATIVE_INFINITY
	private static final Double MAX = Double.MAX_VALUE
	private static final Double MIN = Double.MIN_VALUE

	def "construction"() {
		given:
			Consumer<?> consumer = Mock()
		when:
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		then:
			0 * consumer._
			performer.getActual().is(actual)
		where:
			actual << [null, 1, 12L, 12.0, 99999999999L, BigDecimal.TEN, PINF, NINF, MAX, MIN, NaN]
	}

	/*
	 * Data taken from the DoubleMathTest class.
	 * Tests rounding error comparison.
	 */
	def "is-equal success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isEqual(expected)
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			[actual, expected] << DoubleMathTest.dataDoubleValuesWithRoundingErrorsAndRoundingResult() + [[null,null]]
	}

	def "is-equal fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isEqual(expected)
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and:
			capturedResult.getActual().is(actual)
			capturedResult.getExpected().is(expected)
			capturedResult.getMessage() == NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual		|	expected
			1.0			|	null
			null		|	1.0
			1.0			|	2.0
			1.1			|	1.11
			NaN			|	1.0
			NaN			|	-1.0
			NaN			|	0.0
			PINF		|	MAX
			NINF		|	MIN
			NINF		|	MIN
	}

	/*
	 * Another data taken from the DoubleMathTest class.
	 * Tests minimal possible and first ignored value difference.
	 * Note: only test cases with 'expected comparison result' == 0 (equal numbers) are filtered
	 */
	def "is-equal success 2"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isEqual(expected)
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			[actual, expected] << DoubleMathTest.dataDoubleValuesAndCompareResult().findAll {it[2]==0}
	}

	/*
	 * Another data taken from the DoubleMathTest class.
	 * Tests minimal possible and first ignored value difference.
	 * Note: only test cases with 'expected comparison result' != 0 (NOT equal numbers) are filtered
	 */
	def "is-equal fail 2"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isEqual(expected)
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and:
			capturedResult.getActual().is(actual)
			capturedResult.getExpected().is(expected)
			capturedResult.getMessage() == NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			[actual, expected] << DoubleMathTest.dataDoubleValuesAndCompareResult().findAll {it[2]!=0}
	}

	def "is-not-equal fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNotEqual(expected)
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and:
			capturedResult.getActual().is(actual)
			capturedResult.getExpected() == "Not: $expected"
			capturedResult.getMessage() == NumberAssertPerformer.MESSAGE_NOT_EQUAL_NUMBERS
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			[actual, expected] << DoubleMathTest.dataDoubleValuesWithRoundingErrorsAndRoundingResult() + [[null,null]]
	}

	def "is-not-equal success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNotEqual(expected)
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			actual		|	expected
			1.0			|	null
			null		|	1.0
			1.0			|	2.0
			1.1			|	1.11
			NaN			|	1.0
			NaN			|	-1.0
			NaN			|	0.0
			PINF		|	MAX
			NINF		|	MIN
			NINF		|	MIN
	}

	def "is-not-equal fail 2"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNotEqual(expected)
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and:
			capturedResult.getActual().is(actual)
			capturedResult.getExpected() == "Not: $expected"
			capturedResult.getMessage() == NumberAssertPerformer.MESSAGE_NOT_EQUAL_NUMBERS
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			[actual, expected] << DoubleMathTest.dataDoubleValuesAndCompareResult().findAll {it[2]==0}
	}

	def "is-not-equal success 2"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNotEqual(expected)
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			[actual, expected] << DoubleMathTest.dataDoubleValuesAndCompareResult().findAll {it[2]!=0}
	}

	def "compare-result success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		and:
			Predicate<Integer> resultPredicate = Mock()
			Integer expectedComparisonResult = DoubleMath.compare(actual, expected)
		when:
			AssertResultConstructor messageConstructor =
				performer.performCompareResultCheck(expected, resultPredicate, message)
		then: "specified predicate receives result of the values comparison"
			resultPredicate.test(expectedComparisonResult) >> true
		and: "nothing happens when predicate returns true"
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			actual		|	expected		|	message
			1.0			|	1.0				|	"qwe"
			1.0			|	0.5				|	"rty"
			0.5			|	1.0				|	"qaz"
	}

	def "compare-result fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		and:
			Predicate<Integer> resultPredicate = Mock()
			Integer expectedComparisonResult = DoubleMath.compare(actual, expected)
		when:
			AssertResultConstructor messageConstructor =
				performer.performCompareResultCheck(expected, resultPredicate, message)
		then: "predicate receives comparison result"
			resultPredicate.test(expectedComparisonResult) >> false
		and: "if predicate returns false - consumer receives created assert result"
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and: "result contains specified 'message' as expected value"
			capturedResult.getActual().is(actual)
			capturedResult.getExpected().is(message)
			capturedResult.getMessage() == NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual		|	expected		|	message
			1.0			|	1.0				|	"qwe"
			1.0			|	0.5				|	"rty"
			0.5			|	1.0				|	"qaz"
	}

	def "compare-result nulls"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		and:
			Predicate<Integer> resultPredicate = Mock()
		when: "either actual or expected value is null"
			AssertResultConstructor messageConstructor =
				performer.performCompareResultCheck(expected, resultPredicate, message)
		then: "predicate isn't called"
			0 * resultPredicate.test(_)
		and: "consumer receives created assert result"
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and: "result contains message as expected value"
			capturedResult.getActual().is(actual)
			capturedResult.getExpected().is(message)
			capturedResult.getMessage() == NumberAssertPerformer.MESSAGE_INCOMPARABLE_NUMBERS
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual		|	expected		|	message
			null		|	null			|	"qwe"
			null		|	0.5				|	"rty"
			0.5			|	null			|	"qaz"
	}

	def "is-zero success"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isZero()
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			actual << [0, 0.0, -0.0, MIN, 1e-15]
	}

	def "is-zero fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isZero()
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and:
			capturedResult.getActual().is(actual)
			capturedResult.getExpected() == 0.0
			capturedResult.getMessage() == NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS
			capturedResult.getCause() == null
		where:
			actual << [1,-1,PINF,NINF,MAX,NaN,1e-14]
	}

	def "is-number success"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNumber()
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			actual << [0, 1.1, 12, PINF, NINF, MIN, MAX, 1e-14, 1e-15]
	}

	def "is-number fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isNumber()
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and:
			capturedResult.getActual().is(actual)
			capturedResult.getExpected() == "Any number"
			capturedResult.getMessage() == NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS
			capturedResult.getCause() == null
		where:
			actual << [null,NaN]
	}

	def "is-finite success"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isFinite()
		then:
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			actual << [0, 1.1, 12, MIN, MAX, 1e-14, 1e-15]
	}

	def "is-finite fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		when:
			AssertResultConstructor messageConstructor = performer.isFinite()
		then:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and:
			capturedResult.getActual().is(actual)
			capturedResult.getExpected() == "Any finite number"
			capturedResult.getMessage() == NumberAssertPerformer.MESSAGE_EQUAL_NUMBERS
			capturedResult.getCause() == null
		where:
			actual << [null,NaN,PINF,NINF]
	}

	def "compare-range success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		and:
			BiPredicate<Integer, Integer> rangePredicate = Mock()
			Integer expectedComparisonMin = DoubleMath.compare(actual, min)
			Integer expectedComparisonMax = DoubleMath.compare(actual, max)
		when:
			AssertResultConstructor messageConstructor =
				performer.performCompareResultRangeCheck(min, max, rangePredicate, message)
		then: "specified predicate receives result of the values comparison"
			rangePredicate.test(expectedComparisonMin, expectedComparisonMax) >> true
		and: "nothing happens when predicate returns true"
			0 * consumer.accept(_)
			messageConstructor.getAssertResult() == null
		where:
			actual		|	min		|	max		|	message
			1.0			|	1.0		|	1.0		|	"qwe"
			1.0			|	0.5		|	1.5		|	"rty"
			0.5			|	0.0		|	1.0		|	"qaz"
	}

	def "compare-range fail"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		and:
			BiPredicate<Integer, Integer> rangePredicate = Mock()
			Integer expectedComparisonMin = DoubleMath.compare(actual, min)
			Integer expectedComparisonMax = DoubleMath.compare(actual, max)
		when:
			AssertResultConstructor messageConstructor =
				performer.performCompareResultRangeCheck(min, max, rangePredicate, message)
		then: "predicate receives comparison result"
			rangePredicate.test(expectedComparisonMin, expectedComparisonMax) >> false
		and: "if predicate returns false - consumer receives created assert result"
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and: "result contains specified 'message' as expected value"
			capturedResult.getActual().is(actual)
			capturedResult.getExpected().is(message)
			capturedResult.getMessage() == NumberAssertPerformer.MESSAGE_NUMBERS_COMPARE_FAIL
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual		|	min		|	max		|	message
			1.0			|	1.0		|	1.0		|	"qwe"
			1.0			|	0.5		|	1.5		|	"rty"
			0.5			|	0.0		|	1.0		|	"qaz"
	}

	def "compare-range nulls"() {
		given:
			AssertResult capturedResult = null
			Consumer<AssertResult> consumer = Mock()
			NumberAssertPerformer performer = new NumberAssertPerformer(actual, consumer)
		and:
			BiPredicate<Integer, Integer> rangePredicate = Mock()
		when:
			AssertResultConstructor messageConstructor =
				performer.performCompareResultRangeCheck(min, max, rangePredicate, message)
		then: "predicate is not used"
			0 * rangePredicate.test(_, _)
		and:
			1 * consumer.accept(_) >> {args -> capturedResult = args[0]}
		and:
			capturedResult.getActual().is(actual)
			capturedResult.getExpected().is(message)
			capturedResult.getMessage() == NumberAssertPerformer.MESSAGE_INCOMPARABLE_NUMBERS
			capturedResult.getCause() == null
		and:
			messageConstructor.getAssertResult().is(capturedResult)
		where:
			actual		|	min		|	max		|	message
			null		|	1.0		|	1.0		|	"qwe"
			null		|	null	|	1.5		|	"rty"
			1.0			|	null	|	1.5		|	"qaz"
			0.5			|	null	|	null	|	"pop"
			0.5			|	0.0		|	null	|	"lol"
			null		|	1.0		|	null	|	"kek"
			null		|	null	|	null	|	"zaz"
	}
}
