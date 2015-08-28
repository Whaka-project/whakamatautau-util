package org.whaka.asserts;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.whaka.asserts.matcher.ComparisonMatcher;
import org.whaka.asserts.matcher.ConsistencyMatcher;
import org.whaka.asserts.matcher.FunctionalMatcher;
import org.whaka.asserts.matcher.RegexpMatcher;
import org.whaka.asserts.matcher.ThrowableMatcher;
import org.whaka.util.UberCollections;
import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonPerformers;

import com.google.common.base.Preconditions;


/**
 * Class provides static factory methods for custom Hamcrest matchers provided by the library.
 * 
 * @see NumberMatchers
 */
public final class UberMatchers {

	private UberMatchers() {
	}
	
	/**
	 * Create a matcher that will check that tested item and specified value are equal
	 * according to the specified {@link ComparisonPerformer}.
	 * 
	 * @see #deeplyEqualTo(Object)
	 * @see #reflectivelyEqualTo(Object)
	 * @see ComparisonMatcher
	 * @see ComparisonPerformers
	 */
	public static <T> Matcher<T> equalTo(T item, ComparisonPerformer<? super T> performer) {
		return new ComparisonMatcher<>(item, performer);
	}
	
	/**
	 * Create a matcher that will check that tested item and specified value are equal
	 * according to the {@link ComparisonPerformers#DEEP_EQUALS} performer.
	 * 
	 * @see #equalTo(Object, ComparisonPerformer)
	 * @see #reflectivelyEqualTo(Object)
	 * @see ComparisonMatcher
	 * @see ComparisonPerformers
	 */
	public static <T> Matcher<T> deeplyEqualTo(T item) {
		return equalTo(item, ComparisonPerformers.DEEP_EQUALS);
	}
	
