package com.whaka.util.function;

import static com.whaka.util.UberMaps.*;
import static com.whaka.util.UberPredicates.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.whaka.util.UberMaps;
import com.whaka.util.UberMaps.Entry;

public class MapStream<K,V> implements Stream<UberMaps.Entry<K, V>> {

	private final Stream<UberMaps.Entry<K, V>> actual;
	
	public MapStream(Map<K, V> map) {
		this(map.entrySet().stream());
	}
	
	public MapStream(Stream<? extends Map.Entry<K, V>> actual) {
		this.actual = actual.map(UberMaps::entry);
	}

	private Stream<UberMaps.Entry<K, V>> getActual() {
		return actual;
	}

	@Override
	public Iterator<UberMaps.Entry<K, V>> iterator() {
		return getActual().iterator();
	}

	@Override
	public Spliterator<UberMaps.Entry<K, V>> spliterator() {
		return getActual().spliterator();
	}

	@Override
	public boolean isParallel() {
		return getActual().isParallel();
	}

	@Override
	public MapStream<K, V> sequential() {
		return new MapStream<>(getActual().sequential());
	}

	@Override
	public MapStream<K, V> parallel() {
		return new MapStream<>(getActual().parallel());
	}

	@Override
	public MapStream<K, V> unordered() {
		return new MapStream<>(getActual().unordered());
	}

	@Override
	public MapStream<K, V> onClose(Runnable closeHandler) {
		return new MapStream<>(getActual().onClose(closeHandler));
	}

	@Override
	public void close() {
		getActual().close();
	}

	@Override
	public MapStream<K, V> filter(Predicate<? super UberMaps.Entry<K, V>> predicate) {
		return new MapStream<>(getActual().filter(predicate));
	}
	
	/**
	 * Each entry in the stream are matched as key/value pair against the specified predicate.
	 * Only the successfully matched entries are retained in the stream.
	 */
	public MapStream<K, V> filter(BiPredicate<K, V> predicate) {
		return filter(e -> predicate.test(e.key, e.val));
	}
	
	/**
	 * Filter out only the entries where key matches specified predicate.
	 */
	public MapStream<K, V> filterKey(Predicate<? super K> keyPredicate) {
		return filter(e -> keyPredicate.test(e.key));
	}
	
	/**
	 * Filter out only the entries where value matches specified predicate.
	 */
	public MapStream<K, V> filterValue(Predicate<? super V> valPredicate) {
		return filter(e -> valPredicate.test(e.val));
	}
	
	/**
	 * All the entries that <b>do match</b> specified predicate are removed from the stream.
	 */
	public MapStream<K, V> drop(Predicate<? super UberMaps.Entry<K, V>> predicate) {
		return filter(not(predicate));
	}
	
	/**
	 * Each entry in the stream are matched as key/value pair against the specified predicate.
	 * All the successfully matched entries are removed from the stream.
	 */
	public MapStream<K, V> drop(BiPredicate<K, V> predicate) {
		return drop(e -> predicate.test(e.key, e.val));
	}
	
	/**
	 * All the entries where key matches specified predicate are removed from the stream.
	 */
	public MapStream<K, V> dropKey(Predicate<? super K> keyPredicate) {
		return filterKey(not(keyPredicate));
	}
	
	/**
	 * All the entries where value matches specified predicate are removed from the stream.
	 */
	public MapStream<K, V> dropValue(Predicate<? super V> valPredicate) {
		return filterValue(not(valPredicate));
	}

	@Override
	public <R> UberStream<R> map(Function<? super UberMaps.Entry<K, V>, ? extends R> mapper) {
		return new UberStream<>(getActual().map(mapper));
	}
	
	/**
	 * Each entry in the stream is mapped by the specified function into another entry.
	 */
	public <K2, V2> MapStream<K2, V2> mapEntry(Function<? super UberMaps.Entry<K, V>, ? extends Map.Entry<K2, V2>> mapper) {
		return new MapStream<>(getActual().map(mapper));
	}
	
	/**
	 * Each key/value pair in the stream is mapped by the specified BiFunction into another entry.
	 */
	public <K2, V2> MapStream<K2, V2> mapEntry(BiFunction<K, V, ? extends Map.Entry<K2, V2>> mapper) {
		return mapEntry(e -> mapper.apply(e.key, e.val));
	}
	
	/**
	 * For each entry in the stream - key is mapped by the specified key mapper function, and value is mapped
	 * by the specified value mapper function.
	 */
	public <K2, V2> MapStream<K2, V2> mapEntry(Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valMapper) {
		return mapEntry((k,v) -> entry(keyMapper.apply(k), valMapper.apply(v)));
	}
	
	/**
	 * For each entry in the stream - key is mapped by the specified key mapper function, value is untouched.
	 */
	public <K2> MapStream<K2, V> mapKey(Function<? super K, ? extends K2> keyMapper) {
		return mapEntry(keyMapper, Function.identity());
	}
	
