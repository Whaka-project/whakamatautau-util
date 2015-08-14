package com.whaka.util

import static java.lang.Double.*
import spock.lang.Specification

class DoubleMathTest extends Specification {

	private static final Double PINF = Double.POSITIVE_INFINITY
	private static final Double NINF = Double.NEGATIVE_INFINITY
	private static final Double MAX = Double.MAX_VALUE
	private static final Double MIN = Double.MIN_VALUE

	private static final Double MINIMAL_POSSIBLE_STEP = Math.pow(10, -DoubleMath.MAXIMUM_DEFAULT_DECIMALS)
	private static final Double TOO_SMALL_STEP = Math.pow(10, -DoubleMath.MAXIMUM_DEFAULT_DECIMALS - 1)

	def "as-double"() {
		given:
			Double d = 9999.0
		expect:
			DoubleMath.asDouble(null) == null
			DoubleMath.asDouble(d).is(d)
			DoubleMath.asDouble(number) == number.doubleValue()
		where:
			number << [0, 1, 12L, 12 as byte, 12 as short, 12F, Long.MAX_VALUE, Double.NaN]
	}

	def "test roundTo specific decimals"() {
		when:
			double roundedValue = DoubleMath.roundTo(value, decimals)
		then:
			DoubleMath.equals(roundedValue, result)
		where:
			value	|	decimals	|	result
			0.1		|	1			|	0.1
			0.1		|	0			|	0.0
			0.14	|	1			|	0.1
			0.15	|	1			|	0.2
			0.14	|	0			|	0.0
			0.15	|	0			|	0.0
			0.12345	|	2			|	0.12
			0.12344	|	4			|	0.1234
			0.12345	|	4			|	0.1235
			1234.5	|	2			|	1234.5
			1234.5	|	1			|	1234.5
			1234.5	|	0			|	1235.0
			1234.5	|	-1			|	1230.0
			1234.5	|	-2			|	1200.0
			1234.5	|	-3			|	1000.0
			1234.5	|	-4			|	0.0
			NaN		|	42			|	NaN
			NaN		|	-42			|	NaN
			PINF	|	27			|	PINF
			NINF	|	-27			|	NINF
	}

	def "test normal double compare with accuracy"() {
		expect:
			DoubleMath.compare(value1, value2, accuracy) == result
			DoubleMath.compare(value2, value1, accuracy) == -result
	where:
		[value1, value2, accuracy, result] << dataDoubleValuesAndAccuracyAndCompareResult()
	}

	def "test normal double equals with accuracy"() {
		expect:
			DoubleMath.equals(value1, value2, accuracy) == (result == 0)
			DoubleMath.equals(value2, value1, accuracy) == (result == 0)
		where:
			[value1, value2, accuracy, result] << dataDoubleValuesAndAccuracyAndCompareResult()
	}

	def dataDoubleValuesAndAccuracyAndCompareResult() {

		def ALMOST_MAX = MAX / 10

		return [
			// a, b, accuracy, compare result
			[0.0,		0.0,		0.0,		0],
			[1.0,		0.0,		0.0,		1],
			[0.5,		0.51,		0.1,		0],
			[0.051,		0.052,		0.002,		0],
			[0.0005,	0.0006,		0.00001,	-1],
			[1.1e-10,	1.0e-10,	1e-11,		1],
			[1.1e-10,	1.0e-10,	1e-10,		0],
			[MIN,		0.00005,	1e-22,		-1],
			[MIN,		0.00005,	0.01,		0],
			[MIN,		MIN,		0.0,		0],
			[MAX,		MAX,		0.0,		0],
			[MAX,		ALMOST_MAX,	10.0,		1],
			[MIN,		MAX,		0.0,		-1],
			[MIN,		MAX,		1e100,		-1],
		]
	}

	def "test DEFAULT round"() {
		expect:
			DoubleMath.equals(DoubleMath.round(value), result)
		where:
			[value,	result] << dataDoubleValuesWithRoundingErrorsAndRoundingResult()
	}

	def "test normal double DEFAULT compare rounded values"() {
		expect:
			DoubleMath.compare(value1, value2) == 0
			DoubleMath.compare(value2, value1) == 0
		where:
			[value1, value2] << dataDoubleValuesWithRoundingErrorsAndRoundingResult()
	}

	/**
	 * Hardcoded values with expected rounding result
	 *
	 * @return double 'double' array, where each inner array contains two elements: value with rounding error, and
	 * expected value.
	 */
	public static double[][] dataDoubleValuesWithRoundingErrorsAndRoundingResult() {
		return [
	        [0.0, 0.0],
	        [0.1, 0.1],
	        [0.01, 0.01],
	        [1.0, 1.0],
	        [10.0, 10.0],
	        [0.000000000000029976021664879227, 0.00000000000003],
	        [0.0000000029999999151542056, 0.000000003],
	        [0.6299999999999999, 0.63],
	        [-0.09999999999999998, -0.1],
	        [899.8999999999999, 899.9],
	        [8999.900000000001, 8999.9],
	        [89999999.89999999, 89999999.9],
	        [899999999999999.8, 899999999999999.9],
	        [NaN, NaN],
	        [PINF, PINF],
	        [NINF, NINF],
		]
	}

