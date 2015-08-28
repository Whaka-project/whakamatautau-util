package org.whaka.asserts.matcher;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * <p>Class allows to create instances of the {@link Matcher}
 * using functional interfaces to delegate actual functionality.
 * 
 * <p>Three components are required to create a matcher:
 * <ul>
 * 	<li>Type of matched items
 * 	<li>Predicate of an item, to perform actual matching
 * 	<li>Description, providing some information about a matcher
 * </ul>
 * 
 * @see #FunctionalMatcher(Class, Predicate, Consumer)
 * @see #FunctionalMatcher(Class, Predicate, String)
 */
public class FunctionalMatcher<T> extends BaseMatcher<T> {

	private final Class<? super T> type;
	private final Predicate<T> predicate;
	private final Consumer<Description> describer;
	
	/**
	 * Equal to the {@link #FunctionalMatcher(Class, Predicate, Consumer)} but with an automatically created
	 * consumer that calls {@link Description#appendText(String)} with the specified string.
	 * 
	 */
	public FunctionalMatcher(Class<? super T> type, Predicate<T> predicate, String description) {
		this(type, predicate, d -> d.appendText(description));
	}
	
	/**
	 * To create an instance of the functional matcher you have to specify a {@link Class} of items,
	 * for Hamcrest's matchers don't perform any basic type assertion; a {@link Predicate} of the same type,
	 * that will be called for each item that's either <code>null</code>, or an instance of the specified class;
	 * and a {@link Consumer} for the {@link Description} type, that will be called each time matcher have to describe
	 * itself.
	 * 
	 * @see #FunctionalMatcher(Class, Predicate, String)
	 */
	public FunctionalMatcher(Class<? super T> type, Predicate<T> predicate, Consumer<Description> describer) {
		this.type = Objects.requireNonNull(type, "type");
		this.predicate = Objects.requireNonNull(predicate, "predicate");
		this.describer = Objects.requireNonNull(describer, "describer");
	}
	
	public Class<? super T> getType() {
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
