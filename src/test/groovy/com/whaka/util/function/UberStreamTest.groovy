package com.whaka.util.function

import java.util.function.Supplier
import java.util.stream.Stream

import spock.lang.Specification

class UberStreamTest extends Specification {

	def "construction"() {
		given:
			Stream stream = Mock()
		when:
			def uber = new UberStream(stream)
		then:
			uber.getActual().is(stream)
	}

	def "drop"() {
		given:
			UberStream uber = new UberStream(col.stream())
		expect:
			uber.drop(filter).toArray() == result
		where:
			col					|	filter					|	result
			[1,2,3]				|	{it > 3}				|	[1,2,3]
			[1,2,3]				|	{it > 2}				|	[1,2]
			[1,2,3]				|	{it > 1}				|	[1]
			[1,2,3]				|	{it > 0}				|	[]
			["qwe", "rty"]		|	{it.startsWith("q")}	|	["rty"]
			["qwe", "rty"]		|	{it.startsWith("r")}	|	["qwe"]
			["qwe", "rty"]		|	{it.startsWith("z")}	|	["qwe","rty"]
			["qwe", "rty"]		|	{it.length() == 3}		|	[]
			[1,2,null]			|	{it > 0}				|	[null]
			[1,null]			|	{it > 0}				|	[null]
			[null]				|	{it > 0}				|	[null]
	}

	def "dropNulls"() {
		given:
			UberStream uber = new UberStream(col.stream())
		expect:
			uber.dropNulls().toArray() == result
		where:
			col					|	result
			[1,2,3]				|	[1,2,3]
			[1,2]				|	[1,2]
			[1]					|	[1]
			[1,2,null,4]		|	[1,2,4]
			["qwe", "rty"]		|	["qwe","rty"]
			[null, "rty"]		|	["rty"]
			["qwe", null]		|	["qwe"]
			[null, null]		|	[]
	}

	def "find"() {
		given:
			UberStream uber = new UberStream(col.stream())
		expect:
			uber.find(filter) == result
		where:
			col					|	filter					|	result
			[1,2,3]				|	{it > 3}				|	null
			[1,2,3]				|	{it > 2}				|	3
			[1,2,3]				|	{it > 1}				|	2
			[1,2,3]				|	{it > 0}				|	1
			[1,2,3]				|	{it < 4}				|	1
			[1,2,3]				|	{it < 3}				|	1
			[1,2,3]				|	{it < 2}				|	1
			[1,2,3]				|	{it < 1}				|	null
			["qwe", "rty"]		|	{it.startsWith("q")}	|	"qwe"
			["qwe", "rty"]		|	{it.startsWith("r")}	|	"rty"
			["qwe", "rty"]		|	{it.startsWith("z")}	|	null
			["qwe", "rty"]		|	{it.length() == 3}		|	"qwe"
			[1,2,null]			|	{it > 0}				|	1
			[1,null]			|	{it > 0}				|	1
			[null]				|	{it > 0}				|	null
	}

	def "flatMapCol"() {
		given:
			UberStream uber = new UberStream(col.stream())
		expect:
			uber.flatMapCol(mapper).toArray() == result
		where:
			col					|	mapper							|	result
			[1,2,3]				|	{[it * 2, it * 3]}				|	[2, 3, 4, 6, 6, 9]
			["qwe", "rty"]		|	{it.toCharArray() as List}		|	['q','w','e','r','t','y']
	}

	def "flatMapArr"() {
		given:
			UberStream uber = new UberStream(col.stream())
		expect:
			uber.flatMapArr(mapper).toArray() == result
		where:
			col					|	mapper								|	result
			[1,2,3]				|	{[it * 2, it * 3] as Integer[]}		|	[2, 3, 4, 6, 6, 9]
			["qwe", "rty"]		|	{it.toCharArray() as Character[]}	|	['q','w','e','r','t','y']
	}

	def "toArray"() {
		given:
			UberStream uber = new UberStream(col.stream())
		expect:
			uber.toArray(type) == result
		where:
			col					|	type		|	result
			[1,2,3]				|	Integer		|	[1,2,3] as Integer[]
			["qwe", "rty"]		|	String		|	["qwe", "rty"] as String[]
	}

	def "to"() {
		expect:
			new UberStream((col as Collection).stream()).to({-> new LinkedList<>()}) instanceof LinkedList
			new UberStream((col as Collection).stream()).to({-> new LinkedList<>()}) == col as LinkedList
			new UberStream((col as Collection).stream()).to({-> new HashSet<>()}) instanceof HashSet
			new UberStream((col as Collection).stream()).to({-> new HashSet<>()}) == col as HashSet
		where:
			col << [
				[1,2,3,3] as int[],
				[1,2,3,3] as Set,
				[1,2,3,3] as List,
				["qwe", "rty"] as String[]
			]
	}

