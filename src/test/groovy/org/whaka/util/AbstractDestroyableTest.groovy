package org.whaka.util

import spock.lang.Specification

class AbstractDestroyableTest extends Specification {

	def "test"() {
		given:
			AbstractDestroyable init = Spy(AbstractDestroyable)

		expect:
			!init.isDestroyed()

		when: init.assertNotDestroyed()
		then: notThrown(IllegalStateException)

		when: init.destroy()
		then: 1 * init.doDestroy()

		when: init.destroy()
		then: 0 * init.doDestroy()

		expect:
			init.isDestroyed()

		when: init.assertNotDestroyed()
		then: thrown(IllegalStateException)
	}
}
