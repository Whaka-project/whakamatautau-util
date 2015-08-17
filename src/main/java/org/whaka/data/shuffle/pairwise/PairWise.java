package org.whaka.data.shuffle.pairwise;

import java.util.function.Function;

import org.whaka.data.Columns;
import org.whaka.data.Rows;
import org.whaka.data.shuffle.IndexShuffle;
import org.whaka.data.shuffle.Shuffle;

/**
 * 
 * @see #SEQUENTIAL
 */
public final class PairWise implements Shuffle {

	/**
	 * <p>PairWising shuffle, performing sequential filtering of the multiplied data.
	 * 
	 * @see #sequential(Columns)
	 * @see SequentialStrategy
	 */
	public static final PairWise SEQUENTIAL = new PairWise(SequentialStrategy.INSTANCE);
	
	private final Shuffle delegate;
	
	private PairWise(Function<int[], int[][]> strategy) {
		this(new IndexShuffle(strategy));
	}
	
	private PairWise(Shuffle delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public Rows apply(Columns t) {
		return delegate.apply(t);
	}
	
	/**
	 * <p>Performs {@link Shuffle} operation using {@link #SEQUENTIAL} instance.
	 * <p>Usability method for more comfortable manual use.
	 */
	public static Rows sequential(Columns cols) {
		return SEQUENTIAL.apply(cols);
	}
}
