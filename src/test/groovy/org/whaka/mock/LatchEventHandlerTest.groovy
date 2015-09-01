package org.whaka.mock

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import spock.lang.Specification

class LatchEventHandlerTest extends Specification {

	def "construction"() {
		given:
			CountDownLatch latch = new CountDownLatch(1)
		when:
			def handler = new LatchEventHandler(latch)
		then:
			handler.getLatch().is(latch)
	}

	def "static construction"() {
		when:
			def handler = EventHandlers.latch(12)
		then:
			handler.getLatch().getCount() == 12
	}

	def "is open"() {
		given:
			CountDownLatch latch = Spy(CountDownLatch, constructorArgs: [2])
		and:
			def handler = new LatchEventHandler(latch)

		when:
			def res = handler.isOpen()
		then:
			1 * latch.getCount() >> 2
		and:
			res == true

		when:
			def res2 = handler.isOpen()
		then:
			1 * latch.getCount() >> 0
		and:
			res2 == false
	}

	def "test"() {
		given:
			CountDownLatch latch = Spy(CountDownLatch, constructorArgs: [2])
		and:
			def handler = new LatchEventHandler(latch)

		when:
			def res = handler.test("qwe")
		then:
			1 * latch.getCount() >> 2
		and:
			res == true

		when:
			def res2 = handler.test("qwe")
		then:
			1 * latch.getCount() >> 0
		and:
			res2 == false
	}

	def "event collected"() {
		given:
			CountDownLatch latch = Spy(CountDownLatch, constructorArgs: [2])
		and:
			def handler = new LatchEventHandler(latch)

		when:
			handler.eventCollected("qwe")
		then:
			1 * latch.countDown()

		when:
			handler.eventCollected("qwe")
		then:
			1 * latch.countDown()
	}

	def "close"() {
		given:
			CountDownLatch latch = Spy(CountDownLatch, constructorArgs: [2])
		and:
			def handler = new LatchEventHandler(latch)

		expect:
			handler.isOpen() == true

		when:
			handler.close()
		then:
			handler.isOpen() == false
	}

	def "await"() {
		given:
			CountDownLatch latch = Spy(CountDownLatch, constructorArgs: [2])
		and:
			def handler = new LatchEventHandler(latch)

		expect: "handler is open after creation"
			handler.isOpen() == true

		when: "await is called on open handler"
			def res = handler.await(42, TimeUnit.DAYS)
		then: "underlying latch is called with the same arguments"
			1 * latch.await(42, TimeUnit.DAYS) >> result
		and: "actual result is returned"
			res == result
		and: "handler is still open"
			handler.isOpen() == true

		when: "await is called on closed handler"
			handler.close()
			def res2 = handler.await(42, TimeUnit.DAYS)
		then: "underlying latch IS NOT called"
			0 * latch.await(_, _)
		and: "result is always false"
			res2 == false
		and: "handler is closed"
			handler.isOpen() == false

		where:
			result << [true, false]
	}

	def "await and close"() {
		given:
			CountDownLatch latch = Spy(CountDownLatch, constructorArgs: [2])
		and:
			def handler = new LatchEventHandler(latch)

		expect: "handler is open after creation"
			handler.isOpen() == true

		when: "awaitAndClose is called on open handler"
			def res = handler.awaitAndClose(42, TimeUnit.DAYS)
		then: "underlying latch is called with the same arguments"
			1 * latch.await(42, TimeUnit.DAYS) >> result
		and: "actual result is returned"
			res == result
		and: "handler is closed"
			handler.isOpen() == false

		when: "awaitAndClose is called on closed handler"
			def res2 = handler.awaitAndClose(42, TimeUnit.DAYS)
		then: "underlying latch IS NOT called"
			0 * latch.await(_, _)
		and: "result is always false"
			res2 == false
		and: "handler is still closed"
			handler.isOpen() == false

		where:
			result << [true, false]
	}
}
