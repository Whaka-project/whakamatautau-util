package org.whaka.data.shuffle;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Abstract implementation of the {@link RowCollector} interface.
 * Provides functionality to collect rows into a list,
 * if they are validated by an abstract validation method.
 * 
 * <p>Subclasses should implement {@link #isValidRow(int[])} method,
 * providing proper algorithm to validate incoming rows.
 */
public abstract class AbstractRowCollector implements RowCollector {

	/**
	 * Final list in which incoming valid rows will be collected.
	 */
	protected final LinkedList<int[]> rows = new LinkedList<>();

	@Override
	public boolean addRowIfValid(int[] row) {
		return isValidRow(row) && rows.add(row);
	}

	@Override
	public List<int[]> getRows() {
		return Collections.unmodifiableList(rows);
	}
}
