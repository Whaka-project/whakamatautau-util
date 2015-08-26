package org.whaka.asserts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.StringDescription;
import org.whaka.asserts.matcher.ResultProvidingMatcher;

/**
 * <p>Class provides ability to construct (or throw) an assert error with multiple assert results.
 * "Hamcrest Matchers" are used as main assertion tool.
 *
 * <p>It is also possible to create instance of the AssertError without throwing it, see {@link #build()}
 * method. Or to receive list of collected asserts with {@link #getAssertResults()} method.
 *
 * @see Matcher
 * @see Matchers
 * @see #checkThat(Object, Matcher)
 * @see #checkThat(Object, Matcher, String)
 * @see #checkThat(Object, Matcher, String, Throwable)
 */
public class AssertBuilder {

	private final List<AssertResult> assertResults = new ArrayList<>();

	/**
	 * The same instance of the list is used throughout the building process.
	 * So any operations with returned list will affect actual state of the builder.
	 */
	public final List<AssertResult> getAssertResults() {
		return assertResults;
	}
	
	/**
	 * If this builder contains any assert results (see {@link #getAssertResults()}) then an {@link Optional}
	 * containing new instance of an AssertError will be returned. Otherwise an empty optional is returned.
	 */
	public Optional<AssertError> build() {
		return assertResults.size() > 0 ? Optional.of(new AssertError(assertResults)) : Optional.empty();
	}
	
	/**
	 * Method {@link #build()} is called. If it returns instance of the AssertError - it got thrown.
	 */
	public void performAssert() throws AssertError {
		Optional<AssertError> error = build();
		if (error.isPresent())
			throw error.get();
	}

	public AssertBuilder addResult(AssertResult result) {
		Objects.requireNonNull(result, "Assert result cannot be null!");
		assertResults.add(result);
		return this;
	}
	
	/**
	 * {@link String#format(String, Object...)} is called for the specified message and arguments.
	 * AssertResult is created with the result message and added to the builder.
	 */
	public AssertBuilder addMessage(String message, Object ... args) {
		Objects.requireNonNull(message, "Message cannot be null!");
		if (args.length > 0)
			message = String.format(message, args);
		addResult(new AssertResult(message));
		return this;
	}
	
	/**
	 * <p>Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 * If matcher doesn't matches specified item - assert result is created and added to the builder.
	 * 
	 * @see #checkThat(Object, Matcher, String)
	 * @see #checkThat(Object, Matcher, String, Throwable)
	 */
	public <T> AssertBuilder checkThat(T item, Matcher<T> matcher) {
		return checkThat(item, matcher, null);
	}
	
	/**
	 * <p>Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 * If matcher doesn't matches specified item - assert result is created with the specified message
	 * and added to the builder.
	 * 
	 * @see #checkThat(Object, Matcher)
	 * @see #checkThat(Object, Matcher, String, Throwable)
	 */
	public <T> AssertBuilder checkThat(T item, Matcher<T> matcher, String message) {
		return checkThat(item, matcher, message, null);
	}
	
	/**
	 * <p>Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 * If matcher doesn't matches specified item - assert result is created with the specified message,
	 * and specified cause and added to the builder.
	 * 
	 * @see #checkThat(Object, Matcher)
	 * @see #checkThat(Object, Matcher, String)
	 */
	public <T> AssertBuilder checkThat(T item, Matcher<T> matcher, String message, Throwable cause) {
		AssertResult result = cresteResult(item, matcher, message, cause);
		if (result != null)
			addResult(result);
		return this;
	}
	
	private static <T> AssertResult cresteResult(T item, Matcher<T> matcher, String message, Throwable cause) {
		if (matcher instanceof ResultProvidingMatcher)
			return ((ResultProvidingMatcher<T>)matcher).matches(item, message, cause);
		if (!matcher.matches(item))
			return createDefaultResult(item, matcher, message, cause);
		return null;
	}
	
	private static <T> AssertResult createDefaultResult(T item, Matcher<T> matcher, String message, Throwable cause) {
		StringDescription expected = new StringDescription();
		matcher.describeTo(expected);
		return new AssertResult(item, expected.toString(), message, cause);
	}
}
