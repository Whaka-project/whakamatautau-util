package org.whaka.util;

import java.util.Optional;
import java.util.function.Function;


public class UberObjects {

	private UberObjects() {
	}
	
	public static String toString(Object o) {
		return UberArrays.toString(o);
	}
	
	public static <T,V> V map(T source, Function<T, V> function) {
		return map(source, function, null);
	}
	
	public static <T,V> V map(T source, Function<T, V> function, V orElse) {
		return Optional.ofNullable(source).map(function).orElse(orElse);
	}
}
