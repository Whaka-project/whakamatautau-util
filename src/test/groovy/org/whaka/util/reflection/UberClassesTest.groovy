package org.whaka.util.reflection

import java.util.stream.Collectors

import spock.lang.Specification

class UberClassesTest extends Specification {

	def "descends"() {
		expect:
			UberClasses.descends(a, b) == expected
		where:
			a			|	b					|	expected
			String		|	String				|	false
			String		|	CharSequence		|	true
			String		|	Comparable			|	true
			String		|	Serializable		|	true
			String		|	Object				|	true
			String		|	Number				|	false
			String		|	List				|	false
			ArrayList	|	ArrayList			|	false
			ArrayList	|	List				|	true
			ArrayList	|	AbstractList		|	true
			ArrayList	|	Collection			|	true
			ArrayList	|	AbstractCollection	|	true
			ArrayList	|	Object				|	true
			Collection	|	Set					|	false
			Set			|	Collection			|	true
	}

	def "descends NPE"() {
		when:
			UberClasses.descends(a, b)
		then:
			thrown(NullPointerException)
		where:
			a		|	b
			String	|	null
			null	|	String
			null	|	null
	}

	def "stream-ancestors"() {
		given:
			def expected = []
			for (Class<?> c = type; c != null; c = c.getSuperclass())
				expected << c
		expect:
			UberClasses.streamAncestors(type).collect(Collectors.toList()) == expected
		where:
			type << [String, Double, BigDecimal, StringBuilder, ArrayList]
	}

	def "stream-interfaces"() {
		given:
			def expected = [type].plus(type.getInterfaces()).flatten()
		expect:
			UberClasses.streamInterfaces(type).collect(Collectors.toList()) == expected
		where:
			type << [String, Double, BigDecimal, StringBuilder, ArrayList]
	}

	def "stream-type-linerization"() {
		expect:
			UberClasses.streamTypeLinearization(type)
				.filter{Class<?> c -> c.getDeclaringClass() == UberClassesTest}
				.collect(Collectors.toList()) == expected
		where:
			type		|	expected
			I1			|	[I1]
			I2			|	[I2, I1]
			I3			|	[I3, I2, I1]
			Z1			|	[Z1]
			Z2			|	[Z2, I3, I2, I1, Z1]
			Z3			|	[Z3, Z2, I3, I2, I1, Z1]
			ClassA		|	[ClassA, I1]
			ClassB		|	[ClassB, I2, I1, Z3, Z2, I3, Z1, ClassA]
			ClassC		|	[ClassC, I3, I2, I1, ClassB, Z3, Z2, Z1, ClassA]
	}

	static interface I1 {}
	static interface I2 extends I1 {}
	static interface I3 extends I2 {}

	static interface Z1 {}
	static interface Z2 extends I3, Z1 {}
	static interface Z3 extends Z2 {}

	static class ClassA implements I1 {}
	static class ClassB extends ClassA implements I2, Z3 {}
	static class ClassC extends ClassB implements I3 {}
}
