package com.whaka.util.function

import static com.whaka.util.UberMaps.*

import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate

import spock.lang.Specification

import com.whaka.util.UberCollections
import com.whaka.util.UberMaps

class MapStreamTest extends Specification {
	def "toMap"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.toMap() == map
		where:
			map << maps()
	}

	def "toLinkedMap"() {
		given:
			def mapstr = new MapStream(map)
		when:
			def res = mapstr.toLinkedMap()
		then:
			res instanceof LinkedHashMap
			res.equals(map)
		where:
			map << maps()
	}

	def "to"() {
		given:
			def mapstr = new MapStream(map)
		when:
			def res = mapstr.to({new LinkedHashMap<>()})
		then:
			res instanceof LinkedHashMap
			res.equals(map)
		where:
			map << maps()
	}

	def "toKeys"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.toKeys().toSet() == map.keySet()
		where:
			map << maps()
	}

	def "toValues"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.toValues().toList() == map.values() as List
		where:
			map << maps()
	}

	def "peekKeys"() {
		given:
			def mapstr = new MapStream(map)
			def set = new HashSet<>()
		when:
			mapstr.peekKeys({set.add(it)}).forEach({} as Consumer)
		then:
			set.equals(map.keySet())
		where:
			map << maps()
	}

	def "forEachKey"() {
		given:
			def mapstr = new MapStream(map)
			def set = new HashSet<>()
		when:
			mapstr.forEachKey({set.add(it)})
		then:
			set.equals(map.keySet())
		where:
			map << maps()
	}

	def "peekValues"() {
		given:
			def mapstr = new MapStream(map)
			def list = new ArrayList<>()
		when:
			mapstr.peekValues({list.add(it)}).forEach({} as Consumer)
		then:
			UberCollections.containsEqualElements(list, map.values())
		where:
			map << maps()
	}

	def "forEachValue"() {
		given:
			def mapstr = new MapStream(map)
			def list = new ArrayList<>()
		when:
			mapstr.forEachValue({list.add(it)})
		then:
			UberCollections.containsEqualElements(list, map.values())
		where:
			map << maps()
	}

	def maps() {
		return [
			[:],
			[1:10,2:20,3:30],
			["qwe":"rty"],
			[null:"rty"],
			["qwe":null],
			[null:null],
			[1:null,2:null,3:null]
		]
	}

	def "filter"() {
		given:
			def mapstr = new MapStream(map)
			Predicate<UberMaps.Entry> entryPredicate = predicate
		expect:
			mapstr.filter(entryPredicate).toMap() == result
		where:
			map					|	predicate			|	result
			[1:10,2:20,3:30]	|	{it.key > 1}		|	[2:20,3:30]
			[1:10,2:20,3:30]	|	{it.key() > 1}		|	[2:20,3:30]
			[1:10,2:20,3:30]	|	{it.val > 30}		|	[:]
			[1:10,2:20,3:30]	|	{it.val() < 30}		|	[1:10,2:20]
	}

	def "filter with bi-predicate"() {
		given:
			def mapstr = new MapStream(map)
			BiPredicate entryPredicate = predicate
		expect:
			mapstr.filter(entryPredicate).toMap() == result
		where:
			map					|	predicate			|	result
			[1:10,2:20,3:30]	|	{k,v->k > 1}		|	[2:20,3:30]
			[1:10,2:20,3:30]	|	{k,v->k > 1}		|	[2:20,3:30]
			[1:10,2:20,3:30]	|	{k,v->v > 30}		|	[:]
			[1:10,2:20,3:30]	|	{k,v->v < 30}		|	[1:10,2:20]
	}

	def "filter key"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.filterKey(keyPredicate).toMap() == result
		where:
			map					|	keyPredicate	|	result
			[1:10,2:20,3:30]	|	{it < 3}		|	[1:10,2:20]
			[1:10,2:20,3:30]	|	{it > 1}		|	[2:20,3:30]
			[1:10,2:20,3:30]	|	{it > 2}		|	[3:30]
			[1:10,2:20,3:30]	|	{it > 3}		|	[:]
			[1:10,2:20,3:30]	|	{true}			|	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{false}			|	[:]
	}

	def "filter value"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.filterValue(valPredicate).toMap() == result
		where:
			map					|	valPredicate	|	result
			[1:10,2:20,3:30]	|	{it < 30}		|	[1:10,2:20]
			[1:10,2:20,3:30]	|	{it > 10}		|	[2:20,3:30]
			[1:10,2:20,3:30]	|	{it > 20}		|	[3:30]
			[1:10,2:20,3:30]	|	{it > 30}		|	[:]
			[1:10,2:20,3:30]	|	{true}			|	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{false}			|	[:]
	}

	def "drop"() {
		given:
			def mapstr = new MapStream(map)
			Predicate<UberMaps.Entry> entryPredicate = predicate
		expect:
			mapstr.drop(entryPredicate).toMap() == result
		where:
			map					|	predicate			|	result
			[1:10,2:20,3:30]	|	{it.key > 1}		|	[1:10]
			[1:10,2:20,3:30]	|	{it.key() > 1}		|	[1:10]
			[1:10,2:20,3:30]	|	{it.val > 30}		|	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{it.val() < 30}		|	[3:30]
			[1:10,2:20,3:30]	|	{false}				|	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{true}				|	[:]
	}

	def "drop with bi-predicate"() {
		given:
			def mapstr = new MapStream(map)
			BiPredicate entryPredicate = predicate
		expect:
			mapstr.drop(entryPredicate).toMap() == result
		where:
			map					|	predicate			|	result
			[1:10,2:20,3:30]	|	{k,v->k > 1}		|	[1:10]
			[1:10,2:20,3:30]	|	{k,v->k > 1}		|	[1:10]
			[1:10,2:20,3:30]	|	{k,v->v > 30}		|	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{k,v->v < 30}		|	[3:30]
			[1:10,2:20,3:30]	|	{k,v->false}		|	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{k,v->true}			|	[:]
	}

	def "dop key"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.dropKey(keyPredicate).toMap() == result
		where:
			map					|	keyPredicate	|	result
			[1:10,2:20,3:30]	|	{it < 3}		|	[3:30]
			[1:10,2:20,3:30]	|	{it > 1}		|	[1:10]
			[1:10,2:20,3:30]	|	{it > 2}		|	[1:10,2:20]
			[1:10,2:20,3:30]	|	{it > 3}		|	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{true}			|	[:]
			[1:10,2:20,3:30]	|	{false}			|	[1:10,2:20,3:30]
	}

	def "drop value"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.dropValue(valPredicate).toMap() == result
		where:
			map					|	valPredicate	|	result
			[1:10,2:20,3:30]	|	{it < 30}		|	[3:30]
			[1:10,2:20,3:30]	|	{it > 10}		|	[1:10]
			[1:10,2:20,3:30]	|	{it > 20}		|	[1:10,2:20]
			[1:10,2:20,3:30]	|	{it > 30}		|	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{true}			|	[:]
			[1:10,2:20,3:30]	|	{false}			|	[1:10,2:20,3:30]
	}

	def "map entry"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.mapEntry((Function) mapper).toMap() == result
		where:
			map					|	mapper						||	result
			[1:10,2:20,3:30]	|	{it}						||	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{entry(1, 2)}				||	[1:2,1:2,1:2]
			[1:10,2:20,3:30]	|	{entry(it.key*2, it.val)}	||	[2:10,4:20,6:30]
			[1:10,2:20,3:30]	|	{entry(it.key, it.val*2)}	||	[1:20,2:40,3:60]
			[1:10,2:20,3:30]	|	{entry(it.val, it.key)}		||	[10:1,20:2,30:3]
			[1:10,2:20,3:30]	|	{entry(it.key, null)}		||	[1:null,2:null,3:null]
			[1:10,2:20,3:30]	|	{entry(null, it.val)}		||	[(null):30]
	}

	def "map entry with bifunction"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.mapEntry((BiFunction) mapper).toMap() == result
		where:
			map					|	mapper					||	result
			[1:10,2:20,3:30]	|	{k,v->entry(k,v)}		||	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{k,v->entry(1, 2)}		||	[1:2,1:2,1:2]
			[1:10,2:20,3:30]	|	{k,v->entry(k*2, v)}	||	[2:10,4:20,6:30]
			[1:10,2:20,3:30]	|	{k,v->entry(k, v*2)}	||	[1:20,2:40,3:60]
			[1:10,2:20,3:30]	|	{k,v->entry(v, k)}		||	[10:1,20:2,30:3]
			[1:10,2:20,3:30]	|	{k,v->entry(k, null)}	||	[1:null,2:null,3:null]
			[1:10,2:20,3:30]	|	{k,v->entry(null, v)}	||	[(null):30]
	}

	def "map with two functions"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.mapEntry(keyMapper, valMapper).toMap() == result
		where:
			map					|	keyMapper		|	valMapper			||	result
			[1:10,2:20,3:30]	|	{it}			|	{it}				||	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{it*2}			|	{it}				||	[2:10,4:20,6:30]
			[1:10,2:20,3:30]	|	{it}			|	{it*2}				||	[1:20,2:40,3:60]
			[1:10,2:20,3:30]	|	{it}			|	{null}				||	[1:null,2:null,3:null]
			[1:10,2:20,3:30]	|	{null}			|	{it}				||	[(null):30]
	}

	def "map key"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.mapKey(keyMapper).toMap() == result
		where:
			map					|	keyMapper					||	result
			[1:10,2:20,3:30]	|	{it}						||	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{it*2}						||	[2:10,4:20,6:30]
			[1:10,2:20,3:30]	|	{42}						||	[42:30]
			[1:10,2:20,3:30]	|	{null}						||	[(null):30]
	}

	def "map value"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.mapValue(valMapper).toMap() == result
		where:
			map					|	valMapper					||	result
			[1:10,2:20,3:30]	|	{it}						||	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{it*2}						||	[1:20,2:40,3:60]
			[1:10,2:20,3:30]	|	{42}						||	[1:42,2:42,3:42]
			[1:10,2:20,3:30]	|	{null}						||	[1:null,2:null,3:null]
	}

	def "flat map entry"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.flatMapEntry(mapper).toMap() == result
		where:
			map					|	mapper									||	result
			[1:10,2:20,3:30]	|	{[(it.key):it.val]}						||	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{[(it.val):it.key]}						||	[10:1,20:2,30:3]
			[1:10,2:20,3:30]	|	{[(it.key):it.val,(it.val):it.key]}		||	[1:10,10:1,2:20,20:2,3:30,30:3]
	}

	def "flat map key"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.flatMapKey(keyMapper).toMap() == result
		where:
			map					|	keyMapper			||	result
			[1:10,2:20,3:30]	|	{[it]}				||	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{[it,it*10]}		||	[1:10,10:10,2:20,20:20,3:30,30:30]
	}

	def "distinctValues"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.distinctValues().toMap() == result
		where:
			map					||	result
			[1:10,2:10,3:20]	||	[1:10,3:20]
			[1:10,2:20,3:20]	||	[1:10,2:20]
	}

	def "sortedKeys"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.sortedKeys(comparator).toLinkedMap() == result
		where:
			map					|	comparator			||	result
			[1:20,2:20,3:20]	|	{a,b->a-b}			||	[1:20,2:20,3:20]
			[1:20,2:20,3:20]	|	{a,b->b-a}			||	[3:20,2:20,1:20]
	}

	def "sortedValues"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.sortedValues(comparator).toLinkedMap() == result
		where:
			map					|	comparator			||	result
			[1:10,2:20,3:30]	|	{a,b->a-b}			||	[1:10,2:20,3:30]
			[1:10,2:20,3:30]	|	{a,b->b-a}			||	[3:30,2:20,1:10]
	}

	def "find"() {
		given:
			def mapstr = new MapStream(map)
			Predicate<UberMaps.Entry> entryPredicate = predicate
		expect:
			mapstr.find(entryPredicate).orElse(null) == result
		where:
			map					|	predicate			|	result
			[1:10,2:20,3:30]	|	{it.key > 1}		|	entry(2, 20)
			[1:10,2:20,3:30]	|	{it.key > 2}		|	entry(3, 30)
			[1:10,2:20,3:30]	|	{it.val > 30}		|	null
			[1:10,2:20,3:30]	|	{it.val < 30}		|	entry(1, 10)
	}

	def "findKey"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.findKey(predicate).orElse(null) == result
		where:
			map					|	predicate		|	result
			[1:10,2:20,3:30]	|	{it > 1}		|	entry(2, 20)
			[1:10,2:20,3:30]	|	{it > 2}		|	entry(3, 30)
			[1:10,2:20,3:30]	|	{it > 3}		|	null
			[1:10,2:20,3:30]	|	{it < 3}		|	entry(1, 10)
	}

	def "findByKey"() {
		given:
			def mapstr = new MapStream(map)
		expect:
			mapstr.findByKey(key).orElse(null) == result
		where:
			map					|	key		|	result
			[1:10,2:20,3:30]	|	1		|	entry(1, 10)
			[1:10,2:20,3:30]	|	2		|	entry(2, 20)
			[1:10,2:20,3:30]	|	3		|	entry(3, 30)
			[1:10,2:20,3:30]	|	4		|	null
			[1:10,2:20,3:30]	|	null	|	null
	}
}
