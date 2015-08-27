package org.whaka.util.reflection.comparison.performers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResultBuilder;
import org.whaka.util.reflection.properties.ClassPropertyKey;

/**
 * <p>Compares any kinds of collections as sets, using specified delegate to check if both collections
 * contains all the same elements regarding their order.
 * 
 * <p>If collections are of the same size, and failed result is produced - it will always contain
 * only one sub-result with actual value being null. This indicates that expected collection
 * contains an element that is not contained in the actual one. More specific result is impossible
 * with this kind of comparison.
 */
public class SetComparisonPerformer<T> extends ContainerComparisonPerformer<T, Collection<? extends T>> {

	private final static ClassPropertyKey key = new ClassPropertyKey("contains", Collection.class);
	
	public SetComparisonPerformer(ComparisonPerformer<? super T> elementPerformer) {
		super(elementPerformer);
	}
	
	@Override
	public ComparisonResult apply(Collection<? extends T> actual, Collection<? extends T> expected) {
		if (actual == expected)
			return new ComparisonResult(actual, expected, this, true);
		if (actual == null || expected == null)
			return new ComparisonResult(actual, expected, this, false);
		if (actual.size() != expected.size())
			return createSizeCheckResult(actual, expected);
		return performElementComparison(actual, expected);
	}
	
	private ComparisonResult createSizeCheckResult(Collection<? extends T> actual, Collection<? extends T> expected) {
		return new ComplexComparisonResultBuilder<Collection<? extends T>>(Collection.class)
				.compare("size", actual.size(), expected.size())
				.build(actual, expected, this);
	}
	
	private ComparisonResult performElementComparison(Collection<? extends T> actual, Collection<? extends T> expected) {
		List<T> actualCopy = new ArrayList<>(actual);
		for (T expectedElement : expected) {
			int matchedIndex = findMatchingIndex(expectedElement, actualCopy);
			if (matchedIndex < 0)
				return createMissingElementResult(actual, expected, null, expectedElement);
			actualCopy.remove(matchedIndex);
		}
		return new ComplexComparisonResult(actual, expected, this, Collections.emptyMap());
	}
	
	private int findMatchingIndex(T expectedElement, List<T> list) {
		for (int i = 0; i < list.size(); i++)
			if (getElementPerformer().apply(list.get(i), expectedElement).isSuccess())
				return i;
		return -1;
	}
	
	private ComparisonResult createMissingElementResult(Collection<? extends T> actual, Collection<? extends T> expected,
			Object actualElement, Object expectedElement) {
		ComparisonResult result = new ComparisonResult(actualElement, expectedElement, getElementPerformer(), false);
		Map<ClassPropertyKey, ComparisonResult> results = new LinkedHashMap<>();
		results.put(key, result);
		return new ComplexComparisonResult(actual, expected, this, results);
	}
}
