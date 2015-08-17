package org.whaka.util.reflection.comparison.performers;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import org.whaka.util.reflection.comparison.ComparisonPerformer;

abstract class ContainerComparisonPerformer<T, Container> implements ComparisonPerformer<Container> {
	
	private final ComparisonPerformer<? super T> elementPerformer;

	public ContainerComparisonPerformer(ComparisonPerformer<? super T> elementPerformer) {
		this.elementPerformer = Objects.requireNonNull(elementPerformer,
				"Element comparison performer cannot be null!");
	}
	
	public ComparisonPerformer<? super T> getElementPerformer() {
		return elementPerformer;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("elements", getElementPerformer())
			.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getElementPerformer());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			ContainerComparisonPerformer<?,?> that = (ContainerComparisonPerformer<?,?>) object;
			return Objects.equals(getElementPerformer(), that.getElementPerformer());
		}
		return false;
	}
	
	
}