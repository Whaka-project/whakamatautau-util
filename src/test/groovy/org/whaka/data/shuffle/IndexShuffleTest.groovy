package org.whaka.data.shuffle

import java.lang.invoke.MethodHandleImpl.BindCaller.T
import java.util.function.Function

import spock.lang.Specification

import org.whaka.data.Column
import org.whaka.data.ColumnKey
import org.whaka.data.Columns
import org.whaka.data.Row
import org.whaka.data.Rows

class IndexShuffleTest extends Specification {

	def "construction"() {
		given:
			Function<int[], int[][]> indexCalculator = Mock()
		when:
			def shuffle = new IndexShuffle(indexCalculator)
		then:
			0 * indexCalculator.apply(_)
		and:
			shuffle.getIndexCalculator().is(indexCalculator)
	}

	def "apply"() {

		given: "abstract function that calculates indexes"
			Function<int[], int[][]> indexCalculator = Mock()
		and: "index shuffle instance built upon it"
			def shuffle = new IndexShuffle(indexCalculator)
		and: "some data columns"
			def cols = new Columns(columns.collect {k,v -> new Column(k, v)})

		when: "shuffle is applied to columns"
			Rows rows = shuffle.apply(cols)
		then: "index calculator is called with an array of integers representing sizes of the column data lists"
			1 * indexCalculator.apply(columns.collect {k,v->v.size()}) >>
				(int[][]) resultIndexes.collect {k,v->k}
		and: "resulting rows are of the same number as an 'index map' returned by the calculator"
			List<Row> list = rows.getRows()
			list.size() == resultIndexes.size()
		and: "rows contains data, represented by the indexes in the 'index map' returned by the calculator"
			def expectedDataRows = resultIndexes.collect {k,v->v}
			def columnKeys = columns.collect {k,v->k}
			for (int i = 0; i < list.size(); i++) {
				// there's exactly one entry in each row for each one column key in the columns
				assert list.get(i).getEntries().collect {it.getKey()} == columnKeys
				// each row contains data from the columns dictionaries, according to index map
				assert list.get(i).getEntries().collect {it.getValue()} == expectedDataRows.getAt(i)
			}

		where:
			// columns represent 'initial' data, as it represented by the columns object
			columns = [
					(key(String)):	["qwe",		"rty",	"qaz"],
					(key(Integer)):	[100,		200,	300],
					(key(Double)):	[15.0,		30.0,	45.0],
					(key(Boolean)):	[true,		false],
				]
			// result indexes represent corresponding elements from the columns' dictionaries
			resultIndexes = [
					[0,0,0,0]: ["qwe", 100, 15.0, true],
					[1,1,1,1]: ["rty", 200, 30.0, false],
					[2,2,2,1]: ["qaz", 300, 45.0, false],
					[0,1,2,0]: ["qwe", 200, 45.0, true],
				]
	}

	private static <T> ColumnKey<T> key(Class<T> type) {
		return new ColumnKey<T>(type)
	}
}
