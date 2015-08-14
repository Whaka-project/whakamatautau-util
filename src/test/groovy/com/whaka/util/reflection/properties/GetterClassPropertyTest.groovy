package com.whaka.util.reflection.properties

import java.lang.reflect.Method

import spock.lang.Specification

class GetterClassPropertyTest extends Specification {

	def "FieldMember construction"() {
		when:
			GetterClassProperty member = new GetterClassProperty(getter)
		then:
			member.getKey().getDeclaringClass() == getter.getDeclaringClass()
			member.getKey().getId() == "${getter.getName()}()"
			member.getType() == getter.getReturnType()
			member.getGetter().is(getter)
			member.getValue(target) == value
		where:
			target		|	getter										||	value
			BASE		|	TestBase.getDeclaredMethod("getString")		||	"qwe"
			BASE		|	TestBase.getDeclaredMethod("getInt")		||	42
			BASE		|	TestBase.getDeclaredMethod("isBoolean")		||	true
			SUB			|	TestBase.getDeclaredMethod("getString")		||	"qwe"
			SUB			|	TestBase.getDeclaredMethod("getInt")		||	42
			SUB			|	TestBase.getDeclaredMethod("isBoolean")		||	true
			SUB			|	TestSub.getDeclaredMethod("getString")		||	"qaz"
			SUB			|	TestSub.getDeclaredMethod("getDouble")		||	1.5
			SUB			|	TestSub.getDeclaredMethod("getChar")		||	'q'
			SUB			|	TestSub.getMethod("isBoolean")				||	true
			BASE		|	TestSub.getMethod("isBoolean")				||	true
	}

	def "illegal target exception"() {
		given:
			GetterClassProperty prop = new GetterClassProperty(getter)
		when:
			prop.getValue(target)
		then:
			thrown(IllegalArgumentException)
		where:
			target		|	getter
			BASE		|	TestSub.getDeclaredMethod("getString")
			42			|	TestSub.getDeclaredMethod("getString")
			""			|	TestSub.getDeclaredMethod("getString")
	}

	def "null exception"() {
		given:
			GetterClassProperty prop = new GetterClassProperty(TestSub.class.getDeclaredMethod("getString"))

		when:
			prop.getValue(null)
		then:
			thrown(NullPointerException)

		when:
			new GetterClassProperty(null)
		then:
			thrown(NullPointerException)
	}

	def "static getter null target"() {
		given:
			GetterClassProperty member = new GetterClassProperty(getter)
		expect:
			member.getGetter().is(getter)
			member.getValue(target) == value
		where:
			target		|	getter										||	value
			SUB			|	TestBase.getDeclaredMethod("staticGetter")	||	10
			BASE		|	TestSub.getDeclaredMethod("staticGetter")	||	20
			42			|	TestBase.getDeclaredMethod("staticGetter")	||	10
			""			|	TestSub.getDeclaredMethod("staticGetter")	||	20
			null		|	TestBase.getDeclaredMethod("staticGetter")	||	10
			null		|	TestSub.getDeclaredMethod("staticGetter")	||	20
	}

	def "equals/hashCode"() {
		given:
			GetterClassProperty member1 = new GetterClassProperty(getter1, setter1)
			GetterClassProperty member2 = new GetterClassProperty(getter2, setter2)
			boolean equal = getter1.equals(getter2) && setter1.equals(setter2)
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
			[getter1, getter2, setter1, setter2] << [
				[TestBase.getDeclaredMethod("staticGetter"), TestBase.getDeclaredMethod("staticGetter"), null, null],
				[TestBase.getDeclaredMethod("getString"), TestSub.getDeclaredMethod("getString"), null, null],
				[TestBase.getDeclaredMethod("isBoolean"), TestSub.getMethod("isBoolean"), null, null],

				[TestBase.getDeclaredMethod("getString"), TestBase.getDeclaredMethod("getString"),
					TestBase.getDeclaredMethod("setString", String), null],

				[TestBase.getDeclaredMethod("getString"), TestBase.getDeclaredMethod("getString"),
					TestBase.getDeclaredMethod("setString", String), TestBase.getDeclaredMethod("setString", String)],

				[TestSub.getDeclaredMethod("getString"), TestBase.getDeclaredMethod("getString"),
					TestBase.getDeclaredMethod("setString", String), TestBase.getDeclaredMethod("setString", String)],
			]
	}

	def "is-mutable"() {
		given:
			Method getter = TestBase.getDeclaredMethod("getString")
			Method setter = TestBase.getDeclaredMethod("setString", String)
		when:
			GetterClassProperty memberImmutable = new GetterClassProperty(getter)
			GetterClassProperty memberMutable = new GetterClassProperty(getter, setter)
		then:
			memberImmutable.isMutable() == false
			memberMutable.isMutable() == true
	}