	def "test normal double DEFAULT compare"() {
		expect:
			DoubleMath.compare(value1, value2) == result as int
			DoubleMath.compare(value2, value1) == -result as int
		where:
			[value1, value2, result] << dataDoubleValuesAndCompareResult()
	}

	def "test normal double DEFAULT equals"() {
		expect:
			DoubleMath.equals(value1, value2) == (result == 0)
			DoubleMath.equals(value2, value1) == (result == 0)
		where:
			[value1, value2, result] << dataDoubleValuesAndCompareResult()
	}

	/**
	 * Cases with simulated minimal possible, and too small difference.
	 *
	 * @return double 'double' array where each inner array contains three elements: two values to compare,
	 * and expected comparison result (as integer value).
	 */
	public static double[][] dataDoubleValuesAndCompareResult() {

		def WITH_TOO_SMALL_STEP = 1.0 - TOO_SMALL_STEP
		def WITH_MINIMAL_STEP = 1.0 - MINIMAL_POSSIBLE_STEP
		def WITH_A_BIT_BIGGER_STEP = 1.0 - MINIMAL_POSSIBLE_STEP * 2

		return [
			[0.01, 0.01, 0],
			[0.01, 0.02, -1],
			[WITH_TOO_SMALL_STEP, WITH_MINIMAL_STEP, 0],
			[WITH_MINIMAL_STEP, WITH_A_BIT_BIGGER_STEP, 1],
		]
	}

	def "test nulls equals, accuracy irrelevant"() {
		expect:
			DoubleMath.equals(value1, value2, accuracy) == result
			DoubleMath.equals(value2, value1, accuracy) == result
			DoubleMath.equals(value1, value2) == result
			DoubleMath.equals(value2, value1) == result
		where:
			value1	|	value2	|	accuracy	||	result
			1.0		|	1.0		|	0.0			||	true
			0.0		|	0.0		|	0.0			||	true
			1.0		|	null	|	12.0		||	false
			null	|	0.0		|	0.25		||	false
			null	|	null	|	0.5			||	true
	}

	def "test isNumber"() {
		expect:
			DoubleMath.isNumber(value) == (value != null && !Double.isNaN(value))
		where:
			value << dataDoubleValuesAndNull()
	}

	def "test isFinite"() {
		expect:
			DoubleMath.isFinite(value) == (value != null && Double.isFinite(value))
		where:
			value << dataDoubleValuesAndNull()
	}

	def "test zero compare"() {
		expect:
			DoubleMath.isZero(value) == DoubleMath.equals(value, 0.0)
			DoubleMath.isPositive(value) == (DoubleMath.isNumber(value) && DoubleMath.compare(value, 0.0) > 0)
			DoubleMath.isNegative(value) == (DoubleMath.isNumber(value)  && DoubleMath.compare(value, 0.0) < 0)
		where:
			value << dataDoubleValuesAndNull()
	}

	def dataDoubleValuesAndNull() {
		return [0.0, 0.001, MIN, MAX, -MIN, -MAX, NaN, PINF, NINF, 1e-14, 1e-13, -1e-14, -1e-13, null]
	}

	def "test NaN double compare, accuracy irrelevant"() {

		expect: "NaN is equal to itself"
			DoubleMath.compare(NaN, NaN, accuracy) == 0
			DoubleMath.compare(NaN, NaN) == 0

		and: "NaN is greater than any other value"
			DoubleMath.compare(value, NaN, accuracy) == -1
			DoubleMath.compare(NaN, value, accuracy) == 1
			DoubleMath.compare(value, NaN) == -1
			DoubleMath.compare(NaN, value) == 1

		where: "infinities are included and accuracy is irrelevant"
			[value,	accuracy] << dataDoubleValAndAccuracyWithInfinities()
	}

	def "test NaN double equal, accuracy irrelevant"() {

		expect: "NaN is equal to itself"
			DoubleMath.equals(NaN, NaN, accuracy)
			DoubleMath.equals(NaN, NaN)

		and: "NaN is NOT equal to any other value"
			!DoubleMath.equals(value, NaN, accuracy)
			!DoubleMath.equals(NaN, value, accuracy)
			!DoubleMath.equals(value, NaN)
			!DoubleMath.equals(NaN, value)

		where: "infinities are included and accuracy is irrelevant"
			[value,	accuracy] << dataDoubleValAndAccuracyWithInfinities()
	}

