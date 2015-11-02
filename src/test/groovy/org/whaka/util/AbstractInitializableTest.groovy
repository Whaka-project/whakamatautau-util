package org.whaka.util

import spock.lang.Specification

class AbstractInitializableTest extends Specification {

	def "test"() {
		given:
			AbstractInitializable init = Spy(AbstractInitializable)

		expect:
			!init.isInitialized()

		when: init.assertInitialized()
		then: thrown(IllegalStateException)

		when: init.initialize()
		then: 1 * init.doInitialize()

		when: init.initialize()
		then: 0 * init.doInitialize()

		expect:
			init.isInitialized()

		when: init.assertInitialized()
		then: notThrown(IllegalStateException)
	}
}
