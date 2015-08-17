package org.whaka.data;

import static org.whaka.util.UberStreams.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.base.MoreObjects;

/**
 * Class provides mutable usability functionality to build {@link Columns} instance.
 * 
 * @see #addColumn(Column)
 * @see #addColumn(Column, int)
 * @see #addColumn(ColumnKey, Collection)
 * @see #addColumn(ColumnKey, Object...)
 * @see #removeColumn(ColumnKey)
 * @see #build()
 */
public final class ColumnsBuilder {

	private List<Column<?>> columns = new ArrayList<>();
	
	public boolean isPresent(ColumnKey<?> key) {
		return stream(getColumns()).anyMatch(keyMatcher(key));
	}
	
	/**
	 * Returns a column with the same key as specified, or <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public <T> Column<T> getColumn(ColumnKey<T> key) {
		return (Column<T>) stream(getColumns()).find(keyMatcher(key)).orElse(null);
	}
	
	private static Predicate<Column<?>> keyMatcher(ColumnKey<?> key) {
		return column -> column.getKey() == key;
	}
	
	public List<Column<?>> getColumns() {
		return unmodifiableList(columns);
	}
	
	/**
	 * Equal to the {@link #addColumn(ColumnKey, Collection)} with the specified data
	 * wrapped into {@link Arrays#asList(Object...)}
	 */
	@SuppressWarnings("unchecked")
	public <T> ColumnsBuilder addColumn(ColumnKey<T> key, T... data) {
		return addColumn(key, Arrays.asList(data));
	}
	
	/**
	 * Equal to the {@link #addColumn(Column)} with a new {@link Column} instance being created
	 * using specified key and data.
	 */
	public <T> ColumnsBuilder addColumn(ColumnKey<T> key, Collection<T> data) {
		return addColumn(new Column<>(key, data));
	}
	
	/**
	 * Equal to the {@link #addColumn(Column, int)} with current number of columns specified as index,
	 * so new column will be added at the end of the list.
	 */
	public ColumnsBuilder addColumn(Column<?> column) {
		return addColumn(column, getColumns().size());
	}
	
	/**
	 * <p>Add specified column at the specified in the list.
	 * Index works the same way as in the {@link List#add(int, Object)}.
	 * 
	 * @throws IllegalArgumentException if {@link #isPresent(ColumnKey)} returns <code>true</code>
	 * for the key of the specified column.
	 */
	public ColumnsBuilder addColumn(Column<?> column, int idx) {
		ColumnKey<?> key = column.getKey();
		if (isPresent(key))
			throw new IllegalArgumentException("Column with specified key is already present: " + getColumn(key));
		columns.add(idx, column);
		return this;
	}
	
	/**
	 * Removes column with the specified key from the list.
	 * Returns removed column or <code>null</code> if there was no such column.
	 */
	public <T> Column<T> removeColumn(ColumnKey<T> key) {
		Column<T> column = getColumn(key);
		columns.remove(column);
		return column;
	}
	
	/**
	 * Builder new {@link Columns} instance, using result of the {@link #getColumns()} method as input data.
	 */
	public Columns build() {
		return new Columns(getColumns());
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(getColumns())
				.toString();
	}
}
