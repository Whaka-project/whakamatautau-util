package com.whaka.util.reflection

import groovy.transform.PackageScope

import java.lang.reflect.Method

import spock.lang.Specification

import com.whaka.util.reflection.comparison.TestEntities.Methods

class UberMethodsTest extends Specification {

	def "overrides"() {
		expect:
			UberMethods.overrides(a, b) == result
		where:
			a											|	b											|	result
			getMethod("publicGetString", Methods2)		|	getMethod("publicGetString", Methods)		|	true
			getMethod("publicGetString", Methods)		|	getMethod("publicGetString", Methods2)		|	false
			getMethod("publicGetString", Methods2)		|	getMethod("publicGetString", Methods2)		|	false
			getMethod("publicGetString", Methods2)		|	getMethod("protectedGetString", Methods)	|	false
			getMethod("protectedGetString", Methods2)	|	getMethod("protectedGetString", Methods)	|	true
			getMethod("protectedGetString", Methods)	|	getMethod("protectedGetString", Methods2)	|	false
			getMethod("protectedGetString", Methods2)	|	getMethod("protectedGetString", Methods2)	|	false
			getMethod("defaultGetString", Methods2)		|	getMethod("defaultGetString", Methods)		|	false
			getMethod("defaultGetString", Methods)		|	getMethod("defaultGetString", Methods2)		|	false
			getMethod("defaultGetString", Methods2)		|	getMethod("defaultGetString", Methods2)		|	false
			getMethod("privateGetString", Methods2)		|	getMethod("privateGetString", Methods)		|	false
			getMethod("privateGetString", Methods)		|	getMethod("privateGetString", Methods2)		|	false
			getMethod("privateGetString", Methods2)		|	getMethod("privateGetString", Methods2)		|	false
			getMethod("publicGetCS", Methods2)			|	getMethod("publicGetCS", Methods)			|	true
			getMethod("publicGetCS", Methods)			|	getMethod("publicGetCS", Methods2)			|	false
	}

	def "is-equal-signature"() {
		expect:
			UberMethods.isEqualSignature(a, b) == result
		where:
			a											|	b											|	result
			getMethod("publicGetString", Methods2)		|	getMethod("publicGetString", Methods)		|	true
			getMethod("publicGetString", Methods)		|	getMethod("publicGetString", Methods2)		|	true
			getMethod("publicGetString", Methods2)		|	getMethod("publicGetString", Methods2)		|	true
			getMethod("publicGetString", Methods2)		|	getMethod("protectedGetString", Methods)	|	false
			getMethod("protectedGetString", Methods2)	|	getMethod("protectedGetString", Methods)	|	true
			getMethod("protectedGetString", Methods)	|	getMethod("protectedGetString", Methods2)	|	true
			getMethod("protectedGetString", Methods2)	|	getMethod("protectedGetString", Methods2)	|	true
			getMethod("protectedGetString", Methods2)	|	getMethod("defaultGetString", Methods)		|	false
			getMethod("defaultGetString", Methods2)		|	getMethod("defaultGetString", Methods)		|	true
			getMethod("defaultGetString", Methods)		|	getMethod("defaultGetString", Methods2)		|	true
			getMethod("defaultGetString", Methods2)		|	getMethod("defaultGetString", Methods2)		|	true
			getMethod("defaultGetString", Methods2)		|	getMethod("privateGetString", Methods)		|	false
			getMethod("privateGetString", Methods2)		|	getMethod("privateGetString", Methods)		|	true
			getMethod("privateGetString", Methods)		|	getMethod("privateGetString", Methods2)		|	true
			getMethod("privateGetString", Methods2)		|	getMethod("privateGetString", Methods2)		|	true
			getMethod("privateGetString", Methods2)		|	getMethod("publicGetString", Methods2)		|	false
			getMethod("privateGetString", Methods2)		|	getMethod("publicGetString", Methods)		|	false
	}

	def "is-equal-package"() {
		expect:
			UberMethods.isEqualPackage(a, b) == result
		where:
			a											|	b											|	result
			getMethod("publicGetString", Methods2)		|	getMethod("publicGetString", Methods)		|	false
			getMethod("publicGetString", Methods)		|	getMethod("publicGetString", Methods2)		|	false
			getMethod("publicGetString", Methods2)		|	getMethod("publicGetString", Methods2)		|	true
			getMethod("publicGetString", Methods)		|	getMethod("protectedGetString", Methods)	|	true
			getMethod("protectedGetString", Methods2)	|	getMethod("protectedGetString", Methods)	|	false
			getMethod("protectedGetString", Methods)	|	getMethod("protectedGetString", Methods2)	|	false
			getMethod("protectedGetString", Methods2)	|	getMethod("protectedGetString", Methods2)	|	true
			getMethod("protectedGetString", Methods)	|	getMethod("defaultGetString", Methods)		|	true
			getMethod("defaultGetString", Methods2)		|	getMethod("defaultGetString", Methods)		|	false
			getMethod("defaultGetString", Methods)		|	getMethod("defaultGetString", Methods2)		|	false
			getMethod("defaultGetString", Methods2)		|	getMethod("defaultGetString", Methods2)		|	true
			getMethod("defaultGetString", Methods)		|	getMethod("privateGetString", Methods)		|	true
			getMethod("privateGetString", Methods2)		|	getMethod("privateGetString", Methods)		|	false
			getMethod("privateGetString", Methods)		|	getMethod("privateGetString", Methods2)		|	false
			getMethod("privateGetString", Methods2)		|	getMethod("privateGetString", Methods2)		|	true
			getMethod("privateGetString", Methods2)		|	getMethod("publicGetString", Methods2)		|	true
			getMethod("privateGetString", Methods)		|	getMethod("publicGetString", Methods)		|	true
	}

	private Method getMethod(String name, Class<?> declaringClass) {
		return declaringClass.getDeclaredMethod(name)
	}

	static class Methods2 extends Methods {

		@Override
		public String publicGetCS() {
			return ""
		}

		@Override
		public String publicGetString() {
			return super.publicGetString()
		}

		@Override
		protected String protectedGetString() {
			return super.protectedGetString()
		}

		@SuppressWarnings("all")
		@PackageScope String defaultGetString() {
			return "qwe"
		}

		private String privateGetString() {
			return ""
		}
	}
}
