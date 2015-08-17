package org.whaka.asserts.builder;

import org.whaka.asserts.AssertResult;

/**
 * <p>Class provides functionality to set fields "message" and "cause" to underlying instance
 * of the {@link AssertResult} with builder pattern.
 *
 * <p>Use method {@link #create(AssertResult)} to receive instance of the constructor, or {@link #EMPTY} constant
 * as "empty" instance with no underlying result.
 *
 * <p>When there's no underlying result - setters will cause no effect.
 *
 * <p><b>Note:</b> {@link #withMessage(String)} and {@link #withMessage(String, Object[])} are two different methods.
 */
public class AssertResultConstructor {

	public static final AssertResultConstructor EMPTY = new AssertResultConstructor(null);
	
	private final AssertResult assertResult;
	
	private AssertResultConstructor(AssertResult result) {
		assertResult = result;
	}
	
	/**
	 * If specified result is <code>null</code> - {@link #EMPTY} will be returned.
	 * Otherwise - new instance will be created and returned.
	 */
	public static AssertResultConstructor create(AssertResult result) {
		return result == null ? EMPTY : new AssertResultConstructor(result);
	}
	
	public AssertResult getAssertResult() {
		return assertResult;
	}
	
	/**
	 * Message directly set to the underlying assert result (if present).
	 * No additional formatting is performed.
	 *
	 * @see #withMessage(String, Object[])
	 */
	public AssertResultConstructor withMessage(String message) {
		if (assertResult != null)
			setMessageToResult(message);
		return this;
	}
	
	/**
	 * If specified message is not null - {@link String#format(String, Object[])} is performed with the message as
	 * format string and specified arguments. Then message set to the underlying assert result (if present).
	 *
	 * @see #withMessage(String)
	 */
	public AssertResultConstructor withMessage(String message, Object... args) {
		if (assertResult != null) {
			if (message != null)
				message = String.format(message, args);
			setMessageToResult(message);
		}
		return this;
	}
	
	private void setMessageToResult(String message) {
		assertResult.setMessage(message);
	}
	
	/**
	 * Set specified cause to the underlying assert result (if present).
	 */
	public AssertResultConstructor withCause(Throwable cause) {
		if (assertResult != null)
			assertResult.setCause(cause);
		return this;
	}
}
