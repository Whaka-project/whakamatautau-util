package org.whaka.util.reflection.comparison.performers

import java.util.function.Function

import spock.lang.Specification

import org.whaka.util.reflection.comparison.ComparisonPerformer
import org.whaka.util.reflection.comparison.ComparisonPerformers
import org.whaka.util.reflection.properties.ClassProperty
import org.whaka.util.reflection.properties.ClassPropertyKey
import org.whaka.util.reflection.properties.FunctionalClassProperty

class PropertyDynamicPerformerBuilderTest extends Specification {

	def "construction"() {
		given:
			PropertyDynamicPerformerBuilder<?> builder = new PropertyDynamicPerformerBuilder(type)
		expect:
			builder.getType() == type
			builder.getDynamicPerformer().is(builder.getDynamicPerformer())
			builder.getPropertyPerformers().isEmpty()
		where:
			type << [null, String, Integer, PropertyDynamicPerformerBuilderTest]
	}

	def "add existing property with delegate"() {
		given:
			PropertyDynamicPerformerBuilder<?> builder = new PropertyDynamicPerformerBuilder(String)
		and:
			ClassPropertyKey propertyKey = Mock()
			ClassProperty<Integer, String> mockProperty = Mock()
			mockProperty.getKey() >> propertyKey
		and:
			ComparisonPerformer<Integer> delegatePerformer = Mock()

		expect: "not property performers is created yet"
			builder.getPropertyPerformers().isEmpty()

		when: "add property is called with existing property and specific delegate"
			builder.addProperty(mockProperty, delegatePerformer)
		then: "delegate is not called immediately"
			0 * delegatePerformer.compare(_, _)
		and: "property performers map in the builder has 1 element"
			builder.getPropertyPerformers().size() == 1
		and: "map has element with the key from property with PropertyDelegatePerformer as value"
			def propertyPerformer = builder.getPropertyPerformers()[mockProperty.getKey()]
			propertyPerformer instanceof PropertyDelegatePerformer
		and: "PropertyDelegatePerformer contains specified property and specified delegate performer"
			propertyPerformer.getProperty().is(mockProperty)
			propertyPerformer.getDelegatePerformer().is(delegatePerformer)
	}

	def "add existing property"() {
		given:
			PropertyDynamicPerformerBuilder<?> builder = new PropertyDynamicPerformerBuilder(String)
		and:
			ClassPropertyKey propertyKey = Mock()
			ClassProperty<Integer, String> mockProperty = Mock()
			mockProperty.getKey() >> propertyKey

		expect: "not property performers is created yet"
			builder.getPropertyPerformers().isEmpty()

		when: "add property is called with existing property"
			builder.addProperty(mockProperty)
		then: "property performers map has 1 element"
			builder.getPropertyPerformers().size() == 1
		and: "map has element with key from specified property and PropertyDelegatePerformer as value"
			def propertyPerformer = builder.getPropertyPerformers()[mockProperty.getKey()]
			propertyPerformer instanceof PropertyDelegatePerformer
		and: "PropertyDelegatePerformer contains specified property and default dynamic performer from the builder"
			propertyPerformer.getProperty().is(mockProperty)
			propertyPerformer.getDelegatePerformer().is(builder.getDynamicPerformer())
	}

	def "add functional property with delegate"() {
		given:
			PropertyDynamicPerformerBuilder<?> builder = new PropertyDynamicPerformerBuilder(String)
		and:
			Function<String, Integer> getterFunction = Mock()
			ComparisonPerformer<Integer> delegatePerformer = Mock()

		expect: "not property performers is created yet"
			builder.getPropertyPerformers().isEmpty()

		when: "add property is called with property name, getter function, and specific performer"
			builder.addProperty("propName", getterFunction, delegatePerformer)
		then: "neither function, nor delegate are called immediately"
			0 * getterFunction.apply(_)
			0 * delegatePerformer.compare(_, _)
		and: "property performers map in the builder has 1 element"
			builder.getPropertyPerformers().size() == 1
		and: "map has element with the key with specified property name and constructor type"
			def propertyPerformer = builder.getPropertyPerformers()[new ClassPropertyKey("propName", String)]
			propertyPerformer instanceof PropertyDelegatePerformer
		and: "PropertyDelegatePerformer contains specified delegate performer"
			propertyPerformer.getDelegatePerformer().is(delegatePerformer)
		and: "PropertyDelegatePerformer contains instanceof FunctionalClassProperty as property"
			def property = propertyPerformer.getProperty()
			property instanceof FunctionalClassProperty
		and: "FunctionalClassProperty contains specified function as getter"
			property.getGetter().is(getterFunction)
	}

