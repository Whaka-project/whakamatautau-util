package com.whaka.data;

import static com.google.common.base.Preconditions.*;
import static com.whaka.util.UberStreams.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import com.google.common.base.MoreObjects;

/**
 * <p>Represents a single row of data in the table-like structure of the {@link Rows}.
 * 
 * <p>Each row is an ordered collection of {@link RowEntry} instances of various type.
 * The main rule is that row cannot have two entries with the same {@link ColumnKey}.
 * 
 * <p><b>Note:</b> class doesn't override default hash-code and equals functionality!
 * Row is immutable and unique "by instance" and may be compared by links.
 */
public final class Row implements Iterable<RowEntry<?>> {

	public final List<RowEntry<?>> entries;
	
	/**
	 * @throws IllegalArgumentException if specified entries is <code>null</code>, empty,
	 * or contain a <code>null</code> value. Or if any two entries in the specified collection
	 * contain the same {@link ColumnKey}.
	 */
	public Row(Collection<RowEntry<?>> entries) {
		checkArgument(entries != null && !entries.isEmpty(), "Row data cannot be null or empty!");
		checkArgument(!entries.contains(null), "Row data cannot contain null values!");
		long distinctKeys = stream(entries).map(RowEntry::getKey).distinct().count();
		checkArgument(entries.size() == distinctKeys, "Keys duplication in the row data!");
		this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
	}
	
	public boolean isPresent(ColumnKey<?> key) {
		return stream(entries).anyMatch(keyMatcher(key));
	}
	
	/**
	 * @throws NoSuchElementException if there's no {@link RowEntry} with the specified column key.
	 * @see #isPresent(ColumnKey)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(ColumnKey<T> key) {
		return (T) stream(entries).find(keyMatcher(key))
			.orElseThrow(() -> new NoSuchElementException("No such column: " + key))
			.getValue();
	}
	
	private static Predicate<RowEntry<?>> keyMatcher(ColumnKey<?> key) {
		return value -> value.getKey() == key;
	}

	public List<RowEntry<?>> getEntries() {
		return entries;
	}
	
	@Override
	public Iterator<RowEntry<?>> iterator() {
		return entries.iterator();
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(entries)
				.toString();
	}
}
