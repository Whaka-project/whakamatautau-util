package org.whaka.asserts

import static org.whaka.TestData.*
import spock.lang.Specification

class AssertErrorTest extends Specification {

	def "constructing error"() {
		given:
			List<AssertResult> results = createAssertResultsList(actual, expected, message, cause)
		when:
			AssertError error1 = new AssertError(results)
			AssertError error2 = new AssertError(results)
		then:
			error1.equals(error2)
			error1.hashCode() == error2.hashCode()
			error1.getResults().equals(results)
			error2.getResults().equals(results)
		where:
			actual << variousObjects()
			expected << variousObjects().reverse()
			message << variousMessages()
			cause << variousCauses()
	}

	def "no results exception"() {
		when: "empty collection is specified"
			AssertError error = new AssertError([])
		then: "IAE is thrown, cuz error must contain at least one result"
			thrown(IllegalArgumentException)
	}

	def "null result exception"() {
		when: "null is specified instead of a collection"
			new AssertError(null)
		then: "NPE is thrown, cuz empty collection is not a valid case also"
			thrown(NullPointerException)

		when: "specified collection contains null"
			new AssertError([new AssertResult(), null])
		then: "NPE is thrown, cuz null assert result is not a valid case"
			thrown(NullPointerException)
	}
}
