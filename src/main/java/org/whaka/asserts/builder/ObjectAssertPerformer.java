package org.whaka.asserts.builder;

import static java.util.Collections.*;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.whaka.asserts.AssertResult;
import org.whaka.asserts.ComparisonAssertResult;
import org.whaka.util.UberCollections;
import org.whaka.util.reflection.comparison.ComparisonFail;
import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonPerformers;
import org.whaka.util.reflection.comparison.ComparisonResult;

/**
 * <p>Holds instance of the 'actual' object and provides methods to assert its states.
 * When assert is not passed - new {@link AssertResult} is created and passed into specified consumer.
 *
 * <p>"is*" methods return {@link AssertResultConstructor} so configuration of the result
 * can be continued after it's creation.
 *
 * <p>Example:
 * <pre>
 * String val = "qwe";
 *
 * AssertBuilder builder = new AssertBuilder()
 * ObjectAssertPerformer performer = new ObjectAssertPerformer(val, builder);
 * performer.isNotNull();
 * performer.isEqual("qaz").withMessage("Should be 'qaz'!");
 * List&lt;AssertResult&rt; results = builder.getAssertResults();</pre>
 *
 * <p>Here result will contain one AssertResult instance, indication that actual value is "qwe",
 * while "qaz" were expected.
 */
public class ObjectAssertPerformer<T> extends AssertPerformer<T> {

	public static final String MESSAGE_NOT_NULL_VALUE = "Object expected to be NOT null!";
	public static final String MESSAGE_NULL_CONSISTENT = "Objects expected to be both null, or both not null!";
	public static final String MESSAGE_EQUAL_OBJECTS = "Objects expected to be equal!";
	public static final String MESSAGE_NOT_EQUAL_OBJECTS = "Objects expected to be NOT equal!";
	
	public static final String EXPECTED_NON_NULL_VALUE = "Non-null value";
	
	public ObjectAssertPerformer(T actual, Consumer<AssertResult> consumer) {
		super(actual, consumer);
	}

	/**
	 * Assert passes if <code>actual</code> and <code>expected</code> values
	 * are consistently both equal to <code>null</code>, or both not equal to <code>null</code>.
	 */
	public AssertResultConstructor isNullConsistent(Object expected) {
		return performCheck((a, e) -> (a == null) == (e == null), expected).withMessage(MESSAGE_NULL_CONSISTENT);
	}
	
	public AssertResultConstructor isEqual(Object expected) {
		return performCheck(Objects::deepEquals, expected).withMessage(MESSAGE_EQUAL_OBJECTS);
	}
	
	/**
	 * <p>Perform <b>custom</b> comparison using specified comparison performer.
	 * <p>Performer is executed in a safe way, using {@link ComparisonPerformers#safePerform(Object, Object, ComparisonPerformer)}
	 * meaning that this method will never throw an exception, unless the performer itself is null.
	 * <p>{@link ComparisonAssertResult} created as a result if comparison has failed. <b>Note:</b> result is created
	 * using {@link ComparisonAssertResult#createWithCause(ComparisonResult)} method meaning that if comparison
	 * performer returns instance of the {@link ComparisonFail} - assert result will contain the same cause.
	 */
	public AssertResultConstructor isEqual(T expected, ComparisonPerformer<? super T> performer) {
		ComparisonResult comparisonResult = ComparisonPerformers.safePerform(getActual(), expected, performer);
		AssertResult result = null;
		if (!comparisonResult.isSuccess())
			result = performResult(ComparisonAssertResult.createWithCause(comparisonResult));
		return AssertResultConstructor.create(result).withMessage(MESSAGE_EQUAL_OBJECTS);
	}
	
	public AssertResultConstructor isNotEqual(Object expected) {
		String actualExpected = String.format("Not '%s'", expected);
		return performCheck((a,b) -> !Objects.deepEquals(a, expected), actualExpected).withMessage(MESSAGE_NOT_EQUAL_OBJECTS);
	}
	
	public AssertResultConstructor isNull() {
		return isEqual(null);
	}
	
	public AssertResultConstructor isNotNull() {
		return performCheck((a,b) -> a != null, EXPECTED_NON_NULL_VALUE).withMessage(MESSAGE_NOT_NULL_VALUE);
	}
	
	public AssertResultConstructor isIn(Collection<? extends T> col) {
		return isIn(col, UberCollections.deepEqualsPredicate());
	}
	
	public AssertResultConstructor isIn(Collection<? extends T> col, BiPredicate<T, T> predicate) {
		if (col == null || col.isEmpty())
			throw new IllegalArgumentException("Elements collection cannot be null or empty!");
		return new CollectionAssertPerformer<>(singleton(getActual()), getConsumer()).containsAny(col, predicate);
	}
}
