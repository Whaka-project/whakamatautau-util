package org.whaka.asserts;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.whaka.asserts.matcher.ThrowableMatcher;


/**
 * Class provides static factory methods for custom Hamcrest matchers provided by the librabry.
 * 
 * @see NumberMatchers
 */
public final class UberMatchers {

	private UberMatchers() {
	}
	
	/**
	 * Create a matcher that will check that an exception is an instance of the specified type.
	 * And will set asserted throwable as cause of the result.
	 * 
	 * @see ThrowableMatcher
	 */
	public static Matcher<Throwable> throwableOfType(Class<? extends Throwable> type) {
		return ThrowableMatcher.throwableOfType(type);
	}
	
	/**
	 * Create a matcher that will check that an exception is <code>null</code>.
	 * And will set asserted throwable as cause of the result.
	 * 
	 * @see ThrowableMatcher
	 */
	public static Matcher<Throwable> notExpected() {
		return ThrowableMatcher.notExpected();
	}
	
	/**
	 * <p>If specified object is <code>null</code> - method returns {@link Matchers#nullValue()}.
	 * Otherwise it returns {@link Matchers#notNullValue()}
	 * 
	 * <p>Factory method is useful in case you
	 */
	public static Matcher<Object> nullConsistentWith(Object other) {
		return other == null ? Matchers.nullValue() : Matchers.notNullValue();
	}
}