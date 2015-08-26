package org.whaka.asserts.matcher;

import static java.util.Optional.*;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.whaka.asserts.AssertResult;

/**
 * <p>Class implements a {@link Matcher} that checks type of an exception. If specified expected type is <code>null</code>
 * then matcher will expect <code>null</code> instead of actual throwable value.
 * 
 * <p>Difference from {@link Matchers#instanceOf(Class)} and {@link Matchers#nullValue()} is that this matcher
 * sets asserted throwable as cause of the result.
 * 
 * <p>Also matcher produces more obvious (exception related) messages.
 * 
 * @see #throwableOfType(Class)
 * @see #notExpected()
 */
public class ThrowableMatcher extends ResultProvidingMatcher<Throwable> {

	private static final ThrowableMatcher NOT_EXPECTED = new ThrowableMatcher(null);
	
	private final Class<? extends Throwable> expectedType;
	
	private ThrowableMatcher(Class<? extends Throwable> expectedType) {
		this.expectedType = expectedType;
	}
	
	public Class<? extends Throwable> getExpectedType() {
		return expectedType;
	}
	
	@Override
	public AssertResult matches(Throwable item, String message, Throwable cause) {
		if (getExpectedType() == null ? item == null : getExpectedType().isInstance(item))
			return null;
		Class<?> actual = ofNullable(item).map(Object::getClass).orElse(null);
		if (cause == null)
			cause = item;
		return new AssertResult(actual, getExpectedMessage(), message, cause);
	}
	
	@Override
	public void describeTo(Description description) {
		description.appendText(getExpectedMessage());
	}
	
	private String getExpectedMessage() {
		return getExpectedType() == null ? "no exception"
				: "instance of " + getExpectedType().getCanonicalName();
	}
	
	/**
	 * Creates an instance of {@link ThrowableMatcher} with specified type, as expected.
	 * 
	 * @see #notExpected()
	 */
	@Factory
	public static ThrowableMatcher throwableOfType(Class<? extends Throwable> type) {
		return type == null ? NOT_EXPECTED : new ThrowableMatcher(type);
	}
	
	/**
	 * Creates an instance of {@link ThrowableMatcher} with <code>null</code> as expected type.
	 * 
	 * @see #throwableOfType(Class)
	 */
	@Factory
	public static ThrowableMatcher notExpected() {
		return NOT_EXPECTED;
	}
}