package org.whaka.asserts.builder;

import static org.whaka.util.UberPredicates.*;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.whaka.asserts.AssertResult;

public class StringAssertPerformer extends AssertPerformer<String> {

	public static final String MESSAGE_EMPTY_STRING = "String expected to be empty!";
	public static final String MESSAGE_EMPTY_OR_NULL_STRING = "String expected to be empty or null!";
	public static final String MESSAGE_NOT_EMPTY_STRING = "String expected to be NOT empty!";
	public static final String MESSAGE_STRING_LENGTH = "Illegal length of a string!";
	public static final String MESSAGE_STRING_NOT_MATCH = "String doesn't match expected pattern!";
	
	public StringAssertPerformer(String actual, Consumer<AssertResult> consumer) {
		super(actual, consumer);
	}
	
	public AssertResultConstructor isEmpty() {
		return nonNullCheck(String::isEmpty, "Empty string").withMessage(MESSAGE_EMPTY_STRING);
	}
	
	public AssertResultConstructor isNullOrEmpty() {
		return performCheck((a,e) -> a == null || a.isEmpty(), "Empty or null string").withMessage(MESSAGE_EMPTY_OR_NULL_STRING);
	}
	
	public AssertResultConstructor isNotEmpty() {
		return nonNullCheck(not(String::isEmpty), "Not empty string").withMessage(MESSAGE_NOT_EMPTY_STRING);
	}
	
	public AssertResultConstructor isLength(int length) {
		return nonNullCheck((s,l) -> s.length() == l, length, "Length: %d").withMessage(MESSAGE_STRING_LENGTH);
	}
	
	public AssertResultConstructor contains(String part) {
		return nonNullCheck(String::contains, part, "Containing: '%s'").withMessage(MESSAGE_STRING_NOT_MATCH);
	}
	
	public AssertResultConstructor startsWith(String part) {
		return nonNullCheck(String::startsWith, part, "Starts with: '%s'").withMessage(MESSAGE_STRING_NOT_MATCH);
	}
	
	public AssertResultConstructor endsWith(String part) {
		return nonNullCheck(String::endsWith, part, "Ends with: '%s'").withMessage(MESSAGE_STRING_NOT_MATCH);
	}
	
	public AssertResultConstructor matches(String pattern) {
		Objects.requireNonNull(pattern, "String matcher cannot be null!");
		return matches(Pattern.compile(pattern));
	}
	
	public AssertResultConstructor matches(Pattern pattern) {
		return nonNullCheck((s,p) -> p.matcher(s).matches(), pattern, "Matching: '%s'").withMessage(MESSAGE_STRING_NOT_MATCH);
	}
	
	private AssertResultConstructor nonNullCheck(Predicate<String> check, String expected) {
		return performCheck((a,e) -> a != null && check.test(a), expected);
	}
	
	private <T> AssertResultConstructor nonNullCheck(BiPredicate<String, T> check, T another, String expected) {
		Objects.requireNonNull(another, "String matcher cannot be null!");
		BiPredicate<String, String> predicate = (a,e) -> a != null && check.test(a, another);
		return performCheck(predicate, String.format(expected, another));
	}
}
