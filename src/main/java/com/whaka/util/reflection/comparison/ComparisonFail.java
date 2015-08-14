package com.whaka.util.reflection.comparison;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.whaka.util.UberObjects;

/**
 * Class represents <b>always-failed</b> result of comparison. May contain throwable cause.
 */
public class ComparisonFail extends ComparisonResult {

	private final Throwable cause;
	
	public ComparisonFail(Object actual, Object expected, ComparisonPerformer<?> comparisonPerformer, Throwable cause) {
		super(actual, expected, comparisonPerformer, false);
		this.cause = cause;
	}
	
	public Throwable getCause() {
		return cause;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("cause", getCause())
				.add("actual", UberObjects.toString(getActual()))
				.add("expected", UberObjects.toString(getExpected()))
				.add("performer", getComparisonPerformer())
				.toString();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getActual(), getExpected(), getComparisonPerformer(), getCause());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			ComparisonFail that = (ComparisonFail) object;
			return getActual() == that.getActual()
				&& getExpected() == that.getExpected()
				&& Objects.equals(getComparisonPerformer(), that.getComparisonPerformer())
				&& Objects.equals(getCause(), that.getCause());
		}
		return false;
	}
}
