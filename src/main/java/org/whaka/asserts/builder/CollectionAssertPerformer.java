package org.whaka.asserts.builder;

import static java.util.Collections.*;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.whaka.asserts.AssertResult;
import org.whaka.asserts.ComparisonAssertResult;
import org.whaka.util.UberCollections;
import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonPerformers;
import org.whaka.util.reflection.comparison.ComparisonResult;
import org.whaka.util.reflection.comparison.performers.SetComparisonPerformer;

public class CollectionAssertPerformer<T> extends AssertPerformer<Collection<? extends T>> {

	public static final String MESSAGE_NOT_EMPTY_COLLECTION_EXPECTED = "Not empty collection was expected!";
	public static final String MESSAGE_EMPTY_COLLECTION_EXPECTED = "Empty collection was expected!";
	public static final String MESSAGE_EMPTY_OR_NULL_COLLECTION_EXPECTED = "Empty collection or null was expected!";
	public static final String MESSAGE_ILLEGAL_COLLECTION_SIZE = "Illegal collection size!";
	public static final String MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES = "Collection doesn't contains expected values!";

	/**
	 * Comparison performer created from {@link UberCollections#DEEP_EQUALS_PREDICATE} predicate.
	 * Used as default performer for the {@link #containsSameElements(Collection, ComparisonPerformer)} method.
	 */
	public static final ComparisonPerformer<Object> DEEP_COLLECTION_PERFORMER =
			ComparisonPerformers.fromPredicate(UberCollections.deepEqualsPredicate());
	
	public CollectionAssertPerformer(Collection<? extends T> actual, Consumer<AssertResult> consumer) {
		super(actual, consumer);
	}

	public AssertResultConstructor isNotEmpty() {
		return performCheck((a,e) -> a != null && !a.isEmpty(), "Not []").withMessage(MESSAGE_NOT_EMPTY_COLLECTION_EXPECTED);
	}
	
	public AssertResultConstructor isEmpty() {
		return performCheck((a,e) -> a != null && a.isEmpty(), emptyList()).withMessage(MESSAGE_EMPTY_COLLECTION_EXPECTED);
	}
	
	public AssertResultConstructor isEmptyOrNull() {
		return performCheck((a,e) -> a == null || a.isEmpty(), emptyList()).withMessage(MESSAGE_EMPTY_OR_NULL_COLLECTION_EXPECTED);
	}
	
	public AssertResultConstructor isSize(int size) {
		return performCheck((a,e) -> a != null && a.size() == size, "Size "+size).withMessage(MESSAGE_ILLEGAL_COLLECTION_SIZE);
	}
	
	public AssertResultConstructor contains(T expected) {
		return contains(expected, UberCollections.deepEqualsPredicate());
	}
	
	public AssertResultConstructor contains(T expected, BiPredicate<T, T> predicate) {
		return containsAll(Collections.singleton(expected), predicate);
	}
	
	public AssertResultConstructor containsAny(Collection<? extends T> anyOf) {
		return containsAny(anyOf, UberCollections.deepEqualsPredicate());
	}
	
	public AssertResultConstructor containsAny(Collection<? extends T> anyOf, BiPredicate<T, T> predicate) {
		String formattedExpected = "Any of " + UberCollections.toString(anyOf);
		return performCheck((a,e) -> a != null && UberCollections.containsAny(a, anyOf, predicate),
			formattedExpected).withMessage(MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES);
	}
	
	public AssertResultConstructor containsAll(Collection<? extends T> allOf) {
		return containsAll(allOf, UberCollections.deepEqualsPredicate());
	}
	
	public AssertResultConstructor containsAll(Collection<? extends T> allOf, BiPredicate<T, T> predicate) {
		String formattedExpected = "All of " + UberCollections.toString(allOf);
		return performCheck((a,e) -> a != null && UberCollections.containsAll(a, allOf, predicate),
			formattedExpected).withMessage(MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES);
	}
	
	public AssertResultConstructor containsSameElements(Collection<? extends T> equalCollection) {
		return containsSameElements(equalCollection, DEEP_COLLECTION_PERFORMER);
	}
	
	public AssertResultConstructor containsSameElements(Collection<? extends T> equalCollection,
			ComparisonPerformer<? super T> comparisonPerformer) {
		SetComparisonPerformer<T> setPerformer = ComparisonPerformers.set(comparisonPerformer);
		ComparisonResult comparisonResult = setPerformer.compare(getActual(), equalCollection);
		AssertResult result = null;
		if (!comparisonResult.isSuccess())
			result = performResult(ComparisonAssertResult.createWithCause(comparisonResult));
		return AssertResultConstructor.create(result).withMessage(MESSAGE_COLLECTION_NOT_CONTAINS_EXPECTED_VALUES);
	}
}
