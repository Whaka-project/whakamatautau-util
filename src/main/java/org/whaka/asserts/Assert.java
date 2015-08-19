package org.whaka.asserts;

import org.hamcrest.Matcher;

/**
 * <p>Entry point for all the functionality in the "Assertion framework". Not instantiable. Provides bunch of static
 * methods for easy access.
 *
 * <p>"<code>assertThat</code>" methods are clones of "<code>checkThat</code>" methods from the {@link AssertBuilder},
 * and are similar by functionality, but with assertion itself performed immediately.
 *
 * <p>Method {@link #fail(String, Object...)} allows you to create an assertion result
 * consisting only from a single message, and then instantly throw an assertion error with this result.
 * Easiest way to momentarily indicate functional fail.
 *
 * <p>Method {@link #builder()} provides the most obvious and easy way to create instance of the {@link AssertBuilder}.
 * 
 * @see #assertThat(Object, Matcher)
 * @see #assertThat(Object, Matcher, String)
 * @see #assertThat(Object, Matcher, String, Throwable)
 */
public class Assert {

	private Assert() {
	}

	/**
	 * Create instance of the {@link AssertBuilder}.
	 * Just a substitute for a constructor, with no additional functionality.
	 */
	public static AssertBuilder builder() {
		return new AssertBuilder();
	}
	
	/**
	 * <p>Create message with arguments and throw {@link AssertError} immediately.
	 * <p>Equivalent to {@link AssertBuilder#addMessage(String, Object...)}
	 */
	public static void fail(String message, Object... args) {
		builder().addMessage(message, args).performAssert();
	}
	
	/**
	 * <p>Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 * If item doesn't match expectations - {@link AssertError} is created, and {@link AssertError} is thrown instantly.
	 * 
	 * @see #assertThat(Object, Matcher, String)
	 * @see #assertThat(Object, Matcher, String, Throwable)
	 */
	public static <T> void assertThat(T item, Matcher<T> matcher) {
		builder().checkThat(item, matcher).performAssert();
	}
	
	/**
	 * <p>Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 * If item doesn't match expectations - {@link AssertError} is created with the specified message,
	 * and {@link AssertError} is thrown instantly.
	 * 
	 * @see #assertThat(Object, Matcher)
	 * @see #assertThat(Object, Matcher, String, Throwable)
	 */
	public static <T> void assertThat(T item, Matcher<T> matcher, String message) {
		builder().checkThat(item, matcher, message).performAssert();
	}
	
	/**
	 * <p>Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 * If item doesn't match expectations - {@link AssertError} is created with the specified message
	 * and the specified cause, and {@link AssertError} is thrown instantly.
	 * 
	 * @see #assertThat(Object, Matcher)
	 * @see #assertThat(Object, Matcher, String)
	 */
	public static <T> void assertThat(T item, Matcher<T> matcher, String message, Throwable cause) {
		builder().checkThat(item, matcher, message, cause).performAssert();
	}
}
