package org.whaka.util.reflection.comparison.performers

import spock.lang.Specification

import org.whaka.util.reflection.comparison.ComparisonPerformer
import org.whaka.util.reflection.comparison.ComparisonPerformers
import org.whaka.util.reflection.comparison.ComparisonResult
import org.whaka.util.reflection.comparison.ComplexComparisonResult
import org.whaka.util.reflection.properties.ClassPropertyKey

class MapComparisonPerformerTest extends Specification {

	def "construction"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			MapComparisonPerformer<?> performer = new MapComparisonPerformer(delegate)
		expect:
			performer.getElementPerformer().is(delegate)
	}

	def "perform comparison - null check"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			MapComparisonPerformer<?> performer = new MapComparisonPerformer(delegate)
		and:
			Map<?, ?> map = [key1: 42, key2: 43]
			def result = null

		when: "perform comparison is called with two nulls"
			result = performer.compare(null, null)
		then: "delegate is not used"
			0 * delegate._
		and: "result is successful"
			checkResult(result, null, null, performer, true)

		when: "perform comparison is called with one null and one non-null objects"
			result = performer.compare(map, null)
		then: "delegate is not used"
			0 * delegate._
		and: "result is NOT successful"
			checkResult(result, map, null, performer, false)

		when: result = performer.compare(null, map)
		then: 0 * delegate._
		and:  checkResult(result, null, map, performer, false)

		when: "perform comparison is called with two 'identical' objects"
			result = performer.compare(map, map)
		then: "delegate is not used"
			0 * delegate._
		and: "result is successful"
			checkResult(result, map, map, performer, true)
	}

	def "perform comparison - unmatched key values"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			MapComparisonPerformer<?> performer = new MapComparisonPerformer(delegate)
		and:
			Map<String, Object> map1 = [key1: 42, key2: true, key3: "qwe"] as LinkedHashMap
			Map<String, Object> map2 = [key2: true, key1: 42, key3: "qaz"] as LinkedHashMap
		and:
			ComparisonResult trueResult = new ComparisonResult(null, null, null, true)

		when: "perform comparison is called with two maps of the same size"
			def result = performer.compare(map1, map2)
		then: "delegate is called with the pair of elements for the first found key"
			1 * delegate.compare(42, 42) >> trueResult
		and: "if previous comparison is successfult - pair of values for the next key is used"
			1 * delegate.compare(true, true) >> trueResult
		and: "if previous comparison is successfult - next key is used"
			1 * delegate.compare("qwe", "qaz") >> new ComparisonResult("qwe", "qaz", delegate, false)
		and: "the moment delegate returned false for a pair of values - result is returned"
			checkResult(result, map1, map2, performer, false)
		and: "result is complex and contains single sub result"
			result instanceof ComplexComparisonResult
			result.getPropertyResults().size() == 1
		and: "sub result is mapped by the property key 'Map#<key>' where key is the object kye of the map value"
			def subResult = result.getPropertyResults()[new ClassPropertyKey("key3", Map)]
			checkResult(subResult, "qwe", "qaz", delegate, false)
	}

	def "perform comparison - different keys"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			MapComparisonPerformer<?> performer = new MapComparisonPerformer(delegate)
		and:
			Map<String, Object> map1 = [key1: 42, key2: true, key3: "qwe"] as LinkedHashMap
			Map<String, Object> map2 = [key2: true, key1: 42, key4: "some"] as LinkedHashMap

		when: "perform comparison is called with actual map containing unique key"
			def result = performer.compare(map1, map2)
		then:
			0 * delegate._
		and: "the result is failed"
			checkResult(result, map1, map2, performer, false)
		and: "result is complex and contains single sub result"
			result instanceof ComplexComparisonResult
			result.getPropertyResults().size() == 1
		and: "sub result is mapped by the property key 'Map#keySet' with null as expected value"
			def subResult = result.getPropertyResults()[new ClassPropertyKey("keySet", Map)]
			checkResult(subResult, map1.keySet(), map2.keySet(), ComparisonPerformers.DEEP_EQUALS, false)
	}

	def "perform comparison - length check"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			MapComparisonPerformer<?> performer = new MapComparisonPerformer(delegate)
			Map<String, Object> map1 = [key1: 42, key2: true] as LinkedHashMap
			Map<String, Object> map2 = [key2: true, key1: 42, key4: "some"] as LinkedHashMap
			def result = null
		when: "performer is called with maps of different size"
			result = performer.compare(map1, map2)
		then: "delegate is not used"
			0 * delegate._
		and: "result is failed"
			checkResult(result, map1, map2, performer, false)
		and: "result is complex, and contains single sub result"
			result instanceof ComplexComparisonResult
			result.getPropertyResults().size() == 1
		and: "sub result with key 'Map#size' is failed, and contains corresponding length values"
			ComparisonResult lengthResult = result.getPropertyResults()[new ClassPropertyKey("size", Map)]
			checkResult(lengthResult, 2, 3, ComparisonPerformers.DEEP_EQUALS, false)
	}

	def "perform comparison - equal maps"() {
		given:
			ComparisonPerformer<?> delegate = Mock()
			MapComparisonPerformer<?> performer = new MapComparisonPerformer(delegate)
		and:
			Map<String, Object> map1 = [key1: 42, key2: true] as LinkedHashMap
			Map<String, Object> map2 = [key2: true, key1: 42] as LinkedHashMap
		and:
			ComparisonResult trueResult = new ComparisonResult(null, null, null, true)

		when: "perform comparison is called with expected map containing unique key"
			def result = performer.compare(map1, map2)
		then:
			1 * delegate.compare(42, 42) >> trueResult
			1 * delegate.compare(true, true) >> trueResult
		and: "the result is successfult"
			checkResult(result, map1, map2, performer, true)
	}

	void checkResult(ComparisonResult result, Object actual, Object expected, ComparisonPerformer performer, boolean success) {
		assert result.getActual() == actual
		assert result.getExpected() == expected
		assert result.getComparisonPerformer().is(performer)
		assert result.isSuccess() == success
	}
}