	/**
	 * Create a matcher that will check that tested item and specified value are equal
	 * according to the {@link ComparisonPerformers#REFLECTIVE_EQUALS} performer.
	 * 
	 * @see #equalTo(Object, ComparisonPerformer)
	 * @see #deeplyEqualTo(Object)
	 * @see ComparisonMatcher
	 * @see ComparisonPerformers
	 */
	public static <T> Matcher<T> reflectivelyEqualTo(T item) {
		return equalTo(item, ComparisonPerformers.REFLECTIVE_EQUALS);
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
	 * <p>Create a matcher that will check that tested item and specified value are either both consistently <b>nulls</b>,
	 * or both consistently <b>not-nulls</b>.
	 * 
	 * <p>Equal to {@link #consistentWith(Object, Matcher)} with {@link Matchers#nullValue()} as delegate.
	 * 
	 * @see ConsistencyMatcher
	 */
	public static Matcher<Object> nullConsistentWith(Object value) {
		return consistentWith(value, Matchers.nullValue());
	}
	
	/**
	 * Create a matcher that will check that string representation of <b>any</b> object
	 * matches specified regexp pattern.
	 * 
	 * @throws NullPointerException if specified string is <code>null</code>
	 * 
	 * @see #matches(Pattern)
	 * @see RegexpMatcher
	 */
	public static Matcher<Object> matches(String pattern) {
		return RegexpMatcher.create(pattern);
	}
	
	/**
	 * Create a matcher that will check that string representation of <b>any</b> object
	 * matches specified pattern.
	 * 
	 * @throws NullPointerException if specified pattern is <code>null</code>
	 * 
	 * @see #matches(String)
	 * @see RegexpMatcher
	 */
	public static Matcher<Object> matches(Pattern pattern) {
		return RegexpMatcher.create(pattern);
	}
	
	/**
	 * Create a matcher that will check that an exception is an instance of the specified type.
	 * And will set asserted throwable as cause of the result.
	 * 
	 * @see #notExpected()
	 * @see ThrowableMatcher
	 */
	public static Matcher<Throwable> throwableOfType(Class<? extends Throwable> type) {
		return ThrowableMatcher.throwableOfType(type);
	}
	
	/**
	 * Create a matcher that will check that an exception is <code>null</code>.
	 * And will set asserted throwable as cause of the result.
	 * 
	 * @see #throwableOfType(Class)
	 * @see ThrowableMatcher
	 */
	public static Matcher<Throwable> notExpected() {
		return ThrowableMatcher.notExpected();
	}

	/**
	 * <p>Create a matcher that will check that a collection contains the specified item.
	 * Specified predicate is used to check elements equality.
	 * 
	 * <p><b>Note:</b> matcher will throw an NPE if matched against a <code>null</code> value!
	 * 
	 * @throws NullPointerException if specified predicate is <code>null</code>
	 */
	public static <T> Matcher<Collection<? extends T>> hasItem(T item, BiPredicate<T, T> matcher) {
		Objects.requireNonNull(matcher, "Predicate cannot be null!");
		return new FunctionalMatcher<Collection<? extends T>>(
				Collection.class,
				c -> UberCollections.contains(c, item, matcher),
				d -> d.appendText("has item ").appendValue(item).appendText(" with a predicate"));
	}
	
	/**
	 * Equal to {@link #hasAnyItem(Collection, BiPredicate)} with {@link Objects#deepEquals(Object, Object)}
	 * used as a predicate.
	 * 
	 * <p><b>Note:</b> matcher will throw an NPE if matched against a <code>null</code> value!
	 * 
	 * @throws NullPointerException if specified collection is <code>null</code>
	 * @throws IllegalArgumentException if specified collection is <code>empty</code>
	 */
	public static <T> Matcher<Collection<? extends T>> hasAnyItem(Collection<T> items) {
		return hasAnyItem(items, Objects::deepEquals);
	}
	
	/**
	 * <p>Create a matcher that will check that a collection contains any one of the specified item.
	 * Specified predicate is used to check elements equality.
	 * 
	 * <p><b>Note:</b> matcher will throw an NPE if matched against a <code>null</code> value!
	 * 
	 * @throws NullPointerException if specified collection or specified predicate is <code>null</code>
	 * @throws IllegalArgumentException if specified collection is <code>empty</code>
	 * 
	 * @see #hasAnyItem(Collection)
	 */
	public static <T> Matcher<Collection<? extends T>> hasAnyItem(Collection<T> items, BiPredicate<T, T> matcher) {
		Objects.requireNonNull(items, "Items collection cannot be null!");
		Objects.requireNonNull(matcher, "Predicate cannot be null!");
		Preconditions.checkArgument(!items.isEmpty(), "Items cannot be empty!");
		return new FunctionalMatcher<Collection<? extends T>>(
				Collection.class,
				c -> UberCollections.containsAny(c, items, matcher),
				d -> d.appendText("has any one of ").appendValue(items).appendText(" with a predicate"));
	}

	/**
	 * Create a matcher that will check that an item is contained in the specified collection.
	 * Specified predicate is used to check elements equality.
	 * 
	 * @throws NullPointerException if specified collection or predicate is <code>null</code>
	 */
	public static <T> Matcher<T> isIn(Collection<? extends T> col, BiPredicate<T, T> matcher) {
		Objects.requireNonNull(col, "Collection cannot be null!");
		Objects.requireNonNull(matcher, "Predicate cannot be null!");
		return new FunctionalMatcher<T>(
				Object.class,
				t -> UberCollections.contains(col, t, matcher),
				d -> d.appendText("one of ").appendValue(col).appendText(" with a predicate"));
	}
	
	/**
	 * Create a matcher that will apply specified function to the received value and then delegate result
	 * to the specified matcher delegate.
	 * 
	 * @throws NullPointerException if specified matcher or function is <code>null</code>
	 */
	public static <T, V> Matcher<V> convert(Matcher<T> m, Function<V, T> f) {
		Objects.requireNonNull(m, "Delegate matcher cannot be null!");
		Objects.requireNonNull(f, "Converting function cannot be null!");
		return new FunctionalMatcher<V>(
				Object.class,
				v -> m.matches(f.apply(v)),
				d -> m.describeTo(d));
	}
}