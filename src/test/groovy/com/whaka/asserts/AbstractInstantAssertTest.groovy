package com.whaka.asserts

import spock.lang.Specification

import com.google.common.base.Function
import com.whaka.asserts.builder.AssertBuilder
import com.whaka.asserts.builder.AssertResultConstructor

class AbstractInstantAssertTest extends Specification {

	def "construction"() {
		given:
			Function<String, String> mockPerformerCreator = Mock(Function)
		when:
			AbstractInstantAssert<Integer, String> instantAssert = new AbstractInstantAssert<Integer, String>("testActual") {
				protected Integer createPerformer(AssertBuilder builder, String actual) {
					return mockPerformerCreator.apply(actual)
				}
			}
		then:
			instantAssert.getActual() == "testActual"
			0 * mockPerformerCreator.apply(_)
	}

	def "perform-instant-assert/create-performer"() {
		given:
			Function<String, Integer> mockPerformerCreator = Mock(Function)
			Function<Integer, AssertResultConstructor> mockFunction = Mock(Function)
			AssertResultConstructor mockMessageConstructor = Mock(AssertResultConstructor)

		and:
			AbstractInstantAssert<Integer, String> instantAssert = new AbstractInstantAssert<Integer, String>("testActual") {
				protected Integer createPerformer(AssertBuilder builder, String actual) {
					return mockPerformerCreator.apply(actual)
				}
			}

		and:

		when: "perform-instant-assert is called with a function AND null message"
			instantAssert.performInstantAssert(mockFunction, null)
		then: "#createPerformer is called once with the actual value specified as constructor argument"
			1 * mockPerformerCreator.apply("testActual") >> 42
		and: "function receives the object returned from #createPerformer"
			1 * mockFunction.apply(42) >> mockMessageConstructor
		and: "message constructor returned from function is never called, cuz message is null"
			0 * mockMessageConstructor.withMessage(_) >> mockMessageConstructor

		when: "perform-instant-assert is called again with a function AND some message"
			instantAssert.performInstantAssert(mockFunction, "testMessage")
		then: "#createPerformer is called again with the same value specified as constructor argument"
			1 * mockPerformerCreator.apply("testActual") >> 43
		and: "function receives new object returned from #createPerformer"
			1 * mockFunction.apply(43) >> mockMessageConstructor
		and: "message constructor returned from function receives test message"
			1 * mockMessageConstructor.withMessage("testMessage")
	}
}
