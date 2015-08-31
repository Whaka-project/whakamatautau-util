package org.whaka.util.function;

import java.util.Map;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * <p>Final immutable container for 2 objects of different types.
 * 
 * <p>Values might be accessed directly: {@link #_1} and {@link #_2},
 * or thru getters: {@link #get_1()} and {@link #get_2()}.
 * 
 * <p>Class also implements {@link java.util.Map.Entry}, so getters:
 * {@link #getKey()} and {@link #getValue()} are available.
 * 
 * @see #tuple2(Object, Object)
 * @see Tuple3
 * @see Tuple4
 * @see Tuple5
 */
public final class Tuple2<A,B> implements Map.Entry<A, B> {

	public final A _1;
	public final B _2;
	
	public Tuple2(A _1, B _2) {
		this._1 = _1;
		this._2 = _2;
	}
	
	/**
	 * Factory method to create new instance of the immutable tuple.
	 */
	public static <A,B> Tuple2<A, B> tuple2(A _1, B _2) {
		return new Tuple2<>(_1, _2);
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
	 * Equal to {@link #get_1()}
	 */
	@Override
	public A getKey() {
		return _1;
	}
	
	/**
	 * Equal to {@link #get_2()}
	 */
	@Override
	public B getValue() {
		return _2;
	}

	@Override
	public B setValue(B value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		return Objects.hash(_1, _2);
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			Tuple2<?,?> that = (Tuple2<?,?>) object;
			return Objects.equals(this._1, that._1)
					&& Objects.equals(this._2, that._2);
		}
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(_1)
				.addValue(_2)
				.toString();
	}
}
