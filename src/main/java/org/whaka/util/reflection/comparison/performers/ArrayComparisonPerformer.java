package org.whaka.util.reflection.comparison.performers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResultBuilder;
import org.whaka.util.reflection.properties.ClassPropertyKey;

/**
 * Compares elements of the list with corresponding indexes using specified delegate.
 * If arrays have different length - early result is returned and delegate is not used.
 */
public class ArrayComparisonPerformer<T> extends ContainerComparisonPerformer<T, T[]> {
	
	public ArrayComparisonPerformer(ComparisonPerformer<? super T> elementPerformer) {
		super(elementPerformer);
	}
	
	@Override
	public ComparisonResult compare(T[] actual, T[] expected) {
		if (actual == expected)
			return new ComparisonResult(actual, expected, this, true);
		if (actual == null || expected == null)
			return new ComparisonResult(actual, expected, this, false);
		if (actual.length != expected.length)
			return createLengthCheckResult(actual, expected);
		return performElementsComparison(actual, expected);
	}
	
	private ComparisonResult createLengthCheckResult(T[] actual, T[] expected) {
		return new ComplexComparisonResultBuilder<T[]>(Object[].class)
				.compare("length", actual.length, expected.length)
				.build(actual, expected, this);
	}
	
	private ComparisonResult performElementsComparison(T[] actual, T[] expected) {
		Map<ClassPropertyKey, ComparisonResult> results = new LinkedHashMap<>();
		for (int i = 0; i < actual.length; i++) {
			T actualElement = actual[i];
			T expectedElement = expected[i];
			ComparisonResult result = getElementPerformer().compare(actualElement, expectedElement);
			results.put(createKey(i), result);
		}
		return new ComplexComparisonResult(actual, expected, this, results);
	}
	
	private static ClassPropertyKey createKey(int index) {
		return new ClassPropertyKey(index, Object[].class);
	}
}