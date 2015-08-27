package org.whaka.util.reflection.comparison.performers

import spock.lang.Specification

import org.whaka.util.reflection.comparison.ComparisonPerformer
import org.whaka.util.reflection.comparison.ComparisonResult
import org.whaka.util.reflection.comparison.ComplexComparisonResult
import org.whaka.util.reflection.comparison.TestEntities.JobPosition
import org.whaka.util.reflection.comparison.TestEntities.Person
import org.whaka.util.reflection.properties.ClassPropertyKey

class ReflectiveComparisonPerformerTest extends Specification {

	static final ReflectiveComparisonPerformer INSTANCE = new ReflectiveComparisonPerformer()
	static final Person MARTIN = new Person("Martin", 30, true)
	static final Person MARTINA = new Person("Martina", 30, false)

	def "INSTANCE - simple objects"() {
		when:
			def result1 = INSTANCE.apply(actual, expected)
			def result2 = INSTANCE.apply(expected, actual)
		then:
			boolean equals = Objects.deepEquals(actual, expected)
			checkResult(result1, actual, expected, INSTANCE, equals)
			checkResult(result2, expected, actual, INSTANCE, equals)
		where:
			actual							|	expected
			null							|	null
			""								|	null
			12								|	null
			""								|	12
			""								|	""
			12								|	12
			this							|	null
			this							|	12
			this							|	null
			this							|	this
			[1,2] as int[]					|	[1,2] as int[]
			[1,2] as int[]					|	[1,2] as List<Integer>
			[["qwe"],["qaz"]] as String[]	|	[["qwe"],["qaz"]] as String[]
			[["qwe"],["qaz"]] as String[]	|	[["qwe"],["qazq"]] as String[]
			String							|	String
			Integer							|	Object
	}

	def "INSTANCE - different classes"() {
		given:
			ClassPropertyKey key = new ClassPropertyKey("getClass()", Object)
		when:
			def result = INSTANCE.apply(actual, expected)
		then:
			checkResult(result, actual, expected, INSTANCE, false)
			result instanceof ComplexComparisonResult
			result.getPropertyResults().size() == 1
			result.getPropertyResults().containsKey(key)
		and:
			ComparisonResult innerResult = result.getPropertyResults()[key]
			innerResult.getActual() == actual.getClass()
			innerResult.getExpected() == expected.getClass()
			innerResult.getComparisonPerformer().is(INSTANCE)
			innerResult.isSuccess() == false
		where:
			actual					|	expected
			"42"					|	42
			42.0 as Double			|	42
			42L						|	42
			42f						|	42
			[:]						|	[]
			new StringBuilder()		|	new StringBuffer()
			String					|	"String"
	}

	def "INSTANCE - complex objects"() {
		when:
			ComparisonResult result = INSTANCE.apply(MARTIN, MARTINA)
		then:
			checkMartinMartinaResult(result)

		when:
			JobPosition positionRacer = new JobPosition("F1 Racer", MARTIN)
			JobPosition positionSpy = new JobPosition("International Spy", MARTINA)
			ComparisonResult resultPosition = INSTANCE.apply(positionRacer, positionSpy)
		then:
			checkResult(resultPosition, positionRacer, positionSpy, INSTANCE, false)
			resultPosition instanceof ComplexComparisonResult
			def map = resultPosition.getPropertyResults()
			map.size() == 2
		and:
			ComparisonResult titleResult = map[new ClassPropertyKey("title", JobPosition)]
			checkResult(titleResult, "F1 Racer", "International Spy", INSTANCE, false)
		and:
			ComparisonResult employeeResult = map[new ClassPropertyKey("employee", JobPosition)]
			checkMartinMartinaResult(employeeResult)
	}

	def "INSTANCE - arrays of complex objects"() {
		given:
			Person[] arr1 = [MARTIN, MARTIN, MARTIN]
			Person[] arr2 = [MARTINA, MARTINA, null]
		when:
			ComparisonResult result = INSTANCE.apply(arr1, arr2)
		then:
			checkResult(result, arr1, arr2, INSTANCE.ARRAY_DELEGATE, false)
		and:
			result instanceof ComplexComparisonResult
			def map = result.getPropertyResults()
			map.size() == 3
		and:
			ComparisonResult elementResult1 = map[new ClassPropertyKey(0, Object[])]
			checkMartinMartinaResult(elementResult1)
		and:
			ComparisonResult elementResult2 = map[new ClassPropertyKey(1, Object[])]
			checkMartinMartinaResult(elementResult1)
		and:
			ComparisonResult elementResult3 = map[new ClassPropertyKey(2, Object[])]
			checkResult(elementResult3, MARTIN, null, INSTANCE, false)
	}

	void checkMartinMartinaResult(ComparisonResult result) {
		checkResult(result, MARTIN, MARTINA, INSTANCE, false)
		assert result instanceof ComplexComparisonResult

		def map = result.getPropertyResults()
		assert map.size() == 3

		ComparisonResult nameResult = map[new ClassPropertyKey("name", Person)]
		checkResult(nameResult, "Martin", "Martina", INSTANCE, false)

		ComparisonResult ageResult = map[new ClassPropertyKey("age", Person)]
		checkResult(ageResult, 30, 30, INSTANCE, true)

		ComparisonResult maleResult = map[new ClassPropertyKey("male", Person)]
		checkResult(maleResult, true, false, INSTANCE, false)
	}

	void checkResult(ComparisonResult result, Object actual, Object expected, ComparisonPerformer performer, boolean success) {
		assert result.getActual() == actual
		assert result.getExpected() == expected
		assert result.getComparisonPerformer().is(performer)
		assert result.isSuccess() == success
	}
}
