package org.whaka.asserts;

import org.whaka.asserts.builder.AssertBuilder;
import org.whaka.asserts.builder.NumberAssertPerformer;

public class NumberAssert extends AbstractInstantAssert<NumberAssertPerformer, Number> {

	public NumberAssert(Number actual) {
		super(actual);
	}

	@Override
	protected NumberAssertPerformer createPerformer(AssertBuilder builder, Number actual) {
		return builder.checkNumber(actual);
	}
	
	public void isZero() {
		isZero(null);
	}
	
	public void isZero(String message) {
		performInstantAssert(NumberAssertPerformer::isZero, message);
	}
	
	public void isEqual(Number expected) {
		isEqual(expected, null);
	}
	
	public void isEqual(Number expected, String message) {
		performInstantAssert(p -> p.isEqual(expected), message);
	}
	
	public void isNotEqual(Number unexpected) {
		isNotEqual(unexpected, null);
	}
	
	public void isNotEqual(Number unexpected, String message) {
		performInstantAssert(p -> p.isNotEqual(unexpected), message);
	}
	
	public void isNumber() {
		isNumber(null);
	}
	
	public void isNumber(String message) {
		performInstantAssert(NumberAssertPerformer::isNumber, message);
	}
	
	public void isFinite() {
		isFinite(null);
	}
	
	public void isFinite(String message) {
		performInstantAssert(NumberAssertPerformer::isFinite, message);
	}
	
	public void isPositive() {
		isPositive(null);
	}
	
	public void isPositive(String message) {
		performInstantAssert(NumberAssertPerformer::isPositive, message);
	}
	
	public void isNegative() {
		isNegative(null);
	}
	
	public void isNegative(String message) {
		performInstantAssert(NumberAssertPerformer::isNegative, message);
	}
	
	public void isGreaterThan(Number other) {
		isGreaterThan(other, null);
	}
	
	public void isGreaterThan(Number other, String message) {
		performInstantAssert(p -> p.isGreaterThan(other), message);
	}
	
	public void isLowerThan(Number other) {
		isLowerThan(other, null);
	}
	
	public void isLowerThan(Number other, String message) {
		performInstantAssert(p -> p.isLowerThan(other), message);
	}
	
	public void isGreaterThanOrEqual(Number other) {
		isGreaterThanOrEqual(other, null);
	}
	
	public void isGreaterThanOrEqual(Number other, String message) {
		performInstantAssert(p -> p.isGreaterThanOrEqual(other), message);
	}
	
	public void isLowerThanOrEqual(Number other) {
		isLowerThanOrEqual(other, null);
	}
	
	public void isLowerThanOrEqual(Number other, String message) {
		performInstantAssert(p -> p.isLowerThanOrEqual(other), message);
	}
	
	public void isBetween(Number min, Number max) {
		isBetween(min, max, null);
	}
	
	public void isBetween(Number min, Number max, String message) {
		performInstantAssert(p -> p.isBetween(min, max), message);
	}
	
	public void isBetweenOrEqual(Number min, Number max) {
		isBetweenOrEqual(min, max, null);
	}
	
	public void isBetweenOrEqual(Number min, Number max, String message) {
		performInstantAssert(p -> p.isBetweenOrEqual(min, max), message);
	}
}
