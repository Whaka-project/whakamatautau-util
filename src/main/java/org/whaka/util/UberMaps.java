package org.whaka.util;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.MoreObjects;

public class UberMaps {

	private UberMaps() {
	}
	
	/**
	 * <p>Create instance of the {@link UberMaps.Entry} with key and value from the specified entry.
	 * <p><b>Note:</b> result entry is immutable!
	 * @see #entry(Object, Object)
	 */
	public static <K,V> Entry<K, V> entry(Map.Entry<K, V> e) {
		return e instanceof Entry ? (Entry<K,V>) e : entry(e.getKey(), e.getValue());
	}
	
	/**
	 * <p>Create instance of the {@link UberMaps.Entry} that implements {@link java.util.Map.Entry}.
	 * <p><b>Note:</b> result entry is immutable!
	 */
	public static <K,V> Entry<K, V> entry(K key, V val) {
		return new Entry<>(key, val);
	}
	
	
	/**
	 * <p><b>Immutable</b> implementation of {@link java.util.Map.Map.Entry}.
	 * 
	 * <p>Entry also implements {@link Predicate} of a map, and tests whether specified map contains an entry
	 * like this one: {@link Entry#test(Map)}.
	 * 
	 * @see UberMaps#entry(Object, Object)
	 */
	public static final class Entry<K,V> implements Map.Entry<K, V>, Predicate<Map<K, V>>, Cloneable {

		public final K key;
		public final V val;

		public Entry(K key, V val) {
			this.key = key;
			this.val = val;
		}
		
		@Override
		public K getKey() {
			return key;
		}
		
		/**
		 * Equal to {@link #getKey()}
		 */
		public K key() {
			return key;
		}

		@Override
		public V getValue() {
			return val;
		}
		
		/**
		 * Equal to {@link #getValue()}
		 */
		public V val() {
			return val;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Method returns true is specified map contains the key,
		 * and the value is equal to the one returned by the map.
		 */
		@Override
		public boolean test(Map<K, V> t) {
			return t.containsKey(key) && Objects.equals(val, t.get(key));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(getKey(), getValue());
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj.getClass() == getClass()) {
				UberMaps.Entry<?, ?> that = (UberMaps.Entry<?, ?>) obj;
				return Objects.equals(getKey(), that.getKey())
					&& Objects.equals(getValue(), that.getValue());
			}
			return false;
		}
		
		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.addValue(getKey() + "=" + getValue())
					.toString();
		}
		
		@Override
		public Map.Entry<K, V> clone() {
			return new Entry<>(key, val);
		}
		
		public static <K,V> Function<Entry<K, V>, Entry<V, K>> swap() {
			return e -> entry(e.val, e.key);
		}
	}
}
