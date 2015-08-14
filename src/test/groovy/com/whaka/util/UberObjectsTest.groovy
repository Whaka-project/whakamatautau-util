package com.whaka.util

import spock.lang.Specification

class UberObjectsTest extends Specification {

	def "to-string"() {
		expect:
			UberObjects.toString(value) == "$value"
		where:
			value << [null, false, 12, 'q', 12L, 15.5, "",
				[1,2,3] as int[],
				[1,2,3] as List<Integer>,
				[[1,2,3],[4,5,6]] as int[][],
				["qwe", [1,2,3] as int[]] as Object[]
			]
	}
}
