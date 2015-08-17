package org.whaka.util.reflection.comparison;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import org.whaka.util.UberObjects;

/**
 * <p>Instance of this class represents single result of performed comparison for two objects.
 * Result tracks 'actual' object, 'expected' object, instance of a {@link ComparisonPerformer} used to compare those
 * objects, and a single boolean flag as a result of comparison - objects either equal, or not.
 * 
 * <p>So short and uninformative result can be created for two peimitive objects, when there's nothing to compare
 * except for value itself. Or for two objects when they are not null-consistent. For more complex comparison
 * see {@link ComplexComparisonResult}
 */
public class ComparisonResult {

	private final Object actual;
	private final Object expected;
	private final ComparisonPerformer<?> comparisonPerformer;
	private final boolean success;
	
	/**
	 * @param actual - actual compared value
	 * @param expected - expected compared value
	 * @param comparisonPerformer - the performer that performed the comparison
	 * @param success - indicator whether values are equal
	 */
	public ComparisonResult(Object actual, Object expected,
			ComparisonPerformer<?> comparisonPerformer, boolean success) {
		
		this.actual = actual;
		this.expected = expected;
		this.comparisonPerformer = comparisonPerformer;
		this.success = success;
	}
	
	public Object getActual() {
		return actual;
	}
	
	public Object getExpected() {
		return expected;
	}
	
	public ComparisonPerformer<?> getComparisonPerformer() {
		return comparisonPerformer;
	}

	public boolean isSuccess() {
		return success;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ComparisonResult.class)
				.add("success", isSuccess())
				.add("actual", UberObjects.toString(getActual()))
				.add("expected", UberObjects.toString(getExpected()))
				.add("performer", getComparisonPerformer())
				.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getActual(), getExpected(), getComparisonPerformer(), isSuccess());
	}
	
	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			ComparisonResult that = (ComparisonResult) object;
			return getActual() == that.getActual()
					&& getExpected() == that.getExpected()
					&& Objects.equals(getComparisonPerformer(), that.getComparisonPerformer())
					&& Objects.equals(isSuccess(), that.isSuccess());
		}
		return false;
	}
}
