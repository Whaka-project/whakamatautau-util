package org.whaka.data;

import static com.google.common.base.Preconditions.*;
import static org.whaka.util.UberStreams.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.base.MoreObjects;

/**
 * <p>Class represents a "table" of data, as ordered collection of the {@link Column} instances.
 * <p>Provides functionality to add columns as already created instances, or as keys and data.
 * Also to get, or remove added columns.
 * 
 * <p><b>Note:</b> class doesn't override default hash-code and equals functionality!
 * Columns is immutable and unique "by instance" and may be compared by links.
 */
public final class Columns implements Iterable<Column<?>> {

	public final List<Column<?>> columns;
	
	/**
	 * Equal to {@link #Columns(Collection)} with specified columns
	 * wrapped into {@link Arrays#asList(Object...)}
	 */
	public Columns(Column<?> ... columns) {
		this(Arrays.asList(columns));
	}
	
	/**
	 * @throws IllegalArgumentException if specified collection contains null value, or if any two columns
	 * in the collection contains the same column key.
	 */
	public Columns(Collection<Column<?>> columns) {
		if (columns == null || columns.isEmpty()) {
			this.columns = Collections.emptyList();
		}
		else {
			checkArgument(!columns.contains(null), "Column cannot be null!");
			long distinctKeys = stream(columns).map(Column::getKey).distinct().count();
			checkArgument(columns.size() == distinctKeys, "Keys duplication in the columns!");
			this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
		}
	}
	
	public boolean isPresent(ColumnKey<?> key) {
		return stream(columns).anyMatch(keyMatcher(key));
	}
	
	/**
	 * Returns a column with the same key as specified, or <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public <T> Column<T> getColumn(ColumnKey<T> key) {
		return (Column<T>) stream(columns).find(keyMatcher(key)).orElse(null);
	}
	
	private static Predicate<Column<?>> keyMatcher(ColumnKey<?> key) {
		return column -> column.getKey() == key;
	}
	
	public List<Column<?>> getColumns() {
		return unmodifiableList(columns);
	}
	
	@Override
	public Iterator<Column<?>> iterator() {
		return getColumns().iterator();
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(columns)
				.toString();
	}
}
