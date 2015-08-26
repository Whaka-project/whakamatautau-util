package org.whaka.asserts;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.whaka.asserts.matcher.ConsistencyMatcher;
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
	 * Create a matcher that will check that tested item and specified value are either both consistently matched,
	 * or both consistently not matched by the specified matcher.
	 * 
	 * @see #nullConsistentWith(Object)
	 * @see ConsistencyMatcher
	 */
	public static <T> Matcher<T> consistentWith(T value, Matcher<? super T> matcher) {
		return new ConsistencyMatcher<T>(value, matcher);
	}
	
	/**
	 * Create a matcher that will check that tested item and specified value are either both consistently <b>nulls</b>,
	 * or both consistently <b>not-nulls</b>.
	 * 
	 * @see #consistentWith(Object, Matcher)
	 * @see ConsistencyMatcher
	 */
	public static Matcher<Object> nullConsistentWith(Object value) {
		return consistentWith(value, Matchers.nullValue());
	}
}