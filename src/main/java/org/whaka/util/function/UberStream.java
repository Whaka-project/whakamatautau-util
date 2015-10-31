package org.whaka.util.function;

import static org.whaka.util.UberMaps.*;
import static org.whaka.util.UberPredicates.*;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.whaka.util.UberMaps;

/**
 * Proxy class for any {@link Stream}.
 * Class implements Stream interface, so it can be used as one.
 * Provides various additional useful methods.
 */
public class UberStream<T> implements Stream<T> {

	private final Stream<T> actual;
	
	public UberStream(Stream<T> actual) {
		this.actual = Objects.requireNonNull(actual, "Actual stream cannot be null!");
	}
	
	private Stream<T> getActual() {
		return actual;
	}

	@Override
	public Iterator<T> iterator() {
		return getActual().iterator();
	}

	@Override
	public Spliterator<T> spliterator() {
		return getActual().spliterator();
	}

	@Override
	public boolean isParallel() {
		return getActual().isParallel();
	}

	@Override
	public UberStream<T> sequential() {
		return new UberStream<>(getActual().sequential());
	}

	@Override
	public UberStream<T> parallel() {
		return new UberStream<>(getActual().parallel());
	}

	@Override
	public UberStream<T> unordered() {
		return new UberStream<>(getActual().unordered());
	}

	@Override
	public UberStream<T> onClose(Runnable closeHandler) {
		return new UberStream<>(getActual().onClose(closeHandler));
	}

	@Override
	public void close() {
		getActual().close();
	}

	@Override
	public UberStream<T> filter(Predicate<? super T> predicate) {
		return new UberStream<>(getActual().filter(predicate));
	}

	/**
	 * <p>Filter only instances of the specified class (or nulls) and cast them to the specified type.
	 * <pre>
	 * 	#filter(x -> x == null || filteredClass.isInstance(x))
	 * 		.map(x -> filteredClass.cast(x));
	 * </pre>
	 * 
	 * <b>Note:</b> <code>nulls</code> survive the filtering!
	 * Use separate filter or {@link #dropNulls()} to filter them out!
	 */
	public <R extends T> UberStream<R> filterByClass(Class<R> filteredClass) {
		return filter(anyOf(Objects::isNull, filteredClass::isInstance)).map(filteredClass::cast);
	}
	
	/**
	 * Filter out all elements that match specified predicate. Equal to:
	 * <pre>
	 * 	#filter(not(predicate));
	 * </pre>
	 */
	public UberStream<T> drop(Predicate<? super T> predicate) {
		return new UberStream<>(getActual().filter(not(predicate)));
	}
	
	/**
	 * Filter out all null elements. Equal to:
	 * <pre>
	 * 	#filter(Objects::nonNull);
	 * </pre>
	 */
	public UberStream<T> dropNulls() {
		return filter(Objects::nonNull);
	}
	
	/**
	 * Find first element matching specified predicate. Equal to:
	 * <pre>
	 * 	#filter(predicate).findFirst();
	 * </pre>
	 */
	public Optional<T> find(Predicate<? super T> predicate) {
		return filter(predicate).findFirst();
	}
	
	@Override
	public <R> UberStream<R> map(Function<? super T, ? extends R> mapper) {
		return new UberStream<>(getActual().map(mapper));
	}

	@Override
	public IntStream mapToInt(ToIntFunction<? super T> mapper) {
		return getActual().mapToInt(mapper);
	}
	
