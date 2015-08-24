package org.whaka.asserts.matcher;

import org.hamcrest.BaseMatcher;
import org.whaka.asserts.AssertResult;

public abstract class ResultProvidingMatcher<T> extends BaseMatcher<T> {

	public abstract AssertResult createAssertResult(T item, String message, Throwable cause);
}
