package org.whaka.asserts.builder

import java.util.function.Consumer
import java.util.regex.Pattern

import spock.lang.Specification

import org.whaka.TestData
import org.whaka.asserts.AssertResult

class StringAssertPerformerTest extends Specification {

	def "construction"() {
		given:
			Consumer<AssertResult> consumer = Mock()
		when:
			def performer = new StringAssertPerformer(actual, consumer)
		then:
			0 * consumer.accept(_)
		and:
			performer.getActual().is(actual)
			performer.getConsumer().is(consumer)
		where:
			actual << TestData.variousMessages()
	}

	def "isEmpty success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer("", consumer)
		when:
			def messageConstructor = performer.isEmpty()
		then:
			0 * consumer.accept(_)
		and:
			messageConstructor == AssertResultConstructor.EMPTY
	}

	def "isEmpty fail"() {
		given:
			AssertResult captured = null
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.isEmpty()
		then:
			1 * consumer.accept(_) >> {captured = it[0]}
		and:
			messageConstructor.getAssertResult().is(captured)
		and:
			captured.getActual().is(actual)
			captured.getExpected() == "Empty string"
			captured.getMessage() == StringAssertPerformer.MESSAGE_EMPTY_STRING
			captured.getCause() == null
		where:
			actual << [null, " ", "qwe", "1313131313131321"]
	}

	def "isNullOrEmpty success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.isNullOrEmpty()
		then:
			0 * consumer.accept(_)
		and:
			messageConstructor == AssertResultConstructor.EMPTY
		where:
			actual << [null, ""]
	}

	def "isNullOrEmpty fail"() {
		given:
			AssertResult captured = null
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.isNullOrEmpty()
		then:
			1 * consumer.accept(_) >> {captured = it[0]}
		and:
			messageConstructor.getAssertResult().is(captured)
		and:
			captured.getActual().is(actual)
			captured.getExpected() == "Empty or null string"
			captured.getMessage() == StringAssertPerformer.MESSAGE_EMPTY_OR_NULL_STRING
			captured.getCause() == null
		where:
			actual << [" ", "qwe", "1313131313131321"]
	}

	def "isNotEmpty success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.isNotEmpty()
		then:
			0 * consumer.accept(_)
		and:
			messageConstructor == AssertResultConstructor.EMPTY
		where:
			actual << [" ", "qwe", "3131313132131313"]
	}

	def "isNotEmpty fail"() {
		given:
			AssertResult captured = null
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.isNotEmpty()
		then:
			1 * consumer.accept(_) >> {captured = it[0]}
		and:
			messageConstructor.getAssertResult().is(captured)
		and:
			captured.getActual().is(actual)
			captured.getExpected() == "Not empty string"
			captured.getMessage() == StringAssertPerformer.MESSAGE_NOT_EMPTY_STRING
			captured.getCause() == null
		where:
			actual << [null, ""]
	}

	def "isLength success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.isLength(actual.length())
		then:
			0 * consumer.accept(_)
		and:
			messageConstructor == AssertResultConstructor.EMPTY
		where:
			actual << ["", " ", "qwe", "2313213131313"]
	}

	def "isLength fail"() {
		given:
			AssertResult captured = null
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.isLength(length)
		then:
			1 * consumer.accept(_) >> {captured = it[0]}
		and:
			messageConstructor.getAssertResult().is(captured)
		and:
			captured.getActual().is(actual)
			captured.getExpected() == "Length: $length"
			captured.getMessage() == StringAssertPerformer.MESSAGE_STRING_LENGTH
			captured.getCause() == null
		where:
			actual			|	length
			null			|	0
			null			|	-1
			null			|	1
			""				|	-1
			""				|	1
			"qwe"			|	2
			"qwe"			|	4
	}

	def "contains success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.contains(part)
		then:
			0 * consumer.accept(_)
		and:
			messageConstructor == AssertResultConstructor.EMPTY
		where:
			actual			|	part
			""				|	""
			" "				|	" "
			"qwe"			|	"qwe"
			"qwe"			|	"qw"
			"qwe"			|	"we"
			"qwe"			|	"q"
			"qwe"			|	"w"
			"qwe"			|	"e"
			"QWE"			|	"W"
	}

	def "contains fail"() {
		given:
			AssertResult captured = null
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.contains(part)
		then:
			1 * consumer.accept(_) >> {captured = it[0]}
		and:
			messageConstructor.getAssertResult().is(captured)
		and:
			captured.getActual().is(actual)
			captured.getExpected() == "Containing: '$part'"
			captured.getMessage() == StringAssertPerformer.MESSAGE_STRING_NOT_MATCH
			captured.getCause() == null
		where:
			actual			|	part
			null			|	""
			null			|	" "
			""				|	" "
			"qwe"			|	"r"
			"QWE"			|	"qwe"
			"QWE"			|	"q"
			"QWE"			|	"e"
	}

	def "startsWith success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.startsWith(part)
		then:
			0 * consumer.accept(_)
		and:
			messageConstructor == AssertResultConstructor.EMPTY
		where:
			actual			|	part
			""				|	""
			" "				|	" "
			"qwe"			|	"qwe"
			"qwe"			|	"qw"
			"qwe"			|	"q"
			"QWE"			|	"Q"
	}

	def "startsWith fail"() {
		given:
			AssertResult captured = null
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.startsWith(part)
		then:
			1 * consumer.accept(_) >> {captured = it[0]}
		and:
			messageConstructor.getAssertResult().is(captured)
		and:
			captured.getActual().is(actual)
			captured.getExpected() == "Starts with: '$part'"
			captured.getMessage() == StringAssertPerformer.MESSAGE_STRING_NOT_MATCH
			captured.getCause() == null
		where:
			actual			|	part
			null			|	""
			null			|	" "
			""				|	" "
			"qwe"			|	"we"
			"qwe"			|	"w"
			"qwe"			|	"r"
			"QWE"			|	"qwe"
			"QWE"			|	"q"
	}

	def "endsWith success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.endsWith(part)
		then:
			0 * consumer.accept(_)
		and:
			messageConstructor == AssertResultConstructor.EMPTY
		where:
			actual			|	part
			""				|	""
			" "				|	" "
			"qwe"			|	"qwe"
			"qwe"			|	"we"
			"qwe"			|	"e"
			"QWE"			|	"E"
	}

	def "endsWith fail"() {
		given:
			AssertResult captured = null
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.endsWith(part)
		then:
			1 * consumer.accept(_) >> {captured = it[0]}
		and:
			messageConstructor.getAssertResult().is(captured)
		and:
			captured.getActual().is(actual)
			captured.getExpected() == "Ends with: '$part'"
			captured.getMessage() == StringAssertPerformer.MESSAGE_STRING_NOT_MATCH
			captured.getCause() == null
		where:
			actual			|	part
			null			|	""
			null			|	" "
			""				|	" "
			"qwe"			|	"qw"
			"qwe"			|	"w"
			"qwe"			|	"r"
			"QWE"			|	"qwe"
			"QWE"			|	"e"
	}

	def "matches success"() {
		given:
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)
		when:
			def messageConstructor = performer.matches((String) pattern)
		then:
			0 * consumer.accept(_)
		and:
			messageConstructor == AssertResultConstructor.EMPTY

		when:
			def messageConstructor2 = performer.matches((Pattern) Pattern.compile(pattern))
		then:
			0 * consumer.accept(_)
		and:
			messageConstructor2 == AssertResultConstructor.EMPTY

		where:
			actual			|	pattern
			""				|	""
			" "				|	"\\s"
			"qwe"			|	"[qwe]{3}"
			"qwe"			|	"qwe"
			"qwe"			|	"q.e"
			"qwe"			|	"..."
			"QWE"			|	"Q.*"
			"QWE"			|	"(?i)q[we]*"
	}

	def "matches string fail"() {

		given:
			AssertResult captured = null
			Consumer<AssertResult> consumer = Mock()
			def performer = new StringAssertPerformer(actual, consumer)

		when:
			def messageConstructor = performer.matches((String) pattern)
		then:
			1 * consumer.accept(_) >> {captured = it[0]}
		and:
			messageConstructor.getAssertResult().is(captured)
		and:
			captured.getActual().is(actual)
			captured.getExpected() == "Matching: '$pattern'"
			captured.getMessage() == StringAssertPerformer.MESSAGE_STRING_NOT_MATCH
			captured.getCause() == null

		when:
			def messageConstructor2 = performer.matches((Pattern) Pattern.compile(pattern))
		then:
			1 * consumer.accept(_) >> {captured = it[0]}
		and:
			messageConstructor2.getAssertResult().is(captured)
		and:
			captured.getActual().is(actual)
			captured.getExpected() == "Matching: '$pattern'"
			captured.getMessage() == StringAssertPerformer.MESSAGE_STRING_NOT_MATCH
			captured.getCause() == null

		where:
			actual			|	pattern
			null			|	""
			null			|	" "
			""				|	" "
			""				|	"\\s+"
			"qwe"			|	"[QWE]{3}"
			"qwe"			|	"[qwe]{2}"
			"qwe"			|	"q.r"
			"QWE"			|	"qwe"
			"QWE"			|	"QW[qwe]"
	}
}
