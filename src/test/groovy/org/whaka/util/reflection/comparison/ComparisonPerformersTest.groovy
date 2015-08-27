package org.whaka.util.reflection.comparison

import static org.whaka.util.reflection.comparison.ComparisonPerformers.*

import java.util.function.BiPredicate

import spock.lang.Specification

import org.whaka.util.reflection.comparison.performers.ArrayComparisonPerformer
import org.whaka.util.reflection.comparison.performers.GettersDynamicPerformerBuilder
import org.whaka.util.reflection.comparison.performers.ListComparisonPerformer
import org.whaka.util.reflection.comparison.performers.MapComparisonPerformer
import org.whaka.util.reflection.comparison.performers.PropertyDynamicPerformerBuilder
import org.whaka.util.reflection.comparison.performers.SetComparisonPerformer
import org.whaka.util.reflection.comparison.performers.GettersDynamicPerformerBuilder.PatternPredicate

class ComparisonPerformersTest extends Specification {

	def "buildGetters"() {
		given:
			GettersDynamicPerformerBuilder<?> builder = ComparisonPerformers.buildGetters(type)
		expect: "created builder contains specified type"
			builder.getType() == type
		and: "created builder contains one excluding filter with DEFAULT_METHODS field"
			builder.getExcludingFilters().size() == 1
			def filter = builder.getExcludingFilters()[0]
			filter instanceof PatternPredicate
			filter.getPattern().pattern() == GettersDynamicPerformerBuilder.DEFAULT_METHODS
		where:
			type << [String, Integer, Object, Class, ComparisonPerformersTest]
	}

	def "buildProperties"() {
		given:
			PropertyDynamicPerformerBuilder<?> builder = ComparisonPerformers.buildProperties(type)
		expect:
			builder.getType() == type
		where:
			type << [String, Integer, Object, Class, ComparisonPerformersTest]
	}

	def "array"() {
		given:
			ComparisonPerformer<String> elementDelegate = Mock()
		when:
			ComparisonPerformer<String[]> arrayPerformer = ComparisonPerformers.array(elementDelegate)
		then:
			arrayPerformer instanceof ArrayComparisonPerformer<String>
			arrayPerformer.getElementPerformer().is(elementDelegate)
	}

	def "list"() {
		given:
			ComparisonPerformer<String> elementDelegate = Mock()
		when:
			ComparisonPerformer<List<String>> listPerformer = ComparisonPerformers.list(elementDelegate)
		then:
			listPerformer instanceof ListComparisonPerformer<String>
			listPerformer.getElementPerformer().is(elementDelegate)
	}

	def "set"() {
		given:
			ComparisonPerformer<String> elementDelegate = Mock()
		when:
			ComparisonPerformer<Collection<String>> setPerformer = ComparisonPerformers.set(elementDelegate)
		then:
			setPerformer instanceof SetComparisonPerformer<String>
			setPerformer.getElementPerformer().is(elementDelegate)
	}

	def "map"() {
		given:
			ComparisonPerformer<String> elementDelegate = Mock()
		when:
			ComparisonPerformer<Map<?, String>> mapPerformer = ComparisonPerformers.map(elementDelegate)
		then:
			mapPerformer instanceof MapComparisonPerformer<String>
			mapPerformer.getElementPerformer().is(elementDelegate)
	}

	def "from-predicate"() {
		given:
			BiPredicate<?, ?> predicate = Mock()
			ComparisonPerformer<?> performer = ComparisonPerformers.fromPredicate(predicate)

		when:
			ComparisonResult result1 = performer.appl(42, "qwe")
		then:
			1 * predicate.test(42, "qwe") >> true
		and:
			checkResult(result1, 42, "qwe", performer, true)

		when:
			ComparisonResult result2 = performer.appl(false, true)
		then:
			1 * predicate.test(false, true) >> false
		and:
			checkResult(result2, false, true, performer, false)
	}

	def "DEEP_EQUALS"() {
		when:
			ComparisonResult result = DEEP_EQUALS.appl(actual, expected)
		then:
			result.getActual().is(actual)
			result.getExpected().is(expected)
			result.getComparisonPerformer().is(DEEP_EQUALS)
			result.isSuccess() == Objects.deepEquals(actual, expected)
		where:
			actual						|	expected
			null						|	null
			"qwe"						|	""
			42							|	'q'
			43							|	43
			[1,2,3] as int[]			|	[1,2,3] as int[]
			[1,2,3] as int[]			|	[1,2,3] as List<Integer>
			[[1,2] as int[]] as int[][]	|	[[1,2] as int[]] as int[][]
			[[1,2] as int[]] as int[][]	|	[[1,2] as List<Integer>] as Object[]
	}

	void checkResult(ComparisonResult result, Object actual, Object expected, ComparisonPerformer performer, boolean success) {
		assert result.getActual() == actual
		assert result.getExpected() == expected
		assert result.getComparisonPerformer().is(performer)
		assert result.isSuccess() == success
	}
}
