package org.whaka.util.reflection.comparison.performers

import spock.lang.Specification

import org.whaka.util.reflection.comparison.ComparisonPerformer
import org.whaka.util.reflection.comparison.ComparisonPerformers
import org.whaka.util.reflection.comparison.ComparisonResult
import org.whaka.util.reflection.comparison.ComplexComparisonResult
import org.whaka.util.reflection.properties.ClassPropertyKey

class ListComparisonPerformerTest extends Specification {

	def "construction"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
		when:
			ListComparisonPerformer<?> performer = new ListComparisonPerformer(delegate)
		then:
			performer.getElementPerformer().is(delegate)
	}

	def "perform comparison - null check"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			ListComparisonPerformer<?> performer = new ListComparisonPerformer(delegate)
			List<Object> list = [1, false, "qwe"]
			def result = null

		when: "performer is called with two nulls"
			result = performer.compare(null, null)
		then: "delegate is not used"
			0 * delegate._
		and: "result is successful"
			checkResult(result, null, null, performer, true)

		when: "one of the arguments is null"
			result = performer.compare(list, null)
		then: "delegate is not used"
			0 * delegate._
		and: "result is failed"
			checkResult(result, list, null, performer, false)

		when: result = performer.compare(null, list)
		then: 0 * delegate._
		and: checkResult(result, null, list, performer, false)

		when: "performer is called with two 'identical' lists"
			result = performer.compare(list, list)
		then: "delegate is not used"
			0 * delegate._
		and: "result is successful"
			checkResult(result, list, list, performer, true)
	}

	def "perform comparison - length check"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			ListComparisonPerformer<?> performer = new ListComparisonPerformer(delegate)
			List<Object> list1 = [1, false, "qwe"]
			List<Object> list2 = [1, false]
			def result = null

		when: "performer is called with lists of different size"
			result = performer.compare(list1, list2)
		then: "delegate is not used"
			0 * delegate._
		and: "result is failed"
			checkResult(result, list1, list2, performer, false)
		and: "result is complex, and contains single sub result"
			result instanceof ComplexComparisonResult
			result.getPropertyResults().size() == 1
		and: "sub result with key 'List#size' is failed, and contains corresponding length values"
			ComparisonResult lengthResult = result.getPropertyResults()[new ClassPropertyKey("size", List)]
			checkResult(lengthResult, 3, 2, ComparisonPerformers.DEEP_EQUALS, false)
	}

	def "perform comparison - elements"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			ListComparisonPerformer<?> performer = new ListComparisonPerformer(delegate)
		and:
			List<Object> array1 = [1, false, "qwe"]
			List<Object> array2 = [2, true, "qaz"]
		and:
			ComparisonResult subResult1 = Mock()
			ComparisonResult subResult2 = Mock()
			ComparisonResult subResult3 = Mock()

		when: "performer is called with two lists of the same length"
			def result = performer.compare(array1, array2)
		then: "delegate is called for each pair of elements in two lists in order"
			1 * delegate.compare(1, 2) >> subResult1
		and:
			1 * delegate.compare(false, true) >> subResult2
		and:
			1 * delegate.compare("qwe", "qaz") >> subResult3
		and: "final result is complex and contains the same number of sub results as lists size"
			checkResult(result, array1, array2, performer, false)
			result instanceof ComplexComparisonResult
			def map = result.getPropertyResults()
			map.size() == 3
		and: "each sub result is mapped by the key 'List#<index>' where index is an Integer object"
			map[new ClassPropertyKey(0, List)].is(subResult1)
			map[new ClassPropertyKey(1, List)].is(subResult2)
			map[new ClassPropertyKey(2, List)].is(subResult3)
	}

	void checkResult(ComparisonResult result, Object actual, Object expected, ComparisonPerformer performer, boolean success) {
		assert result.getActual() == actual
		assert result.getExpected() == expected
		assert result.getComparisonPerformer().is(performer)
		assert result.isSuccess() == success
	}
}
