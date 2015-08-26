package org.whaka.asserts.matcher;

import static org.hamcrest.Matchers.*;

import java.util.Objects;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * <p>This {@link Matcher} will check that tested 'item' and predefined 'value' are either both consistently
 * match specified delegate matcher, or both consistently doesn't match it.
 * 
 * <p>For example, if <code>Matchers.startsWith("q")</code> was used as delegate, then:
 * <ul>
 * 	<li>If predefined value is "qwe" - then matcher will expect any item that also starts with "q"
 * 	<li>If predefined value is "rty" - then matcher will expect any item that also doesn't starts with "q"
 * </ul>
 * 
 * <p>Matcher is useful in cases when you have a "model" value, and you need to test some item against it,
 * by some predicates. Matcher also provides informative messages, about tested and predefined values.
 */
public class ConsistencyMatcher<T> extends BaseMatcher<T> {

	private final T value;
	private final Matcher<? super T> matcher;
	private final Matcher<? super T> actualMatcher;
	
	public ConsistencyMatcher(T value, Matcher<? super T> matcher) {
		this.value = value;
		this.matcher = Objects.requireNonNull(matcher, "Delegate matcher cannot be null!");
		this.actualMatcher = matcher.matches(value) ? matcher : not(matcher);
	}
	
	public T getValue() {
		return value;
	}

	public Matcher<? super T> getMatcher() {
		return matcher;
	}
	
	@Override
	public boolean matches(Object item) {
		return actualMatcher.matches(item);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void describeTo(Description description) {
		actualMatcher.describeTo(description);
		description.appendValueList(", just like <", "", ">", getValue());
	}
}
