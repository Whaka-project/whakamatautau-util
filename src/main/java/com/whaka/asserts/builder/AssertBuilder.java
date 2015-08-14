package com.whaka.asserts.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import com.whaka.asserts.AssertError;
import com.whaka.asserts.AssertResult;
import com.whaka.util.reflection.comparison.ComparisonPerformer;

/**
 * Class provides ability to construct (or perform) assert error with multiple assert results.
 * <p><b>Note:</b> class implements {@link Consumer}, so methods {@link #accept(AssertResult)}
 * and {@link #addResult(AssertResult)} are synonyms, though latter returns the builder itself.
 *
 * <p>It is also possible to create instance of the AssertError without throwing it, see {@link #build()}
 * method. Or to receive list of collected asserts with {@link #getAssertResults()} method.
 *
 * @see #checkObject(Object)
 * @see #checkBoolean(Boolean)
 * @see #checkNumber(Number)
 * @see #checkCollection(Collection)
 * @see #checkThrowable(Throwable)
 */
public class AssertBuilder implements Consumer<AssertResult> {

	private final List<AssertResult> assertResults = new ArrayList<>();

	@Override
	public void accept(AssertResult result) {
		addResult(result);
	}

	/**
	 * The same instance of the list is used thruout the building process.
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
	 * AssertResult is created directly with the specified message. No additional formatting is performed.
	 *
	 * @see #addMessage(String, Object[])
	 */
	public AssertBuilder addMessage(String message) {
		return performMessageConstruction(messageConstructor -> messageConstructor.withMessage(message));
	}
	
	/**
	 * If specified message is not null - {@link String#format(String, Object[])} is performed using the message
	 * and specified additional arguments. Then AssertResult is created with the result message.
	 *
	 * @see #addMessage(String)
	 */
	public AssertBuilder addMessage(String message, Object... args) {
		return performMessageConstruction(messageConstructor -> messageConstructor.withMessage(message, args));
	}
	
	private AssertBuilder performMessageConstruction(Consumer<AssertResultConstructor> messagePerformer) {
		AssertResult result = new AssertResult();
		messagePerformer.accept(AssertResultConstructor.create(result));
		addResult(result);
		return this;
	}
	
	public AssertBuilder perform(Consumer<AssertBuilder> consumer) {
		consumer.accept(this);
		return this;
	}
	
	public <T> ObjectAssertPerformer<T> checkObject(T actual) {
		return new ObjectAssertPerformer<>(actual, this);
	}
	
	public BooleanAssertPerformer checkBoolean(Boolean actual) {
		return new BooleanAssertPerformer(actual, this);
	}

	public NumberAssertPerformer checkNumber(Number actual) {
		return new NumberAssertPerformer(actual, this);
	}
	
	public StringAssertPerformer checkString(String actual) {
		return new StringAssertPerformer(actual, this);
	}
	
	public ThrowableAssertPerformer checkThrowable(Throwable actual) {
		return new ThrowableAssertPerformer(actual, this);
	}
	
	public <T> CollectionAssertPerformer<T> checkCollection(Collection<? extends T> collection) {
		return new CollectionAssertPerformer<>(collection, this);
	}

	/**
	 * Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 * Result added to the builder.
	 */
	public <T> AssertResultConstructor checkThat(T item, Matcher<T> matcher) {
		AssertResult result = null;
		if (!matcher.matches(item)) {
			StringDescription expected = new StringDescription();
			matcher.describeTo(expected);
			result = new AssertResult(item, expected.toString(), null);
			accept(result);
		}
		return AssertResultConstructor.create(result);
	}
	
	/**
	 * Equivalent of: <pre>#checkObject(actual).isEqual(expected);</Pre>
	 */
	public AssertResultConstructor equals(Object actual, Object expected) {
		return checkObject(actual).isEqual(expected);
	}
	
	/**
	 * Equivalent of: <pre>#checkObject(actual).isNotEqual(expected);</Pre>
	 */
	public AssertResultConstructor notEqual(Object actual, Object expected) {
		return checkObject(actual).isNotEqual(expected);
	}
	
	/**
	 * Equivalent of: <pre>#checkObject(actual).isEqual(expected, performer);</Pre>
	 */
	public <T> AssertResultConstructor equals(T actual, T expected, ComparisonPerformer<T> performer) {
		return checkObject(actual).isEqual(expected, performer);
	}
	
	/**
	 * Equivalent of: <pre>#checkObject(actual).isNull();</Pre>
	 */
	public AssertResultConstructor isNull(Object actual) {
		return checkObject(actual).isNull();
	}
	
	/**
	 * Equivalent of: <pre>#checkObject(actual).isNotNull();</Pre>
	 */
	public AssertResultConstructor isNotNull(Object actual) {
		return checkObject(actual).isNotNull();
	}
	
	/**
	 * Equivalent of: <pre>#checkBoolean(b).isTrue();</Pre>
	 */
	public AssertResultConstructor isTrue(Boolean b) {
		return checkBoolean(b).isTrue();
	}
	
	/**
	 * Equivalent of: <pre>#checkBoolean(b).isFalse();</Pre>
	 */
	public AssertResultConstructor isFalse(Boolean b) {
		return checkBoolean(b).isFalse();
	}
	
	/**
	 * Equivalent of: <pre>#checkNumber(actual).isEqual(expected);</Pre>
	 */
	public AssertResultConstructor equalNumbers(Number actual, Number expected) {
		return checkNumber(actual).isEqual(expected);
	}
	
	/**
	 * Equivalent of: <pre>#checkCollection(col).isEmpty();</Pre>
	 */
	public AssertResultConstructor isEmpty(Collection<?> col) {
		return checkCollection(col).isEmpty();
	}
	
	/**
	 * Equivalent of: <pre>#checkCollection(col).isNotEmpty();</Pre>
	 */
	public AssertResultConstructor isNotEmpty(Collection<?> col) {
		return checkCollection(col).isNotEmpty();
	}
	
	/**
	 * Equivalent of: <pre>#checkCollection(actual).containsSameElements(expected);</Pre>
	 */
	public <T> AssertResultConstructor sameElements(Collection<? extends T> actual, Collection<? extends T> expected) {
		return this.<T>checkCollection(actual).containsSameElements(expected);
	}
	
	/**
	 * Equivalent of: <pre>#checkThrowable(null).isInstanceOf(expected);</Pre>
	 */
	public AssertResultConstructor throwableExpected(Class<? extends Throwable> expected) {
		return checkThrowable(null).isInstanceOf(expected);
	}
	
	/**
	 * Equivalent of: <pre>#checkThrowable(t).notExpected();</Pre>
	 */
	public AssertResultConstructor notExpected(Throwable t) {
		return checkThrowable(t).notExpected();
	}
}
