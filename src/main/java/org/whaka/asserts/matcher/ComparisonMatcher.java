package org.whaka.asserts.matcher;

import java.util.Objects;
import java.util.Optional;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.whaka.asserts.ComparisonAssertResult;
import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonPerformers;
import org.whaka.util.reflection.comparison.ComparisonResult;

/**
 * <p>{@link Matcher} implementation that allows to perform object comparison using {@link ComparisonPerformer}
 * as delegate.
 * 
 * <p><b>Note:</b> matcher able to provide specific {@link ComparisonAssertResult} containing more
 * information about performed comparison.
 * 
 * @see ComparisonPerformers
 */
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
	public Optional<ComparisonAssertResult> matches(T item, String message, Throwable cause) {
		ComparisonResult comparisonResult = getComparisonPerformer().apply(item, getValue());
		if (comparisonResult.isSuccess())
			return Optional.empty();
		ComparisonAssertResult result = ComparisonAssertResult.createWithCause(comparisonResult, message);
		if (result.getCause() == null)
			result.setCause(cause);
		return Optional.of(result);
	}
	
	@Override
	public void describeTo(Description description) {
		description.appendText("equal to ").appendValue(getValue())
			.appendText(" according to " + getComparisonPerformer());
	}
}