	/**
	 * For each entry in the stream - key is untouched, and value is mapped by the specified value mapper function.
	 */
	public <V2> MapStream<K, V2> mapValue(Function<? super V, ? extends V2> valMapper) {
		return mapEntry(Function.identity(), valMapper);
	}
	
	@Override
	public IntStream mapToInt(ToIntFunction<? super UberMaps.Entry<K, V>> mapper) {
		return getActual().mapToInt(mapper);
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super UberMaps.Entry<K, V>> mapper) {
		return null;
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super UberMaps.Entry<K, V>> mapper) {
		return null;
	}

	@Override
	public <R> UberStream<R> flatMap(Function<? super UberMaps.Entry<K, V>, ? extends Stream<? extends R>> mapper) {
		return new UberStream<>(getActual().flatMap(mapper));
	}
	
	/**
	 * Each entry in the stream is mapped by the specified function into another map. Then each produced map
	 * is flattened into a stream of entries, and they are combined into a single resulting stream.
	 */
	public <K2, V2> MapStream<K2, V2> flatMapEntry(Function<? super UberMaps.Entry<K, V>, ? extends Map<K2, V2>> mapper) {
		return new MapStream<>(getActual().flatMap(e -> mapper.apply(e).entrySet().stream()));
	}
	
	/**
	 * For each entry in the stream - key is mapped by the specified function into a collection.
	 * Then result stream is mapped into stream of map entries where keys are stream elements, and values are
	 * the value of the original key. For example:
	 * <pre>
	 * 	Map<Integer, Integer> map = {1=10, 2=20};
	 * 	Map<Integer, Integer> map2 = new MapStream(map).flatMapKey(i -> asList(i, i * 10)).collect();
	 * 	System.out.println(map2); // {1=10, 10=10, 2=20, 20=20}
	 * </pre>
	 */
	public <K2> MapStream<K2, V> flatMapKey(Function<? super K, ? extends Collection<? extends K2>> mapper) {
		return flatMapEntry(e -> new MapStream<>(mapper.apply(e.key).stream().map(k -> UberMaps.<K2,V>entry(k, e.val))).toMap());
	}
	
	@Override
	public IntStream flatMapToInt(Function<? super UberMaps.Entry<K, V>, ? extends IntStream> mapper) {
		return getActual().flatMapToInt(mapper);
	}

	@Override
	public LongStream flatMapToLong(Function<? super UberMaps.Entry<K, V>, ? extends LongStream> mapper) {
		return getActual().flatMapToLong(mapper);
	}

	@Override
	public DoubleStream flatMapToDouble(Function<? super UberMaps.Entry<K, V>, ? extends DoubleStream> mapper) {
		return getActual().flatMapToDouble(mapper);
	}

	@Override
	public MapStream<K, V> distinct() {
		return new MapStream<>(getActual().distinct());
	}
	
	/**
	 * Only the entries with unique value are preserved in the stream.
	 */
	public MapStream<K, V> distinctValues() {
		Set<V> set = new HashSet<>();
		return filterValue(set::add);
	}
	
	@Override
	public MapStream<K, V> sorted() {
		return new MapStream<>(getActual().sorted());
	}

	@Override
	public MapStream<K, V> sorted(Comparator<? super UberMaps.Entry<K, V>> comparator) {
		return new MapStream<>(getActual().sorted(comparator));
	}
	
	/**
	 * All entries in the stream are sorted by comparing keys using specified comparator.
	 */
	public MapStream<K, V> sortedKeys(Comparator<? super K> comparator) {
		return sorted(Map.Entry.comparingByKey(comparator));
	}
	
	/**
	 * All entries in the stream are sorted by comparing values using specified comparator.
	 */
	public MapStream<K, V> sortedValues(Comparator<? super V> comparator) {
		return sorted(Map.Entry.comparingByValue(comparator));
	}

	@Override
	public MapStream<K, V> peek(Consumer<? super UberMaps.Entry<K, V>> action) {
		return new MapStream<>(getActual().peek(action));
	}
	
	/**
	 * Specified consumer are called for each key in the stream.
	 */
	public MapStream<K, V> peekKeys(Consumer<? super K> action) {
		return peek(e -> action.accept(e.key));
	}
	
	/**
	 * Specified consumer are called for each value in the stream.
	 */
	public MapStream<K, V> peekValues(Consumer<? super V> action) {
		return peek(e -> action.accept(e.val));
	}

	@Override
	public MapStream<K, V> limit(long maxSize) {
		return new MapStream<>(getActual().limit(maxSize));
	}

	@Override
	public MapStream<K, V> skip(long n) {
		return new MapStream<>(getActual().skip(n));
	}