	def "add functional property"() {
		given:
			PropertyDynamicPerformerBuilder<?> builder = new PropertyDynamicPerformerBuilder(String)
		and:
			Function<String, Integer> getterFunction = Mock()

		expect: "not property performers is created yet"
			builder.getPropertyPerformers().isEmpty()

		when: "add property is called with property name, getter function, and specific performer"
			builder.addProperty("propName", getterFunction)
		then: "function isn't called immediately"
			0 * getterFunction.apply(_)
		and: "property performers map in the builder has 1 element"
			builder.getPropertyPerformers().size() == 1
		and: "map has element with the key with specified property name and constructor type"
			def propertyPerformer = builder.getPropertyPerformers()[new ClassPropertyKey("propName", String)]
			propertyPerformer instanceof PropertyDelegatePerformer
		and: "PropertyDelegatePerformer contains default dynamic performer from the builder"
			propertyPerformer.getDelegatePerformer().is(builder.getDynamicPerformer())
		and: "PropertyDelegatePerformer contains instanceof FunctionalClassProperty as property"
			def property = propertyPerformer.getProperty()
			property instanceof FunctionalClassProperty
		and: "FunctionalClassProperty contains specified function as getter"
			property.getGetter().is(getterFunction)
	}

	def "build"() {
		given:
			PropertyDynamicPerformerBuilder<?> builder = new PropertyDynamicPerformerBuilder(String)
			ComparisonPerformer<?> delegatePerformer = Mock()
		when:
			builder.addProperty("qwe", Mock(Function))
			builder.addProperty("rty", Mock(Function))
			builder.addProperty("qaz", Mock(Function))
			builder.addProperty("qwe2", Mock(Function), delegatePerformer)
			builder.addProperty("rty2", Mock(Function), delegatePerformer)
			builder.addProperty("qaz2", Mock(Function), delegatePerformer)
		and:
			CompositeComparisonPerformer<?> resultPerformer = builder.build("someName")
		then:
			resultPerformer.getName() == "someName"
			resultPerformer.getPerformers() == builder.getPropertyPerformers()
			checkCompositeResult(resultPerformer)
	}

	def "performer is recursive"() {
		given:
			PropertyDynamicPerformerBuilder<?> builder = new PropertyDynamicPerformerBuilder(type)
		when:
			ComparisonPerformer<String> performer = builder.build("qwe")
		then:
			builder.getDynamicPerformer().getRegisteredDelegates().get(type).is(performer)
		where:
			type << [Object, Integer, String, BigDecimal]
	}

	def "performer recursion override"() {
		given:
			ComparisonPerformer<?> DEFAULT = ComparisonPerformers.DEEP_EQUALS
			PropertyDynamicPerformerBuilder<?> builder = new PropertyDynamicPerformerBuilder(type)
		when:
			builder.getDynamicPerformer().registerDelegate(type, DEFAULT)
			ComparisonPerformer<String> performer = builder.build("qwe")
		then:
			builder.getDynamicPerformer().getRegisteredDelegates().get(type).is(DEFAULT)
		where:
			type << [Object, Integer, String, BigDecimal]
	}

	def "second build is prohibited"() {
		given:
			PropertyDynamicPerformerBuilder<?> builder = new PropertyDynamicPerformerBuilder(String)

		when:
			builder.build("qwe")
		then:
			notThrown(IllegalStateException)

		when:
			builder.build("qwe")
		then:
			thrown(IllegalStateException)

		when:
			builder.build("qwe")
		then:
			thrown(IllegalStateException)
	}

	void checkCompositeResult(CompositeComparisonPerformer<?> result) {
		Map<ClassPropertyKey, ComparisonPerformer<?>> performers = result.getPerformers()
		for (ClassPropertyKey key : performers.keySet()) {
			def performer = performers.get(key)
			assert performer instanceof PropertyDelegatePerformer
			assert performer.getProperty().getKey() == key
		}
	}
}
