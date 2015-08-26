package org.whaka.asserts.matcher;

import org.hamcrest.BaseMatcher;
import org.whaka.asserts.AssertResult;

public abstract class ResultProvidingMatcher<T> extends BaseMatcher<T> {

	@Override
	@SuppressWarnings("unchecked")
	public final boolean matches(Object item) {
		return matches((T) item, null, null) == null;
	}
	
	/**
	 * This method should return <code>null</code> in case matcher successfully matched specified item.
	 * Otherwise it should return an instance of the {@link AssertResult}.
	 */
	public abstract AssertResult matches(T item, String message, Throwable cause);
}