	@Override
	public LongStream mapToLong(ToLongFunction<? super T> mapper) {
		return getActual().mapToLong(mapper);
	}
	
	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
		return getActual().mapToDouble(mapper);
	}
	
	@Override
	public <R> UberStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		return new UberStream<>(getActual().flatMap(mapper));
	}
	
	/**
	 * Maps each element of the stream into collection and them flattens result stream. Equal to:
	 * <pre>
	 * 	#flatMap(e -> mapper.apply(e).stream())
	 * </pre>
	 */
	public <R> UberStream<R> flatMapCol(Function<? super T, ? extends Collection<? extends R>> mapper) {
		return new UberStream<>(getActual().flatMap(t -> mapper.apply(t).stream()));
	}
	
	/**
	 * Maps each element of the stream into array and them flattens result stream. Equal to:
	 * <pre>
	 * 	#flatMap(e -> Stream.of(mapper.apply(e)))
	 * </pre>
	 */
	public <R> UberStream<R> flatMapArr(Function<? super T, R[]> mapper) {
		return new UberStream<>(getActual().flatMap(t -> Stream.of(mapper.apply(t))));
	}

	@Override
	public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
		return getActual().flatMapToInt(mapper);
	}

	@Override
	public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
		return getActual().flatMapToLong(mapper);
	}

	@Override
	public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
		return getActual().flatMapToDouble(mapper);
	}
	
	public <V> UberStream<UberMaps.Entry<T, V>> zip(Function<T, V> mapper) {
		return map(t -> UberMaps.entry(t, mapper.apply(t)));
	}

	@Override
	public UberStream<T> distinct() {
		return new UberStream<>(getActual().distinct());
	}

	@Override
	public UberStream<T> sorted() {
		return new UberStream<>(getActual().sorted());
	}

	@Override
	public UberStream<T> sorted(Comparator<? super T> comparator) {
		return new UberStream<>(getActual().sorted(comparator));
	}

	@Override
	public UberStream<T> peek(Consumer<? super T> action) {
		return new UberStream<>(getActual().peek(action));
	}

	@Override
	public UberStream<T> limit(long maxSize) {
		return new UberStream<>(getActual().limit(maxSize));
	}

	@Override
	public UberStream<T> skip(long n) {
		return new UberStream<>(getActual().skip(n));
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		getActual().forEach(action);
	}

	@Override
	public void forEachOrdered(Consumer<? super T> action) {
		getActual().forEachOrdered(action);
	}

	@Override
	public Object[] toArray() {
		return getActual().toArray();
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		return getActual().toArray(generator);
	}
	
	@SuppressWarnings("unchecked")
	public <A> A[] toArray(Class<A> type) {
		return toArray(size -> (A[]) Array.newInstance(type, size));
	}

	@Override
	public T reduce(T identity, BinaryOperator<T> accumulator) {
		return getActual().reduce(identity, accumulator);
	}

	@Override
	public Optional<T> reduce(BinaryOperator<T> accumulator) {
		return getActual().reduce(accumulator);
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
		return getActual().reduce(identity, accumulator, combiner);
	}

	@Override
	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
		return getActual().collect(supplier, accumulator, combiner);
	}

	@Override
	public <R, A> R collect(Collector<? super T, A, R> collector) {
		return getActual().collect(collector);
	}
	
	/**
	 * Collect all elements of the stream into a collection provided by the specified factory.
	 */
	public <C extends Collection<T>> C to(Supplier<C> factory) {
		return collect(Collectors.toCollection(factory));
	}
	
	/**
	 * Collect all elements of the stream into a list.
	 */
	public List<T> toList() {
		return collect(Collectors.toList());
	}
	
	/**
	 * Collect all elements of the stream into a set.
	 */
	public Set<T> toSet() {
		return collect(Collectors.toSet());
	}
	
	/**
	 * <p>Specified function is applied to each element of the stream. Results are used as keys in the result map.
	 * Values are the elements themselves. Equal to:
	 * <pre>
	 * 	#collect(MapCollectors.toMap(keyFunction, e -> e));
	 * </pre>
	 * 
	 * <p><b>Note:</b> merges are resolved as "last one is to stay", so keys duplication is allowed.
	 * 
	 * @see MapCollectors#toMap(Function, Function)
	 */
	public <K> Map<K, T> toMap(Function<T, K> keyFunction) {
		return collect(MapCollectors.toMap(keyFunction, Function.identity()));
	}
	
	/**
	 * <p>Specified functions are applied to each element of the stream.
	 * Results are used as key/value pairs in the result map. Equal to:
	 * <pre>
	 * 	#collect(MapCollectors.toMap(keyFunction, valFunction));
	 * </pre>
	 * 
	 * <p><b>Note:</b> merges are resolved as "last one is to stay", so keys duplication is allowed.
	 * 
	 * @see MapCollectors#toMap(Function, Function)
	 */
	public <K,V> Map<K, V> toMap(Function<T, K> keyFunction, Function<T, V> valFunction) {
		return collect(MapCollectors.toMap(keyFunction, valFunction));
	}
	
	/**
	 * <p>Specified function is applied to each element of the stream. Results are used as keys in the result map.
	 * Values are the elements themselves. Map is created from the specified supplier. Equal to:
	 * <pre>
	 * 	#collect(MapCollectors.toMap(keyFunction, e -> e, mapSupplier));
	 * </pre>
	 * 
	 * <p><b>Note:</b> merges are resolved as "last one is to stay", so keys duplication is allowed.
	 * 
	 * @see MapCollectors#toMap(Function, Function, Supplier)
	 */
	public <K, M extends Map<K, T>> M toMap(Function<T, K> keyFunction, Supplier<M> mapSupplier) {
		return collect(MapCollectors.toMap(keyFunction, Function.identity(), mapSupplier));
	}
	
	/**
	 * <p>Specified functions are applied to each element of the stream.
	 * Results are used as key/value pairs in the result map.
	 * Map is created from the specified supplier. Equal to:
	 * <pre>
	 * 	#collect(MapCollectors.toMap(keyFunction, valFunction, mapSupplier));
	 * </pre>
	 * 
	 * <p><b>Note:</b> merges are resolved as "last one is to stay", so keys duplication is allowed.
	 * 
	 * @see MapCollectors#toMap(Function, Function, Supplier)
	 */
	public <K, V, M extends Map<K, V>> M toMap(Function<T, K> keyFunction, Function<T, V> valFunction, Supplier<M> mapSupplier) {
		return collect(MapCollectors.toMap(keyFunction, valFunction, mapSupplier));
	}
	
	/**
	 * Equal to calling {@link #toMap(Function)} and then constructing new {@link MapStream}.
	 */
	public <K> MapStream<K, T> toMapStream(Function<T, K> keyFunction) {
		return toMapStream(keyFunction, x -> x);
	}
	
	/**
	 * Equal to calling {@link #toMap(Function, Function)} and then constructing new {@link MapStream}.
	 */
	public <K, V> MapStream<K, V> toMapStream(Function<T, K> keyFunction, Function<T, V> valFunction) {
		return new MapStream<>(map(t -> entry(keyFunction.apply(t), valFunction.apply(t))));
	}
	
	/**
	 * All elements of the stream are mapped to string using {@link String#valueOf(Object)}. Then all elements
	 * are concatenated to string using specified delimiter. Equal to:
	 * <pre>
	 * 	#map(String::valueOf).collect(Collectors.joining(delimiter));
	 * </pre>
	 */
	public String join(CharSequence delimiter) {
		return join(delimiter, "", "");
	}
	
	/**
	 * All elements of the stream are mapped to string using {@link String#valueOf(Object)}. Then all elements
	 * are concatenated to string using specified delimiter, prefix, and suffix. Equal to:
	 * <pre>
	 * 	#map(String::valueOf).collect(Collectors.joining(delimiter, prefix, suffix));
	 * </pre>
	 */
	public String join(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
		return map(String::valueOf).collect(Collectors.joining(delimiter, prefix, suffix));
	}

	@Override
	public Optional<T> min(Comparator<? super T> comparator) {
		return getActual().min(comparator);
	}

	@Override
	public Optional<T> max(Comparator<? super T> comparator) {
		return getActual().max(comparator);
	}

	@Override
	public long count() {
		return getActual().count();
	}

	@Override
	public boolean anyMatch(Predicate<? super T> predicate) {
		return getActual().anyMatch(predicate);
	}

	@Override
	public boolean allMatch(Predicate<? super T> predicate) {
		return getActual().allMatch(predicate);
	}

	@Override
	public boolean noneMatch(Predicate<? super T> predicate) {
		return getActual().noneMatch(predicate);
	}

	@Override
	public Optional<T> findFirst() {
		return getActual().findFirst();
	}

	@Override
	public Optional<T> findAny() {
		return getActual().findAny();
	}
}
