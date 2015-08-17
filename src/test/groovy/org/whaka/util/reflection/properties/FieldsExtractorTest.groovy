package org.whaka.util.reflection.properties

import groovy.transform.PackageScope

import java.lang.reflect.Field

import spock.lang.Specification

class FieldsExtractorTest extends Specification {

	def "test extract all"() {
		given:
			FieldsExtractor extractor = new FieldsExtractor()
		when:
			Map<ClassPropertyKey, FieldClassProperty> properties = extractor.extractAll(target)
		then:
			properties.keySet().containsAll(target.declaredFields
			.collect {Field field ->
				new ClassPropertyKey(field.getName(), field.getDeclaringClass())
			})
		where:
			target << [A, B, C, Integer, String]
	}

	static class A {
		private String privateStr = "private str A"
		@PackageScope String defaultStr = "default str A"
		protected String protectedStr = "protected str A"
		public String publicStr = "public str A"

		private static String privateStaticStr = "private static str A"
		@PackageScope static String defaultStaticStr = "default static str A"
		protected static String protectedStaticStr = "protected static str A"
		public static String publicStaticStr = "public static str A"

		public static Object publicStaticObj = "public static obj A"
	}

	static class B extends A {
		private String privateStr = "private str B"
		@PackageScope String protectedStr = "protected str B"

		private static String publicStaticStr = "public static str B"
	}

	static class C extends B {
		private String protectedStr = "protected str C"
		@PackageScope String defaultStr = "default str C"

		private Object publicStaticObj = "private obj C"
	}

	private static final A instanceA = new A()
	private static final B instanceB = new B()
	private static final C instanceC = new C()

	def "test extract order"() {
		given:
			FieldsExtractor extractor = new FieldsExtractor()
		when:
			def properties = extractor.extractAll(type)
		then:
			def filterKeys = properties.values()findAll { it.getType() == Object }*.getKey()
			filterKeys*.getId() == fields
		where:
			type	|	fields
			OrderA	|	["qwe3", "rty2", "qaz1"]
			OrderB	|	["pop2", "lol1", "qwe3", "rty2", "qaz1"]
	}

	static class OrderA {
		private Object qwe3 = ""
		private Object rty2 = ""
		private Object qaz1 = ""
	}

	static class OrderB extends OrderA {
		private Object pop2 = ""
		private Object lol1 = ""
	}
}