package com.whaka.util.reflection.properties

import java.lang.reflect.Field

import spock.lang.Specification

class FieldClassPropertyTest extends Specification {

	def "FieldMember construction"() {
		when:
			FieldClassProperty member = new FieldClassProperty(field)
		then:
			member.getKey().getDeclaringClass() == field.getDeclaringClass()
			member.getKey().getId() == field.getName()
			member.getType() == field.getType()
			member.getField().is(field)
			member.getValue(target) == value
		where:
			target		|	field										||	value
			BASE		|	TestBase.getDeclaredField("strConst")		||	"qaz"
			BASE		|	TestBase.getDeclaredField("strField")		||	"qwe"
			BASE		|	TestBase.getDeclaredField("intField")		||	42
			BASE		|	TestBase.getDeclaredField("boolField")		||	true
			SUB			|	TestBase.getDeclaredField("strConst")		||	"qaz"
			SUB			|	TestBase.getDeclaredField("strField")		||	"qwe"
			SUB			|	TestBase.getDeclaredField("intField")		||	42
			SUB			|	TestBase.getDeclaredField("boolField")		||	true
			SUB			|	TestSub.getDeclaredField("strConst")		||	"pop"
			SUB			|	TestSub.getDeclaredField("strField")		||	"rty"
			SUB			|	TestSub.getDeclaredField("doubField")		||	1.5
			SUB			|	TestSub.getDeclaredField("charField")		||	'q'
			SUB			|	TestSub.getField("boolField")				||	true
	}

	def "illegal target exception"() {
		given:
			FieldClassProperty prop = new FieldClassProperty(field)
		when:
			prop.getValue(target)
		then:
			thrown(IllegalArgumentException)
		where:
			target		|	field
			BASE		|	TestSub.getDeclaredField("strConst")
			42			|	TestBase.getDeclaredField("strConst")
			""			|	TestBase.getDeclaredField("strConst")
	}

	def "null exception"() {

		given:
			FieldClassProperty prop = new FieldClassProperty(TestSub.class.getDeclaredField("strConst"))

		when:
			prop.getValue(null)
		then:
			thrown(NullPointerException)

		when:
			new FieldClassProperty(null)
		then:
			thrown(NullPointerException)

	}

	def "static field null target"() {
		given:
			FieldClassProperty member = new FieldClassProperty(field)
		expect:
			member.getField().is(field)
			member.getValue(target) == field.get(target)
		where:
			target		|	field
			null		|	TestBase.getDeclaredField("staticField")
			null		|	TestSub.getDeclaredField("staticField")
			SUB			|	TestBase.getDeclaredField("staticField")
			BASE		|	TestSub.getDeclaredField("staticField")
			42			|	TestBase.getDeclaredField("staticField")
			""			|	TestSub.getDeclaredField("staticField")
	}

	def "equals/hashCode"() {
		given:
			FieldClassProperty member1 = new FieldClassProperty(field1)
			FieldClassProperty member2 = new FieldClassProperty(field2)
			boolean equal = field1.equals(field2)
		expect:
			member1.equals(member2) == equal
			member2.equals(member1) == equal
			member1.equals(member1) == true
			member2.equals(member2) == true
		and:
			equal ? member1.hashCode() == member2.hashCode() : true
			member1.hashCode() == member1.hashCode()
			member2.hashCode() == member2.hashCode()
		where:
			field1												|	field2
			TestBase.getDeclaredField("staticField")		|	TestBase.getDeclaredField("staticField")
			TestSub.getDeclaredField("strConst")			|	TestBase.getDeclaredField("strConst")
			TestSub.getDeclaredField("doubField")			|	TestSub.getDeclaredField("doubField")
	}

	def "is-mutable"() {
		given:
			FieldClassProperty member = new FieldClassProperty(field)
		expect:
			member.isMutable() == mutable
		where:
			field												|	mutable
			TestBase.getDeclaredField("staticField")		|	false
			TestSub.getDeclaredField("staticField")		|	true
			TestSub.getDeclaredField("doubField")			|	true
			TestSub.getDeclaredField("strConst")			|	false
	}

	def "set-value"() {
		given:
			FieldClassProperty member = new FieldClassProperty(field)
		when:
			member.setValue(target, value)
		then:
			member.getValue(target) == value
		where:
			target		|	field												|	value
			null		|	TestSub.getDeclaredField("staticField")		|	"new static value"
			SUB			|	TestSub.getDeclaredField("doubField")			|	42.0 as Double
			SUB			|	TestBase.getDeclaredField("boolField")		|	false
	}

	def "set-value error"() {

		when:
			Field fieldImmutable = TestBase.class.getDeclaredField("staticField")
			FieldClassProperty memberImmutable = new FieldClassProperty(fieldImmutable)
			memberImmutable.setValue(null, "qwe")
		then:
			thrown(UnsupportedOperationException)

		when:
			Field fieldString = TestSub.class.getDeclaredField("staticField")
			FieldClassProperty memberString = new FieldClassProperty(fieldString)
			memberString.setValue(null, 42)
		then:
			thrown(IllegalArgumentException)
	}

	private static class TestBase {

		private static final String staticField = "static1"

		private final String strConst = "qaz"

		private String strField = "qwe"
		protected int intField = 42
		public boolean boolField = true
	}

	private static class TestSub extends TestBase {

		private static String staticField = "static2"

		private final String strConst = "pop"

		private String strField = "rty"
		protected double doubField = 1.5
		public char charField = 'q'
	}

	private static final TestBase BASE = new TestBase()
	private static final TestSub SUB = new TestSub()
}
