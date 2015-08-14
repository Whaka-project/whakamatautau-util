package com.whaka.util.reflection.comparison.performers

import java.lang.reflect.Method
import java.util.function.Predicate
import java.util.regex.Pattern

import spock.lang.Specification

import com.whaka.util.reflection.comparison.ComparisonPerformer
import com.whaka.util.reflection.comparison.ComparisonPerformers
import com.whaka.util.reflection.comparison.TestEntities
import com.whaka.util.reflection.comparison.performers.GettersDynamicPerformerBuilder.PatternPredicate
import com.whaka.util.reflection.properties.ClassPropertyExtractor
import com.whaka.util.reflection.properties.ClassPropertyKey
import com.whaka.util.reflection.properties.GetterClassProperty
import com.whaka.util.reflection.properties.GettersExtractor

class GettersDynamicPerformerBuilderTest extends Specification {

	def "construction"() {
		when:
			GettersDynamicPerformerBuilder<?> builder = new GettersDynamicPerformerBuilder(type)
		then:
			builder.getType() == type
			builder.getIncludingFilters().isEmpty()
			builder.getExcludingFilters().isEmpty()
			builder.getGettersExtractor() instanceof GettersExtractor
			builder.getDynamicPerformer().is(builder.getDynamicPerformer())

		when:
			ClassPropertyExtractor<?> mockExtractor = Mock()
			GettersDynamicPerformerBuilder<?> builder2 = new GettersDynamicPerformerBuilder(type, mockExtractor)
		then:
			builder2.getType() == type
			builder2.getIncludingFilters().isEmpty()
			builder2.getExcludingFilters().isEmpty()
			builder2.getGettersExtractor().is(mockExtractor)
			builder2.getDynamicPerformer().is(builder2.getDynamicPerformer())

		when:
			new GettersDynamicPerformerBuilder(type, null)
		then:
			thrown(NullPointerException)

		where:
			type << [null, String, Integer, Object, GettersDynamicPerformerBuilderTest]
	}

	def "add including/excluding filter - predicate"() {
		given:
			GettersDynamicPerformerBuilder<?> builder = new GettersDynamicPerformerBuilder(Object)
			Predicate<Method> mockPredicate = Mock()
			Predicate<Method> mockPredicate2 = Mock()

		expect: "both including and excluding filters are empty at the beginning"
			builder.getIncludingFilters().isEmpty()
			builder.getExcludingFilters().isEmpty()

		when: "add filter is called with a predicate"
			builder.addFilter(mockPredicate)
		then: "including filters are now contains this predicate"
			builder.getIncludingFilters() == [mockPredicate] as Set

		when: "add filter is called with the same (equal) predicate again"
			builder.addFilter(mockPredicate)
		then: "including filters contains only one instance, cuz it's a set"
			builder.getIncludingFilters() == [mockPredicate] as Set

		when: "add filter is called with another predicate"
			builder.addFilter(mockPredicate2)
		then: "including filters contains both predicates"
			builder.getIncludingFilters() == [mockPredicate, mockPredicate2] as Set

		when: "add excluding filter is called with the same predicate any number of times"
			builder.addExcludingFilter(mockPredicate)
			builder.addExcludingFilter(mockPredicate)
		then: "exclusing filters are now contains one instance of this predicate"
			builder.getExcludingFilters() == [mockPredicate] as Set

		when: "add excluding filter is called with another predicate any number of times"
			builder.addExcludingFilter(mockPredicate2)
			builder.addExcludingFilter(mockPredicate2)
		then: "exclusing filters are now contains one instance of each predicate"
			builder.getExcludingFilters() == [mockPredicate, mockPredicate2] as Set
	}

	def "add including/exclusing filter - pattern"() {
		given:
			GettersDynamicPerformerBuilder<?> builder = new GettersDynamicPerformerBuilder(Object)
			PatternPredicate ppQwe = new PatternPredicate("qwe")
			PatternPredicate ppRty = new PatternPredicate("rty")

		expect:
			builder.getIncludingFilters().isEmpty()
			builder.getExcludingFilters().isEmpty()

		when: "add filter is called with the same string any anount of times"
			builder.addFilter("qwe")
			builder.addFilter("qwe")
		then: "include filters contains single new PatternPredicate created from the string"
			builder.getIncludingFilters() == [ppQwe] as Set

		when: "add filter is called with another string"
			builder.addFilter("rty")
			builder.addFilter("rty")
		then: "new predicate is created"
			builder.getIncludingFilters() == [ppQwe, ppRty] as Set

		when:
			builder.addExcludingFilter("rty")
			builder.addExcludingFilter("rty")
		then:
			builder.getExcludingFilters() == [ppRty] as Set

		when:
			builder.addExcludingFilter("qwe")
			builder.addExcludingFilter("qwe")
		then:
			builder.getExcludingFilters() == [ppRty, ppQwe] as Set
	}

