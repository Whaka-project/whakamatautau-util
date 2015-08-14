package com.whaka.util

import spock.lang.Specification

class UberMathTest extends Specification {

	def "nChooseK: Pascal's triangle"() {
		expect:
			UberMath.nChooseK(n, k) == result
		where:
			n	|	k	|	result
			4	|	2	|	6
			5	|	2	|	10
			5	|	3	|	10
			6	|	2	|	15
			6	|	3	|	20
			6	|	4	|	15
			7	|	2	|	21
			7	|	3	|	35
			7	|	4	|	35
			7	|	5	|	21
			8	|	2	|	28
			8	|	3	|	56
			8	|	4	|	70
			8	|	5	|	56
			8	|	6	|	28
	}

	def "nChooseK: choose 1 from any n > 0 == n"() {
		expect:
			UberMath.nChooseK(n, 1) == n
		where:
			n << (1..100)
	}

	def "nChooseK: choose n - 1 from any n > 0 == n"() {
		expect:
			UberMath.nChooseK(n, n - 1) == n
		where:
			n << (1..100)
	}

	def "nChooseK: choose 0 from any n == 1"() {
		expect:
			UberMath.nChooseK(n, 0) == 1
		where:
			n << (0..100)
	}

	def "nChooseK: choose n from any n == 1"() {
		expect:
			UberMath.nChooseK(n, n) == 1
		where:
			n << (0..100)
	}
}
