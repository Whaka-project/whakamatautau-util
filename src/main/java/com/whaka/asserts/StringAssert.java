package com.whaka.asserts;

import java.util.function.BiFunction;
import java.util.regex.Pattern;

import com.whaka.asserts.builder.AssertBuilder;
import com.whaka.asserts.builder.AssertResultConstructor;
import com.whaka.asserts.builder.StringAssertPerformer;


public class StringAssert extends AbstractInstantAssert<StringAssertPerformer, String> {

	public StringAssert(String actual) {
		super(actual);
	}
	
	@Override
	protected StringAssertPerformer createPerformer(AssertBuilder builder, String actual) {
		return builder.checkString(actual);
	}
	
	public void isEmpty() {
		isEmpty(null);
	}
	
	public void isEmpty(String message) {
		performInstantAssert(StringAssertPerformer::isEmpty, message);
	}
	
	public void isNullOrEmpty() {
		isNullOrEmpty(null);
	}
	
	public void isNullOrEmpty(String message) {
		performInstantAssert(StringAssertPerformer::isNullOrEmpty, message);
	}
	
	public void isNotEmpty() {
		isNotEmpty(null);
	}
	
	public void isNotEmpty(String message) {
		performInstantAssert(StringAssertPerformer::isNotEmpty, message);
	}
	
	public void isLength(int length) {
		isLength(length, null);
	}
	
	public void isLength(int length, String message) {
		performInstant(StringAssertPerformer::isLength, length, message);
	}
	
	public void contains(String part) {
		contains(part, null);
	}
	
	public void contains(String part, String message) {
		performInstant(StringAssertPerformer::contains, part, message);
	}
	
	public void startsWith(String part) {
		startsWith(part, null);
	}
	
	public void startsWith(String part, String message) {
		performInstant(StringAssertPerformer::startsWith, part, message);
	}
	
	public void endsWith(String part) {
		endsWith(part, null);
	}
	
	public void endsWith(String part, String message) {
		performInstant(StringAssertPerformer::endsWith, part, message);
	}
	
	public void matches(String pattern) {
		matches(pattern, null);
	}
	
	public void matches(String pattern, String message) {
		performInstant(StringAssertPerformer::matches, pattern, message);
	}
	
	public void matches(Pattern pattern) {
		matches(pattern, null);
	}
	
	public void matches(Pattern pattern, String message) {
		performInstant(StringAssertPerformer::matches, pattern, message);
	}
	
	private <T> void performInstant(BiFunction<StringAssertPerformer, T, AssertResultConstructor> match, T arg, String message) {
		performInstantAssert(p -> match.apply(p, arg), message);
	}
}
