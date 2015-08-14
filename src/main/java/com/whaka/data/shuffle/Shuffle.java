package com.whaka.data.shuffle;

import java.util.function.Function;

import com.whaka.data.Columns;
import com.whaka.data.Rows;

/**
 * <p>Type represents a strategy to convert a collection of columns
 * with various data dictionaries, into a table-like rows.
 */
public interface Shuffle extends Function<Columns, Rows> {
}
