package org.whaka.asserts.matcher;

import static org.hamcrest.Matchers.*;

import java.util.Objects;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

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
