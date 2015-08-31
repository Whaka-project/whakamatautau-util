package org.whaka.util.function;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * <p>Final immutable container for 4 objects of different types.
 * 
 * <p>Values might be accessed directly: {@link #_1} and {@link #_2} and {@link #_3} and {@link #_4},
 * or thru getters: {@link #get_1()} and {@link #get_2()} and {@link #get_3()} and {@link #get_4()}.
 * 
 * @see #tuple4(Object, Object, Object, Object)
 * @see Tuple2
 * @see Tuple3
 * @see Tuple5
 */
public final class Tuple4<A,B,C,D> {

	public final A _1;
	public final B _2;
	public final C _3;
	public final D _4;
	
	public Tuple4(A _1, B _2, C _3, D _4) {
		this._1 = _1;
		this._2 = _2;
		this._3 = _3;
		this._4 = _4;
	}
	
	/**
	 * Factory method to create new instance of the immutable tuple.
	 */
	public static <A,B,C,D> Tuple4<A,B,C,D> tuple4(A _1, B _2, C _3, D _4) {
		return new Tuple4<>(_1, _2, _3, _4);
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
	
	/**
	 * Value of the {@link #_4} field
	 */
	public D get_4() {
		return _4;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(_1, _2, _3, _4);
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			Tuple4<?,?,?,?> that = (Tuple4<?,?,?,?>) object;
			return Objects.equals(this._1, that._1)
					&& Objects.equals(this._2, that._2)
					&& Objects.equals(this._3, that._3)
					&& Objects.equals(this._4, that._4);
		}
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(_1)
				.addValue(_2)
				.addValue(_3)
				.addValue(_4)
				.toString();
	}
}
