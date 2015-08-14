package com.whaka.util;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiPredicate;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Comparators {

	private Comparators() {
	}
	
	/**
	 * Creates bi-predicate that calls #compare on specified comparator for each pair of arguments and returns
	 * <code>true</code> if comparator returns <code>0</code>
	 */
	public static <T extends Comparable<T>> BiPredicate<T, T> comparePredicate(Comparator<T> comparator) {
		Objects.requireNonNull(comparator, "Comparator cannot be null!");
		return (a,b) -> comparator.compare(a, b) == 0;
	}
	
	public static <T extends Comparable<T>> Comparator<T> nullsStart() {
		return NULLS_START_COMPARATOR;
	}
	
	public static <T extends Comparable<T>> Comparator<T> nullsEnd() {
		return NULLS_END_COMPARATOR;
	}
	
	private static final Comparator NULLS_START_COMPARATOR = (a,b) -> {
		if (a == b)
			return 0;
		if (a == null)
			return -1;
		if (b == null)
			return 1;
		return Integer.signum(((Comparable)a).compareTo(b));
	};
	
	private static final Comparator NULLS_END_COMPARATOR = (a,b) -> {
		if (a == b)
			return 0;
		if (a == null)
			return 1;
		if (b == null)
			return -1;
		return Integer.signum(((Comparable)a).compareTo(b));
	};
}
