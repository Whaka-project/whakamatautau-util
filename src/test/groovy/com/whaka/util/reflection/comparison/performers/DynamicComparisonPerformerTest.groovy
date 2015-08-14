package com.whaka.util.reflection.comparison.performers

import java.util.List
import java.util.function.Function

import spock.lang.Specification

import com.whaka.util.reflection.comparison.ComparisonPerformer
import com.whaka.util.reflection.comparison.ComparisonResult

class DynamicComparisonPerformerTest extends Specification {

	def "construction"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
		expect:
			performer.getDefaultDelegate().is(performer.getDefaultDelegate())
			performer.getRegisteredDelegates().isEmpty()
			performer.getArrayDelegateProviders().isEmpty()
			performer.getCollectionDelegateProviders().isEmpty()
	}

	def "register/remove performer"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
			ComparisonPerformer<Object> delegate1 = Mock()
			ComparisonPerformer<Object> delegate2 = Mock()
			def result = null

		when: "performer is registered for a type"
			result = performer.registerDelegate(String, delegate1)
		then: "map of performers contains registered instance with the type, as key"
			result.is(performer)
			performer.getRegisteredDelegates() == [(String): delegate1]

		when: "another performer is registered for another type"
			result = performer.registerDelegate(Number, delegate2)
		then: "both performers are awailable in the map with types as keys"
			result.is(performer)
			performer.getRegisteredDelegates() == [(String): delegate1, (Number): delegate2]

		when: "map is changed directly"
			performer.getRegisteredDelegates().remove(String)
		then: "it affects actual state of the performer"
			performer.getRegisteredDelegates() == [(Number): delegate2]
	}

	def "find registered performer - simple type"() {
		given: "delegate performers for types String, Number, and Object"
			ComparisonPerformer<String> delegateString = Mock()
			ComparisonPerformer<Number> delegateNumber = Mock()
			ComparisonPerformer<Object> delegateObject = Mock()
		and: "String, and Number delegates are registered in the dynamic performer"
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
			performer.registerDelegate(String, delegateString)
			performer.registerDelegate(Number, delegateNumber)
			def result = null

		when: "find registered performer is called with two String objects"
			result = performer.findRegisteredDelegate("", "")
		then: "String delegate is returned"
			result.is(delegateString)

		when: "find registered performer is called with two Integer objects"
			result = performer.findRegisteredDelegate(42 as Integer, 43 as Integer)
		then: "Number delegate is returned"
			result.is(delegateNumber)

		when: "find registered performer is called with one Long and one Double objects"
			result = performer.findRegisteredDelegate(1L as Long, 1.2 as Double)
		then: "Number delegate is also returned"
			result.is(delegateNumber)

		when: "find registered performer is called with one String and one Integer objects"
			result = performer.findRegisteredDelegate("", 42 as Integer)
		then: "null is returned, for there's no delegate registered for any type, common to specified objects"
			result == null

		when: "Object delegate is registered in the dynamic performer"
			performer.registerDelegate(Object, delegateObject)
		and: "find registered performer is called again with a String and an Integer"
			result = performer.findRegisteredDelegate("", 42 as Integer)
		then: "Object delegate is returned"
			result.is(delegateObject)
	}

	def "find registered performer - registration order"() {
		given: "delegate performers for types String, Number, and Object"
			ComparisonPerformer<Object> delegateString = Mock()
			ComparisonPerformer<Object> delegateInteger = Mock()
			ComparisonPerformer<Object> delegateObject = Mock()
		and: "delegates are registred in the dynamic performer in this order: String, Object, Number"
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
			performer.registerDelegate(String, delegateString)
			performer.registerDelegate(Object, delegateObject)
			performer.registerDelegate(Number, delegateInteger)
			def result = null

		when: "find registered performer is called with two strings"
			result = performer.findRegisteredDelegate("", "")
		then: "String delegate is returned"
			result.is(delegateString)

		when: "find registered performer is called with two integers"
			result = performer.findRegisteredDelegate(42 as Integer, 43 as Integer)
		then: "Object delegate is returned, for it was registered before Number delegate"
			result.is(delegateObject)

		when: "find registered performer is called with a string and an integer"
			result = performer.findRegisteredDelegate("", 43 as Integer)
		then: "also Object delegate is returned"
			result.is(delegateObject)
	}

	def "find registered performer - array"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
		when: "find registered performer is called with two arrays"
			String[] arr1 = ["qwe", "rty"]
			Integer[] arr2 = [42, 43]
			ComparisonPerformer result = performer.findRegisteredDelegate(arr1, arr2)
		then: "returned result is a special array performer with dynamic performer itself as a delegate"
			result instanceof ArrayComparisonPerformer
			result.getElementPerformer().is(performer)
	}

	def "override default array performer"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
			ComparisonPerformer<CharSequence[]> delegateArray = Mock()
		and: "function converting one performer into another is created and registered for the CharSequence[] class"
			Function<ComparisonPerformer<?>, ComparisonPerformer<?>> mockProvider = Mock()
			performer.registerArrayDelegateProvider(CharSequence[], mockProvider)

		when: "find registered performer is called with two String arrays"
			String[] arr1 = ["qwe", "rty"]
			String[] arr2 = ["pop", "qaz"]
			ComparisonPerformer result = performer.findRegisteredDelegate(arr1, arr2)
		then: "registered provider function gets called once with dynamic performer itself as argument"
			1 * mockProvider.apply(performer) >> delegateArray
		and: "object returned by the provider is the result"
			result.is(delegateArray)

		when: "find registered performer is called with one Object and one String arrays"
			Object[] objArr1 = [1,2,3]
			ComparisonPerformer result2 = performer.findRegisteredDelegate(objArr1, arr2)
		then: "provider function doesn't get called, for specified objects are not subtypes of the registered class"
			0 * mockProvider.apply(_)
		and: "default array performer is returned"
			result2 instanceof ArrayComparisonPerformer
			result2.getElementPerformer().is(performer)
	}

	def "overriding array performer - order"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
		and:
			Function<ComparisonPerformer<?>, ComparisonPerformer<?>> mockProviderObject = Mock()
			Function<ComparisonPerformer<?>, ComparisonPerformer<?>> mockProviderCharSequence = Mock()
			performer.registerArrayDelegateProvider(Object[], mockProviderObject)
			performer.registerArrayDelegateProvider(CharSequence[], mockProviderCharSequence)

		when: "find registered performer is called with two String arrays"
			String[] arr1 = ["qwe", "rty"]
			String[] arr2 = ["pop", "qaz"]
			ComparisonPerformer result = performer.findRegisteredDelegate(arr1, arr2)
		then: "Object provider is called, for it was registered first"
			1 * mockProviderObject.apply(performer)
		and: "CharSequence provider doesn't get called"
			0 * mockProviderCharSequence.apply(_)
	}

	def "bidimensional array"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
			ComparisonPerformer<CharSequence[][]> delegateArray = Mock()
		and: "function provider is registered for the bidimensional CharSequence[][] class"
			Function<ComparisonPerformer<?>, ComparisonPerformer<?>> mockProvider = Mock()
			performer.registerArrayDelegateProvider(CharSequence[][], mockProvider)

		when: "find registered performer is called with two String arrays"
			String[] arr1 = ["qwe", "rty"]
			String[] arr2 = ["pop", "qaz"]
			ComparisonPerformer result = performer.findRegisteredDelegate(arr1, arr2)
		then: "function provider doesn't get called, for arrays aren't instances of the registered class"
			0 * mockProvider.apply(_)
		and: "default array performer is returned"
			result instanceof ArrayComparisonPerformer
			result.getElementPerformer().is(performer)

		when: "find registered delegate is called with two bidimensional String[][] objects"
			String[][] biArr1 = [arr1, arr2]
			String[][] biArr2 = [arr2, arr1]
			ComparisonPerformer result2 = performer.findRegisteredDelegate(biArr1, biArr2)
		then: "registered provider is called once with the dynamic performer itself as argument"
			1 * mockProvider.apply(performer) >> delegateArray
		and: "object returned by the provider is the result"
			result2.is(delegateArray)
	}

	def "Warning: arrays of objects!"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
			ComparisonPerformer<Object[][]> delegateArray = Mock()
		and: "function provider is registered for the single dimensional Object[] class"
			Function<ComparisonPerformer<?>, ComparisonPerformer<?>> mockProvider = Mock()
			performer.registerArrayDelegateProvider(Object[], mockProvider)
		when: "find registered performer is called with two bidimentional Object[][] instances"
			Object[][] arr1 = [[1,2,3], ["qwe", "rty", "qaz"]]
			Object[][] arr2 = [[5,6,7], ["q", "r", "z"]]
			def result = performer.findRegisteredDelegate(arr1, arr2)
		then: "registered provider is called, for Object[][] is an instance of the Object[]"
			1 * mockProvider.apply(performer)

		// Beware of the Object class! It covers everything!
	}

	def "simple types override arrays"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
		and: "delegate performers are registered for the types: String[] and CharSequence"
			ComparisonPerformer<String[]> delegateArray = Mock()
			ComparisonPerformer<Object> delegateCharSequence = Mock()
			performer.registerDelegate(String[], delegateArray)
			performer.registerDelegate(CharSequence[], delegateCharSequence)
		and: "delegate provider is registered for the Object[] class"
			Function<ComparisonPerformer<?>, ComparisonPerformer<?>> providerArray = Mock()
			performer.registerArrayDelegateProvider(Object[], providerArray)
			def result = null

		when: "find registered performers is called with two String[] arrays"
			String[] arr1 = ["qwe", "rty"]
			String[] arr2 = ["pop", "qaz"]
			result = performer.findRegisteredDelegate(arr1, arr2)
		then: "delegate provider is not called"
			0 * providerArray._
		and: "result is the delegate String[] performer"
			result.is(delegateArray)

		when: "find registered performers is called with two CharSequence[] arrays"
			CharSequence[] csArr1 = ["qwe", "rty"]
			CharSequence[] csArr2 = ["pop", "qaz"]
			result = performer.findRegisteredDelegate(csArr1, csArr2)
		then: "delegate provider is not called"
			0 * providerArray._
		and: "result is the delegate CharSequence[] performer"
			result.is(delegateCharSequence)

		when: "find registered performers is called with two Number[] arrays"
			Number[] nArr1 = [1,2,3]
			Number[] nArr2 = [3,4,5]
			result = performer.findRegisteredDelegate(nArr1, nArr2)
		then: "delegate provider is called, for no registered type provider was found"
			1 * providerArray.apply(performer) >> null
		and: "object returned by the provider is the result"
			result == null
	}

	def "find registered performer - collection/list"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
		when: "find registered performer is called with two collections of any type"
			ComparisonPerformer result = performer.findRegisteredDelegate(collection1, collection2)
		then: "result is an instance of the Set performer, that compares collections, as sets"
			result instanceof SetComparisonPerformer
		and: "result has dynamic performer itself as delegate"
			result.getElementPerformer().is(performer)
		where:
			collection1						|	collection2
			[1,2,3] as Collection<?>		|	["qwe","rty","qaz"] as Collection<?>
			[] as Collection<?>				|	[42] as Collection<?>
			['q'] as Set<Character>			|	[42] as List<Integer>
			['q'] as Set<Character>			|	[42] as Set<Integer>
			[3,2,1] as List<Integer>		|	[4,3,2] as List<Integer>
			[[1,2],2,1] as List<Object>		|	[4,3,2] as List<Integer>
	}

	def "override default collection performer"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
			Function<ComparisonPerformer<?>, ComparisonPerformer<Collection<?>>> mockProviderCollection = Mock()
			ComparisonPerformer<List<?>> delegateList = Mock()
		and: "function converting one performer into another is registered for the types: List of CharSequence"
			Function<ComparisonPerformer<?>, ComparisonPerformer<List<?>>> mockProvider = Mock()
			performer.registerCollectionDelegateProvider(List, CharSequence, mockProvider)
		and:
			def result = null

		when: "find registered performer is called with two lists of strings"
			List<String> list1 = ["qwe", "rty"]
			List<String> list2 = ["pop", "qaz"]
			result = performer.findRegisteredDelegate(list1, list2)
		then: "registered provider function gets called once with dynamic performer itself as argument"
			1 * mockProvider.apply(performer) >> delegateList
		and: "object returned by the provider is the result"
			result.is(delegateList)

		when: "find registered performer is called with two lists of char sequences declared as List<Object>"
			List<Object> csList1 = ["qwe", new StringBuilder()]
			List<Object> csList2 = [new StringBuffer(), "qaz"]
			result = performer.findRegisteredDelegate(csList1, csList2)
		then: "provider function is called again, for all elements of both collections are instances of CharSequence"
			1 * mockProvider.apply(performer) >> delegateList
		and:
			result.is(delegateList)

		when: "find registered performer is called with a list of char sequences and a list of objects"
			List<Object> objList = ["pop", 42]
			result = performer.findRegisteredDelegate(csList1, objList)
		then: "provider function doesn't get called"
			0 * mockProvider.apply(_)
		and: "default collection performer is returned"
			result instanceof SetComparisonPerformer
			result.getElementPerformer().is(performer)

		when: "find registered performer is called with two sets of strings"
			Set<String> set1 = ["qwe", "rty"]
			Set<String> set2 = ["pop", "qaz"]
			result = performer.findRegisteredDelegate(set1, set2)
		then: "provider function doesn't get called, for collections aren't instances of the List"
			0 * mockProvider.apply(_)
		and: "default collection performer is returned"
			result instanceof SetComparisonPerformer
			result.getElementPerformer().is(performer)

		when: "new delegate provider function is registered for the types: Collection of Objects"
			performer.registerCollectionDelegateProvider(Collection, Object, mockProviderCollection)
		and:  "find registered performer is called with two sets of strings again"
			result = performer.findRegisteredDelegate(set1, set2)
		then: "newly registered provider is called once"
			1 * mockProviderCollection.apply(performer) >> null
		and: "object returned by delegate is the result"
			result == null
	}

	def "overriding collection performer - order"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
		and:
			Function<ComparisonPerformer<?>, ComparisonPerformer<?>> mockProviderCollection = Mock()
			Function<ComparisonPerformer<?>, ComparisonPerformer<?>> mockProviderList = Mock()
			performer.registerCollectionDelegateProvider(Collection, Object, mockProviderCollection)
			performer.registerCollectionDelegateProvider(List, Object, mockProviderList)

		when: "find registered performer is called with two lists"
			List<String> list1 = ["qwe", "rty"]
			List<String> list2 = ["pop", "qaz"]
			ComparisonPerformer result = performer.findRegisteredDelegate(list1, list2)
		then: "Collection provider is called, for it was registered first"
			1 * mockProviderCollection.apply(performer)
		and: "List provider doesn't get called"
			0 * mockProviderList.apply(_)
	}

	def "overriding collection performer - elements order"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
		and:
			Function<ComparisonPerformer<?>, ComparisonPerformer<?>> mockProviderListOfObjects = Mock()
			Function<ComparisonPerformer<?>, ComparisonPerformer<?>> mockProviderListOfStrings = Mock()
			performer.registerCollectionDelegateProvider(List, Object, mockProviderListOfObjects)
			performer.registerCollectionDelegateProvider(List, String, mockProviderListOfStrings)

		when: "find registered performer is called with two lists of strings"
			List<String> list1 = ["qwe", "rty"]
			List<String> list2 = ["pop", "qaz"]
			ComparisonPerformer result = performer.findRegisteredDelegate(list1, list2)
		then: "List of Objects provider is called, for it was registered first"
			1 * mockProviderListOfObjects.apply(performer)
		and: "List of Strings provider doesn't get called"
			0 * mockProviderListOfStrings.apply(_)
	}

	def "find registered performer - map"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
		when: "find registered performer is called with two maps of any type"
			ComparisonPerformer result = performer.findRegisteredDelegate(collection1, collection2)
		then: "result is an instance of the Map performer, that compares maps, by keys"
			result instanceof MapComparisonPerformer
		and: "result has dynamic performer itself as delegate"
			result.getElementPerformer().is(performer)
		where:
			collection1						|	collection2
			[:] as Map<?,?>					|	[:] as Map<?,?>
			[q:1] as Map<?,?>				|	[w:2] as Map<?,?>
			[q:1] as HashMap<?,?>			|	[w:2] as LinkedHashMap<?, ?>
			[q:1, (1):"q"] as TreeMap<?, ?>	|	[w:2,false:true] as LinkedHashMap<?, ?>
	}

	def "get registered or default performer"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
		and: "delegate performers are registered for the types: CharSequence and Number"
			ComparisonPerformer<Object> delegateCharSequence = Mock()
			ComparisonPerformer<Object> delegateNumber = Mock()
			performer.registerDelegate(CharSequence, delegateCharSequence)
			performer.registerDelegate(Number, delegateNumber)

		expect: "when common registered delegate cannot be found - default performer is returned"
			performer.getDelegate("", "").is(delegateCharSequence)
			performer.getDelegate(12, 42).is(delegateNumber)
			performer.getDelegate(12, "").is(performer.getDefaultDelegate())
			performer.getDelegate(false, true).is(performer.getDefaultDelegate())
			performer.getDelegate(null, null).is(performer.getDefaultDelegate())
	}

	def "perform comparison"() {
		given:
			DynamicComparisonPerformer performer = new DynamicComparisonPerformer()
			ComparisonResult finalResult = new ComparisonResult(1, 2, null, false)
		and: "delegate performer is registered for the type String"
			ComparisonPerformer<String> delegateString = Mock()
			performer.registerDelegate(String, delegateString)
		and: "new default delegate is set"
			ComparisonPerformer<Object> delegateObject = Mock()
			performer.setDefaultDelegate(delegateObject)
			def result = null

		when: "perform comparison is called with two strings "
			result = performer.compare("qwe", "qaz")
		then: "call is delegated to registered String delegate with the same arguments"
			1 * delegateString.compare("qwe", "qaz") >> finalResult
		and: "object returned from delegate is the result"
			result.is(finalResult)

		when: "perform comparison is called with two numbers"
			result = performer.compare(42, 43)
		then: "default delegate is used, for there's no specific delegate is registered for any ancestor of numbers"
			1 * delegateObject.compare(42, 43) >> finalResult
		and: "object returned from delegate is the result"
			result.is(finalResult)
	}
}