	def "toList"() {
		given:
			UberStream uber = new UberStream((col as Collection).stream())
		expect:
			uber.toList() == col as List
		where:
			col << [
				[1,2,3,3] as int[],
				[1,2,3,3] as Set,
				[1,2,3,3] as List,
				["qwe", "rty"] as String[]
			]
	}

	def "toSet"() {
		given:
			UberStream uber = new UberStream((col as Collection).stream())
		expect:
			uber.toSet() == col as Set
		where:
			col << [
				[1,2,3,3] as int[],
				[1,2,3,3] as Set,
				[1,2,3,3] as List,
				["qwe", "rty"] as String[]
			]
	}

	def "toMap"() {
		given:
			UberStream uber = new UberStream(col.stream())
		expect:
			uber.toMap(mapper) == result
		where:
			col 			|	mapper				|	result
			[]				|	{it * 2}			|	[:]
			[1,2,3]			|	{it * 2}			|	[2:1, 4:2, 6:3]
			[1,2,3]			|	{it}				|	[1:1, 2:2, 3:3]
			["qwe","rty"]	|	{it.charAt(0)}		|	[("qwe".charAt(0)):"qwe", ("rty".charAt(0)):"rty"]
			["q:w","q:e"]	|	{it.split(":")[0]}	|	["q":"q:e"]		// << duplicated keys are allowed
	}

	def "toMap with supplier"() {
		given:
			Supplier hash = {-> new HashMap()}
			Supplier tree = {-> new TreeMap()}
			Supplier link = {-> new LinkedHashMap()}
		expect:
			new UberStream(col.stream()).toMap(mapper, hash) == result
			new UberStream(col.stream()).toMap(mapper, hash) instanceof HashMap
			new UberStream(col.stream()).toMap(mapper, tree) == result
			new UberStream(col.stream()).toMap(mapper, tree) instanceof TreeMap
			new UberStream(col.stream()).toMap(mapper, link) == result
			new UberStream(col.stream()).toMap(mapper, link) instanceof LinkedHashMap
		where:
			col 			|	mapper				|	result
			[]				|	{it * 2}			|	[:]
			[1,2,3]			|	{it * 2}			|	[2:1, 4:2, 6:3]
			[1,2,3]			|	{it}				|	[1:1, 2:2, 3:3]
			["qwe","rty"]	|	{it.charAt(0)}		|	[("qwe".charAt(0)):"qwe", ("rty".charAt(0)):"rty"]
			["q:w","q:e"]	|	{it.split(":")[0]}	|	["q":"q:e"]		// << duplicated keys are allowed
	}

	def "toMapStream"() {
		given:
			UberStream uber = new UberStream(col.stream())
		when:
			def res = uber.toMapStream(mapper)
		then:
			res instanceof MapStream
			res.toMap() == result
		where:
			col 			|	mapper				|	result
			[]				|	{it * 2}			|	[:]
			[1,2,3]			|	{it * 2}			|	[2:1, 4:2, 6:3]
			[1,2,3]			|	{it}				|	[1:1, 2:2, 3:3]
			["qwe","rty"]	|	{it.charAt(0)}		|	[("qwe".charAt(0)):"qwe", ("rty".charAt(0)):"rty"]
			["q:w","q:e"]	|	{it.split(":")[0]}	|	["q":"q:e"]		// << duplicated keys are allowed
	}

	def "join"() {
		given:
			UberStream uber = new UberStream(col.stream())
		expect:
			uber.join(delim) == result
		where:
			col 			|	delim				|	result
			[]				|	"qwe"				|	""
			[1,2,3]			|	""					|	"123"
			[1,2,3]			|	","					|	"1,2,3"
			[1,2,3]			|	", "				|	"1, 2, 3"
			["qwe","rty"]	|	"qwe"				|	"qweqwerty"
			["qwe","rty"]	|	":"					|	"qwe:rty"
	}

	def "join with prefix and suffix"() {
		given:
			UberStream uber = new UberStream(col.stream())
		expect:
			uber.join(delim, pref, suff) == result
		where:
			col 			|	delim	|	pref	|	suff	||	result
			[]				|	"qwe"	|	"["		|	"]"		||	"[]"
			[1,2,3]			|	""		|	"q"		|	"w"		||	"q123w"
			[1,2,3]			|	","		|	"*"		|	"*"		||	"*1,2,3*"
			[1,2,3]			|	", "	|	", "	|	", "	||	", 1, 2, 3, "
			["qwe","rty"]	|	"qwe"	|	"qwe"	|	"rty"	||	"qweqweqwertyrty"
			["qwe","rty"]	|	":"		|	":"		|	""		||	":qwe:rty"
	}
}