	def "set-value"() {
		given:
			TestBase mockBase = Mock(TestBase)
			Method getter = TestBase.getDeclaredMethod("getInt")
			Method setter = TestBase.getDeclaredMethod("setInt", int)
			GetterClassProperty memberMutable = new GetterClassProperty(getter, setter)
			int newInt = 777

		when:
			memberMutable.setValue(mockBase, newInt)
		then:
			1 * mockBase.setInt(newInt) >> newInt

		when:
			def newValue = memberMutable.getValue(mockBase)
		then:
			1 * mockBase.getInt() >> newInt
			newValue == newInt
	}

	def "set-value exception"() {
		given:
			Method getter = TestBase.getDeclaredMethod("getInt")
			Method setter = TestBase.getDeclaredMethod("setInt", int)

		when:
			GetterClassProperty memberImmutable = new GetterClassProperty(getter)
			memberImmutable.setValue(BASE, 777)
		then:
			thrown(UnsupportedOperationException)

		when:
			GetterClassProperty memberInt = new GetterClassProperty(getter, setter)
			memberInt.setValue(BASE, "qwe")
		then:
			thrown(IllegalArgumentException)
	}

	def "illegal setter"() {
		given:
			Method getterBoolean = TestBase.getDeclaredMethod("isBoolean")
		when:
			new GetterClassProperty(getterBoolean, illegalSetter)
		then:
			thrown(IllegalArgumentException)
		where:
			illegalSetter << [
				TestBase.getDeclaredMethod("setBoolean"),
				TestBase.getDeclaredMethod("setBoolean", boolean, boolean),
				TestBase.getDeclaredMethod("setInt", int),
			]
	}

	def "static inconsistent"() {
		given:
			Method instanceGetter = TestBase.getDeclaredMethod("getString")
			Method instanceSetter = TestBase.getDeclaredMethod("setString", String)
			Method staticGetter = TestBase.getDeclaredMethod("staticGetter")
			Method staticSetter = TestBase.getDeclaredMethod("staticSetter", Object)

		when:
			new GetterClassProperty(instanceGetter, staticSetter)
		then:
			thrown(IllegalArgumentException)

		when:
			new GetterClassProperty(staticGetter, instanceSetter)
		then:
			thrown(IllegalArgumentException)
	}

	def "type compatibility"() {

		given:
			Method objectGetter = TestBase.getDeclaredMethod("getObject")
			Method objectSetter = TestBase.getDeclaredMethod("setObject", Object)
			Method stringGetter = TestBase.getDeclaredMethod("getString")
			Method stringSetter = TestBase.getDeclaredMethod("setString", String)
			Method intGetter = TestBase.getDeclaredMethod("getInt")
			Method intSetter = TestBase.getDeclaredMethod("setInt", int)
			Method integerGetter = TestBase.getDeclaredMethod("getInteger")
			Method integerSetter = TestBase.getDeclaredMethod("setInteger", Integer)

		when: "type of the setter argument is more generic than type of the getter result"
			new GetterClassProperty(stringGetter, objectSetter)
		then: "no exception is thrown, cuz all getter results can fit into setter type"
			notThrown(IllegalArgumentException)

		when: "type of the getter is primitive, and type of the setter is corresponding wrapper or its ancestor"
			new GetterClassProperty(intGetter, integerSetter)
			new GetterClassProperty(intGetter, objectSetter)
		then: "no exception is thrown, cuz any getter result can be cast to setter type"
			notThrown(IllegalArgumentException)

		when: "type of the getter is wider than the type of the setter"
			new GetterClassProperty(objectGetter, stringSetter)
		then: "IAE is thrown, for not all getter results can fit into setter"
			thrown(IllegalArgumentException)

		when: "type of the getter is a wrapper type, and type of the setter is the corresponding primitive type"
			new GetterClassProperty(integerGetter, intSetter)
		then: "IAE is thrown, for getter may return null, whic cannot be fit into setter"
			thrown(IllegalArgumentException)
	}

	private static class TestBase {

		private String getString() {
			return "qwe"
		}

		private void setString(String str) {
		}

		private Object getObject() {
		}

		private void setObject(Object o) {
		}

		protected int getInt() {
			return 42
		}

		protected int setInt(int x) {
			return x
		}

		protected Integer getInteger() {
			return 42
		}

		protected void setInteger(Integer x) {
		}

		public boolean isBoolean() {
			return true
		}

		public void setBoolean() {
		}

		public void setBoolean(boolean value, boolean value2) {
		}

		public static Object staticGetter() {
			return 10
		}

		public static void staticSetter(Object o) {
		}
	}

	private static class TestSub extends TestBase {

		private String getString() {
			return "qaz"
		}

		protected double getDouble() {
			return 1.5
		}

		public char getChar() {
			return 'q'
		}

		public void setBooleanSub(boolean value) {
		}

		public static Object staticGetter() {
			return 20
		}
	}

	private static final TestBase BASE = new TestBase()
	private static final TestSub SUB = new TestSub()
}
