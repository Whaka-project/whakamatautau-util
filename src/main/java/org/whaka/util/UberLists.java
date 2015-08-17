package org.whaka.util;

import java.util.List;
import java.util.function.BiPredicate;

public class UberLists {

	private UberLists() {
	}
	
	public static <T> int getIndex(List<T> list, T item) {
		return getIndex(list, item, UberCollections.deepEqualsPredicate());
	}
	
	public static <T> int getIndex(List<T> list, T item, BiPredicate<T, T> predicate) {
		for (int i = 0; i < list.size(); i++)
			if (predicate.test(list.get(i), item))
				return i;
		return -1;
	}
}
