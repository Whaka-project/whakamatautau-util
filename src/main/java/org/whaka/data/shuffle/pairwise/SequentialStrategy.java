package org.whaka.data.shuffle.pairwise;

import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.*;

import java.util.List;
import java.util.function.Function;

import org.whaka.data.shuffle.IndexShuffle;

/**
 * <p>Class implements PairWising strategy as "index mapping" function <code>(int[] -> int[][])</code>
 * according to {@link IndexShuffle}
 * 
 * <p>Strategy sets <b>speed</b> as it's primary goal, so it may provide "untrue" pairwising, meaning that
 * it might miss some pairs. But the strategy is extremely <b>useful in terms of time-value ratio</b>,
 * especially for larger sets of data, when production of the <i>"perfect"</i> collection becomes almost impossible.
 * (See documentation to the {@link PairWise} class, on the subject of <i>"true"</i> pairwising and <i>"perfect"</i> collection).
 * 
 * <p>To minimize number of pair comparisons required to filter out all the "invalid" rows this strategy
 * performs filter operation after multiplying each next column, rather than building the whole
 * multiplication table, and filtering it once. Example:
 * <pre>
 * 	// input "sizes array" shows that we have 4 columns with 2 possible elements each
 * 	int[] sizes = [2,2,2,2]
 * 
 * 	// strategy creates "full multiplication" of the first column:
 * 	int[][] rows = [
 * 		[0],
 * 		[1],
 * 	]
 * 
 * 	// then strategy takes each next column, and performs "full multiplication" with existing rows
 * 	// also performing filter operation on each multiplication.
 * 	int[][] rows = [
 * 		[0, 0],
 * 		[0, 1],
 * 		[1, 0],
 * 		[1, 1],
 * 	]
 * 
 * 	// All pairs are unique - nothing is filtered out. Next column:
 * 	int[][] rows = [
 * 		[0, 0, 0],
 * 		[0, 1, 1],
 * 		[1, 0, 1],
 * 		[1, 1, 0],
 * 	]
 * 
 * 	// Next column:
 * 	int[][] rows = [
 * 		[0, 0, 0, 0],
 * 		[0, 0, 0, 1],
 * 		[0, 1, 1, 0],
 * 		[0, 1, 1, 1],
 * 		[1, 0, 1, 1],
 * 		[1, 1, 0, 0],
 * 	]
 * </pre>
 * In this example result map represents "true" pairwise, where all possible combinations are present for any
 * selected pair of indexes. And it's pretty close to an <i>"ideal"</i> orthogonal array.
 * 
 * @see #INSTANCE
 * @see PairWise
 */
public class SequentialStrategy implements Function<int[], int[][]> {

	/**
	 * Just an instance of the class. Currently strategy has no state, so all instances act equally.
	 */
	public static final SequentialStrategy INSTANCE = new SequentialStrategy();
	
	@Override
	public int[][] apply(int[] sizes) {
		if (sizes.length == 0)
			return new int[0][0];
		List<int[]> rows =  range(0, sizes[0]).mapToObj(i->new int[]{i}).collect(toList());
		for (int column = 1; column < sizes.length; column++)
			rows = appendColumn(rows, column, sizes);
		return rows.toArray(new int[rows.size()][]);
	}
	
	private static List<int[]> appendColumn(List<int[]> rows, int column, int[] sizes) {
		HashRowCollector rowCollector = new HashRowCollector();
		int[] newrow = new int[column + 1];
		for (int[] row : rows) {
			System.arraycopy(row, 0, newrow, 0, row.length);
			if (sizes[column] > 0) {
				for (int i = 0; i < sizes[column]; i++) {
					newrow[newrow.length - 1] = i;
					rowCollector.addRowIfValid(newrow.clone());
				}
			}
			else {
				newrow[newrow.length - 1] = -1;
				rowCollector.addRowIfValid(newrow.clone());
			}
		}
		return rowCollector.getRows();
	}
}
