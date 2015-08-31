package org.whaka.util.function;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * <p>Final immutable container for 3 objects of different types.
 * 
 * <p>Values might be accessed directly: {@link #_1} and {@link #_2} and {@link #_3},
 * or thru getters: {@link #get_1()} and {@link #get_2()} and {@link #get_3()}.
 * 
 * @see #tuple3(Object, Object, Object)
 * @see Tuple2
 * @see Tuple4
 * @see Tuple5
 */
public final class Tuple3<A,B,C> {

	public final A _1;
	public final B _2;
	public final C _3;
	
	public Tuple3(A _1, B _2, C _3) {
		this._1 = _1;
		this._2 = _2;
		this._3 = _3;
	}
	
	/**
	 * Factory method to create new instance of the immutable tuple.
	 */
	public static <A,B,C> Tuple3<A, B, C> tuple3(A _1, B _2, C _3) {
		return new Tuple3<>(_1, _2, _3);
	}
	
	/**
	 * Value of the {@link #_1} field
	 */
	public A get_1() {
		return _1;
	}
	
	/**
	 * Value of the {@link #_2} field
	 */
	public B get_2() {
		return _2;
	}

	/**
	 * Value of the {@link #_3} field
	 */
	public C get_3() {
		return _3;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_1, _2, _3);
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			Tuple3<?,?,?> that = (Tuple3<?,?,?>) object;
			return Objects.equals(this._1, that._1)
					&& Objects.equals(this._2, that._2)
					&& Objects.equals(this._3, that._3);
		}
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(_1)
				.addValue(_2)
				.addValue(_3)
				.toString();
	}
}
