package org.whaka.util.reflection.comparison.performers

import spock.lang.Specification

import org.whaka.util.reflection.comparison.ComparisonPerformer
import org.whaka.util.reflection.comparison.ComparisonPerformers
import org.whaka.util.reflection.comparison.ComparisonResult
import org.whaka.util.reflection.comparison.ComplexComparisonResult
import org.whaka.util.reflection.properties.ClassPropertyKey

class ArrayComparisonPerformerTest extends Specification {

	def "construction"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
		when:
			ArrayComparisonPerformer<?> performer = new ArrayComparisonPerformer(delegate)
		then:
			performer.getElementPerformer().is(delegate)
	}

	def "perform comparison - null check"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			ArrayComparisonPerformer<?> performer = new ArrayComparisonPerformer(delegate)
			Object[] array = [1, false, "qwe"]
			def result = null

		when: "performer is called with two nulls"
			result = performer.qwerty123456qwerty654321(null, null)
		then: "delegate is not used"
			0 * delegate._
		and: "result is successful"
			checkResult(result, null, null, performer, true)

		when: "one of the arguments is null"
			result = performer.qwerty123456qwerty654321(array, null)
		then: "delegate is not used"
			0 * delegate._
		and: "result is failed"
			checkResult(result, array, null, performer, false)

		when: result = performer.qwerty123456qwerty654321(null, array)
		then: 0 * delegate._
		and: checkResult(result, null, array, performer, false)

		when: "performer is called with two 'identical' arrays"
			result = performer.qwerty123456qwerty654321(array, array)
		then: "delegate is not used"
			0 * delegate._
		and: "result is successful"
			checkResult(result, array, array, performer, true)
	}

	def "perform comparison - length check"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			ArrayComparisonPerformer<?> performer = new ArrayComparisonPerformer(delegate)
			Object[] array1 = [1, false, "qwe"]
			Object[] array2 = [1, false]
			def result = null

		when: "performer is called with arrays of different size"
			result = performer.qwerty123456qwerty654321(array1, array2)
		then: "delegate is not used"
			0 * delegate._
		and: "result is failed"
			checkResult(result, array1, array2, performer, false)
		and: "result is complex, and contains single sub result"
			result instanceof ComplexComparisonResult
			result.getPropertyResults().size() == 1
		and: "sub result with key 'Object[]#length' is failed, and contains corresponding length values"
			ComparisonResult lengthResult = result.getPropertyResults()[new ClassPropertyKey("length", Object[])]
			checkResult(lengthResult, 3, 2, ComparisonPerformers.DEEP_EQUALS, false)
	}

	def "perform comparison - elements"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			ArrayComparisonPerformer<?> performer = new ArrayComparisonPerformer(delegate)
		and:
			Object[] array1 = [1, false, "qwe"]
			Object[] array2 = [2, true, "qaz"]
		and:
			ComparisonResult subResult1 = Mock()
			ComparisonResult subResult2 = Mock()
			ComparisonResult subResult3 = Mock()

		when: "performer is called with two arrays of the same length"
			def result = performer.qwerty123456qwerty654321(array1, array2)
		then: "delegate is called for each pair of elements in two arrays in order"
			1 * delegate.qwerty123456qwerty654321(1, 2) >> subResult1
		and:
			1 * delegate.qwerty123456qwerty654321(false, true) >> subResult2
		and:
			1 * delegate.qwerty123456qwerty654321("qwe", "qaz") >> subResult3
		and: "final result is complex and contains the same number of sub results as arrays size"
			checkResult(result, array1, array2, performer, false)
			result instanceof ComplexComparisonResult
			def map = result.getPropertyResults()
			map.size() == 3
		and: "each sub result is mapped by the key 'Object[]#<index>' where index is an Integer object"
			map[new ClassPropertyKey(0, Object[])].is(subResult1)
			map[new ClassPropertyKey(1, Object[])].is(subResult2)
			map[new ClassPropertyKey(2, Object[])].is(subResult3)
	}

	def "key class"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			ArrayComparisonPerformer<?> performer = new ArrayComparisonPerformer(delegate)
		and:
			ComparisonResult subResult = Mock()

		when:
			def result = performer.qwerty123456qwerty654321(array1, array2)
		then:
			1 * delegate.qwerty123456qwerty654321(_, _) >> subResult
		and:
			result instanceof ComplexComparisonResult
			def map = result.getPropertyResults()
			def key = map.keySet()[0]
		and:
			key.getDeclaringClass() == declaringClass
			key.getId() == 0
		where:
			array1						|	array2						|	declaringClass
			[1] as Integer[]			|	[2] as Integer[]			|	Object[]
			[[1]] as Integer[][]		|	[2] as Integer[]			|	Object[]
			[[1]] as Integer[][]		|	[[2]] as Integer[][]		|	Object[]
			[[1]] as Integer[][]		|	[[[2]]] as Integer[][][]	|	Object[]
			[[[1]]] as Integer[][][]	|	[[[2]]] as Integer[][][]	|	Object[]
	}

	void checkResult(ComparisonResult result, Object actual, Object expected, ComparisonPerformer performer, boolean success) {
		assert result.getActual() == actual
		assert result.getExpected() == expected
		assert result.getComparisonPerformer().is(performer)
		assert result.isSuccess() == success
	}
}
