package com.whaka.util.reflection.comparison.performers

import java.util.function.Consumer

import spock.lang.Specification

import com.whaka.util.reflection.comparison.ComparisonPerformer

class AbstractDynamicPerformerBuilderTest extends Specification {

	def "construction"() {
		given:
			AbstractDynamicPerformerBuilder<?> builder = new AbstractDynamicPerformerBuilder(type) {
				ComparisonPerformer build(String name) {return null}
			}
		expect:
			builder.getType() == type
			builder.getDynamicPerformer().is(builder.getDynamicPerformer())
		where:
			type << [null, String, Integer, AbstractDynamicPerformerBuilderTest]
	}

	def "configure dynamic performer"() {
		given:
			AbstractDynamicPerformerBuilder<?> builder = new AbstractDynamicPerformerBuilder(Object) {
				ComparisonPerformer build(String name) {return null}
			}
		and:
			Consumer<DynamicComparisonPerformer> consumer = Mock()
		when: "configure dynamic performer is called with a consumer"
			builder.configureDynamicPerformer(consumer)
		then: "consumer is called with the same performer as returned from #getDynamicPerformer"
			1 * consumer.accept(builder.getDynamicPerformer())
	}
}
