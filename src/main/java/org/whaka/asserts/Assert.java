package org.whaka.asserts;

import java.util.Collection;
import java.util.function.Consumer;

import org.hamcrest.Matcher;

import org.whaka.asserts.builder.AssertBuilder;

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
	 * Equivalent to:
	 * <pre>
	 * 	#builder().perform(builderConfiguration).performAssert();
	 * </pre>
	 */
	public static void buildAndPerform(Consumer<AssertBuilder> builderConfiguration) {
		builder().perform(builderConfiguration).performAssert();
	}
	
	/**
	 * <p>Create message and throw {@link AssertError} immediately.
	 * <p>Equivalent to {@link AssertBuilder#addMessage(String)}
	 */
	public static void fail(String message) {
		builder().addMessage(message).performAssert();
	}
	
	/**
	 * <p>Create message with arguments and throw {@link AssertError} immediately.
	 * <p>Equivalent to {@link AssertBuilder#addMessage(String, Object...)}
	 */
	public static void fail(String message, Object... args) {
		builder().addMessage(message, args).performAssert();
	}
	
	public static <T> ObjectAssert<T> assertObject(T actual) {
		return new ObjectAssert<>(actual);
	}
	
	public static BooleanAssert assertBoolean(Boolean actual) {
		return new BooleanAssert(actual);
	}
	
	public static NumberAssert assertNumber(Number actual) {
		return new NumberAssert(actual);
	}
	
	public static StringAssert assertString(String actual) {
		return new StringAssert(actual);
	}
	
	public static <T> CollectionAssert<T> assertCollection(Collection<? extends T> actual) {
		return new CollectionAssert<T>(actual);
	}
	
	public static ThrowableAssert assertThrowable(Throwable actual) {
		return new ThrowableAssert(actual);
	}
	
	/**
	 * Equivalent to:
	 * <pre>
	 * 	#assertThrowable(null).isInstanceOf(expected);
	 * </pre>
	 */
	public static void assertThrowableExpected(Class<? extends Throwable> expected) {
		assertThrowable(null).isInstanceOf(expected);
	}
	
	/**
	 * Equivalent to:
	 * <pre>
	 * 	#assertThrowable(t).notExpected();
	 * </pre>
	 */
	public static void notExpected(Throwable t) {
		assertThrowable(t).notExpected();
	}
	
	/**
	 * Equal to the {@link #assertThat(Object, Matcher, String)}, but with null message.
	 */
	public static <T> void assertThat(T item, Matcher<T> matcher) {
		buildAndPerform(b -> b.checkThat(item, matcher));
	}
	
	/**
	 * Performs assertion check of the specified item using hamcrest {@link Matcher} object.
	 */
	public static <T> void assertThat(T item, Matcher<T> matcher, String message) {
		buildAndPerform(b -> b.checkThat(item, matcher).withMessage(message));
	}
}
