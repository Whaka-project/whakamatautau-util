package com.whaka.util.reflection

import java.lang.reflect.Modifier

import spock.lang.Specification

class VisibilityTest extends Specification {

	def "get-from-modifiers"() {
		expect:
			Visibility.getFromModifiers(modifiers) == visibility
		where:
			modifiers				|	visibility
			Modifier.PUBLIC			|	Visibility.PUBLIC
			Modifier.PROTECTED		|	Visibility.PROTECTED
			Modifier.PRIVATE		|	Visibility.PRIVATE
			0						|	Visibility.DEFAULT
	}
}
