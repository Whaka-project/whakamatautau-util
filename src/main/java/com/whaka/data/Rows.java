package com.whaka.data;

import static com.google.common.base.Preconditions.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.MoreObjects;

/**
 * <p>Class represents a table-like structure as an ordered list of {@link Row} instances.
 * Each row contains a list of entries, and each entry is represented by a {@link ColumnKey} instance.
 * 
 * <p><b>Note:</b> class doesn't override default hash-code and equals functionality!
 * Rows is immutable and unique "by instance" and may be compared by links.
 */
public final class Rows implements Iterable<Row> {

	public final List<Row> rows;
	
	public Rows(Collection<Row> rows) {
		checkArgument(rows == null || !rows.contains(null), "Row cannot be null!");
		this.rows = rows == null || rows.isEmpty() ?
				emptyList() : unmodifiableList(new ArrayList<>(rows));
	}
	
	public List<Row> getRows() {
		return rows;
	}
	
	@Override
	public Iterator<Row> iterator() {
		return getRows().iterator();
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(rows)
				.toString();
	}
}
