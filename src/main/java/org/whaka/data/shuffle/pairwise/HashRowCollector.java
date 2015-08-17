package org.whaka.data.shuffle.pairwise;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.primitives.Booleans;
import org.whaka.data.shuffle.AbstractRowCollector;

/**
 * <p>PairWising collector that uses pair 'hashes' to filter distinct rows.
 * 
 * <p>Row validity in this case means that row has any unique element pairs, comparing to all the already
 * collected rows (see documentation for the {@link PairWise} class, for more info on pairwising).
 * 
 * <p>Core functionality is that collector matches each added row against all the rows already collected.
 * Only if row contains at least one unique pair of elements - it is also collected.
 * 
 * @see CleanUp CleanUp - util allowing to perform additional clean-up upon a hash collector.
 */
class HashRowCollector extends AbstractRowCollector {
	
	private final LinkedList<long[]> hashes = new LinkedList<>();
	private final AtomicBoolean cleanUpRequired = new AtomicBoolean();
	
	/**
	 * Returns true if the specified row has any unique pairs of elements,
	 * comparing to all the already collected rows.
	 */
	@Override
	public boolean isValidRow(int[] row) {
		return row.length < 3 || isUniqueHashPresent(createHash(row), hashes);
	}

	@Override
	public boolean addRowIfValid(int[] row) {
		if (row.length < 3) {
			rows.add(row);
			return true;
		}
		long[] hash = createHash(row);
		if (isUniqueHashPresent(hash, hashes)) {
			rows.add(row);
			hashes.add(hash);
			cleanUpRequired.set(true);
			return true;
		}
		return false;
	}
	
	@Override
	public List<int[]> getRows() {
		if (cleanUpRequired.compareAndSet(true, false))
			CleanUp.FULL_CLEAN_UP.performCleanUp(this);
		return super.getRows();
	}
	
	/**
	 * Returns <code>true</code> if specified array contains at least one elements that is not contained in
	 * all the arrays in the specified list.
	 */
	private static boolean isUniqueHashPresent(long[] hash, List<long[]> hashes) {
		boolean[] matches = new boolean[hash.length];
		for (long[] next : hashes) {
			if (next == hash)
				continue;
			for (int i = 0; i < hash.length; i++)
				if (!matches[i])
					matches[i] = hash[i] == next[i];
			if (!Booleans.contains(matches, false))
				return false;
		}
		return true;
	}
	
	/**
	 * Creates array of longs, were each long represents each pair of ints in the specified array.
	 * Each long is unique for each unique pair of ints, for it can represent the same number of bits without
	 * any overlap.
	 */
	private static long[] createHash(int[] row) {
		long[] hash = new long[countPairs(row.length)];
		for (int a = 0, index = 0; a < row.length - 1; a++)
			for (int b = a + 1; b < row.length; b++)
				hash[index++] = combine(row[a], row[b]);
		return hash;
	}
	
	/**
	 * Method counts number of pairs in a row of the specified size, as "row sum" of the size,
	 * where "row sum" is the sum of the set: [0, 1, 2, 3, ..., rowSize - 1]. Example:
	 * <pre>
	 * 	In row of size 1 - sum of the set [0] - zero pairs
	 * 	In row of size 2 - sum of the set [0, 1] - 1 pair
	 * 	In row of size 3 - sum of the set [0, 1, 2] - 3 pairs
	 * </pre>
	 */
	private static int countPairs(int rowSize) {
		return (rowSize * (rowSize-1)) / 2;
	}
	
	/**
	 * Combines two ints into one long, unique for the specified pair.
	 */
	private static long combine(int a, int b) {
		return ((long) a) << 32 | b & 0xFFFFFFFFL;
	}
	
	private enum CleanUp {
		
		/**
		 * No clean-up performed.
		 */
		NO_CLEAN_UP {
			@Override
			protected void performCleanUp(HashRowCollector collector) {}
		},
		
		/**
		 * <p>All collected rows are iterated top-to-bottom once and each row is matched against all the next rows.
		 * Strategy is mirroring core behavior of the collector, which match rows bottom-to-top when each row
		 * is added. Example:
		 * <pre>
		 * 	// Collected rows:
		 * 	[0,0,0]
		 * 	[0,0,1]
		 * 	[0,1,0]
		 * 	[0,1,1]
		 * 	[1,0,0]
		 * 	[1,0,1]
		 * 	[1,1,0]
		 * 
		 * 	// Clean-up performed:
		 * 	[0,0,1]
		 * 	[0,1,0]
		 * 	[0,1,1]
		 * 	[1,0,0]
		 * 	[1,0,1]
		 * 	[1,1,0]
		 * </pre>
		 * First row contained no unique pairs compared to all the next rows, so it was removed.
		 * 
		 * <p>In most cases this policy produces less optimal result than {@link #FULL_CLEAN_UP}, but sometimes
		 * it may produce the same or even less amount of rows. Though the elements distribution will be farther
		 * from the "ideal" orthogonal array. Policy takes less time, than full clean-up.
		 */
		SEQUENTIAL_CLEAN_UP {
			@Override
			protected void performCleanUp(HashRowCollector collector) {
				List<long[]> hashes = collector.hashes;
				Iterator<int[]> rowsIterator = collector.rows.iterator();
				Iterator<long[]> hashIterator = collector.hashes.iterator();
				int index = 1;
				while (hashIterator.hasNext()) {
					rowsIterator.next();
					if (isUniqueHashPresent(hashIterator.next(), hashes.subList(index, hashes.size()))) {
						index++;
					}
					else {
						rowsIterator.remove();
						hashIterator.remove();
					}
				}
			}
		},
		
		/**
		 * <p>All collected rows are iterated bottom-to-top once and each row is matched against all the other rows.
		 * <pre>
		 * 	// Collected rows:
		 * 	[0,0,0]
		 * 	[0,0,1]
		 * 	[0,1,0]
		 * 	[0,1,1]
		 * 	[1,0,0]
		 * 	[1,0,1]
		 * 	[1,1,0]
		 * 
		 * 	// Clean-up performed:
		 * 	[0,0,0]
		 * 	[0,1,1]
		 * 	[1,0,1]
		 * 	[1,1,0]
		 * </pre>
		 * Perfect orthogonal array is produces as a result.
		 * 
		 * <p>Of all policies this one produces the result closest to the perfect orthogonal array.
		 */
		FULL_CLEAN_UP {
			@Override
			protected void performCleanUp(HashRowCollector collector) {
				Iterator<int[]> rowsIterator = collector.rows.descendingIterator();
				Iterator<long[]> hashIterator = collector.hashes.descendingIterator();
				while (hashIterator.hasNext()) {
					rowsIterator.next();
					if (!isUniqueHashPresent(hashIterator.next(), collector.hashes)) {
						rowsIterator.remove();
						hashIterator.remove();
					}
				}
			}
		},
		;
		
		protected abstract void performCleanUp(HashRowCollector collector);
	}
}