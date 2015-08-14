package com.whaka.util.reflection.properties;

import static com.whaka.util.reflection.UberMethods.*;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.whaka.util.reflection.UberClasses;

public class GettersExtractor implements ClassPropertyExtractor<GetterClassProperty<?, ?>> {

	@Override
	public Map<ClassPropertyKey, GetterClassProperty<?, ?>> extractAll(Class<?> target) {
		Objects.requireNonNull(target, "Target class cannot be null!");
		Predicate<Method> notOverridden = createNotOverriddenPredicate();

		Map<String, List<Method>> gettersByKey =
			UberClasses.streamTypeLinearization(target)
				.flatMap(GettersExtractor::streamGetters)
				.filter(notOverridden)
				.collect(Collectors.groupingBy(m -> m.getDeclaringClass() + m.getName()));
				
		return gettersByKey.values().stream()
				.map(l -> l.stream().max(GettersExtractor::compareByReturnType).get())
				.map(GetterClassProperty::new)
				.collect(Collectors.toMap(ClassProperty::getKey, p -> p));
	}
	
	private static int compareByReturnType(Method a, Method b) {
		Class<?> aReturn = a.getReturnType();
		Class<?> bReturn = b.getReturnType();
		if (aReturn.isAssignableFrom(bReturn))
			return -1;
		if (bReturn.isAssignableFrom(aReturn))
			return 1;
		return 0;
	}
	
	private static Stream<Method> streamGetters(Class<?> type) {
		return Stream.of(type.getDeclaredMethods())
				.filter(m -> m.getParameterCount() == 0 && m.getReturnType() != void.class);
	}
	
	private static Predicate<Method> createNotOverriddenPredicate() {
		List<Method> validExtractedMethods = new LinkedList<>();
		return m -> {
			if (validExtractedMethods.stream().noneMatch(g -> overrides(g, m))) {
				validExtractedMethods.add(m);
				return true;
			}
			return false;
		};
	}
}