package org.whaka.data

import spock.lang.Specification

class RowTest extends Specification {

	def "construction"() {
		given:
			Class[] types = rowTypes
			Object[] values = rowValues
			assert types.size() == values.size() : "The same number of types and values required for the test!"
		and:
			List<RowEntry> data = []
			for (int i = 0; i < types.size(); i++)
				data << new RowEntry(new ColumnKey(types[i]), values[i])
		when:
			def row = new Row(data)
		then:
			row.getEntries() == data
		and:
			data.forEach {
				assert row.isPresent(it.getKey())
				assert row.getValue(it.getKey()).is(it.getValue())
			}
		where:
			rowTypes							|	rowValues
			[String]							|	["qwe"]
			[String, Integer]					|	["qwe", 12]
			[String, Integer, Double]			|	["qwe", 12, 1.5]
			[String, Integer, Double, Boolean]	|	["qwe", 12, 1.5, false]
			[String, Integer, Double, Boolean]	|	["qwe", 12, 1.5, null]
	}

	def "illegal data"() {
		given:
			def key = new ColumnKey(String)
			def entry1 = new RowEntry(key, "qwe")
			def entry2 = new RowEntry(key, "rty")

		when:
			new Row(null)
		then:
			thrown(IllegalArgumentException)

		when:
			new Row([])
		then:
			thrown(IllegalArgumentException)

		when:
			new Row([entry1, null])
		then:
			thrown(IllegalArgumentException)

		when: "specified collection contains values with the same key"
			new Row([entry1, entry2])
		then:
			thrown(IllegalArgumentException)
	}

	def "illegal keys are not found"() {
		given:
			def key1 = new ColumnKey(String)
			def key2 = new ColumnKey(String)
			def entry = new RowEntry(key1, "qwe")
		and:
			def row = new Row([entry])
		expect:
			row.isPresent(key1) == true
			row.isPresent(key2) == false
			row.getValue(key1) == "qwe"
		when:
			row.getValue(key2)
		then:
			thrown(NoSuchElementException)
	}
}
