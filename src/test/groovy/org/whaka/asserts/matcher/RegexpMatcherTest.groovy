package org.whaka.asserts.matcher

import java.util.regex.Pattern

import spock.lang.Specification

class RegexpMatcherTest extends Specification {

	def "construction"() {
		given:
			def pattern = Pattern.compile(".")
		when:
			def m = new RegexpMatcher(pattern)
		then:
			m.getPattern().is(pattern)
	}

	def "construction NPE"() {
		when:
			new RegexpMatcher(null)
		then:
			thrown(NullPointerException)
	}

	def "static matching: pattern"() {
		given:
			Pattern pattern = Pattern.compile(".")
		when:
			RegexpMatcher m = RegexpMatcher.matching(pattern as Pattern)
		then:
			m.getPattern().is(pattern)
	}

	def "static matching: string"() {
		when:
			RegexpMatcher m = RegexpMatcher.matching(pattern as String)
		then:
			pattern == "${m.getPattern()}"
		where:
			pattern << ["", ".", "\\s+", "[qwe]"]
	}

	def "static matching: NPE"() {

		when:
			RegexpMatcher.matching(null as Pattern)
		then:
			thrown(NullPointerException)

		when:
			RegexpMatcher.matching(null as String)
		then:
			thrown(NullPointerException)
	}

	def "matches"() {
		expect:
			RegexpMatcher.matching(pattern as String).matches(item as Object) == result
		where:
			pattern				|	item			||	result
			"."					|	"a"				||	true
			"."					|	"z"				||	true
			"."					|	"az"			||	false
			".."				|	"az"			||	true
			".{2}"				|	"az"			||	true
			".{2,3}"			|	"az"			||	true
			".{2,3}"			|	"azz"			||	true
			".{2,3}"			|	"azzz"			||	false
			".*"				|	12				||	true
			"12"				|	12				||	true
			"[12]{2}"			|	12				||	true
			"fa.*"				|	false			||	true
			"null"				|	null			||	true
			"[alnu]{4}"			|	null			||	true
			"\\{\\}"			|	new HashMap()	||	true
			"\\[\\]"			|	new HashSet()	||	true
	}
}
