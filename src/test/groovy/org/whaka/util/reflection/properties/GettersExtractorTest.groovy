package org.whaka.util.reflection.properties

import spock.lang.Specification

class GettersExtractorTest extends Specification {

	def "extract all - contains getters"() {
		given:
			GettersExtractor extractor = new GettersExtractor()

		when:
			def extracted = extractor.extractAll(A).keySet()
		then:
			contains(extracted, "getDouble()", A)
			contains(extracted, "getBoolean()", A)
			contains(extracted, "getString()", A)
			contains(extracted, "getInt()", A)
			contains(extracted, "someChar()", A)		// static
			contains(extracted, "toString()", A)
			contains(extracted, "clone()", Object)
			contains(extracted, "hashCode()", Object)
			contains(extracted, "getClass()", Object)

		when:
			def extractedB = extractor.extractAll(B).keySet()
		then:
			contains(extractedB, "getInt()", B)
			contains(extractedB, "getString()", B)
			contains(extractedB, "getBoolean()", B)
			contains(extractedB, "getDouble()", A)
			contains(extractedB, "getBoolean()", A)
			contains(extractedB, "getString()", A)
			contains(extractedB, "someChar()", A)		// static
			contains(extractedB, "toString()", A)
			contains(extractedB, "clone()", Object)
			contains(extractedB, "hashCode()", Object)
			contains(extractedB, "getClass()", Object)
	}

	def "extract all - not contains voids"() {
		given:
			GettersExtractor extractor = new GettersExtractor()
		when:
			def extracted = extractor.extractAll(A).keySet()
		then:
			notContains(extracted, "getVoid()", A)
			notContains(extracted, "notify()", Object)
			notContains(extracted, "notifyAll()", Object)
			notContains(extracted, "wait()", Object)
			notContains(extracted, "finalize()", Object)
	}

	def "extract all - not contains methods with parameters"() {
		given:
			GettersExtractor extractor = new GettersExtractor()
		when:
			def extracted = extractor.extractAll(A).keySet()
		then:
			notContains(extracted, "getStringWithParam()", A)
			notContains(extracted, "equals()", Object)
	}

	def "extract all - not contains overriden copy"() {
		given:
			GettersExtractor extractor = new GettersExtractor()

		when:
			def extracted = extractor.extractAll(A).keySet()
		then:
			contains(extracted, "toString()", A)
			notContains(extracted, "toString()", Object)
		and:
			contains(extracted, "getString()", A)
			contains(extracted, "getInt()", A)

		when:
			def extractedB = extractor.extractAll(B).keySet()
		then:
			contains(extractedB, "getInt()", B)
			notContains(extractedB, "getInt()", A)
		and:
			contains(extractedB, "toString()", A)
			notContains(extractedB, "toString()", Object)
		and:
			contains(extractedB, "getString()", A)
			contains(extractedB, "getString()", B)

		when:
			def extractedC = extractor.extractAll(C).keySet()
		then:
			contains(extractedC, "toString()", A)
			notContains(extractedC, "toString()", Object)
		and:
			contains(extractedC, "getInt()", B)
			notContains(extractedC, "getInt()", A)
		and:
			contains(extractedC, "getString()", C)
			contains(extractedC, "getString()", A)
			notContains(extractedC, "getString()", B)
	}

	def "extract all - interface linearization"() {
		given:
			GettersExtractor extractor = new GettersExtractor()

		when:
			def extractedI = extractor.extractAll(I).keySet()
		then:
			contains(extractedI, "iString()", I)

		when:
			def extractedZ = extractor.extractAll(Z).keySet()
		then:
			contains(extractedZ, "zString()", Z)

		when:
			def extractedI2 = extractor.extractAll(I2).keySet()
		then:
			contains(extractedI2, "iString()", I)
			contains(extractedI2, "zString()", Z)
			contains(extractedI2, "i2String()", I2)
	}

	void contains(Set<ClassPropertyKey> keys, String name, Class<?> declaringClass) {
		assert keys.contains(new ClassPropertyKey(name, declaringClass))
	}

	void notContains(Set<ClassPropertyKey> keys, String name, Class<?> declaringClass) {
		assert keys.contains(new ClassPropertyKey(name, declaringClass)) == false
	}

	static interface I {
		public String iString()
	}

	static interface Z {
		public String zString()
	}

	static interface I2 extends I, Z {
		public String i2String()
	}


	static class A {

		private void getVoid() {
		}

		private String getStringWithParam(String param) {
			return "String with " + param
		}

		private double getDouble() {
			return 1.5
		}

		private boolean getBoolean() {
			return true
		}

		private CharSequence getString() {
			return "strA"
		}

		protected int getInt() {
			return 42
		}

		public static char someChar() {
			return 'q'
		}

		@Override
		String toString() {
			return "AAAA"
		}
	}

	static class B extends A {

		private boolean getBoolean() {
			return false
		}

		protected String getString() {
			return "strB"
		}

		@Override
		protected int getInt() {
			return 88
		}
	}

	static class C extends B {

		@Override
		public String getString() {
			return "strC"
		}
	}

	private static final A instanceA = new A()
	private static final B instanceB = new B()
	private static final C instanceC = new C()
}
