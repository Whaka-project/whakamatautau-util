package org.whaka.util

import org.whaka.util.UberCloser.CloseException
import org.whaka.util.function.DangerousConsumer

import spock.lang.Specification

class UberCloserTest extends Specification {

	def "auto closeable is called"() {
		given:
			AutoCloseable target = Mock()

		when: UberCloser.close(target)
		then: 1 * target.close()

		when: UberCloser.close(target, "qwe")
		then: 1 * target.close()

		when: UberCloser.closeQuietly(target)
		then: 1 * target.close()

		when: UberCloser.closeQuietly(target, "rty")
		then: 1 * target.close()
	}

	def "destroyable is called"() {
		given:
			Destroyable target = Mock()

		when: UberCloser.close(target)
		then: 1 * target.destroy()

		when: UberCloser.close(target, "qwe")
		then: 1 * target.destroy()

		when: UberCloser.closeQuietly(target)
		then: 1 * target.destroy()

		when: UberCloser.closeQuietly(target, "qwe")
		then: 1 * target.destroy()
	}

	def "custom target is called"() {
		given:
			DangerousConsumer operation = Mock()

		when: UberCloser.close(42, operation)
		then: 1 * operation.accept(42)

		when: UberCloser.close(false, operation, "qwe")
		then: 1 * operation.accept(false)

		when: UberCloser.closeQuietly("target", operation)
		then: 1 * operation.accept("target")

		when: UberCloser.closeQuietly(1.5, operation, "qwe")
		then: 1 * operation.accept(1.5)
	}

	def "auto closeable rethrown"() {
		given:
			AutoCloseable target = Mock()
			RuntimeException cause = new RuntimeException()

		when: UberCloser.close(target)
		then: 1 * target.close() >> {throw cause}
		and:
			CloseException e1 = thrown()
			e1.getMessage().contains("${target}")
			e1.getCause().is(cause)

		when: UberCloser.close(target, "test message")
		then: 1 * target.close() >> {throw cause}
		and:
			CloseException e2 = thrown()
			e2.getMessage().contains("test message")
			e2.getCause().is(cause)
	}

	def "destroyable rethrown"() {
		given:
			Destroyable target = Mock()
			RuntimeException cause = new RuntimeException()

		when: UberCloser.close(target)
		then: 1 * target.destroy() >> {throw cause}
		and:
			CloseException e1 = thrown()
			e1.getMessage().contains("${target}")
			e1.getCause().is(cause)

		when: UberCloser.close(target, "other test message")
		then: 1 * target.destroy() >> {throw cause}
		and:
			CloseException e2 = thrown()
			e2.getMessage().contains("other test message")
			e2.getCause().is(cause)
	}

	def "custom target rethrown"() {
		given:
			DangerousConsumer operation = Mock()
			RuntimeException cause = new RuntimeException()

		when: UberCloser.close("some target", operation)
		then: 1 * operation.accept("some target") >> {throw cause}
		and:
			CloseException e1 = thrown()
			e1.getMessage().contains("some target")
			e1.getCause().is(cause)

		when: UberCloser.close(42, operation, "another test message")
		then: 1 * operation.accept(42) >> {throw cause}
		and:
			CloseException e2 = thrown()
			e2.getMessage().contains("another test message")
			e2.getCause().is(cause)
	}

	def "auto closeable NOT thrown"() {
		given:
			AutoCloseable target = Mock()
			RuntimeException cause = new RuntimeException()

		when: UberCloser.closeQuietly(target)
		then: 1 * target.close() >> {throw cause}
		and:  notThrown()

		when: UberCloser.closeQuietly(target, "test message")
		then: 1 * target.close() >> {throw cause}
		and:  notThrown()
	}

	def "destroyable NOT thrown"() {
		given:
			Destroyable target = Mock()
			RuntimeException cause = new RuntimeException()

		when: UberCloser.closeQuietly(target)
		then: 1 * target.destroy() >> {throw cause}
		and:  notThrown()

		when: UberCloser.closeQuietly(target, "test message")
		then: 1 * target.destroy() >> {throw cause}
		and:  notThrown()
	}

	def "custom target NOT thrown"() {
		given:
			DangerousConsumer operation = Mock()
			RuntimeException cause = new RuntimeException()

		when: UberCloser.closeQuietly("some target", operation)
		then: 1 * operation.accept("some target") >> {throw cause}
		and:  notThrown()

		when: UberCloser.closeQuietly(42, operation, "another test message")
		then: 1 * operation.accept(42) >> {throw cause}
		and:  notThrown()
	}
}
