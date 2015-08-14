package com.whaka.data.shuffle.pairwise

import static com.whaka.data.shuffle.pairwise.HashRowCollector.*
import spock.lang.Specification

class HashRowCollectorTest extends Specification {

	def "combine"() {
		given:
			int aInt = Integer.parseUnsignedInt(a, 2)
			int bInt = Integer.parseUnsignedInt(b, 2)
			long combineLong = Long.parseUnsignedLong(combine, 2)
		expect: "two ints are combined into a single long"
			HashRowCollector.combine(aInt, bInt) == combineLong
		where:
			a									|	b									|	combine
			"01111111111111111111111111111111"	|	"01111111111111111111111111111111"	|	"0111111111111111111111111111111101111111111111111111111111111111"
			"10000000000000000000000000000000"	|	"01111111111111111111111111111111"	|	"1000000000000000000000000000000001111111111111111111111111111111"
			"01111111111111111111111111111111"	|	"10000000000000000000000000000000"	|	"0111111111111111111111111111111110000000000000000000000000000000"
			"10000000000000000000000000000000"	|	"10000000000000000000000000000000"	|	"1000000000000000000000000000000010000000000000000000000000000000"
			"00000000000000000000000000101010"	|	"00000000000000000000000001010101"	|	"0000000000000000000000000010101000000000000000000000000001010101"
	}

	def "create hash"() {
		expect:
			HashRowCollector.createHash(row as int[]) == hash
		where:
			row				|	hash
			[1,2,3]			|	[combine(1, 2), combine(1, 3), combine(2, 3)]
			[1,2,3,4]		|	[combine(1, 2), combine(1, 3), combine(1, 4), combine(2, 3), combine(2, 4), combine(3, 4)]
	}

	def "isUniqueHashPresent"() {
		given:
			List<long[]> hashes = [
					[1, 2, 3] as long[],
					[4, 5, 6] as long[],
					[7, 8, 9] as long[],
				]
		expect:
			HashRowCollector.isUniqueHashPresent(hash as long[], hashes) == expected
		where:
			hash		|	expected
			[1, 2, 3]	|	false
			[4, 5, 6]	|	false
			[7, 8, 9]	|	false
			[1, 5, 9]	|	false
			[7, 5, 3]	|	false
			[10, 2, 3]	|	true
			[1, 10, 3]	|	true
			[1, 3, 10]	|	true
			[3, 2, 3]	|	true	// << '3' wasn't at this index before
			[1, 1, 3]	|	true	// << '1' wasn't at this index before
			[1, 2, 2]	|	true	// << '2' wasn't at this index before
	}

	def "isValidRow: empty collector"() {
		given:
			def collector = new HashRowCollector()
		expect:
			collector.isValidRow(row as int[]) == true
		where:
			row		|	_
			[0,0,0]	|	_
			[1,0,1]	|	_
			[0,1,1]	|	_
			[1,1,1]	|	_
			[0,0,1]	|	_
			[0,1,0]	|	_
			[1,0,0]	|	_
			[1,1,0]	|	_
	}

	def "isValidRow: some rows added"() {
		given:
			def collector = new HashRowCollector()
		and:
			collector.addRowIfValid([0,0,0] as int[])
			collector.addRowIfValid([1,0,1] as int[])
			collector.addRowIfValid([0,1,1] as int[])
		expect:
			collector.isValidRow(row as int[]) == expected
		where:
			row		|	expected
			[0,0,0]	|	false
			[1,0,1]	|	false
			[0,1,1]	|	false
			[1,1,1]	|	true
			[0,0,1]	|	false
			[0,1,0]	|	true
			[1,0,0]	|	true
			[1,1,0]	|	true
	}

	def "addRowIfValid: some rows added"() {
		given:
			def collector = new HashRowCollector()
		and:
			collector.addRowIfValid([0,0,0] as int[])
			collector.addRowIfValid([1,0,1] as int[])
			collector.addRowIfValid([0,1,1] as int[])
		expect:
			collector.addRowIfValid(row as int[]) == expected
		where:
			row		|	expected
			[0,0,0]	|	false
			[1,0,1]	|	false
			[0,1,1]	|	false
			[1,1,1]	|	true
			[0,0,1]	|	false
			[0,1,0]	|	true
			[1,0,0]	|	true
			[1,1,0]	|	true
	}

	def "clean-up"() {
		given:
			def collector = new HashRowCollector()
		and:
			initialRows.forEach {collector.addRowIfValid(it as int[])}
		expect:
			collector.rows == expectedRows
		where:
			initialRows = [
				[0,0,0],
				[0,0,1],
				[0,1,0],
				[0,1,1],
				[1,0,0],
				[1,0,1],
				[1,1,0],
			]
			expectedRows = [
				[0,0,0],
				[0,1,1],
				[1,0,1],
				[1,1,0],
			]
	}
}
