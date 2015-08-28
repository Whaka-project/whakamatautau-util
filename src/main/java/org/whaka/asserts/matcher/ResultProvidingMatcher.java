package org.whaka.asserts.matcher;

import java.util.Optional;

import org.hamcrest.BaseMatcher;
import org.whaka.asserts.AssertResult;

public abstract class ResultProvidingMatcher<T> extends BaseMatcher<T> {

	@Override
	@SuppressWarnings("unchecked")
	public final boolean matches(Object item) {
		return !matches((T) item, null, null).isPresent();
	}
	
	/**
	 * This method should return {@link Optional#empty()} in case matcher successfully matched specified item.
	 * Otherwise it should return an optional containing an instance of the {@link AssertResult}.
	 */
	public abstract Optional<? extends AssertResult> matches(T item, String message, Throwable cause);
}
