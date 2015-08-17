package org.whaka.data.shuffle.pairwise

import java.util.function.Function

import spock.lang.Specification

import org.whaka.data.Columns
import org.whaka.data.Rows
import org.whaka.data.shuffle.IndexShuffle
import org.whaka.data.shuffle.Shuffle

class PairWiseTest extends Specification {

	def "constructor with delegate"() {
		given:
			Shuffle delegate = Mock()
			Columns cols = new Columns()
			Rows rows = new Rows([])

		when:
			PairWise pw = new PairWise(delegate)
		then:
			pw.delegate.is(delegate)

		when:
			def res = pw.apply(cols)
		then:
			1 * delegate.apply(cols) >> rows
		and:
			res.is(rows)
	}

	def "constructor with strategy"() {
		given:
			Function<int[], int[][]> strategy = Mock()
		when:
			PairWise pw = new PairWise(strategy)
		then:
			pw.delegate instanceof IndexShuffle
			(pw.delegate as IndexShuffle).indexCalculator.is(strategy)
	}

	def "SEQUENTIAL"() {
		expect:
			PairWise.SEQUENTIAL instanceof PairWise
			PairWise.SEQUENTIAL.delegate instanceof IndexShuffle
		and:
			IndexShuffle shuffle = PairWise.SEQUENTIAL.delegate
			shuffle.indexCalculator instanceof SequentialStrategy
	}
}
