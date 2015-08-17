package org.whaka.asserts.builder;

import java.util.Objects;
import java.util.function.Consumer;

import org.whaka.asserts.AssertResult;

/**
 * <p>Holds instance of the 'actual' boolean value and provides methods to assert its boolean states.
 * When assert is not passed - new {@link AssertResult} is created and passed into specified consumer.
 *
 * <p>"is*" methods return {@link AssertResultConstructor} so configuration of the result
 * can be continued after it's creation.
 *
 * <p>Example:
 * <pre>
 * Boolean val = false;
 *
 * AssertBuilder builder = new AssertBuilder()
 * BooleanAssertPerformer performer = new BooleanAssertPerformer(val, builder);
 * performer.isTrue();
 * List&lt;AssertResult&rt; results = builder.getAssertResults();</pre>
 *
 * <p>Here result will contain one AssertResult instance, indication that actual value is "false",
 * while "true" were expected.
 */
public class BooleanAssertPerformer extends AssertPerformer<Boolean> {

	public BooleanAssertPerformer(Boolean actual, Consumer<AssertResult> consumer) {
		super(actual, consumer);
	}
	
	public AssertResultConstructor isTrue() {
		return performCheck(Boolean.TRUE);
	}
	
	public AssertResultConstructor isFalse() {
		return performCheck(Boolean.FALSE);
	}
	
	private AssertResultConstructor performCheck(Boolean expected) {
		return performCheck(Objects::equals, expected);
	}
}