	def "build"() {
		given: "custom mock property extractor and builder created upon it"
			ClassPropertyExtractor<GetterClassProperty<?, ?>> mockExtractor = Mock()
			GettersDynamicPerformerBuilder<?> builder = new GettersDynamicPerformerBuilder(Object, mockExtractor)
		and: "two custom mock 'including' filters registered in the builder"
			Predicate<Method> includingFilter1 = Mock()
			Predicate<Method> includingFilter2 = Mock()
			builder.addFilter(includingFilter1)
			builder.addFilter(includingFilter2)
		and: "two custom mock 'excluding' filters registered in the builder"
			Predicate<Method> excludingFilter1 = Mock()
			Predicate<Method> excludingFilter2 = Mock()
			builder.addExcludingFilter(excludingFilter1)
			builder.addExcludingFilter(excludingFilter2)
		and: "map of three mock getter properties, mapped by custom keys"
			ClassPropertyKey key1 = Mock()
			ClassPropertyKey key2 = Mock()
			ClassPropertyKey key3 = Mock()
			ClassPropertyKey keyStatic = Mock()
			GetterClassProperty<?, ?> getter1 = Mock()
			getter1.getKey() >> key1
			GetterClassProperty<?, ?> getter2 = Mock()
			getter2.getKey() >> key2
			GetterClassProperty<?, ?> getter3 = Mock()
			getter3.getKey() >> key3
			GetterClassProperty<?, ?> getterStatic = Mock()
			getterStatic.getKey() >> keyStatic
			Map<ClassPropertyKey, GetterClassProperty<?, ?>> gettersMap = [
					(key1): getter1,
					(key2): getter2,
					(key3): getter3,
					(keyStatic): getterStatic,
				]
		and: "one of the getter properties is static"
			getterStatic.isStatic() >> true
		and: "two any method objects"
			Method theMethod1 = Object.class.getDeclaredMethod("equals", Object)
			Method theMethod2 = Object.class.getDeclaredMethod("hashCode")
			Method theMethod3 = Object.class.getDeclaredMethod("clone")

		when: "build is called with some name"
			def result = builder.build("someName")
		then: "extractor, specified in the constructor is called with the class, specified in the constructor"
			1 * mockExtractor.extractAll(Object) >> gettersMap

		and: "Method is received from the first property value from the returned map of properties"
			1 * getter1.getGetter() >> theMethod1
		and: "including filters are called in order, until any of them returns true"
			1 * includingFilter1.test(theMethod1) >> false
			1 * includingFilter2.test(theMethod1) >> true
		and: "excluding filters are called in order, until any of them returns true"
			1 * excludingFilter1.test(theMethod1) >> false
			1 * excludingFilter2.test(theMethod1) >> false

		and: "then next getter property from the map is requested for the method"
			1 * getter2.getGetter() >> theMethod2
		and: "inclusing filters are called again for the next method"
			1 * includingFilter1.test(theMethod2) >> false
			1 * includingFilter2.test(theMethod2) >> false
		and: "if no inclusing filter returned true - excluding methods are not called"
			0 * excludingFilter1.test(theMethod2)
			0 * excludingFilter2.test(theMethod2)

		and: "next getter method received"
			1 * getter3.getGetter() >> theMethod3
		and: "when including filter returns true - subsequent filters are ignored - method is sent for exclusion test"
			1 * includingFilter1.test(theMethod3) >> true
			0 * includingFilter2.test(theMethod3)
		and: "when excluding filter returns true - subsequent filter are ignored - method is excluded"
			1 * excludingFilter1.test(theMethod3) >> true
			0 * excludingFilter2.test(theMethod3)

		and: "static methods are ignored before any filters"
			0 * getterStatic.getGetter()

		and: "result contains specified name"
			result.getName() == "someName"
		and: "result is a composite performer with a single delegate performer"
			result instanceof CompositeComparisonPerformer
			result.getPerformers().size() == 1
		and: "delegate performer is an instance of the PropertyDelegatePerformer"
			def propertyPerformer = result.getPerformers()[key1]
			propertyPerformer instanceof PropertyDelegatePerformer
		and: "delegate contains property from the mextractor map, and default dynamic performer from the builder"
			propertyPerformer.getProperty().is(getter1)
			propertyPerformer.getDelegatePerformer().is(builder.getDynamicPerformer())

		and: "getter2 is not included, for not a single including filter was matched"
			result.getPerformers()[key2] == null

		and: "getter3 is not included, for an excluding filter has matched"
			result.getPerformers()[key3] == null
	}

