package org.whaka.util.function

import java.util.function.BinaryOperator
import java.util.function.Supplier
import java.util.stream.Stream

import spock.lang.Specification

class MapCollectorsTest extends Specification {

	def "toMap"() {
		given:
			Stream<Map.Entry> stream = map.entrySet().stream()
		expect:
			stream.collect(MapCollectors.toMap()) == map
		where:
			map << maps()
	}

	def "toMap with supplier"() {
		given:
			Stream<Map.Entry> stream = map.entrySet().stream()
		when:
			def res = stream.collect(MapCollectors.toMap({new LinkedHashMap<>()}))
		then:
			res instanceof LinkedHashMap
			res.equals(map)
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

	def "toMap with two functions"() {
		given:
			Stream stream = col.stream()
		expect:
			stream.collect(MapCollectors.toMap(keyMapper, valMapper)) == result
		where:
			col						|	keyMapper			|	valMapper		||	result
			[1,2,3,3]				|	{it}				|	{it}			||	[1:1,2:2,3:3]
			[1,2,3,3]				|	{it*2}				|	{it*10}			||	[2:10,4:20,6:30]
			[1,2,3,3]				|	{it}				|	{null}			||	[1:null,2:null,3:null]
			[1,2,3,3]				|	{null}				|	{it}			||	[(null):3]
			["q:a","w:b","w:c"]		|	{it.split(":")[0]}	|	{it}			||	["q":"q:a","w":"w:c"]
	}

	def "toMap with two functions and merger"() {
		given:
			Stream stream = col.stream()
			BinaryOperator merger = {a,b -> a == null ? null : a + b}
		expect:
			stream.collect(MapCollectors.toMap(keyMapper, valMapper, merger)) == result
		where:
			col						|	keyMapper			|	valMapper		||	result
			[1,2,3,3]				|	{it}				|	{it}			||	[1:1,2:2,3:6]
			[1,2,3,3]				|	{it*2}				|	{it*10}			||	[2:10,4:20,6:60]
			[1,2,3,3]				|	{it}				|	{null}			||	[1:null,2:null,3:null]
			[1,2,3,3]				|	{null}				|	{it}			||	[(null):9]
			["q:a","w:b","w:c"]		|	{it.split(":")[0]}	|	{it}			||	["q":"q:a","w":"w:bw:c"]
	}

	def "toMap with two functions and supplier"() {
		given:
			Stream stream = col.stream()
			Supplier supplier = {new LinkedHashMap<>()}
		when:
			def res = stream.collect(MapCollectors.toMap(keyMapper, valMapper, supplier))
		then:
			res instanceof LinkedHashMap
			res.equals(result)
		where:
			col						|	keyMapper			|	valMapper		||	result
			[1,2,3,3]				|	{it}				|	{it}			||	[1:1,2:2,3:3]
			[1,2,3,3]				|	{it*2}				|	{it*10}			||	[2:10,4:20,6:30]
			[1,2,3,3]				|	{it}				|	{null}			||	[1:null,2:null,3:null]
			[1,2,3,3]				|	{null}				|	{it}			||	[(null):3]
			["q:a","w:b","w:c"]		|	{it.split(":")[0]}	|	{it}			||	["q":"q:a","w":"w:c"]
	}

	def "toMap with two functions, merger, and supplier"() {
		given:
			Stream stream = col.stream()
			BinaryOperator merger = {a,b -> a == null ? null : a + b}
			Supplier supplier = {new LinkedHashMap<>()}
		when:
			def res = stream.collect(MapCollectors.toMap(keyMapper, valMapper, merger, supplier))
		then:
			res instanceof LinkedHashMap
			res.equals(result)
		where:
			col						|	keyMapper			|	valMapper		||	result
			[1,2,3,3]				|	{it}				|	{it}			||	[1:1,2:2,3:6]
			[1,2,3,3]				|	{it*2}				|	{it*10}			||	[2:10,4:20,6:60]
			[1,2,3,3]				|	{it}				|	{null}			||	[1:null,2:null,3:null]
			[1,2,3,3]				|	{null}				|	{it}			||	[(null):9]
			["q:a","w:b","w:c"]		|	{it.split(":")[0]}	|	{it}			||	["q":"q:a","w":"w:bw:c"]
	}
}
