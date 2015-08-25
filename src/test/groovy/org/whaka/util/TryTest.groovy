package org.whaka.util

import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function

import org.hamcrest.Matcher
import org.whaka.TestData
import org.whaka.asserts.AssertError
import org.whaka.asserts.AssertResult
import org.whaka.util.function.DangerousBiFunction
import org.whaka.util.function.DangerousConsumer
import org.whaka.util.function.DangerousFunction
import org.whaka.util.function.DangerousRunnable
import org.whaka.util.function.DangerousSupplier

import spock.lang.Specification

class TryTest extends Specification {

	private static Throwable[] CAUSES = TestData.variousCauses().findAll {e -> e instanceof Exception}

	def "perform runnable success"() {
		given:
			DangerousRunnable code = Mock()
		when:
			Try t = Try.run(code)
		then:
			1 * code.run()
		and:
			t.getResult() == null
			t.getCause() == null
			t.isSuccess() == true
	}

	def "perform runnable fail"() {
		given:
			DangerousRunnable code = Mock()
		when:
			Try t = Try.run(code)
		then:
			1 * code.run() >> {throw cause}
		and:
			t.getResult() == null
			t.getCause() == cause
			t.isSuccess() == false
		where:
			cause << CAUSES
	}

	def "perform supplier success"() {
		given:
			DangerousSupplier<?> code = Mock()
		when:
			Try t = Try.perform(code)
		then:
			1 * code.get() >> result
		and:
			t.getResult() == result
			t.getCause() == null
			t.isSuccess() == true
		where:
			result << TestData.variousObjects()
	}

	def "perform supplier fail"() {
		given:
			DangerousSupplier<?> code = Mock()
		when:
			Try t = Try.perform(code)
		then:
			1 * code.get() >> {throw cause}
		and:
			t.getResult() == null
			t.getCause() == cause
			t.isSuccess() == false
		where:
			cause << CAUSES
	}

	def "perform with resource - success"() {
		given:
			AutoCloseable resource = Mock()
			DangerousSupplier<?> resourceSupplier = Mock()
			DangerousFunction<?, ?> code = Mock()
		when:
			Try t = Try.withResource(resourceSupplier, code)
		then:
			1 * resourceSupplier.get() >> resource
			1 * code.apply(resource) >> "qwe"
			1 * resource.close()
		and:
			t.getResult() == "qwe"
			t.getCause() == null
			t.isSuccess() == true
	}

	def "perform with resource - perform fail"() {
		given:
			AutoCloseable resource = Mock()
			DangerousSupplier<?> resourceSupplier = Mock()
			DangerousFunction<?, ?> code = Mock()
		when:
			Try t = Try.withResource(resourceSupplier, code)
		then:
			1 * resourceSupplier.get() >> resource
			1 * code.apply(resource) >> {throw cause}
			1 * resource.close()
		and:
			t.getResult() == null
			t.getCause() == cause
			t.isSuccess() == false
		where:
			cause << CAUSES
	}

	def "perform with resource - resource supplier fail"() {
		given:
			AutoCloseable resource = Mock()
			DangerousSupplier<?> resourceSupplier = Mock()
			DangerousFunction<?, ?> code = Mock()
		when:
			Try t = Try.withResource(resourceSupplier, code)
		then:
			1 * resourceSupplier.get() >> {throw cause}
			0 * code.apply(resource)
			0 * resource.close()
		and:
			t.getResult() == null
			t.getCause() == cause
			t.isSuccess() == false
		where:
			cause << CAUSES
	}

	def "perform with resource - resource close fail"() {
		given:
			AutoCloseable resource = Mock()
			DangerousSupplier<?> resourceSupplier = Mock()
			DangerousFunction<?, ?> code = Mock()
		when:
			Try t = Try.withResource(resourceSupplier, code)
		then:
			1 * resourceSupplier.get() >> resource
			1 * code.apply(resource) >> "qwe"
			1 * resource.close() >> {throw cause}
		and:
			t.getResult() == "qwe"
			t.getCause() == null
			t.isSuccess() == true
			t.getSuppressed() == cause
		where:
			cause << CAUSES
	}

	def "perform with resource - both perform and resource close fail"() {
		given:
			AutoCloseable resource = Mock()
			DangerousSupplier<?> resourceSupplier = Mock()
			DangerousFunction<?, ?> code = Mock()
			def performException = new RuntimeException("PerformException")
			def closeException = new RuntimeException("CloseException")
		when:
			Try t = Try.withResource(resourceSupplier, code)
		then:
			1 * resourceSupplier.get() >> resource
			1 * code.apply(resource) >> {throw performException}
			1 * resource.close() >> {throw closeException}
		and:
			t.getResult() == null
			t.getCause() == performException
			t.getSuppressed() == closeException
			t.isSuccess() == false
	}

