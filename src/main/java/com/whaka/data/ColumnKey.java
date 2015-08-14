package com.whaka.data;

import static com.google.common.base.MoreObjects.*;
import static java.lang.String.*;
import static java.lang.System.*;
import static java.util.Objects.*;

import com.google.common.base.MoreObjects;

/**
 * <p>Identifier to be used with classes: {@link Column} and {@link Columns}.
 * 
 * <p>Represents unique types keys to identify data columns.
 * Allows optionally to set a string name for a key.
 * 
 * <p><b>Note:</b> class doesn't override default hash-code and equals functionality!
 * Keys are immutable and unique "by instance" and may be compared by links.
 */
public final class ColumnKey<T> {

	public final Class<T> type;
	public final String name;

	/**
	 * Equal to {@link #ColumnKey(Class, String)} with <code>null</code> specified as name.
	 */
	public ColumnKey(Class<T> type) {
		this(type, null);
	}
	
	/**
	 * @param type required to be non-null
	 * @param name if null - string representation of the identity hash code will be used as a name
	 */
	public ColumnKey(Class<T> type, String name) {
		this.type = requireNonNull(type, "Column key type cannot be null!");
		this.name = firstNonNull(name, valueOf(identityHashCode(this)));
	}
	
	public Class<T> getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(type.getSimpleName())
				.addValue(name)
				.toString();
	}
}
