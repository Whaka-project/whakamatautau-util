package com.whaka.data.shuffle;

import static com.google.common.base.Preconditions.*;
import static com.whaka.util.UberStreams.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.whaka.data.Column;
import com.whaka.data.ColumnKey;
import com.whaka.data.Columns;
import com.whaka.data.Row;
import com.whaka.data.RowEntry;
import com.whaka.data.Rows;

/**
 * <p>Implementation of a {@link Shuffle} type, that delegates core calculations to a specified function.
 * 
 * <p>For each specified {@link Columns} this shuffle creates an <b>int</b> array,
 * representing sizes of the column dictionaries. For example, if specified columns contains 3 {@link Column}
 * instances, and each of them contains a dictionary of 3 elements - generated array will look like this: [3, 3, 3].
 * 
 * <p>Created array is delegated to specified "index calculator" function of type <code>(int[] -> int[][])</code>.
 * Int array of double depth, created by the function then transformed into {@link Rows} instance so that
 * each row (<code>int[]</code>) represents single {@link Row} instance, and each <b>int</b> in this row represents
 * an element with the same index in the dictionary of the corresponding column. Example (pseudo code):
 * <pre>
 * 	// there're 3 columns with 3 elements each, like this:
 *	col1 = ["qwe", "rty", "qaz"]
 *	col2 = ['a', 'b', 'c']
 *	col3 = [true, false, null]
 *
 *	// index shuffle creates a "size array":
 *	int[] sizes = [3, 3, 3]
 *
 *	// then function produces an "index map", where each index,
 *	// is between 0 (inclusive), and a "size array" element (exclusive):
 *	int[][] indexes = [
 *		[0, 0, 0]
 *		[1, 1, 1]
 *		[2, 2, 2]
 *	]
 *
 *	// Columns object is indexed, so each input column can be represented by an index
 *	// So index shuffle takes result "index map" and transforms it into rows, like this:
 *	row = [col1, col2, col3]
 *
 *	// Each element of the int[] row corresponds to specific column
 *	// and contains an index of an element, from the column's dictionary:
 *	row1 = ["qwe", 'a', true]
 *	row2 = ["rty", 'b', false]
 *	row3 = ["qaz", 'c', null]
 * </pre>
 * 
 * <b>Note:</b> "index map" returned by a delegate function might contain negative indexes.
 * In this case value will be ignored and result {@link Row} instance will not contain a {@link RowEntry}
 * with corresponding {@link ColumnKey}. This might happen in case of an empty column in the input data.
 */
public class IndexShuffle implements Shuffle {

	private final Function<int[], int[][]> indexCalculator;
	
	/**
	 * @throws NullPointerException if specified index calculator function is <code>null</code>
	 */
	public IndexShuffle(Function<int[], int[][]> indexCalculator) {
		this.indexCalculator = Objects.requireNonNull(indexCalculator, "Index calculator function cannot be null!");
	}
	
	public Function<int[], int[][]> getIndexCalculator() {
		return indexCalculator;
	}
	
	@Override
	public Rows apply(Columns cols) {
		checkArgument(!cols.getColumns().isEmpty(), "Cannot shuffle empty columns!");
		int[] columnSizes = stream(cols).map(Column::getData).mapToInt(List::size).toArray();
		int[][] rowsIndexes = indexCalculator.apply(columnSizes);
		List<Row> rows = stream(rowsIndexes).map(arr -> createRow(arr, cols)).toList();
		return new Rows(rows);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Row createRow(int[] rowIndexes, Columns cols) {
		List<RowEntry<?>> data = new ArrayList<>();
		for (int i = 0; i < rowIndexes.length; i++) {
			if (rowIndexes[i] < 0)
				continue;
			Column<?> col = cols.getColumns().get(i);
			Object value = col.getData().get(rowIndexes[i]);
			data.add(new RowEntry(col.getKey(), value));
		}
		return new Row(data);
	}
}
