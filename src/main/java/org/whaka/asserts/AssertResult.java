package org.whaka.asserts;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Objects;

public class AssertResult {

	private Object actual;
	private Object expected;
	private String message;
	private Throwable cause;

	public AssertResult() {
	}
	
	public AssertResult(String message) {
		this(message, null);
	}
	
	public AssertResult(String message, Throwable cause) {
		this(null, null, message, cause);
	}

	public AssertResult(Object actual, Object expected, String message) {
		this(actual, expected, message, null);
	}

	public AssertResult(Object actual, Object expected, String message, Throwable cause) {
		this.actual = actual;
		this.expected = expected;
		this.message = message;
		this.cause = cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	public Throwable getCause() {
		return cause;
	}

	public void setActual(Object actual) {
		this.actual = actual;
	}
	
	public Object getActual() {
		return actual;
	}

	public void setExpected(Object expected) {
		this.expected = expected;
	}
	
	public Object getExpected() {
		return expected;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		ToStringHelper tsh = MoreObjects.toStringHelper(this);
		if (getActual() != null || getExpected() != null) {
			tsh.add("actual", getActual());
			tsh.add("expected", getExpected());
		}
		if (getMessage() != null)
			tsh.add("message", getMessage());
		if (getCause() != null)
			tsh.add("cause", getCause().getClass());
		return tsh.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getActual(), getExpected(), getMessage(), getCause());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			AssertResult that = (AssertResult) object;
			return Objects.equal(getActual(), that.getActual())
					&& Objects.equal(getExpected(), that.getExpected())
					&& Objects.equal(getMessage(), that.getMessage())
					&& Objects.equal(getCause(), that.getCause());
		}
		return false;
	}
}
