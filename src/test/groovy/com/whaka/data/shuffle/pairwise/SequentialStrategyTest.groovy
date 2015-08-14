package com.whaka.data.shuffle.pairwise

import spock.lang.Specification

class SequentialStrategyTest extends Specification {

	def "test expected tables"() {
		expect:
			SequentialStrategy.INSTANCE.apply(input as int[]) == (result as int[][])
		where:
			[input, result] << [
				[
					[],
					[]
				],
				[
					[3],
					[
						[0],
						[1],
						[2],
					]
				],
				[
					[2,3],
					[
						[0,0],
						[0,1],
						[0,2],
						[1,0],
						[1,1],
						[1,2],
					]
				],
				[
					[2,0],
					[
						[0,-1],
						[1,-1],
					]
				],
				[
					[2,2,2],
					[
						[0,0,0],
						[0,1,1],
						[1,0,1],
						[1,1,0],
					]
				],
				[
					[2,3,2],
					[
						[0,0,0],
						[0,1,1],
						[0,2,1],
						[1,0,1],
						[1,1,0],
						[1,2,0],
					]
				],
				[
					[2,0,2],
					[
						[0,-1,0],
						[0,-1,1],
						[1,-1,0],
						[1,-1,1],
					]
				],
				[
					[2,2,2,2],
					[
						[0,0,0,0],
						[0,0,0,1],
						[0,1,1,0],
						[0,1,1,1],
						[1,0,1,1],
						[1,1,0,0],
					]
				],
				[
					[2,2,2,2,2],
					[
						[0,0,0,0,1],
						[0,0,0,1,0],
						[0,1,1,0,1],
						[0,1,1,1,0],
						[1,0,1,1,1],
						[1,1,0,0,0],
					]
				],
			]
	}
}
