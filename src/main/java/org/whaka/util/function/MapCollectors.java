package org.whaka.util.function;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * <p>Class provides static factory methods to create {@link Collector} instances to transform streams into maps.
 * 
 * <p>Main differences from <code>{@link Collectors}.toMap</code> methods are:
 * <ul>
 * 	<li>This class doesn't use {@link Map#merge(Object, Object, java.util.function.BiFunction)} method and instead
 * performs all merging "manually", using "containsKey", "get", and "put". <b>Note!</b> This means that
 * both elements of a merging function might be nulls!
 * <br><br>
 * 	<li>Default merging function doesn't throw an exception. Instead it replaces old value with the new one.
 * (The same, as if "put" method would be used.) See: {@link #replacingMerger()}
 * </ul>
 * 
 * <p>Also there're collector factories specifically for stream of map entries: {@link #toMap()} or {@link #toMap(Supplier)}
 */
public class MapCollectors {

	private MapCollectors() {
	}
	
	/**
	 * Create new collector that will collect stream of map entries into a default map.
	 * No additional parameters are required, for map entries are naturally suitable for a map.
	 * 
	 * @see #toMap(Supplier)
	 */
	public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> toMap() {
		return toMap(Entry::getKey, Entry::getValue);
	}
	
	/**
	 * Create new collector that will collect stream of map entries into a map provided by the specified supplier.
	 * No additional parameters are required, for map entries are naturally suitable for a map.
	 * 
	 * @see #toMap()
	 */
	public static <K, V, M extends Map<K, V>> Collector<Map.Entry<K, V>, ?, M> toMap(Supplier<M> mapSupplier) {
		return toMap(Entry::getKey, Entry::getValue, mapSupplier);
	}
	
	/**
	 * Map each element of the a stream into key/value pair by specified functions.
	 * Then collect them into a default map.
	 * Any key collisions will be replaced with latter value.
	 * 
	 * @see #toMap(Function, Function, Supplier)
	 * @see #toMap(Function, Function, BinaryOperator)
	 * @see #toMap(Function, Function, BinaryOperator, Supplier)
	 */
	public static <T, K, V> Collector<T, ?, Map<K, V>> toMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper
			) {
		return toMap(keyMapper, valueMapper, replacingMerger());
	}
	
	/**
	 * Map each element of the a stream into key/value pair by specified functions.
	 * Then collect them into a default map.
	 * Any key collisions will be resolved by the specified merge function.
	 * 
	 * @see #toMap(Function, Function)
	 * @see #toMap(Function, Function, Supplier)
	 * @see #toMap(Function, Function, BinaryOperator, Supplier)
	 */
	public static <T, K, V> Collector<T, ?, Map<K, V>> toMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper,
			BinaryOperator<V> mergeFunction
			) {
		return toMap(keyMapper, valueMapper, mergeFunction, HashMap::new);
	}
	
	/**
	 * Map each element of the a stream into key/value pair by specified functions.
	 * Then collect them into a map provided by the specified supplier.
	 * Any key collisions will be replaced with latter value.
	 * 
	 * @see #toMap(Function, Function)
	 * @see #toMap(Function, Function, BinaryOperator)
	 * @see #toMap(Function, Function, BinaryOperator, Supplier)
	 */
	public static <T, K, V, M extends Map<K, V>> Collector<T, ?, M> toMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper,
			Supplier<M> mapSupplier
			) {
		return toMap(keyMapper, valueMapper, replacingMerger(), mapSupplier);
	}
	
	/**
	 * Map each element of the a stream into key/value pair by specified functions.
	 * Then collect them into a map provided by the specified supplier.
	 * Any key collisions will be resolved by the specified merge function.
	 * 
	 * @see #toMap(Function, Function)
	 * @see #toMap(Function, Function, Supplier)
	 * @see #toMap(Function, Function, BinaryOperator)
	 */
	public static <T, K, V, M extends Map<K, V>> Collector<T, ?, M> toMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper,
			BinaryOperator<V> mergeFunction,
			Supplier<M> mapSupplier
	) {
		BiConsumer<M, T> accumulator = (map, element) -> createMapInserter(map, mergeFunction)
				.accept(keyMapper.apply(element), valueMapper.apply(element));
		return Collector.of(mapSupplier, accumulator, mapMerger(mergeFunction));
	}
	
	private static <K, V, M extends Map<K,V>> BinaryOperator<M> mapMerger(BinaryOperator<V> mergeFunction) {
        return (m1, m2) -> {
            for (Map.Entry<K,V> e : m2.entrySet()) {
            	K key = e.getKey();
            	V val = e.getValue();
            	if (m1.containsKey(key))
            		val = mergeFunction.apply(m1.get(key), val);
            	m1.put(key, val);
            }
            return m1;
        };
    }
	
	private static <K,V> BiConsumer<K,V> createMapInserter(Map<K, V> map, BinaryOperator<V> mergeFunction) {
		return (key, val) -> {
			if (map.containsKey(key))
        		val = mergeFunction.apply(map.get(key), val);
        	map.put(key, val);
		};
	}
	
	/**
	 * Creates a binary operator that always returns second argument.
	 */
	public static <V> BinaryOperator<V> replacingMerger() {
		return (a,b) -> b;
	}
}
