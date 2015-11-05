package org.whaka.data;

import static java.util.Arrays.*;
import static org.whaka.util.UberStreams.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * <p>Class provides functional methods to transform arrays or collections to append a null value to it.
 * <p>Each instance provides two methods:
 * <ul>
 * 	<li>{@link #apply(Object[])}
 * 	<li>{@link #apply(Collection)}
 * </ul>
 * Each method applies functionality specific to a strategy. Currently there're these options:
 * <ul>
 * 	<li>{@link #NO_STRATEGY}
 * 	<li>{@link #NULLABLE_START}
 * 	<li>{@link #NULLABLE_END}
 * </ul>
 * Also for manual use there're some static methods available:
 * <ul>
 * 	<li>{@link #nullableStart(Collection)}
 * 	<li>{@link #nullableEnd(Collection)}
 * </ul>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class NullStrategy {

	/**
	 * <p>No strategy is applied.
	 * <ul>
	 * 	<li>{@link #apply(Object[])} specified array will be cloned without any changes.
	 * 	<br/><br/>
	 * 	<li>{@link #apply(Collection)} specified collection will be transformed into a list without any other changes.
	 * </ul>
	 */
	public static NullStrategy NO_STRATEGY = new NullStrategy(ArrayList::new);
	
	/**
	 * <p>If specified set contains <code>null</code> as a first element - nothing happens.
	 * Otherwise - <code>null</code> is appended at the beginning of a set.
	 * 
	 * @see #apply(Object[])
	 * @see #apply(Collection)
	 */
	public static NullStrategy NULLABLE_START = new NullStrategy(NullStrategy::nullableStart);
	
	/**
	 * <p>If specified set contains <code>null</code> as a last element - nothing happens.
	 * Otherwise - <code>null</code> is appended at the end of a set.
	 * 
	 * @see #apply(Object[])
	 * @see #apply(Collection)
	 */
	public static NullStrategy NULLABLE_END = new NullStrategy(NullStrategy::nullableEnd);

	private final Function<Collection, List> function;
	
	private NullStrategy(Function<Collection, List> function) {
		this.function = function;
	}
	
	/**
	 * Specified array is cloned <b>in any case</b>.
	 * Any other behavior is specific to a strategy.
	 */
	public <T> T[] apply(T[] arr) {
		return (T[]) stream(apply(asList(arr))).toArray(arr.getClass().getComponentType());
	}
	
	/**
	 * Specified collection is copied into a list <b>in any case</b>.
	 * Any other behavior is specific to a strategy.
	 */
	public <T> List<T> apply(Collection<T> col) {
		return function.apply(col);
	}
	
	/**
	 * <p>Equal to the {@link #NULLABLE_START} strategy.
	 * <p>Specified {@link Collection} is copied into a list <b>in any case</b>.
	 */
	public static <T> List<T> nullableStart(Collection<T> col) {
		List<T> list = new ArrayList<>(col);
		if (list.isEmpty() || list.get(0) != null)
			list.add(0, null);
		return list;
	}
	
	/**
	 * <p>Equal to the {@link #NULLABLE_END} strategy.
	 * <p>Specified {@link Collection} is copied into a list <b>in any case</b>.
	 */
	public static <T> List<T> nullableEnd(Collection<T> col) {
		List<T> list = new ArrayList<>(col);
		if (list.isEmpty() || list.get(list.size() - 1) != null)
			list.add(null);
		return list;
	}
}