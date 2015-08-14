package com.whaka.data;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * <p>Represents a "cell" in a table-like structure of the {@link Row} and {@link Rows}.
 * 
 * <p>Each entry is identified by an instance of the {@link ColumnKey},
 * and contains a single value of the corresponding type.
 * 
 * <p><b>Note:</b> class doesn't override default hash-code and equals functionality!
 * Entries are immutable and unique "by instance" and may be compared by links.
 */
public final class RowEntry<T> {

	public final ColumnKey<T> key;
	public final T value;
	
	/**
	 * @throws NullPointerException if specified key is <code>null</code>
	 */
	public RowEntry(ColumnKey<T> key, T value) {
		this.key = Objects.requireNonNull(key, "Column key cannot be null!");
		this.value = value;
	}
	
	public ColumnKey<T> getKey() {
		return key;
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(getKey())
				.addValue(getValue())
				.toString();
	}
}