	def dataDoubleValAndAccuracyWithInfinities() {
		return [
			[0.0,	0.0],
			[0.0,	1e13],
			[0.0,	1e-13],
			[999,	1e-13],
			[-999,	1e-13],
			[MAX,	1.0],
			[MIN,	1.0],
			[PINF,	255.01],
			[NINF,	9999.0]
		]
	}

	def "test infinities compare, accuracy irrelevant"() {

		expect: "infinities are equal to itself, and not equal accordingly"
			DoubleMath.compare(PINF, PINF, accuracy) == 0
			DoubleMath.compare(NINF, NINF, accuracy) == 0
			DoubleMath.compare(NINF, PINF, accuracy) == -1
			DoubleMath.compare(PINF, NINF, accuracy) == 1
			DoubleMath.compare(PINF, PINF) == 0
			DoubleMath.compare(NINF, NINF) == 0
			DoubleMath.compare(NINF, PINF) == -1
			DoubleMath.compare(PINF, NINF) == 1

		and: "negative infinity is lower than any other finite number"
			DoubleMath.compare(NINF, value, accuracy) == -1
			DoubleMath.compare(value, NINF, accuracy) == 1
			DoubleMath.compare(NINF, value) == -1
			DoubleMath.compare(value, NINF) == 1

		and: "positive infinity is greater than any other finite number"
			DoubleMath.compare(PINF, value, accuracy) == 1
			DoubleMath.compare(value, PINF, accuracy) == -1
			DoubleMath.compare(PINF, value) == 1
			DoubleMath.compare(value, PINF) == -1

		where:
			[value,	accuracy] << dataDoubleValAndAccuracyNoInfinities()
	}

	def "test infinities equal, accuracy irrelevant"() {

		expect: "infinities are equal to itself"
			DoubleMath.equals(PINF, PINF, accuracy)
			DoubleMath.equals(NINF, NINF, accuracy)
			DoubleMath.equals(PINF, PINF)
			DoubleMath.equals(NINF, NINF)

		and: "infinities are NOT equal to any other finite number"
			!DoubleMath.equals(PINF, value, accuracy)
			!DoubleMath.equals(value, PINF, accuracy)
			!DoubleMath.equals(NINF, value, accuracy)
			!DoubleMath.equals(value, NINF, accuracy)
			!DoubleMath.equals(PINF, value)
			!DoubleMath.equals(value, PINF)
			!DoubleMath.equals(NINF, value)
			!DoubleMath.equals(value, NINF)

		where:
			[value,	accuracy] << dataDoubleValAndAccuracyNoInfinities()
	}

	def dataDoubleValAndAccuracyNoInfinities() {
		return [
			[0.0,	0.0],
			[0.0,	1e13],
			[0.0,	1e-13],
			[999,	1e-13],
			[-999,	1e-13],
			[MAX,	1.0],
			[MIN,	1.0],
		]
	}

	def "test compare and equals negative accuracy exception"() {

		when:
			DoubleMath.compare(1.0, 2.0, accuracy)
		then:
			thrown(IllegalArgumentException)

		when:
			DoubleMath.equals(1.0, 2.0, accuracy)
		then:
			thrown(IllegalArgumentException)

		where:
			accuracy << [-1e-13, -1, -100, -9999, -MIN, -MAX, PINF, NINF, NaN]
	}

	def "test roundTo illegal decimals exception"() {

		when:
			DoubleMath.roundTo(1.0, decimals)
		then:
			thrown(IllegalArgumentException)

		where:
			decimals << [DoubleMath.MAXIMUM_POSSIBLE_DECIMALS + 1, -DoubleMath.MAXIMUM_POSSIBLE_DECIMALS - 1]
	}

	def "test last affected decimal"() {
		expect:
			DoubleMath.getLastAffectedDecimal(value) == result
		where:
			[value, result] << dataDoubleValueAndLastAffectedDecimal()
	}

	def dataDoubleValueAndLastAffectedDecimal() {

		def final DEFMAX = DoubleMath.MAXIMUM_DEFAULT_DECIMALS

		def data = [
			[0.0, DEFMAX],
			[1.0, DEFMAX],
			[2.5, DEFMAX],
			[7.7, DEFMAX],
			[0.12, DEFMAX],
			[-9.99, DEFMAX],
			[10.0, DEFMAX - 1],
			[25.0, DEFMAX - 1],
			[88.98, DEFMAX - 1],
			[110.02, DEFMAX - 2],
			[810.02, DEFMAX - 2],
			[2005.0, DEFMAX - 3],
			[2.5e5, DEFMAX - 5],
			[NaN, 0],
			[NINF, 0],
			[PINF, 0],
		]

		for (int i = -10; i < 20; i++) {
			double d = Math.pow(10, i)
			int exp = DEFMAX
			if (i > DEFMAX)
				exp = 0
			else if (i > 0)
				exp -= i
			data << [d, exp]
		}

		return data
	}
}