	def "build with no filters"() {
		given: "builder with no filters registered"
			ClassPropertyExtractor<GetterClassProperty<?, ?>> mockExtractor = Mock()
			GettersDynamicPerformerBuilder<?> builder = new GettersDynamicPerformerBuilder(String, mockExtractor)
		and: "property keys"
			ClassPropertyKey key1 = Mock()
			ClassPropertyKey key2 = Mock()
			ClassPropertyKey key3 = Mock()
		and: "some methods"
			Method theMethod1 = Object.class.getDeclaredMethod("equals", Object)
			Method theMethod2 = Object.class.getDeclaredMethod("hashCode")
			Method theMethod3 = Object.class.getDeclaredMethod("clone")
		and: "some mock properties"
			GetterClassProperty<?, ?> getter1 = Mock()
			getter1.getKey() >> key1
			getter1.getGetter() >> theMethod1
			GetterClassProperty<?, ?> getter2 = Mock()
			getter2.getKey() >> key2
			getter2.getGetter() >> theMethod2
			GetterClassProperty<?, ?> getter3 = Mock()
			getter3.getKey() >> key3
			getter3.getGetter() >> theMethod3
			Map<ClassPropertyKey, GetterClassProperty<?, ?>> gettersMap = [
					(key1): getter1,
					(key2): getter2,
					(key3): getter3,
				]

		when: "build is called with some name specified"
			def result = builder.build("someName")
		then: "extractor specified in the constructor is called with the class specified in the constructor"
			1 * mockExtractor.extractAll(String) >> gettersMap

		and: "result contains specified name"
			result.getName() == "someName"
		and: "result is a composite performer with 3 delegate performers"
			result instanceof CompositeComparisonPerformer
			result.getPerformers().size() == 3

		and: "all getter properties provided by the extractor are present in the result performer"
			def propertyPerformer = result.getPerformers()[key1]
			propertyPerformer instanceof PropertyDelegatePerformer
			propertyPerformer.getProperty().is(getter1)
			propertyPerformer.getDelegatePerformer().is(builder.getDynamicPerformer())

		and:
			def propertyPerformer2 = result.getPerformers()[key2]
			propertyPerformer2 instanceof PropertyDelegatePerformer
			propertyPerformer2.getProperty().is(getter2)
			propertyPerformer2.getDelegatePerformer().is(builder.getDynamicPerformer())

		and:
			def propertyPerformer3 = result.getPerformers()[key3]
			propertyPerformer3 instanceof PropertyDelegatePerformer
			propertyPerformer3.getProperty().is(getter3)
			propertyPerformer3.getDelegatePerformer().is(builder.getDynamicPerformer())
	}

	def "performer is recursive"() {
		given:
			GettersDynamicPerformerBuilder<?> builder = new GettersDynamicPerformerBuilder(type)
		when:
			ComparisonPerformer<String> performer = builder.build("qwe")
		then:
			builder.getDynamicPerformer().getRegisteredDelegates().get(type).is(performer)
		where:
			type << [Object, Integer, String, BigDecimal]
	}

	def "performer recursion override"() {
		given:
			ComparisonPerformer<?> DEFAULT = ComparisonPerformers.DEEP_EQUALS
			GettersDynamicPerformerBuilder<?> builder = new GettersDynamicPerformerBuilder(type)
		when:
			builder.getDynamicPerformer().registerDelegate(type, DEFAULT)
			ComparisonPerformer<String> performer = builder.build("qwe")
		then:
			builder.getDynamicPerformer().getRegisteredDelegates().get(type).is(DEFAULT)
		where:
			type << [Object, Integer, String, BigDecimal]
	}

	def "second build is prohibited"() {
		given:
			GettersDynamicPerformerBuilder<?> builder = new GettersDynamicPerformerBuilder(String)

		when:
			builder.build("qwe")
		then:
			notThrown(IllegalStateException)

		when:
			builder.build("qwe")
		then:
			thrown(IllegalStateException)

		when:
			builder.build("qwe")
		then:
			thrown(IllegalStateException)
	}

	def "PatternPredicate"() {
		given:
			Pattern pattern = Pattern.compile("get.*")
			def methods = TestEntities.Person.class.getDeclaredMethods()

		when:
			new PatternPredicate((Pattern) null)
		then:
			thrown(NullPointerException)

		when:
			new PatternPredicate((String) null)
		then:
			thrown(NullPointerException)

		when:
			PatternPredicate pp1 = new PatternPredicate(pattern)
		then:
			pp1.getPattern().is(pattern)
		and:
			pp1 == new PatternPredicate(Pattern.compile("get.*"))

		when:
			PatternPredicate pp2 = new PatternPredicate("(get|is).*")
		then:
			pp2.getPattern().pattern() == "(get|is).*"
		and:
			pp2 == new PatternPredicate("(get|is).*")

		expect:
			methods.findAll { pp1.test(it) } == methods.findAll { pattern.matcher(it.getName()).matches() }
		and:
			methods.findAll { pp2.test(it) } == methods.findAll { it.getName().matches("(get|is).*") }
	}
}
