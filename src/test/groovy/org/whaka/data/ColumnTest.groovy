package org.whaka.data

import spock.lang.Specification

class ColumnTest extends Specification {

	def "construction"() {
		given:
			def key = new ColumnKey(type)
		when:
			def column = new Column(key, data)
		then:
			column.getKey().is(key)
			column.getType().is(key.type)
			column.getType().is(type)
			column.getData() == data

		where:
			[type, data] << typesAndDatas()
	}

	def "static create"() {
		when:
			def column = Column.create(type, data)
		then:
			column.getType().is(type)
			column.getData() == data

		where:
			[type, data] << typesAndDatas()
	}

	private def typesAndDatas() {
		return [
			[Integer, [1,2,3,4]],
			[Integer, [1,2,3,4]],
			[Integer, [1,2,3,4]],
			[String, ["qwe", "rty"]],
			[String, ["qwe", "rty"]],
			[Double, [null, 1.0]],
			[Double, [null, 1.0]],
			[Double, [null, 1.0]],
			[Long, [1L, null]],
			[Long, [1L, null]],
			[Long, [1L, null]],
			[Boolean, [null, false, null]],
			[Boolean, [null, false, null]],
			[Boolean, [null, false, null]],
		]
	}

	def "varargs construction"() {
		given:
			def key = new ColumnKey(Integer)
		when:
			def column = new Column(key, 1, 2, 3, 4)
		then:
			column.getKey().is(key)
			column.getType().is(key.type)
			column.getType().is(Integer)
			column.getData() == [1, 2, 3, 4]
	}

	def "varargs static create"() {
		when:
			def column = Column.create(Integer, 1, 2, 3, 4)
		then:
			column.getType().is(Integer)
			column.getData() == [1, 2, 3, 4]
	}
}
