package org.whaka.util.reflection.comparison.performers;

import static org.whaka.util.reflection.comparison.ComparisonPerformers.*;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.primitives.Primitives;
import org.whaka.util.reflection.comparison.ComparisonFail;
import org.whaka.util.reflection.comparison.ComparisonPerformers;
import org.whaka.util.reflection.comparison.ComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResultBuilder;
import org.whaka.util.reflection.properties.ClassProperty;
import org.whaka.util.reflection.properties.ClassPropertyKey;
import org.whaka.util.reflection.properties.FieldsExtractor;

public class ReflectiveComparisonPerformer extends AbstractComparisonPerformer<Object> {

	public final ArrayComparisonPerformer<Object> ARRAY_DELEGATE = ComparisonPerformers.array(this);
	private final FieldsExtractor fieldsExtractor = new FieldsExtractor();
	
	public ReflectiveComparisonPerformer() {
		super("ReflectiveEquals");
	}
	
	@Override
	public ComparisonResult compare(Object actual, Object expected) {
		if (actual == expected)
			return new ComparisonResult(actual, expected, this, true);
		if (actual == null || expected == null)
			return new ComparisonResult(actual, expected, this, false);
		if (actual.getClass() != expected.getClass())
			return createClassCheckResult(actual, expected);
		if (isSuitableForDefaultCompare(actual.getClass())) {
			boolean success = DEEP_EQUALS.compare(actual, expected).isSuccess();
			return new ComparisonResult(actual, expected, this, success);
		}
		if (actual instanceof Object[] && expected instanceof Object[]) {
			return ARRAY_DELEGATE.compare((Object[]) actual, (Object[]) expected);
		}
		return performPropertiesComparison(actual, expected);
	}
	
	private ComparisonResult createClassCheckResult(Object actual, Object expected) {
		return new ComplexComparisonResultBuilder<>(Object.class)
				.compare("getClass()", actual.getClass(), expected.getClass(), this)
				.build(actual, expected, this);
	}
	
	private static boolean isSuitableForDefaultCompare(Class<?> type) {
		return type.isPrimitive()
				|| Primitives.isWrapperType(type)
				|| type == String.class
				|| type == Class.class
				|| (type.isArray() && isSuitableForDefaultCompare(type.getComponentType()));
	}
	
	private ComparisonResult performPropertiesComparison(Object actual, Object expected) {
		Map<ClassPropertyKey, ComparisonResult> results = new LinkedHashMap<>();
		for (ClassProperty<?, ?> property : fieldsExtractor.extractAll(actual.getClass()).values())
			if (!property.isStatic())
				results.put(property.getKey(), performPropertyComparison(property, actual, expected));
		return new ComplexComparisonResult(actual, expected, this, results);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ComparisonResult performPropertyComparison(ClassProperty property, Object actual, Object expected) {
		Object actualValue, expectedValue;
		try {
			actualValue = property.getValue(actual);
			expectedValue = property.getValue(expected);
		} catch (Throwable e) {
			return new ComparisonFail(actual, expected, this, e);
		}
		return compare(actualValue, expectedValue);
	}
}
