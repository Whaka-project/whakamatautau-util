package org.whaka.data;

import static java.util.Collections.*;
import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.base.MoreObjects;

/**
 * <p>Class represents a column of data and to be used with the {@link Columns}.
 * <p>Each column is identified by an instance of the {@link ColumnKey} with the same type
 * and contains a collection of 'data'. Column doesn't perform any assertion or validation of the data,
 * it just holds it.
 * 
 * <p><b>Note:</b> class doesn't override default hash-code and equals functionality.
 * Each column is immutable and unique "by instance" so they can be compared by links.
 */
public final class Column<T> {
		public final ColumnKey<T> key;
	public final List<T> data;
	
	/**
	 * Equal to {@link #Column(ColumnKey, Collection)} with specified data
	 * wrapped into {@link Arrays#asList(Object...)}
	 */
	@SafeVarargs
	public Column(ColumnKey<T> key, T... data) {
		this(key, Arrays.asList(data));
	}
	
	/**
	 * If specified collection is <code>null</code> - result column will contain empty list as data.
	 * 
	 * @throws NullPointerException if specified key is null
	 */
	public Column(ColumnKey<T> key, Collection<T> data) {
		this.key = requireNonNull(key, "Column key cannot be null!");
		this.data = data == null || data.isEmpty() ?
			emptyList() : unmodifiableList(new ArrayList<>(data));
	}
	
	@SafeVarargs
	public static <A> Column<A> create(Class<A> type, A... data) {
		return create(type, Arrays.asList(data));
	}
	
	public static <A> Column<A> create(Class<A> type, Collection<A> data) {
		return new Column<>(new ColumnKey<>(type), data);
	}

	/**
	 * Equal to #getKey().getType()
	 */
	public Class<T> getType() {
		return key.type;
	}
	
	public ColumnKey<T> getKey() {
		return key;
	}
	
	public List<T> getData() {
		return data;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(getKey())
				.addValue(getData())
				.toString();
	}
}
