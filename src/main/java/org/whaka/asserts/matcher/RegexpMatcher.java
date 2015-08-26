package org.whaka.asserts.matcher;

import static java.util.Objects.*;

import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * <p>{@link Matcher} implementation that can match <b>any</b> object against regexp {@link Pattern}.
 * Any matched object is converted into a string using {@link String#valueOf(Object)} and then matched
 * against the pattern specified at the construction. Therefore <code>null</code> value will be successfully
 * matched against pattern string: <code>"null"</code>.
 * 
 * <p><b>Note:</b> if matcher is required to match only strings, or non-null values - {@link Matchers#allOf(Matcher...)}
 * might be used to combine it with additional predicates.
 * 
 * @see #create(String)
 * @see #create(Pattern)
 */
public class RegexpMatcher extends BaseMatcher<Object> {

	private final Pattern pattern;
	
	private RegexpMatcher(Pattern pattern) {
		this.pattern = requireNonNull(pattern, "Pattern cannot be null!");
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	
	@Override
	public boolean matches(Object item) {
		return getPattern().matcher(String.valueOf(item)).matches();
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("matching ").appendValue(getPattern());
	}
	
	/**
	 * Creates new instance of the {@link RegexpMatcher} with a new {@link Pattern} created from the specified string.
	 * 
	 * @see #create(Pattern)
	 * @throws NullPointerException if specified string is <code>null</code>
	 */
	@Factory
	public static RegexpMatcher create(String pattern) {
		return create(Pattern.compile(requireNonNull(pattern, "Pattern cannot be null!")));
	}
	
	/**
	 * Creates new instance of the {@link RegexpMatcher} with specified pattern.
	 * 
	 * @see #create(String)
	 * @throws NullPointerException if specified pattern is <code>null</code>
	 */
	@Factory
	public static RegexpMatcher create(Pattern pattern) {
		return new RegexpMatcher(pattern);
	}
}