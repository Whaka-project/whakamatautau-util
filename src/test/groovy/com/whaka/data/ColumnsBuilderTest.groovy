package com.whaka.data

import spock.lang.Shared
import spock.lang.Specification

class ColumnsBuilderTest extends Specification {

	@Shared	static final Column c1 = column(String, ["qwe", "rty"])
	@Shared	static final Column c2 = column(Integer, [12, 13, 14])
	@Shared	static final Column c3 = column(Double, [1.0, 2.5, 3.14])
	@Shared	static final Column c4 = column(Boolean, [true, false, null])

	def "construction"() {
		when:
			def builder = new ColumnsBuilder()
		then:
			builder.getColumns().isEmpty()
		and:
			builder.build().getColumns().isEmpty()
	}

	def "addColumn with column"() {
		given:
			ColumnsBuilder cols = new ColumnsBuilder()
			def columns = []
		expect:
			[c1, c2, c3, c4].forEach {
				assert cols.addColumn(it).getColumns() == columns << it
			}
		and:
			cols.build().getColumns() == columns
	}

	def "addColumn with column and index"() {
		given:
			ColumnsBuilder cols = new ColumnsBuilder()
		expect:
			cols.addColumn(c1, 0).getColumns() == [c1]
			cols.addColumn(c2, 0).getColumns() == [c2, c1]
			cols.addColumn(c3, 1).getColumns() == [c2, c3, c1]
			cols.addColumn(c4, 3).getColumns() == [c2, c3, c1, c4]
		and:
			cols.build().getColumns() == [c2, c3, c1, c4]
	}

	def "addColumn with key and values"() {
		given:
			ColumnsBuilder cols = new ColumnsBuilder()
			def columns = []
		expect:
			[c1, c2, c3, c4].forEach {
				assert cols.addColumn(it.getKey(), it.getData()).getColumns().size() == (columns << it).size()
			}
		and:
			def columnCollections = [cols.getColumns(), cols.build().getColumns()]
			columns.forEach {
				def col = it
				columnCollections.forEach {
					assert it[columns.indexOf(col)].getKey().is(col.getKey())
					assert it[columns.indexOf(col)].getData() == col.getData()
				}
			}
	}

	def "isPresent/getColumn"() {
		given:
			ColumnsBuilder cols = new ColumnsBuilder()
			def added = [c1, c4]

		expect:
			[c1, c2, c3, c4].forEach {
				assert cols.isPresent(it.getKey()) == false
				assert cols.getColumn(it.getKey()) == null
			}

		when:
			added.forEach {cols.addColumn(it)}
		then:
			[c1, c2, c3, c4].forEach {
				if (added.contains(it)) {
					assert cols.isPresent(it.getKey()) == true
					assert cols.getColumn(it.getKey()).is(it)
				}
				else {
					assert cols.isPresent(it.getKey()) == false
					assert cols.getColumn(it.getKey()) == null
				}
			}
	}

	def "removeColumn"() {
		given:
			ColumnsBuilder cols = new ColumnsBuilder()
			def columns = [c1, c2, c3, c4]
			def removed = []
			columns.forEach {cols.addColumn(it)}
		expect:
			columns.forEach {
				assert cols.removeColumn(it.getKey()).is(it)
				assert cols.getColumns() == columns - (removed << it)
			}

		when:
			columns.forEach {cols.addColumn(it)}
			cols.removeColumn(c2.getKey())
			cols.removeColumn(c3.getKey())
		then:
			cols.getColumns() == [c1, c4]

		when:
			cols.addColumn(c3)
			cols.addColumn(c2)
		then:
			cols.getColumns() == [c1, c4, c3, c2]
	}

	def "add duplicated key"() {
		given:
			ColumnsBuilder cols = new ColumnsBuilder()
			cols.addColumn(c1)

		when:
			cols.addColumn(c1)
		then:
			thrown(IllegalArgumentException)

		when:
			cols.addColumn(c1, 0)
		then:
			thrown(IllegalArgumentException)

		when:
			cols.addColumn(c1.getKey(), [""])
		then:
			thrown(IllegalArgumentException)

		when:
			cols.addColumn(c2.getKey(), [42])
		then:
			notThrown(IllegalArgumentException)
	}

	def "builder pattern"() {
		given:
			ColumnsBuilder cols = new ColumnsBuilder()
		expect:
			cols.addColumn(c1).is(cols)
	}

	private static Column column(Class type, List values) {
		return new Column(key(type), values)
	}

	private static ColumnKey key(Class type) {
		return new ColumnKey(type)
	}
}
