package org.whaka.util

import spock.lang.Specification

import org.whaka.TestData
import org.whaka.util.function.DangerousConsumer

class ResourceTest extends Specification {

	def "creation doesn't trigger close"() {
		given:
			DangerousConsumer closeOperation = Mock()
		when:
			Resource resource = Resource.create(value, closeOperation)
		then:
			0 * closeOperation.accept(_)
		and:
			resource.getValue().is(value)
			resource.getClose().is(closeOperation)
		where:
			value << TestData.variousObjects() - null
	}

	def "close delegates to operation"() {
		given:
			DangerousConsumer closeOperation = Mock()
		when:
			Resource.create(value, closeOperation).close()
		then:
			1 * closeOperation.accept(value)
		where:
			value << TestData.variousObjects() - null
	}
}
