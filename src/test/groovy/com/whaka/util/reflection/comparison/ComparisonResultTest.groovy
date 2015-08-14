package com.whaka.util.reflection.comparison

import spock.lang.Specification

class ComparisonResultTest extends Specification {

	def "construction"() {
		given:
			ComparisonResult result = new ComparisonResult(actual, expected, comparisonPerformer, success)
		expect:
			result.getActual().is(actual)
			result.getExpected().is(expected)
			result.getComparisonPerformer().is(comparisonPerformer)
			result.isSuccess() == success
		where:
			actual		|	expected		|	comparisonPerformer			|	success
			null		|	null			|	null						|	false
			null		|	null			|	null						|	true
			null		|	null			|	Mock(ComparisonPerformer)	|	false
			""			|	null			|	Mock(ComparisonPerformer)	|	false
			null		|	""				|	Mock(ComparisonPerformer)	|	true
			""			|	""				|	Mock(ComparisonPerformer)	|	true
			12			|	""				|	Mock(ComparisonPerformer)	|	true
	}
}
