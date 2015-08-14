package com.whaka.util

import spock.lang.Specification

class ComparatorsTest extends Specification {

	def "nulls-start"() {
		given:
			Comparator<?> comp = Comparators.nullsStart()

		expect:
			comp.compare(comparableA, comparableB) == result
			comp.compare(comparableB, comparableA) == -result
			comp.compare(null, comparableA) == -1
			comp.compare(null, comparableB) == -1
			comp.compare(comparableA, null) == 1
			comp.compare(comparableB, null) == 1
		and:
			comp.compare(null, null) == 0

		where:
			comparableA		|	comparableB		||	result
			1				|	2				||	-1
			5.5				|	5.4				||	1
			""				|	"qwe"			||	-1
			42				|	42				||	0
	}

	def "nulls-end"() {
		given:
			Comparator<?> comp = Comparators.nullsEnd()

		expect:
			comp.compare(comparableA, comparableB) == result
			comp.compare(comparableB, comparableA) == -result
			comp.compare(null, comparableA) == 1
			comp.compare(null, comparableB) == 1
			comp.compare(comparableA, null) == -1
			comp.compare(comparableB, null) == -1
		and:
			comp.compare(null, null) == 0

		where:
			comparableA		|	comparableB		||	result
			1				|	2				||	-1
			5.5				|	5.4				||	1
			""				|	"qwe"			||	-1
			42				|	42				||	0
	}
}
