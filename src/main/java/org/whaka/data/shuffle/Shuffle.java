package org.whaka.data.shuffle;

import java.util.function.Function;

import org.whaka.data.Columns;
import org.whaka.data.Rows;

/**
 * <p>Type represents a strategy to convert a collection of columns
 * with various data dictionaries, into a table-like rows.
 */
public interface Shuffle extends Function<Columns, Rows> {
}
