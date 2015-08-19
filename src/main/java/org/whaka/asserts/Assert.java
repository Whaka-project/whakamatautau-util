package org.whaka.asserts;

import org.hamcrest.Matcher;

/**
 * <p>Entry point for all the functionality in the "Assertion framework". Not instantiable. Provides bunch of static
 * methods for easy access.
 *
 * <p>All "assert*" methods create corresponding specific "instant asserts" for specific types. Each instant assert
 * is a kind of a delegate for a corresponding "assert performer" from the builder package. Every time when
 * assertion method is called on an "instant assert" - new instance of the {@link AssertBuilder} is created, and after
 * assertion is performed - {@link AssertBuilder#performAssert()} is called, throwing immediate exception,
 * if preceding assert has failed. Thus "instant asserts" provide kind of a "syntactic sugar" to shorten calling
 * constructions a lot, but also reducing some functionality as a drawback. So if you need all the possible functionality
 * please use assertion builder directly.
 *
 * <p>Methods {@link #fail(String)} and {@link #fail(String, Object...)} allow you to create an assertion result
 * consisting only from a single message, and then instantly throw an assertion error with this result.
 * Easiest way to momentarily indicate functional fail.
 *
 * <p>Method {@link #builder()} provides the most obvious and easy way to create instance of the {@link AssertBuilder}.
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
	 * Equal to the {@link #assertThat(Object, Matcher, String)}, but with null message.
	 */
	public static <T> void assertThat(T item, Matcher<T> matcher) {
		builder().checkThat(item, matcher).performAssert();
	}
	
	/**
	 * Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 */
	public static <T> void assertThat(T item, Matcher<T> matcher, String message) {
		builder().checkThat(item, matcher, message).performAssert();
	}
	
	/**
	 * Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 */
	public static <T> void assertThat(T item, Matcher<T> matcher, String message, Throwable cause) {
		builder().checkThat(item, matcher, message, cause).performAssert();
	}
}
