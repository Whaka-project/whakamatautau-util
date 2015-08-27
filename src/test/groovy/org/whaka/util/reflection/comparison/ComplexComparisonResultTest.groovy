package org.whaka.util.reflection.comparison

import static org.whaka.util.reflection.comparison.ComparisonPerformers.*
import spock.lang.Specification

import org.whaka.util.reflection.comparison.TestEntities.JobPosition
import org.whaka.util.reflection.comparison.TestEntities.Person
import org.whaka.util.reflection.properties.ClassPropertyKey
import org.whaka.util.reflection.properties.ClassPropertyStack

class ComplexComparisonResultTest extends Specification {

	static ClassPropertyKey propertyKey1 = new ClassPropertyKey("length()", String)
	static ClassPropertyKey propertyKey2 = new ClassPropertyKey("value", String)
	static ComparisonResult resultSuccess = new ComparisonResult(1, 1, null, true)
	static ComparisonResult resultFail = new ComparisonResult("qwe", "rty", null, false)

	def "construction"() {
		given:
			ComplexComparisonResult result = new ComplexComparisonResult(actual, expected, comparisonPerformer, unequalProperties)
		expect:
			result.getActual().is(actual)
			result.getExpected().is(expected)
			result.getComparisonPerformer().is(comparisonPerformer)
			result.getPropertyResults() == unequalProperties
			result.isSuccess() == result.getPropertyResults().values().every {it.isSuccess()}
		where:
			actual	| expected	| comparisonPerformer		|	unequalProperties
			null	| null		| null						|	[:]
			null	| null		| null						|	[:]
			12		| null		| Mock(ComparisonPerformer)	|	[:]
			12		| 10		| Mock(ComparisonPerformer)	|	[(propertyKey1): resultSuccess]
			""		| 10		| Mock(ComparisonPerformer)	|	[(propertyKey1): resultFail]
			""		| ""		| Mock(ComparisonPerformer)	|	[(propertyKey1): resultSuccess, (propertyKey2): resultFail]
			false	| true		| Mock(ComparisonPerformer)	|	[(propertyKey1): resultFail, (propertyKey2): resultFail]
			1.5		| null		| Mock(ComparisonPerformer)	|	[(propertyKey1): resultSuccess, (propertyKey2): resultSuccess]
	}

	def "null present exception"() {
		when: "property results argument might be null"
			def result = new ComplexComparisonResult(null, null, null, null)
		then: "returned map is empty"
			result.getPropertyResults().isEmpty()

		when: "property results argument contains null key"
			new ComplexComparisonResult(null, null, null, [(propertyKey1): resultSuccess, (null): resultFail])
		then:
			thrown(IllegalArgumentException)

		when: "property results argument contains null value"
			new ComplexComparisonResult(null, null, null, [(propertyKey1): resultSuccess, (propertyKey2): null])
		then:
			notThrown(IllegalArgumentException)

		when:
			new ComplexComparisonResult(null, null, null, [(propertyKey1): resultSuccess, (null): null])
		then:
			thrown(IllegalArgumentException)
	}

	def "flatten"() {
		given:
			def keyChildName = new ClassPropertyKey("name", Object)
			def childName = new ComparisonResult("Qwe", "Qaz", null, false)
			def childMap = [(keyChildName): childName]
		and:
			def keyParentName = new ClassPropertyKey("name", Object)
			def keyParentChild = new ClassPropertyKey("child", Object)
			def parentName = new ComparisonResult("Pop", "Rty", null, false)
			def parentChild = new ComplexComparisonResult(null, null, null, childMap)
			def parentMap = [(keyParentName): parentName, (keyParentChild): parentChild]
		and:
			def parent = new ComplexComparisonResult(null, null, null, parentMap)

		when:
			def flattenResult = parent.flatten()
		then:
			flattenResult.size() == 2
		and:
			def firstEntry = flattenResult.entrySet()[0]
			firstEntry.getKey() == ClassPropertyStack.createStack(keyParentName)
			firstEntry.getKey().toCallString() == "Object#name"
			firstEntry.getValue() == parentName
		and:
			def secondEntry = flattenResult.entrySet()[1]
			secondEntry.getKey() == ClassPropertyStack.createStack(keyParentChild, keyChildName)
			secondEntry.getKey().toCallString() == "Object#child.name"
			secondEntry.getValue() == childName
	}

