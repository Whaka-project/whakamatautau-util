package org.whaka.asserts;

import java.util.Collection;

import org.whaka.asserts.builder.AssertBuilder;
import org.whaka.asserts.builder.CollectionAssertPerformer;
import org.whaka.util.reflection.comparison.ComparisonPerformer;

public class CollectionAssert<T> extends AbstractInstantAssert<CollectionAssertPerformer<T>, Collection<? extends T>> {

	public CollectionAssert(Collection<? extends T> actual) {
		super(actual);
	}
	
	@Override
	protected CollectionAssertPerformer<T> createPerformer(AssertBuilder builder, Collection<? extends T> actual) {
		return builder.checkCollection(actual);
	}
	
	public void isNotEmpty() {
		isNotEmpty(null);
	}
	
	public void isNotEmpty(String message) {
		performInstantAssert(CollectionAssertPerformer::isNotEmpty, message);
	}
	
	public void isEmpty() {
		isEmpty(null);
	}
	
	public void isEmpty(String message) {
		performInstantAssert(CollectionAssertPerformer::isEmpty, message);
	}
	
	public void isEmptyOrNull() {
		isEmptyOrNull(null);
	}
	
	public void isEmptyOrNull(String message) {
		performInstantAssert(CollectionAssertPerformer::isEmptyOrNull, message);
	}
	
	public void isSize(int size) {
		isSize(size, null);
	}
	
	public void isSize(int size, String message) {
		performInstantAssert(collectionPerformer -> collectionPerformer.isSize(size), message);
	}
	
	public void contains(T expected) {
		contains(expected, null);
	}
	
	public void contains(T expected, String message) {
		performInstantAssert(collectionPerformer -> collectionPerformer.contains(expected), message);
	}
	
	public void containsAny(Collection<? extends T> anyOf) {
		containsAny(anyOf, null);
	}
	
	public void containsAny(Collection<? extends T> anyOf, String message) {
		performInstantAssert(collectionPerformer -> collectionPerformer.containsAny(anyOf), message);
	}
	
	public void containsAll(Collection<? extends T> allOf) {
		containsAll(allOf, null);
	}
	
	public void containsAll(Collection<? extends T> allOf, String message) {
		performInstantAssert(collectionPerformer -> collectionPerformer.containsAll(allOf), message);
	}
	
	public void containsSameElements(Collection<? extends T> equalCollection) {
		containsSameElements(equalCollection, (String) null);
	}
	
	public void containsSameElements(Collection<? extends T> equalCollection, String message) {
		performInstantAssert(colPerformer -> colPerformer.containsSameElements(equalCollection), message);
	}
	
	public void containsSameElements(Collection<? extends T> equalCollection, ComparisonPerformer<? super T> performer) {
		containsSameElements(equalCollection, performer, null);
	}
	
	public void containsSameElements(Collection<? extends T> equalCollection,
			ComparisonPerformer<? super T> performer, String message) {
		performInstantAssert(colPerformer -> colPerformer.containsSameElements(equalCollection, performer), message);
	}
}
