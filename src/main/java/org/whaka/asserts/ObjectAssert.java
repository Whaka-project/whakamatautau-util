package org.whaka.asserts;

import java.util.Collection;

import org.whaka.asserts.builder.AssertBuilder;
import org.whaka.asserts.builder.ObjectAssertPerformer;
import org.whaka.util.reflection.comparison.ComparisonPerformer;


public class ObjectAssert<T> extends AbstractInstantAssert<ObjectAssertPerformer<T>, T> {

	public ObjectAssert(T actual) {
		super(actual);
	}
	
	@Override
	protected ObjectAssertPerformer<T> createPerformer(AssertBuilder builder, T actual) {
		return builder.checkObject(actual);
	}
	
	public void isNullConsistent(Object expected) {
		isNullConsistent(expected, null);
	}
	
	public void isNullConsistent(Object expected, String message) {
		performInstantAssert(objectPerformer -> objectPerformer.isNullConsistent(expected), message);
	}
	
	public void isEqual(Object expected) {
		isEqual(expected, null);
	}
	
	public void isEqual(Object expected, String message) {
		performInstantAssert(objectPerformer -> objectPerformer.isEqual(expected), message);
	}
	
	public void isEqual(T expected, ComparisonPerformer<? super T> comparisonPerformer) {
		isEqual(expected, comparisonPerformer, null);
	}
	
	public void isEqual(T expected, ComparisonPerformer<? super T> comparisonPerformer, String message) {
		performInstantAssert(objectPerformer -> objectPerformer.isEqual(expected, comparisonPerformer), message);
	}
	
	public void isNotEqual(Object expected) {
		isNotEqual(expected, null);
	}
	
	public void isNotEqual(Object expected, String message) {
		performInstantAssert(objectPerformer -> objectPerformer.isNotEqual(expected), message);
	}
	
	public void isNull() {
		isNull(null);
	}
	
	public void isNull(String message) {
		performInstantAssert(ObjectAssertPerformer::isNull, message);
	}
	
	public void isNotNull() {
		isNotNull(null);
	}
	
	public void isNotNull(String message) {
		performInstantAssert(ObjectAssertPerformer::isNotNull, message);
	}
	
	public void isIn(Collection<? extends T> col) {
		isIn(col, null);
	}
	
	public void isIn(Collection<? extends T> col, String message) {
		performInstantAssert(objectPerformer -> objectPerformer.isIn(col), message);
	}
}
