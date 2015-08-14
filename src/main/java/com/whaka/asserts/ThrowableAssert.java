package com.whaka.asserts;

import com.whaka.asserts.builder.AssertBuilder;
import com.whaka.asserts.builder.ThrowableAssertPerformer;

public class ThrowableAssert extends AbstractInstantAssert<ThrowableAssertPerformer, Throwable> {

	public ThrowableAssert(Throwable actual) {
		super(actual);
	}
	
	@Override
	protected ThrowableAssertPerformer createPerformer(AssertBuilder builder, Throwable actual) {
		return builder.checkThrowable(actual);
	}
	
	public void notExpected() {
		notExpected(null);
	}
	
	public void notExpected(String message) {
		performInstantAssert(ThrowableAssertPerformer::notExpected, message);
	}

	public void isInstanceOf(Class<? extends Throwable> type) {
		isInstanceOf(type, null);
	}
	
	public void isInstanceOf(Class<? extends Throwable> type, String message) {
		performInstantAssert(throwablePerformer -> throwablePerformer.isInstanceOf(type), message);
	}
}