	def "getResult"() {

		given:
			def cause = new RuntimeException("some")

		expect:
			Try.run((DangerousRunnable) { } ).getResult()									== null
			Try.run((DangerousRunnable) { throw cause } ).getResult()						== null
			Try.perform((DangerousSupplier) { "qwe" } ).getResult()							== "qwe"
			Try.perform((DangerousSupplier) { null } ).getResult()							== null
			Try.perform((DangerousSupplier) { throw cause } ).getResult()					== null
			Try.withResource({ (AutoCloseable){} }, {res -> 42}).getResult()				== 42
			Try.withResource({ (AutoCloseable){} }, {res -> null}).getResult()				== null
			Try.withResource({ throw cause }, {res -> 42}).getResult()						== null
			Try.withResource({ (AutoCloseable){} }, {res -> throw cause}).getResult()		== null
			Try.withResource({ (AutoCloseable){ throw cause } }, {res -> 42}).getResult()	== 42
			Try.withResource({ (AutoCloseable){ throw cause } }, {res -> null}).getResult()	== null
	}

	def "getOptionalResult"() {

		given:
			def cause = new RuntimeException("some")

		expect:
			Try.run((DangerousRunnable) { } ).getOptionalResult().isPresent()									== false
			Try.run((DangerousRunnable) { throw cause } ).getOptionalResult().isPresent()						== false
			Try.perform((DangerousSupplier) { "qwe" } ).getOptionalResult().isPresent()							== true
			Try.perform((DangerousSupplier) { null } ).getOptionalResult().isPresent()							== false
			Try.perform((DangerousSupplier) { throw cause } ).getOptionalResult().isPresent()					== false
			Try.withResource({ (AutoCloseable){} }, {res -> 42}).getOptionalResult().isPresent()				== true
			Try.withResource({ (AutoCloseable){} }, {res -> null}).getOptionalResult().isPresent()				== false
			Try.withResource({ throw cause }, {res -> 42}).getOptionalResult().isPresent()						== false
			Try.withResource({ (AutoCloseable){} }, {res -> throw cause}).getOptionalResult().isPresent()		== false
			Try.withResource({ (AutoCloseable){ throw cause } }, {res -> 42}).getOptionalResult().isPresent()	== true
			Try.withResource({ (AutoCloseable){ throw cause } }, {res -> null}).getOptionalResult().isPresent()	== false
	}

	def "orElse"() {

		given:
			def cause = new RuntimeException("some")

		expect:
			Try.run((DangerousRunnable) { } ).getOrElse(9000)								== null
			Try.run((DangerousRunnable) { throw cause } ).getOrElse(9000)					== 9000
			Try.perform((DangerousSupplier) { "qwe" } ).getOrElse(9000)							== "qwe"
			Try.perform((DangerousSupplier) { null } ).getOrElse(9000)							== null
			Try.perform((DangerousSupplier) { throw cause } ).getOrElse(9000)					== 9000
			Try.withResource({ (AutoCloseable){} }, {res -> 42}).getOrElse(9000)				== 42
			Try.withResource({ (AutoCloseable){} }, {res -> null}).getOrElse(9000)				== null
			Try.withResource({ throw cause }, {res -> 42}).getOrElse(9000)						== 9000
			Try.withResource({ (AutoCloseable){} }, {res -> throw cause}).getOrElse(9000)		== 9000
			Try.withResource({ (AutoCloseable){ throw cause } }, {res -> 42}).getOrElse(9000)	== 42
			Try.withResource({ (AutoCloseable){ throw cause } }, {res -> null}).getOrElse(9000)	== null
	}