	@Override
	public void forEach(Consumer<? super UberMaps.Entry<K, V>> action) {
		getActual().forEach(action);
	}
	
	/**
	 * Each entry in the stream are passed as key/value pair into the specified consumer.
	 */
	public void forEach(BiConsumer<K, V> action) {
		forEach(e -> action.accept(e.key, e.val));
	}
	
	/**
	 * Specified consumer are called for each key in the stream.
	 */
	public void forEachKey(Consumer<? super K> action) {
		getActual().forEach(e -> action.accept(e.key));
	}
	
	/**
	 * Specified consumer are called for each value in the stream.
	 */
	public void forEachValue(Consumer<? super V> action) {
		getActual().forEach(e -> action.accept(e.val));
	}

	@Override
	public void forEachOrdered(Consumer<? super UberMaps.Entry<K, V>> action) {
		getActual().forEachOrdered(action);
	}

	/**
	 * Map all entries in the stream by {@link Entry#getKey()} method.
	 */
	public UberStream<K> toKeys() {
		return map(Entry::getKey);
	}
	
	/**
	 * Map all entries in the stream by {@link Entry#getValue()} method.
	 */
	public UberStream<V> toValues() {
		return map(Entry::getValue);
	}
	
	@Override
	public Object[] toArray() {
		return getActual().toArray();
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		return getActual().toArray(generator);
	}

	@Override
	public UberMaps.Entry<K, V> reduce(UberMaps.Entry<K, V> identity, BinaryOperator<UberMaps.Entry<K, V>> accumulator) {
		return getActual().reduce(identity, accumulator);
	}

	@Override
	public Optional<UberMaps.Entry<K, V>> reduce(BinaryOperator<UberMaps.Entry<K, V>> accumulator) {
		return getActual().reduce(accumulator);
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super UberMaps.Entry<K, V>, U> accumulator, BinaryOperator<U> combiner) {
		return getActual().reduce(identity, accumulator, combiner);
	}

	/**
	 * Collect stream back to the map form. No additional parameters required, for map is built from the entries.
	 * @see MapCollectors#toMap()
	 */
	public Map<K, V> toMap() {
		return getActual().collect(MapCollectors.toMap());
	}
	
	/**
	 * Collect stream to the linked map. No additional parameters required, for map is built from the entries.
	 * @see MapCollectors#toMap()
	 */
	public Map<K, V> toLinkedMap() {
		return to(LinkedHashMap::new);
	}
	
	/**
	 * Collect stream into a map provided by the specified supplier.
	 * No additional parameters required, for map is built from the entries.
	 * @see MapCollectors#toMap()
	 */
	public <M extends Map<K, V>> M to(Supplier<M> mapSupplier) {
		return getActual().collect(MapCollectors.toMap(mapSupplier));
	}
	
	@Override
	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super UberMaps.Entry<K, V>> accumulator, BiConsumer<R, R> combiner) {
		return getActual().collect(supplier, accumulator, combiner);
	}

	@Override
	public <R, A> R collect(Collector<? super UberMaps.Entry<K, V>, A, R> collector) {
		return getActual().collect(collector);
	}

	@Override
	public Optional<UberMaps.Entry<K, V>> min(Comparator<? super UberMaps.Entry<K, V>> comparator) {
		return getActual().min(comparator);
	}

	@Override
	public Optional<UberMaps.Entry<K, V>> max(Comparator<? super UberMaps.Entry<K, V>> comparator) {
		return getActual().max(comparator);
	}

	@Override
	public long count() {
		return getActual().count();
	}

	@Override
	public boolean anyMatch(Predicate<? super UberMaps.Entry<K, V>> predicate) {
		return getActual().anyMatch(predicate);
	}

	@Override
	public boolean allMatch(Predicate<? super UberMaps.Entry<K, V>> predicate) {
		return getActual().allMatch(predicate);
	}

	@Override
	public boolean noneMatch(Predicate<? super UberMaps.Entry<K, V>> predicate) {
		return getActual().noneMatch(predicate);
	}

	@Override
	public Optional<UberMaps.Entry<K, V>> findFirst() {
		return getActual().findFirst();
	}

	@Override
	public Optional<UberMaps.Entry<K, V>> findAny() {
		return getActual().findAny();
	}
	
	/**
	 * Find first entry that matches specified predicate.
	 */
	public Optional<Entry<K, V>> find(Predicate<? super UberMaps.Entry<K, V>> predicate) {
		return filter(predicate).findFirst();
	}
	
	/**
	 * Find first entry where key matches specified predicate.
	 */
	public Optional<Entry<K, V>> findKey(Predicate<? super K> keyPredicate) {
		return filterKey(keyPredicate).findFirst();
	}
	
	/**
	 * Find first entry where key is equal to the specified object.
	 */
	public Optional<Entry<K, V>> findByKey(K key) {
		return findKey(Predicate.isEqual(key));
	}
}
