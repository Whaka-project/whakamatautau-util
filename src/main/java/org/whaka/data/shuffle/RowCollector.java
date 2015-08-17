package org.whaka.data.shuffle;

import java.util.List;

/**
 * Each instance of a row collector able to store a state of a numerous 'rows', represented by int arrays.
 * Row collector might be used by various index calculators to store intermediate state while calculating all the rows.
 */
public interface RowCollector {

	/**
	 * If this method returns <code>true</code> for a specific row - then immediate call
	 * of the {@link #addRowIfValid(int[])} for the same row should also return <code>true</code>
	 * (if no other rows were added inbetween method calls).
	 */
	boolean isValidRow(int[] row);
	
	/**
	 * <p>This method returns <code>true</code> if specified row is valid and added to the collection,
	 * or <code>false</code> otherwise.
	 * 
	 * <p>If {@link #isValidRow(int[])} returns <code>true</code> for a specific row, then immediate call to this
	 * method should also be successful for the same row (if no other rows were added inbetween method calls).
	 */
	boolean addRowIfValid(int[] row);
	
	/**
	 * Returns all the collected rows. All rows for which {@link #addRowIfValid(int[])} returned true should be
	 * present in the result collection.
	 */
	List<int[]> getRows();
}
