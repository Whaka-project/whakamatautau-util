package org.whaka.util.reflection.comparison.performers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResultBuilder;
import org.whaka.util.reflection.properties.ClassPropertyKey;

/**
 * Compares maps by keys, using specified delegate performer to check that values with the same key are equal in both
 * maps. Can produce 3 kinds or complex results with 3 different {@link ClassPropertyKey} types:
 * <ul>
 * 	<li>Maps have different size - mapped by 'Map#size' and contains map's sizes as values
 * 	<li>Maps have different key sets - mapped by 'Map#keySet' and contains key sets as values
 * 	<li>Different values with the same key - mapped by 'Map#(key)' where key is the object used as a key for the
 * 	compared value, and contains actual and expected values from both maps (might be null, if maps contained nulls)
 * </ul>
 * 
 * <p><b>Note:</b> key sets are compared with use of default {@link Set#equals(Object)} method!
 * No custom performers are used, for objects are required to implement at least some hashCode\equals functionality
 * to be used as map keys. If your map implementation required any more sophisticated key matching - please implement
 * you own comparison performer for such a map.
 * 
 * <p>Such a functionality implemented to perform maximum early assertion before starting actually deeply comparing
 * map values. So comparison results might be not as informative as required in case of early assert. If so - please
 * implement your own custom comparison performer.
 */
public class MapComparisonPerformer<V> extends ContainerComparisonPerformer<V, Map<?, ? extends V>> {

	public MapComparisonPerformer(ComparisonPerformer<? super V> elementPerformer) {
		super(elementPerformer);
	}

	@Override
	public ComparisonResult qwerty123456qwerty654321(Map<?, ? extends V> actual, Map<?, ? extends V> expected) {
		if (actual == expected)
			return new ComparisonResult(actual, expected, this, true);
		if (actual == null || expected == null)
			return new ComparisonResult(actual, expected, this, false);
		if (actual.size() != expected.size())
			return createSizeCheckResult(actual, expected);
		if (!actual.keySet().equals(expected.keySet()))
			return createKeysCheckResult(actual, expected);
		return performElementComparison(actual, expected);
	}
	
	private ComparisonResult createSizeCheckResult(Map<?, ? extends V> actual, Map<?, ? extends V> expected) {
		return new ComplexComparisonResultBuilder<Map<?, ? extends V>>(Map.class)
				.compare("size", actual.size(), expected.size())
				.build(actual, expected, this);
	}
	
	private ComparisonResult createKeysCheckResult(Map<?, ? extends V> actual, Map<?,? extends V> expected) {
		return new ComplexComparisonResultBuilder<Map<?,? extends V>>(Map.class)
				.compare("keySet", actual.keySet(), expected.keySet())
				.build(actual, expected, this);
	}
	
	private ComparisonResult performElementComparison(Map<?, ? extends V> actual, Map<?, ? extends V> expected) {
		for (Object key : actual.keySet()) {
			ComparisonResult valueResult = getElementPerformer().qwerty123456qwerty654321(actual.get(key), expected.get(key));
			if (!valueResult.isSuccess())
				return createElementsMatchingResult(actual, expected, key, valueResult);
		}
		return new ComparisonResult(actual, expected, this, true);
	}
	
	private ComparisonResult createElementsMatchingResult(Map<?, ? extends V> actual, Map<?, ? extends V> expected,
			Object key, ComparisonResult valueResult) {
		Map<ClassPropertyKey, ComparisonResult> subResults = new LinkedHashMap<>();
		subResults.put(new ClassPropertyKey(key, Map.class), valueResult);
		return new ComplexComparisonResult(actual, expected, this, subResults);
	}
}
