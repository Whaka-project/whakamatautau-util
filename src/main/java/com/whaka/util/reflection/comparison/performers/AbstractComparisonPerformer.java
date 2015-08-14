package com.whaka.util.reflection.comparison.performers;

import com.google.common.base.MoreObjects;
import com.whaka.util.reflection.comparison.ComparisonPerformer;

/**
 * <p>Can be used to create 'named' performer.
 * <p>Provides {@link #getName()} method.
 * <p>{@link #toString()} creates human readable string using the name.
 */
public abstract class AbstractComparisonPerformer<T> implements ComparisonPerformer<T> {

	private final String name;
	
	public AbstractComparisonPerformer(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		String name = getName();
		if (name == null || name.trim().isEmpty())
			return super.toString();
		return MoreObjects.toStringHelper(ComparisonPerformer.class)
				.addValue(name)
				.toString();
	}
}
