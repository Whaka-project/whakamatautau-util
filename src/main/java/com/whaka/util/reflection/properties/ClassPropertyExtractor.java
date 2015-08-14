package com.whaka.util.reflection.properties;

import java.util.Map;

public interface ClassPropertyExtractor<P extends ClassProperty<?, ?>> {

	public Map<ClassPropertyKey, P> extractAll(Class<?> target);
}
