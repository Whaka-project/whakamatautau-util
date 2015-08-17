package org.whaka.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * <b>Note:</b> methods
 * <ul>
 * 	<li>{@link #containsAll(Collection, Collection, BiPredicate)}
 * 	<li>{@link #containsAny(Collection, Collection, BiPredicate)}
 * 	<li>{@link #containsEqualElements(Collection, Collection, BiPredicate)}
 * </ul>
 * Accept specific predicate that will be used to compare elements equality. But methods:
 * <ul>
 * 	<li>{@link #containsAll(Collection, Collection)}
 * 	<li>{@link #containsAny(Collection, Collection)}
 * 	<li>{@link #containsEqualElements(Collection, Collection)}
 * </ul>
 * Use default 'deep-equal' predicate. See: {@link UberCollections#deepEqualsPredicate()}
 */
public class UberCollections {
	
	private UberCollections() {
	}
	
	/**
	 * Elements order is not important
	 */
	public static <T> boolean containsEqualElements(Collection<? extends T> col1, Collection<? extends T> col2) {
		return containsEqualElements(col1, col2, UberCollections.deepEqualsPredicate());
	}

	/**
	 * Elements order is not important
	 */
	public static <T> boolean containsEqualElements(Collection<? extends T> col1, Collection<? extends T> col2, BiPredicate<T, T> predicate) {
		if ((col1 == null || col2 == null) && col1 != col2)
			return false;
		if (col1 == col2)
			return true;
		return col1.size() == col2.size() && containsAll(col1, col2, predicate);
	}
	
	/**
	 * Equal to {@link #containsAny(Collection, Collection, BiPredicate)} with element wrapped into singleton list.
	 */
	public static <T> boolean contains(Collection<? extends T> col, T element, BiPredicate<T, T> predicate) {
		return containsAny(col, Collections.singleton(element), predicate);
	}
	
	public static <T> boolean containsAny(Collection<? extends T> col, Collection<? extends T> anyOf) {
		return containsAny(col, anyOf, UberCollections.deepEqualsPredicate());
	}
	
	public static <T> boolean containsAny(Collection<? extends T> col, Collection<? extends T> anyOf, BiPredicate<T, T> predicate) {
		if (anyOf.isEmpty())
			return true;
		for (T o : anyOf)
			for (T t : col)
				if (predicate.test(t, o))
					return true;
		return false;
	}
	
	public static <T> boolean containsAll(Collection<? extends T> col, Collection<? extends T> allOf) {
		return containsAll(col, allOf, UberCollections.deepEqualsPredicate());
	}
	
	public static <T> boolean containsAll(Collection<? extends T> col, Collection<? extends T> allOf, BiPredicate<T, T> predicate) {
		List<T> list = new ArrayList<>(col);
		for (T item : allOf) {
			int idx = UberLists.getIndex(list, item, predicate);
			if (idx < 0)
				return false;
			list.remove(idx);
		}
		
		return true;
	}
	
	public static String toString(Collection<?> collection) {
		return collection == null ? "null" : UberArrays.toString(collection.toArray());
	}
	
	public static Object[] toArrayRecursive(Collection<?> col) {
		return UberArrays.eliminateCollections(col.toArray());
	}

	/**
	 * @return {@link #DEEP_EQUALS_PREDICATE}
	 */
	@SuppressWarnings("unchecked")
	public static <T> BiPredicate<T, T> deepEqualsPredicate() {
		return DEEP_EQUALS_PREDICATE;
	}
	
	/**
	 * If both objects are collections - {@link #containsEqualElements(Collection, Collection, BiPredicate)} is
	 * performed with this predicate. Otherwise - {@link Objects#deepEquals(Object, Object)} is performed.
	 */
	@SuppressWarnings("rawtypes")
	public static final BiPredicate DEEP_EQUALS_PREDICATE = new BiPredicate() {
		@Override
		@SuppressWarnings("unchecked")
		public boolean test(Object a, Object b) {
			if (a instanceof Collection<?> && b instanceof Collection<?>)
				return containsEqualElements((Collection<Object>)a, (Collection<Object>)b, this);
			return Objects.deepEquals(a, b);
		}
	};
}
