package org.whaka.asserts.matcher;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class FunctionalMatcher<T> extends BaseMatcher<T> {

	private final Class<T> type;
	private final Predicate<T> predicate;
	private final Consumer<Description> describer;
	
	public FunctionalMatcher(Class<T> type, Predicate<T> predicate, Consumer<Description> describer) {
		this.type = Objects.requireNonNull(type, "type");
		this.predicate = Objects.requireNonNull(predicate, "predicate");
		this.describer = Objects.requireNonNull(describer, "describer");
	}
	
	public Class<T> getType() {
		return type;
	}
	
	public Predicate<T> getPredicate() {
		return predicate;
	}
	
	public Consumer<Description> getDescriber() {
		return describer;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean matches(Object item) {
		return (item == null || getType().isInstance(item)) && getPredicate().test((T) item);
	}
	
	@Override
	public void describeTo(Description description) {
		getDescriber().accept(description);
	}
}
