package org.whaka.util.function;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * <p>Final immutable container for 5 objects of different types.
 * 
 * <p>Values might be accessed directly: {@link #_1} and {@link #_2} and {@link #_3} and {@link #_4} and {@link #_5},
 * or thru getters: {@link #get_1()} and {@link #get_2()} and {@link #get_3()} and {@link #get_4()} and {@link #get_5()}.
 * 
 * @see #tuple5(Object, Object, Object, Object, Object)
 * @see Tuple2
 * @see Tuple3
 * @see Tuple4
 */
public final class Tuple5<A,B,C,D,E> {

	public final A _1;
	public final B _2;
	public final C _3;
	public final D _4;
	public final E _5;
	
	public Tuple5(A _1, B _2, C _3, D _4, E _5) {
		this._1 = _1;
		this._2 = _2;
		this._3 = _3;
		this._4 = _4;
		this._5 = _5;
	}
	
	/**
	 * Factory method to create new instance of the immutable tuple.
	 */
	public static <A,B,C,D,E> Tuple5<A,B,C,D,E> tuple5(A _1, B _2, C _3, D _4, E _5) {
		return new Tuple5<>(_1, _2, _3, _4, _5);
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
	
	/**
	 * Value of the {@link #_5} field
	 */
	public E get_5() {
		return _5;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(_1, _2, _3, _4, _5);
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			Tuple5<?,?,?,?,?> that = (Tuple5<?,?,?,?,?>) object;
			return Objects.equals(this._1, that._1)
					&& Objects.equals(this._2, that._2)
					&& Objects.equals(this._3, that._3)
					&& Objects.equals(this._4, that._4)
					&& Objects.equals(this._5, that._5);
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
				.addValue(_5)
				.toString();
	}
}
