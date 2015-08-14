package com.whaka.util.reflection.comparison.performers;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.whaka.util.reflection.comparison.ComparisonFail;
import com.whaka.util.reflection.comparison.ComparisonPerformer;
import com.whaka.util.reflection.comparison.ComparisonResult;
import com.whaka.util.reflection.properties.ClassProperty;

/**
 * When executed performer extracts values of the specified property from both actual and expected received objects,
 * and then executes specified delegate performer on extracted values. If value extraction has thrown an exception
 * {@link ComparisonFail} is returned.
 */
class PropertyDelegatePerformer<V, T> implements ComparisonPerformer<T> {

	private final ClassProperty<V, T> property;
	private final ComparisonPerformer<? super V> delegatePerformer;
	
	public PropertyDelegatePerformer(ClassProperty<V,T> property, ComparisonPerformer<? super V> delegatePerformer) {
		this.property = Objects.requireNonNull(property, "Property cannot be null!");
		this.delegatePerformer = Objects.requireNonNull(delegatePerformer, "Delegate performer cannot be null!");
	}
	
	public ClassProperty<V, T> getProperty() {
		return property;
	}
	
	public ComparisonPerformer<? super V> getDelegatePerformer() {
		return delegatePerformer;
	}
	
	@Override
	public ComparisonResult compare(T actual, T expected) {
		V actualValue, expectedValue;
		try {
			actualValue = getProperty().getValue(actual);
			expectedValue = getProperty().getValue(expected);
		} catch (Throwable e) {
			return new ComparisonFail(actual, expected, this, e);
		}
		return getDelegatePerformer().compare(actualValue, expectedValue);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("property", getProperty())
				.add("delegate", getDelegatePerformer())
				.toString();
	}
}