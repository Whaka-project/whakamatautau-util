package org.whaka.util.reflection.properties;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.whaka.util.reflection.UberClasses;

public class FieldsExtractor implements ClassPropertyExtractor<FieldClassProperty<?, ?>> {

	@Override
	public Map<ClassPropertyKey, FieldClassProperty<?,?>> extractAll(Class<?> target) {
		Objects.requireNonNull(target, "Target class cannot be null!");
		return UberClasses.streamAncestors(target)
			.flatMap(FieldsExtractor::streamFields)
			.map(FieldClassProperty::new)
			.collect(Collectors.toMap(ClassProperty::getKey, p -> p, (a,b) -> b, LinkedHashMap::new));
	}
	
	private static Stream<Field> streamFields(Class<?> type) {
		return Stream.of(type.getDeclaredFields());
	}
}