	def "on any result - on success"() {
		given:
			BiConsumer consumer = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { "qwe" })
			Try t2 = t1.onAnyResult(consumer)
		then:
			1 * consumer.accept("qwe", null)
		and:
			t2.is(t1)
	}

	def "on any result - on fail"() {
		given:
			BiConsumer consumer = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { throw cause })
			Try t2 = t1.onAnyResult(consumer)
		then:
			1 * consumer.accept(null, cause)
		and:
			t2.is(t1)
		where:
			cause << CAUSES
	}

	def "on perform success - on success"() {
		given:
			Consumer consumer = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { "qwe" })
			Try t2 = t1.onPerformSuccess(consumer)
		then:
			1 * consumer.accept("qwe")
		and:
			t2.is(t1)
	}

	def "on perform success - on fail"() {
		given:
			Consumer consumer = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { throw cause })
			Try t2 = t1.onPerformSuccess(consumer)
		then:
			0 * consumer.accept(_)
		and:
			t2.is(t1)
		where:
			cause << CAUSES
	}

	def "on perform success try - on success"() {
		given:
			DangerousFunction mapper = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { "qwe" })
			Try t2 = t1.onPerformSuccessTry(mapper)
		then:
			1 * mapper.apply("qwe") >> 42
		and:
			t2 != t1
		and:
			t2.getResult() == 42
			t2.getCause() == null
			t2.getSuppressed() == null
			t2.isSuccess() == true
	}

	def "on perform success try - on fail"() {
		given:
			DangerousFunction mapper = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { throw cause })
			Try t2 = t1.onPerformSuccessTry(mapper)
		then:
			0 * mapper.apply(_)
		and:
			t2 != t1
		and:
			t2.getResult() == null
			t2.getCause() == cause
			t2.getSuppressed() == null
			t2.isSuccess() == false
		where:
			cause << CAUSES
	}

	def "on perform success try - on fail and suppressed"() {
		given:
			AutoCloseable resource = Mock()
			DangerousFunction code = Mock()
			DangerousFunction mapper = Mock()
			def performCause = new RuntimeException("PerformException")
			def closeCause = new RuntimeException("CloseException")
		when:
			Try t1 = Try.withResource({ resource }, code)
			Try t2 = t1.onPerformSuccessTry(mapper)
		then:
			1 * code.apply(resource) >> { throw performCause }
			1 * resource.close() >> { throw closeCause }
			0 * mapper.apply(_)
		and:
			t2 != t1
		and:
			t2.getResult() == null
			t2.getCause() == performCause
			t2.getSuppressed() == closeCause
			t2.isSuccess() == false
	}

	def "on perform success try - mapping fail"() {
		given:
			DangerousFunction mapper = Mock()
			def cause = new RuntimeException("some")
		when:
			Try t1 = Try.perform((DangerousSupplier) { "qwe" })
			Try t2 = t1.onPerformSuccessTry(mapper)
		then:
			1 * mapper.apply("qwe") >> { throw cause }
		and:
			t2 != t1
		and:
			t2.getResult() == null
			t2.getCause() == cause
			t2.getSuppressed() == null
			t2.isSuccess() == false
	}

	def "on perform success try with resource - on success"() {
		given:
			AutoCloseable resource = Mock()
			DangerousSupplier resourceSupplier = Mock()
			DangerousBiFunction mapper = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { "qwe" })
			Try t2 = t1.onPerformSuccessTryWithResource(resourceSupplier, mapper)
		then:
			1 * resourceSupplier.get() >> resource
			1 * mapper.apply(resource, "qwe") >> 42
			1 * resource.close()
		and:
			t2 != t1
		and:
			t2.getResult() == 42
			t2.getCause() == null
			t2.getSuppressed() == null
			t2.isSuccess() == true
	}

	def "on perform success try with resource - on fail"() {
		given:
			AutoCloseable resource = Mock()
			DangerousSupplier resourceSupplier = Mock()
			DangerousBiFunction mapper = Mock()
			def cause = new RuntimeException("some")
		when:
			Try t1 = Try.perform((DangerousSupplier) { throw cause })
			Try t2 = t1.onPerformSuccessTryWithResource(resourceSupplier, mapper)
		then:
			0 * resourceSupplier.get() >> resource
			0 * mapper.apply(resource, "qwe") >> 42
			0 * resource.close()
		and:
			t2 != t1
		and:
			t2.getResult() == null
			t2.getCause() == cause
			t2.getSuppressed() == null
			t2.isSuccess() == false
	}

	def "on perform fail - on success"() {
		given:
			Consumer consumer = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { "qwe" })
			Try t2 = t1.onPerformFail(consumer)
		then:
			0 * consumer.accept(_)
		and:
			t2.is(t1)
	}

	def "on perform fail - on fail"() {
		given:
			Consumer consumer = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { throw cause })
			Try t2 = t1.onPerformFail(consumer)
		then:
			1 * consumer.accept(cause)
		and:
			t2.is(t1)
		where:
			cause << CAUSES
	}

	def "on perform fail dangerous - on fail"() {
		given:
			DangerousConsumer consumer = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { throw cause })
			Try t2 = t1.onPerformFailDangerous(consumer)
		then:
			1 * consumer.accept(cause)
		and:
			t2.is(t1)
		where:
			cause << CAUSES
	}

	def "on perform fail dangerous - direct rethrow"() {
		given:
			DangerousConsumer consumer = Mock()
		when:
			Try.perform((DangerousSupplier) { throw cause }).onPerformFailDangerous(consumer)
		then:
			1 * consumer.accept(cause) >> { throw cause }
		and:
			Exception e = thrown()
			e.is(cause)
		where:
			cause << CAUSES
	}

	def "on perform fail catch - on success"() {
		given:
			DangerousConsumer consumer = Mock()
			def returns = []
		when:
			Try t1 = Try.perform((DangerousSupplier) { "qwe" })
			for (type in catchTypes) { returns << t1.onPerformFailCatch(type, consumer) }
		then:
			0 * consumer.accept(_)
		and:
			returns.each {
				assert it.is(t1)
			}
		where:
			catchTypes << CAUSES.collect { it.getClass() }
	}

	def "on perform fail catch - on fail - catches types"() {
		given:
			DangerousConsumer notCaughtConsumer = Mock()
			DangerousConsumer caughtConsumer = Mock()
			def returnsMap = [:]
		when:
			Try t = Try.perform((DangerousSupplier) { throw cause })
			for (type in notCaughtTypes) {
				returnsMap << [(t) : t.onPerformFailCatch(type, notCaughtConsumer)]
			}
			for (type in caughtTypes) {
				Try t1 = Try.perform((DangerousSupplier) { throw cause })
				returnsMap << [(t1) : t1.onPerformFailCatch(type, caughtConsumer)]
			}
		then:
			caughtTypes.size * caughtConsumer.accept(cause)
			0 * notCaughtConsumer.accept(_)
		and:
			returnsMap.each { k, v ->
				assert k.is(v)
			}
		where:
			cause							|	caughtTypes								|	notCaughtTypes
			(new Exception())				|	[Throwable, Exception]						|	[RuntimeException, IOException, IllegalArgumentException, FileNotFoundException]
			(new RuntimeException())		|	[Throwable, Exception, RuntimeException]	|	[IOException, IllegalArgumentException, FileNotFoundException]
			(new IOException())				|	[Throwable, Exception, IOException]			|	[RuntimeException, IllegalArgumentException, FileNotFoundException]
			(new IllegalArgumentException())|	[Throwable, Exception, RuntimeException, IllegalArgumentException]	|	[IOException, FileNotFoundException]
			(new FileNotFoundException())	|	[Throwable, Exception, IOException, FileNotFoundException]			|	[RuntimeException, IllegalArgumentException]
	}

	def "on perform fail catch - on fail - caught once"() {
		given:
			DangerousConsumer consumer = Mock()
			def returns = []
		when:
			Try t = Try.perform((DangerousSupplier) { throw cause })
			for (type in [RuntimeException, IOException, Exception, Throwable]) {
				returns << t.onPerformFailCatch(type, consumer)
			}
		then:
			1 * consumer.accept(cause)
		and:
			returns.each {
				assert it.is(t)
			}
		where:
			cause << CAUSES
	}

	def "on perform fail catch - on fail - caught once before OR after retry"() {
		given:
			DangerousConsumer consumer = Mock()
		when:
			Try t = Try.perform((DangerousSupplier) { throw cause})
			t.onPerformFailCatch(RuntimeException.class, consumer)
			Try t2 = t.onPerformSuccessTry({ x -> 42})
			t2.onPerformFailCatch(Exception.class, consumer)
		then:
			1 * consumer.accept(cause)
		and:
			t2 != t
		where:
			cause << [new RuntimeException("qwe"), new IOException("rty")]
	}

	def "on perform fail catch - on fail - caught twice on two tries"() {
		given:
			DangerousConsumer consumer = Mock()
		when:
			Try t = Try.perform((DangerousSupplier) { throw cause})
			Try t2 = t.onPerformSuccessTry({ x -> 42})
			t.onPerformFailCatch(Exception.class, consumer)
			t2.onPerformFailCatch(Exception.class, consumer)
		then:
			2 * consumer.accept(cause)
		and:
			t2 != t
		where:
			cause << [new RuntimeException("qwe"), new IOException("rty")]
	}

	def "isCaught"() {
		given:
			DangerousConsumer consumer = Mock()
			Try t = Try.perform((DangerousSupplier) { throw new IOException()})

		expect:
			!t.isCaught()

		when:
			t.onPerformFailCatch(RuntimeException, consumer)
		then:
			0 * consumer.accept(_)
			!t.isCaught()

		when:
			t.onPerformFailCatch(FileNotFoundException, consumer)
		then:
			0 * consumer.accept(_)
			!t.isCaught()

		when:
			t.onPerformFailCatch(IOException, consumer)
		then:
			1 * consumer.accept(_)
			t.isCaught()

		when:
			t.onPerformFailCatch(Exception, consumer)
		then:
			0 * consumer.accept(_)
			t.isCaught()
	}

	def "on perform fail throw - on success"() {
		given:
			Function throwFunction = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { "qwe" })
			Try t2 = t1.onPerformFailThrow(throwFunction)
		then:
			0 * throwFunction.apply(_)
		and:
			t2.is(t1)
	}

	def "on perform fail throw - on fail"() {
		given:
			Function throwFunction = Mock()
		when:
			Try.perform((DangerousSupplier) { throw cause})
				.onPerformFailThrow(throwFunction)
		then:
			1 * throwFunction.apply(cause) >> new RuntimeException("qwe", cause)
		and:
			RuntimeException e = thrown()
			e.getMessage() == "qwe"
			e.getCause() == cause
		where:
			cause << CAUSES
	}

	def "on perform fail rethrow - on success"() {
		when:
			Try t1 = Try.perform((DangerousSupplier) { "qwe" })
			Try t2 = t1.onPerformFailRethrow()
		then:
			t2.is(t1)
	}

	def "on perform fail rethrow - on fail"() {
		when:
			Try.perform((DangerousSupplier) { throw cause }).onPerformFailRethrow()
		then:
			Exception e = thrown()
			e.is(cause)
		where:
			cause << CAUSES
	}

	def "assert fail - on success"() {
		given:
			Matcher matcher = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { "qwe" })
			Try t2 = t1.assertFail(matcher, "msg")
		then:
			0 * matcher.matches(_)
		and:
			t2.is(t1)
	}

	def "assert fail - on fail - success"() {
		given:
			Matcher matcher = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { throw cause })
			Try t2 = t1.assertFail(matcher, "msg")
		then:
			1 * matcher.matches(cause) >> true
		and:
			notThrown(AssertError)
		and:
			t2.is(t1)
		where:
			cause << CAUSES
	}

	def "assert fail - on fail - fail"() {
		given:
			Matcher matcher = Mock()
		when:
			Try t1 = Try.perform((DangerousSupplier) { throw cause })
			Try t2 = t1.assertFail(matcher, "msg")
		then:
			1 * matcher.matches(cause) >> false
		and:
			1 * matcher.describeTo(_) >> {it[0].appendText("qwertyqaz")}
		and:
			AssertError e = thrown()
		and:
			e.getResults().size() == 1
			AssertResult res = e.getResults()[0]
		and:
			res.getActual().is(cause)
			res.getExpected() == "qwertyqaz"
			res.getMessage() == "msg"
			res.getCause() == null
		where:
			cause << CAUSES
	}

	def "assert fail not expected - on success"() {
		when:
			Try.perform((DangerousSupplier){ "qwe" }).assertFailNotExpected()
		then:
			notThrown(AssertError)

		when:
			Try.perform((DangerousSupplier){ "qwe" }).assertFailNotExpected("message")
		then:
			notThrown(AssertError)
	}

	def "assert fail not expected - on fail"() {

		given:
			def cause = new RuntimeException("cause")

		when:
			Try.perform((DangerousSupplier){ throw cause }).assertFailNotExpected()
		then:
			thrown(AssertError)

		when:
			Try.perform((DangerousSupplier){ throw cause }).assertFailNotExpected("test-fail-assert")
		then:
			AssertError e = thrown()
			e.getMessage().contains("test-fail-assert")
	}

	def "assert fail is instance of - on success"() {

		when:
			Try.perform((DangerousSupplier){ "qwe" }).assertFailIsInstanceOf(RuntimeException)
		then:
			notThrown(AssertError)

		when:
			Try.perform((DangerousSupplier){ "qwe" }).assertFailIsInstanceOf(IllegalArgumentException, "message")
		then:
			notThrown(AssertError)
	}

	def "assert fail is instance of - on fail"() {

		given:
			def cause = new RuntimeException("cause")

		when:
			Try.perform((DangerousSupplier){ throw cause }).assertFailIsInstanceOf(RuntimeException)
		then:
			notThrown(AssertError)

		when:
			Try.perform((DangerousSupplier){ throw cause }).assertFailIsInstanceOf(RuntimeException, "message")
		then:
			notThrown(AssertError)

		when:
			Try.perform((DangerousSupplier){ throw cause }).assertFailIsInstanceOf(IllegalArgumentException)
		then:
			thrown(AssertError)

		when:
			Try.perform((DangerousSupplier){ throw cause }).assertFailIsInstanceOf(IllegalArgumentException, "test-fail-instance")
		then:
			AssertError e = thrown()
			e.getMessage().contains("test-fail-instance")
	}
}
