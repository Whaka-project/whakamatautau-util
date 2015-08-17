package org.whaka.asserts.builder;

import java.util.function.Consumer;

import org.whaka.asserts.AssertResult;
import org.whaka.util.UberObjects;

/**
 * <p>Holds instance of the 'actual' throwable value and provides methods to assert its throwable states.
 * When assert is not passed - new {@link AssertResult} is created and passed into specified consumer.
 *
 * <p>Methods return {@link AssertResultConstructor} so configuration of the result
 * can be continued after it's creation.
 *
 * <p><b>Note:</b> when existing throwable is asserted - it is added to the result as cause.
 *
 * @see #notExpected()
 * @see #isInstanceOf(Class)
 */
public class ThrowableAssertPerformer extends AssertPerformer<Throwable> {

	public static final String MESSAGE_UNEXPECTED_THROWABLE_HAPPENED = "Unexpected throwable happened!";
	public static final String MESSAGE_ILLEGAL_THROWABLE_HAPPENED = "Illegal throwable happened!";
	public static final String MESSAGE_EXPECTED_THROWABLE_NOT_HAPPENED = "Expected throwable not happened!";
	
	public ThrowableAssertPerformer(Throwable actual, Consumer<AssertResult> consumer) {
		super(actual, consumer);
	}

	/**
	 * <p>Example:
	 * <pre>
	 * AssertBuilder builder = new AssertBuilder()
	 * try {
	 *     throw new IllegalArgumentException();
	 * } catch (Exception e) {
	 *     ThrowableAssertPerformer performer = new ThrowableAssertPerformer(e, builder);
	 *     performer.notExpected();
	 * }
	 * List&lt;AssertResult&gt; results = builder.getAssertResults();</pre>
	 *
	 * <p>Here result will contain one AssertResult instance, indication that no exception was expected.
	 *
	 * <p>Example:
	 * <pre>
	 * AssertBuilder builder = new AssertBuilder()
	 *
	 * Throwable cause = null;
	 * ThrowableAssertPerformer performer = new ThrowableAssertPerformer(cause, builder);
	 * performer.notExpected();
	 *
	 * List&lt;AssertResult&gt; results = builder.getAssertResults();</pre>
	 *
	 * <p>Here result will contain no AssertResult instances, for no throwable was expected, and none happened.
	 */
	public AssertResultConstructor notExpected() {
		return isInstanceOf(null);
	}

	/**
	 * <p>Example:
	 * <pre>
	 * AssertBuilder builder = new AssertBuilder()
	 * try {
	 *     throw new IllegalArgumentException();
	 * } catch (Exception e) {
	 *     ThrowableAssertPerformer performer = new ThrowableAssertPerformer(e, builder);
	 *     performer.isInstanceOf(NullPointegerException.class);
	 * }
	 * List&lt;AssertResult&gt; results = builder.getAssertResults();</pre>
	 *
	 * <p>Here result will contain one AssertResult instance, indication that actual throwable is of "illegal" type.
	 */
	public AssertResultConstructor isInstanceOf(Class<? extends Throwable> type) {
		if (getActual() == null) {
			if (type != null)
				return performExpectedThrowableNotHappened(type);
		}
		else {
			if (type == null)
				return performUnexpectedThrowableHappened();
			if (!type.isAssignableFrom(getActual().getClass()))
				return performIllegalThrowableHappened(type);
		}
		return AssertResultConstructor.EMPTY;
	}
	
	private AssertResultConstructor performUnexpectedThrowableHappened() {
		Class<?> actualType = UberObjects.map(getActual(), Object::getClass);
		return createAndPerformResult(actualType, null)
			.withMessage(MESSAGE_UNEXPECTED_THROWABLE_HAPPENED).withCause(getActual());
	}
	
	private AssertResultConstructor performExpectedThrowableNotHappened(Class<? extends Throwable> expected) {
		return createAndPerformResult(null, expected).withMessage(MESSAGE_EXPECTED_THROWABLE_NOT_HAPPENED);
	}
	
	private AssertResultConstructor performIllegalThrowableHappened(Class<? extends Throwable> expected) {
		return createAndPerformResult(getActual().getClass(), expected)
			.withMessage(MESSAGE_ILLEGAL_THROWABLE_HAPPENED).withCause(getActual());
	}
	
	private AssertResultConstructor createAndPerformResult(Object actual, Object expected) {
		AssertResult result = performResult(actual, expected);
		return AssertResultConstructor.create(result);
	}
}
