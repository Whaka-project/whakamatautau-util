package org.whaka.util.reflection.comparison.performers

import spock.lang.Specification

import org.whaka.util.reflection.comparison.ComparisonFail
import org.whaka.util.reflection.comparison.ComparisonPerformer
import org.whaka.util.reflection.comparison.ComparisonResult
import org.whaka.util.reflection.properties.ClassProperty

class PropertyDelegatePerformerTest extends Specification {

	def "construction"() {
		given:
			ClassProperty<Integer, String> mockLengthProperty = Mock()
			ComparisonPerformer<Integer> delegatePerformer = Mock()
			PropertyDelegatePerformer<Integer, String> performer =
					new PropertyDelegatePerformer(mockLengthProperty, delegatePerformer)

		expect:
			performer.getProperty().is(mockLengthProperty)
			performer.getDelegatePerformer().is(delegatePerformer)
	}

	def "perform comparison"() {
		given:
			ClassProperty<Integer, String> mockLengthProperty = Mock()
			ComparisonPerformer<Integer> delegatePerformer = Mock()
			ComparisonResult mockResult = Mock()
			PropertyDelegatePerformer<Integer, String> performer =
					new PropertyDelegatePerformer(mockLengthProperty, delegatePerformer)

		when: "perform comparison is called with two 'outer type' objects"
			def result = performer.apply("qwe", "rtyqaz")
		then: "specified property is called twice with each object, to get property values"
			1 * mockLengthProperty.getValue("qwe") >> 3
			1 * mockLengthProperty.getValue("rtyqaz") >> 6
		and: "delegate performer is called once with corresponding property values"
			1 * delegatePerformer.apply(3, 6) >> mockResult
		and: "result from delegate is retturned"
			result.is(mockResult)
	}

	def "property extract fail"() {
		given:
			ClassProperty<Integer, String> mockLengthProperty = Mock()
			ComparisonPerformer<Integer> delegatePerformer = Mock()
			PropertyDelegatePerformer<Integer, String> performer =
					new PropertyDelegatePerformer(mockLengthProperty, delegatePerformer)
			Exception error = new RuntimeException("some")

		when: "perform comparison is called"
			def result = performer.apply("qwe", "rtyqaz")
		then: "if specified property throws an exception on #getValue for the first object"
			1 * mockLengthProperty.getValue("qwe") >> {throw error}
		and: "property isn't called for the second object"
			0 * mockLengthProperty.getValue("rtyqaz")
		and: "delegate isn't called"
			0 * delegatePerformer.apply(_, _)
		and: "ComparisonFail is returned"
			result instanceof ComparisonFail
			result.getActual() == "qwe"
			result.getExpected() == "rtyqaz"
			result.getComparisonPerformer().is(performer)
			result.isSuccess() == false
			result.getCause().is(error)

		when: "perform comparison is called"
			result = performer.apply("qwe", "rtyqaz")
		then: "if specified property throws an exception on #getValue for the second object"
			1 * mockLengthProperty.getValue("qwe") >> null
			1 * mockLengthProperty.getValue("rtyqaz") >> {throw error}
		and: "delegate isn't called"
			0 * delegatePerformer.apply(_, _)
		and: "ComparisonFail is returned"
			result instanceof ComparisonFail
			result.getActual() == "qwe"
			result.getExpected() == "rtyqaz"
			result.getComparisonPerformer().is(performer)
			result.isSuccess() == false
			result.getCause().is(error)
	}

	def "null arguments exception"() {
		given:
			ClassProperty<Integer, String> mockLengthProperty = Mock()
			ComparisonPerformer<Integer> delegatePerformer = Mock()

		when:
			new PropertyDelegatePerformer(null, delegatePerformer)
		then:
			thrown(NullPointerException)

		when:
			new PropertyDelegatePerformer(mockLengthProperty, null)
		then:
			thrown(NullPointerException)

		when:
			new PropertyDelegatePerformer(null, null)
		then:
			thrown(NullPointerException)
	}
}
