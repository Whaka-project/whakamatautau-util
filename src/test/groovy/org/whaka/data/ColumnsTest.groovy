package org.whaka.data

import spock.lang.Shared
import spock.lang.Specification

class ColumnsTest extends Specification {

	@Shared	static final Column c1 = column(String, ["qwe", "rty"])
	@Shared	static final Column c2 = column(Integer, [12, 13, 14])
	@Shared	static final Column c3 = column(Double, [1.0, 2.5, 3.14])
	@Shared	static final Column c4 = column(Boolean, [true, false, null])

	def "construction"() {
		expect:
			new Columns().getColumns().isEmpty()
			new Columns(c2).getColumns() == [c2]
			new Columns(c2, c4, c1).getColumns() == [c2, c4, c1]
			new Columns(c1, c2, c3, c4).getColumns() == [c1, c2, c3, c4]
		and:
			new Columns(null as Collection).getColumns().isEmpty()
			new Columns([]).getColumns().isEmpty()
			new Columns([c2]).getColumns() == [c2]
			new Columns([c2, c4, c1]).getColumns() == [c2, c4, c1]
			new Columns([c1, c2, c3, c4]).getColumns() == [c1, c2, c3, c4]
	}

	def "construction illegal values"() {

		when:
			new Columns([c1, null])
		then:
			thrown(IllegalArgumentException)

		when:
			new Columns([c1, c1])
		then:
			thrown(IllegalArgumentException)

		when:
			new Columns([c1, new Column(c1.getKey(), ["a", "b", "c"])])
		then:
			thrown(IllegalArgumentException)

		when:
			new Columns([c1, new Column(c2.getKey(), [1, 2, 3])])
		then:
			notThrown(IllegalArgumentException)
	}

	def "isPresent/getColumn: empty columns"() {
		given:
			Columns cols = new Columns()
		expect:
			[c1, c2, c3, c4].forEach {
				assert cols.isPresent(it.getKey()) == false
				assert cols.getColumn(it.getKey()) == null
			}
	}

	def "isPresent/getColumn"() {
		given:
			Columns cols = new Columns(addedColumns)
		expect:
			[c1, c2, c3, c4].forEach {
				if (addedColumns.contains(it)) {
					assert cols.isPresent(it.getKey()) == true
					assert cols.getColumn(it.getKey()).is(it)
				}
				else {
					assert cols.isPresent(it.getKey()) == false
					assert cols.getColumn(it.getKey()) == null
				}
			}
		where:
			addedColumns << [
				[c1],
				[c1, c4],
				[c2, c1, c3],
				[c4, c1, c2, c3],
			]
	}

	private static Column column(Class type, List values) {
		return new Column(key(type), values)
	}

	private static ColumnKey key(Class type) {
		return new ColumnKey(type)
	}
}
