package org.whaka.asserts;

import com.google.common.base.Function;
import org.whaka.asserts.builder.AssertBuilder;
import org.whaka.asserts.builder.AssertResultConstructor;

public abstract class AbstractInstantAssert<PerformerType, ValueType> {

	private final ValueType actual;
	
	public AbstractInstantAssert(ValueType actual) {
		this.actual = actual;
	}
	
	public ValueType getActual() {
		return actual;
	}
	
	protected abstract PerformerType createPerformer(AssertBuilder builder, ValueType actual);
	
	/**
	 * <p>This method will call {@link #createPerformer(AssertBuilder, Object)} with newly created
	 * instance of the AssertBuilder and object specified in constructor as "actual" value.
	 *
	 * <p>Object received as a result will be passed into the "assertFunction" specified in this method.
	 *
	 * <p>If specified message is not null - {@link AssertResultConstructor#withMessage(String)} will be called
	 * on a constructor received as a function output (if it's not null).
	 *
	 * <p>Then {@link AssertBuilder#performAssert()} will be called, so if any asserts was added to the builder
	 * in the process - error will be thrown.
	 */
	protected void performInstantAssert(
			Function<PerformerType, AssertResultConstructor> assertFunction,
			String message) throws AssertError {
		AssertBuilder builder = Assert.builder();
		PerformerType performer = createPerformer(builder, getActual());
		AssertResultConstructor messageConstructor = assertFunction.apply(performer);
		setMessageIfNotNull(messageConstructor, message);
		builder.performAssert();
	}
	
	private static void setMessageIfNotNull(AssertResultConstructor constructor, String message) {
		if (constructor != null && message != null)
			constructor.withMessage(message);
	}
}
