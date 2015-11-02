package org.whaka.util

import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.LongConsumer
import java.util.function.LongFunction

import org.whaka.util.function.DangerousBiConsumer
import org.whaka.util.function.DangerousBiFunction
import org.whaka.util.function.DangerousConsumer
import org.whaka.util.function.DangerousFunction

import spock.lang.Specification

class TimeoutTest extends Specification {

	def "construction"() {
		when:
			def to = Timeout.create(millis)
		then:
			to.getMillis() == millis
		where:
			millis << [0, 100, 1000, 99999, 9123456789L]
	}

	def "negative millis exception"() {
		when:
			def to = Timeout.create(millis)
		then:
			thrown(IllegalArgumentException)
		where:
			millis << [-1, -100, -5555, -987654321L]
	}

	def "sleep"() {
		given:
			long before = System.currentTimeMillis()
		when:
			Timeout.create(millis).sleep()
		then:
			(System.currentTimeMillis() - before) >= millis
		where:
			millis << [50, 100, 500]
	}

	def "await"() {
		given:
			def to = Timeout.create(millis)
		and:
			LongConsumer longConsumer = Mock()
			BiConsumer biConsumer = Mock()

		when:
			to.await(longConsumer)
		then:
			1 * longConsumer.accept(millis)

		when:
			to.await(biConsumer)
		then:
			biConsumer.accept(millis, TimeUnit.MILLISECONDS)

		where:
			millis << [0, 10, 100, 1000, 55555]
	}

	def "await and get"() {
		given:
			def to = Timeout.create(millis)
		and:
			LongFunction longFunction = Mock()
			BiFunction biFunction = Mock()

		when:
			def res1 = to.awaitAndGet(longFunction)
		then:
			1 * longFunction.apply(millis) >> "qwe"
		and:
			res1 == "qwe"

		when:
			def res2 = to.awaitAndGet(biFunction)
		then:
			biFunction.apply(millis, TimeUnit.MILLISECONDS) >> 42
		and:
			res2 == 42

		where:
			millis << [0, 10, 100, 1000, 55555]
	}

	def "await dangerous"() {
		given:
			DangerousConsumer operation = Mock()
			DangerousBiConsumer bioperation = Mock()
			RuntimeException cause = new RuntimeException()

		when:
			Timeout.create(1000).awaitDangerous(operation, "some test name")
		then:
			1 * operation.accept(1000) >> {throw cause}
		and:
			IllegalStateException caught = thrown()
		and:
			caught.getMessage().contains("some test name")
			caught.getCause().is(cause)

		when:
			Timeout.create(2000).awaitDangerous(bioperation, "some other test name")
		then:
			1 * bioperation.accept(2000, TimeUnit.MILLISECONDS) >> {throw cause}
		and:
			IllegalStateException caught2 = thrown()
		and:
			caught2.getMessage().contains("some other test name")
			caught2.getCause().is(cause)
	}

	def "await and get dangerous"() {
		given:
			DangerousFunction operation = Mock()
			DangerousBiFunction bioperation = Mock()
			RuntimeException cause = new RuntimeException("sup")

		when:
			Timeout.create(3000).awaitAndGetDangerous(operation, "some third test name")
		then:
			1 * operation.apply(3000) >> {throw cause}
		and:
			IllegalStateException caught = thrown()
		and:
			caught.getMessage().contains("some third test name")
			caught.getCause().is(cause)

		when:
			Timeout.create(4000).awaitAndGetDangerous(bioperation, "some fourth test name")
		then:
			1 * bioperation.apply(4000, TimeUnit.MILLISECONDS) >> {throw cause}
		and:
			IllegalStateException caught2 = thrown()
		and:
			caught2.getMessage().contains("some fourth test name")
			caught2.getCause().is(cause)
	}
}
