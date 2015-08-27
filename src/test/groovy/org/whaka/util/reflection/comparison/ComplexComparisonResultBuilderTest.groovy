package org.whaka.util.reflection.comparison

import spock.lang.Specification

import org.whaka.util.reflection.properties.ClassPropertyKey

class ComplexComparisonResultBuilderTest extends Specification {

	def "construction"() {
		given:
			ComplexComparisonResultBuilder<?> builder = new ComplexComparisonResultBuilder(type)
		expect:
			builder.getType() == type
			builder.getPropertyResults().isEmpty()
		where:
			type << [String, Integer, ComplexComparisonResult, BigDecimal]
	}

	def "create-key"() {
		given:
			ComplexComparisonResultBuilder<?> builder = new ComplexComparisonResultBuilder(type)
		when:
			ClassPropertyKey key = builder.createKey(name)
		then:
			key == new ClassPropertyKey(name, type)
		where:
			type			|	name
			String			|	"length()"
			Integer			|	"value"
			Object			|	"hashCode()"
			null			|	"some"
	}

	def "default comparison performer"() {
		given:
			ComplexComparisonResultBuilder<?> builder = new ComplexComparisonResultBuilder(Object)
		expect:
			builder.getDefaultComparisonPerformer().is(ComparisonPerformers.DEEP_EQUALS)
		when:
			builder.setDefaultComparisonPerformer(ComparisonPerformers.REFLECTIVE_EQUALS)
		then:
			builder.getDefaultComparisonPerformer().is(ComparisonPerformers.REFLECTIVE_EQUALS)
	}

	def "example"() {
		given:
			ComplexComparisonResultBuilder<String> builder = new ComplexComparisonResultBuilder<>(String)
			ComparisonPerformer<String> performer = Mock()
			ComparisonPerformer<String> mockPerformer = Mock()
			char[] actualChars = "qwe".toCharArray()
			char[] expectedChars = "qwerty".toCharArray()

		when:
			builder.compare("length", 3, 3)
		then:
			builder.getPropertyResults().size() == 1
			builder.getPropertyResults().get(builder.createKey("length")).isSuccess()

		when:
			ComplexComparisonResult complexResult1 = builder.build("str", "str", performer)
		then:
			complexResult1.getActual() == "str"
			complexResult1.getExpected() == "str"
			complexResult1.getComparisonPerformer() == performer
			complexResult1.getPropertyResults().size() == 1
			complexResult1.isSuccess()

		when:
			builder.compare("length2", 3, 6)
		then:
			builder.getPropertyResults().size() == 2
			ComparisonResult result = builder.getPropertyResults().get(builder.createKey("length2"))
			result.getActual() == 3
			result.getExpected() == 6
			result.getComparisonPerformer() == builder.getDefaultComparisonPerformer()
			result.isSuccess() == false

		when:
			builder.compare("toCharArray1", "qwe".toCharArray(), "qwe".toCharArray())
		then: "deep equality is used by default - arrays are resolved"
			builder.getPropertyResults().size() == 3
			builder.getPropertyResults().get(builder.createKey("toCharArray1")).isSuccess()

		when: "compare is called with the custom performer"
			builder.compare("toCharArray2", actualChars, expectedChars, mockPerformer)
		then: "specified performer is called"
			1 * mockPerformer.appl(actualChars, expectedChars) >> new ComparisonResult(0, 0, mockPerformer, true)
		and: "result returned by the performer is used"
			builder.getPropertyResults().get(builder.createKey("toCharArray2")).isSuccess()

		when:
			builder.compare("toCharArray3", actualChars, expectedChars, mockPerformer)
		then:
			1 * mockPerformer.appl(actualChars, expectedChars) >> new ComparisonResult(0, 0, mockPerformer, false)
		and:
			builder.getPropertyResults().get(builder.createKey("toCharArray3")).isSuccess() == false

		when:
			ComplexComparisonResult complexResult2 = builder.build("qwe", "qwerty", null)
		then:
			complexResult2.getActual() == "qwe"
			complexResult2.getExpected() == "qwerty"
			complexResult2.getComparisonPerformer() == null
			complexResult2.isSuccess() == false
			Map<ClassPropertyKey, ComparisonResult> results2 = builder.getPropertyResults()
			ComparisonResult charsComparison = results2.get(new ClassPropertyKey("toCharArray3", String))
			charsComparison.getActual() == 0
			charsComparison.getExpected() == 0
			charsComparison.getComparisonPerformer().is(mockPerformer)
			charsComparison.isSuccess() == false
	}
}
