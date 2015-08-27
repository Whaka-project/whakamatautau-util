package org.whaka.util.reflection.comparison;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.whaka.util.reflection.properties.ClassPropertyKey;

/**
 * <p>Class provides functionality to build complex comparison results for a single type. Class specified
 * in the constructor represents the compared type. All created property keys will be pointing to this type.
 * Actual and expected values at the end of building process also should be instances of the specified type.
 */
public class ComplexComparisonResultBuilder<T> {

	private final Map<ClassPropertyKey, ComparisonResult> propertyResults = new HashMap<>();
	private final Class<? super T> type;
	
	private ComparisonPerformer<Object> defaultComparisonPerformer = ComparisonPerformers.DEEP_EQUALS;

	public ComplexComparisonResultBuilder(Class<? super T> type) {
		this.type = type;
	}
	
	/**
	 * Creates key with the specified name and the type specified at the constructor
	 */
	public ClassPropertyKey createKey(String name) {
		return new ClassPropertyKey(name, type);
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public void setDefaultComparisonPerformer(ComparisonPerformer<Object> defaultComparisonPerformer) {
		this.defaultComparisonPerformer = Objects.requireNonNull(defaultComparisonPerformer,
				"Default comparison performer cannot be null!");
	}
	
	public ComparisonPerformer<Object> getDefaultComparisonPerformer() {
		return defaultComparisonPerformer;
	}

	public Map<ClassPropertyKey, ComparisonResult> getPropertyResults() {
		return propertyResults;
	}
	
	/**
	 * <p>Perform comparison of the specified values using default performer, and store the result. Property key
	 * is created from the specified name using {@link #createKey(String)} method.
	 * 
	 * @see #getDefaultComparisonPerformer()
	 * @see #setDefaultComparisonPerformer(ComparisonPerformer)
	 */
	public ComplexComparisonResultBuilder<T> compare(String propertyName, Object actual, Object expected) {
		return compare(propertyName, actual, expected, getDefaultComparisonPerformer());
	}
	
	/**
	 * <p>Perform comparison of the specified values using specified performer, and store the result. Property key
	 * is created from the specified name using {@link #createKey(String)} method.
	 * 
	 * <p>Equal to performing comparison manually and using {@link #addResult(String, ComparisonResult)} to store
	 * result. But improves readability.
	 */
	public <X> ComplexComparisonResultBuilder<T> compare(String propertyName, X actual, X expected, ComparisonPerformer<? super X> performer) {
		addResult(propertyName, performer.appl(actual, expected));
		return this;
	}
	
	/**
	 * <p>Store specified result. Property key is created from the specified name
	 * using {@link #createKey(String)} method.
	 */
	public ComplexComparisonResultBuilder<T> addResult(String propertyName, ComparisonResult result) {
		propertyResults.put(createKey(propertyName), result);
		return this;
	}
	
	/**
	 * Build complex result using map of stored results. Specified values and performer are used to construct result.
	 * @see ComplexComparisonResult#ComplexComparisonResult(Object, Object, ComparisonPerformer, Map)
	 */
	public ComplexComparisonResult build(T actual, T expected, ComparisonPerformer<? super T> performer) {
		return new ComplexComparisonResult(actual, expected, performer, propertyResults);
	}
}
