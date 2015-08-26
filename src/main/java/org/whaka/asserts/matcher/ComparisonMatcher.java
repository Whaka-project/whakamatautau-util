package org.whaka.asserts.matcher;

import java.util.Objects;

import org.hamcrest.Description;
import org.whaka.asserts.AssertResult;
import org.whaka.asserts.ComparisonAssertResult;
import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonResult;

public class ComparisonMatcher<T> extends ResultProvidingMatcher<T> {

	private final T value;
	private final ComparisonPerformer<? super T> comparisonPerformer;
	
	public ComparisonMatcher(T value, ComparisonPerformer<? super T> comparisonPerformer) {
		this.value = value;
		this.comparisonPerformer = Objects.requireNonNull(comparisonPerformer, "Comparison performer cannot be null!");
	}

	public T getValue() {
		return value;
	}

	public ComparisonPerformer<? super T> getComparisonPerformer() {
		return comparisonPerformer;
	}

	@Override
	public AssertResult matches(T item, String message, Throwable cause) {
		ComparisonResult comparisonResult = getComparisonPerformer().compare(item, getValue());
		if (comparisonResult.isSuccess())
			return null;
		AssertResult result = ComparisonAssertResult.createWithCause(comparisonResult);
		if (result.getCause() == null)
			result.setCause(cause);
		return result;
	}
	
	@Override
	public void describeTo(Description description) {
		description.appendText("equal to ").appendValue(getValue())
			.appendText(" according to " + getComparisonPerformer());
	}
}
