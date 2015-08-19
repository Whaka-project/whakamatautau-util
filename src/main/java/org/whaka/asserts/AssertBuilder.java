package org.whaka.asserts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

/**
 * Class provides ability to construct (or perform) assert error with multiple assert results.
 * <p><b>Note:</b> class implements {@link Consumer}, so methods {@link #accept(AssertResult)}
 * and {@link #addResult(AssertResult)} are synonyms, though latter returns the builder itself.
 *
 * <p>It is also possible to create instance of the AssertError without throwing it, see {@link #build()}
 * method. Or to receive list of collected asserts with {@link #getAssertResults()} method.
 *
 * @see #checkThrowable(Throwable)
 */
public class AssertBuilder implements Consumer<AssertResult> {

	private final List<AssertResult> assertResults = new ArrayList<>();

	@Override
	public void accept(AssertResult result) {
		addResult(result);
	}

	/**
	 * The same instance of the list is used throughout the building process.
	 * So any operations with returned list will affect actual state of the builder.
	 */
	public final List<AssertResult> getAssertResults() {
		return assertResults;
	}
	
	/**
	 * New instance of the AssertError is returned if any assert results are present.
	 * Otherwise null is returned.
	 */
	public AssertError build() {
		if (assertResults.size() > 0)
			return new AssertError(assertResults);
		return null;
	}
	
	/**
	 * Method {@link #build()} is called. If it returns instance of the AssertError - it got thrown.
	 */
	public void performAssert() throws AssertError {
		AssertError error = build();
		if (error != null)
			throw error;
	}

	public AssertBuilder addResult(AssertResult result) {
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
	 */
	public <T> AssertBuilder checkThat(T item, Matcher<T> matcher) {
		return checkThat(item, matcher, null);
	}
	
	/**
	 * <p>Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 * If matcher doesn't matches specified item - assert result is created with the specified message
	 * and added to the builder.
	 */
	public <T> AssertBuilder checkThat(T item, Matcher<T> matcher, String message) {
		return checkThat(item, matcher, message, null);
	}
	
	/**
	 * <p>Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 * If matcher doesn't matches specified item - assert result is created with the specified message,
	 * and specified cause and added to the builder.
	 */
	public <T> AssertBuilder checkThat(T item, Matcher<T> matcher, String message, Throwable cause) {
		if (!matcher.matches(item)) {
			StringDescription expected = new StringDescription();
			matcher.describeTo(expected);
			if (item instanceof Throwable && cause == null)
				cause = (Throwable) item;
			AssertResult result = new AssertResult(item, expected.toString(), message, cause);
			accept(result);
		}
		return this;
	}
}
