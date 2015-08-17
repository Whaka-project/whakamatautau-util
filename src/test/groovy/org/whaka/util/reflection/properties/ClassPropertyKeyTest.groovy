package org.whaka.util.reflection.properties

import spock.lang.Specification

class ClassPropertyKeyTest extends Specification {

	def "name constructor"() {
		when:
			ClassPropertyKey key = new ClassPropertyKey(name)
		then:
			key.getDeclaringClass() == null
			key.getId() == name
		where:
			declaringClass		|	name
			Class.class			|	"qwe"
			String.class		|	"_"
			Integer.class		|	"trfg"
			int[].class			|	"length"
	}

	def "name and class constructor"() {
		when:
			ClassPropertyKey key = new ClassPropertyKey(name, declaringClass)
	then:
		key.getDeclaringClass() == declaringClass
		key.getId() == name
		where:
			declaringClass		|	name
			Class.class			|	"qwe"
			String.class		|	"_"
			Integer.class		|	"trfg"
			int[].class			|	"length"
			null				|	"field"
	}

	def "illegal name exception"() {
		when:
			new ClassPropertyKey(illegalName)
		then:
			thrown(IllegalArgumentException)

		when:
			new ClassPropertyKey(illegalName, String.class)
		then:
			thrown(IllegalArgumentException)

		where:
			illegalName << [null, "", " ", "	", "\n\r"]
	}

	def "primitive class exception"() {
		when: "primitive type is specified as declaring class"
			new ClassPropertyKey("qwe", primitiveClass)
		then: "exception is thrown, cuz primitives cannot declare any members"
			thrown(IllegalArgumentException)
		where:
			primitiveClass << [boolean.class, byte.class, short.class, char.class,
				 int.class, long.class, float.class, double.class, void.class]
	}

	def "hashCode/equals"() {
		given:
			ClassPropertyKey key1 = new ClassPropertyKey(name1, declaringClass1)
			ClassPropertyKey key2 = new ClassPropertyKey(name2, declaringClass2)
			boolean equals = declaringClass1 == declaringClass2 && name1 == name2
		expect:
			key1.equals(key2) == equals
			key2.equals(key1) == equals
			key1.equals(key1) == true
			key2.equals(key2) == true
		and:
			equals ? key1.hashCode() == key2.hashCode() : true
			key1.hashCode() == key1.hashCode()
			key2.hashCode() == key2.hashCode()
		where:
			declaringClass1		|	name1		|	declaringClass2		|	name2
			String.class		|	"qwe"		|	String.class		|	"qwe"
			String.class		|	"qwe"		|	String.class		|	"qaz"
			String.class		|	"qwe"		|	Integer.class		|	"qwe"
			Integer.class		|	"qwe"		|	Integer.class		|	"qwe"
	}

	def "to-string"() {
		given:
			ClassPropertyKey key = new ClassPropertyKey(name, declaringClass)
		expect:
			key.toString() == "${key.getDeclaringClass().getSimpleName()}#${key.getId()}"
		where:
			declaringClass		|	name
			Class.class			|	"qwe"
			String.class		|	"_"
			Integer.class		|	"toIntegerValue()"
			int[].class			|	"length"
	}
}
