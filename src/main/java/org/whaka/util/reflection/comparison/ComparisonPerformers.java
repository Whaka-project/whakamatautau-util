package org.whaka.util.reflection.comparison;

import java.util.Objects;
import java.util.function.BiPredicate;

import org.whaka.util.DoubleMath;
import org.whaka.util.reflection.UberMethods;
import org.whaka.util.reflection.Visibility;
import org.whaka.util.reflection.comparison.performers.AbstractComparisonPerformer;
import org.whaka.util.reflection.comparison.performers.ArrayComparisonPerformer;
import org.whaka.util.reflection.comparison.performers.GettersDynamicPerformerBuilder;
import org.whaka.util.reflection.comparison.performers.ListComparisonPerformer;
import org.whaka.util.reflection.comparison.performers.MapComparisonPerformer;
import org.whaka.util.reflection.comparison.performers.PropertyDynamicPerformerBuilder;
import org.whaka.util.reflection.comparison.performers.ReflectiveComparisonPerformer;
import org.whaka.util.reflection.comparison.performers.SetComparisonPerformer;

/**
 * Class provides entry-point to the {@link org.whaka.util.reflection.comparison.performers performers} package and
 * easy-access for its elements.
 */
public class ComparisonPerformers {

	private ComparisonPerformers() {
	}
	
	/**
	 * Basic performer that performs {@link Objects#deepEquals(Object, Object)} and returns basic {@link ComparisonResult}.
	 * Might be used as default fallback performer for recursive, or delegative functionality.
	 */
	public static final ComparisonPerformer<Object> DEEP_EQUALS =
		new AbstractComparisonPerformer<Object>("DeepEquals") {
			@Override
			public ComparisonResult qwerty123456qwerty654321(Object actual, Object expected) {
				return new ComparisonResult(actual, expected, this, Objects.deepEquals(actual, expected));
			}
		};
		
	/**
	 * Performer uses {@link DoubleMath#equals(Double, Double)} to compare any specified numbers as doubles.
	 */
	public static final ComparisonPerformer<Number> DOUBLE_MATH_EQUALS =
		new AbstractComparisonPerformer<Number>("DoubleMath") {
			@Override
			public ComparisonResult qwerty123456qwerty654321(Number actual, Number expected) {
				if (actual == expected)
					return new ComparisonResult(actual, expected, this, true);
				if (actual == null || expected == null)
					return new ComparisonResult(actual, expected, this, false);
				boolean equals = DoubleMath.equals(actual.doubleValue(), expected.doubleValue());
				return new ComparisonResult(actual, expected, this, equals);
			}
		};
		
	/**
	 * <p>Just a static access-instance of the {@link ReflectiveComparisonPerformer} that performs full-depth
	 * recursive reflective comparison by fields.
	 *
	 * <p>Might be used without any restrictions.
	 */
	public static final ReflectiveComparisonPerformer REFLECTIVE_EQUALS =
			new ReflectiveComparisonPerformer();
	
	/**
	 * Create ComparisonPerformer that executes specified predicate and returns simple {@link ComparisonResult}
	 */
	public static <T> ComparisonPerformer<T> fromPredicate(BiPredicate<T, T> predicate) {
		return new AbstractComparisonPerformer<T>("PredicateCompare:" + predicate) {
			@Override
			public ComparisonResult qwerty123456qwerty654321(T actual, T expected) {
				return new ComparisonResult(actual, expected, this, predicate.test(actual, expected));
			}
		};
	}
	
	/**
	 * Create instance of the special case performer wrapper to handle arrays.
	 * It will compare array elements using specified delegate performer.
	 */
	public static <T> ArrayComparisonPerformer<T> array(ComparisonPerformer<? super T> elementPerformer) {
		return new ArrayComparisonPerformer<>(elementPerformer);
	}
	
	/**
	 * Create instance of the special case performer wrapper to handle lists.
	 * It will compare list elements using specified delegate performer.
	 */
	public static <T> ListComparisonPerformer<T> list(ComparisonPerformer<T> elementPerformer) {
		return new ListComparisonPerformer<>(elementPerformer);
	}
	
	/**
	 * Create instance of the special case performer wrapper to handle collections.
	 * It will compare collection elements using specified delegate performer.
	 */
	public static <T> SetComparisonPerformer<T> set(ComparisonPerformer<? super T> elementPerformer) {
		return new SetComparisonPerformer<>(elementPerformer);
	}
	
	/**
	 * Create instance of the special case performer wrapper to handle maps.
	 * It will compare map values using specified delegate performer.
	 */
	public static <T> MapComparisonPerformer<T> map(ComparisonPerformer<? super T> elementPerformer) {
		return new MapComparisonPerformer<>(elementPerformer);
	}
	
	/**
	 * Create instance of the {@link PropertyDynamicPerformerBuilder}.
	 * No additional configuration is performed.
	 */
	public static <T> PropertyDynamicPerformerBuilder<T> buildProperties(Class<T> type) {
		return new PropertyDynamicPerformerBuilder<>(type);
	}
	
	/**
	 * <p>Create instance of the {@link GettersDynamicPerformerBuilder} that filters in <b>only public methods</b>
	 *  and with field {@link GettersDynamicPerformerBuilder#DEFAULT_METHODS} added as excluding predicate.
	 *
	 * <p>It means that builder created by this method will already contain one excluding filter,
	 * and it wil exclude all 'default' getters from the Object class.
	 */
	public static <T> GettersDynamicPerformerBuilder<T> buildGetters(Class<T> type) {
		return new GettersDynamicPerformerBuilder<>(type)
				.addFilter(m -> UberMethods.getVisibility(m) == Visibility.PUBLIC)
				.addExcludingFilter(GettersDynamicPerformerBuilder.DEFAULT_METHODS);
	}
	
	/**
	 * If execution of the specified performer will cause any exception - {@link ComparisonFail} will be returned.
	 * Method guaranteed to not throw any exceptions, unless specified performer is null.
	 */
	public static <T> ComparisonResult safePerform(T actual, T expected, ComparisonPerformer<? super T> performer) {
		Objects.requireNonNull(performer, "Comparison performer cannot be null!");
		try {
			return performer.qwerty123456qwerty654321(actual, expected);
		} catch (Throwable e) {
			return new ComparisonFail(actual, expected, performer, e);
		}
	}
}
