package org.whaka.util.reflection.properties

import java.util.function.BiConsumer
import java.util.function.Function

import spock.lang.Specification

import org.whaka.util.reflection.Visibility

class FunctionalClassPropertyTest extends Specification {

	def "full-constructor"() {
		given:
			Function<String, Object> getter = Mock()
			BiConsumer<String, Object> setter = Mock()
		when:
			FunctionalClassProperty<String> prop = new FunctionalClassProperty<>(
				new ClassPropertyKey("length", String), int, Visibility.PROTECTED, getter, setter, true)
		then:
			prop.getKey().getId() == "length"
			prop.getKey().getDeclaringClass() == String
			prop.getType() == int
			prop.getVisibility() == Visibility.PROTECTED
			prop.getGetter().is(getter)
			prop.getSetter().is(setter)
			prop.isMutable() == true
			prop.isStatic() == true
	}

	def "non-static-constructor"() {
		given:
			Function<String, Object> getter = Mock()
			BiConsumer<String, Object> setter = Mock()
		when:
			FunctionalClassProperty<String> prop = new FunctionalClassProperty<>(
				new ClassPropertyKey("length", String), int, Visibility.PRIVATE, getter, setter)
		then:
			prop.getKey().getId() == "length"
			prop.getKey().getDeclaringClass() == String
			prop.getType() == int
			prop.getVisibility() == Visibility.PRIVATE
			prop.getGetter().is(getter)
			prop.getSetter().is(setter)
			prop.isMutable() == true
			prop.isStatic() == false
	}

	def "no-setter-constructor"() {
		given:
			Function<String, Object> getter = Mock()
		when:
			FunctionalClassProperty<String> prop = new FunctionalClassProperty<>(
			new ClassPropertyKey("length", String), int, Visibility.DEFAULT, getter)
		then:
			prop.getKey().getId() == "length"
			prop.getKey().getDeclaringClass() == String
			prop.getType() == int
			prop.getVisibility() == Visibility.DEFAULT
			prop.getGetter().is(getter)
			prop.getSetter() == null
			prop.isMutable() == false
			prop.isStatic() == false
	}

	def "getter/setter"() {
		given:
			Function<String, Object> getter = Mock()
			BiConsumer<String, Object> setter = Mock()
			FunctionalClassProperty<String> prop = new FunctionalClassProperty<>(
				new ClassPropertyKey("length", String), int, Visibility.PROTECTED, getter, setter)
			def result = null

		when:
			result = prop.getValue("qwe")
		then:
			1 * getter.apply("qwe") >> 42
			result == 42

		when:
			prop.setValue("qwe", 12)
		then:
			1 * setter.accept("qwe", 12)
	}

	def "newPublic-full"() {
		given:
			Function<String, Object> getter = Mock()
			BiConsumer<String, Object> setter = Mock()
		when:
			FunctionalClassProperty<String> prop = FunctionalClassProperty.newPublic(
				new ClassPropertyKey("length", String), int, getter, setter, true)
		then:
			prop.getKey().getId() == "length"
			prop.getKey().getDeclaringClass() == String
			prop.getType() == int
			prop.getVisibility() == Visibility.PUBLIC
			prop.getGetter().is(getter)
			prop.getSetter().is(setter)
			prop.isMutable() == true
			prop.isStatic() == true
	}

	def "newPublic-non-static"() {
		given:
			Function<String, Object> getter = Mock()
			BiConsumer<String, Object> setter = Mock()
		when:
			FunctionalClassProperty<String> prop = FunctionalClassProperty.newPublic(
				new ClassPropertyKey("length", String), int, getter, setter)
		then:
			prop.getKey().getId() == "length"
			prop.getKey().getDeclaringClass() == String
			prop.getType() == int
			prop.getVisibility() == Visibility.PUBLIC
			prop.getGetter().is(getter)
			prop.getSetter().is(setter)
			prop.isMutable() == true
			prop.isStatic() == false
	}

	def "newPublic-no-setter"() {
		given:
			Function<String, Object> getter = Mock()
		when:
			FunctionalClassProperty<String> prop = FunctionalClassProperty.newPublic(
				new ClassPropertyKey("length", String), int, getter)
		then:
			prop.getKey().getId() == "length"
			prop.getKey().getDeclaringClass() == String
			prop.getType() == int
			prop.getVisibility() == Visibility.PUBLIC
			prop.getGetter().is(getter)
			prop.getSetter() == null
			prop.isMutable() == false
			prop.isStatic() == false
	}
}
