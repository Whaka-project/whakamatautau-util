package com.whaka.asserts

import spock.lang.Specification

import com.whaka.util.reflection.comparison.ComparisonFail
import com.whaka.util.reflection.comparison.ComparisonPerformers
import com.whaka.util.reflection.comparison.ComparisonResult
import com.whaka.util.reflection.comparison.ComplexComparisonResult
import com.whaka.util.reflection.properties.ClassPropertyKey

class ComparisonAssertResultTest extends Specification {

	def "construction"() {
		given:
			String message = _message
			ComparisonResult result = _result
			ComparisonAssertResult assertResult1 = new ComparisonAssertResult(result)
			ComparisonAssertResult assertResult2 = new ComparisonAssertResult(result, message)
		expect:
			assertResult1.getComparisonResult().is(result)
			assertResult2.getComparisonResult().is(result)
			assertResult1.getActual().is(result.getActual())
			assertResult2.getActual().is(result.getActual())
			assertResult1.getExpected().is(result.getExpected())
			assertResult2.getExpected().is(result.getExpected())
			assertResult1.getMessage() == null
			assertResult2.getMessage() == message
			assertResult1.getCause() == null
			assertResult2.getCause() == null
		where:
			_message	|	_result
			null		|	new ComparisonResult(null, null, null, false)
			null		|	new ComparisonResult(null, null, null, false)
			null		|	new ComparisonResult(1, 2, null, false)
			""			|	new ComparisonResult(1, 2, null, false)
			""			|	new ComparisonResult(1, 2, ComparisonPerformers.DEEP_EQUALS, false)
			"que?"		|	new ComplexComparisonResult("qwe", "rty", null, [(new ClassPropertyKey("prop")): new ComparisonResult(false, false, null, false)])
			"FAIL"		|	new ComparisonFail(true, true, null, new RuntimeException())
	}

	def "create-with-cause"() {
		given:
			String message = _message
			ComparisonResult result = _result
			ComparisonAssertResult assertResult1 = ComparisonAssertResult.createWithCause(result)
			ComparisonAssertResult assertResult2 = ComparisonAssertResult.createWithCause(result, message)
		and:
			def cause = result instanceof ComparisonFail ? result.getCause() : null
		expect:
			assertResult1.getComparisonResult().is(result)
			assertResult2.getComparisonResult().is(result)
			assertResult1.getActual().is(result.getActual())
			assertResult2.getActual().is(result.getActual())
			assertResult1.getExpected().is(result.getExpected())
			assertResult2.getExpected().is(result.getExpected())
			assertResult1.getMessage() == null
			assertResult2.getMessage() == message
			assertResult1.getCause().is(cause)
			assertResult2.getCause().is(cause)
		where:
			_message	|	_result
			null		|	new ComparisonResult(null, null, null, false)
			null		|	new ComparisonResult(null, null, null, false)
			null		|	new ComparisonResult(1, 2, null, false)
			""			|	new ComparisonResult(1, 2, null, false)
			""			|	new ComparisonResult(1, 2, ComparisonPerformers.DEEP_EQUALS, false)
			"que?"		|	new ComplexComparisonResult("qwe", "rty", null, [(new ClassPropertyKey("prop")): new ComparisonResult(false, false, null, false)])
			"FAIL"		|	new ComparisonFail(true, true, null, new RuntimeException())
	}
}
