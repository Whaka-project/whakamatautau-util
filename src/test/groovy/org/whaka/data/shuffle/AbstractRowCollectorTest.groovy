package org.whaka.data.shuffle

import spock.lang.Specification

class AbstractRowCollectorTest extends Specification {

	def "add row with different validity"() {
		given:
			AbstractRowCollector col = Spy()
			int[] row = [1,2,3]

		when:
			col.addRowIfValid(row)
		then:
			1 * col.isValidRow(row) >> false
		and:
			col.getRows().isEmpty()

		when:
			col.addRowIfValid(row)
		then:
			1 * col.isValidRow(row) >> true
		and:
			col.getRows() == [[1,2,3] as int[]]
	}

	def "add multiple rows with different validity"() {
		given:
			Map<int[], Boolean> rows = expectedRows
		and:
			AbstractRowCollector col = Spy()
			col.isValidRow(_) >> {rows[it[0]]}
		when:
			rows.forEach {k,v ->
				assert col.addRowIfValid(k) == v
			}
		then:
			col.getRows() == rows.findAll({k,v->v}).collect{k,v->k}
		where:
			expectedRows = [
				([0,0,0] as int[]): true,
				([1,0,1] as int[]): true,
				([0,1,1] as int[]): true,
				([1,1,1] as int[]): true,
				([0,0,1] as int[]): false,
				([0,1,0] as int[]): true,
				([1,0,0] as int[]): true,
				([1,1,0] as int[]): false,
			]
	}
}