	def "flatten - real example"() {
		given:
			def personName = new ClassPropertyKey("name", Person)
			def personAge = new ClassPropertyKey("age", Person)
			def personMale = new ClassPropertyKey("male", Person)
			Person martin = new Person("Martin", 30, true)
			Person martina = new Person("Martina", 30, false)
			ComplexComparisonResult personResult = REFLECTIVE_EQUALS.qwerty123456qwerty654321(martin, martina)
		and:
			def jobTitle = new ClassPropertyKey("title", JobPosition)
			def jobEmployee = new ClassPropertyKey("employee", JobPosition)
			JobPosition positionRacer = new JobPosition("F1 Racer", martin)
			JobPosition positionSpy = new JobPosition("International Spy", martina)
			ComplexComparisonResult jobResult = REFLECTIVE_EQUALS.qwerty123456qwerty654321(positionRacer, positionSpy)

		when:
			def flattenPersonResult = personResult.flatten()
		then:
			flattenPersonResult.size() == 3
		and:
			def firstEntry = flattenPersonResult.entrySet()[0]
			firstEntry.getKey() == ClassPropertyStack.createStack(personName)
			checkResult(firstEntry.getValue(), "Martin", "Martina", REFLECTIVE_EQUALS, false)
		and:
			def secondEntry = flattenPersonResult.entrySet()[1]
			secondEntry.getKey() == ClassPropertyStack.createStack(personAge)
			checkResult(secondEntry.getValue(), 30, 30, REFLECTIVE_EQUALS, true)
		and:
			def thirdEntry = flattenPersonResult.entrySet()[2]
			thirdEntry.getKey() == ClassPropertyStack.createStack(personMale)
			checkResult(thirdEntry.getValue(), true, false, REFLECTIVE_EQUALS, false)

		when:
			def flattenJobResult = jobResult.flatten()
		then:
			flattenJobResult.size() == 4
		and:
			def firstJobEntry = flattenJobResult.entrySet()[0]
			firstJobEntry.getKey() == ClassPropertyStack.createStack(jobTitle)
			checkResult(firstJobEntry.getValue(), "F1 Racer", "International Spy", REFLECTIVE_EQUALS, false)
		and:
			def secondJobEntry = flattenJobResult.entrySet()[1]
			secondJobEntry.getKey() == ClassPropertyStack.createStack(jobEmployee, personName)
			checkResult(secondJobEntry.getValue(), "Martin", "Martina", REFLECTIVE_EQUALS, false)
		and:
			def thirdJobEntry = flattenJobResult.entrySet()[2]
			thirdJobEntry.getKey() == ClassPropertyStack.createStack(jobEmployee, personAge)
			checkResult(thirdJobEntry.getValue(), 30, 30, REFLECTIVE_EQUALS, true)
		and:
			def fourthJobEntry = flattenJobResult.entrySet()[3]
			fourthJobEntry.getKey() == ClassPropertyStack.createStack(jobEmployee, personMale)
			checkResult(fourthJobEntry.getValue(), true, false, REFLECTIVE_EQUALS, false)
	}

	def "flatten with a parent stack"() {
		given:
			def keyChildName = new ClassPropertyKey("name", Object)
			def childName = new ComparisonResult("Qwe", "Qaz", null, false)
			def childMap = [(keyChildName): childName]
			def child = new ComplexComparisonResult(null, null, null, childMap)

		when:
			def flattenResult = child.flatten()
		then:
			def entry = flattenResult.entrySet()[0]
			entry.getKey() == ClassPropertyStack.createStack(keyChildName)
			entry.getKey().toCallString() == "Object#name"

		when:
			ClassPropertyKey startingKey = new ClassPropertyKey("child")
			def flattenResult2 = child.flatten(ClassPropertyStack.createStack(startingKey))
		then:
			def entry2 = flattenResult2.entrySet()[0]
			entry2.getKey() == ClassPropertyStack.createStack(startingKey, keyChildName)
			entry2.getKey().toCallString() == "?#child.name"
	}

	void checkResult(ComparisonResult result, Object actual, Object expected, ComparisonPerformer performer, boolean success) {
		assert result.getActual() == actual
		assert result.getExpected() == expected
		assert result.getComparisonPerformer().is(performer)
		assert result.isSuccess() == success
	}
}
