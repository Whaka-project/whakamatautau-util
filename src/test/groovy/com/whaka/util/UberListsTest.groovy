package com.whaka.util

import java.util.function.BiPredicate

import spock.lang.Specification

class UberListsTest extends Specification {

	def "index-of"() {
		expect:
			UberLists.getIndex(list, item) == index
		where:
			list				|	item			||	index
			[]					|	-12				||	-1
			[]					|	null			||	-1
			[12]				|	null			||	-1
			[12, null]			|	null			||	1
			[1,2,3]				|	1				||	0
			[1,2,3]				|	2				||	1
			[1,2,3]				|	3				||	2
			[1,2,3]				|	4				||	-1
			[1,2,(int[])[3,4]]	|	(int[])[3,4]	||	2	// < default deep predicate is used
			[1,2,(int[])[3,4]]	|	(int[])[4,3]	||	-1
	}

	def "index-of with predicate"() {
		given:
			BiPredicate<?, Object> falsePredicate = Mock(BiPredicate)
			BiPredicate<?, Object> truePredicate = Mock(BiPredicate)
			BiPredicate<?, Object> shallowPredicate = Objects.&equals
			falsePredicate.test(_, _) >> false
			truePredicate.test(_, _) >> true
		expect:
			UberLists.getIndex(list, item, truePredicate) == (list.isEmpty() ? -1 : 0)
			UberLists.getIndex(list, item, falsePredicate) == -1
			UberLists.getIndex(list, item, shallowPredicate) == index
		where:
			list				|	item			||	index
			[]					|	-12				||	-1
			[]					|	null			||	-1
			[12]				|	null			||	-1
			[12, null]			|	null			||	1
			[1,2,3]				|	1				||	0
			[1,2,3]				|	2				||	1
			[1,2,3]				|	3				||	2
			[1,2,3]				|	4				||	-1
			[1,2,(int[])[3,4]]	|	(int[])[3,4]	||	-1	// < shallow predicate is used
			[1,2,(int[])[3,4]]	|	(int[])[4,3]	||	-1
	}
}
