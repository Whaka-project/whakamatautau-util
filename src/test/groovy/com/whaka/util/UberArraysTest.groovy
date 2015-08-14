package com.whaka.util

import org.spockframework.runtime.ConditionNotSatisfiedError

import spock.lang.Specification

class UberArraysTest extends Specification {

	def "eliminate-collections"() {
		given:
			List<Object> list = [
				arr(1,2),
				[
					arr(3,4),
					arr(5,6),
					[
						arr(7,8),
						arr(9,0)
					]
				]
			]

		when:
			checkNoCollections(list.toArray())
		then:
			thrown(ConditionNotSatisfiedError)

		when:
			Object[] arr = UberCollections.toArrayRecursive(list)
			checkNoCollections(arr)
		then:
			notThrown(ConditionNotSatisfiedError)
	}

	private void checkNoCollections(Object[] arr) {
		for (Object o : arr) {
			assert !(o instanceof Collection)
			if (o instanceof Object[])
				checkNoCollections(o as Object[])
		}
	}

	def "get-array-depth"() {
		expect:
			UberArrays.getArrayDepth(type) == depth
		where:
			type						|	depth
			Object						|	0
			Object[]					|	1
			Object[][]					|	2
			Object[][][]				|	3
			Object[][][][]				|	4
			Object[][][][][]			|	5
			Object[][][][][][]			|	6
			Object[][][][][][][]		|	7
			Object[][][][][][][][]		|	8
	}

	def "create-array-class"() {
		expect:
			UberArrays.createArrayClass(type, depth) == arrayType
		where:
			type		|	depth		||	arrayType
			Object		|	0			||	Object
			Object		|	-1			||	Object
			Object		|	-2			||	Object
			Object		|	1			||	Object[]
			Object		|	2			||	Object[][]
			Object		|	7			||	Object[][][][][][][]
			String		|	2			||	String[][]
			String[]	|	2			||	String[][][]
			String[][]	|	2			||	String[][][][]
			String[][]	|	0			||	String[][]
			String[][]	|	-1			||	String[]
			String[][]	|	-2			||	String
			String[][]	|	-3			||	String
			String[]	|	-3			||	String
			String		|	-3			||	String
			int			|	4			||	int[][][][]
			int[][][][]	|	-3			||	int[]
	}

	def "create-array-class VS array-depth"() {
		when:
			def type = UberArrays.createArrayClass(Object, depth)
		then:
			UberArrays.getArrayDepth(type) == depth
		where:
			depth << [1,10,100,200,255]
	}

	def "to-string"() {
		expect:
			UberArrays.toString(value) == result
		where:
			value										|	result
			null										|	"null"
			false										|	"false"
			12											|	"12"
			'q'											|	"q"
			12L											|	"12"
			15.5 as double								|	"15.5"
			""											|	""
			[1,2,3] as List<Integer>					|	"[1, 2, 3]"
			["qwe", [1,2,3] as int[]] as Object[]		|	"[qwe, [1, 2, 3]]"
			[true, false] as boolean[]					|	"[true, false]"
			[1,2,3] as byte[]							|	"[1, 2, 3]"
			[1,2,3] as short[]							|	"[1, 2, 3]"
			['q', 'w', 'e'] as char[]					|	"[q, w, e]"
			[1,2,3] as int[]							|	"[1, 2, 3]"
			[1L,2L,3L] as long[]						|	"[1, 2, 3]"
			[1F,2F,3F] as float[]						|	"[1.0, 2.0, 3.0]"
			[1.0,2.0,3.0] as double[]					|	"[1.0, 2.0, 3.0]"
			[[1,2,3],[4,5,6]] as int[][]				|	"[[1, 2, 3], [4, 5, 6]]"
	}

	private int[] arr(int[] arr) {
		return arr
	}
}
