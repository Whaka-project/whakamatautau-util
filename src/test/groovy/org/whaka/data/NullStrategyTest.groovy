package org.whaka.data

import static org.whaka.data.NullStrategy.*
import spock.lang.Specification

class NullStrategyTest extends Specification {

	def "null strategy constants"() {
		given:
			NullStrategy strategy = nullStrategy
		expect:
			strategy.apply(list as List) == result
		where:
			nullStrategy		|	list			|	result
			NO_STRATEGY			|	[12]			|	[12]
			NO_STRATEGY			|	[12,22]			|	[12,22]
			NO_STRATEGY			|	[null]			|	[null]
			NULLABLE_START		|	[12]			|	[null,12]
			NULLABLE_START		|	[12,22]			|	[null,12,22]
			NULLABLE_START		|	[12,22,null]	|	[null,12,22,null]
			NULLABLE_START		|	[null,12,22]	|	[null,12,22]
			NULLABLE_START		|	[null]			|	[null]
			NULLABLE_END		|	[12]			|	[12,null]
			NULLABLE_END		|	[12,22]			|	[12,22,null]
			NULLABLE_END		|	[null,12,22]	|	[null,12,22,null]
			NULLABLE_END		|	[12,22,null]	|	[12,22,null]
			NULLABLE_END		|	[null]			|	[null]
	}

	def "static methods"() {
		expect:
			NO_STRATEGY.apply(list) == list
			NULLABLE_START.apply(list) == nullableStart(list)
			NULLABLE_END.apply(list) == nullableEnd(list)
		where:
			list << [
				[12],
				[12,22],
				[null],
				[12,22,null],
				[null,12,22],
			]
	}
}
