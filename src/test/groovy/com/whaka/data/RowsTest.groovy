package com.whaka.data

import spock.lang.Shared
import spock.lang.Specification

class RowsTest extends Specification {

	@Shared	static final Row r1 = row(12, "qwe", false, 15.5)
	@Shared	static final Row r2 = row(null, 'z', 42, 12)
	@Shared	static final Row r3 = row("rty", true, 23.5, 777)

	def "construction"() {
		expect:
			new Rows(rows).getRows() == rows
		where:
			rows << [
				[r1],
				[r1,r3],
				[r1,r2,r3],
				[r3,r1,r2],
			]
	}

	def "iterable"() {
		given:
			def rows = new Rows(data)
		expect:
			rows instanceof Iterable<Row>
			for (row in rows)
				assert data.contains(row)
		where:
			data << [
				[r1],
				[r1,r3],
				[r1,r2,r3],
				[r3,r1,r2],
			]
	}

	def "construction illegal values"() {
		when:
			new Rows([r1, null])
		then:
			thrown(IllegalArgumentException)
	}

	private static Row row(Object ... values) {
		return new Row(values.collect(RowsTest.&entry))
	}

	private static RowEntry entry(Object value) {
		return new RowEntry<>(key(value ? value.getClass() : String), value)
	}

	private static ColumnKey key(Class type) {
		return new ColumnKey(type)
	}
}
