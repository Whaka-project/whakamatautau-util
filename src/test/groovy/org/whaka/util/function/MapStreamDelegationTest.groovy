package org.whaka.util.function

import java.util.function.*
import java.util.stream.Collector
import java.util.stream.DoubleStream
import java.util.stream.IntStream
import java.util.stream.LongStream
import java.util.stream.Stream

import org.whaka.util.UberMaps

import spock.lang.Specification
import spock.lang.Unroll

class MapStreamDelegationTest extends Specification {

	@Unroll
	def "no parameters delegation"() {
		given:
			Stream delegate = Mock()
			delegate.map(_) >> delegate
		and:
			MapStream mapstr = new MapStream(delegate)

		when: mapstr."$call"()
		then: 1 * delegate."$call"() >> result

		where:
			call			|	result
			'iterator'		|	Mock(Iterator)
			'spliterator'	|	Mock(Spliterator)
			'isParallel'	|	false
			'sequential'	|	Mock(Stream)
			'parallel'		|	Mock(Stream)
			'unordered'		|	Mock(Stream)
			'close'			|	null
			'distinct'		|	Mock(Stream)
			'sorted'		|	Mock(Stream)
			'toArray'		|	[]
			'count'			|	100
			'findFirst'		|	Optional.empty()
			'findAny'		|	Optional.empty()
	}

	@Unroll
	def "1 parameters delegation"() {
		given:
			Stream delegate = Mock()
			delegate.map(_) >> delegate
		and:
			MapStream mapstr = new MapStream(delegate)

		when: mapstr."$call"(param)
		then: 1 * delegate."$call"(param) >> result

		where:
			call				|	param					|	result
			'onClose'			|	Mock(Runnable)			|	Mock(Stream)
			'filter'			|	Mock(Predicate)			|	Mock(Stream)
			'mapToInt'			|	Mock(ToIntFunction)		|	Mock(IntStream)
			'mapToLong'			|	Mock(ToLongFunction)	|	Mock(LongStream)
			'mapToDouble'		|	Mock(ToDoubleFunction)	|	Mock(DoubleStream)
			'flatMap'			|	Mock(Function)			|	Mock(Stream)
			'flatMapToInt'		|	Mock(Function)			|	Mock(IntStream)
			'flatMapToLong'		|	Mock(Function)			|	Mock(LongStream)
			'flatMapToDouble'	|	Mock(Function)			|	Mock(DoubleStream)
			'sorted'			|	Mock(Comparator)		|	Mock(Stream)
			'peek'				|	Mock(Consumer)			|	Mock(Stream)
			'limit'				|	100						|	Mock(Stream)
			'skip'				|	100						|	Mock(Stream)
			'forEach'			|	Mock(Consumer)			|	null
			'forEachOrdered'	|	Mock(Consumer)			|	null
			'toArray'			|	Mock(IntFunction)		|	[]
			'reduce'			|	Mock(BinaryOperator)	|	Optional.empty()
			'collect'			|	Mock(Collector)			|	null
			'min'				|	Mock(Comparator)		|	Optional.empty()
			'max'				|	Mock(Comparator)		|	Optional.empty()
			'anyMatch'			|	Mock(Predicate)			|	false
			'allMatch'			|	Mock(Predicate)			|	false
			'noneMatch'			|	Mock(Predicate)			|	false
	}

	def "reduce 2"() {
		given:
			Stream delegate = Mock()
			delegate.map(_) >> delegate
		and:
			MapStream mapstr = new MapStream(delegate)
		and:
			UberMaps.Entry entry = UberMaps.entry(1,2)
			BinaryOperator accumulator = Mock()

		when: mapstr.reduce(entry, accumulator)
		then: 1 * delegate.reduce(entry, accumulator)
	}

	def "reduce 3"() {
		given:
			Stream delegate = Mock()
			delegate.map(_) >> delegate
		and:
			MapStream mapstr = new MapStream(delegate)
		and:
			UberMaps.Entry entry = UberMaps.entry(1,2)
			BiFunction accumulator = Mock()
			BinaryOperator combiner = Mock()

		when: mapstr.reduce(entry, accumulator, combiner)
		then: 1 * delegate.reduce(entry, accumulator, combiner)
	}

	def "collect 3"() {
		given:
			Stream delegate = Mock()
			delegate.map(_) >> delegate
		and:
			MapStream mapstr = new MapStream(delegate)
		and:
			Supplier supplier = Mock()
			BiConsumer accumulator = Mock()
			BiConsumer combiner = Mock()

		when: mapstr.collect(supplier, accumulator, combiner)
		then: 1 * delegate.collect(supplier, accumulator, combiner)
	}
}
