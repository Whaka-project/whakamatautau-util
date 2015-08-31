package org.whaka.mock

import java.util.concurrent.CountDownLatch

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

		when:
			handler.close()
		then:
			1 * latch.getCount() >> 10
		and:
			1 * latch.countDown()
		and:
			1 * latch.getCount() >> 5
		and:
			1 * latch.countDown()
		and:
			1 * latch.getCount() >> 0
		and:
			0 * latch._
	}
}
