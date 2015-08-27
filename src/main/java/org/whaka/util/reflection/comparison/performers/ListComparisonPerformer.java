package org.whaka.util.reflection.comparison.performers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResultBuilder;
import org.whaka.util.reflection.properties.ClassPropertyKey;

/**
 * Compares elements of lists with corresponding indexes using specified delegate.
 * If lists have different size - early result is returned, and delegate isn't used.
 */
public class ListComparisonPerformer<T> extends ContainerComparisonPerformer<T, List<? extends T>> {
	
	public ListComparisonPerformer(ComparisonPerformer<? super T> elementPerformer) {
		super(elementPerformer);
	}
	
	@Override
	public ComparisonResult appl(List<? extends T> actual, List<? extends T> expected) {
		if (actual == expected)
			return new ComparisonResult(actual, expected, this, true);
		if (actual == null || expected == null)
			return new ComparisonResult(actual, expected, this, false);
		if (actual.size() != expected.size())
			return createSizeCheckResult(actual, expected);
		return performElementComparison(actual, expected);
	}
	
	private ComparisonResult createSizeCheckResult(List<? extends T> actual, List<? extends T> expected) {
		return new ComplexComparisonResultBuilder<List<? extends T>>(List.class)
				.compare("size", actual.size(), expected.size())
				.build(actual, expected, this);
	}
	
	private ComparisonResult performElementComparison(List<? extends T> actual, List<? extends T> expected) {
		Map<ClassPropertyKey, ComparisonResult> results = new LinkedHashMap<>();
		for (int i = 0; i < actual.size(); i++) {
			T actualElement = actual.get(i);
			T expectedElement = expected.get(i);
			ComparisonResult result = getElementPerformer().appl(actualElement, expectedElement);
			results.put(createKey(i), result);
		}
		return new ComplexComparisonResult(actual, expected, this, results);
	}
	
	private static ClassPropertyKey createKey(int index) {
		return new ClassPropertyKey(index, List.class);
	}
}