package org.whaka.asserts;

import org.whaka.asserts.builder.AssertBuilder;
import org.whaka.asserts.builder.BooleanAssertPerformer;

public class BooleanAssert extends AbstractInstantAssert<BooleanAssertPerformer, Boolean> {

	public BooleanAssert(Boolean actual) {
		super(actual);
	}
	
	@Override
	protected BooleanAssertPerformer createPerformer(AssertBuilder builder, Boolean actual) {
		return builder.checkBoolean(actual);
	}
	
	public void isTrue() {
		isTrue(null);
	}
	
	public void isTrue(String message) {
		performInstantAssert(BooleanAssertPerformer::isTrue, message);
	}
	
	public void isFalse() {
		isFalse(null);
	}
	
	public void isFalse(String message) {
		performInstantAssert(BooleanAssertPerformer::isFalse, message);
	}
}
